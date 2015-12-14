/**************************************************************************************************
 Filename:       DeviceInformationServiceTableRow.java
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

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ti.util.GenericCharacteristicTableRow;

public class DeviceInformationServiceTableRow extends
		GenericCharacteristicTableRow {
	TextView SystemIDLabel;
	TextView ModelNRLabel;
	TextView SerialNRLabel;
	TextView FirmwareREVLabel;
	TextView HardwareREVLabel;
	TextView SoftwareREVLabel;
	TextView ManifacturerNAMELabel;
	public DeviceInformationServiceTableRow(Context con) {
		super(con);
		this.SystemIDLabel = new TextView(con) {
			{
				setText("System ID: -");
				setId(200);
			}
		};
		this.ModelNRLabel = new TextView(con) {
			{
				setText("Model NR: -");
				setId(201);
			}
		};
		this.SerialNRLabel = new TextView(con) {
			{
				setText("Serial NR: -");
				setId(202);
			}
		};		
		this.FirmwareREVLabel = new TextView(con) {
			{
				setText("Firmware Revision: -");
				setId(203);
			}
		};
		this.HardwareREVLabel = new TextView(con) {
			{
				setText("Hardware Revision: -");
				setId(204);
			}
		};
		this.SoftwareREVLabel = new TextView(con) {
			{
				setText("Software Revision: -");
				setId(205);
			}
		};
		this.ManifacturerNAMELabel = new TextView(con) {
			{
				setText("Manifacturer Name: -");
				setId(206);
			}
		};
		RelativeLayout.LayoutParams tmpLayoutParams = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.MATCH_PARENT,
		        RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
		        this.value.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
		SystemIDLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.MATCH_PARENT,
		        RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
		        this.SystemIDLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
		ModelNRLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.MATCH_PARENT,
		        RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
		        this.ModelNRLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
		SerialNRLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.MATCH_PARENT,
		        RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
		        this.SerialNRLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
		FirmwareREVLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.MATCH_PARENT,
		        RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
		        this.FirmwareREVLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
		HardwareREVLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.MATCH_PARENT,
		        RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
		        this.HardwareREVLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
		SoftwareREVLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.MATCH_PARENT,
		        RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
		        this.SoftwareREVLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
		ManifacturerNAMELabel.setLayoutParams(tmpLayoutParams);
		
		
		rowLayout.addView(SystemIDLabel);
		rowLayout.addView(ModelNRLabel);
		rowLayout.addView(SerialNRLabel);
		rowLayout.addView(FirmwareREVLabel);
		rowLayout.addView(HardwareREVLabel);
		rowLayout.addView(SoftwareREVLabel);
		rowLayout.addView(ManifacturerNAMELabel);
		
		
	}
	
	@Override
	public void onClick(View v) {
		//Do nothing
	}
}
