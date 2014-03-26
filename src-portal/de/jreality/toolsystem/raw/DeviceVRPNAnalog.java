package de.jreality.toolsystem.raw;

import vrpn.AnalogRemote;
import vrpn.AnalogRemote.AnalogChangeListener;
import vrpn.AnalogRemote.AnalogUpdate;
import de.jreality.scene.tool.InputSlot;

public class DeviceVRPNAnalog implements AnalogChangeListener {
	
	private AnalogRemote analog;
	private int numAnalogs;
	private double[] analogValue; 
	private InputSlot[] analogSlot;

	
	public DeviceVRPNAnalog(String device, int numAnalogs0){
		try {
			analog = new AnalogRemote(device, null, null, null, null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		numAnalogs = numAnalogs0;
		init();
	}
	
	public void init(){
		//initialize before Changelistener
		analogValue = new double[numAnalogs]; 
		analogSlot = new InputSlot[numAnalogs];
		for(int i=0;i<numAnalogs;i++){
			analogValue[i]=0.;
		}
		
		analog.addAnalogChangeListener( this );
	}
	
	public void setSlot(int idx,  InputSlot slot){
		analogSlot[idx]=slot;
	}
	
	public double getValue(int button0){
		return analogValue[button0];	
	}
	public InputSlot getSlot(int button0){
		return analogSlot[button0];	
	}

	@Override
	public void analogUpdate(AnalogUpdate u, AnalogRemote an) {
		for(int i=0;i<numAnalogs;i++){
			analogValue[i] =  u.channel[i];
		}
	}
}

