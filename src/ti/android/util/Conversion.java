/**************************************************************************************************
  Filename:       Conversion.java
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
package ti.android.util;

import java.util.Formatter;

/* This class encapsulates utility functions */
public class Conversion {

  public static byte loUint16(short v) {
    return (byte) (v & 0xFF);
  }

  public static byte hiUint16(short v) {
    return (byte) (v >> 8);
  }

  public static short buildUint16(byte hi, byte lo) {
    return (short) ((hi << 8) + (lo & 0xff));
  }

  public static String BytetohexString(byte[] b, int len) {
    StringBuilder sb = new StringBuilder(b.length * (2 + 1));
    Formatter formatter = new Formatter(sb);

    for (int i = 0; i < len; i++) {
      if (i < len - 1)
        formatter.format("%02X:", b[i]);
      else
        formatter.format("%02X", b[i]);

    }
    formatter.close();

    return sb.toString();
  }

  static String BytetohexString(byte[] b, boolean reverse) {
    StringBuilder sb = new StringBuilder(b.length * (2 + 1));
    Formatter formatter = new Formatter(sb);

    if (!reverse) {
      for (int i = 0; i < b.length; i++) {
        if (i < b.length - 1)
          formatter.format("%02X:", b[i]);
        else
          formatter.format("%02X", b[i]);

      }
    } else {
      for (int i = (b.length - 1); i >= 0; i--) {
        if (i > 0)
          formatter.format("%02X:", b[i]);
        else
          formatter.format("%02X", b[i]);

      }
    }
    formatter.close();

    return sb.toString();
  }

  // Convert hex String to Byte
  public static int hexStringtoByte(String sb, byte[] results) {

    int i = 0;
    boolean j = false;

    if (sb != null) {
      for (int k = 0; k < sb.length(); k++) {
        if (((sb.charAt(k)) >= '0' && (sb.charAt(k) <= '9')) || ((sb.charAt(k)) >= 'a' && (sb.charAt(k) <= 'f'))
            || ((sb.charAt(k)) >= 'A' && (sb.charAt(k) <= 'F'))) {
          if (j) {
            results[i] += (byte) (Character.digit(sb.charAt(k), 16));
            i++;
          } else {
            results[i] = (byte) (Character.digit(sb.charAt(k), 16) << 4);
          }
          j = !j;
        }
      }
    }
    return i;
  }

  public static boolean isAsciiPrintable(String str) {
    if (str == null) {
      return false;
    }
    int sz = str.length();
    for (int i = 0; i < sz; i++) {
      if (isAsciiPrintable(str.charAt(i)) == false) {
        return false;
      }
    }
    return true;
  }

  private static boolean isAsciiPrintable(char ch) {
    return ch >= 32 && ch < 127;
  }

}