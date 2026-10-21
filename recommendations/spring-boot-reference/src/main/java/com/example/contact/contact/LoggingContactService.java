package com.example.contact.contact;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class LoggingContactService implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(LoggingContactService.class);

    @Override
    public String accept(ContactRequest request) {
        String ref = UUID.randomUUID().toString();
        // Log the reference only. Never log message bodies or emails (personal data).
        log.info("contact accepted ref={}", ref);
        return ref;
    }
}
