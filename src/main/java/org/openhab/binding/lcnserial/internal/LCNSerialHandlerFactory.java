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

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;

import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;

import org.openhab.binding.lcnserial.internal.handler.LCNSerialEventHandlers;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * The {@link LCNSerialHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Fischer - Initial contribution
 */


@Component(service = ThingHandlerFactory.class, configurationPid = "binding.lcnserial")

//===========================================
/**
 * Handler Factory for the LCNSerial-Binding
 *
*/
//===========================================
@NonNullByDefault
public class LCNSerialHandlerFactory extends BaseThingHandlerFactory 
{

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(LCNSerialBindingConstants.THING_TYPE_LCNSERIAL);

    private @NonNullByDefault({}) SerialPortManager serialPortManager;
    
    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) 
    {
        this.serialPortManager = serialPortManager;
        
        SerialPortIdentifier serPortIdentifier = this.serialPortManager.getIdentifier("COM3");
        String owner = serPortIdentifier.getCurrentOwner();
    }
    
       
    //==========================================================
    /**
     *  boolean supportsThingType(ThingTypeUID thingTypeUID)  
     *
     * @param thingTypeUID		TypeUID to be tested
    */
    //==========================================================
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) 
    {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    //==========================================================
    /**
     *  ThingHandler createHandler(Thing thing) 
     *
     * @param	thing			Thing to create the handler for
     * @return	ThimgHandler
    */
    ////==========================================================
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) 
    {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(LCNSerialBindingConstants.THING_TYPE_LCNSERIAL)) 
        {
            return new LCNSerialEventHandlers(thing); 
        }

        return null;
    }
}
