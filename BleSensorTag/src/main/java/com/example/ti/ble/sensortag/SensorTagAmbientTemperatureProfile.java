/**************************************************************************************************
 Filename:       SensorTagAmbientTemperatureProfile.java
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.ble.common.GenericBluetoothProfile;
import com.example.ti.util.GenericCharacteristicTableRow;
import com.example.ti.util.Point3D;

public class SensorTagAmbientTemperatureProfile extends GenericBluetoothProfile {
	public SensorTagAmbientTemperatureProfile(Context con,BluetoothDevice device,BluetoothGattService service,BluetoothLeService controller) {
		super(con,device,service,controller);
		this.tRow =  new GenericCharacteristicTableRow(con);
		
		List<BluetoothGattCharacteristic> characteristics = this.mBTService.getCharacteristics();
		
		for (BluetoothGattCharacteristic c : characteristics) {
			if (c.getUuid().toString().equals(SensorTagGatt.UUID_IRT_DATA.toString())) {
				this.dataC = c;
			}
			if (c.getUuid().toString().equals(SensorTagGatt.UUID_IRT_CONF.toString())) {
				this.configC = c;
			}
			if (c.getUuid().toString().equals(SensorTagGatt.UUID_IRT_PERI.toString())) {
				this.periodC = c;
			}
		}
		this.tRow.sl1.autoScale = true;
		this.tRow.sl1.autoScaleBounceBack = true;
		this.tRow.setIcon(this.getIconPrefix(), this.dataC.getUuid().toString(),"temperature");
		
		//this.tRow.title.setText(GattInfo.uuidToName(UUID.fromString(this.dataC.getUuid().toString())));
		this.tRow.title.setText("Ambient Temperature Data");
		this.tRow.uuidLabel.setText(this.dataC.getUuid().toString());
		this.tRow.value.setText("0.0'C");
		this.tRow.periodMinVal = 200;
		this.tRow.periodBar.setMax(255 - (this.tRow.periodMinVal / 10));
		this.tRow.periodBar.setProgress(100);
	}
	public void configureService() {
        int error = mBTLeService.writeCharacteristic(this.configC, (byte)0x01);
        if (error != 0) {
            if (this.configC != null)
            Log.d("SensorTagAmbientTemperatureProfile","Sensor config failed: " + this.configC.getUuid().toString() + " Error: " + error);
        }
        error = this.mBTLeService.setCharacteristicNotification(this.dataC, true);
        if (error != 0) {
            if (this.dataC != null)
            Log.d("SensorTagAmbientTemperatureProfile","Sensor notification enable failed: " + this.configC.getUuid().toString() + " Error: " + error);
        }
        /*
		if (mBTLeService.writeCharacteristic(this.configC, (byte)0x01)) {
			mBTLeService.waitIdle(GATT_TIMEOUT);
		} else {
			Log.d("SensorTagAmbientTemperatureProfile","Sensor config failed: " + this.configC.getUuid().toString());
        }
        */

		this.isConfigured = true;
	}
	public void deConfigureService() {
        int error = mBTLeService.writeCharacteristic(this.configC, (byte)0x00);
        if (error != 0) {
            if (this.configC != null)
            Log.d("SensorTagAmbientTemperatureProfile","Sensor config failed: " + this.configC.getUuid().toString() + " Error: " + error);
        }
        error = this.mBTLeService.setCharacteristicNotification(this.dataC, false);
        if (error != 0) {
            if (this.dataC != null)
            Log.d("SensorTagAmbientTemperatureProfile","Sensor notification enable failed: " + this.configC.getUuid().toString() + " Error: " + error);
        }
        this.isConfigured = false;
	}
    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {
        byte[] value = c.getValue();
		if (c.equals(this.dataC)){
			Point3D v = Sensor.IR_TEMPERATURE.convert(value);
			if (this.tRow.config == false) { 
				if ((this.isEnabledByPrefs("imperial")) == true) this.tRow.value.setText(String.format("%.1f'F", (v.x * 1.8) + 32));
				else this.tRow.value.setText(String.format("%.1f'C", v.x));
			}
			this.tRow.sl1.addValue((float)v.x);
		}
	}
	public static boolean isCorrectService(BluetoothGattService service) {
		if ((service.getUuid().toString().compareTo(SensorTagGatt.UUID_IRT_SERV.toString())) == 0) {
			return true;
		}
		else return false;
	}
    @Override
    public Map<String,String> getMQTTMap() {
        Point3D v = Sensor.IR_TEMPERATURE.convert(this.dataC.getValue());
        Map<String,String> map = new HashMap<String, String>();
        map.put("ambient_temp",String.format("%.2f",v.x));
        return map;
    }
}
