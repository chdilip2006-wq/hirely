package com.hirely;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class PasswordEncodingTest {
    @Test void passwordsAreStoredAsOneWayBcryptHashes() {
        var encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("a-long-demo-password");
        assertNotEquals("a-long-demo-password", hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertTrue(encoder.matches("a-long-demo-password", hash));
        assertFalse(encoder.matches("another-password", hash));
    }
}
