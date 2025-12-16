Feature: Booking and Scheduling
  As a renter or item owner
  I want to manage bookings and availability
  So that rentals are scheduled correctly

  Scenario: Select available rental dates
    Given the renter is viewing an item
    When the renter selects available dates
    Then the selected dates are marked for booking

  Scenario: View unavailable booking dates
    Given the renter is viewing an item
    When the renter checks the availability calendar
    Then unavailable dates are shown as blocked

  Scenario: Book an item
    Given the renter has selected valid rental dates
    When the renter confirms the booking
    Then the item is reserved for those dates

  Scenario: Complete payment during booking
    Given the renter is booking an item
    When the renter fills in the payment form
    Then the payment is processed successfully

  Scenario: Prevent double bookings
    Given an item is already booked for certain dates
    When another renter attempts to book the same item for those dates
    Then the booking is rejected

  Scenario: Item owner monitors booking requests
    Given the item owner has listed an item
    When a booking request is made
    Then the item owner can view and monitor the request

  Scenario: Item owner views booking calendar
    Given the item owner is logged in
    When the owner opens the booking calendar
    Then all bookings and availability are displayed
