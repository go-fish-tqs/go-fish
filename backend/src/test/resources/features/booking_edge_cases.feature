Feature: Booking Edge Cases
  As a user of the platform
  I want the booking system to handle edge cases properly
  So that the platform is robust and reliable

  # Booking Date Validation Edge Cases
  Scenario: Reject booking with past start date
    Given I am logged in as a renter
    And I am viewing an available item
    When I try to book the item with a start date in the past
    Then the booking should be rejected
    And I should see an error message "Start date cannot be in the past"

  Scenario: Reject booking with end date before start date
    Given I am logged in as a renter
    And I am viewing an available item
    When I set the end date before the start date
    Then the booking should be rejected
    And I should see an error message "End date must be after start date"

  Scenario: Reject booking for same day (minimum rental period)
    Given I am logged in as a renter
    And I am viewing an available item
    When I try to book the item for the same day start and end
    Then the booking should be rejected with minimum rental period error

  # Booking Conflict Edge Cases
  Scenario: Detect overlapping booking at start
    Given an item is booked from January 10 to January 15
    When another user tries to book from January 8 to January 12
    Then the booking should be rejected due to date conflict

  Scenario: Detect overlapping booking at end
    Given an item is booked from January 10 to January 15
    When another user tries to book from January 14 to January 18
    Then the booking should be rejected due to date conflict

  Scenario: Detect booking contained within existing booking
    Given an item is booked from January 10 to January 20
    When another user tries to book from January 12 to January 18
    Then the booking should be rejected due to date conflict

  Scenario: Detect booking that contains existing booking
    Given an item is booked from January 12 to January 15
    When another user tries to book from January 10 to January 20
    Then the booking should be rejected due to date conflict

  Scenario: Allow back-to-back bookings
    Given an item is booked from January 10 to January 15
    When another user tries to book from January 15 to January 20
    Then the booking should be accepted

  # Owner Self-Booking Edge Cases
  Scenario: Reject owner booking their own item
    Given I am logged in as an item owner
    When I try to book my own item
    Then the booking should be rejected
    And I should see an error message "You cannot book your own item"

  # Suspended User Edge Cases
  Scenario: Suspended user cannot create new bookings
    Given I am logged in as a suspended user
    When I try to create a new booking
    Then the booking should be rejected
    And I should see an error message about account suspension

  # Payment Edge Cases
  Scenario: Handle payment failure gracefully
    Given I am logged in as a renter
    And I have created a booking
    When the payment processing fails
    Then the booking should remain in pending status
    And I should see an error message about payment failure

  Scenario: Prevent double payment for same booking
    Given I have already paid for a booking
    When I try to pay for the same booking again
    Then the payment should be rejected
    And I should see a message that the booking is already paid

  # Cancellation Edge Cases
  Scenario: Cancel booking before start date
    Given I have an upcoming booking
    When I cancel the booking before the start date
    Then the booking should be cancelled
    And the dates should become available again

  Scenario: Reject cancellation of already started booking
    Given I have a booking that has already started
    When I try to cancel the booking
    Then the cancellation should be rejected

  Scenario: Reject cancellation of completed booking
    Given I have a booking that has already ended
    When I try to cancel the booking
    Then the cancellation should be rejected

  # Item Availability Edge Cases
  Scenario: Booking unavailable item fails
    Given an item is marked as unavailable
    When I try to book that item
    Then the booking should be rejected
    And I should see an error message about item unavailability

  Scenario: Booking deactivated item fails
    Given an item has been deactivated by admin
    When I try to book that item
    Then the booking should be rejected
    And I should see an error message about item being deactivated
