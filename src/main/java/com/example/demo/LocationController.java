package com.example.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class LocationController {

    private static final String JSON_FILE = "locations.json";

    @PostMapping("/locations")
    public ResponseEntity<Location> createLocation(@RequestParam("name") String name,
                                                   @RequestParam("address") String address,
                                                   @RequestParam("city") String city,
                                                   @RequestParam("state") String state,
                                                   @RequestParam("zipCode") String zipCode,
                                                   @RequestParam("userEmail") String userEmail) {
        String id = UUID.randomUUID().toString();
        Location location = new Location(id, name, address, city, state, zipCode, userEmail);

        // Validate address
        if (!isValidAddress(name, address, city, state, zipCode)) {
            return ResponseEntity.badRequest().build();
        }
        // Access email
        if (!isCorrectEmail(userEmail)) {
            return ResponseEntity.badRequest().build();
        }

        // Read existing locations from JSON file
        List<Location> locations = new ArrayList<>();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        try (FileReader reader = new FileReader(JSON_FILE)) {
            JSONArray jsonArray = (JSONArray) parser.parse(reader);
            for (Object obj : jsonArray) {
                JSONObject json = (JSONObject) obj;
                String idJson = (String) json.get("id");
                String nameJson = (String) json.get("name");
                String addressJson = (String) json.get("address");
                String cityJson = (String) json.get("city");
                String stateJson = (String) json.get("state");
                String zipCodeJson = (String) json.get("zipCode");
                String userEmailJson = (String) json.get("userEmail");
                Location loc = new Location(idJson, nameJson, addressJson, cityJson, stateJson, zipCodeJson, userEmailJson);
                locations.add(loc);
            }
        } catch (IOException | ParseException e) {
        }


        for (Location loc : locations) {
            if (loc.getAddress().equalsIgnoreCase(address) &&
                    loc.getCity().equalsIgnoreCase(city) &&
                    loc.getState().equalsIgnoreCase(state) &&
                    loc.getZipCode().equalsIgnoreCase(zipCode)) {
                // If location already exists, return HTTP 409 Conflict
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        locations.add(location);

        JSONArray jsonArray = new JSONArray();
        for (Location loc : locations) {
            JSONObject locationJson = new JSONObject();
            locationJson.put("id", loc.getId());
            locationJson.put("name", loc.getName());
            locationJson.put("address", loc.getAddress());
            locationJson.put("city", loc.getCity());
            locationJson.put("state", loc.getState());
            locationJson.put("zipCode", loc.getZipCode());
            locationJson.put("userEmail", loc.getUserEmail());
            jsonArray.add(locationJson);
        }
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            writer.write(jsonArray.toJSONString());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        //201
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    private boolean isValidAddress(String name, String address, String city, String state, String zipCode) {
        if (name == null || name.trim().isEmpty() || address == null || address.trim().isEmpty()) {
            return false;
        }


        if (city == null || state == null || zipCode == null) {
            return false;
        }
        if (!zipCode.matches("\\d{5}(-\\d{4})?")) {
            return false;
        }

        return true;
    }

    private boolean isCorrectEmail(String userEmail) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<User> users = objectMapper.readValue(new File("users.json"), new TypeReference<List<User>>() {
            });
            for (User user : users) {
                if (user.getEmail().equals(userEmail)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}