package org.example.model;

public record Ticket(
        String ticketId,
        String bookingId,
        String seatId,
        String eTicketCode
) {}