import com.example.demo.Location
import com.example.demo.LocationController
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Subject

class LocationControllerSpec extends Specification {

    @Subject
    LocationController locationController = new LocationController()

    def "should create location with valid input"() {
        given:
        def name = "Starbucks"
        def address = "123 Main St."
        def city = "Anytown"
        def state = "CA"
        def zipCode = "60601"
        def userEmail = "adil@mail.ru"

        when:
        def response = locationController.createLocation(name, address, city, state, zipCode, userEmail)

        then:
        response.statusCode == HttpStatus.CREATED
        Location createdLocation = response.body
        createdLocation.id != null
        createdLocation.name == name
        createdLocation.address == address
        createdLocation.city == city
        createdLocation.state == state
        createdLocation.zipCode == zipCode
        createdLocation.userEmail == userEmail
    }

    def "should return bad request for invalid input"() {
        given:
        def name = null
        def address = "123 Main St."
        def city = "Anytown"
        def state = "CA"
        def zipCode = "12345"
        def userEmail = "user@example.com"

        when:
        def response = locationController.createLocation(name, address, city, state, zipCode, userEmail)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
    }

    def "should return conflict if location already exists"() {
        given:
        def name = "Starbucks"
        def address = "Saryarka 8"
        def city = "Anytown"
        def state = "CA"
        def zipCode = "60601"
        def userEmail = "adil@mail.ru"
        // Create location with same address, city, state, and zip code
        def existingLocation = new Location(name: name, address: address, city: city, state: state, zipCode: zipCode, userEmail: userEmail)
        def objectMapper = new ObjectMapper()
        def jsonArray = objectMapper.readValue(new File("locations.json"), List)
        jsonArray.add(existingLocation)
        objectMapper.writeValue(new File("locations.json"), jsonArray)

        when:
        def response = locationController.createLocation(name, address, city, state, zipCode, userEmail)

        then:
        response.statusCode == HttpStatus.CONFLICT
    }

    def "should return bad request if user email is invalid"() {
        given:
        def name = "Starbucks"
        def address = "123 Main St."
        def city = "Anytown"
        def state = "CA"
        def zipCode = "12345"
        def userEmail = "invalid-email"

        when:
        def response = locationController.createLocation(name, address, city, state, zipCode, userEmail)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
    }
}