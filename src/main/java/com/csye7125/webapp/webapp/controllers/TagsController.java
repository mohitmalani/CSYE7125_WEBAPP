package com.csye7125.webapp.webapp.controllers;

import com.csye7125.webapp.webapp.daos.TagsDAO;
import com.csye7125.webapp.webapp.models.tags.Tag;
import com.csye7125.webapp.webapp.models.users.User;
import com.csye7125.webapp.webapp.commons.AuthenticationService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TagsController {
    TagsDAO tagsDAO;
    AuthenticationService authenticationService;
    Logger logger = LoggerFactory.getLogger(TagsController.class);

    private TagsController(TagsDAO tagsDAO, AuthenticationService authenticationService){

        this.tagsDAO = tagsDAO;
        this.authenticationService = authenticationService;
    }

    @PostMapping(path = "/v1/tags", produces = "application/json")
    public ResponseEntity<String> postTag(@RequestHeader HttpHeaders headers, @RequestBody String tagRequestBody) {
        logger.info("[TagsController][postTag][Recieved Request] with request body:" + tagRequestBody);


        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TasksController][getAllTasks]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        String tagName;
        try{
            JSONObject jsonObject = new JSONObject(tagRequestBody);
            tagName = jsonObject.getString("name");
        } catch (JSONException e){
            e.printStackTrace();
            logger.error("[TasksController][getAllTasks]" + new JSONObject().put("message","Tag name absent in request").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Tag name absent in request").toString());
        }

        Optional<Tag> savedTag = tagsDAO.findTagByNameAndUserId(tagName, user.getId());
        if(savedTag.isPresent()){
            logger.error("[TasksController][getAllTasks]" + new JSONObject().put("message","Tag name already exists").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Tag name already exists").toString());
        }

        Tag tag = new Tag();
        tag.setName(tagName);
        tag.setUserId(user.getId());
        Tag persistedTag = tagsDAO.saveAndFlush(tag);
        JSONObject tagObject = new JSONObject();
        tagObject.put("id", persistedTag.getId());
        tagObject.put("dueDate", persistedTag.getName());
        tagObject.put("createdAt", persistedTag.getCreatedAt());
        tagObject.put("updatedAt", persistedTag.getUpdatedAt());
        return ResponseEntity.ok().body(tagObject.toString());
    }


    @GetMapping(path = "/v1/tags", produces = "application/json")
    public ResponseEntity<String> getAllTags(@RequestHeader HttpHeaders headers) {
        logger.info("[TagsController][getAllTags][Received Request]");
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TasksController][getAllTasks]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();



        Optional<List<Tag>> tagsList = tagsDAO.findAllByUserId(user.getId());
        if(tagsList.isEmpty()){
            logger.error("[TasksController][getAllTags]" + new JSONObject().put("message", "Bad request").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Bad request").toString());
        }

        JSONArray jsonArray = new JSONArray();
        for(Tag tag: tagsList.get()){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", tag.getId());
            jsonObject.put("dueDate", tag.getName());
            jsonObject.put("createdAt", tag.getCreatedAt());
            jsonObject.put("updatedAt", tag.getUpdatedAt());
            jsonArray.put(jsonObject);
        }

        return ResponseEntity.ok().body(jsonArray.toString());
    }

}
