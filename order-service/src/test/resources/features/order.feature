# ============================================================
# Feature: Order Microservice
# Aggregates: Order, Schedule, Payment
# Language: Gherkin (BDD) – Cucumber/SpecFlow format
# ============================================================

# ----------------------------------------------------------------
# 1. ORDER CREATION
# ----------------------------------------------------------------

Feature: Order creation

  Background:
    Given the system is operational
    And the product catalog contains at least one available product

  # --- Online Order ---

  Scenario: Create an online delivery order
    Given an authenticated customer with a valid delivery address
    When the customer sends the command "CreateOnlineOrder" with type "DELIVERY" and at least one product
    Then the event "OrderCreated" is emitted
    And the order has status "CREATED"
    And the order contains the selected products
    And the order type is "DELIVERY"

  Scenario: Create an online pickup order
    Given an authenticated customer
    When the customer sends the command "CreateOnlineOrder" with type "PICKUP" and at least one product
    Then the event "OrderCreated" is emitted
    And the order has status "CREATED"
    And the order type is "PICKUP"

  Scenario: Attempt to create an online order with no products
    Given an authenticated customer
    When the customer sends the command "CreateOnlineOrder" with an empty product list
    Then the command is rejected with error "PRODUCTS_REQUIRED"
    And no event is emitted

  Scenario: Attempt to create an online order with an unavailable product
    Given an authenticated customer
    And the product "PIZZA_MARGHERITA" is not available
    When the customer sends the command "CreateOnlineOrder" with product "PIZZA_MARGHERITA"
    Then the command is rejected with error "PRODUCT_NOT_AVAILABLE"

  # --- Table Order ---

  Scenario: Create a table order
    Given an authenticated operator
    And table "T5" is occupied
    When the operator sends the command "CreateTableOrder" for table "T5" with at least one product
    Then the event "TableOrderCreated" is emitted
    And the order has status "CREATED"
    And the order type is "TABLE"
    And the order is associated with table "T5"

  Scenario: Attempt to create a table order for a non-existent table
    Given an authenticated operator
    When the operator sends the command "CreateTableOrder" for table "T99" (non-existent)
    Then the command is rejected with error "TABLE_NOT_FOUND"

# ----------------------------------------------------------------
# 2. ORDER MODIFICATION
# ----------------------------------------------------------------

Feature: Order modification

  Background:
    Given an order with id "ORD-001" exists in status "CREATED"

  Scenario: Modify products of an existing order
    Given order "ORD-001" is in status "CREATED"
    When the command "ModifyOrder" is sent with new products ["PIZZA_DIAVOLA", "WATER"]
    Then order "ORD-001" contains exactly the products ["PIZZA_DIAVOLA", "WATER"]
    And the event "OrderModified" is emitted

  Scenario: Attempt to modify products of an order in preparation
    Given order "ORD-001" is in status "PREPARATION_STARTED"
    When the command "ModifyOrder" is sent for order "ORD-001"
    Then the command is rejected with error "ORDER_NOT_MODIFIABLE"
    And order "ORD-001" remains unchanged

  Scenario: Modify the delivery time of a delivery order
    Given order "ORD-001" is of type "DELIVERY" and in status "CREATED"
    When the command "ModifyDeliveryOrder" is sent with new time "20:30"
    Then order "ORD-001" has delivery time "20:30"
    And the event "DeliveryOrderModified" is emitted

  Scenario: Modify the delivery address of a delivery order
    Given order "ORD-001" is of type "DELIVERY" and in status "CREATED"
    When the command "ModifyDeliveryOrder" is sent with new address "10 Main Street"
    Then order "ORD-001" has delivery address "10 Main Street"

  Scenario: Attempt to modify delivery time on a pickup order
    Given order "ORD-001" is of type "PICKUP"
    When the command "ModifyDeliveryOrder" is sent for order "ORD-001"
    Then the command is rejected with error "WRONG_ORDER_TYPE"

  Scenario: Modify the pickup time of a pickup order
    Given order "ORD-001" is of type "PICKUP" and in status "CREATED"
    When the command "ModifyPickupOrder" is sent with new time "19:00"
    Then order "ORD-001" has pickup time "19:00"
    And the event "PickupOrderModified" is emitted

# ----------------------------------------------------------------
# 3. ORDER CANCELLATION
# ----------------------------------------------------------------

Feature: Order cancellation

  Scenario: Cancel a delivery order
    Given order "ORD-002" of type "DELIVERY" exists in status "CREATED"
    When the command "CancelDeliveryOrder" is sent for order "ORD-002"
    Then order "ORD-002" has status "CANCELLED"
    And the event "DeliveryOrderCancelled" is emitted

  Scenario: Cancel a pickup order
    Given order "ORD-003" of type "PICKUP" exists in status "CREATED"
    When the command "CancelPickupOrder" is sent for order "ORD-003"
    Then order "ORD-003" has status "CANCELLED"
    And the event "PickupOrderCancelled" is emitted

  Scenario: Attempt to cancel an already cancelled order
    Given order "ORD-004" exists in status "CANCELLED"
    When the command "CancelDeliveryOrder" is sent for order "ORD-004"
    Then the command is rejected with error "ORDER_ALREADY_CANCELLED"

  Scenario: Delete an order permanently
    Given order "ORD-005" exists in status "CREATED"
    When the command "DeleteOrder" is sent for order "ORD-005"
    Then order "ORD-005" no longer exists in the system
    And the event "OrderDeleted" is emitted

  Scenario: Attempt to delete a confirmed order
    Given order "ORD-006" exists in status "CONFIRMED"
    When the command "DeleteOrder" is sent for order "ORD-006"
    Then the command is rejected with error "ORDER_NOT_DELETABLE"

# ----------------------------------------------------------------
# 4. ORDER CONFIRMATION (POLICY)
# ----------------------------------------------------------------

Feature: Order confirmation via Policy

  Scenario: Automatic confirmation of a delivery order after creation
    Given the event "OrderCreated" was just emitted for a "DELIVERY" order
    When the Policy processes the event
    Then the event "DeliveryOrderConfirmed" is emitted
    And the order has status "CONFIRMED"

  Scenario: Automatic confirmation of a pickup order after creation
    Given the event "OrderCreated" was just emitted for a "PICKUP" order
    When the Policy processes the event
    Then the event "PickupOrderConfirmed" is emitted
    And the order has status "CONFIRMED"

  Scenario: Order not confirmed when payment is still pending
    Given order "ORD-007" is "ONLINE" with payment pending
    When the Policy receives the event "OrderCreated"
    Then the Policy does not emit "DeliveryOrderConfirmed" until payment is processed
    And the order remains in status "PENDING_PAYMENT"

# ----------------------------------------------------------------
# 5. ORDER LIFECYCLE – PREPARATION
# ----------------------------------------------------------------

Feature: Order preparation lifecycle

  Scenario: Start preparation of a confirmed order
    Given order "ORD-008" exists in status "CONFIRMED"
    When the kitchen starts preparing the order
    Then the event "OrderPreparationStarted" is emitted
    And the order has status "PREPARATION_STARTED"

  Scenario: Finish preparation of an order
    Given order "ORD-008" exists in status "PREPARATION_STARTED"
    When the kitchen finishes preparing the order
    Then the event "OrderPreparationFinished" is emitted
    And the order has status "PREPARATION_FINISHED"

  Scenario: Attempt to start preparation of an unconfirmed order
    Given order "ORD-009" exists in status "CREATED"
    When preparation start is attempted
    Then the command is rejected with error "ORDER_NOT_CONFIRMED"

# ----------------------------------------------------------------
# 6. NOTIFICATIONS
# ----------------------------------------------------------------

Feature: Order notifications

  Scenario: Send notification to customer after order confirmation
    Given order "ORD-010" has just changed status to "CONFIRMED"
    When the command "SendNotification" is sent for order "ORD-010"
    Then the customer receives a notification with updated status "CONFIRMED"
    And the notification contains the order number "ORD-010"

  Scenario: Send notification when preparation has started
    Given order "ORD-010" is in status "PREPARATION_STARTED"
    When the command "SendNotification" is sent
    Then the customer receives a notification with message "Your order is being prepared"

  Scenario: Attempt to send notification for a non-existent order
    When the command "SendNotification" is sent for order "ORD-999" (non-existent)
    Then the command is rejected with error "ORDER_NOT_FOUND"

# ----------------------------------------------------------------
# 7. SCHEDULING – Choose Delivery/Pickup Time
# ----------------------------------------------------------------

Feature: Time selection and scheduling

  Scenario: Choose an available delivery time slot
    Given order "ORD-011" is of type "DELIVERY" in status "CREATED"
    And the Schedule system has generated available time slots
    When the customer sends the command "ChooseDeliveryTime" with time "21:00"
    Then delivery time "21:00" is associated with order "ORD-011"
    And the event "DeliveryTimeSelected" is emitted

  Scenario: Attempt to choose an unavailable delivery time slot
    Given order "ORD-011" is of type "DELIVERY"
    And the time slot "23:00" is not available
    When the customer sends the command "ChooseDeliveryTime" with time "23:00"
    Then the command is rejected with error "TIME_NOT_AVAILABLE"

  Scenario: Choose an available pickup time slot
    Given order "ORD-012" is of type "PICKUP" in status "CREATED"
    When the customer sends the command "ChoosePickupTime" with time "20:00"
    Then pickup time "20:00" is associated with order "ORD-012"
    And the event "PickupTimeSelected" is emitted

  Scenario: Schedule deliveries by the system
    Given confirmed DELIVERY orders exist without an assigned driver
    When the command "ScheduleDeliveries" is sent
    Then the event "DeliveriesScheduled" is emitted
    And every delivery order has an assigned driver
    And the event "DriversAssigned" is emitted

  Scenario: Send pickup order to the pickup point
    Given order "ORD-012" is in status "PREPARATION_FINISHED"
    When the command "SendPickupOrder" is sent
    Then the order is sent to the pickup point
    And the event "PickupOrderSent" is emitted

  Scenario: Send delivery order to the driver
    Given order "ORD-011" is in status "PREPARATION_FINISHED" with an assigned driver
    When the command "SendDeliveryOrder" is sent
    Then the order is sent to the driver
    And the event "DeliveryOrderSent" is emitted

# ----------------------------------------------------------------
# 8. PAYMENT
# ----------------------------------------------------------------

Feature: Order payment

  Scenario: Successful online payment processing
    Given order "ORD-013" is in status "CONFIRMED" with amount 25.50 EUR
    When the command "ProcessOnlinePayment" is sent with a valid card
    Then the event "OnlinePaymentProcessed" is emitted
    And the event "OrderPayed" is emitted
    And the order has status "PAID"

  Scenario: Failed online payment due to invalid card
    Given order "ORD-013" is in status "CONFIRMED"
    When the command "ProcessOnlinePayment" is sent with an expired card
    Then the payment is rejected with error "PAYMENT_FAILED"
    And the order remains in status "CONFIRMED"
    And no "OrderPayed" event is emitted

  Scenario: Successful offline payment (cash/POS)
    Given order "ORD-014" is in status "CONFIRMED" of type "TABLE"
    When the command "ProcessOfflinePayment" is sent with method "CASH"
    Then the event "OfflinePaymentProcessed" is emitted
    And the event "OrderPayed" is emitted
    And the order has status "PAID"

  Scenario: Attempt to pay an already paid order
    Given order "ORD-015" is in status "PAID"
    When the command "ProcessOnlinePayment" is sent
    Then the command is rejected with error "ORDER_ALREADY_PAID"

  Scenario: Failed offline payment
    Given order "ORD-014" is in status "CONFIRMED"
    When the command "ProcessOfflinePayment" is sent with method "POS" and a failed transaction
    Then the payment is rejected with error "PAYMENT_FAILED"
    And the order remains in status "CONFIRMED"

# ----------------------------------------------------------------
# 9. END-TO-END SCENARIOS (COMPLETE FLOWS)
# ----------------------------------------------------------------

Feature: Complete online delivery order flow

  Scenario: Full delivery order flow from creation to payment
    Given an authenticated customer with a valid address
    When the customer creates a delivery order with ["PIZZA_MARGHERITA", "COCA_COLA"]
    And the customer chooses delivery time "20:30"
    And the customer processes online payment with a valid card
    Then the events emitted in sequence are:
      | OrderCreated              |
      | DeliveryOrderConfirmed    |
      | OnlinePaymentProcessed    |
      | OrderPayed                |
      | DeliveriesScheduled       |
      | DriversAssigned           |
      | OrderPreparationStarted   |
      | OrderPreparationFinished  |
    And the order has final status "DELIVERED"

  Scenario: Full pickup order flow from creation to collection
    Given an authenticated customer
    When the customer creates a pickup order with ["BURGER_CLASSIC"]
    And the customer chooses pickup time "19:00"
    And the customer processes online payment
    Then the events emitted in sequence include:
      | OrderCreated              |
      | PickupOrderConfirmed      |
      | OnlinePaymentProcessed    |
      | OrderPayed                |
      | OrderPreparationStarted   |
      | OrderPreparationFinished  |
      | PickupOrderSent           |
    And the order has final status "PICKED_UP"

  Scenario: Full table order flow with offline payment
    Given an authenticated operator at table "T3"
    When the operator creates a table order with ["PIZZA_DIAVOLA", "BEER"]
    And the kitchen starts and completes preparation
    And offline payment is processed with method "CASH"
    Then the events emitted in sequence include:
      | TableOrderCreated         |
      | OrderPreparationStarted   |
      | OrderPreparationFinished  |
      | OfflinePaymentProcessed   |
      | OrderPayed                |
    And the order has final status "PAID"
