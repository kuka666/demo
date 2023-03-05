package com.example.demo;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoApplicationTests {

    @Test
    public void testCreateUser() throws Exception {
        String name = "John";
        String email = "john@example.com";

        UserController userControllerMock = Mockito.mock(UserController.class);
        Mockito.when(userControllerMock.emailExists(Mockito.anyString())).thenReturn(false);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userControllerMock).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .param("name", name)
                        .param("email", email))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}