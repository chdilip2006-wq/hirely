package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.Instant;

@Document("jobs")
public class JobDoc {
    @Id public String id;
    @Indexed public String companyId;
    @Indexed public String recruiterId;
    public String title,description,requirements,location,jobType,workplaceType,experienceLevel;
    public BigDecimal salaryMin,salaryMax;
    @Indexed public String status="DRAFT";
    @Indexed public Instant createdAt=Instant.now();
    public Instant updatedAt=Instant.now();
}
