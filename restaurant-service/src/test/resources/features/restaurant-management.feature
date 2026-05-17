Feature: Restaurant Creation and Configuration
  As a manager user
  I want to create and configure restaurant information
  So that I can set up my restaurant in the system

  Scenario: Create a new restaurant
    When I create a restaurant with name "Pizza Palace", address "456 Oak Avenue", phone "123456789", and email "info@pizzapalace.com"
    Then the restaurant should be created successfully

  Scenario: Retrieve created restaurant details
    Given I created a restaurant with name "Pizza Palace", address "456 Oak Avenue", phone "123456789", and email "info@pizzapalace.com"
    When I retrieve the restaurant details
    Then the restaurant details should match the created information

  Scenario: Update restaurant information
    Given I created a restaurant with name "Pizza Palace", address "321 Main Street", phone "123456789", and email "info@pizzapalace.com"
    And I am the manager of the restaurant
    When I update the restaurant with name "Pizza Paradise", address "789 Elm Street", phone "987654321", and email "info@pizzaparadise.com"
    Then the restaurant update should be successful

  Scenario: Delete restaurant
    Given I created a restaurant with name "Pizza Palace", address "456 Oak Avenue", phone "123456789", and email "info@pizzapalace.com"
    And I am the manager of the restaurant
    When I delete the restaurant
    Then the restaurant should be deleted successfully
