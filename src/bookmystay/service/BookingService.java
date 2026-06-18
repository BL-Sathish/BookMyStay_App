package bookmystay.service;

import bookmystay.model.Reservation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ═══════════════════════════════════════════════════════
 * USE CASE 4: Reservation Confirmation & Room Allocation
 * Branch  : feature/reservation-confirmation
 * ═══════════════════════════════════════════════════════
 *
 * Data Structures Used:
 *   Set<String>              → bookedRoomIds  (HashSet for O(1) dup-check)
 *   HashMap<String, Set<String>> → roomType → Set of assigned room IDs
 *
 * Responsibilities:
 *   - Assign a unique room ID to each confirmed reservation
 *   - Prevent reuse of already-assigned room IDs (zero double-booking)
 *   - Atomically decrement inventory count after allocation
 *
 * Design Note:
 *   HashSet.contains() and HashSet.add() are O(1) amortised, making
 *   double-booking checks extremely fast even under load.
 */
public class BookingService {

    // ── Core Data Structures ─────────────────────────────────────────────────
    // Global set of all currently occupied room IDs
    private final Set<String> bookedRoomIds;

    // roomType → set of assigned room IDs for that type
    // e.g., "Single" → {"SINGLE-101", "SINGLE-102"}
    private final Map<String, Set<String>> roomTypeToAssignedIds;

    // Counter per room type to generate sequential IDs
    private final Map<String, Integer> roomTypeCounter;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final InventoryService inventoryService;
    private final SearchService    searchService;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BookingService(InventoryService inventoryService, SearchService searchService) {
        this.inventoryService     = inventoryService;
        this.searchService        = searchService;
        this.bookedRoomIds        = new HashSet<>();
        this.roomTypeToAssignedIds = new HashMap<>();
        this.roomTypeCounter      = new HashMap<>();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Confirms a reservation dequeued from BookingQueueService.
     *
     * Steps:
     *  1. Verify availability via SearchService (defensive read-only check)
     *  2. Generate a unique room ID
     *  3. Add ID to the global bookedRoomIds Set (double-booking guard)
     *  4. Map the ID under the room type in roomTypeToAssignedIds
     *  5. Decrement inventory count atomically
     *  6. Mark reservation as CONFIRMED
     *
     * @return true if confirmed successfully, false if no rooms available
     */
    public boolean confirmReservation(Reservation reservation) {
        String roomType = reservation.getRoomType();

        // Step 1: Availability guard
        if (!searchService.isRoomAvailable(roomType)) {
            System.out.println("[Booking] REJECTED – No " + roomType + " rooms available for "
                    + reservation.getGuestName());
            reservation.setStatus(Reservation.Status.CANCELLED);
            return false;
        }

        // Step 2: Generate unique room ID
        String roomId = generateUniqueRoomId(roomType);

        // Step 3: Double-booking guard using HashSet
        if (bookedRoomIds.contains(roomId)) {
            // This should never happen with sequential generation, but guard anyway
            System.out.println("[Booking] CONFLICT – Room ID already booked: " + roomId);
            return false;
        }
        bookedRoomIds.add(roomId);

        // Step 4: Update roomType → assigned IDs map
        roomTypeToAssignedIds
                .computeIfAbsent(roomType, k -> new HashSet<>())
                .add(roomId);

        // Step 5: Decrement inventory
        inventoryService.decrementCount(roomType);

        // Step 6: Finalise reservation
        reservation.setAssignedRoomId(roomId);
        reservation.setStatus(Reservation.Status.CONFIRMED);

        System.out.printf("[Booking] CONFIRMED → %s | Guest: %s | Assigned Room: %s%n",
                reservation.getReservationId(), reservation.getGuestName(), roomId);
        return true;
    }

    /**
     * Cancels an existing confirmed reservation and releases the room.
     */
    public boolean cancelReservation(Reservation reservation) {
        if (reservation.getStatus() != Reservation.Status.CONFIRMED) {
            System.out.println("[Booking] Cannot cancel – reservation is not CONFIRMED: "
                    + reservation.getReservationId());
            return false;
        }

        String roomId   = reservation.getAssignedRoomId();
        String roomType = reservation.getRoomType();

        // Release the room ID
        bookedRoomIds.remove(roomId);
        Set<String> assigned = roomTypeToAssignedIds.get(roomType);
        if (assigned != null) assigned.remove(roomId);

        // Restore inventory
        inventoryService.incrementCount(roomType);
        reservation.setStatus(Reservation.Status.CANCELLED);

        System.out.printf("[Booking] CANCELLED → %s | Room %s released back to inventory%n",
                reservation.getReservationId(), roomId);
        return true;
    }

    /**
     * Returns an unmodifiable view of all currently booked room IDs.
     */
    public Set<String> getAllBookedRoomIds() {
        return java.util.Collections.unmodifiableSet(bookedRoomIds);
    }

    /**
     * Prints a summary of allocations per room type.
     */
    public void printAllocationSummary() {
        System.out.println("\n========== Room Allocation Summary ==========");
        if (roomTypeToAssignedIds.isEmpty()) {
            System.out.println("  [No rooms allocated yet]");
        } else {
            roomTypeToAssignedIds.forEach((type, ids) ->
                    System.out.printf("  %-10s → %s%n", type, ids));
        }
        System.out.println("=============================================\n");
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Generates a unique, sequential room ID for the given type.
     * Format: "SINGLE-101", "DOUBLE-201", "SUITE-301"
     */
    private String generateUniqueRoomId(String roomType) {
        int baseNumber = getBaseNumber(roomType);
        int seq        = roomTypeCounter.merge(roomType, 1, Integer::sum); // increment & return
        return roomType.toUpperCase() + "-" + (baseNumber + seq);
    }

    private int getBaseNumber(String roomType) {
        switch (roomType) {
            case "Single": return 100;
            case "Double": return 200;
            case "Suite":  return 300;
            default:       return 900;
        }
    }
}
