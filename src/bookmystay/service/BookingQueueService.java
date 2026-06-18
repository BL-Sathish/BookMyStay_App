package bookmystay.service;

import bookmystay.model.Reservation;

import java.util.LinkedList;
import java.util.Queue;

/**
 * ═══════════════════════════════════════════════════════
 * USE CASE 3: Booking Request (First-Come-First-Served)
 * Branch  : feature/booking-request-queue
 * ═══════════════════════════════════════════════════════
 *
 * Data Structures Used:
 *   Queue<Reservation> implemented via LinkedList
 *   → FIFO guarantees fair, arrival-order processing
 *
 * Responsibilities:
 *   - Accept booking requests and enqueue them
 *   - Enforce FIFO ordering (first-come, first-served)
 *   - Handle high-traffic scenarios fairly
 *
 * Design Note:
 *   Enqueueing is O(1). Dequeueing is O(1).
 *   LinkedList chosen because Queue interface methods
 *   (offer, poll, peek) map cleanly onto it.
 */
public class BookingQueueService {

    // ── Core Data Structure ───────────────────────────────────────────────────
    private final Queue<Reservation> bookingQueue;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BookingQueueService() {
        bookingQueue = new LinkedList<>();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Accepts a new booking request and adds it to the tail of the queue.
     * Returns true if enqueued successfully.
     */
    public boolean enqueue(Reservation reservation) {
        boolean result = bookingQueue.offer(reservation);
        if (result) {
            System.out.printf("[Queue] Enqueued → %s | Guest: %s | RoomType: %s | Queue size: %d%n",
                    reservation.getReservationId(),
                    reservation.getGuestName(),
                    reservation.getRoomType(),
                    bookingQueue.size());
        }
        return result;
    }

    /**
     * Retrieves and removes the head reservation (oldest request) for processing.
     * Returns null if the queue is empty.
     */
    public Reservation dequeue() {
        Reservation head = bookingQueue.poll();
        if (head != null) {
            System.out.printf("[Queue] Dequeued → %s | Guest: %s | Remaining: %d%n",
                    head.getReservationId(), head.getGuestName(), bookingQueue.size());
        } else {
            System.out.println("[Queue] Queue is empty – nothing to dequeue.");
        }
        return head;
    }

    /**
     * Peeks at the head without removing it.
     */
    public Reservation peek() {
        return bookingQueue.peek();
    }

    /**
     * Returns current queue depth.
     */
    public int queueSize() {
        return bookingQueue.size();
    }

    /**
     * Returns true when no pending requests remain.
     */
    public boolean isEmpty() {
        return bookingQueue.isEmpty();
    }

    /**
     * Prints all pending requests in FIFO order (non-destructive).
     */
    public void printQueue() {
        System.out.println("\n========== Pending Booking Queue ==========");
        if (bookingQueue.isEmpty()) {
            System.out.println("  [Queue is empty]");
        } else {
            int pos = 1;
            for (Reservation r : bookingQueue) {
                System.out.printf("  #%d | %s | Guest: %-15s | RoomType: %s%n",
                        pos++, r.getReservationId(), r.getGuestName(), r.getRoomType());
            }
        }
        System.out.println("===========================================\n");
    }
}
