Feature: Menu Management
  As a restaurant manager
  I want to manage my restaurant's menus and menu items
  So that customers can see what food is available

  Background:
    Given a restaurant named "Pizza Palace" exists
    And I am logged in as the manager of this restaurant

  # --- Menu management: add, update, delete given a restaurant ---

  Scenario: Add a new menu
    When I add a menu named "Winter Menu" valid from 2026-12-01 to 2027-02-28
    Then the menu "Winter Menu" should be created successfully
    And the restaurant should have 1 menu

  Scenario: Update an existing menu
    Given the restaurant has a menu named "Winter Menu" valid from 2026-12-01 to 2027-02-28
    When I update the menu "Winter Menu" to have name "Spring Menu" and valid from 2027-03-01 to 2027-05-31
    Then the menu should be updated successfully
    And the restaurant should have a menu named "Spring Menu" valid from 2027-03-01 to 2027-05-31

  Scenario: Remove a menu
    Given the restaurant has a menu named "Winter Menu" valid from 2026-12-01 to 2027-02-28
    When I remove the menu named "Winter Menu"
    Then the menu "Winter Menu" should be removed successfully
    And the restaurant should have 0 menus

  # --- Category management: add, update, delete given a menu ---

  Scenario: Create a new menu category
    Given the restaurant has a menu
    When I add a "Pizzas" category to the menu
    Then the "Pizzas" category should be created successfully
    And the menu should have 1 category

  Scenario: Update a menu category
    Given the menu has a "Pizzas" category
    When I update the "Pizzas" category name to "Gourmet Pizzas"
    Then the category should be updated successfully
    And the menu should have a "Gourmet Pizzas" category

  Scenario: Remove a menu category
    Given the menu has a "Pizzas" category
    When I remove the "Pizzas" category from the menu
    Then the "Pizzas" category should be removed successfully
    And the menu should have 0 categories

  # --- Item management: add, update, delete given a category ---

  Scenario: Add an item to a category
    Given the menu has a "Pizzas" category
    When I add a menu item "Margherita" with description "The real classic" and price 5.80 to the "Pizzas" category
    Then the item "Margherita" should be added successfully to the "Pizzas" category
    And the "Pizzas" category should have 1 item

  Scenario: Update an existing menu item
    Given the menu has a "Pizzas" category
    And the "Pizzas" category has an item "Margherita" with price 5.80
    When I update the item "Margherita" in the "Pizzas" category to have price 6.00
    Then the item "Margherita" should be updated successfully
    And the "Pizzas" category should reflect the new price 6.00 for "Margherita"

  Scenario: Remove a menu item
    Given the menu has a "Pizzas" category
    And the "Pizzas" category has an item "Margherita" with price 5.80
    When I remove the item "Margherita" from the "Pizzas" category
    Then the item "Margherita" should be removed successfully
    And the "Pizzas" category should have 0 items

  # --- Variation management: add, update, delete given a category ---

  Scenario: Add a variation to a menu category
    Given the menu has a "Pizzas" category with items "Margherita" and "Marinara"
    When I add a variation "Dough" with options "Spelt" (+2.00) and "Gluten free" (+2.50) to the category "Pizzas"
    Then the variation "Dough" should be added successfully to the category "Pizzas"
    And all items in the category "Pizzas" should have the variation "Dough"

  Scenario: Update an existing variation of a menu category
    Given the menu has a "Pizzas" category
    And the category "Pizzas" has a variation "Dough" with options "Spelt" (+2.00) and "Gluten free" (+2.50)
    When I update the variation "Dough" for the category "Pizzas" to have options "Normal" (+0.00), "Spelt" (+2.50) and "Whole wheat" (+3.00)
    Then the variation "Dough" should be updated successfully for the category "Pizzas"
    And the category "Pizzas" variation "Dough" should have 3 options

  Scenario: Remove a variation from a menu category
    Given the menu has a "Pizzas" category
    And the category "Pizzas" has a variation "Dough" with options "Spelt" (+2.00) and "Gluten free" (+2.50)
    When I remove the variation "Dough" from the category "Pizzas"
    Then the variation "Dough" should be removed successfully
    And the category "Pizzas" should have 0 variations

  # --- Item Variation management: add, update, delete given an item ---

  Scenario: Add a variation to a specific menu item
    Given the menu has a "Pizzas" category
    And the "Pizzas" category has an item "Pizza Crudo" with price 8.50
    When I add a variation "Crudo preparation" with options "Post-bake" (+0.00) and "Crudo in cottura" (+0.00) to the item "Pizza Crudo"
    Then the variation "Crudo preparation" should be added successfully to the item "Pizza Crudo"
    And the item "Pizza Crudo" should have 1 variation

  Scenario: Update an existing variation of a menu item
    Given the menu has a "Pizzas" category
    And the "Pizzas" category has an item "Pizza Crudo" with price 8.50
    And the item "Pizza Crudo" has a variation "Crudo preparation" with options "Post-bake" (+0.00)
    When I update the variation "Crudo preparation" for the item "Pizza Crudo" to have options "Post-bake" (+0.00) and "Crudo in cottura" (+0.00)
    Then the variation "Crudo preparation" should be updated successfully for the item "Pizza Crudo"
    And the item "Pizza Crudo" variation "Crudo preparation" should have 2 options

  Scenario: Remove a variation from a menu item
    Given the menu has a "Pizzas" category
    And the "Pizzas" category has an item "Pizza Crudo" with price 8.50
    And the item "Pizza Crudo" has a variation "Crudo preparation" with options "Post-bake" (+0.00) and "Crudo in cottura" (+0.00)
    When I remove the variation "Crudo preparation" from the item "Pizza Crudo"
    Then the variation "Crudo preparation" should be removed successfully
    And the item "Pizza Crudo" should have 0 variations
