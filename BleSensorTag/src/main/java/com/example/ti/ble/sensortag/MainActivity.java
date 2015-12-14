/**************************************************************************************************
  Filename:       MainActivity.java
  Revised:        $Date: Wed Apr 22 13:01:34 2015 +0200$
  Revision:       $Revision: 599e5650a33a4a142d060c959561f9e9b0d88146$

  Copyright (c) 2013 - 2014 Texas Instruments Incorporated

  All rights reserved not granted herein.
  Limited License. 

  Texas Instruments Incorporated grants a world-wide, royalty-free,
  non-exclusive license under copyrights and patents it now or hereafter
  owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
  this software subject to the terms herein.  With respect to the foregoing patent
  license, such license is granted  solely to the extent that any such patent is necessary
  to Utilize the software alone.  The patent license shall not apply to any combinations which
  include this software, other than combinations with devices manufactured by or for TI ('TI Devices').
  No hardware patent is licensed hereunder.

  Redistributions must preserve existing copyright notices and reproduce this license (including the
  above copyright notice and the disclaimer and (if applicable) source code license limitations below)
  in the documentation and/or other materials provided with the distribution

  Redistribution and use in binary form, without modification, are permitted provided that the following
  conditions are met:

    * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
      software provided in binary form.
    * any redistribution and use are licensed by TI for use only with TI Devices.
    * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.

  If software source code is provided to you, modification and redistribution of the source code are permitted
  provided that the following conditions are met:

    * any redistribution and use of the source code, including any resulting derivative works, are licensed by
      TI for use only with TI Devices.
    * any redistribution and use of any object code compiled from the source code and any resulting derivative
      works, are licensed by TI for use only with TI Devices.

  Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
  promote products derived from this software without specific prior written permission.

  DISCLAIMER.

  THIS SOFTWARE IS PROVIDED BY TI AND TI'S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL TI AND TI'S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.


 **************************************************************************************************/
package com.example.ti.ble.sensortag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
// import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.ti.ble.common.BleDeviceInfo;
import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.ble.common.HCIDefines;
import com.example.ti.ble.common.HelpView;
import com.example.ti.util.CustomToast;

public class MainActivity extends ViewPagerActivity {
	// Log
	// private static final String TAG = "MainActivity";

	// URLs
	private static final Uri URL_FORUM = Uri
	    .parse("http://e2e.ti.com/support/low_power_rf/default.aspx?DCMP=hpa_hpa_community&HQS=NotApplicable+OT+lprf-forum");
	private static final Uri URL_STHOME = Uri
	    .parse("http://www.ti.com/ww/en/wireless_connectivity/sensortag/index.shtml?INTC=SensorTagGatt&HQS=sensortag");

	// Requests to other activities
	private static final int REQ_ENABLE_BT = 0;
	private static final int REQ_DEVICE_ACT = 1;

	// GUI
	private static MainActivity mThis = null;
	private ScanView mScanView;
	private Intent mDeviceIntent;
	private static final int STATUS_DURATION = 5;

	// BLE management
	private boolean mBtAdapterEnabled = false;
	private boolean mBleSupported = true;
	private boolean mScanning = false;
	private int mNumDevs = 0;
	private int mConnIndex = NO_DEVICE;
	private List<BleDeviceInfo> mDeviceInfoList;
	private static BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBtAdapter = null;
	private BluetoothDevice mBluetoothDevice = null;
	private BluetoothLeService mBluetoothLeService = null;
	private IntentFilter mFilter;
	private String[] mDeviceFilter = null;

	// Housekeeping
	private static final int NO_DEVICE = -1;
	private boolean mInitialised = false;
	SharedPreferences prefs = null;

	public MainActivity() {
		mThis = this;
		mResourceFragmentPager = R.layout.fragment_pager;
		mResourceIdPager = R.id.pager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Start the application
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);



		// Initialize device list container and device filter
		mDeviceInfoList = new ArrayList<BleDeviceInfo>();
		Resources res = getResources();
		mDeviceFilter = res.getStringArray(R.array.device_filter);

		// Create the fragments and add them to the view pager and tabs
		mScanView = new ScanView();
		mSectionsPagerAdapter.addSection(mScanView, "BLE Device List");
		
		HelpView hw = new HelpView();
		hw.setParameters("help_scan.html", R.layout.fragment_help, R.id.webpage);
		mSectionsPagerAdapter.addSection(hw, "Help");

		// Register the BroadcastReceiver
		mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
    }



	@Override
	public void onDestroy() {
		// Log.e(TAG,"onDestroy");
		super.onDestroy();

		
		mBtAdapter = null;
		
		// Clear cache
		File cache = getCacheDir();
		String path = cache.getPath();
    try {
	    Runtime.getRuntime().exec(String.format("rm -rf %s", path));
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.opt_bt:
			onBluetooth();
			break;
		case R.id.opt_e2e:
			onUrl(URL_FORUM);
			break;
		case R.id.opt_sthome:
			onUrl(URL_STHOME);
			break;
		case R.id.opt_license:
			onLicense();
			break;
		case R.id.opt_about:
			onAbout();
			break;
		case R.id.opt_exit:
			Toast.makeText(this, "Exit...", Toast.LENGTH_SHORT).show();
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.optionsMenu = menu;
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void onUrl(final Uri uri) {
		Intent web = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(web);
	}

	private void onBluetooth() {
		Intent settingsIntent = new Intent(
		    android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startActivity(settingsIntent);
	}

	private void onLicense() {
		final Dialog dialog = new LicenseDialog(this);
		dialog.show();
	}

	private void onAbout() {
		final Dialog dialog = new AboutDialog(this);
		dialog.show();
	}

	void onScanViewReady(View view) {


		// License popup on first run
		if (prefs.getBoolean("firstrun", true)) {
			onLicense();
			prefs.edit().putBoolean("firstrun", false).commit();
		}

		if (!mInitialised) {
			// Broadcast receiver
            mBluetoothLeService = BluetoothLeService.getInstance();
            mBluetoothManager = mBluetoothLeService.getBtManager();
            mBtAdapter = mBluetoothManager.getAdapter();
			registerReceiver(mReceiver, mFilter);
			mBtAdapterEnabled = mBtAdapter.isEnabled();
			if (mBtAdapterEnabled) {
				// Start straight away
				//startBluetoothLeService();
			} else {
				// Request BT adapter to be turned on
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQ_ENABLE_BT);
			}
			mInitialised = true;
		} else {
			mScanView.notifyDataSetChanged();
		}
        // Initial state of widgets
        updateGuiState();
	}

	public void onBtnScan(View view) {
		if (mScanning) {
			stopScan();
		} else {
			startScan();
		}
	}

	void onConnect() {
		if (mNumDevs > 0) {

			int connState = mBluetoothManager.getConnectionState(mBluetoothDevice,
			    BluetoothGatt.GATT);

			switch (connState) {
			case BluetoothGatt.STATE_CONNECTED:
				mBluetoothLeService.disconnect(null);
				break;
			case BluetoothGatt.STATE_DISCONNECTED:
				boolean ok = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
				if (!ok) {
					setError("Connect failed");
				}
				break;
			default:
				setError("Device busy (connecting/disconnecting)");
				break;
			}
		}
	}

	private void startScan() {
		// Start device discovery
		if (mBleSupported) {
			mNumDevs = 0;
			mDeviceInfoList.clear();
			mScanView.notifyDataSetChanged();
			scanLeDevice(true);
			mScanView.updateGui(mScanning);
			if (!mScanning) {
				setError("Device discovery start failed");
				setBusy(false);
			}
		} else {
			setError("BLE not supported on this device");
		}

	}

	private void stopScan() {
		mScanning = false;
		mScanView.updateGui(false);
		scanLeDevice(false);
	}

	private void startDeviceActivity() {
		mDeviceIntent = new Intent(this, DeviceActivity.class);
		mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
		startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
	}

	private void stopDeviceActivity() {
		finishActivity(REQ_DEVICE_ACT);
	}

	public void onDeviceClick(final int pos) {

		if (mScanning)
			stopScan();

		setBusy(true);
		mBluetoothDevice = mDeviceInfoList.get(pos).getBluetoothDevice();
		if (mConnIndex == NO_DEVICE) {
			mScanView.setStatus("Connecting");
			mConnIndex = pos;
			onConnect();
		} else {
			mScanView.setStatus("Disconnecting");
			if (mConnIndex != NO_DEVICE) {
				mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
			}
		}
	}

	public void onScanTimeout() {
		runOnUiThread(new Runnable() {
			public void run() {
				stopScan();
			}
		});
	}

	public void onConnectTimeout() {
		runOnUiThread(new Runnable() {
			public void run() {
				setError("Connection timed out");
			}
		});
		if (mConnIndex != NO_DEVICE) {
			mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
			mConnIndex = NO_DEVICE;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// GUI methods
	//
	public void updateGuiState() {
		boolean mBtEnabled = mBtAdapter.isEnabled();

		if (mBtEnabled) {
			if (mScanning) {
				// BLE Host connected
				if (mConnIndex != NO_DEVICE) {
					String txt = mBluetoothDevice.getName() + " connected";
					mScanView.setStatus(txt);
				} else {
					mScanView.setStatus(mNumDevs + " devices");
				}
			}
		} else {
			mDeviceInfoList.clear();
			mScanView.notifyDataSetChanged();
		}
	}

	private void setBusy(boolean f) {
		mScanView.setBusy(f);
	}

	void setError(String txt) {
		mScanView.setError(txt);
		//CustomToast.middleBottom(this, "Turning BT adapter off and on again may fix Android BLE stack problems");
	}

	private BleDeviceInfo createDeviceInfo(BluetoothDevice device, int rssi) {
		BleDeviceInfo deviceInfo = new BleDeviceInfo(device, rssi);

		return deviceInfo;
	}

	boolean checkDeviceFilter(String deviceName) {
		if (deviceName == null)
			return false;

		int n = mDeviceFilter.length;
		if (n > 0) {
			boolean found = false;
			for (int i = 0; i < n && !found; i++) {
				found = deviceName.equals(mDeviceFilter[i]);
			}
			return found;
		} else
			// Allow all devices if the device filter is empty
			return true;
	}

	private void addDevice(BleDeviceInfo device) {
		mNumDevs++;
		mDeviceInfoList.add(device);
		mScanView.notifyDataSetChanged();
		if (mNumDevs > 1)
			mScanView.setStatus(mNumDevs + " devices");
		else
			mScanView.setStatus("1 device");
	}

	private boolean deviceInfoExists(String address) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
			    .equals(address)) {
				return true;
			}
		}
		return false;
	}

	private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
			    .equals(device.getAddress())) {
				return mDeviceInfoList.get(i);
			}
		}
		return null;
	}

	private boolean scanLeDevice(boolean enable) {
		if (enable) {
			mScanning = mBtAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBtAdapter.stopLeScan(mLeScanCallback);
		}
		return mScanning;
	}

	List<BleDeviceInfo> getDeviceInfoList() {
		return mDeviceInfoList;
	}



	// Activity result handling
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQ_DEVICE_ACT:
			// When the device activity has finished: disconnect the device
			if (mConnIndex != NO_DEVICE) {
				mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
			}
			break;

		case REQ_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {

				Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
			} else {
				// User did not enable Bluetooth or an error occurred
				Toast.makeText(this, R.string.bt_not_on, Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		default:
			CustomToast.middleBottom(this, "Unknown request code: " + requestCode);

			// Log.e(TAG, "Unknown request code");
			break;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasted actions from Bluetooth adapter and BluetoothLeService
	//
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				// Bluetooth adapter state change
				switch (mBtAdapter.getState()) {
				case BluetoothAdapter.STATE_ON:
					mConnIndex = NO_DEVICE;
					//startBluetoothLeService();
					break;
				case BluetoothAdapter.STATE_OFF:
					Toast.makeText(context, R.string.app_closing, Toast.LENGTH_LONG)
					    .show();
					finish();
					break;
				default:
					// Log.w(TAG, "Action STATE CHANGED not processed ");
					break;
				}

				updateGuiState();
			} else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				// GATT connect
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
				    BluetoothGatt.GATT_FAILURE);
				if (status == BluetoothGatt.GATT_SUCCESS) {
					setBusy(false);
					startDeviceActivity();
				} else
					setError("Connect failed. Status: " + status);
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				// GATT disconnect
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
				    BluetoothGatt.GATT_FAILURE);
				stopDeviceActivity();
				if (status == BluetoothGatt.GATT_SUCCESS) {
					setBusy(false);
					mScanView.setStatus(mBluetoothDevice.getName() + " disconnected",
					    STATUS_DURATION);
				} else {
					setError("Disconnect Status: " + HCIDefines.hciErrorCodeStrings.get(status));
				}
				mConnIndex = NO_DEVICE;
				mBluetoothLeService.close();
			} else {
				// Log.w(TAG,"Unknown action: " + action);
			}

		}
	};


	// Device scan callback.
	// NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		public void onLeScan(final BluetoothDevice device, final int rssi,
		    byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				public void run() {
					// Filter devices
					if (checkDeviceFilter(device.getName())) {
						if (!deviceInfoExists(device.getAddress())) {
							// New device
							BleDeviceInfo deviceInfo = createDeviceInfo(device, rssi);
							addDevice(deviceInfo);
						} else {
							// Already in list, update RSSI info
							BleDeviceInfo deviceInfo = findDeviceInfo(device);
							deviceInfo.updateRssi(rssi);
							mScanView.notifyDataSetChanged();
						}
					}
				}

			});
		}
	};

}
