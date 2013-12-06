/**************************************************************************************************
  Filename:       AboutDialog.java
  Revised:        $Date: 2013-08-30 12:02:37 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27470 $

  Copyright 2013 Texas Instruments Incorporated. All rights reserved.
 
  IMPORTANT: Your use of this Software is limited to those specific rights
  granted under the terms of a software license agreement between the user
  who downloaded the software, his/her employer (which must be your employer)
  and Texas Instruments Incorporated (the "License").  You may not use this
  Software unless you agree to abide by the terms of the License. 
  The License limits your use, and you acknowledge, that the Software may not be 
  modified, copied or distributed unless used solely and exclusively in conjunction 
  with a Texas Instruments Bluetooth device. Other than for the foregoing purpose, 
  you may not use, reproduce, copy, prepare derivative works of, modify, distribute, 
  perform, display or sell this Software and/or its documentation for any purpose.
 
  YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
  PROVIDED “AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
  INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
  NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
  TEXAS INSTRUMENTS OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT,
  NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER
  LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
  INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
  OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
  OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
  (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 
  Should you have any questions regarding your right to use this Software,
  contact Texas Instruments Incorporated at www.TI.com

 **************************************************************************************************/
package ti.android.ble.sensortag;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class AboutDialog extends Dialog {
  // Log
  private static final String TAG = "AboutDialog";

  private Context mContext;
  private static AboutDialog mDialog;
  private static OkListener mOkListener;

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
      Log.v(TAG, e.getMessage());
    }
    head.setText(appVersion);

    // From About.html web page
    WebView wv = (WebView) findViewById(R.id.content);
    wv.loadUrl("file:///android_asset/about.html");

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
