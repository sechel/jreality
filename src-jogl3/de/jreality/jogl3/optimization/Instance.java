package de.jreality.jogl3.optimization;

import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;

public class Instance {
	
	public Instance(InstanceCollection ic, JOGLFaceSetInstance ins, int posInVBOs){
		this.collection = ic;
		this.fsi = ins;
		this.posInVBOs = posInVBOs;
		dead = false;
		JOGLFaceSetEntity fse = (JOGLFaceSetEntity)fsi.getEntity();
		length = fse.getVBO("vertex_coordinates").getLength();
	}
	
	public void kill(){
		dead = true;
		fsi = null;
	}
	
	public boolean isDead(){
		return dead;
	}
	
	public boolean isAlive(){
		return !dead;
	}
	
	public InstanceCollection collection;
	public JOGLFaceSetInstance fsi;
	public int length;
	public int posInVBOs;
	private boolean dead = false;
}
