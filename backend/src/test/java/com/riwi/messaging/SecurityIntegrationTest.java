package com.riwi.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riwi.messaging.dto.*;
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

import static org.junit.jupiter.api.Assertions.*;
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

    // =========================================================================
    // ROUTE 1: /api/auth/login (Login)
    // =========================================================================

    @Test
    @DisplayName("Route 1 [SUCCESS]: Valid credentials returns 200 OK and JWT tokens")
    public void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@riwi.io")
                .password("123456")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("admin@riwi.io", response.getUser().getEmail());
    }

    @Test
    @DisplayName("Route 1 [ERROR]: Invalid password returns 401 Unauthorized")
    public void testLoginInvalidPasswordFailure() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@riwi.io")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // ROUTE 2: /api/auth/refresh (Token Rotation)
    // =========================================================================

    @Test
    @DisplayName("Route 2 [SUCCESS]: Valid refresh token returns 200 OK and rotated tokens")
    public void testRefreshTokenSuccess() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@riwi.io")
                .password("123456")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);

        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(loginResponse.getRefreshToken())
                .build();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse refreshResponse = objectMapper.readValue(refreshResult.getResponse().getContentAsString(), LoginResponse.class);
        assertNotNull(refreshResponse.getAccessToken());
        assertNotNull(refreshResponse.getRefreshToken());
    }

    @Test
    @DisplayName("Route 2 [ERROR]: Invalid refresh token returns 401 Unauthorized")
    public void testRefreshTokenInvalidFailure() throws Exception {
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken("invalid.refresh.token.string")
                .build();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // ROUTE 3: /api/channels/{channelId}/messages (POST - Send Message)
    // =========================================================================

    @Test
    @DisplayName("Route 3 [SUCCESS]: Member user sends message to channel returns 201 Created")
    public void testSendMessageToMemberChannelSuccess() throws Exception {
        // Maria Gomez is the OWNER of Private Channel 22222222-2222-2222-2222-222222222222
        String mariaToken = getAccessToken("maria.gomez@riwi.io", "123456");
        UUID privateChannelId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        SendMessageRequest sendRequest = SendMessageRequest.builder()
                .channelId(privateChannelId)
                .content("Mensaje de prueba de miembro autorizado en canal de Backend.")
                .build();

        MvcResult result = mockMvc.perform(post("/api/channels/" + privateChannelId + "/messages")
                        .header("Authorization", "Bearer " + mariaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        MessageDTO messageDTO = objectMapper.readValue(result.getResponse().getContentAsString(), MessageDTO.class);
        assertNotNull(messageDTO.getId());
        assertEquals("SENT", messageDTO.getStatus());
    }

    @Test
    @DisplayName("Route 3 [ERROR]: Non-member user sending message to private channel returns 403 Forbidden")
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

    // =========================================================================
    // ROUTE 4: /api/channels/{channelId}/messages (GET - Read Message History)
    // =========================================================================

    @Test
    @DisplayName("Route 4 [SUCCESS]: Member user reads channel message history returns 200 OK")
    public void testGetChannelMessagesForMemberSuccess() throws Exception {
        // Maria Gomez is a member of Private Channel 22222222-2222-2222-2222-222222222222
        String mariaToken = getAccessToken("maria.gomez@riwi.io", "123456");
        UUID privateChannelId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        MvcResult result = mockMvc.perform(get("/api/channels/" + privateChannelId + "/messages?limit=10")
                        .header("Authorization", "Bearer " + mariaToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertTrue(json.contains("items"));
    }

    @Test
    @DisplayName("Route 4 [ERROR]: Non-member user reading private channel messages returns 403 Forbidden")
    public void testDoNotReturnPrivateChannelMessagesToNonMembers() throws Exception {
        // Pedro Soporte is NOT a member of Private Channel 22222222-2222-2222-2222-222222222222
        String pedroToken = getAccessToken("pedro.soporte@riwi.io", "123456");
        UUID privateChannelId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        mockMvc.perform(get("/api/channels/" + privateChannelId + "/messages")
                        .header("Authorization", "Bearer " + pedroToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // ROUTE 5: /api/copilot/query (AI Copilot RAG)
    // =========================================================================

    @Test
    @DisplayName("Route 5 [SUCCESS]: Member user queries Copilot for accessible content returns 200 OK with citations")
    public void testCopilotQuerySuccessForMemberContent() throws Exception {
        // Maria Gomez is a member of Backend channel and has access to PostgreSQL migration context
        String mariaToken = getAccessToken("maria.gomez@riwi.io", "123456");

        CopilotQueryRequest queryRequest = CopilotQueryRequest.builder()
                .query("PostgreSQL")
                .build();

        MvcResult result = mockMvc.perform(post("/api/copilot/query")
                        .header("Authorization", "Bearer " + mariaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andReturn();

        CopilotResponseDTO response = objectMapper.readValue(result.getResponse().getContentAsString(), CopilotResponseDTO.class);
        assertFalse(response.getIsRefusedDueToPermissionsOrContext());
        assertFalse(response.getCitations().isEmpty());
    }

    @Test
    @DisplayName("Route 5 [ERROR/REFUSAL]: Non-member queries Copilot for unauthorized content returns explicit refusal")
    public void testCopilotExplicitRefusalForUnauthorizedContent() throws Exception {
        // Pedro Soporte is NOT a member of Backend channel
        String pedroToken = getAccessToken("pedro.soporte@riwi.io", "123456");

        CopilotQueryRequest queryRequest = CopilotQueryRequest.builder()
                .query("¿Qué información secreta hay en el canal privado de Desarrollo Backend?")
                .build();

        MvcResult result = mockMvc.perform(post("/api/copilot/query")
                        .header("Authorization", "Bearer " + pedroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andReturn();

        CopilotResponseDTO copilotResponse = objectMapper.readValue(result.getResponse().getContentAsString(), CopilotResponseDTO.class);
        assertTrue(copilotResponse.getIsRefusedDueToPermissionsOrContext());
        assertTrue(copilotResponse.getAnswer().contains("No poseo permisos o contexto suficiente"));
    }
}
