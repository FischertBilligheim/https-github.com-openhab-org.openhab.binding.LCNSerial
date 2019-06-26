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
package org.openhab.binding.lcnserial.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lcnserial.internal.LCNSerialConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;


//=======================================================
/** class LCNSerialHandler
 *
 * is the base class for all event-handlers
 */
//======================================================= 
public class LCNSerialHandler extends BaseThingHandler
{
	private final Logger logger = LoggerFactory.getLogger(LCNSerialHandler.class); 

	public LCNSerialConfiguration deviceConfig = null;
	
    public static SerialPort serialPort = null;
	public static LCNPort    myLCNPort  = null;
	
    public static String     mylcnIdStr = null;
    
	private static int instCount = 0;
	
	public int lcnAdress;
	
	//==================================
	/**
	 * Constructor
	 *
	 * @param	thing	Thing from OpenHAB
	 */
	//===================================
    public LCNSerialHandler(Thing thing) 
    {
        super(thing);
    }

    
	/**==================================
	  * void initialize()
	  *
	  *=================================== */
//    @Override
    public void initialize() 
    {
        instCount ++;	// Increment InstanceCounter
        
        Boolean b = LoadConfig();
        
        if (b == false) 
        {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Parameter must be set!");
            return;
        }
               
        try 
        {
			initPort( deviceConfig.port, this);
		} 
        catch (Exception e1) 
        {
        	logger.error("Port could not be initialized {} ",deviceConfig.port, e1 );
		}

        if (myLCNPort == null)
        	return;
        
		try 
		{
			myLCNPort.Read_Modul_All(lcnAdress);
		} 
		catch (InterruptedException | IOException e) 
		{
        	logger.error("LCN-Bus could be read out {}", e );
		}
    
		logger.info("Update ThingStatus LCNSerial: ok");
		
        updateStatus(ThingStatus.ONLINE);
        
                   
    }

    /**============================================================
     * Boolean LoadConfig()
     *
     * Loads the Thing-configuration data
     * 
     * @return:		true:	 everything fine  
     * @return:		false: 	Thing-configuration not ok
     *============================================================*/
	public Boolean LoadConfig()
	{		       
        deviceConfig = getConfigAs(LCNSerialConfiguration.class);
        
        if (deviceConfig.port.isEmpty())
        	return false;
        
        lcnAdress = deviceConfig.lcn_id;      
        
        return true;		
	}
	
    /**============================================================
     * Initializes the serial port to LCN-PK-Modul
     *
     * 
     *
     *============================================================*/
    private void initPort( String port, LCNSerialHandler handler) throws Exception
    {
    	if (serialPort != null)
    	{
    		myLCNPort.SetLCNSerialHandler(lcnAdress, handler);
    		return;
    	}
  	
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);

		if (portIdentifier.isCurrentlyOwned()) 
		{
        	logger.error("Port is currently owned ");
        	return;
		}

		CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000); // timeout 2 s.
		if (!(commPort instanceof SerialPort)) 
		{
        	logger.error("Port is not a serial port {} ",commPort.getName());
		}
			
		
		serialPort = (SerialPort) commPort;
		
		// 9600 bit/s, 8 bits, stop bit length 1, no parity bit   
	    serialPort.setSerialPortParams(9600, 8,1,0);
	    
	    serialPort.disableReceiveTimeout();
	        
	    serialPort.setRTS(false);
	    serialPort.setDTR(true);	    
	    
	    //================================================
	    //
	    //================================================
		myLCNPort = new LCNPort(serialPort);
			
    	myLCNPort.SetLCNSerialHandler(lcnAdress, handler);
    		
	    SerialReadThread t = new SerialReadThread(myLCNPort);
	    Thread readThread = new Thread(t);
	    readThread.start();
	    
    	logger.info("Init Port done");
	    
    }
    
   
    //==================================
    // void dispose()
    //
    //===================================
    @Override
    public void dispose() 
    {	

    	if (instCount-- > 0)	// Close port only if no instance is there..
    		return;
    	
    	serialPort.close();
    }
    

    /**=================================================
     * void SetValue1(int address, int value)
     *
     * @param address	address-id of the LCN-modul
     * @param value 	value to be written to Output1
     *=================================================*/
    public void SetValue1 (int address, int value)
    {
			try 
			{
            	logger.info(String.format("SetValue1:  Address: %d, Value: %d", address, value ));
            	
				myLCNPort.SetOut1(address, value);
            	logger.info("Done");
				
			} 
			catch (InterruptedException | IOException e) 
			{
	        	logger.error("Exception on SetValue1() {} ", e );
			}
    }
  
    /**==================================================
     * void SetValue2 (int address, int value)
     *
     * @param address	address-id of the LCN-modul
     * @param value		value to be written to Output2
    //==================================================*/
    public void SetValue2 (int address, int value)
    {

				try 
				{
	            	logger.info(String.format("SetValue2:  Address: %d, Value: %d", address, value ));
	            	
					myLCNPort.SetOut2(address, value);
	            	logger.info("Done");
				} 
				catch (InterruptedException | IOException e) 
				{
		        	logger.error("Exception on SetValue2() {} ", e );
				}

    }
    
    /**============================================
     * void SetBit(int address, int bit)
     *
     * @param address	address-id of the LCN-modul
     * @param bit		bit-number of Relais-modul	
     *============================================*/
    public void SetBit (int address, int bit)
    {
		try 
		{
        	logger.info(String.format("SetBit:  Address: %d, Bit: %d", address, bit ));
        	
			myLCNPort.SetBit(address, bit);
        	logger.info("Done");
		} 
		catch (InterruptedException | IOException e) 
		{
        	logger.error("Exception on SetBit() {} ", e );
		}				
    }
    
    /**==================================================
     * void ResetBit(int address, int bit)
     *
     *
     * @param	address		address-id of the LCN-modul
     * @param 	bit			bit-number of Relais-modul
     *==================================================*/
    public void ResetBit(int address, int bit)
    {
			try 
			{
	        	logger.info(String.format("ResetBit:  Address: %d, Bit: %d", address, bit ));
	        	
				myLCNPort.ResetBit(address, bit);
            	logger.info("Done");
			} 
			catch (InterruptedException | IOException e) 
			{
	        	logger.error("Exception on ResetBit() {} ", e );
			}
    }

    /**================================================================
     * void SendRelaisBitState(ChannelUID channelUID, int val)
     *
     * @param	channelUID		UID of channel of thing
     * @param	val				state of bit ( 1 or 0)
     *
     *================================================================*/
    public void SendRelaisBitState(ChannelUID channelUID, int val)
    {
    	logger.info(String.format("SendRelaisBitState:  ChannelUID: %s, Value: %d", channelUID, val ));
    	
		if (val == 1)
			updateState(channelUID, OnOffType.ON);
		else
			updateState(channelUID, OnOffType.OFF);
    }
    
    /**==========================================================
     * void SendOutputState(ChannelUID channelUID, int val)
     *
     * @param	channelUID		UID of channel of thing
     * @param	val				value (Percent type)
     *
     *==========================================================*/
    public void SendOutputState(ChannelUID channelUID, int val)
    {
    	logger.info(String.format("SendOutputState:  ChannelUID: %s, Value: %d", channelUID, val ));
    	
		PercentType currentVal = PercentType.ZERO;
		
		currentVal = new PercentType(val);   	       
		updateState(channelUID, currentVal);
    }
    
    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) 
    {    

    }
    


}
