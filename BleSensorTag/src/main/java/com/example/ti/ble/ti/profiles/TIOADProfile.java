/**************************************************************************************************
 Filename:       TIOADProfile.java
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
package com.example.ti.ble.ti.profiles;

import java.io.FileWriter;
import java.util.List;
import java.util.zip.Inflater;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.ble.common.GenericBluetoothProfile;
import com.example.ti.ble.sensortag.FileActivity;
import com.example.ti.ble.sensortag.FwUpdateActivity;
import com.example.ti.ble.sensortag.R;

public class TIOADProfile extends GenericBluetoothProfile {
	private static final String oadService_UUID = "f000ffc0-0451-4000-b000-000000000000";
	private static final String oadImageNotify_UUID = "f000ffc1-0451-4000-b000-000000000000";
	private static final String oadBlockRequest_UUID = "f000ffc2-0451-4000-b000-000000000000";

    public static final String ACTION_PREPARE_FOR_OAD = "com.example.ti.ble.ti.profiles.ACTION_PREPARE_FOR_OAD";
    public static final String ACTION_RESTORE_AFTER_OAD = "com.example.ti.ble.ti.profiles.ACTION_RESTORE_AFTER_OAD";


	private String fwRev;
    private BroadcastReceiver brRecv;
    private boolean clickReceiverRegistered = false;
	
	public TIOADProfile(Context con,BluetoothDevice device,BluetoothGattService service,BluetoothLeService controller) {
		super(con,device,service,controller);
		this.tRow =  new TIOADProfileTableRow(con);
		
		List<BluetoothGattCharacteristic> characteristics = this.mBTService.getCharacteristics();
		
		for (BluetoothGattCharacteristic c : characteristics) {
			if (c.getUuid().toString().equals(oadImageNotify_UUID)) {
				this.dataC = c;
			}
			if (c.getUuid().toString().equals(oadBlockRequest_UUID)) {
				this.configC = c;
			}
		}
		tRow.title.setText("TI OAD Service");
		tRow.sl1.setVisibility(View.INVISIBLE);
		this.tRow.setIcon(this.getIconPrefix(), service.getUuid().toString());

        brRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TIOADProfileTableRow.ACTION_VIEW_CLICKED.equals(intent.getAction())) {
                    Log.d("TIOADProfile","SHOW OAD DIALOG !");
                    prepareForOAD();
                }
            }
        };
        this.context.registerReceiver(brRecv,makeIntentFilter());
        this.clickReceiverRegistered = true;
	}

    @Override
    public void onResume() {
        super.onResume();
        if (!this.clickReceiverRegistered) {
            brRecv = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (TIOADProfileTableRow.ACTION_VIEW_CLICKED.equals(intent.getAction())) {
                        Log.d("TIOADProfile", "SHOW OAD DIALOG !");
                        prepareForOAD();
                    }
                }
            };

            this.context.registerReceiver(brRecv, makeIntentFilter());
            this.clickReceiverRegistered = true;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (this.clickReceiverRegistered) {
            this.context.unregisterReceiver(brRecv);
            this.clickReceiverRegistered = false;
        }
    }
    public static boolean isCorrectService(BluetoothGattService service) {
		if ((service.getUuid().toString().compareTo(oadService_UUID)) == 0) {
			return true;
		}
		else return false;
	}

    public void prepareForOAD () {
        //Override click and launch the OAD screen
        Intent intent = new Intent(ACTION_PREPARE_FOR_OAD);
        context.sendBroadcast(intent);

    }

	@Override
	public void enableService() {
	
	}
	@Override 
	public void disableService() {
		
	}
	@Override
	public void configureService() {
		
	}
	@Override
	public void deConfigureService() {
		
	}
    @Override
    public void periodWasUpdated(int period) {

    }
	@Override
	public void didUpdateFirmwareRevision(String firmwareRev) {
		this.fwRev = firmwareRev;
		this.tRow.value.setText("Current FW :" + firmwareRev); 
	}

    private static IntentFilter makeIntentFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(TIOADProfileTableRow.ACTION_VIEW_CLICKED);
        return fi;
    }

}
