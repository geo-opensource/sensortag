/**************************************************************************************************
 Filename:       HCIDefines.java
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ole on 23/04/15.
 */
public class HCIDefines {
    public static final Map<Integer,String> hciErrorCodeStrings;
    static
    {
        hciErrorCodeStrings = new HashMap<Integer,String>();
        hciErrorCodeStrings.put(0x01,"Unknown HCI Command");
        hciErrorCodeStrings.put(0x02,"Unknown Connection Identifier");
        hciErrorCodeStrings.put(0x03,"Hardware Failure");
        hciErrorCodeStrings.put(0x04,"Page Timeout");
        hciErrorCodeStrings.put(0x05,"Authentication Failure");
        hciErrorCodeStrings.put(0x06,"Pin or Key Missing");
        hciErrorCodeStrings.put(0x07,"Memory Capacity Exceeded");
        hciErrorCodeStrings.put(0x08,"Connnection Timeout");
        hciErrorCodeStrings.put(0x09,"Connection Limit Exceeded");
        hciErrorCodeStrings.put(0x0A,"Synchronous Connection Limit To A Device Exceeded");
        hciErrorCodeStrings.put(0x0B,"ACL Connection Already Exists");
        hciErrorCodeStrings.put(0x0C,"Command Disallowed");
        hciErrorCodeStrings.put(0x0D,"Connected Rejected Due To Limited Resources");
        hciErrorCodeStrings.put(0x0E,"Connection Rejected Due To Security Reasons");
        hciErrorCodeStrings.put(0x0F,"Connection Rejected Due To Unacceptable BD_ADDR");
        hciErrorCodeStrings.put(0x10,"Connection Accept Timeout Exceeded");
        hciErrorCodeStrings.put(0x11,"Unsupported Feature Or Parameter Value");
        hciErrorCodeStrings.put(0x12,"Invalid HCI Command Parameters");
        hciErrorCodeStrings.put(0x13,"Remote User Terminated Connection");
        hciErrorCodeStrings.put(0x14,"Remote Device Terminated Connection Due To Low Resources");
        hciErrorCodeStrings.put(0x15,"Remote Device Terminated Connection Due To Power Off");
        hciErrorCodeStrings.put(0x16,"Connection Terminated By Local Host");
        hciErrorCodeStrings.put(0x17,"Repeated Attempts");
        hciErrorCodeStrings.put(0x18,"Pairing Not Allowed");
        hciErrorCodeStrings.put(0x19,"Unknown LMP PDU");
        hciErrorCodeStrings.put(0x1A,"Unsupported Remote Feature / Unsupported LMP Feature");
        hciErrorCodeStrings.put(0x1B,"SCO Offset Rejected");
        hciErrorCodeStrings.put(0x1C,"SCO Interval Rejected");
        hciErrorCodeStrings.put(0x1D,"SCO Air Mode Rejected");
        hciErrorCodeStrings.put(0x1E,"Invalid LMP Parameters / Invalid LL Parameters");
        hciErrorCodeStrings.put(0x1F,"Unspecified Error");
        hciErrorCodeStrings.put(0x20,"Unsupported LMP Parameter Value / Unsupported LL Parameter Value");
        hciErrorCodeStrings.put(0x21,"Role Change Not Allowed");
        hciErrorCodeStrings.put(0x22,"LMP Response Timeout / LL Response Timeout");
        hciErrorCodeStrings.put(0x23,"LMP Error Transaction Collision");
        hciErrorCodeStrings.put(0x24,"LMP PDU Not Allowed");
        hciErrorCodeStrings.put(0x25,"Encryption Mode Not Acceptable");
        hciErrorCodeStrings.put(0x26,"Link Key Cannot Be Changed");
        hciErrorCodeStrings.put(0x27,"Requested QoS Not Supported");
        hciErrorCodeStrings.put(0x28,"Instant Passed");
        hciErrorCodeStrings.put(0x29,"Pairing With Unit Key Not Supported");
        hciErrorCodeStrings.put(0x2A,"Differemt Tramsaction Collision");
        hciErrorCodeStrings.put(0x2C,"QoS Unacceptable Parameter");
        hciErrorCodeStrings.put(0x2D,"QoS Rejected");
        hciErrorCodeStrings.put(0x2E,"Channel Assessment Not Supported");
        hciErrorCodeStrings.put(0x2F,"Insufficient Security");
        hciErrorCodeStrings.put(0x30,"Parameter Out of Mandatory Range");
        hciErrorCodeStrings.put(0x32,"Role Switch Pending");
        hciErrorCodeStrings.put(0x34,"Reserved Slot Violation");
        hciErrorCodeStrings.put(0x35,"Role Switch Failed");
        hciErrorCodeStrings.put(0x36,"Extended Inquiry Response Too Large");
        hciErrorCodeStrings.put(0x37,"Simple Pairing Not Supported By Host");
        hciErrorCodeStrings.put(0x38,"Host Busy-Pairing");
        hciErrorCodeStrings.put(0x39,"Connection Rejected Due To No Suitable Channel Found");
        hciErrorCodeStrings.put(0x3A,"Controller Busy");
        hciErrorCodeStrings.put(0x3B,"Unacceptable Connection Parameters");
        hciErrorCodeStrings.put(0x3C,"Directed Advertising Timeout");
        hciErrorCodeStrings.put(0x3D,"Connection Terminated Due To MIC Failure");
        hciErrorCodeStrings.put(0x3E,"Connection Failed To Be Established");
        hciErrorCodeStrings.put(0x3F,"MAC Connection Failed");
        hciErrorCodeStrings.put(0x40,"Coarse Clock Adjustment Rejected But Will Try to Adjust Using Clock Dragging");


    }
}
