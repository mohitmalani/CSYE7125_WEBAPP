package com.csye7125.webapp.webapp.controllers;

import com.csye7125.webapp.webapp.commons.CommonUtilsService;
import com.csye7125.webapp.webapp.commons.ValidationService;
import com.csye7125.webapp.webapp.daos.TaskListDAO;
import com.csye7125.webapp.webapp.models.taskLists.TaskList;
import com.csye7125.webapp.webapp.models.users.User;
import com.csye7125.webapp.webapp.commons.AuthenticationService;
import com.csye7125.webapp.webapp.daos.UserDAO;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class UserController {



    @Autowired
    UserDAO userDAO;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    ValidationService validationService;

    @Autowired
    CommonUtilsService commonUtilsService;

    @Autowired
    TaskListDAO taskListDAO;

    Logger logger = LoggerFactory.getLogger(UserController.class);


    @GetMapping(path = "/v1", produces = "application/json")
    public ResponseEntity<String> healthCheck(@RequestHeader HttpHeaders headers) {
        logger.info("[UserController][healthCheck][Recieved Request]");

//        String authorization = headers.getFirst("Authorization");
//        String decodedTokenString = authenticationService.decodeBasicAuthToken(authorization);
//        String[] tokens = new String[2];
//
//        if(decodedTokenString != null){
//            if(decodedTokenString.split(":").length == 2) {
//                tokens = decodedTokenString.split(":", 2);
//            }
//            if(!authenticationService.authenticateUser(tokens)){
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
//            }
//        } else{
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
//        }
//
//        User userObj =  userService.getUser(tokens[0]);
        System.out.println("Health check in progress");
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/v1/user", produces = "application/json")
    public ResponseEntity<String> postUser(@RequestHeader HttpHeaders headers, @RequestBody String reqBody) {
        logger.info("[UserController][postUser][Recieved Request] with request body:" +  reqBody);

        JSONObject reqObj = new JSONObject(reqBody);
        String errorString = validationService.validateSaveObject(reqObj);
        if(!errorString.equals("")) {
            JSONObject respObj = new JSONObject();
            respObj.put("message", errorString);
            logger.error("[UserController][postUser]" + respObj.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respObj.toString());
        }
        User u = new User();
        String email = reqObj.getString("email");
        Optional<User> checkUser = userDAO.findByEmail(email);
        if(checkUser.isPresent()){
            JSONObject resObj = new JSONObject();
            resObj.put("message", "Email already exists.");
            logger.error("[UserController][postUser] " + resObj.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resObj.toString());
        }
        String firstName = reqObj.getString("firstName");
        String lastName = reqObj.getString("lastName");
        String middleName = reqObj.getString("middleName");
        String password = reqObj.getString("password");
        BCryptPasswordEncoder b = new BCryptPasswordEncoder(12);
        u.setPassword(b.encode(password));
//        u.setId(UUID.randomUUID());
        u.setFirstName(firstName);
        u.setMiddleName(middleName);
        u.setLastName(lastName);
        u.setEmail(email);

        try{
            u = userDAO.saveAndFlush(u);
            TaskList taskList = new TaskList();
            taskList.setUserId(u.getId());
            taskList.setName("default");
            taskListDAO.saveAndFlush(taskList);
            return ResponseEntity.status(HttpStatus.CREATED).body(commonUtilsService.getUserAsJSON(u).toString());

        } catch(Exception e) {
            JSONObject resObj = new JSONObject();
            if(e.getMessage().contains("constraint [usertable_username_key]")){
                resObj.put("message", "Email already exists.");
                logger.error("[UserController][postUser] " + resObj.toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resObj.toString());
            } else{
                resObj.put("message", "Unable to Save User Information. Please try again.");
                logger.error("[UserController][postUser] " + resObj.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resObj.toString());
            }
        }
    }


    @PutMapping(path = "/v1/user", produces = "application/json")
    public ResponseEntity<String> putUser(@RequestHeader HttpHeaders headers, @RequestBody String reqBody) {
        logger.info("[UserController][putUser][Recieved Request] with request body:" +  reqBody);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[UserController][putUser] " + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

//        String decodedTokenString = authenticationService.decodeBasicAuthToken(authorization);
//        String[] tokens = new String[2];
//
//        if(decodedTokenString != null){
//            if(decodedTokenString.split(":").length == 2) {
//                tokens = decodedTokenString.split(":", 2);
//            }
//            if(!authenticationService.authenticateUser(tokens)){
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
//            }
//        } else{
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
//        }

        JSONObject updateObj = new JSONObject(reqBody);
        //Validate update object to check for unwanted fields and data type
        String errorString = validationService.validateModifyObject(updateObj);
        if(!errorString.equals("")){
            JSONObject respObj = new JSONObject();
            respObj.put("message", errorString);
            logger.error("[UserController][putUser] " + respObj.toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respObj.toString());
        }

        if(!user.getEmail().equals(updateObj.getString("email"))){
            logger.error("[UserController][putUser] " + new JSONObject().put("message",
                    "Incorrect Authorization credentials for " +
                            updateObj.getString("email")).toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JSONObject().put("message",
                           "Incorrect Authorization credentials for " +
                                   updateObj.getString("email")).toString());
        }



        Optional<User> u = userDAO.findByEmail(updateObj.getString("email"));
        User userToUpdate = u.get();
        try{
            if(updateObj.has("firstName")){
                userToUpdate.setFirstName(updateObj.getString("firstName"));
            }
            if(updateObj.has("lastName")){
                userToUpdate.setLastName(updateObj.getString("lastName"));
            }
            if(updateObj.has("middleName")){
                userToUpdate.setMiddleName(updateObj.getString("middleName"));
            }
            if(updateObj.has("password")){
                String password = updateObj.getString("password");
                BCryptPasswordEncoder b = new BCryptPasswordEncoder(12);
                updateObj.put("password", b.encode(password));
                userToUpdate.setPassword(updateObj.getString("password"));
            }
            if(updateObj.has("email")){
                userToUpdate.setEmail(updateObj.getString("email"));
            }
            User savedUser = userDAO.saveAndFlush(userToUpdate);

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject resObj = new JSONObject();
            resObj.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR);
            resObj.put("message", "Unable to Update User Information. Please try again.");
            logger.error("[UserController][putUser] " + resObj.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resObj.toString());
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(updateObj.toString());
    }


    @PutMapping(path = "/v1/user/email", produces = "application/json")
    public ResponseEntity<String> putEmail(@RequestHeader HttpHeaders headers, @RequestBody String reqBody) {
        logger.info("[UserController][putEmail][Recieved Request] with request body:" +  reqBody);

        String authorization = headers.getFirst("Authorization");
        Optional<User> authenticatedUser = authenticationService.getTokens(authorization);
        if(authenticatedUser.isEmpty()){
            logger.error("[UserController][putEmail] " + new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
        User user = authenticatedUser.get();

        JSONObject updateObj = new JSONObject(reqBody);
        //Validate update object to check for unwanted fields and data type
        String errorString = validationService.validateEmailModifyObject(updateObj);
        if(!errorString.equals("")){
            JSONObject respObj = new JSONObject();
            respObj.put("message", errorString);
            logger.error("[UserController][putEmail] " + respObj.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respObj.toString());
        }

        if(!user.getEmail().equals(updateObj.getString("currentEmail"))){
            logger.error("[UserController][putEmail] " + new JSONObject().put("message",
                    "Incorrect Authorization credentials for " +
                            updateObj.getString("currentEmail")).toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JSONObject().put("message",
                            "Incorrect Authorization credentials for " +
                                    updateObj.getString("currentEmail")).toString());
        }



        Optional<User> u = userDAO.findByEmail(updateObj.getString("currentEmail"));
        User userToUpdate = u.get();
        try{
            if(updateObj.has("newEmail")){
                userToUpdate.setEmail(updateObj.getString("newEmail"));
            }
            User savedUser = userDAO.saveAndFlush(userToUpdate);

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject resObj = new JSONObject();
            resObj.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR);
            resObj.put("message", "Unable to Update User Information. Please try again.");
            logger.error("[UserController][putEmail] " + resObj.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resObj.toString());
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(updateObj.toString());
    }

}
