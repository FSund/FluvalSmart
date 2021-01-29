package com.inledco.fluvalsmart.ota;

import java.text.DecimalFormat;

/**
 * Created by liruya on 2017/5/4.
 */

public class RemoteFirmware
{
    /**
     * firmware major version
     */
    private int major_version;

    /**
     * firmware minor version
     */
    private int minor_version;

    /**
     * firmware release date
     */
    private String release_date;

    /**
     * firmware size,unit byte
     */
    private int file_size;

    /**
     * file name of the remote firmware
     */
    private String file_name;

    private String file_link;

    public RemoteFirmware ()
    {
    }

    public RemoteFirmware (int major_version, int minor_version, String release_date, int size, String file_name, String file_link)
    {
        this.major_version = major_version;
        this.minor_version = minor_version;
        this.release_date = release_date;
        this.file_size = size;
        this.file_name = file_name;
        this.file_link = file_link;
    }

    public RemoteFirmware (int major_version, int minor_version, String release_date, int size, String file_name)
    {
        this.major_version = major_version;
        this.minor_version = minor_version;
        this.release_date = release_date;
        this.file_size = size;
        this.file_name = file_name;
    }

    public int getMajor_version ()
    {
        return major_version;
    }

    public void setMajor_version (int major_version)
    {
        this.major_version = major_version;
    }

    public int getMinor_version ()
    {
        return minor_version;
    }

    public void setMinor_version (int minor_version)
    {
        this.minor_version = minor_version;
    }

    public String getRelease_date ()
    {
        return release_date;
    }

    public void setRelease_date (String release_date)
    {
        this.release_date = release_date;
    }

    public int getSize ()
    {
        return file_size;
    }

    public void setSize (int size)
    {
        this.file_size = size;
    }

    public String getFile_name ()
    {
        return file_name;
    }

    public void setFile_name (String file_name)
    {
        this.file_name = file_name;
    }

    public String getFile_link ()
    {
        return file_link;
    }

    public void setFile_link (String file_link)
    {
        this.file_link = file_link;
    }

    @Override
    public String toString ()
    {
        DecimalFormat df = new DecimalFormat("00");
        return "FirmwareVersion: " + major_version + "." + df.format(minor_version)
               + "\r\nReleaseDate: " + release_date
               + "\r\nFileSize: " + file_size
               + "\r\nFileName: " + file_name
               + "\r\nFileLink: " + file_link;
    }
}
