Backup of the following forum post: https://www.plantedtank.net/threads/reverse-engineering-the-fluval-plant-3-0-ble-protocol.1325539/

I wanted better control over the Plant 3.0 lights than what the phone app allows. So I sniffed some of the bluetooth traffic and recorded the commands the app was sending. Turns out Fluval or the BLE chip manufacturer uses some very basic encryption on the commands. A friend helped me out by writing a python script that will encrypt or decrypt the commands. From here you can write some code to directly control the lights without needing the app. This will allow for control using something like an ESP32 which would not only allow for unlimited light settings and timings but also get rid of the issue of the lights loosing the current time on reboot.

This is the tutorial I used to start the process.

https://blog.wokwi.com/reverse-engineering-a-bluetooth-lightbulb/


Example Command decrypted:

    Raw command captured over BLE from the Fluval App:
    545affc3af5454545454545454ab503c
    
    Same command after being decrypted:
    6804ffffffffffffffff00fb97
    
    0x68   - Header
    0x04   - Change brightness control command
    0xFFFF - Ignore Pink (0xFFFF is interpertperted by the light as No Change/Ignore)
    0xFFFF - Ignore Blue
    0xFFFF - Ignore Cold White
    0xFFFF - Ignore Pure White
    0x00fB - Set Warm White to 251 (Range is 0-1000 or 0x0000-0x03E8)
    0x97   - Checksum

Python Script to encrypt/decrypt BLE commands:

    #!/usr/bin/env python3
    import sys
    
    """
    API SPECIFICATION
    There are a lot of commands defined in the decompiled APK; see CommUtil.java for the full list. But this is an exhaustive list that covers lots of different hardware. At the moment only on/off and brightness per-channel are implemented here.
    COMMAND STRUCTURE
    Every command message begins with a header (0x68), followed by a single command byte, then whatever arguments for that command, followed by a CRC byte. This is then encrypted per below before being sent over BLE.
    BLE MESSAGE ENCRYPTION SCHEME
    A random key (lrand48()) is generated with each message. Then that key is used to XOR the rest of the message. The random key is itself stored in a header, where it is XOR'd with a fixed value (0x54).
    The wrapped, encrypted format is:
    [IV] [Length] [Key] [byte1, byte2, ...]
    Where IV is always 0x54, length is the number of bytes after the key XOR'd with the IV, and the key is a random number XOR'd with the IV.
    When sending messages to the device, there's no need to generate a random if you don't want to. You can just use a fixed number like zero.
    """
    
    def ble_encode(b):
      raw_len = len(b)
      rand = 0
      encoded_bytes = bytearray([0x54, (raw_len + 1) ^ 0x54, rand ^ 0x54])
      for byte in b:
        encoded_bytes.append(byte ^ rand)
    
      return encoded_bytes
    
    def ble_decode(b):
      iv = b[0]
      length = b[1] ^ iv
      key = b[2] ^ iv
    
      decoded_bytes = bytearray()
      for i in range(3, len(b)):
        decoded_bytes.append(b[i] ^ key)
    
      return decoded_bytes
    
    # The last byte of a message is a CRC value that is just every byte XOR'd in order.
    def crc(cmd):
      check = 0
      for i in range(0, len(cmd)):
        check = check ^ cmd[i]
      return check
    
    def buildMessage(raw_bytes):
      raw_msg = bytearray(raw_bytes)
      # Prepend message header (0x68), aka FRM_HDR in apk source code
      msg = bytearray([0x68])
      msg.extend(raw_msg)
      msg.append(crc(msg))
      print("Dec message: ", msg.hex())
      enc_msg = ble_encode(msg)
      print("Enc message: ", enc_msg.hex())
      return enc_msg
    
    def getPowerOnMessage():
      # Power:
      #  CMD_SWITCH (0x03), [0|1]
      return buildMessage([0x03, 0x01])
    
    
    def getPowerOffMessage():
      # Power:
      #  CMD_SWITCH (0x03), [0|1]
      return buildMessage([0x03, 0x00])
    
    # Sets the brightness of one or more channels
    # Level: 0-1000 (0x03E8) -- note this is two bytes and is big-endian
    # Channels not specified will not be modified.
    def getBrightnessMessage(red=False, blue=False, cwhite=False, pwhite=False, wwhite=False):
      # Channel brightness message format:
      #   CMD_CTRL (0x04), <16-bit red>, <16-bit blue>, <16-bit cwhite>, <16-bit pwhite>, <16-bit wwhite>
      # Notes: Values set to 0xFFFF will not modify anything.
      #        Legal range is 0x0000-0x03E8, big-endian.
    
      def consider(color):
        nop = b'\xff\xff'
        if color is False:
          return nop
        elif color < 0 or color > 1000:
          print("fatal: brightness values must be between 0-1000")
          sys.exit(1)
        else:
          return color.to_bytes(2, byteorder='big')
    
      cmd = bytearray([0x04])
      cmd.extend(consider(red))
      cmd.extend(consider(blue))
      cmd.extend(consider(cwhite))
      cmd.extend(consider(pwhite))
      cmd.extend(consider(wwhite))
    
      return buildMessage(cmd)
    
    
    def main():
      # Examples:
      print("Power on")
      getPowerOnMessage()
      print("Power off")
      getPowerOffMessage()
      print("Blue to 950")
      getBrightnessMessage(blue=950)
      print("All off, red to 1000")
      getBrightnessMessage(red=1000, blue=0, wwhite=0, pwhite=0, cwhite=0)
    
      #for i in sys.stdin:
      #  print(ble_decode(bytes.fromhex(i)).hex())
    
    if __name__ == '__main__':
      main()
