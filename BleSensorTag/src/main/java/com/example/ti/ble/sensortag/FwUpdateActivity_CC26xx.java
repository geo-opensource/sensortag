/**************************************************************************************************
 Filename:       FwUpdateActivity_CC26xx.java
 Revised:        $Date: Wed Apr 22 13:01:34 2015 +0200$
 Revision:       $Revision: 599e5650a33a4a142d060c959561f9e9b0d88146$

 Copyright (c) 2013 - 2015 Texas Instruments Incorporated

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

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.util.Conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FwUpdateActivity_CC26xx extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.ti.ble.sensortag.MESSAGE";
    // Log
    private static String TAG = "FwUpdateActivity";

    // Activity
    public static final int FILE_ACTIVITY_REQ = 0;

    // Programming parameters
    private static final short OAD_CONN_INTERVAL = 6; // 15 milliseconds
    private static final short OAD_SUPERVISION_TIMEOUT = 50; // 500 milliseconds
    private static final int GATT_WRITE_TIMEOUT = 300; // Milliseconds

    private static final int FILE_BUFFER_SIZE = 0x40000;
    public static final String FW_CUSTOM_DIRECTORY = Environment.DIRECTORY_DOWNLOADS;
    private static final String FW_FILE_0_91 = "CC2650SensorTag_BLE_All_v0.91.bin";
    private static final String FW_FILE_1_01 = "CC2650SensorTag_BLE_All_v1.01.bin";

    private static final int OAD_BLOCK_SIZE = 16;
    private static final int HAL_FLASH_WORD_SIZE = 4;
    private static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;
    private static final int OAD_IMG_HDR_SIZE = 8;
    private static final long TIMER_INTERVAL = 1000;

    private static final int SEND_INTERVAL = 20; // Milliseconds (make sure this is longer than the connection interval)
    private static final int BLOCKS_PER_CONNECTION = 20; // May sent up to four blocks per connection

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
    private ImgHdr mFileImgHdr;
    private ImgHdr mTargImgHdr;
    private Timer mTimer = null;
    private ProgInfo mProgInfo = new ProgInfo();
    private TimerTask mTimerTask = null;
    private float firmwareRevision;
    private boolean slowAlgo = true;
    private int fastAlgoMaxPackets = BLOCKS_PER_CONNECTION;
    private String internalFWFilename;
    private int packetsSent = 0;

    // Housekeeping
    private boolean mServiceOk = false;
    private boolean mProgramming = false;
    private IntentFilter mIntentFilter;


    public FwUpdateActivity_CC26xx() {
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
            mCharBlock.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mCharConnReq = mCharListCc.get(1);
        }
        String fwString = mDeviceActivity.firmwareRevision();
        String revNum = fwString.substring(0,fwString.indexOf(" "));
        firmwareRevision = Float.parseFloat(revNum);
        if (firmwareRevision < 0.91) {
            internalFWFilename = FW_FILE_0_91;
            slowAlgo = true;
        }
        else if (firmwareRevision < 1.00) {
            internalFWFilename = FW_FILE_1_01;
            slowAlgo = true;
        }
        else {
            internalFWFilename = FW_FILE_1_01;
            slowAlgo = true;
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
        mLog.setMovementMethod(new ScrollingMovementMethod());
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

        // CC26xx setup is different than CC254x
        mBtnLoadB.setVisibility(View.INVISIBLE);
        mBtnLoadA.setText("Factory");
        initIntentFilter();
        mTargImage.setText(mDeviceActivity.firmwareRevision());
        mFileImage.setText("1.01 (Mar 13 2015)");

        if (firmwareRevision < 0.91) {
            mFileImage.setText("0.91 (Feb 13 2015)");

            AlertDialog.Builder b = new AlertDialog.Builder(this);

            b.setMessage(R.string.oad_dialog_old_fw_0_89_cc26xx);
            b.setTitle("Notice");
            b.setPositiveButton("OK",null);

            AlertDialog d = b.create();
            d.show();

        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mTimerTask != null)
            mTimerTask.cancel();
        mTimer = null;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mServiceOk) {
            registerReceiver(mGattUpdateReceiver, mIntentFilter);
            Log.d("FwUpdateActivity_CC26xx","Current firmware revision :" + firmwareRevision);
        } else {
            Toast.makeText(this, "OAD service initialisation failed", Toast.LENGTH_LONG).show();
        }
        mLeService.abortTimedDisconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
                byte [] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);

                if (uuidStr.equals(mCharIdentify.getUuid().toString())) {

                }
                if (uuidStr.equals(mCharBlock.getUuid().toString())) {
                    // Block check here :
                    String block = String.format("%02x%02x",value[1],value[0]);
                    Log.d("FwUpdateActivity_CC26xx :", "Received block req: " + block);
                    if (slowAlgo == true) {
                        programBlock();
                    }
                    else {
                        if (packetsSent != 0) packetsSent--;
                        if (packetsSent > 10) return;
                        while (packetsSent < fastAlgoMaxPackets) {
                            waitABit();
                            programBlock();
                        }
                    }
                }

            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,BluetoothGatt.GATT_SUCCESS);
                if (status != BluetoothGatt.GATT_SUCCESS) {
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
        mLeService.setCharacteristicNotification(mCharBlock, true);
        if ( Build.VERSION.SDK_INT >= 21) Log.d("FWUpdateActivity_CC26xx","Requested connection priority HIGH, result : " + this.mLeService.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH));
        setConnectionParameters();
        loadFile(internalFWFilename, true);
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
        packetsSent = 0;
        updateGui();

        mCharIdentify.setValue(mFileImgHdr.getRequest());
        mLeService.writeCharacteristic(mCharIdentify);

        // Initialize stats
        mProgInfo.reset();
        mTimer = new Timer();
        mTimerTask = new ProgTimerTask();
        mTimer.scheduleAtFixedRate(mTimerTask, 0, TIMER_INTERVAL);
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

        mLeService.setCharacteristicNotification(mCharBlock, false);
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
            mBtnLoadC.setEnabled(false);
        } else {
            // Idle: program label, enable file selector
            mProgressBar.setProgress(0);
            mBtnStart.setText(R.string.start_prog);
            mBtnLoadA.setEnabled(true);
            mBtnLoadC.setEnabled(true);
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

        if (!isAsset) {
            mFileImage.setText(filepath);
        }

        //Always enable button on CC26xx
        mBtnStart.setEnabled(true);

        mFileImgHdr = new ImgHdr(mFileBuffer);

        // Expected duration
        displayStats();

        // Log
        mLog.setText("Programming Image " + internalFWFilename + "\n");
        mLog.append("Ready to program device!\n");

        updateGui();

        return fSuccess;
    }

    private void displayStats() {
        String txt;
        int byteRate;
        int sec = mProgInfo.iTimeElapsed / 1000;
        if (sec > 0) {
            byteRate = mProgInfo.iBytes / sec;
        } else {
            byteRate = 0;
            return;
        }
        float timeEstimate;

        timeEstimate = ((float)(mFileImgHdr.len *4) / (float)mProgInfo.iBytes) * sec;

        txt = String.format("Time: %d / %d sec", sec, (int)timeEstimate);
        txt += String.format("    Bytes: %d (%d/sec)", mProgInfo.iBytes, byteRate);
        mProgressInfo.setText(txt);
    }

    private void setConnectionParameters() {
        // Make sure connection interval is long enough for OAD (Android default connection interval is 7.5 ms)
        byte[] value = { Conversion.loUint16(OAD_CONN_INTERVAL), Conversion.hiUint16(OAD_CONN_INTERVAL), Conversion.loUint16(OAD_CONN_INTERVAL),
                Conversion.hiUint16(OAD_CONN_INTERVAL), 0, 0, Conversion.loUint16(OAD_SUPERVISION_TIMEOUT), Conversion.hiUint16(OAD_SUPERVISION_TIMEOUT) };
        mCharConnReq.setValue(value);
        mLeService.writeCharacteristic(mCharConnReq);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == FILE_ACTIVITY_REQ) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String filename = data.getStringExtra(FileActivity.EXTRA_FILENAME);
                mLeService.setCharacteristicNotification(mCharBlock, true);
                if ( Build.VERSION.SDK_INT >= 21) Log.d("FWUpdateActivity_CC26xx","Requested connection priority HIGH, result : " + this.mLeService.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH));
                setConnectionParameters();
                loadFile(filename, false);
            }
        }
    }

  /*
   * Called when a notification with the current image info has been received
   */

    private void programBlock() {
        if (!mProgramming)
            return;

        if (mProgInfo.iBlocks < mProgInfo.nBlocks) {
            mProgramming = true;
            String msg = new String();

            // Prepare block
            mOadBuffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
            mOadBuffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
            System.arraycopy(mFileBuffer, mProgInfo.iBytes, mOadBuffer, 2, OAD_BLOCK_SIZE);

            // Send block
            mCharBlock.setValue(mOadBuffer);
            boolean success = mLeService.writeCharacteristicNonBlock(mCharBlock);
            Log.d("FwUpdateActivity_CC26xx","Sent block :" + mProgInfo.iBlocks);
            if (success) {
                // Update stats
                packetsSent++;
                mProgInfo.iBlocks++;
                mProgInfo.iBytes += OAD_BLOCK_SIZE;
                mProgressBar.setProgress((mProgInfo.iBlocks * 100) / mProgInfo.nBlocks);
                if (mProgInfo.iBlocks == mProgInfo.nBlocks) {
                    AlertDialog.Builder b = new AlertDialog.Builder(this);

                    b.setMessage(R.string.oad_dialog_programming_finished);
                    b.setTitle("Programming finished");
                    b.setPositiveButton("OK",null);

                    AlertDialog d = b.create();
                    d.show();
                }
            } else {
                mProgramming = false;
                msg = "GATT writeCharacteristic failed\n";
            }
            if (!success) {
                mLog.append(msg);
            }
        } else {
            mProgramming = false;
        }
        if ((mProgInfo.iBlocks % 100) == 0) {
            // Display statistics each 100th block
            runOnUiThread(new Runnable() {
                public void run() {
                    displayStats();
                }
            });
        }

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
            mProgInfo.iTimeElapsed += TIMER_INTERVAL;
        }
    }

    private class ImgHdr {
        short crc0;
        short crc1;
        short ver;
        int len;
        byte[] uid = new byte[4];
        short addr;
        byte imgType;

        ImgHdr(byte[] buf) {
            this.len = ((32 * 0x1000) / (16 / 4));
            this.ver = 0;
            this.uid[0] = this.uid[1] = this.uid[2] = this.uid[3] = 'E';
            this.addr = 0;
            this.imgType = 1; //EFL_OAD_IMG_TYPE_APP
            this.crc0 = calcImageCRC((int)0,buf);
            crc1 = (short)0xFFFF;
        }

        byte[] getRequest() {
            byte[] tmp = new byte[16];
            tmp[0] = Conversion.loUint16((short)this.crc0);
            tmp[1] = Conversion.hiUint16((short)this.crc0);
            tmp[2] = Conversion.loUint16((short)this.crc1);
            tmp[3] = Conversion.hiUint16((short)this.crc1);
            tmp[4] = Conversion.loUint16(this.ver);
            tmp[5] = Conversion.hiUint16(this.ver);
            tmp[6] = Conversion.loUint16((short)this.len);
            tmp[7] = Conversion.hiUint16((short)this.len);
            tmp[8] = tmp[9] = tmp[10] = tmp[11] = this.uid[0];
            tmp[12] = Conversion.loUint16(this.addr);
            tmp[13] = Conversion.hiUint16(this.addr);
            tmp[14] = imgType;
            tmp[15] = (byte)0xFF;
            return tmp;
        }

        short calcImageCRC(int page, byte[] buf) {
            short crc = 0;
            long addr = page * 0x1000;

            byte pageBeg = (byte)page;
            byte pageEnd = (byte)(this.len / (0x1000 / 4));
            int osetEnd = ((this.len - (pageEnd * (0x1000 / 4))) * 4);

            pageEnd += pageBeg;


            while (true) {
                int oset;

                for (oset = 0; oset < 0x1000; oset++) {
                    if ((page == pageBeg) && (oset == 0x00)) {
                        //Skip the CRC and shadow.
                        //Note: this increments by 3 because oset is incremented by 1 in each pass
                        //through the loop
                        oset += 3;
                    }
                    else if ((page == pageEnd) && (oset == osetEnd)) {
                        crc = this.crc16(crc,(byte)0x00);
                        crc = this.crc16(crc,(byte)0x00);

                        return crc;
                    }
                    else {
                        crc = this.crc16(crc,buf[(int)(addr + oset)]);
                    }
                }
                page += 1;
                addr = page * 0x1000;
            }


        }

        short crc16(short crc, byte val) {
            final int poly = 0x1021;
            byte cnt;
            for (cnt = 0; cnt < 8; cnt++, val <<= 1) {
                byte msb;
                if ((crc & 0x8000) == 0x8000) {
                    msb = 1;
                }
                else msb = 0;

                crc <<= 1;
                if ((val & 0x80) == 0x80) {
                    crc |= 0x0001;
                }
                if (msb == 1) {
                    crc ^= poly;
                }
            }

            return crc;
        }

    }



    private class ProgInfo {
        int iBytes = 0; // Number of bytes programmed
        short iBlocks = 0; // Number of blocks programmed
        short nBlocks = 0; // Total number of blocks
        int iTimeElapsed = 0; // Time elapsed in milliseconds

        void reset() {
            iBytes = 0;
            iBlocks = 0;
            iTimeElapsed = 0;
            nBlocks = (short) (mFileImgHdr.len / (OAD_BLOCK_SIZE / HAL_FLASH_WORD_SIZE));
        }
    }
    public void waitABit() {
        int waitTimeout = 20;
        while ((waitTimeout -= 10) > 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
