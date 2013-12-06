/**************************************************************************************************
  Filename:       FwUpdateActivity.java
  Revised:        $Date: 2013-09-05 05:55:20 +0200 (to, 05 sep 2013) $
  Revision:       $Revision: 27614 $

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ti.android.ble.common.BluetoothLeService;
import ti.android.util.Conversion;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FwUpdateActivity extends Activity {
  public final static String EXTRA_MESSAGE = "ti.android.ble.sensortag.MESSAGE";
  // Log
  private static String TAG = "FwUpdateActivity";

  // Activity
  private static final int FILE_ACTIVITY_REQ = 0;

  // Programming parameters
  private static final short OAD_CONN_INTERVAL = 10; // 12.5 msec
  private static final short OAD_SUPERVISION_TIMEOUT = 100; // 1 second
  private static final int PKT_INTERVAL = 20; // Milliseconds
  private static final int GATT_WRITE_TIMEOUT = 100; // Milliseconds

  private static final int FILE_BUFFER_SIZE = 0x40000;
  private static final String FW_CUSTOM_DIRECTORY = Environment.DIRECTORY_DOWNLOADS;
  private static final String FW_FILE_A = "SensorTagImgA.bin";
  private static final String FW_FILE_B = "SensorTagImgB.bin";

  private static final int OAD_BLOCK_SIZE = 16;
  private static final int HAL_FLASH_WORD_SIZE = 4;
  private static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;
  private static final int OAD_IMG_HDR_SIZE = 8;

  // GUI
  private TextView mTargImage;
  private TextView mFileImage;
  private TextView mProgressInfo;
  private TextView mLog;
  private ProgressBar mProgressBar;
  private Button mBtnLoadA;
  private Button mBtnLoadB;
  private Button mBtnLoadC;
  private Button mBtnStart;

  // BLE
  private BluetoothGattService mOadService;
  private BluetoothGattService mConnControlService;
  private List<BluetoothGattCharacteristic> mCharListOad;
  private List<BluetoothGattCharacteristic> mCharListCc;
  private BluetoothGattCharacteristic mCharIdentify = null;
  private BluetoothGattCharacteristic mCharBlock = null;
  private BluetoothGattCharacteristic mCharConnReq = null;
  private DeviceActivity mDeviceActivity = null;
  private BluetoothLeService mLeService;

  // Programming
  private final byte[] mFileBuffer = new byte[FILE_BUFFER_SIZE];
  private final byte[] mOadBuffer = new byte[OAD_BUFFER_SIZE];
  private ImgHdr mFileImgHdr = new ImgHdr();
  private ImgHdr mTargImgHdr = new ImgHdr();
  private Timer mTimer = null;
  private ProgInfo mProgInfo = new ProgInfo();
  private TimerTask mTimerTask = null;

  // Housekeeping
  private boolean mServiceOk = false;
  private boolean mProgramming = false;
  private int mEstDuration = 0;
  private IntentFilter mIntentFilter;

  public FwUpdateActivity() {
    Log.d(TAG, "construct");
    mDeviceActivity = DeviceActivity.getInstance();

    // BLE Gatt Service
    mLeService = BluetoothLeService.getInstance();

    // Service information
    mOadService = mDeviceActivity.getOadService();
    mConnControlService = mDeviceActivity.getConnControlService();

    // Characteristics list
    mCharListOad = mOadService.getCharacteristics();
    mCharListCc = mConnControlService.getCharacteristics();

    mServiceOk = mCharListOad.size() == 2 && mCharListCc.size() >= 3;
    if (mServiceOk) {
      mCharIdentify = mCharListOad.get(0);
      mCharBlock = mCharListOad.get(1);
      mCharConnReq = mCharListCc.get(1);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_fwupdate);

    // Icon padding
    ImageView view = (ImageView) findViewById(android.R.id.home);
    view.setPadding(10, 0, 20, 10);

    // Context title
    setTitle(R.string.title_oad);

    // Initialize widgets
    mProgressInfo = (TextView) findViewById(R.id.tw_info);
    mTargImage = (TextView) findViewById(R.id.tw_target);
    mFileImage = (TextView) findViewById(R.id.tw_file);
    mLog = (TextView) findViewById(R.id.tw_log);
    mProgressBar = (ProgressBar) findViewById(R.id.pb_progress);
    mBtnStart = (Button) findViewById(R.id.btn_start);
    mBtnStart.setEnabled(false);
    mBtnLoadA = (Button) findViewById(R.id.btn_load_a);
    mBtnLoadB = (Button) findViewById(R.id.btn_load_b);
    mBtnLoadC = (Button) findViewById(R.id.btn_load_c);

    // Sanity check
    mBtnLoadA.setEnabled(mServiceOk);
    mBtnLoadB.setEnabled(mServiceOk);
    mBtnLoadC.setEnabled(mServiceOk);
  	initIntentFilter();
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy");
    super.onDestroy();
    if (mTimerTask != null)
      mTimerTask.cancel();
    mTimer = null;
  }

  @Override
  public void onBackPressed() {
    Log.d(TAG, "onBackPressed");
    if (mProgramming) {
      Toast.makeText(this, R.string.prog_ogoing, Toast.LENGTH_LONG).show();
    } else
      super.onBackPressed();
  }
  
  @Override 
  protected void onResume()
  {
  	Log.d(TAG,"onResume");
    super.onResume();
    if (mServiceOk) {
    	registerReceiver(mGattUpdateReceiver, mIntentFilter);
    	
      // Read target image info
      getTargetImageInfo();

      // Change connection parameters for OAD
      setConnectionParameters();
    } else {
      Toast.makeText(this, "OAD service initialisation failed", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onPause() {
  	Log.d(TAG,"onPause");
  	super.onPause();
  	unregisterReceiver(mGattUpdateReceiver);
  }

  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			final String action = intent.getAction();
	    Log.d(TAG, "action: " + action);

	    if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
				byte [] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
		    if (uuidStr.equals(mCharIdentify.getUuid().toString())) {
		    	// Image info notification
		      mTargImgHdr.ver = Conversion.buildUint16(value[1], value[0]);
		      mTargImgHdr.imgType = ((mTargImgHdr.ver & 1) == 1) ? 'B' : 'A';
		      mTargImgHdr.len = Conversion.buildUint16(value[3], value[2]);
		      displayImageInfo(mTargImage, mTargImgHdr);
		    }
			} else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,BluetoothGatt.GATT_SUCCESS);
				if (status != BluetoothGatt.GATT_SUCCESS) {
					Log.e(TAG,"Write failed: " + status);
					Toast.makeText(context, "GATT error: status=" + status, Toast.LENGTH_SHORT).show();
				}
			}
		}
	};


  private void initIntentFilter() {
  	mIntentFilter = new IntentFilter();
  	mIntentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
  	mIntentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
  }

  public void onStart(View v) {
    if (mProgramming) {
      stopProgramming();
    } else {
      startProgramming();
    }
  }

  public void onLoad(View v) {
    if (v.getId() == R.id.btn_load_a)
      loadFile(FW_FILE_A, true);
    else
      loadFile(FW_FILE_B, true);
    updateGui();
  }

  public void onLoadCustom(View v) {
    Intent i = new Intent(this, FileActivity.class);
    i.putExtra(EXTRA_MESSAGE, FW_CUSTOM_DIRECTORY);
    startActivityForResult(i, FILE_ACTIVITY_REQ);
  }

  private void startProgramming() {
    mLog.append("Programming started\n");
    mProgramming = true;
    updateGui();

    // Prepare image notification
    byte[] buf = new byte[OAD_IMG_HDR_SIZE + 2 + 2];
    buf[0] = Conversion.loUint16(mFileImgHdr.ver);
    buf[1] = Conversion.hiUint16(mFileImgHdr.ver);
    buf[2] = Conversion.loUint16(mFileImgHdr.len);
    buf[3] = Conversion.hiUint16(mFileImgHdr.len);
    System.arraycopy(mFileImgHdr.uid, 0, buf, 4, 4);

    // Send image notification
    mCharIdentify.setValue(buf);
    mLeService.writeCharacteristic(mCharIdentify);

    // Initialize stats
    mProgInfo.reset();

    // Start the packet timer
    mTimer = null;
    mTimer = new Timer();
    mTimerTask = new ProgTimerTask();
    mTimer.scheduleAtFixedRate(mTimerTask, 0, PKT_INTERVAL);
  }

  private void stopProgramming() {
    mTimer.cancel();
    mTimer.purge();
    mTimerTask.cancel();
    mTimerTask = null;

    mProgramming = false;
    mProgressInfo.setText("");
    mProgressBar.setProgress(0);
    updateGui();

    if (mProgInfo.iBlocks == mProgInfo.nBlocks) {
      mLog.setText("Programming complete!\n");
    } else {
      mLog.append("Programming cancelled\n");
    }
  }

  private void updateGui() {
  	if (mProgramming) {
  		// Busy: stop label, progress bar, disabled file selector
  		mBtnStart.setText(R.string.cancel);
  		mBtnLoadA.setEnabled(false);
  		mBtnLoadB.setEnabled(false);
  	} else {
  		// Idle: program label, enable file selector
  		mProgressBar.setProgress(0);
  		mBtnStart.setText(R.string.start_prog);
  		if (mFileImgHdr.imgType == 'A') {
  			mBtnLoadA.setEnabled(false);
  			mBtnLoadB.setEnabled(true);
  		} else if (mFileImgHdr.imgType == 'B') {
  			mBtnLoadA.setEnabled(true);
  			mBtnLoadB.setEnabled(false);
  		}
  	}
  }

  private boolean loadFile(String filepath, boolean isAsset) {
    boolean fSuccess = false;

    // Load binary file
    try {
      // Read the file raw into a buffer
      InputStream stream;
      if (isAsset) {
        stream = getAssets().open(filepath);
      } else {
        File f = new File(filepath);
        stream = new FileInputStream(f);
      }
      stream.read(mFileBuffer, 0, mFileBuffer.length);
      stream.close();
    } catch (IOException e) {
      // Handle exceptions here
      mLog.setText("File open failed: " + filepath + "\n");
      return false;
    }

    // Show image info
    mFileImgHdr.ver = Conversion.buildUint16(mFileBuffer[5], mFileBuffer[4]);
    mFileImgHdr.len = Conversion.buildUint16(mFileBuffer[7], mFileBuffer[6]);
    mFileImgHdr.imgType = ((mFileImgHdr.ver & 1) == 1) ? 'B' : 'A';
    System.arraycopy(mFileBuffer, 8, mFileImgHdr.uid, 0, 4);
    displayImageInfo(mFileImage, mFileImgHdr);

    // Verify image types
    boolean ready = mFileImgHdr.imgType != mTargImgHdr.imgType;
    int resid = ready ? R.style.dataStyle1 : R.style.dataStyle2;
    mFileImage.setTextAppearance(this, resid);

    // Enable programming button only if image types differ
    mBtnStart.setEnabled(ready);

    // Expected duration
    mEstDuration = ((PKT_INTERVAL * mFileImgHdr.len * 4) / OAD_BLOCK_SIZE) / 1000;
    displayStats();

    // Log
    mLog.setText("Image " + mFileImgHdr.imgType + " selected.\n");
    mLog.append(ready ? "Ready to program device!\n" : "Incompatible image, select alternative!\n");

    updateGui();

    return fSuccess;
  }

  private void displayImageInfo(TextView v, ImgHdr h) {
    int imgVer = (h.ver) >> 1;
    int imgSize = h.len * 4;
    String s = String.format("Type: %c Ver.: %d Size: %d", h.imgType, imgVer, imgSize);
    v.setText(Html.fromHtml(s));
  }

  private void displayStats() {
    String txt;
    int byteRate;
    int sec = mProgInfo.iTimeElapsed / 1000;
    if (sec > 0) {
      byteRate = mProgInfo.iBytes / sec;
    } else {
      byteRate = 0;
    }

    txt = String.format("Time: %d / %d sec", sec, mEstDuration);
    txt += String.format("    Bytes: %d (%d/sec)", mProgInfo.iBytes, byteRate);
    mProgressInfo.setText(txt);
  }

  private void getTargetImageInfo() {
    // Enable notification
    boolean ok = enableNotification(mCharIdentify, true);
    // Prepare data for request (try image A and B respectively, only one of
    // them will give a notification with the image info)
    if (ok)
      ok = writeCharacteristic(mCharIdentify, (byte) 0);
    if (ok)
      ok = writeCharacteristic(mCharIdentify, (byte) 1);
    if (!ok)
      Toast.makeText(this, "Failed to get target info", Toast.LENGTH_LONG).show();
  }

  private boolean writeCharacteristic(BluetoothGattCharacteristic c, byte v) {
    boolean ok = mLeService.writeCharacteristic(c, v);
    if (ok)
      ok = mLeService.waitIdle(GATT_WRITE_TIMEOUT);
    return ok;
  }

  private boolean enableNotification(BluetoothGattCharacteristic c, boolean enable) {
    boolean ok = mLeService.setCharacteristicNotification(c, enable);
    if (ok)
      ok = mLeService.waitIdle(GATT_WRITE_TIMEOUT);
    return ok;
  }

  private void setConnectionParameters() {
    // Make sure connection interval is long enough for OAD
    byte[] value = { Conversion.loUint16(OAD_CONN_INTERVAL), Conversion.hiUint16(OAD_CONN_INTERVAL), Conversion.loUint16(OAD_CONN_INTERVAL),
        Conversion.hiUint16(OAD_CONN_INTERVAL), 0, 0, Conversion.loUint16(OAD_SUPERVISION_TIMEOUT), Conversion.hiUint16(OAD_SUPERVISION_TIMEOUT) };
    mCharConnReq.setValue(value);
    boolean ok = mLeService.writeCharacteristic(mCharConnReq);
    if (ok)
      ok = mLeService.waitIdle(GATT_WRITE_TIMEOUT);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Check which request we're responding to
    if (requestCode == FILE_ACTIVITY_REQ) {
      // Make sure the request was successful
      if (resultCode == RESULT_OK) {
        String filename = data.getStringExtra(FileActivity.EXTRA_FILENAME);
        Log.d(TAG, "return: " + filename);
        loadFile(filename, false);
      }
    }
  }

  /*
   * Called when a notification with the current image info has been received
   */

  private void onBlockTimer() {

    if (mProgInfo.iBlocks < mProgInfo.nBlocks) {
      mProgramming = true;

      // Prepare block
      mOadBuffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
      mOadBuffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
      System.arraycopy(mFileBuffer, mProgInfo.iBytes, mOadBuffer, 2, OAD_BLOCK_SIZE);

      // Send block
      mCharBlock.setValue(mOadBuffer);
      boolean success = mLeService.writeCharacteristic(mCharBlock);

      if (success) {
        // Update stats
        mProgInfo.iBlocks++;
        mProgInfo.iBytes += OAD_BLOCK_SIZE;
        mProgressBar.setProgress((mProgInfo.iBlocks * 100) / mProgInfo.nBlocks);
      } else {
        // Check if the device has been prematurely disconnected
        if (BluetoothLeService.getBtGatt() == null)
          mProgramming = false;
      }
    } else {
      mProgramming = false;
    }
    mProgInfo.iTimeElapsed += PKT_INTERVAL;

    if (!mProgramming) {
      runOnUiThread(new Runnable() {
        public void run() {
          displayStats();
          stopProgramming();
        }
      });
    }
  }

  private class ProgTimerTask extends TimerTask {
    @Override
    public void run() {
      mProgInfo.mTick++;
      if (mProgramming) {
        onBlockTimer();
        if ((mProgInfo.mTick % PKT_INTERVAL) == 0) {
          runOnUiThread(new Runnable() {
            public void run() {
              displayStats();
            }
          });
        }
      }
    }
  }

  private class ImgHdr {
    short ver;
    short len;
    Character imgType;
    byte[] uid = new byte[4];
  }

  private class ProgInfo {
    int iBytes = 0; // Number of bytes programmed
    short iBlocks = 0; // Number of blocks programmed
    short nBlocks = 0; // Total number of blocks
    int iTimeElapsed = 0; // Time elapsed in milliseconds
    int mTick = 0;

    void reset() {
      iBytes = 0;
      iBlocks = 0;
      iTimeElapsed = 0;
      mTick = 0;
      nBlocks = (short) (mFileImgHdr.len / (OAD_BLOCK_SIZE / HAL_FLASH_WORD_SIZE));
    }
  }

}
