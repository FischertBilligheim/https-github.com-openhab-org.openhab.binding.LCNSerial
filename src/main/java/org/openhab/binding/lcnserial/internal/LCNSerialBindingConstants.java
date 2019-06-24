/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lcnserial.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LCNSerialBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Fischer - Initial contribution
 */
@NonNullByDefault
public class LCNSerialBindingConstants 
{

    private static final String BINDING_ID = "lcnserial";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LCNSERIAL = new ThingTypeUID(BINDING_ID, "lcnActor");

    // List of all Channel ids
    public static final String CHANNEL_OUTPUT1 = "Output1";
    public static final String CHANNEL_OUTPUT2 = "Output2";
    
    public static final String CHANNEL_RELAIS1 = "Relais1";
    public static final String CHANNEL_RELAIS2 = "Relais2";
    public static final String CHANNEL_RELAIS3 = "Relais3";
    public static final String CHANNEL_RELAIS4 = "Relais4";
    public static final String CHANNEL_RELAIS5 = "Relais5";
    public static final String CHANNEL_RELAIS6 = "Relais6";
    public static final String CHANNEL_RELAIS7 = "Relais7";
    public static final String CHANNEL_RELAIS8 = "Relais8";

    public static final String CHANNEL_SHUTTER1 = "Shutter1";
    public static final String CHANNEL_SHUTTER2 = "Shutter2";
    public static final String CHANNEL_SHUTTER3 = "Shutter3";
    public static final String CHANNEL_SHUTTER4 = "Shutter4";    

    // Config parameters
    public static final String CONFIG_PORT   = "port";
    public static final String CONFIG_LCN_ID = "lcn_id";
    
    public static final String CONFIG_SHUTTER1_ENABLED = "Shutter1_enabled";
    public static final String CONFIG_SHUTTER2_ENABLED = "Shutter2_enabled";
    public static final String CONFIG_SHUTTER3_ENABLED = "Shutter3_enabled";
    public static final String CONFIG_SHUTTER4_ENABLED = "Shutter4_enabled";
    
    public static final String CONFIG_SHUTTER1_DRIVETIME = "Shutter1_drivetime";
    public static final String CONFIG_SHUTTER2_DRIVETIME = "Shutter2_drivetime";
    public static final String CONFIG_SHUTTER3_DRIVETIME = "Shutter3_drivetime";
    public static final String CONFIG_SHUTTER4_DRIVETIME = "Shutter4_drivetime";
    
    public static final String CONFIG_SHUTTER1_OVERALLTIME = "Shutter1_overalltime";
    public static final String CONFIG_SHUTTER2_OVERALLTIME = "Shutter2_overalltime";
    public static final String CONFIG_SHUTTER3_OVERALLTIME = "Shutter3_overalltime";
    public static final String CONFIG_SHUTTER4_OVERALLTIME = "Shutter4_overalltime";

}
