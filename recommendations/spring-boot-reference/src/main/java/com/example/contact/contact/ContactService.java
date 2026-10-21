package com.example.contact.contact;

/** Port for the real work. In production: write to an outbox table and let a worker send the email/CRM call. */
public interface ContactService {
    /** @return a reference id the user can quote to support */
    String accept(ContactRequest request);
}
