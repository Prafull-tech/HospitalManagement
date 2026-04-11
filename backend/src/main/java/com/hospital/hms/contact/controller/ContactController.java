package com.hospital.hms.contact.controller;

import com.hospital.hms.contact.entity.ContactMessage;
import com.hospital.hms.contact.repository.ContactMessageRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/contact")
@Validated
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactMessageRepository repository;

    public ContactController(ContactMessageRepository repository) {
        this.repository = repository;
    }

    public static class ContactRequest {
        @NotBlank @Size(max = 255) private String name;
        @NotBlank @Size(max = 255) private String email;
        @Size(max = 20) private String phone;
        @Size(max = 255) private String subject;
        @NotBlank @Size(max = 5000) private String message;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PostMapping
    public ResponseEntity<?> submitContact(@RequestBody @Validated ContactRequest request) {
        ContactMessage msg = new ContactMessage();
        msg.setName(request.getName().trim());
        msg.setEmail(request.getEmail().trim());
        if (request.getPhone() != null) msg.setPhone(request.getPhone().trim());
        if (request.getSubject() != null) msg.setSubject(request.getSubject().trim());
        msg.setMessage(request.getMessage().trim());
        repository.save(msg);
        log.info("Contact message received from email={}", msg.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Message received. We will get back to you soon."));
    }
}
