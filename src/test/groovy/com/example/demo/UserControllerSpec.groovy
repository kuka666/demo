import com.example.demo.UserController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class UserControllerSpec extends Specification {

    @Subject
    UserController userController = new UserController()

    def "should create user with valid email"() {
        given:
        def name = "John Doe"
        def email = "johndoe@example.com"

        when:
        def response = userController.createUser(name, email)

        then:
        response.statusCode == HttpStatus.OK
        response.body.name == name
        response.body.email == email
    }

    @Unroll("should return bad request for invalid email: #invalidEmail")
    def "should not create user with invalid email"() {
        given:
        def name = "John Doe"

        when:
        def response = userController.createUser(name, invalidEmail)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST

        where:
        invalidEmail << ["invalid", "invalid@", "@invalid.com", "invalid@.com", "invalid@.com."]
    }
}