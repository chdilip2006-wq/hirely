package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("saved_jobs")
@CompoundIndex(name="uq_saved_job",def="{'userId':1,'jobId':1}",unique=true)
public class SavedJobDoc {
    @Id public String id;
    public String userId,jobId;
    public Instant createdAt=Instant.now();
}
