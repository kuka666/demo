package com.example.demo

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class FullLocationSpec extends Specification {

    @Autowired
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FullLocation()).build()

    def "should return 200 and a list of locations when user exists"() {
        given:
        def userEmail = "adil@mail.ru"

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/locations/all")
                .param("userEmail", userEmail))

        then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "should return 400 when email does not exist"() {
        given:
        def userEmail = "nonexistent@example.com"

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/locations/all")
                .param("userEmail", userEmail))

        then:
        result.andExpect(MockMvcResultMatchers.status().isBadRequest())
    }
}