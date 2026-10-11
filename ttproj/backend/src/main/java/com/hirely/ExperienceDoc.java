package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("experiences")
public class ExperienceDoc {
    @Id public String id;
    @Indexed public String seekerId;
    public String company,title,location,startDate,endDate,description;
    public boolean current;
}
