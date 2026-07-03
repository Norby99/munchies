Feature: Menu Management
  As a restaurant manager
  I want to manage my restaurant's menus
  So that customers can see what food is available

  Background:
    Given a restaurant exists

  Scenario: Create a new menu
    When I create a "Winter Menu" menu valid from 2026-12-01 to 2027-02-28
    Then the menu should be created successfully
    And the restaurant should have a "Winter Menu" menu valid from 2026-12-01 to 2027-02-28

  Scenario: Retrieve a menu
    Given the restaurant has a "Winter Menu" menu valid from 2026-12-01 to 2027-02-28
    When I retrieve the menu details for "Winter Menu"
    Then the menu should be retrieved successfully
    And the menu details should match a "Winter Menu" menu valid from 2026-12-01 to 2027-02-28

  Scenario: Update an existing menu
    Given the restaurant has a "Winter Menu" menu valid from 2026-12-01 to 2027-02-28
    When I update the menu "Winter Menu" to have name "Spring Menu" and valid from 2027-03-01 to 2027-05-31
    Then the menu should be updated successfully
    And the restaurant should have a "Spring Menu" menu valid from 2027-03-01 to 2027-05-31

  Scenario: Remove a menu
    Given the restaurant has a "Winter Menu" menu valid from 2026-12-01 to 2027-02-28
    When I remove the "Winter Menu" menu
    Then the menu should be removed successfully
    And the restaurant should have no "Winter Menu" menu
