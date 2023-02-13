package com.csye7125.webapp.webapp.commons;

import com.csye7125.webapp.webapp.daos.UserDAO;
import com.csye7125.webapp.webapp.models.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    UserDAO userDAO;

    public boolean authenticateUser(String[] tokens) {
                BCryptPasswordEncoder b = new BCryptPasswordEncoder(12);
                String storedPassword = userDAO.findUserPassword(tokens[0]);
                if(storedPassword != null && b.matches(tokens[1], storedPassword)) {
                    return true;
                }
                return false;
    }


    public Optional<User> getTokens(String authorization){
//        String authorization = headers.getFirst("Authorization");
        String decodedTokenString = decodeBasicAuthToken(authorization);
        String[] tokens = new String[2];

        if(decodedTokenString != null){
            if(decodedTokenString.split(":").length == 2) {
                tokens = decodedTokenString.split(":", 2);
            }
            if(!authenticateUser(tokens)){
                return Optional.empty();
            }else{
                Optional<User> u = userDAO.findByEmail(tokens[0]);
                return u;
            }
        } else{
            return Optional.empty();
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new JSONObject().put("message","Authorization Refused for the credentials provided.").toString());
        }
    }


    public String decodeBasicAuthToken(String authorization){
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            return new String(decodedBytes);
        }

        return null;
    }
}
