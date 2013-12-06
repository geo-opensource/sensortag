/**************************************************************************************************
  Filename:       ScanView.java
  Revised:        $Date: 2013-08-30 12:02:37 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27470 $

  Copyright 2013 Texas Instruments Incorporated. All rights reserved.
 
  IMPORTANT: Your use of this Software is limited to those specific rights
  granted under the terms of a software license agreement between the user
  who downloaded the software, his/her employer (which must be your employer)
  and Texas Instruments Incorporated (the "License").  You may not use this
  Software unless you agree to abide by the terms of the License. 
  The License limits your use, and you acknowledge, that the Software may not be 
  modified, copied or distributed unless used solely and exclusively in conjunction 
  with a Texas Instruments Bluetooth device. Other than for the foregoing purpose, 
  you may not use, reproduce, copy, prepare derivative works of, modify, distribute, 
  perform, display or sell this Software and/or its documentation for any purpose.
 
  YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
  PROVIDED “AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
  INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
  NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
  TEXAS INSTRUMENTS OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT,
  NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER
  LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
  INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
  OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
  OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
  (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 
  Should you have any questions regarding your right to use this Software,
  contact Texas Instruments Incorporated at www.TI.com

 **************************************************************************************************/
package ti.android.ble.sensortag;

import java.util.List;

import ti.android.ble.common.BleDeviceInfo;
import ti.android.util.CustomTimer;
import ti.android.util.CustomTimerCallback;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScanView extends Fragment {
  private static final String TAG = "ScanView";
  private final int SCAN_TIMEOUT = 10; // Seconds
  private final int CONNECT_TIMEOUT = 10; // Seconds
  private MainActivity mActivity = null;

  private DeviceListAdapter mDeviceAdapter = null;
  private TextView mEmptyMsg;
  private TextView mStatus;
  private Button mBtnScan = null;
  private ListView mDeviceListView = null;
  private ProgressBar mProgressBar;

  private CustomTimer mScanTimer = null;
  private CustomTimer mConnectTimer = null;
  @SuppressWarnings("unused")
  private CustomTimer mStatusTimer;
  private Context mContext;
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.i(TAG, "onCreateView");

    // The last two arguments ensure LayoutParams are inflated properly.
    View view = inflater.inflate(R.layout.fragment_scan, container, false);

    mActivity = (MainActivity) getActivity();
    mContext = mActivity.getApplicationContext();

    // Initialize widgets
    mStatus = (TextView) view.findViewById(R.id.status);
    mBtnScan = (Button) view.findViewById(R.id.btn_scan);
    mDeviceListView = (ListView) view.findViewById(R.id.device_list);
    mDeviceListView.setClickable(true);
    mDeviceListView.setOnItemClickListener(mDeviceClickListener);
    mEmptyMsg = (TextView)view.findViewById(R.id.no_device);
    
    // Progress bar to use during scan and connection
    mProgressBar = (ProgressBar) view.findViewById(R.id.pb_busy);
    mProgressBar.setMax(SCAN_TIMEOUT);

    // Alert parent activity
    mActivity.onScanViewReady(view);

    return view;
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "onDestroy");
    super.onDestroy();
  }

  void setStatus(String txt) {
    mStatus.setText(txt);
    mStatus.setTextAppearance(mContext, R.style.statusStyle_Success);
  }

  void setStatus(String txt, int duration) {
    setStatus(txt);
    mStatusTimer = new CustomTimer(null, duration, mClearStatusCallback);
  }

  void setError(String txt) {
    setBusy(false);
    stopTimers();
    mStatus.setText(txt);
    mStatus.setTextAppearance(mContext, R.style.statusStyle_Failure);
  }

	void notifyDataSetChanged() {
		List<BleDeviceInfo> deviceList = mActivity.getDeviceInfoList();
		if (mDeviceAdapter == null) {
			mDeviceAdapter = new DeviceListAdapter(mActivity,deviceList);
		}
		mDeviceListView.setAdapter(mDeviceAdapter);
		mDeviceAdapter.notifyDataSetChanged();
		if (deviceList.size() > 0) {
			mEmptyMsg.setVisibility(View.GONE);
		} else {
			mEmptyMsg.setVisibility(View.VISIBLE);			
		}
	}

  void setBusy(boolean f) {
    if (mProgressBar == null)
      return;
    if (f) {
      mProgressBar.setVisibility(View.VISIBLE);
    } else {
      stopTimers();
      mProgressBar.setVisibility(View.GONE);
    }
  }

  void updateGui(boolean scanning) {
    if (mBtnScan == null)
      return; // UI not ready
    setBusy(scanning);

    if (scanning) {
      mScanTimer = new CustomTimer(mProgressBar, SCAN_TIMEOUT, mPgScanCallback);
      mStatus.setTextAppearance(mContext, R.style.statusStyle_Busy);
      mBtnScan.setText("Stop");
      mStatus.setText("Scanning...");
      mEmptyMsg.setText(R.string.nodevice);
      mActivity.updateGuiState();
    } else {
      // Indicate that scanning has stopped
      mStatus.setTextAppearance(mContext, R.style.statusStyle_Success);
      mBtnScan.setText("Scan");
      mEmptyMsg.setText(R.string.scan_advice);
      mActivity.setProgressBarIndeterminateVisibility(false);
      mDeviceAdapter.notifyDataSetChanged();
    }
  }

  // Listener for device list
  private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
    	Log.d(TAG,"item click");
      mConnectTimer = new CustomTimer(mProgressBar, CONNECT_TIMEOUT, mPgConnectCallback);
      mActivity.onDeviceClick(pos);
    }
  };

  // Listener for progress timer expiration
  private CustomTimerCallback mPgScanCallback = new CustomTimerCallback() {
    public void onTimeout() {
      mActivity.onScanTimeout();
    }

    public void onTick(int i) {
    }
  };

  // Listener for connect/disconnect expiration
  private CustomTimerCallback mPgConnectCallback = new CustomTimerCallback() {
    public void onTimeout() {
      mActivity.onConnectTimeout();
    }

    public void onTick(int i) {
    }
  };

  // Listener for connect/disconnect expiration
  private CustomTimerCallback mClearStatusCallback = new CustomTimerCallback() {
    public void onTimeout() {
      mActivity.runOnUiThread(new Runnable() {
        public void run() {
          setStatus("");
        }
      });
      mStatusTimer = null;
    }

    public void onTick(int i) {
    }
  };

  private void stopTimers() {
    if (mScanTimer != null) {
      mScanTimer.stop();
      mScanTimer = null;
    }
    if (mConnectTimer != null) {
      mConnectTimer.stop();
      mConnectTimer = null;
    }
  }

  //
  // CLASS DeviceAdapter: handle device list
  //
  class DeviceListAdapter extends BaseAdapter {
    private List<BleDeviceInfo> mDevices;
    private LayoutInflater mInflater;

    public DeviceListAdapter(Context context, List<BleDeviceInfo> devices) {
      mInflater = LayoutInflater.from(context);
      mDevices = devices;
    }

    public int getCount() {
      return mDevices.size();
    }

    public Object getItem(int position) {
      return mDevices.get(position);
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ViewGroup vg;

      if (convertView != null) {
        vg = (ViewGroup) convertView;
      } else {
        vg = (ViewGroup) mInflater.inflate(R.layout.element_device, null);
      }

      BleDeviceInfo deviceInfo = mDevices.get(position);
      BluetoothDevice device = deviceInfo.getBluetoothDevice();
      int rssi = deviceInfo.getRssi();
      String descr = device.getName() + "\n" + device.getAddress() + "\nRssi: " + rssi + " dBm";
      ((TextView) vg.findViewById(R.id.descr)).setText(descr);

      return vg;
    }
  }

}
