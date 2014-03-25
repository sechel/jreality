package de.jreality.toolsystem.raw;

import vrpn.ButtonRemote;
import vrpn.ButtonRemote.ButtonChangeListener;
import vrpn.ButtonRemote.ButtonUpdate;
import de.jreality.scene.tool.InputSlot;

public class DeviceVRPNButton implements ButtonChangeListener {
	
	private ButtonRemote button;
	private int numButtons;
	private int[] buttonState; 
	private InputSlot[] buttonSlot;
	
	public DeviceVRPNButton(String device, int numButtons0){
		try {
			button = new ButtonRemote(device, null, null, null, null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		numButtons = numButtons0;
		init();
	}
	
	public void init(){
		//initialize before Changelistener
		buttonState = new int[numButtons]; 
		buttonSlot = new InputSlot[numButtons];
		for(int i=0;i<numButtons;i++){
			buttonState[i]=0;
		}
		button.addButtonChangeListener( this );
	}
	
	@Override
	public void buttonUpdate(ButtonUpdate u, ButtonRemote b) {
		int button = u.button;
		int state = u.state;
		buttonState[button]= state;		
	}
	
	public void setSlot(int idx, InputSlot slot){
		buttonSlot[idx] = slot;
	}
	
	public int getState(int button0){
		return buttonState[button0];	
	}
	public InputSlot getSlot(int button0){
		return buttonSlot[button0];	
	}
}

