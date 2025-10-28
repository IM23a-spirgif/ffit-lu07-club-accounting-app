package ch.bzz.controller;

import ch.bzz.generated.api.BookingApi;
import ch.bzz.generated.model.Booking;
import ch.bzz.generated.model.UpdateBookingsRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class BookingApiController implements BookingApi {
    @Override
    public ResponseEntity<List<Booking>> getBookings() {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateBookings(UpdateBookingsRequest updateBookingsRequest) {
        return null;
    }
}
