package com.hirely;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("companies")
public class CompanyDoc {
    @Id public String id;
    @Indexed(unique=true) public String recruiterId;
    public String name,website,industry,size,location,description;
}
