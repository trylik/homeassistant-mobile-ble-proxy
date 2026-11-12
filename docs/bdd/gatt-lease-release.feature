# planik HATAB-63 · @scenario:cmwddyhfw014s3hp5538nlxz0
# Executable steps: app/src/test/java/com/zen3515/homeassistant_mobile_ble_proxy/proxy/GattLeaseOwnershipTest.kt
Feature: GATT leases follow the API session that opened them

  Scenario: A BLE meter lease is released when the Home Assistant session that opened it closes
    Given Home Assistant's API session "old-session" connected the meter DC:23:4F:35:FE:4C through the proxy
    And the meter keeps sending notifications on that connection
    When the "old-session" API session closes without a disconnect request
    Then the proxy releases the meter's GATT connection, as ESPHome firmware does
    And the meter advertises again so the reconnected Home Assistant session can connect it

  Scenario: A lease re-taken by the reconnected session survives the old session closing
    Given "old-session" connected the meter
    And Home Assistant's reconnected session "new-session" asked to connect the meter too
    When "old-session" closes
    Then the meter's connection stays up for "new-session"
    And it is released only when "new-session" closes

  Scenario: Leases that already ended or belong to another client are left alone
    Given a session's lease on the meter already ended with a disconnect
    When that session closes
    Then nothing is released again
    And another client's lease on a different device is untouched
