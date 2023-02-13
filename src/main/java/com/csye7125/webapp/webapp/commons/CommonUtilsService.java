package com.csye7125.webapp.webapp.commons;

import com.csye7125.webapp.webapp.models.users.User;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class CommonUtilsService {

    public JSONObject getUserAsJSON(User u){
        JSONObject obj = new JSONObject();
        obj.put("id", u.getId());
        obj.put("firstName", u.getFirstName());
        obj.put("middleName", u.getMiddleName());
        obj.put("lastName", u.getLastName());
        obj.put("email", u.getEmail());
        SimpleDateFormat s = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'");
        obj.put("createdAt", s.format(u.getCreatedAt()));
        obj.put("updatedAt", s.format(u.getUpdatedAt()));
        return obj;
    }

}
