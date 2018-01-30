package com.inledco.bleota;

/**
 * Created by liruya on 2017/5/9.
 */

public class OTAConstants
{
    /**
     * get bootloader version, application start - end address and write/erase block size
     */
    public static final byte OTA_CMD_GET_VERSION = 0x00;

    public static final byte OTA_CMD_READ_FLASH = 0x01;

    public static final byte OTA_CMD_WRITE_FLASH = 0x02;

    public static final byte OTA_CMD_ERASE_FLASH = 0x03;

    public static final byte OTA_CMD_CALC_CHECKSUM = 0x09;

    public static final byte OTA_CMD_RESET_DEVICE = 0x0A;

    /**
     * get device status in bootloader or application
     */
    public static final byte OTA_CMD_GET_STATUS = 0x68;

    public static final byte OTA_RESPONSE_SUCCESS = 1;

    public static final byte OTA_REPSONSE_INVALID_COMMAND = -1;

    public static final byte OTA_REPONSE_OUTOF_RANGE = -2;
}
