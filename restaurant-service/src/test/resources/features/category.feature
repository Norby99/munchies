Feature: Category Management
  As a restaurant manager
  I want to manage categories within a menu
  So that menu items are organised by type

  Background:
    Given a restaurant exists
    And the restaurant has a menu

  Scenario: Create a new menu category
    When I create a "Pizzas" category in the menu
    Then the category should be created successfully
    And the menu should have a "Pizzas" category

  Scenario: Update a menu category
    Given the menu has a "Pizzas" category
    When I update the "Pizzas" category name to "Gourmet Pizzas"
    Then the category should be updated successfully
    And the menu should have a "Gourmet Pizzas" category

  Scenario: Remove a menu category
    Given the menu has a "Pizzas" category
    When I remove the "Pizzas" category from the menu
    Then the category should be removed successfully
    And the menu should have no "Pizzas" category
