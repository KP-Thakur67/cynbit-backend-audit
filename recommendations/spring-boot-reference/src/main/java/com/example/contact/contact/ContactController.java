package com.example.contact.contact;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactResponse> submit(@Valid @RequestBody ContactRequest request) {
        // Honeypot filled => bot. Answer exactly like a success so it learns nothing, and do no work.
        if (request.website() != null && !request.website().isBlank()) {
            return ResponseEntity.accepted().body(new ContactResponse("received", UUID.randomUUID().toString()));
        }
        String ref = service.accept(request);
        return ResponseEntity.accepted().body(new ContactResponse("received", ref)); // 202: queued, not yet delivered
    }
}
