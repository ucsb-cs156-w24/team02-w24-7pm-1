package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

    @MockBean
    HelpRequestRepository helpRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET /api/helprequest/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_helprequest() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest help1 = HelpRequest.builder()
                                .requesterEmail("a@ucsb.edu")
                                .teamId("team2")
                                .tableOrBreakoutRoom("table2")
                                .requestTime(ldt1)
                                .explanation("need a help")
                                .solved(true)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                HelpRequest help2 = HelpRequest.builder()
                    .requesterEmail("b@ucsb.edu")
                    .teamId("team3")
                    .tableOrBreakoutRoom("table3")
                    .requestTime(ldt2)
                    .explanation("need a help")
                    .solved(true)
                    .build();

                ArrayList<HelpRequest> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(help1, help2));

                when(helpRequestRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/helprequest/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_helprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00");

                HelpRequest help1 = HelpRequest.builder()
                    .requesterEmail("ucsbedu")
                    .teamId("team2")
                    .tableOrBreakoutRoom("table2")
                    .requestTime(ldt1)
                    .explanation("help")
                    .solved(true)
                                .build();

                when(helpRequestRepository.save(eq(help1))).thenReturn(help1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/helprequest/post?requesterEmail=ucsbedu&teamId=team2&tableOrBreakoutRoom=table2&requestTime=2022-01-03T00:00&explanation=help&solved=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).save(help1);
                String expectedJson = mapper.writeValueAsString(help1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

}
