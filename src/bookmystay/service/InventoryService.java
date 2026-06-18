package bookmystay.service;

import bookmystay.model.Room;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class InventoryService {

    
    private final Map<String, Room> inventory;

  
    public InventoryService() {
        inventory = new HashMap<>();
        seedDefaultRooms();
    }

   
    public void addRoomType(String type, int count, double pricePerNight, String amenities) {
        if (inventory.containsKey(type)) {
            throw new IllegalArgumentException("Room type already exists: " + type);
        }
        inventory.put(type, new Room(type, count, pricePerNight, amenities));
        System.out.println("[Inventory] Room type added: " + type);
    }

    /**
     * Updates the available count for a room type.
     * Negative counts are rejected.
     */
    public void updateRoomCount(String type, int newCount) {
        Room room = getRoomOrThrow(type);
        if (newCount < 0) {
            throw new IllegalArgumentException("Count cannot be negative.");
        }
        room.setAvailableCount(newCount);
        System.out.println("[Inventory] Updated count for " + type + " → " + newCount);
    }

    /**
     * Updates the nightly price for a room type.
     */
    public void updateRoomPrice(String type, double newPrice) {
        Room room = getRoomOrThrow(type);
        room.setPricePerNight(newPrice);
        System.out.println("[Inventory] Updated price for " + type + " → " + newPrice);
    }

    /**
     * Returns an unmodifiable view of the entire inventory map.
     */
    public Map<String, Room> getAllRooms() {
        return Collections.unmodifiableMap(inventory);
    }

    /**
     * Returns a specific Room object by type.
     */
    public Room getRoom(String type) {
        return inventory.get(type);
    }

    /**
     * Decrements the available count by 1 when a booking is confirmed.
     * Called by BookingService after allocation.
     */
    public void decrementCount(String type) {
        Room room = getRoomOrThrow(type);
        if (room.getAvailableCount() <= 0) {
            throw new IllegalStateException("No rooms available to decrement for: " + type);
        }
        room.setAvailableCount(room.getAvailableCount() - 1);
    }

    /**
     * Increments the available count by 1 on cancellation.
     */
    public void incrementCount(String type) {
        Room room = getRoomOrThrow(type);
        room.setAvailableCount(room.getAvailableCount() + 1);
    }

    /**
     * Prints the full inventory summary to stdout.
     */
    public void printInventory() {
        System.out.println("\n========== Current Inventory ==========");
        if (inventory.isEmpty()) {
            System.out.println("  [No rooms in inventory]");
        } else {
            inventory.forEach((type, room) ->
                System.out.printf("  %-10s | Count: %3d | Price: %8.2f/night | %s%n",
                        type, room.getAvailableCount(), room.getPricePerNight(), room.getAmenities()));
        }
        System.out.println("=======================================\n");
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private void seedDefaultRooms() {
        inventory.put("Single", new Room("Single", 10, 2500.00, "WiFi, TV, AC"));
        inventory.put("Double", new Room("Double",  6, 4500.00, "WiFi, TV, AC, Mini-bar"));
        inventory.put("Suite",  new Room("Suite",   3, 9000.00, "WiFi, TV, AC, Jacuzzi, Lounge"));
        System.out.println("[Inventory] Default rooms seeded: Single, Double, Suite");
    }

    private Room getRoomOrThrow(String type) {
        Room room = inventory.get(type);
        if (room == null) {
            throw new IllegalArgumentException("Unknown room type: " + type);
        }
        return room;
    }
}

