package org.openhab.binding.lcnserial.internal.handler;

import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;


//===================================================================
/**
 * LCNSerialEventHandlers extends LCNSerialHandler
 *
 *
 */
//===================================================================
public class LCNSerialEventHandlers extends LCNSerialHandler 
{
	private final Logger logger = LoggerFactory.getLogger(LCNSerialEventHandlers.class); 	
	
	//===================================
	// Some defines
	//
	//===================================
	private static final int cmdShutter_Up   = 0;
	private static final int cmdShutter_Down = 1;
	private static final int cmdShutter_Stop = 2;	
	
	private static final int idShutter1   = 0;	
	private static final int idShutter2   = 1;
	private static final int idShutter3   = 2;	
	private static final int idShutter4   = 3;
	

	private boolean	[] Shutter_enabled      	= { false,false,false,false };
	private int		[] Shutter_driveTimes   	= { 0,0,0,0 };					// Time until window is completely closed
	private int		[] Shutter_overallTimes 	= { 0,0,0,0 };					// Time until shutter stops at the end
	private int		[] Shutter_endPosPercent 	= { 0,0,0,0 };					// End-Pos in Percent overall ( > 100%)
	
	private int		[] Shutter_actValues    = { 0,0,0,0 };
	private long	[] Shutter_startTimes   = { 0,0,0,0 };
	private int		[] Shutter_directions   = { cmdShutter_Stop, cmdShutter_Stop, cmdShutter_Stop, cmdShutter_Stop };

	private Timer	[] Shutter_Timers       = { null, null, null, null };
	
	
	//=================================================
	/** 
	 *  Constructor
	 *  
	 *  Constructor
	 * @param thing		OpenHAB-Thing
	 */
	// =================================================
	public LCNSerialEventHandlers(Thing thing) 
	{
		super(thing);	
	}

	//====================================================
	/** 
	 *  Initialization of data structures 
	 * 
	 */
	// =====================================================
    @Override
    public void initialize() 
    {
    	super.initialize();
    	
    	Shutter_enabled[idShutter1] = deviceConfig.Shutter1_enabled;
    	Shutter_enabled[idShutter2] = deviceConfig.Shutter2_enabled;
    	Shutter_enabled[idShutter3] = deviceConfig.Shutter3_enabled;
    	Shutter_enabled[idShutter4] = deviceConfig.Shutter4_enabled;
    	
    	
    	if ( Shutter_enabled[idShutter1] == true)
    	{
        	Shutter_driveTimes[idShutter1]     = deviceConfig.Shutter1_drivetime;
        	Shutter_overallTimes[idShutter1]   = deviceConfig.Shutter1_overalltime;
        	Shutter_endPosPercent[idShutter1]  = (deviceConfig.Shutter1_overalltime * 100 ) / deviceConfig.Shutter1_drivetime;
    		Shutter_Timers[idShutter1]         = new Timer(true); // timer for shutter1        	
    	}
    	 	
    	if ( Shutter_enabled[idShutter2] == true)
    	{
        	Shutter_driveTimes[idShutter2] 		= deviceConfig.Shutter2_drivetime;
        	Shutter_overallTimes[idShutter2] 	= deviceConfig.Shutter2_overalltime;
        	Shutter_endPosPercent[idShutter2] 	= (deviceConfig.Shutter2_overalltime * 100 ) / deviceConfig.Shutter2_drivetime;
    		Shutter_Timers[idShutter2] 			= new Timer(true); // timer for shutter2        	
    	}
    	
    	if ( Shutter_enabled[idShutter3] == true)
    	{
        	Shutter_driveTimes[idShutter3] 		= deviceConfig.Shutter3_drivetime;
        	Shutter_overallTimes[idShutter3] 	= deviceConfig.Shutter3_overalltime;        	
        	Shutter_endPosPercent[idShutter3] 	= (deviceConfig.Shutter3_overalltime * 100 ) / deviceConfig.Shutter3_drivetime;
    		Shutter_Timers[idShutter3]			= new Timer(true); // timer for shutter3       
    	}


    	if ( Shutter_enabled[idShutter4] == true)
    	{
	    	Shutter_driveTimes[idShutter4] 		= deviceConfig.Shutter4_drivetime;
	     	Shutter_overallTimes[idShutter4] 	= deviceConfig.Shutter4_overalltime;    	
	     	Shutter_endPosPercent[idShutter4] 	= (deviceConfig.Shutter4_overalltime * 100 ) / deviceConfig.Shutter4_drivetime; 
	 		Shutter_Timers[idShutter4] 			= new Timer(true); // timer for shutter4
    	}
    }
	
    //================================================================================
    /**
     * Central Command-Handler 
     *
     * @param channelUID		UID of the channel
     * @param command			command to handle
     */
    //================================================================================
    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) 
    {    
    	String strChannel = channelUID.getId();
    	
    	logger.info(String.format("Handle Command:   ModulNr: %d  Channel: %s  Command: %s",this.lcnAdress,  strChannel, command.toString()));
    	
    	//------------------------------------------------------------
    	//  command: ON / OFF
    	//
    	//------------------------------------------------------------
    	if (command instanceof OnOffType)
    	{
    		handleCommand_OnOff(strChannel, command);
    		return;
    	}
 
    	//------------------------------------------------------------
    	//  command: UP / DOWN
    	//
    	//------------------------------------------------------------
    	if (command instanceof UpDownType)
    	{
    		handleCommand_UpDown(strChannel, command, true);
    		return;
    	}

    	//------------------------------------------------------------
    	//  command: STOP/MOVE
    	//
    	//------------------------------------------------------------
    	if (command instanceof StopMoveType)
    	{
    		handleCommand_StopMove(strChannel, command, true);    		
			handleCommand_Refresh (strChannel, channelUID);
			
    		return;
    	}
    	
    	//-------------------------------------------------------------
    	//  command: PercentType
    	//
    	//-------------------------------------------------------------
    	if (command instanceof PercentType)
    	{
			int val = ((PercentType) command).intValue();
			
			switch (strChannel)
			{
    			case "Output1":
    	    		SetValue1(lcnAdress, val); 	    		
    				break;
    				
    			case "Output2":
    	    		SetValue2(lcnAdress, val);
    				break; 
    				
    			case "Shutter1":
    				handleShutterPercent(strChannel,idShutter1,val);
    				break;
    				
	 			case "Shutter2": 
    				handleShutterPercent(strChannel,idShutter2,val);
	 	 	    	break;
	 				
	 			case "Shutter3":   
    				handleShutterPercent(strChannel,idShutter3,val);
	 	 	    	break;
	 				
	 			case "Shutter4":	 				
    				handleShutterPercent(strChannel,idShutter4,val);
 	 	    	    break;
			}

    		SendOutputState(channelUID, val);   
    		
			return;
    	}
    	
    	//------------------------------------------------------------
    	//  command: REFRESH  (Read actual value)
    	//
    	//------------------------------------------------------------
    	if (command == REFRESH)
    	{
    		handleCommand_Refresh(strChannel, channelUID);
    		return;
    	} 
      
    }

    //===============================================================================
    /**
     * Handle Up/Down commands
     * 
     * @param strChannel		string of the channel
     * @param command			command to execute
     * @param withTimer			Command is called by LCN-Bus handler:
     *                          Start a timer for the rollershutter
     */
    //===============================================================================
    private void handleCommand_UpDown(String strChannel, Command command, boolean withTimer)
    {
		if (command.equals(UpDownType.UP))
		{
	    	updateState(strChannel, UpDownType.UP);
	    	
			switch (strChannel)
			{
    			case "Shutter1":  					
    					Shutter_startTimes[idShutter1] = System.currentTimeMillis();
    					Shutter_directions[idShutter1] = cmdShutter_Up;
    					
    					if (withTimer)
    						StartTimer(strChannel, idShutter1, CalcTimeFromDiff(idShutter1,Shutter_actValues[idShutter1])+5000, true );  // OneShot Timer - if the shutter comes to start-pos
    					
    					SetBit(lcnAdress,2);
    					SetBit(lcnAdress,1);
    				 break;
    				
    			case "Shutter2":   				
						Shutter_startTimes[idShutter2] = System.currentTimeMillis();
						Shutter_directions[idShutter2] = cmdShutter_Up; 
    					
						if (withTimer)
    						StartTimer(strChannel, idShutter1, CalcTimeFromDiff(idShutter1,Shutter_actValues[idShutter2])+5000, true );  // OneShot Timer - if the shutter comes to start-pos
    					
    					SetBit(lcnAdress,4);
    					SetBit(lcnAdress,3);
    				break;	
    				
    			case "Shutter3":    				
						Shutter_startTimes[idShutter3] = System.currentTimeMillis();
						Shutter_directions[idShutter3] = cmdShutter_Up; 
    					
						if (withTimer)
    						StartTimer(strChannel, idShutter1, CalcTimeFromDiff(idShutter1,Shutter_actValues[idShutter3])+5000, true );  // OneShot Timer - if the shutter comes to start-pos
    					
    					SetBit(lcnAdress,6);
    					SetBit(lcnAdress,5);
    				break;
    				
    			case "Shutter4":    				
						Shutter_startTimes[idShutter4] = System.currentTimeMillis();
						Shutter_directions[idShutter4] = cmdShutter_Up; 
    					
						if (withTimer)
    						StartTimer(strChannel, idShutter1, CalcTimeFromDiff(idShutter1,Shutter_actValues[idShutter4])+5000, true );  // OneShot Timer - if the shutter comes to start-pos
    					
    					SetBit(lcnAdress,8);
    					SetBit(lcnAdress,7);
    				break;			
			}
		}
		
		if (command.equals(UpDownType.DOWN))
		{
	    	updateState(strChannel, UpDownType.DOWN);

			switch (strChannel)
			{
    			case "Shutter1":    					
    					Shutter_startTimes[idShutter1] = System.currentTimeMillis();
    					Shutter_directions[idShutter1] = cmdShutter_Down;
    					
    					if (withTimer)
    					{
    						int diff = Shutter_endPosPercent[idShutter1] - Shutter_actValues[idShutter1];   						
    						StartTimer(strChannel, idShutter1, CalcTimeFromDiff(idShutter1,diff)+5000, true );  // OneShot Timer - if the shutter comes to end-pos
    					}
    					
 	 	    	   		ResetBit(lcnAdress,2);
 	 	    	   		SetBit(lcnAdress,1);
    				break;	
    				
    			case "Shutter2":
						Shutter_startTimes[idShutter2] = System.currentTimeMillis();
						Shutter_directions[idShutter2] = cmdShutter_Down;
						
    					if (withTimer)
    					{
    						int diff = Shutter_endPosPercent[idShutter2] - Shutter_actValues[idShutter2];   						
    						StartTimer(strChannel, idShutter2, CalcTimeFromDiff(idShutter2,diff)+5000, true );  // OneShot Timer - if the shutter comes to end-pos
    					}    					
	 	    	   		ResetBit(lcnAdress,4);
	 	    	   		SetBit(lcnAdress,3);
    				break;	
    				
    			case "Shutter3":
						Shutter_startTimes[idShutter3] = System.currentTimeMillis();
						Shutter_directions[idShutter3] = cmdShutter_Down;
						
    					if (withTimer)
    					{
    						int diff = Shutter_endPosPercent[idShutter3] - Shutter_actValues[idShutter3];   						
    						StartTimer(strChannel, idShutter3, CalcTimeFromDiff(idShutter3,diff)+5000, true );  // OneShot Timer - if the shutter comes to end-pos
    					}    					
	 	    	   		ResetBit(lcnAdress,6);
	 	    	   		SetBit(lcnAdress,5);
    				break;
    				
    			case "Shutter4":
						Shutter_startTimes[idShutter4] = System.currentTimeMillis();
						Shutter_directions[idShutter4] = cmdShutter_Down;
						
    					if (withTimer)
    					{
    						int diff = Shutter_endPosPercent[idShutter4] - Shutter_actValues[idShutter4];   						
    						StartTimer(strChannel, idShutter4, CalcTimeFromDiff(idShutter4,diff)+5000, true );  // OneShot Timer - if the shutter comes to end-pos
    					}    					
	 	    	   		ResetBit(lcnAdress,8);
	 	    	   		SetBit(lcnAdress,7);
    				break;			
			}
		}		
			
    }
    
    // ==========================================================
    /**
     //  int CalcTimeFromDiff(int Shutter_Id, int diff)
     //  
     //  
     // @param Shutter_Id
     // @param diff
     // @return drivetime for the diff-range
     */
    // ===========================================================
    private int CalcTimeFromDiff(int Shutter_Id, int diff)
    {
    	long wrk = 0;
    	
    	wrk = (long)diff * (long)Shutter_overallTimes[Shutter_Id] * 1000; // in ms
    	wrk /= Shutter_endPosPercent[Shutter_Id];
    	
    	return (int) wrk;      	
    	
/*
    	int diffTime = Shutter_driveTimes[Shutter_Id]*diff*10;   	
    	return diffTime;
*/
    	
    }
    
    //===================================================================
    /**
     * Handler for shutter percentage-commands
     *
     * @param strChannel		string of the channel
     * @param Shutter_id		1..4 shutter
     * @param val				new percentage value
     *
    */
    //=================================================================== 
    private void handleShutterPercent(String strChannel,int Shutter_id, int val)
    {
    	int diffTime = CalculateDriveTime(Shutter_id, val);
    	  	  			
		if (diffTime == 0)
			return;

		//---------------------------------------------------------------------------
		// Start a one Shot timer, which stops the shutter after the calculated time
		//---------------------------------------------------------------------------
		StartTimer(strChannel, Shutter_id, Math.abs(diffTime), false );  
		
		//-------------------------------------------------
		// Start driving the shutter, no own timer
		//-------------------------------------------------
    	if (diffTime < 0)	// shutter up
    		handleCommand_UpDown(strChannel, UpDownType.UP, false);
    	else			// shutter down
    		handleCommand_UpDown(strChannel, UpDownType.DOWN, false);
    }

    
    // ===================================================================
    /**
     *  Calculate the needed time to drive
     *
     *   diff           x
     *   ----    =  ----------          x =(Drivetime * diff) / 100 (s)
     *   100         Drivetime          x =(Drivetime * diff) * 10  (ms)    
     *
     * @param	Shutter_id		1..4
     */
    // ===================================================================
    int CalculateDriveTime(int Shutter_id, int newVal)
    {
    	int diffTime  = 0;
    	int diff = 0;
    	long wrk = 0;
    	
    	diff = newVal - Shutter_actValues[Shutter_id];			// 0 .. 100+x
    	Shutter_actValues[Shutter_id] = newVal;	
    	
    	wrk = (long)diff * (long)Shutter_overallTimes[Shutter_id] * 1000; // in ms
    	wrk /= Shutter_endPosPercent[Shutter_id];
    	
    	diffTime = (int) wrk;    
    	
/*
    	if (Shutter_actValues[Shutter_id] <= 100)
    	{
        	diff = newVal - Shutter_actValues[Shutter_id];			// 0..100
        	Shutter_actValues[Shutter_id] = newVal;					
        	diffTime = Shutter_driveTimes[Shutter_id]*diff*10;		// in ms    		
    	}
    	else
    	{
    		
    	}
*/   	
    	return diffTime;
    }
    
    //============================================================================================
    /**
     * Start a timer for driving a shutter
     *
     * @param strChannel		String of the channel
     * @param Shutter_id		Shutter (1..4)
     * @param diffTime 			Time in msec to drive
     * @param updatePos			Shall the pos be updated at the end?
     */
    //============================================================================================
    private void StartTimer(String strChannel , int Shutter_id, int diffTime, boolean updatePos)
    {
    	Timer t = Shutter_Timers[Shutter_id];
    	
    	t.schedule(new LCNShutterTimerTask(this, strChannel, Shutter_id, updatePos), diffTime);    	
    }
    
   
    //==========================================================================================
    /**
    * Handler for stopping a shutter
    *
    * @param strChannel		String of the channel
    * @param command		Command to be handled
    * @param bUpdatePos		Boolean: Shall the pos be updated (true/false)
    */
    // ==========================================================================================
    public void handleCommand_StopMove(String strChannel, Command command, boolean bUpdatePos)
    {	
		int diffValue = 0;
		
		if (command.equals(StopMoveType.STOP))
		{
			if (bUpdatePos)
				diffValue = CalculateDiffPos(strChannel);
			
			switch (strChannel)
			{
    			case "Shutter1":
						ResetBit(lcnAdress,1);    				
    					HandleShutterStop(idShutter1, diffValue);											
    				break;
    				
    			case "Shutter2":
  	 	    	   		ResetBit(lcnAdress,3);
						HandleShutterStop(idShutter2, diffValue);  	 	    	   		
    				break;	
    				
    			case "Shutter3":
  	 	    	   		ResetBit(lcnAdress,5);
						HandleShutterStop(idShutter3, diffValue);  	 	    	   		
    				break;
    				
    			case "Shutter4":
  	 	    	   		ResetBit(lcnAdress,7);
						HandleShutterStop(idShutter4, diffValue);  	 	    	   		  	 	    	   		
    				break;			
			}
			
		}
		
		if (command.equals(StopMoveType.MOVE))
		{
			// updateState(strChannel, StopMoveType.MOVE);
			
   	   		// TODO ??
		}		
			
    }
  
    //==================================================================
    /** 
     *  Stop a shutter, use the diff-Value
     *  
     * @param ShutterId 
     * @param diffValue
     */
    //===================================================================== 
    void HandleShutterStop(int ShutterId, int diffValue)
    {
		if (Shutter_directions[ShutterId] == cmdShutter_Up)   // Shutter up
		{									
			Shutter_actValues[ShutterId] -= diffValue;
			if (Shutter_actValues[ShutterId] < 0)
				Shutter_actValues[ShutterId] = 0;
			
		}
		else  // Shutter down
		{							
			Shutter_actValues[ShutterId] += diffValue;
//			if (Shutter_actValues[ShutterId] > 100)
//				Shutter_actValues[ShutterId] = 100;
			
			if (Shutter_actValues[ShutterId] > Shutter_endPosPercent[ShutterId])
				Shutter_actValues[ShutterId] = Shutter_endPosPercent[ShutterId];
		}
	
		Shutter_directions[ShutterId] = cmdShutter_Stop;
    	
		logger.info(String.format("Handle Shutter Stop:   ModulNr: %d  Shutter: %d   DiffValue: %d  ActValue: %d",
                this.lcnAdress, ShutterId , diffValue, Shutter_actValues[ShutterId] ));
  	
    }
    
    //=================================================================================
    /**
     *  Calculate a DiffPos with a diff-Time
     *
     *   x           diffTime
     *   ----    =  ----------          x =(diffTime * 100) / Drivetime (difftime = s)
     *   100         Drivetime          x =(diffTime / 10)  / Drivetime (difftime = ms)
     *   
     * @param 	strChannel		shutter 1..4
     * @return  calculated diff
    */
    //==================================================================================
    int CalculateDiffPos(String strChannel)
    {
		long diffTime = 0;
		long endTime  = System.currentTimeMillis();
		long x = 0;
		
		int id = ChannelIdFromString(strChannel);
		
			diffTime = endTime - Shutter_startTimes[id];
			
//			x = diffTime/10;
//			x = x / Shutter_driveTimes[id];
		
			x = diffTime * Shutter_endPosPercent[id];
			x /= 1000;
			x /= Shutter_overallTimes[id];
			
		return (int)x;
    }
    
    //===========================================
    /** 
     * Retturn the channel-id from a channel-string
     * 
     * 
     * @param strChannel
     * @return	channel-id
     */
    // =========================================
    private int ChannelIdFromString(String strChannel)
    {
    	int ret = 0;
    	
		switch (strChannel)
		{
			case "Shutter1": ret= idShutter1; 	break;				
			case "Shutter2": ret= idShutter2; 	break;					
			case "Shutter3": ret= idShutter3; 	break;				
			case "Shutter4": ret= idShutter4; 	break;			
		}
		
		return ret;
    }
    
    //===================================================================
    /**
     * handler for Command_OnOff
     *
     * @param 	strChannel	string of channel
     * @param 	command		on or off
    */
    //===================================================================
    private void handleCommand_OnOff(String strChannel, Command command)
    {
		if (command.equals(OnOffType.ON))
		{
	    	updateState(strChannel, OnOffType.ON);
	    	   
			switch (strChannel)
			{
    			case "Output1":
    	 	    	   SetValue1(lcnAdress,100);
    				break;	    				
    			case "Output2":
    	 	    	   SetValue2(lcnAdress,100);
    				break;	    				
    			case "Relais_bit1":
    	 	    	   SetBit(lcnAdress,1);
    				break;
    			case "Relais_bit2":
    	 	    	   SetBit(lcnAdress,2);
    				break;
    			case "Relais_bit3":
    	 	    	   SetBit(lcnAdress,3);
    				break;	    				
    			case "Relais_bit4":
    	 	    	   SetBit(lcnAdress,4);
    				break;
    			case "Relais_bit5":
    	 	    	   SetBit(lcnAdress,5);
    				break;    			
    			case "Relais_bit6":
    	 	    	   SetBit(lcnAdress,6);
    				break;    			
    			case "Relais_bit7":
    	 	    	   SetBit(lcnAdress,7);
    				break;    			
    			case "Relais_bit8":
    	 	    	   SetBit(lcnAdress,8);
    				break;    			
			}
		}
		
    	//------------------------------------------------------------
    	//  command: OFF
    	//
    	//------------------------------------------------------------
		if (command.equals(OnOffType.OFF))
		{
	    	updateState(strChannel, OnOffType.OFF);
	    	
			switch (strChannel)
			{
    			case "Output1":
    	 	    	   SetValue1(lcnAdress,0);
    				break; 				
    			case "Output2":
    	 	    	   SetValue2(lcnAdress,0);
    				break;
    				
    			case "Relais_bit1":
    	 	    	   ResetBit(lcnAdress,1);
    				break;
    			case "Relais_bit2":
    	 	    	   ResetBit(lcnAdress,2);
    				break;
    			case "Relais_bit3":
    	 	    	   ResetBit(lcnAdress,3);
    				break;	    				
    			case "Relais_bit4":
    	 	    	   ResetBit(lcnAdress,4);
    				break;
    			case "Relais_bit5":
    	 	    	   ResetBit(lcnAdress,5);
    				break;    			
    			case "Relais_bit6":
    	 	    	   ResetBit(lcnAdress,6);
    				break;    			
    			case "Relais_bit7":
    	 	    	   ResetBit(lcnAdress,7);
    				break;    			
    			case "Relais_bit8":
    	 	    	   ResetBit(lcnAdress,8);
    				break;   
			}
		}   	
    	
    }
 
    //===================================================================
    /**
     * Handler for Command Refresh
     * 
     * @param 	strChannel	string of channel
     * @param 	channelUID	id of channel
    */
    //===================================================================
    private void handleCommand_Refresh(String strChannel, ChannelUID channelUID)
    {
		int val = 0;
   		
		switch (strChannel)
		{	
			//====================== Outputs ============================================	
			case "Output1":	
	    		val = myLCNPort.GetCurrentOut1(lcnAdress);	
	    		SendOutputState(channelUID, val);    				
				break;
				
			case "Output2":
	    		val = myLCNPort.GetCurrentOut2(lcnAdress);
	    		SendOutputState(channelUID, val);  				
				break;
				
			//====================== Relais ============================================				
			case "Relais_bit1":
	    		val = myLCNPort.GetCurrentRelaisBit(lcnAdress, 1);
	    		SendRelaisBitState(channelUID, val);
	 	    	   break;
 			case "Relais_bit2":
	    		val = myLCNPort.GetCurrentRelaisBit(lcnAdress, 2);
	    		SendRelaisBitState(channelUID, val);
 				break;
 			case "Relais_bit3":
	    		val = myLCNPort.GetCurrentRelaisBit(lcnAdress, 3);
	    		SendRelaisBitState(channelUID, val);
 				break;	    				
 			case "Relais_bit4":
	    		val = myLCNPort.GetCurrentRelaisBit(lcnAdress, 4);
	    		SendRelaisBitState(channelUID, val);
 				break;
 			case "Relais_bit5":
	    		val = myLCNPort.GetCurrentRelaisBit(lcnAdress, 5);
	    		SendRelaisBitState(channelUID, val);
 				break;    			
 			case "Relais_bit6":
	    		val = myLCNPort.GetCurrentRelaisBit(lcnAdress, 6);
	    		SendRelaisBitState(channelUID, val);
 				break;    			
 			case "Relais_bit7":
	    		val = myLCNPort.GetCurrentRelaisBit(lcnAdress, 7);
	    		SendRelaisBitState(channelUID, val);
 				break;    			
 			case "Relais_bit8":
	    		val = myLCNPort.GetCurrentRelaisBit(lcnAdress, 8);
	    		SendRelaisBitState(channelUID, val);
 				break; 
 				
 			//====================== Shutter ============================================	 				
			case "Shutter1":
				val = Shutter_actValues[idShutter1];
				
				if (val > 100)
					val = 100;
	    		SendOutputState(channelUID, val);    	
 				break;	
 				
 			case "Shutter2":
				val = Shutter_actValues[idShutter2];
				
				if (val > 100)
					val = 100;
	    		SendOutputState(channelUID, val);    
 				break;
 				
 			case "Shutter3":
				val = Shutter_actValues[idShutter3];
				
				if (val > 100)
					val = 100;
				
	    		SendOutputState(channelUID, val);    
 				break;
 				
 			case "Shutter4":
				val = Shutter_actValues[idShutter4];
				
				if (val > 100)
					val = 100;
				
	    		SendOutputState(channelUID, val);    
 				break;					
 				
		}	 	
    }
    
	
}
