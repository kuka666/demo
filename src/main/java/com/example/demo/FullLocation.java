package com.example.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/locations")
public class FullLocation {
    private static final String ACCESS_FILE = "friends.json";
    private static final String LOCATIONS_FILE = "locations.json";
    ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/all")
    public ResponseEntity<List<Location>> getAllLocations(@RequestParam String userEmail) throws IOException, ParseException {
        // Check if location exists
        if (!emailExists(userEmail)) {
            return ResponseEntity.badRequest().build();
        }

        List<Location> allLocations = readLocations();
        List<Friend> accessList = readAccesses();


        List<Location> userLocations = new ArrayList<>();
        for (Location location : allLocations) {
            if (location.getUserEmail().equals(userEmail)) {
                // Add locations that the user owns
                userLocations.add(location);
            } else {
                for (Friend access : accessList) {
                    if (access.getLocationId().equals(location.getId()) && access.getFriend_email().equals(userEmail)) {
                        userLocations.add(location);
                        break;
                    }
                }
            }
        }

        return ResponseEntity.ok(userLocations);
    }

    private boolean emailExists(String email) {
        try {
            List<User> users = objectMapper.readValue(new File("users.json"), new TypeReference<List<User>>() {
            });
            if (email == null) {
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

    private List<Location> readLocations() {
        try {
            List<Location> locations = objectMapper.readValue(new File(LOCATIONS_FILE), new TypeReference<List<Location>>() {
            });
            return locations;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Friend> readAccesses() {
        try {
            List<Friend> friends = objectMapper.readValue(new File(ACCESS_FILE), new TypeReference<List<Friend>>() {
            });
            return friends;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}





