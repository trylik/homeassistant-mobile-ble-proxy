# planik HATAB-63 · @scenario:cmwdh5u95019u3hp5ldc47ebz
# Executable steps: app/src/test/java/com/zen3515/homeassistant_mobile_ble_proxy/proxy/WriteWithoutResponseGateTest.kt
Feature: Writes without response are paced by Android's write callback

  Scenario: A fragmented write-without-response handshake reaches the meter whole
    Given Home Assistant's tuya_ble sends a pairing request to the meter as several write-without-response fragments
    And the proxy has started the first fragment on Android
    When the next fragment is queued before Android reports the first one with onCharacteristicWrite
    Then the proxy holds the queue until that callback
    And every fragment is written, with no GATT error returned to Home Assistant

  Scenario: A missing write callback cannot stall the connection
    Given the proxy started a write-without-response
    When Android never reports it
    Then the proxy releases the queue once after a short fallback
    And a late callback does not complete the next operation

  Scenario: A connection closing with a paced write drops the fallback
    Given the proxy is waiting for a write-without-response callback
    When the connection is torn down
    Then the fallback is cancelled without releasing anything
