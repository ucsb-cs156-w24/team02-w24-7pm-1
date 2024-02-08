package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)

public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase  {
    @MockBean
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET /api/UCSBDiningCommonsMenuItem/all
        
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsbDiningCommonsMenuItem() throws Exception {

        // arrange
        UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("bakedPestoPastaWithChicken")
                .station("entreeSpecials")
                .build();

        UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem2 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("tofuBanhMiSandwich(v)")
                .station("entreeSpecials")
                .build();

        ArrayList<UCSBDiningCommonsMenuItem> expectedUCSBDiningCommonsMenuItem = new ArrayList<>();
        expectedUCSBDiningCommonsMenuItem.addAll(Arrays.asList(ucsbDiningCommonsMenuItem1, ucsbDiningCommonsMenuItem2));

        when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedUCSBDiningCommonsMenuItem);

        // act
        MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedUCSBDiningCommonsMenuItem);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/UCSBDiningCommonsMenuItem/post...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsbDiningCommonsMenuItem() throws Exception {
        // arrange

        UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("bakedPestoPastaWithChicken")
                .station("entreeSpecials")
                .build();
                
        when(ucsbDiningCommonsMenuItemRepository.save(eq(ucsbDiningCommonsMenuItem1))).thenReturn(ucsbDiningCommonsMenuItem1);

        // act
        MvcResult response = mockMvc.perform(
                post("/api/UCSBDiningCommonsMenuItem/post?diningCommonsCode=ortega&name=bakedPestoPastaWithChicken&station=entreeSpecials")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(ucsbDiningCommonsMenuItem1);
        String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenuItem1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}
