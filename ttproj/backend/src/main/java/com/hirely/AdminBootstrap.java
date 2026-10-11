package com.hirely;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final MongoTemplate mongo;
    private final PasswordEncoder encoder;
    private final String email,password;
    public AdminBootstrap(MongoTemplate mongo,PasswordEncoder encoder,@Value("${app.admin.email:}")String email,@Value("${app.admin.password:}")String password){this.mongo=mongo;this.encoder=encoder;this.email=email;this.password=password;}
    @Override public void run(ApplicationArguments args){
        if(email.isBlank()&&password.isBlank())return;
        if(email.isBlank()||password.length()<16)throw new IllegalStateException("Set both APP_ADMIN_EMAIL and an APP_ADMIN_PASSWORD with at least 16 characters.");
        String normalized=email.trim().toLowerCase(Locale.ROOT);UserDoc user=mongo.findOne(Query.query(Criteria.where("email").is(normalized)),UserDoc.class);
        if(user==null){user=new UserDoc();user.email=normalized;user.fullName="Portal Administrator";user.phone="0000000000";}
        user.role="ADMIN";user.active=true;user.passwordHash=encoder.encode(password);mongo.save(user);
    }
}
