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
import com.sohrab.obd.reader.obdCommand.ObdCommand;
import com.sohrab.obd.reader.obdCommand.ObdConfiguration;
import com.sohrab.obd.reader.obdCommand.SpeedCommand;
import com.sohrab.obd.reader.obdCommand.engine.LoadCommand;
import com.sohrab.obd.reader.obdCommand.engine.RPMCommand;
import com.sohrab.obd.reader.obdCommand.temperature.EngineCoolantTemperatureCommand;
import com.sohrab.obd.reader.service.ObdReaderService;
import com.sohrab.obd.reader.trip.TripRecord;
import com.sohrab.obd.reader.util.DialogUtils;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.MultimediaUtils;
import com.sohrab.obd.reader.util.Utils;

import java.util.ArrayList;

import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_OBD_CONNECTION_STATUS;
import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA;

/*TODO
        notification to prevent app killing
        sound
            OBD connected
            battery low voltagealert
            app battery optimization not disabled
            engine shut down
        auto-kill app when engine shut down?
        send health intent to tasker??  to detect if app not running?
 */


public class SampleActivity extends AppCompatActivity {

    private TextView mObdInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.checkExternalStorageAccess(this);
        Logs.info("App Opened ====================================================================");

        MultimediaUtils.playSound(this, "app-started.mp3");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mObdInfoTextView = findViewById(R.id.tv_obd_info);


        //configure obd: add required command in arrayList and set to ObdConfiguration.
        //If you dont set any command or passing null, then all command OBD command will be requested.  (in case you want to read EVERYTHING)
        ArrayList<ObdCommand> obdCommands = new ArrayList<>();
        obdCommands.add(new SpeedCommand());
        obdCommands.add(new RPMCommand());
        obdCommands.add(new EngineCoolantTemperatureCommand());
        obdCommands.add(new LoadCommand());
        ObdConfiguration.setmObdCommands(this, obdCommands);


        // set gas price per litre so that gas cost can calculated. Default is 7 $/l
        float gasPrice = 7; // per litre, you should initialize according to your requirement.
        ObdPreferences.get(this).setGasPrice(gasPrice);
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
                mObdInfoTextView.setText(connectionStatusMsg);
                Toast.makeText(SampleActivity.this, connectionStatusMsg, Toast.LENGTH_SHORT).show();

                if (connectionStatusMsg.equals(getString(R.string.obd_connected))) {
                    //OBD connected  do what want after OBD connection
                    Logs.info("OBD device connected -------------------");
                    MultimediaUtils.playSound(context, "obd-device-connected.mp3");
                } else if (connectionStatusMsg.equals(getString(R.string.connect_lost))) {
                    Logs.info("OBD device dis-connected -------------------");
                    MultimediaUtils.playSound(context, "obd-device-disconnected.mp3");
                } else {
                    // here you could check OBD connection and pairing status
                }

            } else if (action.equals(ACTION_READ_OBD_REAL_TIME_DATA)) {

                TripRecord tripRecord = TripRecord.getTripRecode(SampleActivity.this);
                mObdInfoTextView.setText(tripRecord.toString());

                // here you can fetch real time data from TripRecord using getter methods like
                //tripRecord.getSpeed();
                //tripRecord.getEngineRpm();

                //=============== Engine coolant temp alert =====================================================
                AlertHandler.checkCoolantTemp(context, tripRecord);

            }

        }
    };

    @Override
    protected void onDestroy() {
        MultimediaUtils.playSound(this, "test.mp3");


        super.onDestroy();
        //unregister receiver
        unregisterReceiver(mObdReaderReceiver);
        //stop service
        stopService(new Intent(this, ObdReaderService.class));
        // This will stop background thread if any running immediately.
        ObdPreferences.get(this).setServiceRunningStatus(false);
    }

}
