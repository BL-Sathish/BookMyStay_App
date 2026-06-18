package bookmystay.model;

/**
 * Represents a room type in the hotel inventory.
 * Use Case 1: Room Inventory Setup & Management
 */
public class Room {

    private String type;        // e.g., "Single", "Double", "Suite"
    private int availableCount;
    private double pricePerNight;
    private String amenities;

    public Room(String type, int availableCount, double pricePerNight, String amenities) {
        this.type           = type;
        this.availableCount = availableCount;
        this.pricePerNight  = pricePerNight;
        this.amenities      = amenities;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getType()           { return type; }
    public int    getAvailableCount() { return availableCount; }
    public double getPricePerNight()  { return pricePerNight; }
    public String getAmenities()      { return amenities; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setAvailableCount(int count) { this.availableCount = count; }
    public void setPricePerNight(double price) { this.pricePerNight = price; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean isAvailable() { return availableCount > 0; }

    @Override
    public String toString() {
        return String.format("Room[type=%s | count=%d | price=%.2f/night | amenities=%s]",
                type, availableCount, pricePerNight, amenities);
    }
}
