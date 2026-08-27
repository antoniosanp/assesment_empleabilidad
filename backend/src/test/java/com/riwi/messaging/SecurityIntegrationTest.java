package com.riwi.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riwi.messaging.dto.LoginRequest;
import com.riwi.messaging.dto.LoginResponse;
import com.riwi.messaging.dto.SendMessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAccessToken(String email, String password) throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readValue(responseJson, LoginResponse.class);
        return response.getAccessToken();
    }

    @Test
    @DisplayName("QA Test 1: Verify rejection of message posting by non-member user to private channel")
    public void testRejectNonMemberAccessToPrivateChannelMessagePosting() throws Exception {
        // Juan Perez is NOT a member of Private Channel 22222222-2222-2222-2222-222222222222
        String juanToken = getAccessToken("juan.perez@riwi.io", "123456");
        UUID privateChannelId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        SendMessageRequest sendRequest = SendMessageRequest.builder()
                .channelId(privateChannelId)
                .content("Intento no autorizado de publicar en canal privado ajeno.")
                .build();

        mockMvc.perform(post("/api/channels/" + privateChannelId + "/messages")
                        .header("Authorization", "Bearer " + juanToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("QA Test 2: Verify private channel messages are not returned to non-member user")
    public void testDoNotReturnPrivateChannelMessagesToNonMembers() throws Exception {
        // Pedro Soporte is NOT a member of Private Channel 22222222-2222-2222-2222-222222222222
        String pedroToken = getAccessToken("pedro.soporte@riwi.io", "123456");
        UUID privateChannelId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        mockMvc.perform(get("/api/channels/" + privateChannelId + "/messages")
                        .header("Authorization", "Bearer " + pedroToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
