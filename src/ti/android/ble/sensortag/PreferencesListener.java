/**************************************************************************************************
  Filename:       PreferencesListener.java
  Revised:        $Date: 2013-09-03 16:44:05 +0200 (ti, 03 sep 2013) $
  Revision:       $Revision: 27592 $

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
  PROVIDED �AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

/**
 * This class provides the link between gui and ble. When a preference is changed this class tells LeStateMachine to turn on/off a sensor or change the polling
 * time.
 * 
 * We could have had this responsibility in leStateMachine, but it is big enough already.
 * */
public class PreferencesListener implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final int MAX_NOTIFICATIONS = 4; // Limit on simultaneous notification in Android 4.3
  private SharedPreferences sharedPreferences;
  private PreferenceFragment preferenceFragment;
  private Context context;

  public PreferencesListener(Context context, SharedPreferences sharedPreferences, PreferenceFragment pf) {
    this.context = context;
    this.sharedPreferences = sharedPreferences;
    this.preferenceFragment = pf;
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Sensor sensor = getSensorFromPrefKey(key);

    boolean noCheckboxWithThatKey = sensor == null;
    if (noCheckboxWithThatKey)
      return;

    boolean turnedOn = sharedPreferences.getBoolean(key, true);

    if (turnedOn && enabledSensors().size() > MAX_NOTIFICATIONS) {
    	// Undo 
    	CheckBoxPreference cb = (CheckBoxPreference) preferenceFragment.findPreference(key);
    	cb.setChecked(false);
    	// Alert user
			alertNotifyLimitaion();
    }
  }

  private void alertNotifyLimitaion() {
  	String msg = "Due to limitations in Android 4.3 BLE " + "you may use a maximum of " + MAX_NOTIFICATIONS
  			+ " notifications simultaneously.\n";
  	
  	AlertDialog.Builder ab = new AlertDialog.Builder(context);

  	ab.setTitle("Notifications limit");
  	ab.setMessage(msg);
  	ab.setIcon(R.drawable.bluetooth);
  	ab.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
  		public void onClick(DialogInterface dialog, int which) {
  		}
  	});

  	// Showing Alert Message
  	AlertDialog alertDialog = ab.create();
  	alertDialog.show();
  }

  /**
   * String is in the format
   * 
   * pref_magnetometer_on
   * 
   * @return Sensor corresponding to checkbox key, or null if there is no corresponding sensor.
   * */
  private Sensor getSensorFromPrefKey(String key) {
    try {
      int start = "pref_".length();
      int end = key.length() - "_on".length();
      String enumName = key.substring(start, end).toUpperCase(Locale.ENGLISH);

      return Sensor.valueOf(enumName);
    } catch (IndexOutOfBoundsException e) {
      // thrown by substring
    } catch (IllegalArgumentException e) {
      // thrown by valueOf
    } catch (NullPointerException e) {
      // thrown by valueOf
    }
    return null; // If exception was thrown while parsing. DON'T replace with catch'em all exception handling.
  }

  private List<Sensor> enabledSensors() {
    List<Sensor> sensors = new ArrayList<Sensor>();
    for (Sensor sensor : Sensor.values())
      if (isEnabledByPrefs(sensor))
        sensors.add(sensor);

    return sensors;
  }

  private boolean isEnabledByPrefs(final Sensor sensor) {
    String preferenceKeyString = "pref_" + sensor.name().toLowerCase(Locale.ENGLISH) + "_on";

    if (!sharedPreferences.contains(preferenceKeyString)) {
      throw new RuntimeException("Programmer error, could not find preference with key " + preferenceKeyString);
    }
    return sharedPreferences.getBoolean(preferenceKeyString, true);
  }
}
