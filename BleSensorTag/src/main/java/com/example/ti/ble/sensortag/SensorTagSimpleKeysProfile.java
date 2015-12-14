/**************************************************************************************************
 Filename:       SensorTagSimpleKeysProfile.java
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
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.View;

import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.ble.common.GattInfo;
import com.example.ti.ble.common.GenericBluetoothProfile;
import com.example.ti.util.Point3D;

public class SensorTagSimpleKeysProfile extends GenericBluetoothProfile {
	public SensorTagSimpleKeysProfile(Context con,BluetoothDevice device,BluetoothGattService service,BluetoothLeService controller) {
		super(con,device,service,controller);
		this.tRow =  new SensorTagSimpleKeysTableRow(con);
		
		List<BluetoothGattCharacteristic> characteristics = this.mBTService.getCharacteristics();
		
		for (BluetoothGattCharacteristic c : characteristics) {
			if (c.getUuid().toString().equals(SensorTagGatt.UUID_KEY_DATA.toString())) {
				this.dataC = c;
			}
		}
		this.tRow.setIcon(this.getIconPrefix(), this.dataC.getUuid().toString());
		this.tRow.title.setText(GattInfo.uuidToName(UUID.fromString(this.dataC.getUuid().toString())));
		this.tRow.uuidLabel.setText(this.dataC.getUuid().toString());
		
		if (!(this.mBTDevice.getName().equals("CC2650 SensorTag"))) {
			SensorTagSimpleKeysTableRow tmpRow = (SensorTagSimpleKeysTableRow) this.tRow;
			tmpRow.sl3.setVisibility(View.INVISIBLE);
			tmpRow.reedStateImage.setVisibility(View.INVISIBLE);
			
		}
		
		
	}
	public static boolean isCorrectService(BluetoothGattService service) {
		if ((service.getUuid().toString().compareTo(SensorTagGatt.UUID_KEY_SERV.toString())) == 0) {
			return true;
		}
		else return false;
	}
	@Override 
	public void enableService () {
		this.isEnabled = true;
	}
	@Override 
	public void disableService () {
		this.isEnabled = false;
	}
	@Override
	public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {
		SensorTagSimpleKeysTableRow tmpRow = (SensorTagSimpleKeysTableRow) this.tRow;
		if (c.equals(this.dataC)){
            byte[] value = c.getValue();
			switch(value[0]) {
			case 0x1:
				tmpRow.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyon_300);
				tmpRow.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyoff_300);
				tmpRow.reedStateImage.setImageResource(R.drawable.reedrelayoff_300);
				break;
			case 0x2:
				tmpRow.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyoff_300);
				tmpRow.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyon_300);
				tmpRow.reedStateImage.setImageResource(R.drawable.reedrelayoff_300);
				break;
			case 0x3:
				tmpRow.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyon_300);
				tmpRow.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyon_300);
				tmpRow.reedStateImage.setImageResource(R.drawable.reedrelayoff_300);
				break;
			case 0x4:
				tmpRow.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyoff_300);
				tmpRow.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyoff_300);
				tmpRow.reedStateImage.setImageResource(R.drawable.reedrelayon_300);
				break;
			case 0x5:
				tmpRow.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyon_300);
				tmpRow.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyoff_300);
				tmpRow.reedStateImage.setImageResource(R.drawable.reedrelayon_300);
				break;
			case 0x6:
				tmpRow.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyoff_300);
				tmpRow.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyon_300);
				tmpRow.reedStateImage.setImageResource(R.drawable.reedrelayon_300);
				break;
			case 0x7:
				tmpRow.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyon_300);
				tmpRow.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyon_300);
				tmpRow.reedStateImage.setImageResource(R.drawable.reedrelayon_300);
				break;
			default:
				tmpRow.leftKeyPressStateImage.setImageResource(R.drawable.leftkeyoff_300);
				tmpRow.rightKeyPressStateImage.setImageResource(R.drawable.rightkeyoff_300);
				tmpRow.reedStateImage.setImageResource(R.drawable.reedrelayoff_300);
				break;
			}
			tmpRow.lastKeys = value[0];
		}
	}
    @Override
    public Map<String,String> getMQTTMap() {
        byte[] value = this.dataC.getValue();
        Map<String,String> map = new HashMap<String, String>();
        map.put("key_1",String.format("%d",value[0] & 0x1));
        map.put("key_2",String.format("%d",value[0] & 0x2));
        map.put("reed_relay",String.format("%d",value[0] & 0x4));
        return map;
    }
}
