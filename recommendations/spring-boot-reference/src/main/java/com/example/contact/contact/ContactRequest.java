package com.example.contact.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Server-side validation is the source of truth (F-02). Field names are illustrative.
 * Note: Hibernate's @Email accepts "user@host" (no TLD), so a @Pattern enforces a dotted domain.
 */
public record ContactRequest(
        @NotBlank @Size(max = 100) String name,

        @NotBlank @Size(max = 254)
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$", message = "must be a valid email address")
        String email,

        @Pattern(regexp = "^[+0-9 ()-]{7,20}$", message = "must be a valid phone number")
        String phone,

        @NotBlank @Size(min = 10, max = 2000) String message,

        /** Honeypot: hidden in the UI, humans leave it empty, bots fill it. */
        @Size(max = 200) String website) {
}
