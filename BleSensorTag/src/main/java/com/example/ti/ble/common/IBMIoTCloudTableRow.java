/**************************************************************************************************
 Filename:       IBMIoTCloudTableRow.java
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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ti.ble.sensortag.R;
import com.example.ti.ble.sensortag.SensorTagMovementTableRow;
import com.example.ti.util.GenericCharacteristicTableRow;
import com.example.ti.util.SparkLineView;

/**
 * Created by ole on 14/04/15.
 */
public class IBMIoTCloudTableRow extends GenericCharacteristicTableRow {
    Switch pushToCloud;
    TextView pushToCloudCaption;
    TextView cloudURL;
    Button configureCloud;
    ImageView cloudConnectionStatus;


    public IBMIoTCloudTableRow(Context con) {
        super(con);

        this.pushToCloud = new Switch(con);
        this.pushToCloud.setId(100);
        this.pushToCloudCaption = new TextView(con);
        this.pushToCloudCaption.setId(101);
        this.pushToCloudCaption.setText("Push to Cloud :");
        this.cloudURL = new TextView(con);
        this.cloudURL.setTextSize(30);
        this.cloudURL.setTextColor(Color.BLUE);
        this.cloudURL.setId(102);
        this.configureCloud = new Button(con);
        this.configureCloud.setId(103);
        this.configureCloud.setText("Advanced");
        this.cloudConnectionStatus = new ImageView(con);
        this.cloudConnectionStatus.setId(104);
        this.cloudConnectionStatus.setImageDrawable(getResources().getDrawable(R.drawable.cloud_disconnected));




        RelativeLayout.LayoutParams txtItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.value.getId());
        txtItemParams.topMargin = 15;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        this.pushToCloudCaption.setLayoutParams(txtItemParams);

        txtItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.value.getId());
        txtItemParams.topMargin = 15;
        txtItemParams.leftMargin = 10;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,pushToCloudCaption.getId());
        pushToCloud.setLayoutParams(txtItemParams);

        txtItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.pushToCloudCaption.getId());
        txtItemParams.topMargin = 30;
        txtItemParams.leftMargin = 0;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        cloudURL.setLayoutParams(txtItemParams);

        txtItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.cloudURL.getId());
        txtItemParams.topMargin = 30;
        txtItemParams.leftMargin = 0;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        configureCloud.setLayoutParams(txtItemParams);

        txtItemParams = new RelativeLayout.LayoutParams(
                150,
                150);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.cloudURL.getId());
        txtItemParams.topMargin = 0;
        txtItemParams.leftMargin = 30;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,configureCloud.getId());
        cloudConnectionStatus.setLayoutParams(txtItemParams);

        this.rowLayout.addView(this.pushToCloudCaption);
        this.rowLayout.addView(this.pushToCloud);
        this.rowLayout.addView(this.cloudURL);
        this.rowLayout.addView(this.configureCloud);
        this.rowLayout.addView(this.cloudConnectionStatus);

    }
    @Override
    public void onClick(View v) {
    }
    public void setCloudConnectionStatusImage(Drawable drawable) {
        this.cloudConnectionStatus.setImageDrawable(drawable);
    }
}


