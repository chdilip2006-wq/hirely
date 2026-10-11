package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("notifications")
public class NotificationDoc {
    @Id public String id;
    @Indexed public String userId;
    public String applicationId,message;
    public boolean read;
    @Indexed public Instant createdAt=Instant.now();
}
