package com.geobioboo.sensortag;

import java.util.UUID;

import ti.android.ble.common.BluetoothLeService;
import ti.android.ble.sensortag.R;
import ti.android.ble.sensortag.Sensor;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class TipOverIntentService extends IntentService {

    private static BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothLeService mBluetoothLeService = null;

    private BluetoothLeService mBtLeService = null;
    private BluetoothGatt mBtGatt = null;
    private static final int GATT_TIMEOUT = 100; // milliseconds

    private static boolean mIsRunning;

    public TipOverIntentService() {
        super("bleh");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!mIsRunning && checkForBluetoothLe()) {
            // listen for bluetooth state change
            registerReceiver(mBluetoothStateChangeReceiver, getBluetoothIntentFilter());

            startBluetoothLeService();

            mBtLeService = BluetoothLeService.getInstance(); // TODO maybe need a callback to init these!

            mBtGatt = BluetoothLeService.getBtGatt();

            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

            mIsRunning = true;
        }
    }

    private boolean checkForBluetoothLe() {
        Log.d("geobio", "tip checkForBluetoothLe");
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
        Log.d("geobio", "tip startBluetoothLeService");

        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        // mServiceConnection calls scanLeDevice
    }

    private void scanLeDevice(boolean enable) {
        Log.d("geobio", "tip scanLeDevice " + enable);
        if (enable) {
            mBtAdapter.startLeScan(mLeScanCallback);
        } else {
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private static IntentFilter getBluetoothIntentFilter() {

        IntentFilter mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return mFilter;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            Log.d("geobio", "tip onLeScan");
            if (isSensorTag(device)) {
                mBluetoothDevice = device;
                connect();
            }
        }
    };

    private void connect() {

        Log.d("geobio", "tip connect");
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
                Log.d("geobio", "tip state change onReceive state changed");
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
                Log.d("geobio", "tip state change onReceive gatt connected");
                // GATT connect
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // TODO then do stuff in DeviceActivity
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("geobio", "tip state change onReceive gatt disconnected");
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
            Log.d("geobio", "tip service connected connected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.initialize();
            scanLeDevice(true);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("geobio", "tip service connection disconnected");
            mBluetoothLeService = null;
        }
    };

    /* **************************************************************
    ************************************************************ */

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        fi.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        fi.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        fi.addAction(BluetoothLeService.ACTION_DATA_READ);
        return fi;
    }

    private void enableSensors(boolean enable) {
        Sensor sensor = Sensor.ACCELEROMETER; // the only sensor I want

        UUID servUuid = sensor.getService();
        UUID confUuid = sensor.getConfig();

        BluetoothGattService serv = mBtGatt.getService(servUuid);
        BluetoothGattCharacteristic charac = serv.getCharacteristic(confUuid);
        byte value = enable ? sensor.getEnableSensorCode() : Sensor.DISABLE_SENSOR_CODE;
        mBtLeService.writeCharacteristic(charac, value);
        mBtLeService.waitIdle(GATT_TIMEOUT);

    }

    private void enableNotifications(boolean enable) {
        Sensor sensor = Sensor.ACCELEROMETER;
        UUID servUuid = sensor.getService();
        UUID dataUuid = sensor.getData();
        BluetoothGattService serv = mBtGatt.getService(servUuid);
        BluetoothGattCharacteristic charac = serv.getCharacteristic(dataUuid);

        mBtLeService.setCharacteristicNotification(charac, enable);
        mBtLeService.waitIdle(GATT_TIMEOUT);

    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("geobio", "tip ** broadcasts receiver received!");
            final String action = intent.getAction();
            int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);

            if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
                // Notification
                byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);

                if (uuidStr.substring(4, 7).toLowerCase().equals("aa4")) {
                    int baroValue = (int) (Sensor.BAROMETER.convert(value).x / 100); // approx 1000
                    //                    if (supposedToBeEnabledSensors.contains("aa4")) {
                    //                        // then need to remove
                    //                        if (baroValue > 100) {
                    //                            supposedToBeEnabledSensors.remove(uuidStr.substring(4, 7));
                    //                        }
                    //                    }

                }
            }

        }
    };

}
