package com.example.demo.Model;

/**
 * Outcome of a contact form submission, so the UI can tell the truth about what happened
 * instead of always claiming "sent".
 */
public record ContactDelivery(Status status, String detail) {

    public enum Status {
        /** Handed to the configured SMTP relay. */
        SENT,
        /** No SMTP host configured: the payload was written to the application log. */
        LOGGED,
        /** SMTP was configured but the send failed. */
        FAILED
    }

    public boolean delivered() {
        return status != Status.FAILED;
    }
}
