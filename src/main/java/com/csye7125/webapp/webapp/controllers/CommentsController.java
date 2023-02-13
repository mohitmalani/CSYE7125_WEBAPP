package com.csye7125.webapp.webapp.controllers;

import com.csye7125.webapp.webapp.daos.CommentsDAO;
import com.csye7125.webapp.webapp.daos.TasksDAO;
import com.csye7125.webapp.webapp.models.comments.Comment;
import com.csye7125.webapp.webapp.models.tasks.Task;
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
public class CommentsController {
    CommentsDAO commentsDAO;
    TasksDAO tasksDAO;
    AuthenticationService authenticationService;
    Logger logger = LoggerFactory.getLogger(CommentsController.class);

    private CommentsController(CommentsDAO commentsDAO,
                               TasksDAO tasksDAO,
                               AuthenticationService authenticationService){
        this.commentsDAO = commentsDAO;
        this.tasksDAO = tasksDAO;
        this.authenticationService = authenticationService;
    }

    @GetMapping(path = "/v1/comments/task/{taskId}", produces = "application/json")
    public ResponseEntity<String> getComments(@RequestHeader HttpHeaders headers, @PathVariable long taskId) {
        logger.info("[CommentsController][getComments][Recieved Request] with params taskId:" + taskId);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[CommentsController][getComments]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        Optional<Task> task = tasksDAO.findById(taskId);
        if(task.isEmpty()){
            logger.error("[CommentsController][getComments]" + new JSONObject().put("message", String.format("Task with id %d does not exist", taskId)).toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Task with id %d does not exist", taskId)).toString());
        }
        Optional<List<Comment>> commentsList = commentsDAO.findByTaskAndUserId(task.get(), user.getId());
        JSONArray jsonArray = new JSONArray();
        if(commentsList.isPresent()){
            for(Comment comment: commentsList.get()){
                JSONObject commentObject = new JSONObject();
                commentObject.put("id", comment.getId());
                commentObject.put("comment", comment.getComment());
                commentObject.put("taskId", comment.getTask().getId());
                commentObject.put("createdAt", comment.getCreatedAt());
                commentObject.put("updatedAt", comment.getUpdatedAt());
                jsonArray.put(commentObject);
            }
        }
        return ResponseEntity.ok().body(jsonArray.toString());
    }

    @PatchMapping(path = "/v1/comments/{commentId}", produces = "application/json")
    public ResponseEntity<String> updateComment(@RequestHeader HttpHeaders headers, @PathVariable long commentId, @RequestBody String commentRequestBody) {
        logger.info("[CommentsController][getComments][Recieved Request] with params commentId:" + commentId + "with request body: " + commentRequestBody);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[CommentsController][getComments]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        Optional<Comment> comment = commentsDAO.findByIdAndUserId(commentId, user.getId());
        if(comment.isPresent()){
            JSONObject jsonObject = new JSONObject(commentRequestBody);
            if(jsonObject.has("comment")){
                Comment comment1 = comment.get();
                comment1.setComment(jsonObject.getString("comment"));
                commentsDAO.saveAndFlush(comment1);
            } else{
                return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("comment field not present")).toString());
            }
            return ResponseEntity.ok().build();
        } else{
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Comment with id %d does not exist", commentId)).toString());
        }
    }

    @PostMapping(path = "/v1/comments", produces = "application/json")
    public ResponseEntity<String> postComment(@RequestHeader HttpHeaders headers, @RequestBody String commentRequestBody) {
        logger.info("[RemindersController][postComment][Received Request] with body:" + commentRequestBody);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[CommentsController][postComment]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        String commentValue;
        Long taskId;
        try{
            JSONObject reqBody = new JSONObject(commentRequestBody);
            commentValue = reqBody.getString("comment");
            taskId = reqBody.getLong("taskId");
        } catch (JSONException e){
            e.printStackTrace();
            logger.error("[CommentsController][postComment]" + new JSONObject().put("message", "Illegal Request").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Illegal Request").toString());
        }

        Optional<Task> savedTask = tasksDAO.findById(taskId);
        if(savedTask.isEmpty()){
            logger.error("[CommentsController][postComment]" + new JSONObject().put("message", String.format("Task with id %d does not exist", taskId)).toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Task with id %d does not exist", taskId)).toString());
        }


        Comment commentToSave = new Comment();
        commentToSave.setComment(commentValue);
        commentToSave.setUserId(user.getId());

        commentToSave.setTask(savedTask.get());
        Comment persistedComment = commentsDAO.saveAndFlush(commentToSave);

        JSONObject commentObject = new JSONObject();
        commentObject.put("id", persistedComment.getId());
        commentObject.put("comment", persistedComment.getComment());
        commentObject.put("taskId", persistedComment.getTask().getId());
        commentObject.put("createdAt", persistedComment.getCreatedAt());
        commentObject.put("updatedAt", persistedComment.getUpdatedAt());

        return ResponseEntity.ok().body(commentObject.toString());
    }
}
