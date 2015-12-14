/**************************************************************************************************
 Filename:       GenericBluetoothProfile.java
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
package com.example.ti.ble.common;

import com.example.ti.util.GenericCharacteristicTableRow;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TableRow;

import java.util.Map;

public class GenericBluetoothProfile {
	protected BluetoothDevice mBTDevice;
	protected BluetoothGattService mBTService;
	protected GenericCharacteristicTableRow tRow;
	protected BluetoothLeService mBTLeService;
	protected BluetoothGattCharacteristic dataC;
	protected BluetoothGattCharacteristic configC;
	protected BluetoothGattCharacteristic periodC;
	protected static final int GATT_TIMEOUT = 250; // milliseconds
	protected Context context;
	protected boolean isRegistered;
    public boolean isConfigured;
    public boolean isEnabled;
	public GenericBluetoothProfile(final Context con,BluetoothDevice device,BluetoothGattService service,BluetoothLeService controller) {
		super();
		this.mBTDevice = device;
		this.mBTService = service;
		this.mBTLeService = controller;
		this.tRow =  new GenericCharacteristicTableRow(con);
		this.dataC = null;
		this.periodC = null;
		this.configC = null;
		this.context = con;
		this.isRegistered = false;
	}
	public void onResume() {
		if (this.isRegistered == false) {
			this.context.registerReceiver(guiReceiver, GenericBluetoothProfile.makeFilter());
			this.isRegistered = true;
		}
	}
	public void onPause() {
		if (this.isRegistered == true) {
			this.context.unregisterReceiver(guiReceiver);
			this.isRegistered = false;
		}
	}
	public static boolean isCorrectService(BluetoothGattService service) {
		//Always return false in parent class
		return false;
	}
    public boolean isDataC(BluetoothGattCharacteristic c) {
        if (this.dataC == null) return false;
        if (c.equals(this.dataC)) return true;
        else return false;
    }
	public void configureService() {
        int error = this.mBTLeService.setCharacteristicNotification(this.dataC, true);
        if (error != 0) {
            if (this.dataC != null)
                printError("Sensor notification enable failed: ",this.dataC,error);
        }
		this.isConfigured = true;
	}
	public void deConfigureService() {
        int error = this.mBTLeService.setCharacteristicNotification(this.dataC, false);
        if (error != 0) {
            if (this.dataC != null)
                printError("Sensor notification disable failed: ",this.dataC,error);
        }
        this.isConfigured = false;
	}
	public void enableService () {
        int error = mBTLeService.writeCharacteristic(this.configC, (byte)0x01);
        if (error != 0) {
            if (this.configC != null)
                printError("Sensor enable failed: ",this.configC,error);
        }
        //this.periodWasUpdated(1000);
        this.isEnabled = true;
	}
	public void disableService () {
        int error = mBTLeService.writeCharacteristic(this.configC, (byte)0x00);
        if (error != 0) {
            if (this.configC != null)
                printError("Sensor disable failed: ",this.configC,error);
        }
        this.isConfigured = false;
	}
	public void didWriteValueForCharacteristic(BluetoothGattCharacteristic c) {
		
	}
	public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {
		if (this.periodC != null) {
			if (c.equals(this.periodC)) {
				byte[] value = c.getValue();
				this.periodWasUpdated(value[0] * 10);
			}
		}
	}
	public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {
        /*
		if (c.equals(this.dataC)) {
			byte[] value = c.getValue();
			this.didUpdateValueForCharacteristic(this.dataC.getUuid().toString(), value);
		}
		*/
	}
	public TableRow getTableRow() {
		return this.tRow;
	}
	public String getIconPrefix() {
		String iconPrefix;
		if (this.mBTDevice.getName().equals("CC2650 SensorTag")) {
			iconPrefix = "sensortag2";
		}
		else iconPrefix = "";
		return iconPrefix;
	}
	public boolean isEnabledByPrefs(String prefName) {
		String preferenceKeyString = "pref_"
		    + prefName;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mBTLeService);

		Boolean defaultValue = true;
		return prefs.getBoolean(preferenceKeyString, defaultValue);
	}
	public void periodWasUpdated(int period) {
		if (period > 2450) period = 2450; 
		if (period < 100) period = 100;
		byte p = (byte)((period / 10) + 10);
		Log.d("GenericBluetoothProfile","Period characteristic set to :" + period);
        /*
		if (this.mBTLeService.writeCharacteristic(this.periodC, p)) {
			mBTLeService.waitIdle(GATT_TIMEOUT);
		} else {
			Log.d("GenericBluetoothProfile","Sensor period failed: " + this.periodC.getUuid().toString());
		}
		*/
        int error = mBTLeService.writeCharacteristic(this.periodC, p);
        if (error != 0) {
            if (this.periodC != null)
                printError("Sensor period failed: ",this.periodC,error);
        }
		this.tRow.periodLegend.setText("Sensor period (currently : " + period + "ms)");
	}
    public Map<String,String> getMQTTMap() {
        return null;
    }
	public void onOffWasUpdated(boolean on) {
		Log.d("GenericBluetoothProfile","Config characteristic set to :" + on);
		if (on) {
			this.configureService();
			this.enableService();
			this.tRow.grayedOut(false);
		}
		else {
			this.deConfigureService();
			this.disableService();
			this.tRow.grayedOut(true);
		}
		
	}
	public void grayOutCell(boolean grayedOut) {
		
		if (grayedOut == true){ 
			this.tRow.setAlpha(0.4f);
			this.tRow.onOff.setChecked(false);
		}
		else {
			this.tRow.setAlpha(1.0f);
			this.tRow.onOff.setChecked(true);
		}
	}
	private static IntentFilter makeFilter() {
		final IntentFilter fi = new IntentFilter();
		fi.addAction(GenericCharacteristicTableRow.ACTION_PERIOD_UPDATE);
		fi.addAction(GenericCharacteristicTableRow.ACTION_ONOFF_UPDATE);
		fi.addAction(GenericCharacteristicTableRow.ACTION_CALIBRATE);
		return fi;
	}
	private final BroadcastReceiver guiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			final String uuid = intent.getStringExtra(GenericCharacteristicTableRow.EXTRA_SERVICE_UUID);
			Log.d("Test", uuid + "!=" + tRow.uuidLabel.getText().toString());
			if ((tRow.uuidLabel.getText().toString().compareTo(uuid)) == 0) { 
				if ((action.compareTo(GenericCharacteristicTableRow.ACTION_PERIOD_UPDATE) == 0)) {
					final int period = intent.getIntExtra(GenericCharacteristicTableRow.EXTRA_PERIOD, 1000);
					periodWasUpdated(period);
				}
				else if ((action.compareTo(GenericCharacteristicTableRow.ACTION_ONOFF_UPDATE) == 0)) {
					final boolean on = intent.getBooleanExtra(GenericCharacteristicTableRow.EXTRA_ONOFF, false);
					onOffWasUpdated(on);
				}
				else if ((action.compareTo(GenericCharacteristicTableRow.ACTION_CALIBRATE) == 0)) {
					calibrationButtonTouched();
				}
			}
		}
	};
	protected void calibrationButtonTouched() {
		
	}
	public void didUpdateFirmwareRevision(String fwRev) {
		
	}
    public void printError (String msg, BluetoothGattCharacteristic c, int error) {
        try {
            Log.d("GenericBluetoothProfile", msg + c.getUuid().toString() + " Error: " + error);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
