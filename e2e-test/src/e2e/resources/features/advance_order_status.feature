Feature: Advance order status

  Scenario: A client advances the status of a freshly placed order
    Given an authenticated client
    And a valid delivery order
    When the client places the order
    And the client advances the order status
    Then order status is advanced successfully
    And notification-service should have logged the order status change
