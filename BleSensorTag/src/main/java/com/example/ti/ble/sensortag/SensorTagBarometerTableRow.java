/**************************************************************************************************
 Filename:       SensorTagBarometerTableRow.java
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

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.example.ti.util.GenericCharacteristicTableRow;

public class SensorTagBarometerTableRow extends GenericCharacteristicTableRow {
	public SensorTagBarometerTableRow(Context con) {
		super(con);
		this.calibrateButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v.equals(this.calibrateButton)) {
			this.calibrationButtonTouched();
			return;
		}
		this.config = !this.config;
		Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
		fadeOut.setAnimationListener(this);
		fadeOut.setDuration(500);
		fadeOut.setStartOffset(0);
		Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
		fadeIn.setAnimationListener(this);
		fadeIn.setDuration(500);
		fadeIn.setStartOffset(250);
		if (this.config == true) {
			this.sl1.startAnimation(fadeOut);
			if ((this.sl2.isEnabled()))this.sl2.startAnimation(fadeOut);
			if ((this.sl3.isEnabled()))this.sl3.startAnimation(fadeOut);
			this.value.startAnimation(fadeOut);
			this.onOffLegend.startAnimation(fadeIn);
			this.onOff.startAnimation(fadeIn);
			this.periodLegend.startAnimation(fadeIn);
			this.periodBar.startAnimation(fadeIn);
			this.calibrateButton.startAnimation(fadeIn);
		}
		else {
			this.sl1.startAnimation(fadeIn);
			if ((this.sl2.isEnabled()))this.sl2.startAnimation(fadeIn);
			if ((this.sl3.isEnabled()))this.sl3.startAnimation(fadeIn);
			this.value.startAnimation(fadeIn);
			this.onOffLegend.startAnimation(fadeOut);
			this.onOff.startAnimation(fadeOut);
			this.periodLegend.startAnimation(fadeOut);
			this.periodBar.startAnimation(fadeOut);
			this.calibrateButton.startAnimation(fadeOut);
		}
		
		
	}
	@Override 
	public void onAnimationStart (Animation animation) {
		
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (this.config == true) {
			this.sl1.setVisibility(View.INVISIBLE);
			if ((this.sl2.isEnabled()))this.sl2.setVisibility(View.INVISIBLE);
			if ((this.sl3.isEnabled()))this.sl3.setVisibility(View.INVISIBLE);
			this.onOff.setVisibility(View.VISIBLE);
			this.onOffLegend.setVisibility(View.VISIBLE);
			this.periodBar.setVisibility(View.VISIBLE);
			this.periodLegend.setVisibility(View.VISIBLE);
			this.calibrateButton.setVisibility(View.VISIBLE);
		}
		else {
			this.sl1.setVisibility(View.VISIBLE);
			if ((this.sl2.isEnabled()))this.sl2.setVisibility(View.VISIBLE);
			if ((this.sl3.isEnabled()))this.sl3.setVisibility(View.VISIBLE);
			this.onOff.setVisibility(View.INVISIBLE);
			this.onOffLegend.setVisibility(View.INVISIBLE);
			this.periodBar.setVisibility(View.INVISIBLE);
			this.periodLegend.setVisibility(View.INVISIBLE);
			this.calibrateButton.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		
	}
	public void calibrationButtonTouched() {
		final Intent intent = new Intent(ACTION_CALIBRATE);
		intent.putExtra(EXTRA_SERVICE_UUID, this.uuidLabel.getText());
		this.context.sendBroadcast(intent);
	}

    @Override
    public void grayedOut(boolean gray) {
        super.grayedOut(gray);
        if (gray) {
            calibrateButton.setAlpha(0.4f);
        }
        else {
            calibrateButton.setAlpha(1.0f);
        }
    }
}
