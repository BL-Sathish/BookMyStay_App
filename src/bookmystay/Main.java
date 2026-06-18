package bookmystay;

import bookmystay.model.Reservation;
import bookmystay.model.Service.ServiceType;
import bookmystay.service.*;

import java.time.LocalDate;


public class Main {

    public static void main(String[] args) {

        // ── Wire up services (dependency injection by hand) ───────────────────
        InventoryService       inventoryService  = new InventoryService();
        SearchService          searchService     = new SearchService(inventoryService);
        BookingQueueService    queueService      = new BookingQueueService();
        BookingService         bookingService    = new BookingService(inventoryService, searchService);
        ServiceManagementModule serviceModule    = new ServiceManagementModule();
        ReportingService       reportingService  = new ReportingService();

        separator("USE CASE 1 – Room Inventory Setup & Management");
        inventoryService.printInventory();
        inventoryService.addRoomType("Penthouse", 2, 15000.00, "WiFi, Rooftop Pool, Butler Service");
        inventoryService.updateRoomPrice("Single", 2800.00);
        inventoryService.printInventory();

        separator("USE CASE 2 – Room Search & Availability Check");
        searchService.displayAvailableRooms();
        searchService.displayRoomsByPriceRange(2000, 5000);
        System.out.println("Is 'Suite' available? → " + searchService.isRoomAvailable("Suite"));
        System.out.println("Is 'Bungalow' available? → " + searchService.isRoomAvailable("Bungalow"));

        separator("USE CASE 3 – Booking Request (First-Come-First-Served)");
        LocalDate today    = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusDays(7);

        // Create pending reservations
        Reservation r1 = new Reservation("Arjun Sharma",   "Single",     today,    today.plusDays(3),  2800.00);
        Reservation r2 = new Reservation("Priya Nair",     "Double",     today,    today.plusDays(5),  4500.00);
        Reservation r3 = new Reservation("Karan Mehta",    "Suite",      tomorrow, tomorrow.plusDays(2), 9000.00);
        Reservation r4 = new Reservation("Divya Reddy",    "Single",     today,    today.plusDays(4),  2800.00);
        Reservation r5 = new Reservation("Rahul Gupta",    "Penthouse",  today,    today.plusDays(2), 15000.00);

        queueService.enqueue(r1);
        queueService.enqueue(r2);
        queueService.enqueue(r3);
        queueService.enqueue(r4);
        queueService.enqueue(r5);
        queueService.printQueue();

        separator("USE CASE 4 – Reservation Confirmation & Room Allocation");
        // Process all pending reservations in FIFO order
        while (!queueService.isEmpty()) {
            Reservation pending = queueService.dequeue();
            boolean confirmed   = bookingService.confirmReservation(pending);
            if (confirmed) {
                reportingService.record(pending);   // record immediately on confirm
            }
        }
        bookingService.printAllocationSummary();

        separator("USE CASE 5 – Add-On Service Selection");
        serviceModule.printCatalogue();

        // Add services to confirmed reservations
        serviceModule.addService(r1, ServiceType.BREAKFAST);
        serviceModule.addService(r1, ServiceType.AIRPORT_PICKUP);
        serviceModule.addService(r2, ServiceType.SPA);
        serviceModule.addService(r2, ServiceType.BREAKFAST);
        serviceModule.addService(r3, ServiceType.ROOM_SERVICE);
        serviceModule.addService(r3, ServiceType.LAUNDRY);
        serviceModule.addService(r3, ServiceType.SPA);

        serviceModule.printServiceBill(r1);
        serviceModule.printServiceBill(r2);
        serviceModule.printServiceBill(r3);

        separator("USE CASE 4b – Cancellation Demo");
        bookingService.cancelReservation(r4);  // cancel Divya's booking
        // Record updated status in report
        reportingService.record(r4);

        separator("USE CASE 6 – Booking History & Reporting");
        reportingService.printFullReport();
        reportingService.printSummary();

        // Filter demos
        System.out.println(">> Bookings by 'Arjun': ");
        reportingService.getByGuestName("Arjun").forEach(System.out::println);

        System.out.println("\n>> CONFIRMED bookings: ");
        reportingService.getByStatus(Reservation.Status.CONFIRMED).forEach(System.out::println);

        System.out.println("\n>> CANCELLED bookings: ");
        reportingService.getByStatus(Reservation.Status.CANCELLED).forEach(System.out::println);

        System.out.println("\n\n✅ BookMyStay demo complete.");
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static void separator(String title) {
        System.out.println("\n" + "═".repeat(64));
        System.out.println("  " + title);
        System.out.println("═".repeat(64));
    }
}
