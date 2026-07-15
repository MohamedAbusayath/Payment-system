Feature: Payment Process

  Scenario: Automatically approve a safe payment below 1000

    Given a payment amount of 500
    And the fraud service returns "SAFE"
    When the payment is processed
    Then the payment status should be "APPROVED"

  Scenario: Send a safe high value payment for checker approval

    Given a payment amount of 5000
    And the fraud service returns "SAFE"
    When the payment is processed
    Then the payment status should be "PENDING_APPROVAL"
