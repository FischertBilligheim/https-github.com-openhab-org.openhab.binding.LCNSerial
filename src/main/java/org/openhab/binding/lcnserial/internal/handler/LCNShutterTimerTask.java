package org.openhab.binding.lcnserial.internal.handler;

import java.util.TimerTask;

import org.eclipse.smarthome.core.library.types.StopMoveType;

public class LCNShutterTimerTask extends TimerTask 
{
	private String usedChannel;
	private LCNSerialEventHandlers usedHandler;
	private int usedShutterId;
	private boolean usedUpdatePos;
	

	public LCNShutterTimerTask(LCNSerialEventHandlers handler, String strChannel, int ShutterId, boolean updatePos)
	{
		usedChannel   = strChannel;
		usedHandler   = handler;	
		usedShutterId = ShutterId;
		usedUpdatePos = updatePos;
	}


	@Override
	public void run()
	{	
	
		if (usedHandler != null)
		{
			usedHandler.handleCommand_StopMove(usedChannel, StopMoveType.STOP, usedUpdatePos);
			usedHandler = null;		// one shot!!
			this.cancel();	
		}
		

	}

}