/**************************************************************************************************
 Filename:       IBMIoTCloudProfile.java
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

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.example.ti.ble.sensortag.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/*
#define START_STRING @"{\n \"d\":{\n"
#define VARIABLE_STRING(a,b) [NSString stringWithFormat:@"\"%@\":\"%@\"",a,b]
#define STOP_STRING @"\n}\n}"
*/

/**
 * Created by ole on 07/04/15.
 */
public class IBMIoTCloudProfile extends GenericBluetoothProfile {
    final String startString = "{\n \"d\":{\n";
    final String stopString = "\n}\n}";
    MqttAndroidClient client;
    MemoryPersistence memPer;
    final String addrShort;
    static IBMIoTCloudProfile mThis;
    Map<String, String> valueMap = new HashMap<String, String>();
    Timer publishTimer;
    public boolean ready;
    private WakeLock wakeLock;
    BroadcastReceiver cloudConfigUpdateReceiver;
    cloudConfig config;

    public IBMIoTCloudProfile(final Context con,BluetoothDevice device,BluetoothGattService service,BluetoothLeService controller) {
        super(con,device,service,controller);
        this.tRow =  new IBMIoTCloudTableRow(con);
        this.tRow.setOnClickListener(null);

        config = readCloudConfigFromPrefs();

        if (config != null) {
            Log.d("IBMIoTCloudProfile", "Stored cloud configuration" + "\r\n" + config.toString());
        }
        else {
            config = initPrefsWithIBMQuickStart();
            Log.d("IBMIoTCloudProfile", "Stored cloud configuration was corrupt, starting new based on IBM IoT Quickstart variables" + config.toString());
        }

        String addr = mBTDevice.getAddress();
        String[] addrSplit = addr.split(":");
        int[] addrBytes = new int[6];
        for (int ii = 0; ii < 6; ii++) {
            addrBytes[ii] = Integer.parseInt(addrSplit[ii], 16);
        }
        ready = false;
        this.addrShort = String.format("%02x%02x%02x%02x%02x%02x",addrBytes[0],addrBytes[1],addrBytes[2],addrBytes[3],addrBytes[4],addrBytes[5]);
        Log.d("IBMIoTCloudProfile", "Device ID : " + addrShort);
        this.tRow.sl1.setVisibility(View.INVISIBLE);
        this.tRow.sl2.setVisibility(View.INVISIBLE);
        this.tRow.sl3.setVisibility(View.INVISIBLE);
        this.tRow.title.setText("Cloud View");
        this.tRow.setIcon("sensortag2cloudservice","","");
        this.tRow.value.setText("Device ID : " + addr);

        IBMIoTCloudTableRow tmpRow = (IBMIoTCloudTableRow) this.tRow;
        tmpRow.pushToCloud.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    connect();
                }
                else {
                    disconnect();
                }
            }
        });


        tmpRow.configureCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloudProfileConfigurationDialogFragment dF = CloudProfileConfigurationDialogFragment.newInstance(addrShort);

                final Activity act = (Activity)context;
                dF.show(act.getFragmentManager(),"CloudConfig");


              }
        });


        /*
        String url = "https://quickstart.internetofthings.ibmcloud.com/#/device/" + addrShort + "/sensor/";

        Pattern pattern = Pattern.compile(url);
        Linkify.addLinks(((IBMIoTCloudTableRow) this.tRow).cloudURL, pattern, "https://");
        ((IBMIoTCloudTableRow) this.tRow).cloudURL.setText(Html.fromHtml("<a href='https://" + url + "'>" + url + "</a>"));
*/

        if (config.service == CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_CLOUD_SERVICE) {
            ((IBMIoTCloudTableRow) this.tRow).cloudURL.setText("Open in browser");
            ((IBMIoTCloudTableRow) this.tRow).cloudURL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://quickstart.internetofthings.ibmcloud.com/#/device/" + addrShort + "/sensor/")));
                }
            });
        }
        else {
            ((IBMIoTCloudTableRow) this.tRow).cloudURL.setText("");
            ((IBMIoTCloudTableRow) this.tRow).cloudURL.setAlpha(0.1f);
        }
        mThis = this;
        cloudConfigUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CloudProfileConfigurationDialogFragment.ACTION_CLOUD_CONFIG_WAS_UPDATED)) {
                    Log.d("IBMIoTCloudProfile","Cloud configuration was updated !");
                    if (client != null) {
                        config = readCloudConfigFromPrefs();
                        if (client.isConnected()) {
                            disconnect();
                            connect();
                        }
                    }
                }
            }
        };
        this.context.registerReceiver(cloudConfigUpdateReceiver,makeCloudConfigUpdateFilter());
    }
    public boolean disconnect() {
        try {
            ((IBMIoTCloudTableRow) tRow).setCloudConnectionStatusImage(context.getResources().getDrawable(R.drawable.cloud_disconnected));
            ready = false;
            if (publishTimer != null) {
                publishTimer.cancel();
            }
            if (client != null) {
                Log.d("IBMIoTCloudProfile", "Disconnecting from cloud : " + client.getServerURI() + "," + client.getClientId());
                if (client.isConnected()) client.disconnect();
                client.unregisterResources();
                client = null;
                memPer = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean connect() {
        try {
            memPer = new MemoryPersistence();
            String url = config.brokerAddress + ":" + config.brokerPort;
            Log.d("IBMIoTCloudProfile","Cloud Broker URL : " + url);
            client = new MqttAndroidClient(this.context,url,config.deviceId);
            MqttConnectOptions options = null;

            if (config.service > CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_CLOUD_SERVICE) {
                options = new MqttConnectOptions();
                options.setCleanSession(config.cleanSession);
                if (config.username.length() > 0)options.setUserName(config.username);
                if (config.password.length() > 0)options.setPassword(config.password.toCharArray());
                Log.d("IBMIoTCloudProfile","Adding Options : Clean Session : " + options.isCleanSession() + ", Username : " + config.username + ", " + "Password : " + "********");
            }

            client.connect(options, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d("IBMIoTCloudProfile", "Connected to cloud : " + client.getServerURI() + "," + client.getClientId());

                    try {
                        client.publish(config.publishTopic,jsonEncode("myName",mBTDevice.getName().toString()).getBytes(),0,false);
                        ready = true;
                    }
                    catch (MqttException e) {
                        e.printStackTrace();
                    }

                    ((IBMIoTCloudTableRow) tRow).setCloudConnectionStatusImage(context.getResources().getDrawable(R.drawable.cloud_connected));

                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.d("IBMIoTCloudProfile","Connection to IBM cloud failed !");
                    Log.d("IBMIoTCloudProfile","Error: " + throwable.getLocalizedMessage());
                    ((IBMIoTCloudTableRow) tRow).setCloudConnectionStatusImage(context.getResources().getDrawable(R.drawable.cloud_disconnected));
                }
            });
        }
        catch (MqttException e) {
            e.printStackTrace();

        }
        publishTimer = new Timer();
        MQTTTimerTask task = new MQTTTimerTask();
        publishTimer.schedule(task,1000,1000);
        return true;
    }


    public String jsonEncode(String variableName, String Value) {
        String tmpString = new String();
        tmpString += startString;
        tmpString += "\"" + variableName + "\"" + ":" + "\"" + Value + "\"";
        tmpString += stopString;
        return tmpString;
    }
    public String jsonEncode(String str) {
        String tmpString = new String();
        tmpString += startString;
        tmpString += str;
        tmpString += stopString;
        return tmpString;
    }
    public void publishString(String str) {
        MqttMessage message = new MqttMessage();
        try {

            client.publish(config.publishTopic,jsonEncode("Test","123").getBytes(),0,false);
            //Log.d("IBMIoTCloudProfile", "Published message :" + message.toString());
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void addSensorValueToPendingMessage(String variableName, String Value) {
        this.valueMap.put(variableName,Value);
    }
    public void addSensorValueToPendingMessage(Map.Entry<String,String> e) {
        this.valueMap.put(e.getKey(),e.getValue());
    }
    @Override
    public void onPause() {
        super.onPause();
        this.context.unregisterReceiver(cloudConfigUpdateReceiver);
    }
    @Override
    public void onResume() {
        super.onResume();
        this.context.registerReceiver(cloudConfigUpdateReceiver,makeCloudConfigUpdateFilter());
    }
    @Override
    public void enableService () {

    }
    @Override
    public void disableService () {

    }
    @Override
    public void configureService() {

    }
    @Override
    public void deConfigureService() {

    }
    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {
    }
    @Override
    public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {
    }
    public static IBMIoTCloudProfile getInstance() {
        return mThis;
    }
    class MQTTTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (ready) {
                    final Activity activity = (Activity) context;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((IBMIoTCloudTableRow)tRow).setCloudConnectionStatusImage(activity.getResources().getDrawable(R.drawable.cloud_connected_tx));
                        }
                    });
                    String publishValues = "";
                    Map<String, String> dict = new HashMap<String, String>();
                    dict.putAll(valueMap);
                    for (Map.Entry<String, String> entry : dict.entrySet()) {
                        String var = entry.getKey();
                        String val = entry.getValue();

                        publishValues += "\"" + var + "\"" + ":" + "\"" + val + "\"" + ",\n";
                    }
                    if (publishValues.length() > 0) {
                        String pub = publishValues.substring(0, publishValues.length() - 2);
                        client.publish(config.publishTopic, jsonEncode(pub).getBytes(), 0, false);
                        //Log.d("IBMIoTCloudProfile", "Published :" + jsonEncode(pub));
                        try {
                            Thread.sleep(60);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((IBMIoTCloudTableRow)tRow).setCloudConnectionStatusImage(activity.getResources().getDrawable(R.drawable.cloud_connected));
                        }
                    });
                }
                else {
                    Log.d("IBMIoTCloudProfile", "MQTTTimerTask ran, but MQTT not ready");
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private static IntentFilter makeCloudConfigUpdateFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(CloudProfileConfigurationDialogFragment.ACTION_CLOUD_CONFIG_WAS_UPDATED);
        return fi;
    }

    class cloudConfig extends Object {
        public Integer service;
        public String username;
        public String password;
        public String deviceId;
        public String brokerAddress;
        public int brokerPort;
        public String publishTopic;
        public boolean cleanSession;
        public boolean useSSL;
        cloudConfig () {

        }
        @Override
        public String toString() {
            String s = new String();
            s = "Cloud configuration :\r\n";
            s += "Service : " + service + "\r\n";
            s += "Username : " + username + "\r\n";
            s += "Password : " + password + "\r\n";
            s += "Device ID : " + deviceId + "\r\n";
            s += "Broker Address : " + brokerAddress + "\r\n";
            s += "Proker Port : " + brokerPort + "\r\n";
            s += "Publish Topic : " + publishTopic + "\r\n";
            s += "Clean Session : " + cleanSession + "\r\n";
            s += "Use SSL : " + useSSL + "\r\n";
            return s;
        }
    }
    public cloudConfig readCloudConfigFromPrefs() {
        cloudConfig c = new cloudConfig();
        try {
            c.service = Integer.parseInt(CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_SERVICE,this.context),10);
            c.username = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_USERNAME,this.context);
            c.password = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_PASSWORD,this.context);
            c.deviceId = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_DEVICE_ID,this.context);
            c.brokerAddress = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_BROKER_ADDR,this.context);
            c.brokerPort = Integer.parseInt(CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_BROKER_PORT,this.context),10);
            c.publishTopic = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_PUBLISH_TOPIC,this.context);
            c.cleanSession = Boolean.parseBoolean(CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_CLEAN_SESSION,this.context));
            c.useSSL = Boolean.parseBoolean(CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_USE_SSL,this.context));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return c;
    }
    public cloudConfig initPrefsWithIBMQuickStart() {
        cloudConfig c = new cloudConfig();
        c.service = CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_CLOUD_SERVICE;
        c.username = CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_USERNAME;
        c.password = CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_PASSWORD;
        c.deviceId = CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_DEVICEID_PREFIX + addrShort;
        c.brokerAddress = CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_BROKER_ADDR;
        try {
            c.brokerPort = Integer.parseInt(CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_BROKER_PORT);
        }
        catch (Exception e) {
            c.brokerPort = 1883;
        }
        c.publishTopic = CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_PUBLISH_TOPIC;
        c.cleanSession = CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_CLEAN_SESSION;
        c.useSSL = CloudProfileConfigurationDialogFragment.DEF_CLOUD_IBMQUICKSTART_USE_SSL;
        return c;
    }
    public void writeCloudConfigToPrefs(cloudConfig c) {
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_SERVICE,c.service.toString(),this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_USERNAME,c.username,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_PASSWORD,c.password,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_DEVICE_ID,c.deviceId,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_BROKER_ADDR,c.brokerAddress,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_BROKER_PORT,((Integer)c.brokerPort).toString(),this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_PUBLISH_TOPIC,c.publishTopic,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_CLEAN_SESSION,((Boolean)c.cleanSession).toString(),this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_USE_SSL,((Boolean)c.useSSL).toString(),this.context);
    }
}
