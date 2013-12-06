/**************************************************************************************************
  Filename:       FileActivity.java
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileActivity extends Activity {
  public final static String EXTRA_FILENAME = "ti.android.ble.devicemonitor.FILENAME";

  // Log
  private static String TAG = "FileActivity";

  // GUI
  private FileAdapter mFileAdapter;
  private ListView mLwFileList;
  private TextView mTwDirName;

  // Housekeeping
  private String mSelectedFile;
  private List<String> mFileList;
  private String mDirectoryName;
  private File mDir;

  public FileActivity() {
    Log.i(TAG, "construct");
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_file);

    Intent i = getIntent();
    mDirectoryName = i.getStringExtra(FwUpdateActivity.EXTRA_MESSAGE);
    mDir = Environment.getExternalStoragePublicDirectory(mDirectoryName);

    mTwDirName = (TextView) findViewById(R.id.tw_directory);
    mLwFileList = (ListView) findViewById(R.id.lw_file);
    mLwFileList.setOnItemClickListener(mFileClickListener);

    // Characteristics list
    mFileList = new ArrayList<String>();
    mFileAdapter = new FileAdapter(this, mFileList);
    mLwFileList.setAdapter(mFileAdapter);

    if (mDir.exists()) {
      mTwDirName.setText(mDir.getPath());
      for (int j = 0; j < mDir.list().length; j++) {
        String f = mDir.list()[j];
        mFileList.add(f);
      }
      mFileAdapter.notifyDataSetChanged();
    } else {
      Toast.makeText(this, mDirectoryName + " does not exist", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "onDestroy");

    mFileList = null;
    mFileAdapter = null;
    super.onDestroy();
  }

  // Listener for characteristic click
  private OnItemClickListener mFileClickListener = new OnItemClickListener() {
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

      // A characteristic item has been selected
      mFileAdapter.setSelectedPosition(pos);
    }
  };

  // Callback for confirm button
  public void onConfirm(View v) {
    Intent i = new Intent();

    Log.i(TAG, "onConfirm");
    i.putExtra(EXTRA_FILENAME, mDir.getAbsolutePath() + File.separator + mSelectedFile);
    setResult(RESULT_OK, i);
    finish();
  }

  //
  // CLASS ServiceAdapter: handle characteristics list
  //
  class FileAdapter extends BaseAdapter {
    Context mContext;
    List<String> mFiles;
    LayoutInflater mInflater;
    int mSelectedPos;

    public FileAdapter(Context context, List<String> files) {
      mInflater = LayoutInflater.from(context);
      mContext = context;
      mFiles = files;
      mSelectedPos = 0;
    }

    public int getCount() {
      return mFiles.size();
    }

    public Object getItem(int pos) {
      return mFiles.get(pos);
    }

    public long getItemId(int pos) {
      return pos;
    }

    public void setSelectedPosition(int pos) {
      mSelectedFile = mFileList.get(pos);
      mSelectedPos = pos;
      notifyDataSetChanged();
    }

    public int getSelectedPosition() {
      return mSelectedPos;
    }

    public View getView(int pos, View view, ViewGroup parent) {
      ViewGroup vg;

      if (view != null) {
        vg = (ViewGroup) view;
      } else {
        vg = (ViewGroup) mInflater.inflate(R.layout.element_file, null);
      }

      // Grab characteristic object
      String file = mFiles.get(pos);

      // Show name, UUID and properties
      TextView twName = (TextView) vg.findViewById(R.id.name);
      twName.setText(file);

      // Highlight selected object
      if (pos == mSelectedPos) {
        twName.setTextAppearance(mContext, R.style.nameStyleSelected);
      } else {
        twName.setTextAppearance(mContext, R.style.nameStyle);
      }

      return vg;
    }
  }

}
