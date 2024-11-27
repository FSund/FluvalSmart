Its basically as follows:

setup esp-home (using esp home addon for HA), i suggest doing it on mac or windows. (install gitforwindows as well). then start it from within the github cmd.

next is to install the matching drivers for your esp32 board’s chip. (ESPhome leads you)

now lets have some fun and create a yaml file like mine: (based on publications released here, credit to those having created it :slight_smile: )
    
    esphome:
      name: "fishtankcontroller"
    
    esp32:
      board: esp32dev
      framework:
        type: esp-idf
        version: recommended
    
    # Enable logging
    logger:
      level: DEBUG
    # Enable Home Assistant API
    api:
      encryption:
       key: "S/lJYWLR+FYCBHhX1sjmyAU5IE/YoO3FKBlrAdeJapo="
    
    
    wifi:
      ssid: !secret wifi_ssid
      password: !secret wifi_password
    
    external_components:
      - source: github://mrzottel/esphome@fluval_ble_led
        components: [ fluval_ble_led ]
    
    time:
      - platform: homeassistant
        id: ha_time
    
    esp32_ble_tracker:
    
    ble_client:
       - mac_address: 44:A6:E5:6E:0E:D1
         id: BLETankFrontClient
    
    fluval_ble_led:
       - ble_client_id: BLETankFrontClient
         time_id: ha_time
         number_of_channels: 4
         id: BLETankFront
    
    
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
    
    text_sensor:
      - platform: fluval_ble_led
        fluval_ble_led_id: BLETankFront
        name: "Front Mode"
    
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
    
    
    switch:
    - platform: fluval_ble_led
      fluval_ble_led_id: BLETankFront
      name: "Front LED Switch"


the mac-adress can be found inside the fluval app, when swiping the light to the left and selecting “upgrade”. the secret is used for pairing iwth your HA instance when adding the final device there.

now lets verify and install the code to the board.

next is wait a little, remove power and place it near the fluval light permanently.

up next is debugging.
inside the dashboard (can be started using “esphome dashboard .” from within github cmd) you can also find the log button for your new device and check for issues.

thats with my aquasky 2.0 // 16w.
values are published as “number.name” to HA.

—> if getting reception / connection issues, like mentionen before in this treath,
try moving the board to a place that is on the same hight as the fluval light.

i did get some cheap esp32dev boards, from within germany (8€/each) on ebay.
Have to basically place it right nex tto the light, since fluvals integrated BT-Chip seems to be very week as well as my board’s.

i hope the az-delivery boards are better in this term, but for now using panzertape it works just fine :smiley:
