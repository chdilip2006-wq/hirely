package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("applications")
@CompoundIndex(name="uq_application_job_seeker",def="{'jobId':1,'seekerId':1}",unique=true)
public class ApplicationDoc {
    @Id public String id;
    @Indexed public String jobId;
    @Indexed public String seekerId;
    public String coverLetter,status="SUBMITTED";
    public String applicantName,applicantEmail,applicantPhone,resumeName;
    public String resumeStorageKey,resumeContentType;
    public Instant createdAt=Instant.now();
}
