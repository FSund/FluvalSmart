package com.inledco.bleota;

import java.util.ArrayList;

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
    private ArrayList<Byte> data_list;

    private Frame ()
    {
    }

    private Frame ( int data_length, int address, int type, ArrayList< Byte > data_list )
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

    public ArrayList< Byte > getData_list ()
    {
        return data_list;
    }

    public void setData_list ( ArrayList< Byte > data_list )
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
        public Builder ()
        {
        }

        /**
         * : len adrH adrL type d0...dn checksum
         * <:><00><0000><00><00...00><00>
         * @param str
         * @return
         */
        public Frame createFromString( String str )
        {
            if ( str.startsWith( ":" ) )
            {
                String s;
                if ( str.endsWith( "\r\n" ) )
                {
                    s = str.substring( 1, str.length() - 2 );
//                    LogUtil.d( TAG, "createFromString: 1\t" + s );
                }
                else
                {
                    s = str.substring( 1 );
//                    LogUtil.d( TAG, "createFromString: 2\t" + s );
                }
                if ( s.length() < 10 || ((s.length()&0x01) != 0x00) )
                {
//                    LogUtil.d( TAG, "createFromString: 3" );
                    return null;
                }
                try
                {
                    int length = Integer.parseInt( s.substring( 0, 2 ), 16 );
                    if ( s.length() != length * 2 + 10 )
                    {
//                        LogUtil.d( TAG, "createFromString: 4" );
                        return null;
                    }
                    int adrH = Integer.parseInt( s.substring( 2, 4 ), 16 );
                    int adrL = Integer.parseInt( s.substring( 4, 6 ), 16 );
                    if ( (adrL&0x01) != 0x00 )
                    {
//                        LogUtil.d( TAG, "createFromString: 5" );
                        return null;
                    }
                    int type = Integer.parseInt( s.substring( 6, 8 ), 16 );
                    if ( type != 0x00 && type != 0x01 && type != 0x02 && type != 0x04 )
                    {
//                        LogUtil.d( TAG, "createFromString: 6\t" + type );
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
                    int checksum = Integer.parseInt( s.substring( s.length() - 2, s.length() ), 16 );
                    sum += checksum;
                    if ( (sum&0xFF) != 0x00 )
                    {
//                        LogUtil.d( TAG, "createFromString: 7" );
                        return null;
                    }
                    Frame frame = new Frame( length, ((adrH<<8)|adrL)>>1, type, bytes );
                    return frame;
                }
                catch ( Exception e )
                {
//                    LogUtil.d( TAG, "createFromString: 8" );
                    return null;
                }
            }
//            LogUtil.d( TAG, "createFromString: 9" );
            return null;
        }
    }
}
