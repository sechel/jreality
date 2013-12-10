package de.jreality.portal.vrpn;

import vrpn.ButtonRemote;
import vrpn.ButtonRemote.ButtonChangeListener;
import vrpn.ButtonRemote.ButtonUpdate;

public class TestVRPNButton implements ButtonChangeListener

{
	public void buttonUpdate( ButtonUpdate u,
									   ButtonRemote button )
	{
		System.out.println( "Button message from vrpn: \n" +
							"\ttime:  " + u.msg_time.getTime( ) + "  button:  " + u.button + "\n" +
							"\tstate:  " + u.state );
	}
	
	

	public static void main( String[] args )
	{
		String buttonName = "DTrack@n01";
		ButtonRemote button = null;
		try
		{
			button = new ButtonRemote( buttonName, null, null, null, null );
		}
		catch( InstantiationException e )
		{
			// do something b/c you couldn't create the button
			System.out.println( "We couldn't connect to button " + buttonName + "." );
			System.out.println( e.getMessage( ) );
			return;
		}
		
		TestVRPNButton test = new TestVRPNButton( );
		button.addButtonChangeListener( test );
		
	}

}

