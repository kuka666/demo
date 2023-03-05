import com.example.demo.Friend
import com.example.demo.FriendController
import net.minidev.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification
import spock.lang.Subject
import net.minidev.json.JSONArray
import spock.lang.Unroll;
class FriendControllerSpec extends Specification {

    @Subject
    FriendController friendController = new FriendController()

    @Unroll
    def "shareLocation returns #expectedStatus when friend email is #friendEmail, location id is #locationId, and access level is #accessLevel"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest()
        MockHttpServletResponse response = new MockHttpServletResponse()
        request.method = "POST"
        request.requestURI = "/friends"
        request.addParameter("friend_email", friendEmail)
        request.addParameter("locationId", locationId)
        request.addParameter("accessLevel", accessLevel)

        when:
        ResponseEntity<Friend> result = friendController.shareLocation(friendEmail, locationId, accessLevel)

        then:
        result.statusCode == expectedStatus
        if (expectedStatus == HttpStatus.OK) {
            result.body.friend_email == friendEmail
            result.body.locationId == locationId
            result.body.accessLevel == accessLevel
        }

        where:
        friendEmail | locationId | accessLevel | expectedStatus
        "alikh@mail.ru" | "bee4c61f-32af-4700-89ca-888ea96511f5" | "ADMIN" | HttpStatus.OK
        "invalid_email" | "location123" | "write" | HttpStatus.BAD_REQUEST
        "john@example.com" | "invalid_location" | "write" | HttpStatus.BAD_REQUEST
        "john@example.com" | "location123" | "invalid_access_level" | HttpStatus.BAD_REQUEST
        "alikh@mail.ru" | "bee4c61f-32af-4700-89ca-888ea96511f5" | "ADMIN" | HttpStatus.BAD_REQUEST // friend already exists
        "location_owner@example.com" | "location123" | "write" | HttpStatus.BAD_REQUEST // friend is owner of location
    }




    def "getFriendsForLocation returns friends associated with the location when location exists"() {
        given:
        String locationId = "5148736b-717d-4e7f-af38-3050a41917c5"
        createFriendsJsonFile([
                [friend_email: "john@example.com", locationId: locationId, accessLevel: "ADMIN"],
                [friend_email: "jane@example.com", locationId: locationId, accessLevel: "READ_ONLY"],
                [friend_email: "doe@example.com", locationId: "e8fc6903-637a-4bc1-af85-90eccf2c448d", accessLevel: "READ_ONLY"]
        ])

        when:
        ResponseEntity<List<Friend>> response = friendController.getFriendsForLocation(locationId)

        then:
        response.statusCode == HttpStatus.OK
        List<Friend> friends = response.body
        friends.size() == 2
        friends[0].friend_email == "john@example.com"
        friends[0].locationId == locationId
        friends[0].accessLevel == "ADMIN"
        friends[1].friend_email == "jane@example.com"
        friends[1].locationId == locationId
        friends[1].accessLevel == "READ_ONLY"

        cleanup:
            deleteFriendsJsonFile()
    }
    def createFriendsJsonFile(List<Map<String, String>> friends) {
        File jsonFile = new File(FriendController.JSON_FILE)
        jsonFile.createNewFile()
        JSONArray jsonArray = new JSONArray()

        friends.each { friend ->
            JSONObject friendJson = new JSONObject(friend)
            jsonArray.add(friendJson)
        }

        new FileWriter(jsonFile).withWriter { writer ->
            writer.write(jsonArray.toJSONString())
        }
    }


}