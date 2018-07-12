package com.inledco.bleota;

import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.ble.api.DataUtil;
import com.inledco.OkHttpManager.DownloadCallback;
import com.inledco.OkHttpManager.HttpCallback;
import com.inledco.OkHttpManager.OkHttpManager;
import com.inledco.blemanager.BleCommunicateListener;
import com.inledco.blemanager.BleManager;
import com.inledco.blemanager.LogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by liruya on 2017/5/8.
 */

public class OTAPresenter implements OTAContract.Presenter
{
    private static final String TAG = "OTAPresenter";

    private static final int OTA_SUPPORT_LOWEST_VERSION = 0x0102;
    private static final String OTA_UPGRADE_LINK = "http://47.88.12.183:8080/OTAInfoModels/GetOTAInfo?deviceid=";
    private static final String OTA_FIRMWARE_LINK = "http://47.88.12.183:8080";

    private OTAContract.View mView;
    private String mAddress;
    private short mDevid;
    private int mDeviceMajorVersion;
    private int mDeviceMinorVersion;
    private String mRemoteVersionUrl;
    private RemoteFirmware mRemoteFirmware;
    private int mAppStartAddress;

    private int mAppEndAddress;

    private int mBootloaderMajorVersion;

    private int mBootloaderMinorVersion;

    private int mEraseBlockSize;

    private int mWriteBlockSize;

    private BleCommunicateListener mCommunicateListener;

    private Handler mHandler;

    private File mFirmwareFile;
    private ArrayList<Frame> mFrames;
    private int mCurrent;
    private int mTotal;

    //auto connect after exit application to bootloader
    private boolean mAutoConnect;
    private boolean mUpgrading;
    private byte mCurrentCommand;
    private CountDownTimer mCountDownTimer;

    public OTAPresenter ( OTAContract.View view, @NonNull short devid, @NonNull String address, @NonNull String remoteVersionUrl )
    {
        mView = view;
        mDevid = devid;
        mAddress = address;
        mRemoteVersionUrl = remoteVersionUrl;
        mHandler = new Handler();
        mCountDownTimer = new CountDownTimer( 1000, 500 )
        {
            @Override
            public void onTick ( long millisUntilFinished )
            {

            }

            @Override
            public void onFinish ()
            {
                mUpgrading = false;
                mView.showMessage( mView.getMvpContext().getString( R.string.ota_response_timeout ) );
            }
        };
    }

    public boolean isUpgrading ()
    {
        return mUpgrading;
    }

    @Override
    public void start ()
    {
        mCommunicateListener = new BleCommunicateListener()
        {
            @Override
            public void onDataValid ( String mac )
            {
                if ( mac.equals( mAddress ) )
                {
                    mView.onDataValid();
                    if ( mAutoConnect )
                    {
                        mAutoConnect = false;
                        mHandler.postDelayed( new Runnable() {
                            @Override
                            public void run ()
                            {
                                enterBootloader();
                            }
                        }, 128 );
                    }
                    LogUtil.d( TAG, "onDataValid: " );
                }
            }

            @Override
            public void onDataInvalid ( final String mac )
            {
                if ( mac.equals( mAddress ) )
                {
                    mView.onDataInvalid();
                    if ( mAutoConnect )
                    {
                        mAutoConnect = false;
                        mView.showMessage( mView.getMvpContext().getString( R.string.ota_msg_reupgrade ) );
                    }
                }
            }

            @Override
            public void onReadMfr ( String mac, String s )
            {
                if ( mac.equals( mAddress ) )
                {
                    decodeMfrData( s );
                }
            }

            @Override
            public void onReadPassword ( String mac, int psw )
            {

            }

            @Override
            public void onDataReceived ( String mac, ArrayList< Byte > list )
            {
                if ( mac.equals( mAddress ) && list.get( 0 ) == mCurrentCommand )
                {
                    mCountDownTimer.cancel();
                    decodeReceiveData( list );
                }
            }
        };
        BleManager.getInstance()
                  .addBleCommunicateListener( mCommunicateListener );
        BleManager.getInstance()
                  .connectDevice( mAddress );
        mView.showMessage( mView.getMvpContext().getString( R.string.ota_connecting ) );
    }

    @Override
    public void stop ()
    {
        mCountDownTimer.cancel();
        BleManager.getInstance()
                  .removeBleCommunicateListener( mCommunicateListener );
        BleManager.getInstance()
                  .disconnectDevice( mAddress );
    }

    @Override
    public void checkRemoteVersion ()
    {
        mRemoteFirmware = null;
        OkHttpManager.get( OTA_UPGRADE_LINK + mDevid, null, new HttpCallback< RemoteFirmware >()
        {
            @Override
            public void onError ( int code, String msg )
            {
                mView.onCheckRemoteFailure();
                if ( msg == null )
                {
                    mView.showMessage( mView.getMvpContext().getString( R.string.ota_msg_remote_not_exists ) );
                }
                else
                {
                    mView.showMessage( msg );
                }
            }

            @Override
            public void onSuccess ( RemoteFirmware result )
            {
                LogUtil.d( TAG, "onSuccess: " + result.toString() );
                mRemoteFirmware = result;
                mFirmwareFile = new File( mView.getMvpContext().getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ),
                                          mRemoteFirmware.getFile_name() );
                mView.onCheckRemoteSuccess( result.getMajor_version(), result.getMinor_version() );
                mView.onFirmwareExists( mFirmwareFile.exists() );
            }
        } );
    }

    @Override
    public void checkDeviceVersion ()
    {
        mDeviceMajorVersion = 0;
        mDeviceMinorVersion = 0;
        BleManager.getInstance()
                  .readMfr( mAddress );
    }

    @Override
    public void isUpgradable ()
    {
        int version = (mDeviceMajorVersion << 8) | mDeviceMinorVersion;
        if ( version == 0 || mRemoteFirmware == null )
        {
            mView.showMessage( mView.getMvpContext().getString( R.string.ota_msg_check_version ) );
            return;
        }
        if ( version < OTA_SUPPORT_LOWEST_VERSION )
        {
            mView.showMessage( mView.getMvpContext().getString( R.string.ota_msg_unsupport_version ) );
            return;
        }
        int remote_version = (mRemoteFirmware.getMajor_version()<<8)|mRemoteFirmware.getMajor_version();
//        if ( version >= remote_version )
//        {
//            mView.showMessage( mView.getMvpContext().getString( R.string.ota_msg_uptodate ) );
//            return;
//        }
//        else
//        {
            convertFirmware();
//        }
    }

    @Override
    public void downloadFirmware ()
    {
        if ( mRemoteFirmware == null || mRemoteFirmware.getFile_link() == null )
        {
            mView.showMessage( mView.getMvpContext().getString( R.string.ota_msg_check_version) );
            return;
        }
        OkHttpManager.download( OTA_FIRMWARE_LINK + mRemoteFirmware.getFile_link(), mFirmwareFile, new DownloadCallback()
        {
            @Override
            public void onError ()
            {
                mView.onDownloadError();
            }

            @Override
            public void onProgress ( long total, long current )
            {
                mView.onDownloadProgress( total, current );
            }

            @Override
            public void onSuccess ( File file )
            {
                mFirmwareFile = file;
                mView.onDownloadSuccess();
            }
        } );
    }

    @Override
    public void convertFirmware ()
    {
        if ( mRemoteFirmware == null || mRemoteFirmware.getFile_name() == null )
        {
            mView.showMessage( mView.getMvpContext().getString( R.string.ota_firmware_invalid ) );
            return;
        }
        mCurrent = 0;
        mTotal = 0;
        mFirmwareFile = new File( mView.getMvpContext().getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ),
                                  mRemoteFirmware.getFile_name() );
        if ( mFirmwareFile == null || !mFirmwareFile.exists() || (!mFirmwareFile.getName().endsWith( ".txt" ) && !mFirmwareFile.getName().endsWith( ".hex" )) )
        {
            mView.showMessage( mView.getMvpContext().getString( R.string.ota_firmware_invalid ) );
            return;
        }
        mFrames = new ArrayList<>();
        new Thread( new Runnable() {
            @Override
            public void run ()
            {
                try
                {
                    FileReader fr = new FileReader( mFirmwareFile );
                    BufferedReader br = new BufferedReader( fr );
                    String line;
                    boolean linear = false;
                    while ( (line = br.readLine()) != null )
                    {
                        Frame frame = new Frame.Builder().createFromString( line );
                        if ( frame == null )
                        {
                            mFrames = null;
                            mView.showMessage( mView.getMvpContext().getString( R.string.ota_firmware_damaged) );
                            return;
                        }
                        if ( frame.getType() == 0x04 )
                        {
                            if ( frame.getData_list().get( 0 ) == 0 && frame.getData_list().get( 1 ) == 0 )
                            {
                                linear = false;
                            }
                            else
                            {
                                linear = true;
                            }
                        }
                        else if ( frame.getType() == 0x00 )
                        {
                            if ( !linear )
                            {
                                mFrames.add( frame );
                            }
                        }
                        else if ( frame.getType() == 0x01 )
                        {
                            mFrames.add( frame );
                        }
                    }
                    mTotal = mFrames.size();
                    enterBootloader();
                    mView.showMessage( mView.getMvpContext().getString( R.string.ota_analysis_success ) );
//                    mView.onConvertFirmwareSuccess();
                }
                catch ( FileNotFoundException e )
                {
                    e.printStackTrace();
                    mFrames = null;
                    mView.showMessage( e.toString() );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    mFrames = null;
                    mView.showMessage( e.toString() );
                }
            }
        } ).start();
    }

    @Override
    public void enterBootloader ()
    {
        mCurrentCommand = OTAConstants.OTA_CMD_GET_STATUS;
        byte[] bytes = new byte[]{ OTAConstants.OTA_CMD_GET_STATUS, 0x00, 0x00, 0x00, OTAConstants.OTA_CMD_GET_STATUS };
        BleManager.getInstance()
                  .sendBytes( mAddress, bytes );
        mCountDownTimer.start();
    }

    @Override
    public void getBootloaderInfo ()
    {
        mCurrentCommand = OTAConstants.OTA_CMD_GET_VERSION;
        byte[] bytes = new byte[]{ OTAConstants.OTA_CMD_GET_VERSION, 0x00, 0x00, 0x00 };
        BleManager.getInstance()
                  .sendBytes( mAddress, bytes );
        mCountDownTimer.start();
    }

    @Override
    public void eraseFirmware ()
    {
        int length = 0;
        for ( int i = mAppStartAddress; i < mAppEndAddress; i += mEraseBlockSize )
        {
            length++;
        }
        mCurrentCommand = OTAConstants.OTA_CMD_ERASE_FLASH;
        byte[] bytes = new byte[]{ OTAConstants.OTA_CMD_ERASE_FLASH,
                                   (byte) length,
                                   (byte) ( mAppStartAddress & 0xFF ),
                                   (byte) ( ( mAppStartAddress >> 8 ) & 0xFF ) };
        BleManager.getInstance()
                  .sendBytes( mAddress, bytes );
        mCountDownTimer.start();
    }

    @Override
    public void upgradeFirmware ()
    {
        if ( mFrames != null && mFrames.size() > 0 )
        {
            Frame frame = mFrames.get( 0 );
            if ( frame.getType() == 0x01 )
            {
                resetDevice();
            }
            else if ( frame.getType() == 0x00 )
            {
                byte[] bytes = new byte[frame.getData_length() + 4];
                int addr = frame.getAddress();
                bytes[0] = OTAConstants.OTA_CMD_WRITE_FLASH;
                bytes[1] = (byte) frame.getData_length();
                bytes[2] = (byte) ( addr & 0x00FF );
                bytes[3] = (byte) ( ( addr & 0xFF00 ) >> 8 );
                for ( int i = 0; i < frame.getData_length(); i++ )
                {
                    bytes[4 + i] = frame.getData_list()
                                        .get( i );
                }
                mCurrentCommand = OTAConstants.OTA_CMD_WRITE_FLASH;
                BleManager.getInstance()
                          .sendBytes( mAddress, bytes );
                mCountDownTimer.start();
            }
        }
    }

    @Override
    public void resetDevice ()
    {
        byte[] bytes = new byte[]{OTAConstants.OTA_CMD_RESET_DEVICE, 0x00, 0x00, 0x00};
        mCurrentCommand = OTAConstants.OTA_CMD_RESET_DEVICE;
        BleManager.getInstance()
                  .sendBytes( mAddress, bytes );
        mCountDownTimer.start();
    }

    private void decodeMfrData ( String s )
    {
        byte[] mfr = DataUtil.hexToByteArray( s.replace( " ", "" ) );
        short devid;
        if ( mfr == null || mfr.length < 4 )
        {
            mView.onCheckDeviceFailure();
        }
        else
        {
            devid = (short) ( ( ( mfr[0] & 0xFF ) << 8 ) | ( mfr[1] & 0xFF ) );
            mDevid = devid;
            mDeviceMajorVersion = mfr[2] & 0xFF;
            mDeviceMinorVersion = mfr[3] & 0xFF;
            mView.onCheckDeviceSuccess( mDeviceMajorVersion, mDeviceMinorVersion );
        }
    }

    private void decodeReceiveData ( ArrayList< Byte > list )
    {
        LogUtil.d( "BleManager", "decodeReceiveData: " + list.toString() );
        if ( list == null || list.size() < 5 )
        {
            return;
        }
        int command = list.get( 0 );
        int length = list.get( 1 );
        byte result;
        switch ( command )
        {
            case OTAConstants.OTA_CMD_GET_VERSION:
                if ( length == 8 && list.size() == 12 )
                {
                    mBootloaderMinorVersion = list.get( 4 ) & 0xFF;
                    mBootloaderMajorVersion = list.get( 5 ) & 0xFF;
                    mAppStartAddress = ( ((list.get( 7 ) & 0xFF) << 8) | (list.get( 6 )&0xFF) );
                    mAppEndAddress = ( ((list.get( 9 ) & 0xFF) << 8) | (list.get( 8 )&0xFF) );
                    mEraseBlockSize = list.get( 10 ) & 0xFF;
                    mWriteBlockSize = list.get( 11 ) & 0xFF;
                    DecimalFormat df = new DecimalFormat( "00" );
                    mView.showMessage( mView.getMvpContext().getString( R.string.ota_bootloader_version )
                                       + mBootloaderMajorVersion +"." + df.format( mBootloaderMinorVersion ) );
                    mHandler.postDelayed( new Runnable() {
                        @Override
                        public void run ()
                        {
                            eraseFirmware();
                        }
                    }, 96 );
                }
                break;

            case OTAConstants.OTA_CMD_READ_FLASH:

                break;

            case OTAConstants.OTA_CMD_WRITE_FLASH:
                if ( length == 1 )
                {
                    result = list.get( 4 );
                    if ( result == OTAConstants.OTA_RESPONSE_SUCCESS )
                    {
                        int adrl = list.get( 2 ) & 0xFF;
                        int adrh = list.get( 3 ) & 0xFF;
                        if ( mFrames.get( 0 ).getAddress() == ((adrh<<8)|adrl) )
                        {
                            DecimalFormat df = new DecimalFormat( "0.0" );
                            mView.showUpgradeProgress( df.format((float) (mCurrent + 1) * 100 / mTotal ) + "%" );
                            mFrames.remove( 0 );
                            mCurrent++;
                            mHandler.postDelayed( new Runnable() {
                                @Override
                                public void run ()
                                {
                                    upgradeFirmware();
                                }
                            }, 48 );
                        }
                    }
                    else
                    {
                        if ( result == OTAConstants.OTA_REPONSE_OUTOF_RANGE )
                        {
                            mUpgrading = false;
                            mView.showMessage(mView.getMvpContext().getString( R.string.ota_outof_range ) );
                        }
                    }
                }
//                BleManager.getInstance().clearReceiveBuffer();
                break;

            case OTAConstants.OTA_CMD_ERASE_FLASH:
                if ( length == 1 )
                {
                    result = list.get( 4 );
                    if ( result == OTAConstants.OTA_RESPONSE_SUCCESS )
                    {
                        mHandler.postDelayed( new Runnable() {
                            @Override
                            public void run ()
                            {
                                mCurrent = 0;
                                upgradeFirmware();
                            }
                        }, 256 );
                        mView.showMessage( mView.getMvpContext().getString( R.string.ota_erasefirmware ) );
                    }
                    else
                    {
                        if ( result == OTAConstants.OTA_REPONSE_OUTOF_RANGE )
                        {
                            mUpgrading = false;
                            mView.showMessage( mView.getMvpContext().getString( R.string.ota_erase_failed ) );
                        }
                    }
                }
                break;

            case OTAConstants.OTA_CMD_CALC_CHECKSUM:
                if ( length == 1 && list.get( 4 ) == OTAConstants.OTA_REPONSE_OUTOF_RANGE )
                {
                    mUpgrading = false;
                    mView.showMessage( mView.getMvpContext().getString( R.string.ota_check_failed) );
                }
                else if ( length == 4 )
                {
                    int start_addr = ( ( list.get( 3 ) & 0xFF ) << 8 ) | ( list.get( 2 ) & 0xFF );
                    int end_addr = ( ( list.get( 5 ) & 0xFF ) << 8 ) | ( list.get( 4 ) & 0xFF );
                    int checksum = ( ( list.get( 7 ) & 0xFF ) << 8 ) | ( list.get( 6 ) & 0xFF );
                    mView.showMessage( mView.getMvpContext().getString( R.string.ota_check_success ) );
                }
                break;

            case OTAConstants.OTA_CMD_RESET_DEVICE:
                if ( length == 1 )
                {
                    result = list.get( 4 );
                    if ( result == OTAConstants.OTA_RESPONSE_SUCCESS )
                    {
                        mUpgrading = false;
                        mView.showMessage( mView.getMvpContext().getString( R.string.ota_upgrade_success ) );
                        BleManager.getInstance().disconnectDevice( mAddress );
                        BleManager.getInstance().refresh( mAddress );
                    }
                }
                break;

            case OTAConstants.OTA_CMD_GET_STATUS:
                if ( length == 1 && list.get( 4 ) == OTAConstants.OTA_RESPONSE_SUCCESS )
                {
                    mHandler.postDelayed( new Runnable() {
                        @Override
                        public void run ()
                        {
                            getBootloaderInfo();
                        }
                    }, 96 );
                    mUpgrading = true;
                    mView.showMessage( mView.getMvpContext().getString( R.string.ota_enter_bootloader ) );
                }
                else
                {
                    if ( length == 0 && list.get( 4 ) == ( OTAConstants.OTA_CMD_GET_STATUS ^ list.get( 2 ) ^ list.get( 3 ) ) )
                    {
                        mView.showMessage( mView.getMvpContext().getString( R.string.ota_reset_tobootloader ) );
                        BleManager.getInstance()
                                  .disconnectDevice( mAddress );
                        BleManager.getInstance().refresh( mAddress );
                        mHandler.postDelayed( new Runnable() {
                            @Override
                            public void run ()
                            {
                                mAutoConnect = true;
                                mView.showMessage( mView.getMvpContext().getString( R.string.ota_connecting) );
                                BleManager.getInstance().connectDevice( mAddress );
                            }
                        }, 30000 );
                    }
                }
                break;

            default:
                if ( length == 1 )
                {
                    result = list.get( 4 );
                    if ( result == OTAConstants.OTA_REPSONSE_INVALID_COMMAND )
                    {
                        mUpgrading = false;
                        mView.showMessage( mView.getMvpContext().getString( R.string.ota_invalid_command ) );
                    }
                }
                break;
        }
    }
}
