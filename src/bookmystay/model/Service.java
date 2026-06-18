package bookmystay.model;

/**
 * Represents an add-on service attached to a reservation.
 * Use Case 5: Add-On Service Selection
 */
public class Service {

    public enum ServiceType {
        BREAKFAST, AIRPORT_PICKUP, SPA, LAUNDRY, ROOM_SERVICE
    }

    private ServiceType type;
    private double      cost;
    private String      description;

    public Service(ServiceType type, double cost, String description) {
        this.type        = type;
        this.cost        = cost;
        this.description = description;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public ServiceType getType()        { return type; }
    public double      getCost()        { return cost; }
    public String      getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("Service[%s | cost=%.2f | desc=%s]",
                type, cost, description);
    }
}
