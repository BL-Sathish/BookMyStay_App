package bookmystay.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a confirmed hotel reservation.
 * Used across Use Cases 3, 4, 5, and 6.
 */
public class Reservation {

    public enum Status { PENDING, CONFIRMED, CANCELLED }

    private static int idCounter = 1000;  // auto-increment seed

    private String          reservationId;
    private String          guestName;
    private String          roomType;
    private String          assignedRoomId;  // e.g., "SINGLE-101"
    private LocalDate       checkIn;
    private LocalDate       checkOut;
    private double          roomCost;
    private Status          status;
    private List<Service>   services;        // add-on services

    public Reservation(String guestName, String roomType,
                       LocalDate checkIn, LocalDate checkOut, double pricePerNight) {
        this.reservationId  = "RES-" + (++idCounter);
        this.guestName      = guestName;
        this.roomType       = roomType;
        this.checkIn        = checkIn;
        this.checkOut       = checkOut;
        long nights         = checkOut.toEpochDay() - checkIn.toEpochDay();
        this.roomCost       = pricePerNight * nights;
        this.status         = Status.PENDING;
        this.services       = new ArrayList<>();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String        getReservationId()  { return reservationId; }
    public String        getGuestName()      { return guestName; }
    public String        getRoomType()       { return roomType; }
    public String        getAssignedRoomId() { return assignedRoomId; }
    public LocalDate     getCheckIn()        { return checkIn; }
    public LocalDate     getCheckOut()       { return checkOut; }
    public double        getRoomCost()       { return roomCost; }
    public Status        getStatus()         { return status; }
    public List<Service> getServices()       { return services; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setAssignedRoomId(String id) { this.assignedRoomId = id; }
    public void setStatus(Status status)     { this.status = status; }

    // ── Business Helpers ─────────────────────────────────────────────────────

    public void addService(Service service) {
        services.add(service);
    }

    public double getTotalServiceCost() {
        return services.stream().mapToDouble(Service::getCost).sum();
    }

    public double getGrandTotal() {
        return roomCost + getTotalServiceCost();
    }

    public long getNights() {
        return checkOut.toEpochDay() - checkIn.toEpochDay();
    }

    @Override
    public String toString() {
        return String.format(
            "Reservation[%s | Guest=%s | Room=%s(%s) | %s to %s | Nights=%d | Total=%.2f | Status=%s]",
            reservationId, guestName, roomType, assignedRoomId,
            checkIn, checkOut, getNights(), getGrandTotal(), status);
    }
}
