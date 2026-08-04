package com.cinebook.module.booking.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.common.util.CursorCodec;
import com.cinebook.common.util.CursorPageResponse;
import com.cinebook.module.booking.dto.request.BookingCreateRequest;
import com.cinebook.module.booking.dto.response.BookingResponse;
import com.cinebook.module.booking.dto.response.BookingSummaryResponse;
import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingSeat;
import com.cinebook.module.booking.entity.BookingSnapshot;
import com.cinebook.module.booking.entity.BookingStatus;
import com.cinebook.module.booking.mapper.BookingMapper;
import com.cinebook.module.booking.repository.BookingQueryRepository;
import com.cinebook.module.booking.repository.BookingRepository;
import com.cinebook.module.booking.repository.BookingSeatRepository;
import com.cinebook.module.booking.validator.BookingStatusManager;
import com.cinebook.module.room.service.RoomService;
import com.cinebook.module.seat.entity.Seat;
import com.cinebook.module.seat.entity.SeatType;
import com.cinebook.module.seat.repository.SeatRepository;
import com.cinebook.module.seat.service.SeatService;
import com.cinebook.module.showtime.entity.Showtime;
import com.cinebook.module.showtime.service.ShowtimeService;
import com.cinebook.module.user.entity.User;
import com.cinebook.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingQueryRepository bookingQueryRepository;
    private final SeatRepository seatRepository;

    private final UserService userService;
    private final ShowtimeService showtimeService;
    private final RoomService roomService;
    private final SeatService seatService;

    private final BookingMapper bookingMapper;
    private final CursorCodec cursorCodec;
    private final BookingStatusManager bookingStatusManager;


    // -----------------------------------------------------------
    // Create Booking
    // -----------------------------------------------------------
    @Transactional
    public BookingResponse createBooking(UUID userId, BookingCreateRequest request) {
        User user = null;
        String guestEmail = null;
        String guestPhone = null;
        String bookingCode = null;

        if (userId != null) {
            user = userService.findOrThrow(userId);
        } else {
            if (request.guestEmail() == null || request.guestPhone() == null) {
                throw new CinebookException(ErrorCode.GUEST_INFO_REQUIRED);
            }
            guestEmail = request.guestEmail();
            guestPhone = request.guestPhone();
            bookingCode = generateBookingCode();
        }

        Showtime showtime = showtimeService.findOrThrow(request.showtimeId());

        // --- Validation ---
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(showtime.getStartTime())) {
            throw new CinebookException(ErrorCode.SHOWTIME_ALREADY_STARTED);
        }

        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new CinebookException(ErrorCode.SEAT_NOT_FOUND);
        }
        boolean allBelongToRoom = seats.stream()
                .allMatch(s -> s.getRoom().getId().equals(showtime.getRoom().getId()));
        if (!allBelongToRoom) {
            throw new CinebookException(ErrorCode.SEAT_NOT_IN_ROOM);
        }

        // Naive double-booking check - race condition this does NOT close.
        // Good enough for single-request testing;
        // Phase 6 (Redis lock) is what makes this actually safe.
        List<UUID> seatIds = seats.stream().map(Seat::getId).toList();
        var alreadyBooked = bookingSeatRepository.findActiveByShowtimeAndSeats(showtime.getId(), seatIds);
        if (!alreadyBooked.isEmpty()) {
            throw new CinebookException(ErrorCode.SEAT_ALREADY_BOOKED);
        }

        // Price calculation (server-side only, never trust client)
        boolean isWeekend = showtime.getStartTime().getDayOfWeek() == DayOfWeek.SATURDAY
                || showtime.getStartTime().getDayOfWeek() == DayOfWeek.SUNDAY;

        int totalPrice = 0;
        List<BookingSeatDraft> drafts = new ArrayList<>();
        for (Seat seat : seats) {
            int seatPrice = calculateSeatPrice(showtime.getBasePrice(), seat.getSeatType(), isWeekend);
            totalPrice += seatPrice;
            drafts.add(new BookingSeatDraft(seat, seatPrice));
        }

        // Snapshot, written once at creation time
        BookingSnapshot snapshot = new BookingSnapshot(
                showtime.getMovie().getTitle(),
                showtime.getMovie().getPosterUrl(),
                showtime.getRoom().getCinema().getName(),
                showtime.getRoom().getCinema().getAddress(),
                showtime.getRoom().getName(),
                showtime.getStartTime().toString(),
                showtime.getFormat(),
                seats.stream().map(Seat::label).toList()
        );

        // Single transaction: Booking + all BookingSeat rows
        // either all commit together or all roll back together (method-level @Transactional).
        Booking booking = Booking.builder()
                .showtime(showtime)
                .user(user)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .bookingCode(bookingCode)
                .snapshot(snapshot)
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .bookingTime(Instant.now())
                .build();
        bookingRepository.save(booking);

        for (BookingSeatDraft draft : drafts) {
            bookingSeatRepository.save(BookingSeat.builder()
                    .booking(booking)
                    .seat(draft.seat())
                    .showtimeId(showtime.getId())
                    .seatLabel(draft.seat().label())
                    .priceSnapshot(draft.price())
                    .build());
        }

        return bookingMapper.toResponse(booking, seats.stream().map(Seat::label).toList());
    }

    private record BookingSeatDraft(
            Seat seat,
            int price
    ) {
    }

    private String generateBookingCode() {
        // Format: 2 chars + 5 nums
        return "CB" + String.format("%05d", new java.util.Random().nextInt(100000));
    }

    // -----------------------------------------------------------
    // Booking History
    // -----------------------------------------------------------
    @Transactional
    public CursorPageResponse<BookingSummaryResponse> getBookingHistories(UUID userId, String cursor, int limit) {
        List<Booking> rows = bookingQueryRepository.findByUser(userId, cursor, limit);

        List<BookingSummaryResponse> items = rows.stream().map(b -> {
            List<String> labels = bookingSeatRepository.findAllByBookingId(b.getId()).stream()
                    .map(BookingSeat::getSeatLabel).toList();
            return new BookingSummaryResponse(
                    b.getId(),
                    b.getSnapshot() != null ? b.getSnapshot().movieName() : b.getShowtime().getMovie().getTitle(),
                    b.getSnapshot() != null ? b.getSnapshot().posterUrl() : b.getShowtime().getMovie().getPosterUrl(),
                    labels,
                    b.getShowtime().getStartTime().atZone(ZoneId.systemDefault()).toInstant(),
                    b.getTotalPrice(),
                    b.getStatus()
            );
        }).toList();

        return CursorPageResponse.of(items, limit, cursorCodec,
                item -> {
                    Booking match = rows.stream().filter(r -> r.getId().equals(item.bookingId())).findFirst().orElseThrow();
                    return new CursorCodec.Cursor(match.getCreatedAt(), match.getId());
                });
    }

    // -----------------------------------------------------------
    // Booking Detail
    // -----------------------------------------------------------
    @Transactional
    public BookingResponse getBookingDetail(UUID userId, UUID bookingId) {
        Booking booking = findOrThrow(bookingId);
        assertMemberOwner(booking, userId);

        List<String> labels = bookingSeatRepository.findAllByBookingId(bookingId).stream()
                .map(BookingSeat::getSeatLabel)
                .toList();

        return bookingMapper.toResponse(booking, labels);
    }

    // -----------------------------------------------------------
    // Cancel Booking
    // -----------------------------------------------------------
    @Transactional
    public void cancelBooking(UUID userId, UUID bookingId) {
        Booking booking = findOrThrow(bookingId);
        assertMemberOwner(booking, userId);
        cancel(booking);
    }

    // -----------------------------------------------------------
    // Lookup Booking (GUEST)
    // -----------------------------------------------------------
    @Transactional(readOnly = true)
    public BookingResponse lookupByCodeAndEmail(String bookingCode, String email) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new CinebookException(ErrorCode.BOOKING_NOT_FOUND));

        boolean matches = booking.isGuestBooking()
                ? email.equalsIgnoreCase(booking.getGuestEmail())
                : email.equalsIgnoreCase(booking.getUser().getEmail());

        if (!matches) {
            throw new CinebookException(ErrorCode.BOOKING_ACCESS_DENIED);
        }

        List<String> labels = bookingSeatRepository.findAllByBookingId(booking.getId()).stream()
                .map(BookingSeat::getSeatLabel)
                .toList();

        return bookingMapper.toResponse(booking, labels);
    }

    // -----------------------------------------------------------
    // Cancel Booking (GUEST)
    // -----------------------------------------------------------
    @Transactional
    public void cancelGuestBooking(
            String bookingCode,
            String email
    ) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() ->
                        new CinebookException(ErrorCode.BOOKING_NOT_FOUND)
                );

        assertGuestOwner(booking, email);

        cancel(booking);
    }

    // -----------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------
    private int calculateSeatPrice(int basePrice, SeatType type, boolean isWeekend) {
        int price = basePrice + switch (type) {
            case VIP -> 20000;
            case COUPLE -> basePrice + 50000;
            case WHEELCHAIR, STANDARD -> 0;
        };
        if (isWeekend) {
            price += (int) Math.round(price * 0.10);
        }

        return price;
    }

    private Booking findOrThrow(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new CinebookException(ErrorCode.BOOKING_NOT_FOUND));
    }

    private void assertMemberOwner(Booking booking, UUID userId) {
        if (booking.isGuestBooking()
                || !booking.getUser().getId().equals(userId)) {
            throw new CinebookException(ErrorCode.BOOKING_ACCESS_DENIED);
        }
    }

    private void assertGuestOwner(Booking booking, String email) {
        if (!booking.isGuestBooking() ||
                !booking.getGuestEmail().equalsIgnoreCase(email)) {
            throw new CinebookException(ErrorCode.BOOKING_ACCESS_DENIED);
        }
    }

    private void cancel(Booking booking) {
        bookingStatusManager.changeStatus(
                booking,
                BookingStatus.CANCELLED
        );
    }
}
