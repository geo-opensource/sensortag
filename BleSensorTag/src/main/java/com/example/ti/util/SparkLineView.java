/**************************************************************************************************
 Filename:       SparkLineView.java
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
package com.example.ti.util;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

@SuppressLint("DrawAllocation") public class SparkLineView extends View {
	private final int numberOfPoints = 15;
	private final Paint pointStrokePaint;
	private final Paint pointFillPaint;
	private final Paint sparkLinePaint;
	private ArrayList<Float> dataPoints;
	public float displayWidth;
	public float maxVal;
	public float minVal;
	public boolean autoScale = false;
	public boolean autoScaleBounceBack = false;
	public SparkLineView(Context context) {
		   super(context);
		this.sparkLinePaint = new Paint() {
			    {
			        setStyle(Paint.Style.STROKE);
			        setStrokeCap(Paint.Cap.ROUND);
			        setStrokeWidth(5.0f);
			        setAntiAlias(true);
			        setARGB(255,255,0,0);
			    }
		};
		this.pointStrokePaint = new Paint() {
			{
				setARGB(255, 255, 255, 255);
				setStyle(Style.FILL_AND_STROKE);
				setAntiAlias(true);
			}
		};
		this.pointFillPaint = new Paint() {
			{
				setARGB(255, 255, 0, 0);
				setStyle(Style.FILL);
				setAntiAlias(true);
			}
		};
		this.dataPoints = new ArrayList<Float>();
		this.maxVal = 1.0f;
		this.minVal = 0.0f;
		int ii = 0;
		
		for (ii = 0; ii < numberOfPoints; ii++) {
			this.dataPoints.add(Float.valueOf(0.0f));
		}
		this.setWillNotDraw(false);
		this.setBackgroundColor(Color.TRANSPARENT);
		this.displayWidth = 200;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int ii;
		int iterations = numberOfPoints;
		float border = 15;
		float w = this.getWidth();
		float h = this.getHeight();
		float max = 0;
		float min = 0;

		super.onDraw(canvas);
		Path path = new Path();
		
		if ((this.dataPoints.size() - 1 - numberOfPoints) < 0) iterations = this.dataPoints.size() - 1;
		
		ArrayList<Float> subList = new ArrayList<Float>(this.dataPoints.subList((this.dataPoints.size() - 1) - iterations, this.dataPoints.size() - 1));


		for (ii = 0; ii < iterations; ii++) {
			Float v = subList.get(ii);

			if (v > max) max = v;
			if (v < min) min = v;
			if (this.autoScale) {
				if (v > this.maxVal) { 
					this.maxVal = v + 0.01f;
					if (this.minVal < -0.001) this.minVal = -v - 0.01f;
					else this.minVal = 0.0f;
				}
				else if (v < this.minVal)  {
					this.minVal = v - 0.01f;
					this.maxVal = -v + 0.01f;
				}
			}
		}
		if (this.autoScaleBounceBack) {
			max = Math.max(max,Math.abs(min));
			this.maxVal = max;
			if (this.minVal < -0.1f) this.minVal = -max;
			else this.minVal = 0.0f;
		}
		for (ii = 0; ii < iterations; ii++) {
			if (ii == 0) {
				Float v = subList.get(ii);
				path.moveTo(0, h - ((h / (this.maxVal - this.minVal)) * (v - this.minVal)));
				continue;
			}
			else {
				//Last value
				Float v0 = subList.get(ii - 1);
				//This value
				Float v1 = subList.get(ii);
				//Last Point coordinate
				PointF p0 = new PointF();
				//This Point coordinate
				PointF p1 = new PointF();
				//Midpoint between p0 and p1
				PointF midPoint;
				//Control point 
				PointF c1;
				PointF c2;
				
				p0.x = ((w - (2 * border) )/ iterations) * (ii - 1) + border;
				p0.y= h - border - (((h - (2 * border)) / (this.maxVal - this.minVal)) * (v0 - this.minVal));
				p1.x = ((w - (2 * border) )/ iterations) * (ii) + border;
				p1.y = h - border - (((h - (2 * border)) / (this.maxVal - this.minVal)) * (v1 - this.minVal));
				midPoint = this.midPointForPoints(p0, p1);
				c1 = this.controlPointForPoints(midPoint, p0);
				path.quadTo(c1.x, c1.y, midPoint.x, midPoint.y);
				c2 = this.controlPointForPoints(midPoint, p1);
				path.quadTo(c2.x, c2.y, p1.x, p1.y);
			}
		}
		canvas.drawPath(path,this.sparkLinePaint);
		for (ii = 0; ii < iterations; ii++) {
			Float v = subList.get(ii);
			Float x,y;
			x = ((w - (2 * border) )/ iterations) * ii + border;
			y =  h - border - (((h - (2 * border)) / (this.maxVal - this.minVal)) * (v - this.minVal));
			canvas.drawCircle(x,y, 10, this.pointStrokePaint);
			canvas.drawCircle(x,y, 7, this.pointFillPaint);
		}
	}
	
	PointF controlPointForPoints(PointF p1, PointF p2) {
		PointF controlPoint = midPointForPoints(p1, p2);
	    Float diffY = (float) Math.abs(p2.y - controlPoint.y);
	    
	    if (p1.y < p2.y)
	        controlPoint.y += diffY;
	    else if (p1.y > p2.y)
	        controlPoint.y -= diffY;
	    
	    return controlPoint;
	}
	PointF midPointForPoints(PointF p1, PointF p2) {
		PointF tmp = new PointF();
		tmp.x = (p1.x + p2.x) / 2;
		tmp.y = (p1.y + p2.y) / 2;
	    return tmp;
	}
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int padding = 40;
		int h = MeasureSpec.getSize(heightMeasureSpec);
		int w = MeasureSpec.getSize(widthMeasureSpec) - padding;
		if (h < 200) h = 200;
		if (w < this.displayWidth) w = (int)this.displayWidth;
		setMeasuredDimension(w, h);
	}
	public void addValue(float value) {
		Float val = Float.valueOf(value);
		this.dataPoints.add(val);
		this.invalidate();
	}
	public void setColor(int a,int r,int g, int b) {
		this.pointFillPaint.setARGB(a, r, g, b);
		this.sparkLinePaint.setARGB(a, r, g, b);
	}
}
