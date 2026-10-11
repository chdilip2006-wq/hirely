package com.hirely;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import java.util.function.Supplier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService(MongoTemplate mongo) {
        return email -> {
            UserDoc user=mongo.findOne(Query.query(Criteria.where("email").is(email.toLowerCase())),UserDoc.class);
            if(user==null) throw new UsernameNotFoundException("Invalid credentials");
            return User.withUsername(user.email).password(user.passwordHash).roles(user.role).disabled(!user.active).build();
        };
    }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors-origin}") String origin) {
        CorsConfiguration cfg = new CorsConfiguration(); cfg.setAllowedOrigins(List.of(origin)); cfg.setAllowCredentials(true);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS")); cfg.setAllowedHeaders(List.of("Content-Type","X-XSRF-TOKEN"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", cfg); return source;
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("${server.servlet.session.cookie.secure:false}") boolean secureCookies) throws Exception {
        CookieCsrfTokenRepository csrfTokens=CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokens.setCookieCustomizer(cookie -> cookie.secure(secureCookies).sameSite("Lax"));
        http.cors(c -> {}).csrf(c -> c.csrfTokenRepository(csrfTokens).csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
            .authorizeHttpRequests(a -> a.requestMatchers("/api/auth/**", "/api/csrf", "/api/health", "/actuator/health", "/api/jobs", "/api/jobs/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll().anyRequest().authenticated())
            .formLogin(f -> f.disable()).httpBasic(b -> b.disable()).logout(l -> l.logoutUrl("/api/auth/logout").logoutSuccessHandler((req,res,auth)->res.setStatus(204)));
        return http.build();
    }

    // The SPA reads the raw token from /api/csrf and sends it in a request header.
    // Spring Security's XOR handler is retained for request attributes, while the
    // header is resolved as the raw token stored in the XSRF-TOKEN cookie.
    private static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            this.xor.handle(request, response, csrfToken);
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());
            return StringUtils.hasText(headerValue)
                ? this.plain.resolveCsrfTokenValue(request, csrfToken)
                : this.xor.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}
