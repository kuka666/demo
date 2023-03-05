package com.example.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@RestController
public class UserController {
    private static final String JSON_FILE = "users.json";
    ObjectMapper objectMapper = new ObjectMapper();
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestParam("name") String name,
                                           @RequestParam("email") String email) {
        // Validate email using regular expression
        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest().build();
        }
        if (emailExists(email)) {
            return ResponseEntity.badRequest().build();
        }
        // Create new user object
        User user = new User(name, email);

        // Write user object to JSON file
        try {
            JSONArray jsonArray;
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            try (FileReader reader = new FileReader(JSON_FILE)) {
                jsonArray = (JSONArray) parser.parse(reader);
            } catch (IOException | ParseException e) {
                jsonArray = new JSONArray();
            }

            JSONObject userJson = new JSONObject();
            userJson.put("name", user.getName());
            userJson.put("email", user.getEmail());
            jsonArray.add(userJson);

            try (FileWriter writer = new FileWriter(JSON_FILE)) {
                writer.write(jsonArray.toJSONString());
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(user);
    }

    // Validate email using regular expression
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    boolean emailExists(String email) {
        try {
            List<User> users = objectMapper.readValue(new File("users.json"), new TypeReference<List<User>>() {
            });
            if(email==null){
                return false;
            }
            for (User user : users) {
                if (user.getEmail().equals(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}