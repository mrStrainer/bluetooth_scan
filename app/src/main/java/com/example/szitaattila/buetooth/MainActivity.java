package com.example.szitaattila.buetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private static int REQUEST_ENABLE_BT;

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private TextView text_status;
    private Button scanButton;
    private Button pairedButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_status = findViewById(R.id.text_status);
        text_status.setText("Press something");

        scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mNewDevicesArrayAdapter.clear();
                doDiscovery();
            }
        });

        pairedButton = findViewById(R.id.button_paired);
        pairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNewDevicesArrayAdapter.clear();
                getPaired();
            }
        });

        /* ArrayAdapter<String> pairedDevicesArrayAdapter =
                new ArrayAdapter<String>(this, R.layout.device_name);*/
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
/*
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
*/
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.paired_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);






    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }
    private void getPaired() {
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            mNewDevicesArrayAdapter.add("No paired devices");
        }
    }
    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        // Indicate scanning in the title
        text_status.setText(R.string.scanning_status);
        //scanButton.setText("Stop scan");

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
            text_status.setText("Scanning stopped");
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(""+device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    text_status.setText("No devices found");
                    mNewDevicesArrayAdapter.add("No devices found");
                } else {
                    text_status.setText("Scan finished");
                }
            }
        }
    };

}
