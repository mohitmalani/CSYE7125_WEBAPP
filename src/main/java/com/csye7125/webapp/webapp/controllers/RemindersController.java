package com.csye7125.webapp.webapp.controllers;

import com.csye7125.webapp.webapp.models.reminders.Reminder;
import com.csye7125.webapp.webapp.models.users.User;
import com.csye7125.webapp.webapp.commons.AuthenticationService;
import com.csye7125.webapp.webapp.daos.RemindersDAO;
import com.csye7125.webapp.webapp.daos.TasksDAO;
import com.csye7125.webapp.webapp.models.tasks.Task;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@RestController
public class RemindersController {
    RemindersDAO remindersDAO;
    TasksDAO tasksDAO;
    AuthenticationService authenticationService;
    Logger logger = LoggerFactory.getLogger(RemindersController.class);

    private RemindersController(RemindersDAO remindersDAO,
                               TasksDAO tasksDAO, AuthenticationService authenticationService){
        this.remindersDAO = remindersDAO;
        this.tasksDAO = tasksDAO;
        this.authenticationService = authenticationService;
    }


    @GetMapping(path = "/v1/reminders/task/{taskId}", produces = "application/json")
    public ResponseEntity<String> getReminder(@RequestHeader HttpHeaders headers, @PathVariable long taskId) {
        logger.info("[RemindersController][getReminder][Recieved Request] with params taskId:" + taskId);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[RemindersController][getReminder]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        Optional<Task> task = tasksDAO.findById(taskId);
        if(task.isEmpty()){
            logger.error("[RemindersController][getReminder]" + new JSONObject().put("message", String.format("Task with id %d does not exist", taskId)).toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Task with id %d does not exist", taskId)).toString());
        }
        Optional<List<Reminder>> reminderList = remindersDAO.findByTaskAndUserId(task.get(), user.getId());
        JSONArray jsonArray = new JSONArray();
        if(reminderList.isPresent()){
            for(Reminder reminder: reminderList.get()){
                JSONObject reminderObject = new JSONObject();
                reminderObject.put("id", reminder.getId());
                reminderObject.put("reminder", reminder.getReminderDate());
                reminderObject.put("taskId", reminder.getTask().getId());
                reminderObject.put("createdAt", reminder.getCreatedAt());
                reminderObject.put("updatedAt", reminder.getUpdatedAt());
                jsonArray.put(reminderObject);
            }
        }
        return ResponseEntity.ok().body(jsonArray.toString());
    }

    @PatchMapping(path = "/v1/reminders/{reminderId}", produces = "application/json")
    public ResponseEntity<String> updateReminder(@RequestHeader HttpHeaders headers, @PathVariable long reminderId, @RequestBody String reminderRequestBody) {
        logger.info("[updateReminder][updateReminder][Received Request] with params reminderId:" + reminderId + " with request body:" + reminderRequestBody);
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[RemindersController][updateReminder]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        Optional<Reminder> reminder = remindersDAO.findByIdAndUserId(reminderId, user.getId());
        if(reminder.isPresent()){
            JSONObject jsonObject = new JSONObject(reminderRequestBody);
            if(jsonObject.has("reminder")){
                Reminder reminder1 = reminder.get();
                try{
                    reminder1.setReminderDate(OffsetDateTime.parse(jsonObject.getString("reminder")));
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error("[RemindersController][updateReminder]" + new JSONObject().put("message","Reminder value should be of the form yyyy-MM-dd'T'HH:mm:ss'Z'").toString());
                    return ResponseEntity.badRequest().body(new JSONObject().put("message", "Reminder value should be of the form yyyy-MM-dd'T'HH:mm:ss'Z'").toString());
                }
                remindersDAO.saveAndFlush(reminder1);
            } else{
                logger.error("[RemindersController][updateReminder]" + new JSONObject().put("message", String.format("reminder field not present")).toString());
                return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("reminder field not present")).toString());
            }
            return ResponseEntity.ok().build();
        } else{
            logger.error("[RemindersController][updateReminder]" + new JSONObject().put("message", String.format("Reminder with id %d does not exist", reminderId)).toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Reminder with id %d does not exist", reminderId)).toString());
        }
    }

    @PostMapping(path = "/v1/reminders", produces = "application/json")
    public ResponseEntity<String> postReminder(@RequestHeader HttpHeaders headers, @RequestBody String commentRequestBody) {
        logger.info("[RemindersController][postReminder][Recieved Request] with request body: " +  commentRequestBody);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[RemindersController][postReminder]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        OffsetDateTime reminder;
        Long taskId;
        try{
            JSONObject reqBody = new JSONObject(commentRequestBody);
            reminder =  OffsetDateTime.parse(reqBody.getString("reminder"));
            taskId = reqBody.getLong("taskId");
        } catch (JSONException e){
            e.printStackTrace();
            logger.error("[RemindersController][postReminder]" + new JSONObject().put("message", "Illegal Request").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Illegal Request").toString());
        } catch (DateTimeParseException e){
            e.printStackTrace();
            logger.error("[RemindersController][postReminder]" + new JSONObject().put("message", "Reminder value should be of the form yyyy-MM-dd'T'HH:mm:ss'Z'").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Reminder value should be of the form yyyy-MM-dd'T'HH:mm:ss'Z'").toString());
        }

        Optional<Task> savedTask = tasksDAO.findById(taskId);

        if(savedTask.isEmpty()){
            logger.error("[RemindersController][postReminder]" + new JSONObject().put("message", String.format("Task with id %d does not exist", taskId)).toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Task with id %d does not exist", taskId)).toString());
        }

        Optional<List<Reminder>> reminderList = remindersDAO.findByTaskAndUserId(savedTask.get(), user.getId());
        if(reminderList.isPresent() && reminderList.get().size() == 5){
            logger.error("[RemindersController][postReminder]" + new JSONObject().put("message", String.format("Only maximum of 5 reminders allowed")).toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Only maximum of 5 reminders allowed")).toString());
        }

        Reminder reminderToSave = new Reminder();
        reminderToSave.setReminderDate(reminder);
        reminderToSave.setUserId(user.getId());

        reminderToSave.setTask(savedTask.get());
        Reminder persistedReminder = remindersDAO.saveAndFlush(reminderToSave);

        JSONObject reminderObject = new JSONObject();
        reminderObject.put("id", persistedReminder.getId());
        reminderObject.put("reminder", persistedReminder.getReminderDate().toString());
        reminderObject.put("taskId", persistedReminder.getTask().getId());
        reminderObject.put("createdAt", persistedReminder.getCreatedAt());
        reminderObject.put("updatedAt", persistedReminder.getUpdatedAt());

        return ResponseEntity.ok().body(reminderObject.toString());
    }
}
