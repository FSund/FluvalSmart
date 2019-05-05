package com.inledco.fluvalsmart.ota;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liruya on 2017/5/4.
 */

public class Frame
{
    /**
     * 1 byte
     */
    private int data_length;

    /**
     * 2 bytes lower fisrt
     */
    private int address;

    /**
     * 1 byte
     */
    private int type;

    /**
     *
     */
    private List<Byte> data_list;

    private Frame ()
    {
    }

    private Frame ( int data_length, int address, int type, List< Byte > data_list )
    {
        this.data_length = data_length;
        this.address = address;
        this.type = type;
        this.data_list = data_list;
    }

    public int getData_length ()
    {
        return data_length;
    }

    public void setData_length ( int data_length )
    {
        this.data_length = data_length;
    }

    public int getAddress ()
    {
        return address;
    }

    public void setAddress ( int address )
    {
        this.address = address;
    }

    public int getType ()
    {
        return type;
    }

    public void setType ( int type )
    {
        this.type = type;
    }

    public List< Byte > getData_list ()
    {
        return data_list;
    }

    public void setData_list ( List< Byte > data_list )
    {
        this.data_list = data_list;
    }

    @Override
    public String toString ()
    {
        String str = "DataLength: " + data_length
                     + "\r\nStartAddress: " + address
                     + "\r\nType: " + type
                     + "\r\nData: " + data_list.toString();
        return str;
    }

    public static class Builder
    {
        private static final String TAG = "Builder";
        public Builder ()
        {
        }

        /**
         * : len adrH adrL type d0...dn checksum
         * <:><00><0000><00><00...00><00>
         * @param str
         * @return
         */
        public Frame createFromString(String str)
        {
            if ( str.startsWith( ":" ) )
            {
                String s;
                if ( str.endsWith( "\r\n" ) )
                {
                    s = str.substring( 1, str.length() - 2 );
                }
                else
                {
                    s = str.substring( 1 );
                }
                if ( s.length() < 10 || ((s.length()&0x01) != 0x00) )
                {
                    return null;
                }
                try
                {
                    int length = Integer.parseInt( s.substring( 0, 2 ), 16 );
                    if ( s.length() != length * 2 + 10 )
                    {
                        return null;
                    }
                    int adrH = Integer.parseInt( s.substring( 2, 4 ), 16 );
                    int adrL = Integer.parseInt( s.substring( 4, 6 ), 16 );
                    if ( (adrL&0x01) != 0x00 )
                    {
                        return null;
                    }
                    int type = Integer.parseInt( s.substring( 6, 8 ), 16 );
                    if ( type != 0x00 && type != 0x01 && type != 0x02 && type != 0x04 )
                    {
                        return null;
                    }
                    int sum = length + adrH + adrL + type;
                    ArrayList<Byte> bytes = new ArrayList<>();
                    for ( int i = 0; i < length; i++ )
                    {
                        int b = Integer.parseInt( s.substring( 8 + i * 2, 10 + i * 2 ), 16 );
                        sum += b;
                        bytes.add( (byte) ( b & 0xFF) );
                    }
                    int checksum = Integer.parseInt(s.substring( s.length() - 2), 16);
                    sum += checksum;
                    if ( (sum&0xFF) != 0x00 )
                    {
                        return null;
                    }
                    Frame frame = new Frame(length, ((adrH << 8) | adrL) >> 1, type, bytes );
                    return frame;
                }
                catch ( Exception e )
                {
                    return null;
                }
            }
            return null;
        }

        /**
         * :LLAAAATTDD....CC
         * LL: 数据长度
         * AAAA: 起始地址
         * TT: 类型
         * DD....: 数据
         * CC： 校验和
         * @param file
         * @return
         */
        public List<Frame> getFramesFromFile(@NonNull final File file) {
            final List<Frame> frames = new ArrayList<>();
            try {
                final FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(":") == false) {
                        return null;
                    }
                    if (line.endsWith("\r\n")) {
                        line = line.substring(1, line.length()-2);
                    } else {
                        line = line.substring(1);
                    }
                    if (line.length() < 10 || (line.length()&0x01) != 0x00) {
                        return null;
                    }
                    int length = Integer.parseInt(line.substring(0, 2), 16);
                    if (length > 16 || line.length() != length*2+10) {
                        return null;
                    }
                    int adrH = Integer.parseInt(line.substring(2, 4), 16);
                    int adrL = Integer.parseInt(line.substring(4, 6), 16);
                    if ((adrL&0x01) != 0x00) {
                        return null;
                    }
                    int type = Integer.parseInt(line.substring(6, 8), 16);
                    if (type != 0x00 && type != 0x01 && type != 0x02 && type != 0x04) {
                        return null;
                    }
                    int sum = length + adrH + adrL + type;
                    List<Byte> bytes = new ArrayList<>();
                    for (int i = 0; i < length; i++) {
                        int b = Integer.parseInt(line.substring(8+i*2, 10+i*2), 16);
                        sum += b;
                        bytes.add((byte) (b&0xFF));
                    }
                    int checksum = Integer.parseInt(line.substring(line.length()-2), 16);
                    sum += checksum;
                    if ((sum&0xFF) != 0x00) {
                        return null;
                    }
                    int adr = ((adrH<<8)|adrL)>>1;
                    if (length > 8) {
                        Frame frm1 = new Frame(8, adr, type, bytes.subList(0, 8));
                        Frame frm2 = new Frame(length-8, adr+4, type, bytes.subList(8, length));
                        frames.add(frm1);
                        frames.add(frm2);
                    } else {
                        Frame frame = new Frame(length, adr, type, bytes);
                        frames.add(frame);
                    }
                }
                return frames;
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
