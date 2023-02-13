package com.csye7125.webapp.webapp.controllers;

import com.csye7125.webapp.webapp.daos.TaskListDAO;
import com.csye7125.webapp.webapp.models.taskLists.TaskList;
import com.csye7125.webapp.webapp.models.users.User;
import com.csye7125.webapp.webapp.commons.AuthenticationService;
import com.csye7125.webapp.webapp.daos.UserDAO;
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
public class TaskListController {

    TaskListDAO taskListDAO;
    UserDAO userDAO;
    AuthenticationService authenticationService;
    Logger logger = LoggerFactory.getLogger(TaskListController.class);

    private TaskListController(TaskListDAO taskListDAO, UserDAO userDAO, AuthenticationService authenticationService){

        this.taskListDAO = taskListDAO;
        this.userDAO = userDAO;
        this.authenticationService = authenticationService;
    }

    @GetMapping(path = "/v1/lists", produces = "application/json")
    public ResponseEntity<String> getAllLists(@RequestHeader HttpHeaders headers) {

        logger.info("[TaskListController][getALLLists][Recieved Request]");
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TaskListController][getAllTasks]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        Optional<List<TaskList>> taskListList = taskListDAO.findAllByUserId(user.getId());
        JSONArray jsonArray = new JSONArray();
        if(taskListList.isPresent()){
            for(TaskList list: taskListList.get()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", list.getId());
                jsonObject.put("name", list.getName());
                jsonObject.put("createdAt", list.getCreatedAt());
                jsonObject.put("updatedAt", list.getUpdatedAt());
                jsonArray.put(jsonObject);
            }
        }
        return ResponseEntity.ok().body(jsonArray.toString());
    }

    @PostMapping(path = "/v1/lists", produces = "application/json")
    public ResponseEntity<String> createList(@RequestHeader HttpHeaders headers, @RequestBody
            String taskListRequestBody) {
        logger.info("[TaskListController][createList][Recieved Request] with body" + taskListRequestBody);
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TaskListController][createList]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();


        JSONObject requestBody = new JSONObject(taskListRequestBody);
        Optional<TaskList> savedList = taskListDAO.findTaskListByName(requestBody.getString("name"));
        if(savedList.isPresent()){
            logger.error("[TaskListController][createList]" + new JSONObject().put("message", "List name already exists").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "List name already exists").toString());
        }
        try{
            TaskList taskList = new TaskList(requestBody.getString("name"));
            taskList.setUserId(user.getId());
            TaskList persistedTaskList = taskListDAO.saveAndFlush(taskList);
            JSONObject returnListObj = new JSONObject();
            returnListObj.put("id", persistedTaskList.getId());
            returnListObj.put("name", persistedTaskList.getName());
            returnListObj.put("createdAt", persistedTaskList.getCreatedAt());
            returnListObj.put("updatedAt", persistedTaskList.getUpdatedAt());
            return ResponseEntity.ok().body(returnListObj.toString());
        } catch(JSONException e){
            e.printStackTrace();
            logger.error("[TaskListController][createList]" + new JSONObject().put("message", "Request body is not valid").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Request body is not valid").toString());
        }
    }

    @PatchMapping(path = "/v1/lists/{listId}", produces = "application/json")
    public ResponseEntity<String> updateList(@RequestHeader HttpHeaders headers, @PathVariable Long listId, @RequestBody
    String taskListRequestBody) {
        logger.info("[TaskListController][updateList][Recieved Request] with body" + taskListRequestBody);
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TaskListController][updateList]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        JSONObject requestBody = new JSONObject(taskListRequestBody);
        String name;
        try{
            String updateListName = requestBody.getString("name");
            Optional<TaskList> taskList = taskListDAO.findByIdAndUserId(listId, user.getId());
            if(taskList.isPresent()){
                TaskList taskList1 = taskList.get();
                taskList1.setName(updateListName);
                TaskList persistedTaskList = taskListDAO.saveAndFlush(taskList1);
                JSONObject returnListObj = new JSONObject();
                returnListObj.put("id", persistedTaskList.getId());
                returnListObj.put("name", persistedTaskList.getName());
                returnListObj.put("createdAt", persistedTaskList.getCreatedAt());
                returnListObj.put("updatedAt", persistedTaskList.getUpdatedAt());
                return ResponseEntity.ok().body(returnListObj.toString());
            } else{
                logger.error("[TaskListController][updateList]" + new JSONObject().put("message", String.format("List %s does not exist", listId)).toString());
                return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("List %s does not exist", listId)).toString());
            }

        }
        catch(JSONException e){
            e.printStackTrace();
            logger.error("[TaskListController][updateList]" + new JSONObject().put("message", "Request body is not valid").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Request body is not valid").toString());
        }

    }

}
