package org.openhab.binding.lcnserial.internal.handler;

import org.eclipse.smarthome.core.thing.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The {@link LCNPort} class handles the communication with the LCN-Bus. 
 * It can send and receive/interpret bus-telegrams
 * 
 * @author Thomas Fischer
 *
 */
public class LCNPort 
{
	//===================================
	// Some defines
	//
	//===================================
	private static final int Out1 = 0;
	private static final int Out2 = 1;
	private static final int Relais = 2;
	private static final int Binary = 3;
	
	private static final byte PC_MODUL = 1;
	
	//===================================
	// Some private variables
	//
	//===================================
    private int [] [] LCN_Module_valueArray = new int [100] [4];
	private SerialPort myPort;
	private LCNSerialHandler [] myHandlers = new LCNSerialHandler[128];		// store all handlers for all LCN-devices
    
	private InputStream in;
	private OutputStream out;
	
	private final Logger logger = LoggerFactory.getLogger(LCNPort.class);
	
	//===================================
	// Constructor
	//
	//===================================
	public LCNPort(SerialPort serPort) throws IOException
	{
		myPort = serPort;
		
		out = myPort.getOutputStream();
		in = myPort.getInputStream();
	}
     
	//=====================================================================
	// void SetLCNSerialHandler(int Address, LCNSerialHandler handler)
	//
	//=====================================================================
    public void SetLCNSerialHandler(int Address, LCNSerialHandler handler)
    {
    	myHandlers[Address] = handler;   	
    }
  
	//=====================================================================
	// void SetNew_Out1_Cashed_Value(int Address, int val)
	//
	//=====================================================================
    private void SetNew_Out1_Cashed_Value(int Address, int val)
    {
        LCN_Module_valueArray[Address][Out1] = val;
        
        Channel chn = myHandlers[Address].getThing().getChannel("Output1");                    
        myHandlers[Address].SendOutputState(chn.getUID(), val);       
    }
   
	//=====================================================================
	// void SetNew_Out2_Cashed_Value(int Address, int val)
	//
	//=====================================================================
    private void SetNew_Out2_Cashed_Value(int Address, int val)
    {
        LCN_Module_valueArray[Address][Out2] = val;
        
        Channel chn = myHandlers[Address].getThing().getChannel("Output2");                    
        myHandlers[Address].SendOutputState(chn.getUID(), val);       
    }
    
	//=====================================================================
	// void SetNew_Relais_Cashed_Value(int Address, int val)
	//
	//=====================================================================
    private void SetNew_Relais_Cashed_Value(int Address, int val)
    {
    	int oldVal =  LCN_Module_valueArray[Address][Relais];
    	int oldBit;
    	int newBit;
    	
        LCN_Module_valueArray[Address][Relais] = val;
        
        for (int i=0; i<8; i++)
        {
        	oldBit = oldVal & (1 << i);
        	newBit = val    & (1 << i);
        	
        	if (oldBit != newBit)
        	{
        		if (myHandlers[Address] == null)
        		{
        	        logger.error(String.format("No Update-Handler found. Address: %d", Address));
        		}
        		       		
                Channel chn = myHandlers[Address].getThing().getChannel("Relais_bit" + Integer.toString(i+1) ); 
                
                if (chn == null)
                {
                	logger.error(String.format("No Channel for Update found. Address: %d", Address));
                }
                else
                {
                	logger.info(String.format("Update RelaisBit. Address: %d,  Bit: %d, Value: %d", Address, i, newBit >> i));
                	
                	myHandlers[Address].SendRelaisBitState(chn.getUID(), newBit >> i);
                }
        	}
        }
        
    }
    
 	//==============================================================
 	// SendTelegram(char* tosend, int len)
 	//
 	//
 	//==============================================================
 	void SendTelegram(char[] tosend, int len) throws InterruptedException, IOException
 	{
		myPort.setRTS(true);
	
		Thread.sleep(20);
		
	    for (int i=0; i<len; i++)  	
	    	out.write((int)tosend[i]);
	    	
		Thread.sleep(20);
	     
	    myPort.setRTS(false);	 
 	}
 
 	//===========================================================================================
 	// int LCN_Port::ReadBytes(int count)
 	//
 	// Java has no unsigned byte type. So we have to use char's
 	//
 	// myPort.readBytes() seems not be synchronous - it seems, it comes also back with a timeout
 	// So we wait until the InputBuffer is filled...
 	//============================================================================================
 	public int ReadBytes(char[] cmdChar, int len) throws  InterruptedException, IOException
 	{

		while (in.available() < len)
		{
			Thread.sleep(10);		// wait for len bytes...
		}
		
		byte[] cmd = new byte[len];
	
		in.read(cmd, 0, len);
		
		for (int i=0; i< len; i++)
		{
			cmdChar[i] = (char) cmd[i];
			cmdChar[i] &= 0xff;
		}
		
		return len;
 	}
 
	//==============================================================
	// char LCN_Port::ReadChar()
	//
	// Java has no unsigned byte type. So we have to use char's
	//==============================================================
	public char ReadByte() throws IOException
	{
		byte[] cmd = new byte[1];
	
		in.read(cmd, 0, 1);
	
		char ret = (char)cmd[0];
		ret &= 0xff;
		
		return ret;
	}
 
 	//==============================================================
 	// int LCN_Port::ReadTelegram(char*buffer)
 	//
 	//
 	//==============================================================
    public int ReadTelegram(char[] cmd) throws InterruptedException, IOException
	{
		ReadBytes(cmd, 8);
		
		char tst = crc16_Calc(cmd, 8);		// Verify CRC
		cmd[0] = ReverseBits(cmd[0]);     	// Sender is in reversed bits		
		
		while (cmd[2] != tst)
		{
			// CRC seems to be wrong, remove the first byte and read one byte more, then test again
			for (int i = 1; i < 8; i++)
				cmd[i - 1] = cmd[i];

			cmd[7] = ReadByte();
			
			tst = crc16_Calc(cmd, 8);
			cmd[0] = ReverseBits(cmd[0]);           // Sender is in reversed bits
		}
		
		return 8;	
	}

    
    //==============================================================
    // void LCN_Port::HandleTelegram
    //
    // Reads the telegrams of the port in a separate thread.
    //==============================================================
    public void HandleTelegram(char[] data, int bytelen)
    {
    	if (bytelen == 0)
    		return;
    	
        byte srcAdr     = (byte) data[0];
//        byte Info       = (byte) data[1];
//        byte crc        = (byte) data[2];
//        byte dstSegm    = (byte) data[3];
        byte dstAdr     = (byte) data[4];
        byte Command    = (byte) data[5];

        byte para1      = (byte) data[6]; // sub-cmd
        byte para2      = (byte) data[7];

    	int val = 0;
    	
        //=========================================================================
        // Prepare Debug-out
        //=========================================================================
//	        Date date = new Date();
//	        DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
//	        String outStr = dateFormat.format(date)  + " | ";        
                
//            for (int i = 0; i < bytelen; i++)
//            	outStr += String.format("0x%02x ", (byte)data[i]);

//            outStr += " | ";
        //==========================================================================
    	String outStr = " | ";
    	
        switch (Command)
        {
          //-----------------------------------------------------------------------------------------
            case 0x04: // Set Output 1, value = rec1[6] * 2;  or subComm= 0xFD:  Switch(On/Off)
          //-----------------------------------------------------------------------------------------            	
                {
               	
                    if (data[6] != 0xFD)
                    {
                    	val = para1 * 2;
                        outStr += String.format("Output1:         | Modul: %2d | Wert: %3d", dstAdr, val);
                    }
                    else // 0xFD: Ein/Aus
                    {
                        if (LCN_Module_valueArray[dstAdr][ Out1] > 0)
                            val = 0;
                        else
                            val= 100;                        
                    
                        outStr += String.format("Output2: Ein/Aus | Modul: %2d | akt. Wert: %3d | ", dstAdr, val);                    
                    }

                    if (srcAdr != PC_MODUL)						// Only inform OpenHAB when not changed by OpenHAB
                    	SetNew_Out1_Cashed_Value(dstAdr, val);
                }
                break;
                
          //-----------------------------------------------------------------------------------------
            case 0x05: // Set Output 2, value = rec1[6] * 2;  Or subComm= 0xFD:  Switch
          //-----------------------------------------------------------------------------------------
                {
                    if (data[6] != 0xFD)
                    {
                    	val = para1 * 2;
                        outStr += String.format("Output2:         | Modul: %2d | akt. Wert: %3d | ", dstAdr, val);
                    }
                    else // 0xFD: On/Off
                    {
                        if (LCN_Module_valueArray[dstAdr][ Out2] > 0)
                            val = 0;
                        else
                            val = 100;
                        
                        outStr += String.format("Output2: Ein/Aus | Modul: %2d | akt. Wert: %3d | ", dstAdr, LCN_Module_valueArray[dstAdr][ Out2]);
                    }
                   
                    if (srcAdr != PC_MODUL)						// Only inform OpenHAB when not changed by OpenHAB
                    	SetNew_Out2_Cashed_Value(dstAdr, val);

                }
                break;

          //-----------------------------------------------------------------------------------------
            case 0x7: // Send Key
          //-----------------------------------------------------------------------------------------         
            	outStr += "Sende Taste             \r\n";
                break;

          //----------------------------------------------------------------------------------------- 
            case 0x8: // Send Key delayed
          //----------------------------------------------------------------------------------------- 
                outStr += String.format("Sende taste Verz. | Modul: %2d | Zeit: 0x%02x  Taste: 0x%02x ", dstAdr, para1, para2);
                break;

          //----------------------------------------------------------------------------------------- 
            case 0x13:   // Relais
          //----------------------------------------------------------------------------------------- 
                {
                    int actValue = LCN_Module_valueArray[dstAdr][ Relais];

                    if (para1 == 0)  // Invert Relais
                    {
                        outStr += String.format("InvertRelais:    | Modul: %2d | Para2: 0x%02x  ", dstAdr, para2);

                        actValue ^= data[7];
                    }
                    else
                    {
                        if (para2 == 0) // Set Relais
                        {
                            outStr += String.format("SetRelais:       | Modul: %2d | Para1: 0x%02x  ", dstAdr, para1);

                            actValue |= data[6];
                        }
                        else             //Reset Relais
                        {
                            outStr += String.format("ResetRelais:     | Modul: %2d | Para1: 0x%02x  ", dstAdr, para2);

                            int t = 0xff;
                            int mask = (int)data[7] ^ t;
                            actValue &= mask;
                        }
                    }

                    if (srcAdr != PC_MODUL)						// Only inform OpenHAB when not changed by OpenHAB ??
                    	SetNew_Relais_Cashed_Value(dstAdr,actValue );
                }
                break;

          //----------------------------------------------------------------------------------------- 
            case 0x14:
          //----------------------------------------------------------------------------------------- 
                outStr += "Relais Kurzzeit Timer:  ";
                break;

          //----------------------------------------------------------------------------------------- 
            case 0x29:      // Add value, value is in para1;
          //----------------------------------------------------------------------------------------- 
                outStr += String.format("Addiere:         | Modul: %2d | Par1: 0x%02x  |  ", dstAdr, para1);
                break;

          //----------------------------------------------------------------------------------------- 
            case 0x68:      // Statusmeldung Ergebnis
          //----------------------------------------------------------------------------------------- 
                if (srcAdr != 0x01) // Kommando vom Buskoppler, nicht Ergebnis
                {
                    outStr += String.format("Status Ret:      | Modul: %2d | Par1: 0x%02x  | Para2: 0x%02x  ", srcAdr, para1, para2);

                    switch (para1)
                    {
                        case 0x10: // Output 1
                            SetNew_Out1_Cashed_Value(srcAdr, para2 * 2);
                            break;
                        case 0x20: // Output 2
                            SetNew_Out2_Cashed_Value(srcAdr, para2 * 2);
                            break;
                        case 0x30: // Relais
                            SetNew_Relais_Cashed_Value(srcAdr, para2);
                            break;
                        case 0x40: // Binary Inputs
                            LCN_Module_valueArray[srcAdr][ Binary] = para2;
                            break;
                    }
                }
                else
                {
                        outStr += String.format("Statusmeldung S: | Modul: %2d | Par1: 0x%02x  | Para2: 0x%02x  ", dstAdr, para1, para2);
                }
                break;
            
          //----------------------------------------------------------------------------------------- 
            default:
          //----------------------------------------------------------------------------------------- 
                outStr += String.format("Unknown-Command 0x%02x  ", Command);
                break;
        }

        logger.info(outStr);
    }

    
    //==============================================================
    // LCN_Port::ReverseBits()
    //
    // Returns the bits of a telegram byte in reverse order
    //==============================================================
    char ReverseBits(char b)
    {
	 	int tst = (b & 0x80) >> 7;
	 	tst += (b & 0x40) >> 5;
	 	tst += (b & 0x20) >> 3;
	 	tst += (b & 0x10) >> 1;
	 	tst += (b & 0x08) << 1;
	 	tst += (b & 0x04) << 3;
	 	tst += (b & 0x02) << 5;
	
	 	return (char)tst;
    }
	
	//========================================================
	// short LCN_Port::crc16_Calc(byte[] tele, int len)
	//
	//========================================================
	public char crc16_Calc(char[] tele, int len)
	{
		int i, result, transformed;
		result = 0;
		int newByte = 0;

		for (i = 0; i < len; i++)
		{
			if (i != 2)
			{
				newByte = tele[i] + result;

				transformed = ((newByte & 0x7f) << 2) | ((newByte & 0x180) >> 7);

				if (transformed > 0xFF)
					result = transformed - 0xFF;
				else
					result = transformed;
			}
		}
		return ((char)result);
	}

	//========================================================
	// char LCN_Port::crc16_Manip(byte[] tele, int len)
	//
	//========================================================
	public char crc16_Manip(char[] tele, int len)
	{
		int i, result, transformed;
		result = 0;
		int newByte = 0;

		for (i = 0; i < len; i++)
		{
			if (i != 2)
			{
				newByte = tele[i] + result;

				transformed = ((newByte & 0x7f) << 2) | ((newByte & 0x180) >> 7);

				if (transformed > 0xFF)
					result = transformed - 0xFF;
				else
					result = transformed;
			}
		}
		
		tele[2] = (char)result;
		return ((char)result);
	}
	
	 //==============================================================
	 // void SetOut1(int Address, int Value)
	 //
	 //
	 //==============================================================
	 public void SetOut1(int Address, int Value) throws InterruptedException, IOException
	 {
	 	char[] tosend = { 0x80, 0x04, 0x89, 0x00, (char)Address, 0x04, (char)(Value / 2), 0x04};
	 	crc16_Manip(tosend,8);

		LCN_Module_valueArray[Address][Out1] = Value;
	 	SendTelegram(tosend, 8);
	 }
	
	 //==============================================================
	 // void SetOut2(int Address, int Value)
	 //
	 //
	 //==============================================================
	 public void SetOut2(int Address, int Value) throws InterruptedException, IOException
	 {
	 	char[] tosend = { 0x80, 0x04, 0x89, 0x00, (char)Address, 0x05, (char)(Value / 2), 0x04};
	 	crc16_Manip(tosend,8);

		LCN_Module_valueArray[Address][Out2] = Value;
	 	SendTelegram(tosend, 8);
	 }

	//==============================================================
	// void Send_Key(int Address, int Key_Id)
	//
	//
	//==============================================================
	void Send_Key(int Address, int Key_Id) throws InterruptedException, IOException
	{
		int Para = (0x1 << (Key_Id - 1));

	 	char[] tosend = { 0x80, 0x04, 0x00, 0x00, (char)Address, 0x07, 0x41, (char)Para};
		crc16_Manip(tosend,8);
		
		SendTelegram(tosend, 8);
	}
	
	//==============================================================
	// void Read_Modul_All(int Address)
	//
	//
	//==============================================================
	void Read_Modul_All(int Address) throws InterruptedException, IOException
	{
		char[] tosend = { 0x80, 0x04, 0x00, 0x00, (char)Address, 0x68, 0xff, 0xFF };
		crc16_Manip(tosend,8);

		SendTelegram(tosend, 8);
	}
	
	//==============================================================
	// void Read_Modul_Out1(int Address)
	//
	//
	//==============================================================
	void  Read_Modul_Out1(int Address) throws InterruptedException, IOException
	{
		char[] tosend = { 0x80, 0x04, 0x00, 0x00, (char)Address, 0x68, 0xff, 0x83 };
		crc16_Manip(tosend,8);
		
		SendTelegram(tosend, 8);
	}
	
	//==============================================================
	// void Read_Modul_Out2(int Address)
	//
	//
	//==============================================================
	void  Read_Modul_Out2(int Address) throws InterruptedException, IOException
	{
		char[] tosend = { 0x80, 0x04, 0x00, 0x00, (char)Address, 0x68, 0xff, 0x8C };
		crc16_Manip(tosend,8);
		
		SendTelegram(tosend, 8);
	}

	//==============================================================
	// void Read_Modul_Outs(int Address)
	//
	//
	//==============================================================
	void  Read_Modul_Outs(int Address) throws InterruptedException, IOException
	{
		char[] tosend = { 0x80, 0x04, 0x00, 0x00, (char)Address, 0x68, 0xff, 0x8F };
		crc16_Manip(tosend,8);
		
		SendTelegram(tosend, 8);
	}
	
	//==============================================================
	// void Read_Modul_Relais(int Address)
	//
	// TODO: Implement a call on the bus....
	//==============================================================
	void  Read_Modul_Relais(int Address)
	{

	}
	
	//==============================================================
	// InvertBit(int Address, int Bit_Nr)
	//
	//
	//==============================================================
	void InvertBit(int Address, int Bit_Nr) throws InterruptedException, IOException
	{
		int Para = (0x1 << (Bit_Nr - 1));

		char[] tosend = { 0x80, 0x04, 0x00, 0x00, (char)Address, 0x13, (char)Para, (char)Para };		
		crc16_Manip(tosend,8);

		SendTelegram(tosend, 8);
	}
	
	//==============================================================
	// SetBit(int Address, int Bit_Nr)
	//
	//
	//==============================================================
	void SetBit(int Address, int Bit_Nr) throws InterruptedException, IOException
	{
		int Para = (0x1 << (Bit_Nr - 1));

		char[] tosend = { 0x80, 0x04, 0x00, 0x00, (char)Address, 0x13, (char)Para, 0x0 };						
		crc16_Manip(tosend,8);

		SendTelegram(tosend, 8);
	}
	
	//==============================================================
	// ResetBit(int Address, int Bit_Nr)
	//
	//
	//==============================================================
	void ResetBit(int Address, int Bit_Nr) throws InterruptedException, IOException
	{
		int Para = (0x1 << (Bit_Nr - 1));

		char[] tosend = { 0x80, 0x04, 0x00, 0x00, (char)Address, 0x13, (char)Para, (char)Para };
		crc16_Manip(tosend,8);

		SendTelegram(tosend, 8);
	}
	
	//=====================================
	// void ReadAllStates()
	//
	//====================================
	void ReadAllStates() throws InterruptedException, IOException
	{
		Read_Modul_All(10);
		Read_Modul_All(11);
		Read_Modul_All(20);
		Read_Modul_All(21);
		Read_Modul_All(22);
		Read_Modul_All(23);
		Read_Modul_All(30);
		Read_Modul_All(31);
	}

	//=====================================
	// int GetCurrentOut1(int Address)
	//
	//====================================
	int GetCurrentOut1(int Address)
	{
		return LCN_Module_valueArray[Address][Out1];
	}
	
	//=====================================
	// int GetCurrentOut2(int Address)
	//
	//====================================
	int GetCurrentOut2(int Address)
	{
		return LCN_Module_valueArray[Address][Out2];
	}

	//===============================================
	// int GetCurrentRelaisBit(int Address, int bit)
	//
	//===============================================
	int GetCurrentRelaisBit(int Address, int bit)
	{
		int val = LCN_Module_valueArray[Address][Relais];

		val = val & (1 << bit-1);
		return (val >> bit-1);
	}
	
	//==============================================================
	// int GetCurrentRelais(int Address)
	//
	// 
	//==============================================================
	int  GetCurrentRelais(int Address)
	{
		return LCN_Module_valueArray[Address][Relais];
	}
}
