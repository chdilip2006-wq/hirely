package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Document("seeker_profiles")
public class ProfileDoc {
    @Id public String id;
    public String location,headline,summary,skills,preferredLocations,preferredJobType,linkedinUrl,portfolioUrl,noticePeriod;
    public Integer experienceYears=0;
    public BigDecimal expectedSalary;
}
