package org.openhab.binding.lcnserial.internal;

//====================================================
/** Class for reading the Config-Values of the Thing
 * 
 * @author Thomas Fischer
 *
 */
//====================================================
public class LCNSerialConfiguration 
{

	public String port;					// Serial Port Name

	public int lcn_id;

	public boolean	Shutter1_enabled;
	public int 		Shutter1_drivetime;
	public int 		Shutter1_overalltime;	
	
	public boolean	Shutter2_enabled;
	public int 		Shutter2_drivetime;
	public int 		Shutter2_overalltime;	
	
	public boolean	Shutter3_enabled;
	public int 		Shutter3_drivetime;
	public int 		Shutter3_overalltime;	
	
	public boolean	Shutter4_enabled;
	public int 		Shutter4_drivetime;
	public int 		Shutter4_overalltime;	
	
	
}
