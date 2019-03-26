package com.inledco.fluvalsmart.ota;

import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ble.api.DataUtil;
import com.inledco.fluvalsmart.R;
import com.liruya.okhttpmanager.DownloadCallback;
import com.liruya.okhttpmanager.HttpCallback;
import com.liruya.okhttpmanager.OKHttpManager;
import com.liruya.tuner168blemanager.BleListener;
import com.liruya.tuner168blemanager.BleManager;
import com.liruya.tuner168blemanager.BleSimpleListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by liruya on 2017/5/8.
 */

public class OTAPresenter extends BaseActivityPresenter<BleOTAActivity> {
    private static final String TAG = "OTAPresenter";

    private static final int OTA_SUPPORT_LOWEST_VERSION = 0x0102;
    private static final String OTA_UPGRADE_LINK = "http://47.88.12.183:8080/OTAInfoModels/GetOTAInfo?deviceid=";
    private static final String OTA_FIRMWARE_LINK = "http://47.88.12.183:8080";

    private boolean mReadRemoteVersion;
    private boolean mProcessing;

    private IOTAView mView;
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

    private final Handler mHandler;

    private File mFirmwareFile;
    private ArrayList<Frame> mFrames;
    private int mCurrent;
    private int mTotal;
    private boolean mTestMode = false;

    private byte mCurrentCommand;
    private CountDownTimer mCountDownTimer;

    private BleListener mBleListener;

    public OTAPresenter(BleOTAActivity t, IOTAView view, @NonNull short devid, @NonNull String address, @NonNull String remoteVersionUrl, boolean mode) {
        super(t);
        mView = view;
        mDevid = devid;
        mAddress = address;
        mRemoteVersionUrl = remoteVersionUrl;
        mTestMode = mode;
        mHandler = new Handler();
        mCountDownTimer = new CountDownTimer(1000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mProcessing = false;
                mView.showMessage(getString(R.string.ota_response_timeout));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.showErrorDialog();
                    }
                });
            }
        };
    }

    public boolean isProcessing() {
        return mProcessing;
    }

    public void start() {
        mBleListener = new BleSimpleListener(mAddress) {
            @Override
            public void onConnected() {
                BleManager.getInstance().setAutoConnect(mAddress, true);
            }

            @Override
            public void onConnectTimeout() {
                mView.showDeviceDisconnected();
                mView.showMessage(getString(R.string.ota_disconnect));
            }

            @Override
            public void onConnectionError(int error, int newState) {
                super.onConnectionError(error, newState);
            }

            @Override
            public void onDisconnected() {
                mView.showDeviceDisconnected();
                mView.showMessage(getString(R.string.ota_disconnect));
            }

            @Override
            public void onDataValid() {
                mView.showDeviceConnected();
                mView.showMessage(getString(R.string.ota_connect_success));
            }

            @Override
            public void onDataReceived(List<Byte> bytes) {
                if (bytes.get(0) == mCurrentCommand) {
                    decodeReceiveData(bytes);
                }
            }

            @Override
            public void onReadMfr(String s) {
                decodeMfrData(s);
            }
        };
        BleManager.getInstance()
                  .addBleListener(mBleListener);
    }

    public void stop() {
        mCountDownTimer.cancel();
        BleManager.getInstance()
                  .removeBleListener(mBleListener);
        BleManager.getInstance()
                  .disconnectDevice(mAddress);
    }

    public void stopProcess() {
        mProcessing = false;
    }

    /**
     * @param flag false:first time true:second time after bootloader
     */
    public void checkUpdate(final boolean flag) {
        BleManager.getInstance()
                  .disconnectDevice(mAddress);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mProcessing = true;
                while (BleManager.getInstance().isConnected(mAddress));
                long st = System.currentTimeMillis();
                while (System.currentTimeMillis() - st < 480);
                st = System.currentTimeMillis();
                mView.showMessage(getString(R.string.ota_connecting));
                BleManager.getInstance()
                          .refresh(mAddress);
                BleManager.getInstance()
                          .connectDevice(mAddress);
                while (BleManager.getInstance()
                                 .isDataValid(mAddress) == false) {
                    if (System.currentTimeMillis() - st > 5000) {
                        mView.showMessage(getString(R.string.ota_disconnect));
                        mProcessing = false;
                        return;
                    }
                }
                mDeviceMajorVersion = 0;
                mDeviceMinorVersion = 0;
                mRemoteFirmware = null;
                mReadRemoteVersion = false;
                final DecimalFormat df = new DecimalFormat("00");

                st = System.currentTimeMillis();
                while (System.currentTimeMillis() - st < 160) {
                }
                BleManager.getInstance()
                          .readMfr(mAddress);
                OKHttpManager.getInstance()
                             .get(OTA_UPGRADE_LINK + mDevid, null, new HttpCallback<RemoteFirmware>() {
                                 @Override
                                 public void onFailure(Call call, IOException e) {
                                     Log.e(TAG, "onFailure: " + e.getMessage());
                                     mView.showRemoteVersion(getString(R.string.failed));
                                     mReadRemoteVersion = true;
                                 }

                                 @Override
                                 public void onError(int code, final String msg) {
                                     if (msg == null) {
                                         mView.showMessage(getString(R.string.ota_msg_remote_not_exists));
                                     }
                                     else {
                                         mView.showMessage(msg);
                                     }
                                     mView.showRemoteVersion(getString(R.string.failed));
                                     mReadRemoteVersion = true;
                                 }

                                 @Override
                                 public void onSuccess(final RemoteFirmware result) {
                                     mRemoteFirmware = result;
                                     mView.showRemoteVersion("" + result.getMajor_version() + "." + df.format(result.getMinor_version()));
                                     mReadRemoteVersion = true;
                                 }
                             });
                long ct = System.currentTimeMillis();
                while (System.currentTimeMillis() - ct < 1000 || mReadRemoteVersion == false) {

                }
                final int device_version = (mDeviceMajorVersion << 8) | mDeviceMinorVersion;
                if (device_version == 0) {
                    mView.showDeviceVersion(getString(R.string.failed));
                }
                if (mRemoteFirmware == null) {
                    mView.showRemoteVersion(getString(R.string.failed));
                }
                if (device_version == 0 || mRemoteFirmware == null) {
                    mProcessing = false;
                    return;
                }
                if (device_version < OTA_SUPPORT_LOWEST_VERSION) {
                    mProcessing = false;
                    mView.showMessage(getString(R.string.ota_msg_unsupport_version));
                    return;
                }
                final int remote_version = (mRemoteFirmware.getMajor_version() << 8) | mRemoteFirmware.getMinor_version();
                if (mTestMode || device_version < remote_version) {
                    if (!flag) {
                        String v = "V" + mRemoteFirmware.getMajor_version() + "." + df.format(mRemoteFirmware.getMinor_version());
                        final String msg = getString(R.string.ota_device_firmware_upgradable).replace("Vxx", v);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mView.showUpgradeConfirmDialog(msg);
                            }
                        });
                    }
                    else {
                        enterBootloader();
                    }
                } else {
                    mView.showMessage(getString(R.string.ota_firmware_newest));
                    mProcessing = false;
                }
            }
        };
        new Thread(runnable).start();
    }

    public void downloadFirmware() {
        if (mRemoteFirmware == null || mRemoteFirmware.getFile_link() == null) {
            mProcessing = false;
            mView.showMessage(getString(R.string.ota_msg_check_version));
            return;
        }
        if (getContext() != null) {
            mFirmwareFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), mRemoteFirmware.getFile_name());
            if (mFirmwareFile.exists()) {
                mFirmwareFile.delete();
            }
            mView.showMessage(getString(R.string.downloading_firmware));
            OKHttpManager.getInstance()
                         .download(OTA_FIRMWARE_LINK + mRemoteFirmware.getFile_link(), mFirmwareFile, new DownloadCallback() {
                             @Override
                             public void onError(String s) {
                                 mView.showMessage(getString(R.string.ota_download_failed));
                                 mProcessing = false;
                             }

                             @Override
                             public void onProgress(long total, long current) {
                                 final float percent = (float) current / total;
                                 final DecimalFormat df = new DecimalFormat("0.0%");
                                 mView.showUpgradeProgress(df.format(percent) + "\r\n");
                             }

                             @Override
                             public void onSuccess(File file) {
                                 mFirmwareFile = file;
                                 mView.showMessage(getString(R.string.ota_download_success));
                                 if (mFirmwareFile == null ||
                                     !mFirmwareFile.exists() ||
                                     (!mFirmwareFile.getName()
                                                    .endsWith(".txt") &&
                                      !mFirmwareFile.getName()
                                                    .endsWith(".hex")))
                                 {
                                     mProcessing = false;
                                     mView.showMessage(getString(R.string.ota_firmware_invalid));
                                     return;
                                 }
                                 convertFirmware();
                             }
                         });
        }
        else {
            mProcessing = false;
        }
    }

    public void convertFirmware() {
        mCurrent = 0;
        mTotal = 0;
        mFrames = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileReader fr = new FileReader(mFirmwareFile);
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    boolean linear = false;
                    while ((line = br.readLine()) != null) {
                        Frame frame = new Frame.Builder().createFromString(line);
                        if (frame == null) {
                            mFrames = null;
                            mProcessing = false;
                            mView.showMessage(getString(R.string.ota_firmware_damaged));
                            return;
                        }
                        if (frame.getType() == 0x04) {
                            linear = frame.getData_list()
                                          .get(0) != 0 ||
                                     frame.getData_list()
                                          .get(1) != 0;
                        }
                        else {
                            if (frame.getType() == 0x00) {
                                if (!linear) {
                                    mFrames.add(frame);
                                }
                            }
                            else {
                                if (frame.getType() == 0x01) {
                                    mFrames.add(frame);
                                }
                            }
                        }
                    }
                    mTotal = mFrames.size();
                    enterBootloader();
                    mView.showMessage(getString(R.string.ota_analysis_success));
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mFrames = null;
                    mProcessing = false;
                    mView.showMessage(e.toString());
                }
                catch (IOException e) {
                    e.printStackTrace();
                    mFrames = null;
                    mProcessing = false;
                    mView.showMessage(e.toString());
                }
            }
        }).start();
    }

    public void enterBootloader() {
        mCurrentCommand = OTAConstants.OTA_CMD_GET_STATUS;
        byte[] bytes = new byte[]{OTAConstants.OTA_CMD_GET_STATUS, 0x00, 0x00, 0x00, OTAConstants.OTA_CMD_GET_STATUS};
        BleManager.getInstance()
                  .sendBytes(mAddress, bytes);
        mCountDownTimer.start();
    }

    public void getBootloaderInfo() {
        mCurrentCommand = OTAConstants.OTA_CMD_GET_VERSION;
        byte[] bytes = new byte[]{OTAConstants.OTA_CMD_GET_VERSION, 0x00, 0x00, 0x00};
        BleManager.getInstance()
                  .sendBytes(mAddress, bytes);
        mCountDownTimer.start();
    }

    public void eraseFirmware() {
        int length = 0;
        for (int i = mAppStartAddress; i < mAppEndAddress; i += mEraseBlockSize) {
            length++;
        }
        mCurrentCommand = OTAConstants.OTA_CMD_ERASE_FLASH;
        byte[] bytes = new byte[]{OTAConstants.OTA_CMD_ERASE_FLASH, (byte) length, (byte) (mAppStartAddress & 0xFF), (byte) ((mAppStartAddress >> 8) & 0xFF)};
        BleManager.getInstance()
                  .sendBytes(mAddress, bytes);
        mCountDownTimer.start();
    }

    public void upgradeFirmware() {
        if (mFrames != null && mFrames.size() > 0) {
            Frame frame = mFrames.get(0);
            if (frame.getType() == 0x01) {
                resetDevice();
            }
            else {
                if (frame.getType() == 0x00) {
                    byte[] bytes = new byte[frame.getData_length() + 4];
                    int addr = frame.getAddress();
                    bytes[0] = OTAConstants.OTA_CMD_WRITE_FLASH;
                    bytes[1] = (byte) frame.getData_length();
                    bytes[2] = (byte) (addr & 0x00FF);
                    bytes[3] = (byte) ((addr & 0xFF00) >> 8);
                    for (int i = 0; i < frame.getData_length(); i++) {
                        bytes[4 + i] = frame.getData_list()
                                            .get(i);
                    }
                    mCurrentCommand = OTAConstants.OTA_CMD_WRITE_FLASH;
                    BleManager.getInstance()
                              .sendBytes(mAddress, bytes);
                    mCountDownTimer.start();
                }
            }
        }
    }

    public void resetDevice() {
        byte[] bytes = new byte[]{OTAConstants.OTA_CMD_RESET_DEVICE, 0x00, 0x00, 0x00};
        mCurrentCommand = OTAConstants.OTA_CMD_RESET_DEVICE;
        BleManager.getInstance()
                  .sendBytes(mAddress, bytes);
        mCountDownTimer.start();
    }

    private void decodeMfrData(String s) {
        byte[] mfr = DataUtil.hexToByteArray(s.replace(" ", ""));
        short devid;
        if (mfr == null || mfr.length < 4) {
            mView.showDeviceVersion(getString(R.string.failed));
        }
        else {
            devid = (short) (((mfr[0] & 0xFF) << 8) | (mfr[1] & 0xFF));
            mDevid = devid;
            mDeviceMajorVersion = mfr[2] & 0xFF;
            mDeviceMinorVersion = mfr[3] & 0xFF;
            DecimalFormat df = new DecimalFormat("00");
            mView.showDeviceVersion("" + mDeviceMajorVersion + "." + df.format(mDeviceMinorVersion));
        }
    }

    private void decodeReceiveData(List<Byte> list) {
        if (list == null || list.size() < 5) {
            return;
        }
        int command = list.get(0);
        int length = list.get(1);
        byte result;
        switch (command) {
            case OTAConstants.OTA_CMD_GET_VERSION:
                if (length == 8 && list.size() == 12) {
                    mCountDownTimer.cancel();
                    mBootloaderMinorVersion = list.get(4) & 0xFF;
                    mBootloaderMajorVersion = list.get(5) & 0xFF;
                    mAppStartAddress = (((list.get(7) & 0xFF) << 8) | (list.get(6) & 0xFF));
                    mAppEndAddress = (((list.get(9) & 0xFF) << 8) | (list.get(8) & 0xFF));
                    mEraseBlockSize = list.get(10) & 0xFF;
                    mWriteBlockSize = list.get(11) & 0xFF;
                    DecimalFormat df = new DecimalFormat("00");
                    mView.showMessage(getString(R.string.ota_bootloader_version) + mBootloaderMajorVersion + "." + df.format(mBootloaderMinorVersion));
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            eraseFirmware();
                        }
                    }, 96);
                }
                break;

            case OTAConstants.OTA_CMD_READ_FLASH:

                break;

            case OTAConstants.OTA_CMD_WRITE_FLASH:
                if (length == 1) {
                    result = list.get(4);
                    if (result == OTAConstants.OTA_RESPONSE_SUCCESS) {
                        int adrl = list.get(2) & 0xFF;
                        int adrh = list.get(3) & 0xFF;
                        if (mFrames.get(0)
                                   .getAddress() == ((adrh << 8) | adrl))
                        {
                            mCountDownTimer.cancel();
                            DecimalFormat df = new DecimalFormat("0.0");
                            mView.showUpgradeProgress(df.format((float) (mCurrent + 1) * 100 / mTotal) + "%");
                            mFrames.remove(0);
                            mCurrent++;
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    upgradeFirmware();
                                }
                            }, 48);
                        }
                    }
                    else {
                        if (result == OTAConstants.OTA_REPONSE_OUTOF_RANGE) {
                            mCountDownTimer.cancel();
                            mProcessing = false;
                            mView.showMessage(getString(R.string.ota_outof_range));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mView.showErrorDialog();
                                }
                            });
                        }
                    }
                }
                break;

            case OTAConstants.OTA_CMD_ERASE_FLASH:
                if (length == 1) {
                    result = list.get(4);
                    if (result == OTAConstants.OTA_RESPONSE_SUCCESS) {
                        mCountDownTimer.cancel();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCurrent = 0;
                                upgradeFirmware();
                            }
                        }, 256);
                        mView.showMessage(getString(R.string.ota_erasefirmware));
                    }
                    else {
                        if (result == OTAConstants.OTA_REPONSE_OUTOF_RANGE) {
                            mCountDownTimer.cancel();
                            mProcessing = false;
                            mView.showMessage(getString(R.string.ota_erase_failed));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mView.showErrorDialog();
                                }
                            });
                        }
                    }
                }
                break;

            case OTAConstants.OTA_CMD_CALC_CHECKSUM:
                if (length == 1 && list.get(4) == OTAConstants.OTA_REPONSE_OUTOF_RANGE) {
                    mCountDownTimer.cancel();
                    mProcessing = false;
                    mView.showMessage(getString(R.string.ota_check_failed));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.showErrorDialog();
                        }
                    });
                }
                else {
                    if (length == 4) {
                        mCountDownTimer.cancel();
                        int start_addr = ((list.get(3) & 0xFF) << 8) | (list.get(2) & 0xFF);
                        int end_addr = ((list.get(5) & 0xFF) << 8) | (list.get(4) & 0xFF);
                        int checksum = ((list.get(7) & 0xFF) << 8) | (list.get(6) & 0xFF);
                        mView.showMessage(getString(R.string.ota_check_success));
                    }
                }
                break;

            case OTAConstants.OTA_CMD_RESET_DEVICE:
                if (length == 1) {
                    result = list.get(4);
                    if (result == OTAConstants.OTA_RESPONSE_SUCCESS) {
                        mCountDownTimer.cancel();
                        mProcessing = false;
                        mView.showMessage(getString(R.string.ota_upgrade_success));
                        BleManager.getInstance()
                                  .disconnectDevice(mAddress);
                        BleManager.getInstance()
                                  .refresh(mAddress);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mView.showSuccessDialog();
                            }
                        });
                    }
                }
                break;

            case OTAConstants.OTA_CMD_GET_STATUS:
                if (length == 1 && list.get(4) == OTAConstants.OTA_RESPONSE_SUCCESS) {
                    mCountDownTimer.cancel();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getBootloaderInfo();
                        }
                    }, 96);
                    mView.showMessage(getString(R.string.ota_enter_bootloader));
                }
                else {
                    if (length == 0 && list.get(4) == (OTAConstants.OTA_CMD_GET_STATUS ^ list.get(2) ^ list.get(3))) {
                        mCountDownTimer.cancel();
                        BleManager.getInstance()
                                  .disconnectDevice(mAddress);
                        mView.showMessage(getString(R.string.ota_reset_tobootloader));
                        Log.e(TAG, "decodeReceiveData: reset enter bootloader");
                        BleManager.getInstance()
                                  .refresh(mAddress);
                        //在UI线程中 使用Handler
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mView.showRepowerDialog();
                                    }
                                }, 1000);
                            }
                        });
                    }
                }
                break;

            default:
                if (length == 1) {
                    result = list.get(4);
                    if (result == OTAConstants.OTA_REPSONSE_INVALID_COMMAND) {
                        mCountDownTimer.cancel();
                        mProcessing = false;
                        mView.showMessage(getString(R.string.ota_invalid_command));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mView.showErrorDialog();
                            }
                        });
                    }
                }
                break;
        }
    }
}
