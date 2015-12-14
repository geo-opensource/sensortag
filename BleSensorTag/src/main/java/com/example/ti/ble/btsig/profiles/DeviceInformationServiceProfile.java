/**************************************************************************************************
 Filename:       DeviceInformationServiceProfile.java
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
package com.example.ti.ble.btsig.profiles;

import java.io.UnsupportedEncodingException;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TableRow;

import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.ble.common.GenericBluetoothProfile;

public class DeviceInformationServiceProfile extends GenericBluetoothProfile {
	private static final String dISService_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
	private static final String dISSystemID_UUID = "00002a23-0000-1000-8000-00805f9b34fb";
	private static final String dISModelNR_UUID = "00002a24-0000-1000-8000-00805f9b34fb";
	private static final String dISSerialNR_UUID = "00002a25-0000-1000-8000-00805f9b34fb";
	private static final String dISFirmwareREV_UUID = "00002a26-0000-1000-8000-00805f9b34fb";
	private static final String dISHardwareREV_UUID = "00002a27-0000-1000-8000-00805f9b34fb";
	private static final String dISSoftwareREV_UUID = "00002a28-0000-1000-8000-00805f9b34fb";
	private static final String dISManifacturerNAME_UUID = "00002a29-0000-1000-8000-00805f9b34fb";
	public final static String ACTION_FW_REV_UPDATED = "com.example.ti.ble.btsig.ACTION_FW_REV_UPDATED";
	public final static String EXTRA_FW_REV_STRING = "com.example.ti.ble.btsig.EXTRA_FW_REV_STRING";
	
	BluetoothGattCharacteristic systemIDc;
	BluetoothGattCharacteristic modelNRc;
	BluetoothGattCharacteristic serialNRc;
	BluetoothGattCharacteristic firmwareREVc;
	BluetoothGattCharacteristic hardwareREVc;
	BluetoothGattCharacteristic softwareREVc;
	BluetoothGattCharacteristic ManifacturerNAMEc;
	DeviceInformationServiceTableRow tRow;
	
	public DeviceInformationServiceProfile(Context con,BluetoothDevice device,BluetoothGattService service,BluetoothLeService controller) {
		super(con,device,service,controller);
		this.tRow =  new DeviceInformationServiceTableRow(con);
		
		List<BluetoothGattCharacteristic> characteristics = this.mBTService.getCharacteristics();
		
		for (BluetoothGattCharacteristic c : characteristics) {
			if (c.getUuid().toString().equals(dISSystemID_UUID)) {
				this.systemIDc = c;
			}
			if (c.getUuid().toString().equals(dISModelNR_UUID)) {
				this.modelNRc = c;
			}
			if (c.getUuid().toString().equals(dISSerialNR_UUID)) {
				this.serialNRc = c;
			}
			if (c.getUuid().toString().equals(dISFirmwareREV_UUID)) {
				this.firmwareREVc = c;
			}
			if (c.getUuid().toString().equals(dISHardwareREV_UUID)) {
				this.hardwareREVc = c;
			}
			if (c.getUuid().toString().equals(dISSoftwareREV_UUID)) {
				this.softwareREVc = c;
			}
			if (c.getUuid().toString().equals(dISManifacturerNAME_UUID)) {
				this.ManifacturerNAMEc = c;
			}
		}
		tRow.title.setText("Device Information Service");
		tRow.sl1.setVisibility(View.INVISIBLE);
		this.tRow.setIcon(this.getIconPrefix(), service.getUuid().toString());
	}
	public static boolean isCorrectService(BluetoothGattService service) {
		if ((service.getUuid().toString().compareTo(dISService_UUID)) == 0) {
			return true;
		}
		else return false;
	}
	@Override
	public void configureService() {
		// Nothing to do here
		
	}
	@Override
	public void deConfigureService() {
		// Nothing to do here
	}
	@Override
	public void enableService () {
		// Read all values
		this.mBTLeService.readCharacteristic(this.systemIDc);
		mBTLeService.waitIdle(GATT_TIMEOUT);
		this.mBTLeService.readCharacteristic(this.modelNRc);
		mBTLeService.waitIdle(GATT_TIMEOUT);
		this.mBTLeService.readCharacteristic(this.serialNRc);
		mBTLeService.waitIdle(GATT_TIMEOUT);
		this.mBTLeService.readCharacteristic(this.firmwareREVc);
		mBTLeService.waitIdle(GATT_TIMEOUT);
		this.mBTLeService.readCharacteristic(this.hardwareREVc);
		mBTLeService.waitIdle(GATT_TIMEOUT);
		this.mBTLeService.readCharacteristic(this.softwareREVc);
		mBTLeService.waitIdle(GATT_TIMEOUT);
		this.mBTLeService.readCharacteristic(this.ManifacturerNAMEc);
	}
	@Override
	public void disableService () {
		// Nothing to do here
	}
	@Override
	public void didWriteValueForCharacteristic(BluetoothGattCharacteristic c) {
		
	}
	@Override
	public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {
		if (this.systemIDc != null) {
			if (c.equals(this.systemIDc)) {
				String s = "System ID: ";
				for (byte b : c.getValue()) {
					s+= String.format("%02x:", b);
				}
				this.tRow.SystemIDLabel.setText(s);
				
			}
		}
		if (this.modelNRc != null) {
			if (c.equals(this.modelNRc)) {
				try {
					this.tRow.ModelNRLabel.setText("Model NR: " + new String(c.getValue(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (this.serialNRc != null) {
			if (c.equals(this.serialNRc)) {
				try {
					this.tRow.SerialNRLabel.setText("Serial NR: " + new String(c.getValue(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (this.firmwareREVc != null) {
			if (c.equals(this.firmwareREVc)) {
				try {
					String s = new String(c.getValue(),"UTF-8");
					this.tRow.FirmwareREVLabel.setText("Firmware Revision: " + s);
					//Post firmware revision to Device activity
					final Intent intent = new Intent(ACTION_FW_REV_UPDATED);
					intent.putExtra(EXTRA_FW_REV_STRING, s);
					context.sendBroadcast(intent);
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (this.hardwareREVc != null) {
			if (c.equals(this.hardwareREVc)) {
				try {
					this.tRow.HardwareREVLabel.setText("Hardware Revision: " + new String(c.getValue(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (this.softwareREVc != null) {
			if (c.equals(this.softwareREVc)) {
				try {
					this.tRow.SoftwareREVLabel.setText("Software Revision: " + new String(c.getValue(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (this.ManifacturerNAMEc != null) {
			if (c.equals(this.ManifacturerNAMEc)) {
				try {
					this.tRow.ManifacturerNAMELabel.setText("Manifacturer Name: " + new String(c.getValue(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {

	}
	@Override
	public String getIconPrefix() {
		String iconPrefix;
		if (this.mBTDevice.getName().equals("CC2650 SensorTag")) {
			iconPrefix = "sensortag2";
		}
		else iconPrefix = ""; 
		return iconPrefix;
	}
	@Override 
	public TableRow getTableRow() {
		return this.tRow;
	}
	
}
