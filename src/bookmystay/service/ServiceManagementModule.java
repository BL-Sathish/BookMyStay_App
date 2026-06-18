package bookmystay.service;

import bookmystay.model.Reservation;
import bookmystay.model.Service;
import bookmystay.model.Service.ServiceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════
 * USE CASE 5: Add-On Service Selection
 * Branch  : feature/addon-service-selection
 * ═══════════════════════════════════════════════════════
 *
 * Data Structures Used:
 *   Map<String, List<Service>>
 *   Key   → reservationId
 *   Value → list of services attached to that booking
 *
 * Responsibilities:
 *   - Attach add-on services (breakfast, spa, airport pickup, etc.)
 *   - Allow multiple services per booking
 *   - Calculate the total additional cost per reservation
 *
 * Design Note:
 *   One-to-many mapping: one reservation → many services.
 *   List<Service> preserves insertion order and allows duplicates
 *   (e.g., breakfast ordered multiple times for multi-night stays).
 */
public class ServiceManagementModule {

    // ── Core Data Structure ───────────────────────────────────────────────────
    // reservationId → list of add-on services
    private final Map<String, List<Service>> serviceMap;

    // ── Service catalogue (fixed prices) ────────────────────────────────────
    private static final Map<ServiceType, Double> SERVICE_PRICE_CATALOGUE;
    static {
        Map<ServiceType, Double> m = new HashMap<>();
        m.put(ServiceType.BREAKFAST,      350.00);
        m.put(ServiceType.AIRPORT_PICKUP, 800.00);
        m.put(ServiceType.SPA,           1500.00);
        m.put(ServiceType.LAUNDRY,        200.00);
        m.put(ServiceType.ROOM_SERVICE,   250.00);
        SERVICE_PRICE_CATALOGUE = Collections.unmodifiableMap(m);
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public ServiceManagementModule() {
        serviceMap = new HashMap<>();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Attaches an add-on service to a confirmed reservation.
     * Only CONFIRMED reservations can receive add-ons.
     *
     * @return true if attached successfully
     */
    public boolean addService(Reservation reservation, ServiceType serviceType) {
        if (reservation.getStatus() != Reservation.Status.CONFIRMED) {
            System.out.println("[Service] Cannot add service – reservation is not CONFIRMED: "
                    + reservation.getReservationId());
            return false;
        }

        double cost        = SERVICE_PRICE_CATALOGUE.getOrDefault(serviceType, 0.00);
        String description = buildDescription(serviceType);
        Service service    = new Service(serviceType, cost, description);

        // Attach to internal map
        serviceMap.computeIfAbsent(reservation.getReservationId(), k -> new ArrayList<>())
                  .add(service);

        // Also attach to reservation object for grand-total calculation
        reservation.addService(service);

        System.out.printf("[Service] Added %-15s (%.2f) to reservation %s%n",
                serviceType, cost, reservation.getReservationId());
        return true;
    }

    /**
     * Returns all services mapped to a reservation ID.
     * Returns an empty list if none exist.
     */
    public List<Service> getServicesForReservation(String reservationId) {
        return serviceMap.getOrDefault(reservationId, Collections.emptyList());
    }

    /**
     * Calculates the total add-on cost for a given reservation.
     */
    public double calculateServiceCost(String reservationId) {
        return getServicesForReservation(reservationId)
                .stream()
                .mapToDouble(Service::getCost)
                .sum();
    }

    /**
     * Prints the service bill for a reservation.
     */
    public void printServiceBill(Reservation reservation) {
        List<Service> services = getServicesForReservation(reservation.getReservationId());
        System.out.printf("%n========== Add-On Services: %s ==========%n",
                reservation.getReservationId());
        if (services.isEmpty()) {
            System.out.println("  [No add-on services]");
        } else {
            for (Service s : services) {
                System.out.printf("  %-20s  ₹%8.2f%n", s.getType(), s.getCost());
            }
            System.out.printf("  %-20s  ₹%8.2f%n", "--- Services Total ---",
                    calculateServiceCost(reservation.getReservationId()));
        }
        System.out.printf("  %-20s  ₹%8.2f%n", "=== GRAND TOTAL ===", reservation.getGrandTotal());
        System.out.println("==========================================\n");
    }

    /**
     * Prints the service price catalogue.
     */
    public void printCatalogue() {
        System.out.println("\n========== Service Catalogue ==========");
        SERVICE_PRICE_CATALOGUE.forEach((type, price) ->
                System.out.printf("  %-20s ₹%.2f%n", type, price));
        System.out.println("=======================================\n");
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private String buildDescription(ServiceType type) {
        switch (type) {
            case BREAKFAST:      return "Continental breakfast in-room";
            case AIRPORT_PICKUP: return "Pickup/drop from nearest airport";
            case SPA:            return "60-min full-body spa session";
            case LAUNDRY:        return "Same-day laundry & dry-cleaning";
            case ROOM_SERVICE:   return "24/7 in-room dining";
            default:             return "Add-on service";
        }
    }
}
