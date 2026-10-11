package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("login_throttles")
public class LoginThrottleDoc {
    @Id public String id;
    public long failures;
    public Instant windowStart;
    public Instant lockedUntil;
    @Indexed(expireAfter="0s") public Instant expiresAt;
}
