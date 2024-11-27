Backup of the following Reddit post: https://www.reddit.com/r/homeassistant/comments/1f03n7f/publication_esp32_code_samples_1x_fluval_aquasky/


This shall help some of you, having same or lower experience with esp32 as me:  
(As i did not find ready to use snippets for these)

ESP32S3 (XIAO ESP32S3) as BT-Proxy:

    substitutions:
      display_name: esp32s3-btproxy
    
    esphome:
      name: ${display_name}
      platformio_options:
        board_build.mcu: esp32s3
        board_build.variant: esp32s3  
    # added the line below to prevent bootloops when flashing modern bin via serial
        board_build.flash_mode: dio     
    
    esp32:
      variant: ESP32S3
      board: esp32-s3-devkitc-1
      framework:
        type: esp-idf
        sdkconfig_options:
          CONFIG_BT_BLE_50_FEATURES_SUPPORTED: y
          CONFIG_BT_BLE_42_FEATURES_SUPPORTED: y
          CONFIG_ESP_TASK_WDT_TIMEOUT_S: "10"    
    
    logger:
    api:
    ota:
      platform: esphome
    
    button:
      - platform: safe_mode
        name: ${display_name} (Safe Mode)
    
    # Change the WiFi config to a non-static IP if needed
    wifi:
      ssid: !secret wifi_ssid
      password: !secret wifi_password
    
    esp32_ble_tracker:
      scan_parameters:
    # Adjust timing if the defaults do not work in your environment
    #    interval: 1100ms
    #    window: 1100ms
        active: true
    
    bluetooth_proxy:
      active: true

The fluval code: Based on the forum treat [https://community.home-assistant.io/t/fluval-aquasky-ble-rgb-light/259598/144](https://community.home-assistant.io/t/fluval-aquasky-ble-rgb-light/259598/144) - credits to those having done the hard work!

    esphome:
      name: "fishtankcontroller"
      platformio_options:
        board_build.flash_mode: dio  # Ensures proper flash mode for the ESP32-S3 board
    
    esp32:
      board: esp32-s3-devkitc-1
      framework:
        type: esp-idf
        version: recommended  # You can specify the version if needed (e.g., 4.4.7)
    
    # Wi-Fi Configuration
    wifi:
      ssid: !secret wifi_ssid
      password: !secret wifi_password
    
      # Enable fallback hotspot in case Wi-Fi fails
      ap:
        ssid: "Fishtankcontroller Fallback"
        password: !secret fallback_password
    
    # Enable logging
    logger:
    
    # Enable Home Assistant API
    api:
      encryption:
        key: "S/lJYWLR+FYCBHhX1sjmyAU5IE/YoO3FKBlrAdeJapo="
    
    # Enable OTA updates
    ota:
      platform: esphome
      password: !secret ota_password
    
    # Time sync with Home Assistant
    time:
      - platform: homeassistant
        id: ha_time
    
    # Include external components (Fluval BLE LED integration)
    external_components:
      - source: github://mrzottel/esphome@fluval_ble_led
        components: [fluval_ble_led]
    
    # Enable BLE Tracker
    esp32_ble_tracker:
    
    # BLE Client setup for the Fluval device
    ble_client:
      - mac_address: 44:A6:E5:6E:0E:D1
        id: BLETankFrontClient
    
    # Fluval BLE LED setup
    fluval_ble_led:
      - ble_client_id: BLETankFrontClient
        time_id: ha_time
        number_of_channels: 4
        id: BLETankFront
    
    # Sensor configuration for each channel of the Fluval LED
    sensor:    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        channel: 1      
        zero_if_off: true
        name: "Front Channel Red"
    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        channel: 2      
        zero_if_off: true
        name: "Front Channel Green"  
    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        channel: 3      
        zero_if_off: true
        name: "Front Channel Blue"  
    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        channel: 4      
        zero_if_off: true
        name: "Front Channel White" 
    
    # Text sensor to read the mode of the Fluval LED
    text_sensor:
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        name: "Front Mode"
    
    # Button configuration for switching modes on the Fluval LED
    button:  
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        mode: "MANUAL"
        name: "Front Switch to manual"
    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        mode: "AUTO"
        name: "Front Switch to auto"
    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        mode: "PRO"
        name: "Front Switch to pro"
    
    # Number entities to control brightness levels on each channel
    number:
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        name: "Front Channel Red"
        channel: 1
        zero_if_off: true
    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        name: "Front Channel Green"
        channel: 2
        zero_if_off: true
    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        name: "Front Channel Blue"
        channel: 3
        zero_if_off: true
    
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        name: "Front Channel White"
        channel: 4
        max_value: 1000
        step: 10
        zero_if_off: true
    
    # Switch entity for turning the Fluval LED on/off
    switch:
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        name: "Front LED Switch"
