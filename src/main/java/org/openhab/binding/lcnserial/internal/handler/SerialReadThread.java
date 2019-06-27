package org.openhab.binding.lcnserial.internal.handler;
import java.io.IOException;

//======================================================
/** Thread for reading LCN-Telegrams
 * 
 * @author Thomas Fischer
 *
 */
//=======================================================
public class SerialReadThread implements Runnable
{
	private LCNPort myLCNPort;
	private boolean bThreadRunning = true;
	
	//=================================================
	/** Constructor
	 * 
	 * @param lcnPort	LCNPort to send Bus-telegrams
	 */
	//=================================================
	public SerialReadThread(LCNPort lcnPort)
	{
		myLCNPort = lcnPort;
	}
	
	//=======================================
	/** Stop the thread
	 * 
	 */
	//======================================
	public void StopSerialReadThread()
	{
		bThreadRunning = false;
	}
	
	//========================================
	/** Run Method of thread
	 * 
	 * Reads telegrams in a loop
	 */
	//========================================
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
