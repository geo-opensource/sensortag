/**************************************************************************************************
  Filename:       Sensor.java
  Revised:        $Date: 2013-08-30 11:44:31 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27454 $

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

//import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
import static ti.android.ble.sensortag.SensorTag.UUID_IRT_SERV;
import static ti.android.ble.sensortag.SensorTag.*;
import static java.lang.Math.pow;

import java.util.List;
import java.util.UUID;

import ti.android.util.Point3D;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;


/**
 * This enum encapsulates the differences amongst the sensors. The differences include UUID values and how to interpret the
 * characteristic-containing-measurement.
 */
public enum Sensor {
  IR_TEMPERATURE(UUID_IRT_SERV, UUID_IRT_DATA, UUID_IRT_CONF) {
    @Override
    public Point3D convert(final byte [] value) {

      /*
       * The IR Temperature sensor produces two measurements; Object ( AKA target or IR) Temperature, and Ambient ( AKA die ) temperature.
       * 
       * Both need some conversion, and Object temperature is dependent on Ambient temperature.
       * 
       * They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes) Which means we need to shift the bytes around to get the correct values.
       */

      double ambient = extractAmbientTemperature(value);
      double target = extractTargetTemperature(value, ambient);
      return new Point3D(ambient, target, 0);
    }

    private double extractAmbientTemperature(byte [] v) {
      int offset = 2;
      return shortUnsignedAtOffset(v, offset) / 128.0;
    }

    private double extractTargetTemperature(byte [] v, double ambient) {
      Integer twoByteValue = shortSignedAtOffset(v, 0);

      double Vobj2 = twoByteValue.doubleValue();
      Vobj2 *= 0.00000015625;

      double Tdie = ambient + 273.15;

      double S0 = 5.593E-14; // Calibration factor
      double a1 = 1.75E-3;
      double a2 = -1.678E-5;
      double b0 = -2.94E-5;
      double b1 = -5.7E-7;
      double b2 = 4.63E-9;
      double c2 = 13.4;
      double Tref = 298.15;
      double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * pow((Tdie - Tref), 2));
      double Vos = b0 + b1 * (Tdie - Tref) + b2 * pow((Tdie - Tref), 2);
      double fObj = (Vobj2 - Vos) + c2 * pow((Vobj2 - Vos), 2);
      double tObj = pow(pow(Tdie, 4) + (fObj / S), .25);

      return tObj - 273.15;
    }
  },

  ACCELEROMETER(UUID_ACC_SERV, UUID_ACC_DATA, UUID_ACC_CONF) {
  	@Override
  	public Point3D convert(final byte[] value) {
  		/*
  		 * The accelerometer has the range [-2g, 2g] with unit (1/64)g.
  		 * 
  		 * To convert from unit (1/64)g to unit g we divide by 64.
  		 * 
  		 * (g = 9.81 m/s^2)
  		 * 
  		 * The z value is multiplied with -1 to coincide with how we have arbitrarily defined the positive y direction. (illustrated by the apps accelerometer
  		 * image)
  		 */
  		Integer x = (int) value[0];
  		Integer y = (int) value[1];
  		Integer z = (int) value[2] * -1;

  		return new Point3D(x / 64.0, y / 64.0, z / 64.0);
  	}
  },

  HUMIDITY(UUID_HUM_SERV, UUID_HUM_DATA, UUID_HUM_CONF) {
    @Override
    public Point3D convert(final byte[] value) {
      int a = shortUnsignedAtOffset(value, 2);
      // bits [1..0] are status bits and need to be cleared according
      // to the user guide, but the iOS code doesn't bother. It should
      // have minimal impact.
      a = a - (a % 4);

      return new Point3D((-6f) + 125f * (a / 65535f), 0, 0);
    }
  },

  MAGNETOMETER(UUID_MAG_SERV, UUID_MAG_DATA, UUID_MAG_CONF) {
    @Override
    public Point3D convert(final byte [] value) {
      Point3D mcal = MagnetometerCalibrationCoefficients.INSTANCE.val;
      // Multiply x and y with -1 so that the values correspond with the image in the app
      float x = shortSignedAtOffset(value, 0) * (2000f / 65536f) * -1;
      float y = shortSignedAtOffset(value, 2) * (2000f / 65536f) * -1;
      float z = shortSignedAtOffset(value, 4) * (2000f / 65536f);
      
			return new Point3D(x - mcal.x, y - mcal.y, z - mcal.z);
    }
  },

  GYROSCOPE(UUID_GYR_SERV, UUID_GYR_DATA, UUID_GYR_CONF, (byte)7) {
    @Override
    public Point3D convert(final byte [] value) {

      float y = shortSignedAtOffset(value, 0) * (500f / 65536f) * -1;
      float x = shortSignedAtOffset(value, 2) * (500f / 65536f);
      float z = shortSignedAtOffset(value, 4) * (500f / 65536f);
      
      return new Point3D(x,y,z);      
    }
  },

  BAROMETER(SensorTag.UUID_BAR_SERV, SensorTag.UUID_BAR_DATA, SensorTag.UUID_BAR_CONF) {
    @Override
    public Point3D convert(final byte [] value) {

      List<Integer> barometerCalibrationCoefficients = BarometerCalibrationCoefficients.INSTANCE.barometerCalibrationCoefficients;
      if (barometerCalibrationCoefficients == null) {
        Log.w("Custom", "Data notification arrived for barometer before it was calibrated.");
        return new Point3D(0,0,0);
      }

      final int[] c; // Calibration coefficients
      final Integer t_r; // Temperature raw value from sensor
      final Integer p_r; // Pressure raw value from sensor
      final Double S; // Interim value in calculation
      final Double O; // Interim value in calculation
      final Double p_a; // Pressure actual value in unit Pascal.

      c = new int[barometerCalibrationCoefficients.size()];
      for (int i = 0; i < barometerCalibrationCoefficients.size(); i++) {
        c[i] = barometerCalibrationCoefficients.get(i);
      }

      t_r = shortSignedAtOffset(value, 0);
      p_r = shortUnsignedAtOffset(value, 2);

      S = c[2] + c[3] * t_r / pow(2, 17) + ((c[4] * t_r / pow(2, 15)) * t_r) / pow(2, 19);
      O = c[5] * pow(2, 14) + c[6] * t_r / pow(2, 3) + ((c[7] * t_r / pow(2, 15)) * t_r) / pow(2, 4);
      p_a = (S * p_r + O) / pow(2, 14);

      return new Point3D(p_a,0,0);
    }
  },

  SIMPLE_KEYS(UUID_KEY_SERV, UUID_KEY_DATA, null) {
    @Override
    public SimpleKeysStatus convertKeys(final byte [] value) {
      /*
       * The key state is encoded into 1 unsigned byte. bit 0 designates the right key. bit 1 designates the left key. bit 2 designates the side key.
       */
      Integer encodedInteger = (int) value[0];

      return SimpleKeysStatus.values()[encodedInteger % 4];
    }
  };

  /**
   * Gyroscope, Magnetometer, Barometer, IR temperature all store 16 bit two's complement values in the awkward format LSB MSB, which cannot be directly parsed
   * as getIntValue(FORMAT_SINT16, offset) because the bytes are stored in the "wrong" direction.
   * 
   * This function extracts these 16 bit two's complement values.
   * */
  private static Integer shortSignedAtOffset(byte[] c, int offset) {
    Integer lowerByte = (int) c[offset] & 0xFF; 
    Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
    return (upperByte << 8) + lowerByte;
  }

  private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
    Integer lowerByte = (int) c[offset] & 0xFF; 
    Integer upperByte = (int) c[offset+1] & 0xFF; // // Interpret MSB as signed
    return (upperByte << 8) + lowerByte;
  }

  public void onCharacteristicChanged(BluetoothGattCharacteristic c) {
    throw new UnsupportedOperationException("Programmer error, the individual enum classes are supposed to override this method.");
  }

  public SimpleKeysStatus convertKeys(byte[] value) {
    throw new UnsupportedOperationException("Programmer error, the individual enum classes are supposed to override this method.");
  }

  public Point3D convert(byte[] value) {
    throw new UnsupportedOperationException("Programmer error, the individual enum classes are supposed to override this method.");
  }

	private final UUID service, data, config;
	private byte enableCode; // See getEnableSensorCode for explanation.
	public static final byte DISABLE_SENSOR_CODE = 0;
	public static final byte ENABLE_SENSOR_CODE = 1;
	public static final byte CALIBRATE_SENSOR_CODE = 2;

	/**
	 * Constructor called by the Gyroscope because he needs a different enable
	 * code.
	 */
  private Sensor(UUID service, UUID data, UUID config, byte enableCode) {
    this.service = service;
    this.data = data;
    this.config = config;
    this.enableCode = enableCode;
  }

  /**
   * Constructor called by all the sensors except Gyroscope
   * */
  private Sensor(UUID service, UUID data, UUID config) {
    this.service = service;
    this.data = data;
    this.config = config;
    this.enableCode = ENABLE_SENSOR_CODE; // This is the sensor enable code for all sensors except the gyroscope
  }

  /**
   * @return the code which, when written to the configuration characteristic, turns on the sensor.
   * */
  public byte getEnableSensorCode() {
    return enableCode;
  }

  public UUID getService() {
    return service;
  }

  public UUID getData() {
    return data;
  }

  public UUID getConfig() {
    return config;
  }

  public static Sensor getFromDataUuid(UUID uuid) {
    for (Sensor s : Sensor.values()) {
      if (s.getData().equals(uuid)) {
        return s;
      }
    }
    throw new RuntimeException("Programmer error, unable to find uuid.");
  }
  
  public static final Sensor[] SENSOR_LIST = {IR_TEMPERATURE, ACCELEROMETER, MAGNETOMETER, GYROSCOPE, HUMIDITY, BAROMETER, SIMPLE_KEYS};
}
