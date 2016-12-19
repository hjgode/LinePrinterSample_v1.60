package com.honeywell.mobility.lineprint.sample;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;



/**
 * Created by E841685 on 06/12/2016.
 */

public class PairingRequest extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            try {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
               // Toast.makeText(context, "Bt Pairing request", Toast.LENGTH_SHORT).show();
                 int pin = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 0);
                device.setPairingConfirmation(true);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}




