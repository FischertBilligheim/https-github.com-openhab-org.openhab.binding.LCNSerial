package org.openhab.binding.lcnserial.internal.handler;
import java.io.IOException;

public class SerialReadThread implements Runnable
{
	private LCNPort myLCNPort;
	private boolean bThreadRunning = true;
	
	public SerialReadThread(LCNPort lcnPort)
	{
		myLCNPort = lcnPort;
	}
	
	public void StopSerialReadThread()
	{
		bThreadRunning = false;
	}
	
	@Override 
	public void run()
	{
		char[] cmd = new char[8];
		
		while (bThreadRunning)
		{
	        try 
	        {
				int len = myLCNPort.ReadTelegram(cmd);
				myLCNPort.HandleTelegram(cmd, len);		
			} 
	        catch (InterruptedException | IOException e) 
	        {
			}	  
		}
	}

}
