package bookmystay.service;

import bookmystay.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════
 * USE CASE 2: Room Search & Availability Check
 * Branch  : feature/room-search-availability
 * ═══════════════════════════════════════════════════════
 *
 * Data Structures Used:
 *   Read-only access to HashMap<String, Room> from InventoryService
 *
 * Responsibilities:
 *   - Display available room types
 *   - Show pricing and amenities
 *   - Prevent booking unavailable rooms (defensive check)
 *
 * Design Note:
 *   This service is purely read-only — it NEVER mutates inventory.
 *   All checks call InventoryService; no direct map manipulation here.
 */
public class SearchService {

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final InventoryService inventoryService;

    // ── Constructor ───────────────────────────────────────────────────────────

    public SearchService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns a list of all Room objects that currently have availability > 0.
     * Read-only – no side effects.
     */
    public List<Room> searchAvailableRooms() {
        Map<String, Room> allRooms = inventoryService.getAllRooms();
        List<Room> available = new ArrayList<>();

        for (Room room : allRooms.values()) {
            if (room.isAvailable()) {
                available.add(room);
            }
        }
        return available;
    }

    /**
     * Searches rooms within a given price range and returns matching available ones.
     */
    public List<Room> searchByPriceRange(double minPrice, double maxPrice) {
        List<Room> result = new ArrayList<>();
        for (Room room : inventoryService.getAllRooms().values()) {
            if (room.isAvailable()
                    && room.getPricePerNight() >= minPrice
                    && room.getPricePerNight() <= maxPrice) {
                result.add(room);
            }
        }
        return result;
    }

    /**
     * Checks whether a specific room type is bookable.
     * Returns false (not throws) so calling code can show a friendly message.
     */
    public boolean isRoomAvailable(String roomType) {
        Room room = inventoryService.getRoom(roomType);
        return room != null && room.isAvailable();
    }

    /**
     * Prints a formatted listing of all available rooms.
     */
    public void displayAvailableRooms() {
        List<Room> available = searchAvailableRooms();
        System.out.println("\n========== Available Rooms ==========");
        if (available.isEmpty()) {
            System.out.println("  [No rooms currently available]");
        } else {
            for (Room room : available) {
                System.out.printf("  %-10s | Rooms Left: %2d | Price: %8.2f/night | %s%n",
                        room.getType(), room.getAvailableCount(),
                        room.getPricePerNight(), room.getAmenities());
            }
        }
        System.out.println("=====================================\n");
    }

    /**
     * Prints details of rooms within the specified price band.
     */
    public void displayRoomsByPriceRange(double min, double max) {
        List<Room> results = searchByPriceRange(min, max);
        System.out.printf("%n=== Rooms between %.2f and %.2f ===%n", min, max);
        if (results.isEmpty()) {
            System.out.println("  [No rooms match this price range]");
        } else {
            results.forEach(r -> System.out.printf(
                    "  %-10s | %.2f/night | %s%n",
                    r.getType(), r.getPricePerNight(), r.getAmenities()));
        }
        System.out.println();
    }
}

