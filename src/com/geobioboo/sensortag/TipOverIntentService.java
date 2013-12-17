package com.geobioboo.sensortag;

import ti.android.ble.common.BluetoothLeService;
import ti.android.ble.sensortag.R;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.widget.Toast;

public class TipOverIntentService extends IntentService {

    private static BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothLeService mBluetoothLeService = null;

    private static boolean mIsRunning;

    public TipOverIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!mIsRunning && checkForBluetoothLe()) {
            // listen for bluetooth state change
            registerReceiver(mBluetoothStateChangeReceiver, getBluetoothIntentFilter());

            startBluetoothLeService();
        }
    }

    private boolean checkForBluetoothLe() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
            return false;
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBtAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_LONG).show();
            return false;
        }

        if (!mBtAdapter.isEnabled()) {
            Toast.makeText(this, "bluetooth is off", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;

    }

    private void startBluetoothLeService() {

        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        // mServiceConnection calls scanLeDevice
    }

    private void scanLeDevice(boolean enable) {
        if (enable) {
            mBtAdapter.startLeScan(mLeScanCallback);
        } else {
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private IntentFilter getBluetoothIntentFilter() {

        IntentFilter mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return mFilter;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

            if (isSensorTag(device)) {
                mBluetoothDevice = device;
                connect();
            }
        }
    };

    private void connect() {

        int connState = mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothGatt.GATT);

        switch (connState) {
            case BluetoothGatt.STATE_CONNECTED:
                // already connected...
                break;
            case BluetoothGatt.STATE_DISCONNECTED:
                mBluetoothLeService.connect(mBluetoothDevice.getAddress());
                break;
            default:
                break;
        }

    }

    /* **************************************** */

    private boolean isSensorTag(BluetoothDevice device) {
        return device != null && "SensorTag".equals(device.getName());
    }

    /* **************************************** */

    private BroadcastReceiver mBluetoothStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                // Bluetooth adapter state change
                switch (mBtAdapter.getState()) {
                    case BluetoothAdapter.STATE_ON:
                        startBluetoothLeService();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    default:
                        break;
                }

            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                // GATT connect
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // TODO then do stuff in DeviceActivity
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // GATT disconnect
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
                //                stopDeviceActivity();
                if (status == BluetoothGatt.GATT_SUCCESS) {

                }
                mBluetoothLeService.close();
            }

        }
    };

    /* **************************************** */

    // Code to manage Service life cycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.initialize();
            scanLeDevice(true);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

}
