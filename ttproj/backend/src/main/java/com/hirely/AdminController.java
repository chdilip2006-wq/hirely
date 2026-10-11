package com.hirely;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final MongoTemplate mongo;
    private final Path storage;
    public AdminController(MongoTemplate mongo,@Value("${app.storage-dir}") String storage){this.mongo=mongo;this.storage=Paths.get(storage).toAbsolutePath().normalize();}
    @GetMapping("/users") List<Map<String,Object>> users(Authentication auth){admin(auth);return mongo.findAll(UserDoc.class).stream().map(u->map("id",u.id,"fullName",u.fullName,"email",u.email,"role",u.role,"active",u.active,"createdAt",u.createdAt)).toList();}
    @PatchMapping("/users/{id}/active") Map<String,Object> active(@PathVariable String id,@RequestBody ActiveForm form,Authentication auth){admin(auth);UserDoc user=mongo.findById(id,UserDoc.class);if(user==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND);if(user.role.equals("ADMIN")&&!form.active&&mongo.count(Query.query(Criteria.where("role").is("ADMIN").and("active").is(true)),UserDoc.class)<=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"At least one active administrator must remain.");user.active=form.active;mongo.save(user);return map("id",user.id,"active",user.active);}
    @GetMapping("/jobs") List<Map<String,Object>> jobs(Authentication auth){admin(auth);return mongo.find(Query.query(new Criteria()).with(Sort.by(Sort.Direction.DESC,"createdAt")),JobDoc.class).stream().map(j->{CompanyDoc company=mongo.findById(j.companyId,CompanyDoc.class);return map("id",j.id,"title",j.title,"company",company==null?"":company.name,"location",j.location,"status",j.status,"recruiterId",j.recruiterId);}).toList();}
    @PatchMapping("/jobs/{id}/status") Map<String,Object> jobStatus(@PathVariable String id,@RequestBody StatusForm form,Authentication auth){admin(auth);if(form.status==null||!Set.of("DRAFT","PUBLISHED","CLOSED").contains(form.status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);JobDoc job=mongo.findById(id,JobDoc.class);if(job==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND);job.status=form.status;mongo.save(job);return map("id",job.id,"status",job.status);}
    @DeleteMapping("/jobs/{id}") ResponseEntity<Void> deleteJob(@PathVariable String id,Authentication auth)throws IOException{admin(auth);JobDoc job=mongo.findById(id,JobDoc.class);if(job==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND);List<ApplicationDoc> apps=mongo.find(Query.query(Criteria.where("jobId").is(id)),ApplicationDoc.class);List<String> appIds=apps.stream().map(a->a.id).toList();if(!appIds.isEmpty())mongo.remove(Query.query(Criteria.where("applicationId").in(appIds)),NotificationDoc.class);mongo.remove(Query.query(Criteria.where("jobId").is(id)),ApplicationDoc.class);mongo.remove(Query.query(Criteria.where("jobId").is(id)),SavedJobDoc.class);mongo.remove(job);for(ApplicationDoc app:apps)if(app.resumeStorageKey!=null){Path file=storage.resolve(app.resumeStorageKey).normalize();if(file.startsWith(storage))Files.deleteIfExists(file);}return ResponseEntity.noContent().build();}
    private void admin(Authentication auth){if(auth==null||auth.getAuthorities().stream().noneMatch(a->a.getAuthority().equals("ROLE_ADMIN")))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator access is required.");UserDoc u=mongo.findOne(Query.query(Criteria.where("email").is(auth.getName())),UserDoc.class);if(u==null||!u.active||!u.role.equals("ADMIN"))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator access is required.");}
    private Map<String,Object> map(Object... values){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<values.length;i+=2)m.put((String)values[i],values[i+1]);return m;}
    record ActiveForm(boolean active){}
    record StatusForm(String status){}
}
