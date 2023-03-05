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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
public class FriendController {
    private static final String JSON_FILE = "friends.json";
    ObjectMapper objectMapper = new ObjectMapper();

    // Share location with a friend
    @PostMapping("/friends")
    public ResponseEntity<Friend> shareLocation(@RequestParam String friend_email,
                                                @RequestParam String locationId,
                                                @RequestParam String accessLevel) {
        if (!locationExists(locationId) || !emailExists(friend_email) || !accessCorrect(accessLevel) || alreadyExist(friend_email, locationId) || checkLocationOwner(locationId, friend_email)) {
            return ResponseEntity.badRequest().build();
        }

        // Create new friend object
        Friend friend = new Friend(friend_email, locationId, accessLevel);

        // Write friend object to JSON file
        try {
            JSONArray jsonArray;
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            try (FileReader reader = new FileReader(JSON_FILE)) {
                jsonArray = (JSONArray) parser.parse(reader);
            } catch (IOException | ParseException e) {
                jsonArray = new JSONArray();
            }

            JSONObject friendJson = new JSONObject();
            friendJson.put("friend_email", friend.getFriend_email());
            friendJson.put("locationId", friend.getLocationId());
            friendJson.put("accessLevel", friend.getAccessLevel());
            jsonArray.add(friendJson);

            try (FileWriter writer = new FileWriter(JSON_FILE)) {
                writer.write(jsonArray.toJSONString());
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(friend);
    }


    @GetMapping("/friends/access")
    public ResponseEntity<List<Friend>> getFriendsForLocation(@RequestParam String locationId) {
        // Check if location exists
        if (!locationExists(locationId)) {
            return ResponseEntity.badRequest().build();
        }


        List<Friend> friends = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            try (FileReader reader = new FileReader(JSON_FILE)) {
                JSONArray jsonArray = (JSONArray) parser.parse(reader);
                for (Object object : jsonArray) {
                    JSONObject friendJson = (JSONObject) object;
                    if (friendJson.get("locationId").equals(locationId)) {
                        Friend friend = new Friend(
                                (String) friendJson.get("friend_email"),
                                (String) friendJson.get("locationId"),
                                (String) friendJson.get("accessLevel")
                        );
                        friends.add(friend);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(friends);
    }

    @PostMapping("/friends/add")
    public ResponseEntity<Friend> addFriendToLocation(@RequestParam String locationId, @RequestParam String userEmail, @RequestParam String friendEmail) {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            List<Friend> friends = objectMapper.readValue(new File(JSON_FILE), new TypeReference<List<Friend>>() {
            });
            if (alreadyExist(friendEmail, locationId)) {
                return ResponseEntity.badRequest().build();
            }

            if (!emailExists(friendEmail)) {
                return ResponseEntity.badRequest().build();
            }

            boolean isAdmin = false;
            for (Friend friend : friends) {
                if (friend.getFriend_email().equals(userEmail) && friend.getLocationId().equals(locationId) && friend.getAccessLevel().equals("ADMIN")) {
                    isAdmin = true;
                    break;
                }
            }

            // If user is an admin, add new friend to Friend
            if (isAdmin) {
                Friend newFriend = new Friend(friendEmail, locationId, "READ_ONLY");
                friends.add(newFriend);


                objectMapper.writeValue(new File(JSON_FILE), friends);

                return new ResponseEntity<Friend>(newFriend, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/friends/delete")
    public ResponseEntity<Void> deleteFriendFromLocation(@RequestParam String friend_email,
                                                         @RequestParam String locationId) {
        // Read the friends JSON file
        List<Friend> friends = readAccesses();

        // Find the friend to delete
        Friend friendToDelete = null;
        for (Friend friend : friends) {
            if (friend.getFriend_email().equals(friend_email) && friend.getLocationId().equals(locationId)) {
                friendToDelete = friend;
                break;
            }
        }

        // Return 404 Not Found if the friend is not found
        if (friendToDelete == null) {
            return ResponseEntity.notFound().build();
        }

        // Remove the friend from the list of friends
        friends.remove(friendToDelete);

        writeFriends(friends);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/friends/updateLevel")
    public ResponseEntity<Void> updateFriendAccessLevel(@RequestParam String locationId,
                                                        @RequestParam String owner_email,
                                                        @RequestParam String friend_email) {

        if (!checkLocationOwner(locationId, owner_email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        List<Friend> friends = readAccesses();


        Friend friendToUpdate = null;
        for (Friend friend : friends) {
            if (friend.getFriend_email().equals(friend_email) && friend.getLocationId().equals(locationId)) {
                friendToUpdate = friend;
                break;
            }
        }


        if (friendToUpdate == null) {
            return ResponseEntity.notFound().build();
        }


        if (friendToUpdate.getAccessLevel().equals("ADMIN")) {
            friendToUpdate.setAccessLevel("READ_ONLY");
        } else {
            friendToUpdate.setAccessLevel("ADMIN");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("friends.json"), friends);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    private boolean locationExists(String locationId) {
        try {
            List<Location> locations = objectMapper.readValue(new File("locations.json"), new TypeReference<List<Location>>() {
            });
            for (Location location : locations) {
                if (location.getId().equals(locationId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkLocationOwner(String id, String email) {
        try {
            List<Location> locations = objectMapper.readValue(new File("locations.json"), new TypeReference<List<Location>>() {
            });
            for (Location location : locations) {
                if (location.getId().equals(id) && location.getUserEmail().equals(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean emailExists(String email) {
        try {
            List<User> users = objectMapper.readValue(new File("users.json"), new TypeReference<List<User>>() {
            });
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

    private boolean alreadyExist(String email, String id) {
        try {
            List<Friend> friends = objectMapper.readValue(new File("friends.json"), new TypeReference<List<Friend>>() {
            });
            for (Friend friend : friends) {
                if (friend.getFriend_email().equals(email) && friend.getLocationId().equals(id)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean accessCorrect(String level) {
        return Objects.equals(level, "READ_ONLY") || Objects.equals(level, "ADMIN");
    }

    private List<Friend> readAccesses() {
        try {
            List<Friend> friends = objectMapper.readValue(new File(JSON_FILE), new TypeReference<List<Friend>>() {
            });
            return friends;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void writeFriends(List<Friend> friends) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(JSON_FILE), friends);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
