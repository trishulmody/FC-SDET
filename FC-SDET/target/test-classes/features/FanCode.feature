Feature: Verify User Completion Percentage

  Task: To Automate the Below Scenario.

  Scenario: All the users of City `FanCode` should have more than half of their todos task completed.
    Given User has the todo tasks
    And User belongs to the city FanCode
    Then User Completed task percentage should be greater than 50%
