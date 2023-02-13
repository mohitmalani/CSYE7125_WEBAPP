package com.csye7125.webapp.webapp.controllers;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.SearchTemplateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.csye7125.webapp.webapp.commons.AuthenticationService;
import com.csye7125.webapp.webapp.daos.TagsDAO;
import com.csye7125.webapp.webapp.daos.TaskListDAO;
import com.csye7125.webapp.webapp.daos.TasksDAO;
import com.csye7125.webapp.webapp.models.tags.Tag;
import com.csye7125.webapp.webapp.models.taskLists.TaskList;
import com.csye7125.webapp.webapp.models.tasks.Task;
import com.csye7125.webapp.webapp.models.tasks.TaskElastic;
import com.csye7125.webapp.webapp.models.tasks.TaskPriority;
import com.csye7125.webapp.webapp.models.tasks.TaskState;
import com.csye7125.webapp.webapp.models.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
public class TasksController {



    @Value(value = "${spring.elastic.index.tasks}")
    private String tasksIndex;

    ElasticsearchClient elasticsearchClient;

    @Value(value = "${spring.kafka.topic.task-update-topic}")
    private String taskUpdateTopicName;

    private KafkaTemplate<String, String> kafkaTemplate;

    ObjectMapper objectMapper;
    TasksDAO tasksDAO;
    TaskListDAO taskListDAO;
    TagsDAO tagsDAO;
    AuthenticationService authenticationService;
    RestTemplate restTemplate;
    Logger logger = LoggerFactory.getLogger(TasksController.class);


    private TasksController(TasksDAO tasksDAO,
                            TaskListDAO taskListDAO, TagsDAO tagsDAO,
                            AuthenticationService authenticationService,
                            KafkaTemplate<String, String> kafkaTemplate,
                           ElasticsearchClient elasticsearchClient
    ){
        this.tasksDAO = tasksDAO;
        this.taskListDAO = taskListDAO;
        this.tagsDAO = tagsDAO;
        this.authenticationService = authenticationService;
        this.kafkaTemplate = kafkaTemplate;
        this.elasticsearchClient = elasticsearchClient;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        restTemplate = new RestTemplate();
    }


    @GetMapping(path = "/v1/list/{listId}/tasks", produces = "application/json")
    public ResponseEntity<String> getAllTasks(@RequestHeader HttpHeaders headers, @PathVariable long listId) {
        logger.info("[TasksController][updateList][Recieved Request] param listId: " + listId);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TasksController][getAllTasks]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

           Optional<TaskList> taskList = taskListDAO.findTaskListById(listId);
            if(taskList.isEmpty()){
                logger.error("[TasksController][getAllTasks]" + new JSONObject().put("message", String.format("List with id %d is not a valid list", listId)).toString());
                return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("List with id %d is not a valid list", listId)).toString());
            }
            Optional<List<Task>> tasks = tasksDAO.findByTaskListAndUserId(taskList.get(), user.getId());
            JSONArray jsonArray = new JSONArray();
            if(tasks.isPresent()) {
                for (Task task : tasks.get()) {
                    JSONObject taskObject = new JSONObject();
                    if(task.getDueDate().compareTo(OffsetDateTime.now()) <= 0){
                        task.setTaskState(TaskState.OVERDUE);
                        tasksDAO.saveAndFlush(task);
                    }
                    taskObject.put("id", task.getId());
                    taskObject.put("name", task.getTask());
                    taskObject.put("dueDate", task.getDueDate());
                    taskObject.put("priority", task.getPriority());
                    taskObject.put("summary", task.getSummary());
                    taskObject.put("state", task.getTaskState());
                    taskObject.put("createdAt", task.getCreatedAt());
                    taskObject.put("updatedAt", task.getUpdatedAt());
                    jsonArray.put(taskObject);
                }
            }
            return ResponseEntity.ok().body(jsonArray.toString());
    }

    @GetMapping(path = "/v1/list/{listId}/task/{taskId}", produces = "application/json")
    public ResponseEntity<String> getTask(@RequestHeader HttpHeaders headers, @PathVariable long listId, @PathVariable long taskId) {
        logger.info("[TasksController][getTask][Recieved Request] param listId: " + listId + " taskId: ", taskId);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TasksController][getTask]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        Optional<TaskList> taskList = taskListDAO.findTaskListById(listId);
        if(taskList.isEmpty()){
            logger.error("[TasksController][getTask]" + new JSONObject().put("message", String.format("List with id %d is not a valid list", listId)).toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("List with id %d is not a valid list", listId)).toString());
        }
        Optional<Task> task = tasksDAO.findByTaskListAndUserIdAndId(taskList.get(), user.getId(), taskId);
        if(task.isPresent()) {
            Task taskVal = task.get();
            JSONObject taskObject = new JSONObject();
            taskObject.put("id", taskVal.getId());
                taskObject.put("name", taskVal.getTask());
                taskObject.put("dueDate", taskVal.getDueDate());
                taskObject.put("priority", taskVal.getPriority());
                taskObject.put("summary", taskVal.getSummary());
                taskObject.put("state", taskVal.getTaskState());
                taskObject.put("createdAt", taskVal.getCreatedAt());
                taskObject.put("updatedAt", taskVal.getUpdatedAt());
            return ResponseEntity.ok().body(taskObject.toString());
        } else{
            logger.error("[TasksController][getTask]" + new JSONObject().put("message", String.format("Task %d does not exist in List %d", taskId, listId)).toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Task %d does not exist in List %d", taskId, listId)).toString());
        }
    }

    @PostMapping(path = "/v1/tasks", produces = "application/json")
    public ResponseEntity<String> postTask(@RequestHeader HttpHeaders headers, @RequestBody String taskRequestBody) throws IOException {
        logger.info("[TasksController][postTask][Recieved Request] with body " + taskRequestBody);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TasksController][postTask]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        Task taskToSave = new Task();
        try{
            JSONObject requestBody = new JSONObject(taskRequestBody);
            taskToSave.setTask(requestBody.getString("name"));
            taskToSave.setSummary(requestBody.getString("summary"));
            taskToSave.setPriority(TaskPriority.valueOf(requestBody.getString("priority")));
            taskToSave.setDueDate(OffsetDateTime.parse(requestBody.getString("dueDate")));
            taskToSave.setUserId(user.getId());

            Optional<TaskList> savedList = taskListDAO.findById(requestBody.getLong("listId"));
            if(savedList.isEmpty()){
                logger.error("[TasksController][postTask]" + new JSONObject().put("message", String.format("List with id %d does not exist", requestBody.getLong("listId"))).toString());
                return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("List with id %d does not exist", requestBody.getLong("listId"))).toString());
            }
            taskToSave.setTaskList(savedList.get());
        } catch (JSONException | IllegalArgumentException e){
            e.printStackTrace();
            logger.error("[TasksController][postTask]" + new JSONObject().put("message", "Request body is not valid").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Request body is not valid").toString());
        }

        Task persistedTask = tasksDAO.saveAndFlush(taskToSave);


        JSONObject taskObject = new JSONObject();
        taskObject.put("id", persistedTask.getId());
        taskObject.put("name", persistedTask.getTask());
        taskObject.put("dueDate", persistedTask.getDueDate());
        taskObject.put("priority", persistedTask.getPriority());
        taskObject.put("summary", persistedTask.getSummary());
        taskObject.put("state", persistedTask.getTaskState());
        taskObject.put("createdAt", persistedTask.getCreatedAt());
        taskObject.put("updatedAt", persistedTask.getUpdatedAt());


       ListenableFuture<SendResult<String, String>> foo =  kafkaTemplate.send(taskUpdateTopicName, taskObject.toString());
        try {
            logger.info("[TasksController][postTask] Task message published to kafka with offset: " + foo.get().getRecordMetadata().offset());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().body(taskObject.toString());
    }

    @PatchMapping(path = "/v1/tasks/{taskId}", produces = "application/json")
    public ResponseEntity<String> updateTask(@RequestHeader HttpHeaders headers, @PathVariable long taskId,
    @RequestBody String taskRequestBody) {
        logger.info("[TasksController][updateTask][Recieved Request] with request body " + taskRequestBody + " and param taskId:" + taskId);
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TasksController][updateTask]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        try{
            Optional<Task> task = tasksDAO.findByIdAndUserId(taskId, user.getId());
            if(task.isPresent()){
                Task taskToSave = task.get();
                JSONObject requestBody = new JSONObject(taskRequestBody);
                if(requestBody.has("name")){
                    taskToSave.setTask(requestBody.getString("name"));
                }
                if(requestBody.has("summary")){
                    taskToSave.setSummary(requestBody.getString("summary"));
                }
                if(requestBody.has("priority")){
                    taskToSave.setPriority(TaskPriority.valueOf(requestBody.getString("priority")));
                }
                if(requestBody.has("dueDate")){
                    OffsetDateTime updatedDueDate = OffsetDateTime.parse(requestBody.getString("dueDate"));
                    if(updatedDueDate.compareTo(OffsetDateTime.now()) <= 0){
                        return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("New due date cannot be less than or equal to current date")).toString());
                    }
                    taskToSave.setDueDate(updatedDueDate);
                    if(taskToSave.getTaskState().equals(TaskState.OVERDUE)){
                        taskToSave.setTaskState(TaskState.COMPLETE);
                    }
                }
                if(requestBody.has("taskState")){
                    String taskState = requestBody.getString("taskState");
                    if(!taskState.equals(TaskState.COMPLETE.toString())){
                        return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Task State can only be updated to COMPLETE state")).toString());
                    }
                    taskToSave.setTaskState(TaskState.COMPLETE);
                }
                if(requestBody.has("listId")){
                    Optional<TaskList> savedList = taskListDAO.findById(requestBody.getLong("listId"));
                    if(savedList.isEmpty()){
                        return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("List with id %d does not exist", requestBody.getLong("listId"))).toString());
                    }
                    taskToSave.setTaskList(savedList.get());
                }
                Task persistedTask = tasksDAO.saveAndFlush(taskToSave);
                JSONObject taskObject = new JSONObject();
                taskObject.put("id", persistedTask.getId());
                taskObject.put("name", persistedTask.getTask());
                taskObject.put("dueDate", new Date(persistedTask.getDueDate().toInstant().toEpochMilli()));
                taskObject.put("priority", persistedTask.getPriority());
                taskObject.put("summary", persistedTask.getSummary());
                taskObject.put("state", persistedTask.getTaskState());
                taskObject.put("createdAt", persistedTask.getCreatedAt());
                taskObject.put("updatedAt", persistedTask.getUpdatedAt());

                ListenableFuture<SendResult<String, String>> foo =  kafkaTemplate.send(taskUpdateTopicName, taskObject.toString());
                try {
                    logger.info("[TasksController][update] Task message published to kafka with offset: " + foo.get().getRecordMetadata().offset());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


                return ResponseEntity.ok().body(taskObject.toString());
            } else{
                logger.error("[TasksController][updateTask]" + new JSONObject().put("message", String.format("Task %s does not exist", taskId)).toString());
                return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Task %s does not exist", taskId)).toString());
            }

        } catch (JSONException | IllegalArgumentException e){
            e.printStackTrace();
            logger.error("[TasksController][updateTask]" + new JSONObject().put("message", "Request body is not valid").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Request body is not valid").toString());
        }


    }


    @DeleteMapping(path = "/v1/tasks/{taskId}", produces = "application/json")
    public ResponseEntity<String> deleteTask(@RequestHeader HttpHeaders headers, @PathVariable long taskId) {
        logger.info("[TasksController][deleteTask][Received Request] with  param taskId:" + taskId);
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TasksController][deleteTask]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        try{
            Optional<Task> task = tasksDAO.findByIdAndUserId(taskId, user.getId());
            if(task.isPresent()){
                Task taskToDelete = task.get();
                tasksDAO.delete(taskToDelete);

                DeleteRequest request = DeleteRequest.of(d -> d.index(tasksIndex).id(String.valueOf(taskId)));

                try{
                    DeleteResponse deleteResponse = elasticsearchClient.delete(request);
                    if (Objects.nonNull(deleteResponse.result()) && !deleteResponse.result().name().equals("NotFound")) {
                        logger.info("[TasksController][deleteTask]" + new StringBuilder("Task with id " + deleteResponse.id() + " has been deleted.").toString());
                    }
                    logger.info("[TasksController][deleteTask]" + new StringBuilder("Task with id " + deleteResponse.id()+" does not exist.").toString());
                } catch (Exception e){
                    e.printStackTrace();
                    logger.error("[TasksController][deleteTask]" + e.getMessage());
                }

                return ResponseEntity.status(204).build();



            } else{
                logger.error("[TasksController][deleteTask]" + new JSONObject().put("message", String.format("Task %d does not exist", taskId)).toString());
                return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("Task %d does not exist", taskId)).toString());
            }

        } catch (JSONException | IllegalArgumentException e){
            logger.error("[TasksController][deleteTask]" + new JSONObject().put("message", "Request body is not valid").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Request body is not valid").toString());
        }


    }


    @PatchMapping(path = "/v1/tags/task/{taskId}", produces = "application/json")
    public ResponseEntity<String> addTagsToTask(@RequestHeader HttpHeaders headers, @PathVariable long taskId,
                                                @RequestBody String taggingRequestBody) {
        logger.info("[TasksController][addTagsToTask][Received Request] with  request body " + taggingRequestBody + "and param taskId:" + taskId);
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[TasksController][addTagsToTask]" + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        try{
            Optional<Task> task = tasksDAO.findByIdAndUserId(taskId, user.getId());
            if(task.isPresent()){
                Task taskToSave = task.get();
                List<Tag> taskTags = taskToSave.getTags();
                List<Long> invalidTagIds = new ArrayList<>();
                JSONObject requestBody = new JSONObject(taggingRequestBody);
                JSONArray suppliedTagIds = requestBody.getJSONArray("tags");
                for (int i = 0; i < suppliedTagIds.length(); i++) {
                    Long tagId = suppliedTagIds.getLong(i);
                    Optional<Tag> tag = tagsDAO.findByIdAndUserId(tagId, user.getId());
                    if(tag.isPresent()){
                        if(taskTags.size() == 10){
                            return ResponseEntity.status(206).body(new JSONObject().put("message", String.format("Some tags were not added. Only maximum of 10 tags can be added")).toString());
                        }
                        taskTags.add(tag.get());
                    } else{
                        invalidTagIds.add(tagId);
                    }
                }

                if(invalidTagIds.size() > 0 && invalidTagIds.size() < suppliedTagIds.length()){
                    return ResponseEntity.status(206).body(new JSONObject().put("message", String.format("Some tags ate invalid and were not added")).toString());
                } else if(invalidTagIds.size() ==  suppliedTagIds.length()){
                    return ResponseEntity.badRequest().body(new JSONObject().put("message", String.format("All tags are invalid")).toString());
                } else{
                   return ResponseEntity.ok().build();
                }

        } else{
                logger.error("[TasksController][addTagsToTask]" + new JSONObject().put("message", "Invalid task Id").toString());
                return ResponseEntity.badRequest().body(new JSONObject().put("message", "Invalid task Id").toString());
            }
        } catch (JSONException | IllegalArgumentException e){
            e.printStackTrace();
            logger.error("[TasksController][addTagsToTask]" + new JSONObject().put("message", "Request body is not valid").toString());
            return ResponseEntity.badRequest().body(new JSONObject().put("message", "Request body is not valid").toString());
        }
    }

    @PostMapping(path = "/v1/tasks/search", produces = "application/json")
    public ResponseEntity<String> searchTasks(@RequestHeader HttpHeaders headers,
                                                @RequestBody String taskSearchRequestBody) throws IOException {
        logger.info("[TasksController][searchTasks][Received Request] with  request body " + taskSearchRequestBody);
        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message", "Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();



        JSONObject requestBody = new JSONObject(taskSearchRequestBody);
        JSONArray searchCriteria = new JSONArray(requestBody.getJSONArray("criteria"));


        JSONObject finalQuery = new JSONObject();
        JSONObject queryObj = new JSONObject();
        JSONObject mustQuery = new JSONObject();
        JSONObject boolQuery = new JSONObject();
        JSONArray filterQuery = new JSONArray();
        JSONArray shouldQuery = new JSONArray();

        for (int i = 0; i < searchCriteria.length(); i++) {
            JSONObject matchQuery = new JSONObject();
            JSONObject criteria = new JSONObject();
            JSONObject jsonObject = (JSONObject) searchCriteria.get(i);
            JSONObject matchQueryObject = new JSONObject();
            matchQueryObject.put("query", jsonObject.get("value").toString());
            matchQueryObject.put("fuzziness", "AUTO");
            matchQueryObject.put("zero_terms_query", "all");
            matchQueryObject.put("operator", "and");
            criteria.put(jsonObject.get("field").toString(), matchQueryObject);
            matchQuery.put("match", criteria);
            shouldQuery.put(matchQuery);

//            filterQuery.put(termQuery);
        }
//        mustQuery.put("match_all", new JSONObject());
        boolQuery.put("should", shouldQuery);
//        boolQuery.put("must", mustQuery);
//        boolQuery.put("filter", filterQuery);
        queryObj.put("bool", boolQuery);
        finalQuery.put("query", queryObj);

        System.out.println("finalQuery " + finalQuery.toString());

        elasticsearchClient.putScript(r -> r
                .id("query-script")
                .script(s -> s
                        .lang("mustache")
                        .source(finalQuery.toString())
                ));

        HttpEntity<String> request = new HttpEntity<String>(queryObj.toString(), headers);

        SearchTemplateResponse<TaskElastic> scriptResponse = elasticsearchClient.searchTemplate(r -> r
                        .index(tasksIndex)
                        .id("query-script"),
                TaskElastic.class
        );

        List<Hit<TaskElastic>> hits = scriptResponse.hits().hits();
        JSONArray jsonArray = new JSONArray();
        for (Hit<TaskElastic> hit : hits) {
            System.out.println(hit);
            TaskElastic task = hit.source();
            JSONObject taskObject = new JSONObject();
            taskObject.put("id", task.getId());
            taskObject.put("name", task.getName());
            taskObject.put("dueDate", task.getDueDate());
            taskObject.put("priority", task.getPriority());
            taskObject.put("summary", task.getSummary());
            taskObject.put("state", task.getState());
            taskObject.put("createdAt", task.getCreatedAt());
            taskObject.put("updatedAt", task.getUpdatedAt());
            jsonArray.put(taskObject);
            System.out.println("Found task " + task.getName() + ", task string " + task.toString());

        }

        return ResponseEntity.ok().body(jsonArray.toString());


    }
}
