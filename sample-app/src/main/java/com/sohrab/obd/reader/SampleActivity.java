package com.sohrab.obd.reader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sohrab.obd.reader.alerts.AlertHandler;
import com.sohrab.obd.reader.application.ObdPreferences;
import com.sohrab.obd.reader.common.AppAutoTerminate;
import com.sohrab.obd.reader.common.AppConfig;
import com.sohrab.obd.reader.common.Declarations;
import com.sohrab.obd.reader.obd.VehicleStatus;
import com.sohrab.obd.reader.obdCommand.ObdCommand;
import com.sohrab.obd.reader.obdCommand.ObdConfiguration;
import com.sohrab.obd.reader.obdCommand.SpeedCommand;
import com.sohrab.obd.reader.obdCommand.control.ModuleVoltageCommand;
import com.sohrab.obd.reader.obdCommand.engine.RPMCommand;
import com.sohrab.obd.reader.obdCommand.temperature.EngineCoolantTemperatureCommand;
import com.sohrab.obd.reader.service.ObdReaderService;
import com.sohrab.obd.reader.trip.TripRecord;
import com.sohrab.obd.reader.util.DateUtils;
import com.sohrab.obd.reader.util.DialogUtils;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.MultimediaUtils;
import com.sohrab.obd.reader.util.Utils;
import com.sohrab.wifi.WifiScanner;

import java.util.ArrayList;
import java.util.Date;

import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_OBD_CONNECTION_STATUS;
import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA;

public class SampleActivity extends AppCompatActivity {

    private TextView mObdInfoTextView;
    private WifiScanner wifiScanner = new WifiScanner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.checkExternalStorageAccess(this);

        //MultimediaUtils.testAllSoundFiles(this);   //test only....

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mObdInfoTextView = findViewById(R.id.tv_obd_info);

        final AppCompatActivity ctx = this;


        if (!AppConfig.validateIfAllConfigValuesPresent(this))
            return;

        if (!Utils.checkRequiredPermissions(this))
            return;

        MultimediaUtils.playSound(this, MultimediaUtils.SoundFile.APP_STARTED, "App started =================================");

        //configure obd: add required command in arrayList and set to ObdConfiguration.
        //If you dont set any command or passing null, then all command OBD command will be requested.  (in case you want to read EVERYTHING)
        ArrayList<ObdCommand> obdCommands = new ArrayList<>();
        obdCommands.add(new SpeedCommand());
        obdCommands.add(new RPMCommand());
        obdCommands.add(new EngineCoolantTemperatureCommand());
        obdCommands.add(new ModuleVoltageCommand());

        //obdCommands = null; // reading ALL
        ObdConfiguration.setmObdCommands(this, obdCommands);


        // set gas price per litre so that gas cost can calculated. Default is 7 $/l
        //float gasPrice = 7; // per litre, you should initialize according to your requirement.
        //ObdPreferences.get(this).setGasPrice(gasPrice);

        //TODO should this be MULTIPLIER offset instead of ADDITION affset? test what's diff at various speeds
        ObdPreferences.get(this).setOffsetVehicleSpeed(AppConfig.getOffsetSpeed());
        ObdPreferences.get(this).setOffsetCoolantTemperature(AppConfig.getOffsetCoolantTemperature());


        /**
         * Register receiver with some action related to OBD connection status
         */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_READ_OBD_REAL_TIME_DATA);
        intentFilter.addAction(ACTION_OBD_CONNECTION_STATUS);
        registerReceiver(mObdReaderReceiver, intentFilter);

        //start service which will execute in background for connecting and execute command until you stop
        startService(new Intent(this, ObdReaderService.class));


        if (Utils.isBatteryOptimizationEnabled(this)) {
            DialogUtils.alertDialog(this, "Disable the battery optimization...");
        }

        wifiScanner.init(this);

        AppAutoTerminate.init(this);
        MultimediaUtils.checkIfAllSoundFilesPresent(this);
    }

    /**
     * Broadcast Receiver to receive OBD connection status and real time data
     */
    private final BroadcastReceiver mObdReaderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            findViewById(R.id.progress_bar).setVisibility(View.GONE);
            mObdInfoTextView.setVisibility(View.VISIBLE);
            String action = intent.getAction();

            if (action.equals(ACTION_OBD_CONNECTION_STATUS)) {
                String connectionStatusMsg = intent.getStringExtra(ObdReaderService.INTENT_OBD_EXTRA_DATA);
                Logs.info(connectionStatusMsg);
                mObdInfoTextView.setText(connectionStatusMsg + getConfigText());
                Toast.makeText(SampleActivity.this, connectionStatusMsg, Toast.LENGTH_SHORT).show();

                if (connectionStatusMsg.equals(getString(R.string.obd_connected))) {
                    MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.OBD_DEVICE_CONNECTED, "OBD device connected");
                } else if (connectionStatusMsg.equals(getString(R.string.connect_lost))) {
                    MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.OBD_DEVICE_DISCONNECTED, "OBD device dis-connected");
                } else {
                    // here you could check OBD connection and pairing status
                }

            } else if (action.equals(ACTION_READ_OBD_REAL_TIME_DATA)) {
                TripRecord tripRecord = TripRecord.getTripRecode(SampleActivity.this);
                mObdInfoTextView.setText(tripRecord.toString() + getConfigText());

                VehicleStatus.update(context, tripRecord);
                AlertHandler.handle(context);
            }
        }
    };


    private String getConfigText() {
        String txt = "";
        txt += "\n\n\n";
        txt += "---------------\n";
        txt += "Conf \n";
        txt += "---------------\n";
        txt += "Coolant alert temperature: " + AppConfig.getCoolantAlertTemperature() + " C\n";
        txt += "Coolant optimal temperature: " + AppConfig.getCoolantOptimalTemperature() + " C\n";
        txt += "Low Voltage alert: " + AppConfig.getLowVoltageAlertLimit() + " V\n";
        txt += "Alert interval: " + AppConfig.getAlertIntervalSeconds() + " sec\n";
        txt += "High speed alert: " + AppConfig.getHighSpeedAlertKmpl() + " Kmpl\n";
        txt += "High speed alert delay: " + AppConfig.getHighSpeedAlertIntervalSeconds() + " sec\n";
        txt += "Tasker health status interval: " + AppConfig.getTaskerHealthStatusIntervalSeconds() + " sec\n";
        txt += "Auto terminate after engine off: " + AppConfig.getAutoTerminateAfterEngineOffSeconds() + " sec\n";
        txt += "Offset speed: " + AppConfig.getOffsetSpeed() + "\n";
        txt += "Offset coolant temperature: " + AppConfig.getOffsetCoolantTemperature() + "\n";

        txt += "\n";

        //txt += "Engine:  Running (" + VehicleStatus.engineRunningDurationSeconds() + " sec),   Stopped ("+  VehicleStatus.engineOffDurationSeconds() + " sec) \n";
        txt += VehicleStatus.getStatus() + "\n";

        txt += "(" + DateUtils.format("HH:mm:ss.S", new Date()) + ")                                      v" + Declarations.APP_VER;

        txt += "\n\nNOTE: Lock the app in 'Recent Apps' to prevent accidental close";
        return txt;
    }

    @Override
    public void onBackPressed() {
        //hack: to prevent activity from closing on back button press
        this.moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        MultimediaUtils.playSound(this, MultimediaUtils.SoundFile.APP_CLOSING);
        Utils.delay(5000);

        super.onDestroy();
        //unregister receiver
        unregisterReceiver(mObdReaderReceiver);
        //stop service
        stopService(new Intent(this, ObdReaderService.class));
        // This will stop background thread if any running immediately.
        ObdPreferences.get(this).setServiceRunningStatus(false);
    }

}
