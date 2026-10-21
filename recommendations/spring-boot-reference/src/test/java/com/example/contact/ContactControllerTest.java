package com.example.contact;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.contact.config.SecurityConfig;
import com.example.contact.contact.ContactController;
import com.example.contact.contact.ContactService;
import com.example.contact.web.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContactController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class ContactControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ContactService service;

    private static String body(String email, String website) {
        return """
            {"name":"Test User","email":"%s","message":"This is a dummy test message.","website":%s}
            """.formatted(email, website);
    }

    @Test
    void validRequestIsAccepted() throws Exception {
        when(service.accept(any())).thenReturn("ref-1");
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON)
                        .content(body("user@example.com", "null")))
           .andExpect(status().isAccepted())
           .andExpect(jsonPath("$.referenceId").value("ref-1"));
    }

    @Test
    void emailWithoutTldIsRejectedWithFieldError() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON)
                        .content(body("absc@gma", "null")))
           .andExpect(status().isUnprocessableEntity())
           .andExpect(jsonPath("$.errors.email").exists());
        verify(service, never()).accept(any());
    }

    @Test
    void honeypotIsSilentlyAcceptedWithoutWork() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON)
                        .content(body("user@example.com", "\"http://spam.example\"")))
           .andExpect(status().isAccepted());
        verify(service, never()).accept(any());
    }
}
