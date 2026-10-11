package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("education")
public class EducationDoc {
    @Id public String id;
    @Indexed public String seekerId;
    public String institution,degree,fieldOfStudy,location,startDate,endDate,description;
}
