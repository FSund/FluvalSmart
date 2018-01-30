package com.inledco.bleota;

import android.content.Context;

/**
 * Created by liruya on 2017/5/8.
 */

public interface OTAContract
{
    interface Presenter
    {
        void start();

        void stop();

        void checkRemoteVersion();

        void checkDeviceVersion();

        void isUpgradable();

        void downloadFirmware();

        void convertFirmware();

        void enterBootloader();

        void getBootloaderInfo();

        void eraseFirmware();

        void upgradeFirmware();

        void resetDevice();
    }

    interface View
    {
        void setPresenter( Presenter presenter );

        Context getMvpContext();

        void onDataValid();

        void onDataInvalid();

        void onCheckRemoteSuccess( int major_version, int minor_version );

        void onCheckRemoteFailure();

        void onCheckDeviceSuccess( int major_version, int minor_version );

        void onCheckDeviceFailure();

        void onFirmwareExists( boolean exist );

        void onDownloadError();

        void onDownloadProgress( long total, long current );

        void onDownloadSuccess();

        void onConvertFirmwareSuccess();

        void onConvertFirmwareError( String msg );

        void onEnterBootloader();

        void onResetToBootloader();

        void showMessage( String msg );

        void showUpgradeWarning();

        void showUpgradeProgress( String msg );
    }
}
