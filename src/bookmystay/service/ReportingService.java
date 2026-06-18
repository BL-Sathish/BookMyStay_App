package bookmystay.service;

import bookmystay.model.Reservation;
import bookmystay.model.Reservation.Status;
import bookmystay.model.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════
 * USE CASE 6: Booking History & Reporting
 * Branch  : feature/booking-history-reporting
 * ═══════════════════════════════════════════════════════
 *
 * Data Structures Used:
 *   List<Reservation> (ArrayList)
 *   → Ordered, index-accessible, supports iteration
 *
 * Responsibilities:
 *   - Store all confirmed reservations in insertion order
 *   - Support retrieval by guest name, status, date range
 *   - Enable cancellation review
 *   - Generate summary reports for admin
 *
 * Design Note:
 *   ArrayList gives O(1) append and O(n) search — perfectly
 *   suited for an audit log where append is frequent and
 *   sequential scans for reports are acceptable.
 */
public class ReportingService {

    // ── Core Data Structure ───────────────────────────────────────────────────
    private final List<Reservation> bookingHistory;

    // ── Constructor ───────────────────────────────────────────────────────────

    public ReportingService() {
        bookingHistory = new ArrayList<>();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Records a reservation into the history log.
     * Called whenever a reservation is confirmed or its status changes.
     */
    public void record(Reservation reservation) {
        bookingHistory.add(reservation);
        System.out.println("[Report] Recorded → " + reservation.getReservationId()
                + " | Status: " + reservation.getStatus());
    }

    /**
     * Returns an unmodifiable view of the entire booking history.
     */
    public List<Reservation> getAllHistory() {
        return Collections.unmodifiableList(bookingHistory);
    }

    /**
     * Filters history by guest name (case-insensitive substring match).
     */
    public List<Reservation> getByGuestName(String name) {
        String lower = name.toLowerCase();
        return bookingHistory.stream()
                .filter(r -> r.getGuestName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Filters history by reservation status.
     */
    public List<Reservation> getByStatus(Status status) {
        return bookingHistory.stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Filters reservations whose check-in date falls within [from, to].
     */
    public List<Reservation> getByCheckInRange(LocalDate from, LocalDate to) {
        return bookingHistory.stream()
                .filter(r -> !r.getCheckIn().isBefore(from) && !r.getCheckIn().isAfter(to))
                .collect(Collectors.toList());
    }

    /**
     * Calculates total revenue from all CONFIRMED reservations.
     */
    public double totalRevenue() {
        return bookingHistory.stream()
                .filter(r -> r.getStatus() == Status.CONFIRMED)
                .mapToDouble(Reservation::getGrandTotal)
                .sum();
    }

    /**
     * Prints a full booking history report to stdout.
     */
    public void printFullReport() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║               BOOKMYSTAY – FULL BOOKING REPORT              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        if (bookingHistory.isEmpty()) {
            System.out.println("  [No bookings recorded yet]");
        } else {
            for (Reservation r : bookingHistory) {
                System.out.println();
                System.out.printf("  %-18s %s%n",    "Reservation ID:",  r.getReservationId());
                System.out.printf("  %-18s %s%n",    "Guest:",           r.getGuestName());
                System.out.printf("  %-18s %s (%s)%n","Room:",           r.getRoomType(), r.getAssignedRoomId());
                System.out.printf("  %-18s %s → %s (%d night/s)%n",
                        "Stay:", r.getCheckIn(), r.getCheckOut(), r.getNights());
                System.out.printf("  %-18s ₹%.2f%n", "Room Cost:",       r.getRoomCost());

                List<Service> services = r.getServices();
                if (!services.isEmpty()) {
                    System.out.printf("  %-18s%n", "Add-Ons:");
                    services.forEach(s -> System.out.printf("    • %-18s ₹%.2f%n", s.getType(), s.getCost()));
                }

                System.out.printf("  %-18s ₹%.2f%n", "Grand Total:",     r.getGrandTotal());
                System.out.printf("  %-18s %s%n",    "Status:",           r.getStatus());
                System.out.println("  " + "─".repeat(55));
            }

            long confirmed  = bookingHistory.stream().filter(r -> r.getStatus() == Status.CONFIRMED).count();
            long cancelled  = bookingHistory.stream().filter(r -> r.getStatus() == Status.CANCELLED).count();

            System.out.printf("%n  Total Reservations : %d%n",   bookingHistory.size());
            System.out.printf("  Confirmed          : %d%n",    confirmed);
            System.out.printf("  Cancelled          : %d%n",    cancelled);
            System.out.printf("  Total Revenue      : ₹%.2f%n", totalRevenue());
        }
        System.out.println("═".repeat(64) + "\n");
    }

    /**
     * Prints a brief summary (admin dashboard style).
     */
    public void printSummary() {
        long confirmed = bookingHistory.stream().filter(r -> r.getStatus() == Status.CONFIRMED).count();
        long cancelled = bookingHistory.stream().filter(r -> r.getStatus() == Status.CANCELLED).count();

        System.out.println("\n========== Booking Summary ==========");
        System.out.printf("  Total Bookings  : %d%n", bookingHistory.size());
        System.out.printf("  Confirmed       : %d%n", confirmed);
        System.out.printf("  Cancelled       : %d%n", cancelled);
        System.out.printf("  Total Revenue   : ₹%.2f%n", totalRevenue());
        System.out.println("=====================================\n");
    }
}
