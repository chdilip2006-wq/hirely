package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("resumes")
public class ResumeDoc {
    @Id public String id;
    @Indexed(unique=true) public String seekerId;
    public String storageKey,originalName,contentType;
    public long sizeBytes;
    public Instant uploadedAt=Instant.now();
}
