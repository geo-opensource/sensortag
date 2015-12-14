/**************************************************************************************************
  Filename:       AboutDialog.java
  Revised:        $Date: 2013-08-30 12:02:37 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27470 $

  Copyright (c) 2013 - 2014 Texas Instruments Incorporated

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

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
// import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class AboutDialog extends Dialog {
  // Log
  // private static final String TAG = "AboutDialog";

  private Context mContext;
  private static AboutDialog mDialog;
  private static OkListener mOkListener;
  private final String errorHTML = "<html><body><h1>Failed to load web page</h1></body></html>";

  public AboutDialog(Context context) {
    super(context);
    mContext = context;
    mDialog = this;
    mOkListener = new OkListener();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialog_about);

    // From About.html web page
    WebView webView = (WebView) findViewById(R.id.web_content);
    webView.setWebViewClient(new WebViewClient(){
      
    	@Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
          view.loadUrl(url);
          return false;
      }
      
    	@Override
    	public void onPageFinished(WebView view, final String url) {
    		// Log.i(TAG,"Web page loaded: " + url);
    	}

    	@Override
    	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    		// Do something
    		view.loadData(errorHTML, "text/html", "UTF-8");
    		// Log.e(TAG,"Failed to load web page");
    	}
    });

    // Header
    Resources res = mContext.getResources();
    String appName = res.getString(R.string.app_name);
    TextView title = (TextView) findViewById(R.id.title);
    title.setText("About " + appName);

    // Application info
    TextView head = (TextView) findViewById(R.id.header);
    String appVersion = "Revision: ";
    try {
      appVersion += mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      // Log.v(TAG, e.getMessage());
    }
    head.setText(appVersion);

    // Dismiss button
    Button okButton = (Button) findViewById(R.id.buttonOK);
    okButton.setOnClickListener(mOkListener);

    // Device information
    TextView foot = (TextView) findViewById(R.id.footer);
    String txt = Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL + " Android " + Build.VERSION.RELEASE + " " + Build.DISPLAY;

    foot.setText(txt);
  }

  private class OkListener implements android.view.View.OnClickListener {
    @Override
    public void onClick(View v) {
      mDialog.dismiss();
    }
  }
}
