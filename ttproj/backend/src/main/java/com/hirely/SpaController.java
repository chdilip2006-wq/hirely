package com.hirely;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class SpaController {
    @GetMapping({"/{path:[^.]*}", "/{path:[^.]*}/{subpath:[^.]*}"})
    public String forwardToFrontend(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.equals("/api") || path.startsWith("/api/")
                || path.equals("/actuator") || path.startsWith("/actuator/")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "forward:/index.html";
    }
}
