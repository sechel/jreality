package de.jreality.scene.pick;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Cylinder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.ui.viewerapp.ViewerApp;

public class TestBruteForcePicking implements Runnable{
	
	private CatenoidHelicoid ch;
	double t=0;
	double speed=0.001;
	int sleeptime=25;

	public static void main(String[] args) {

		TestBruteForcePicking test=new TestBruteForcePicking();		
		//test.animate();
	}
	
	public TestBruteForcePicking(){
		AABBPickSystem.defaultBuildTree=false;
		
		SceneGraphComponent geoNode=new  SceneGraphComponent();
		
		SceneGraphComponent cmpCH = new SceneGraphComponent();
		SceneGraphComponent cmpSp = new SceneGraphComponent();
		SceneGraphComponent cmpCyl = new SceneGraphComponent();
		
		ch = new CatenoidHelicoid(20);
		ch.setGeometryAttributes("vertices.pickable", Boolean.FALSE);
		ch.setGeometryAttributes("edges.pickable", Boolean.TRUE);
		
		Sphere sp = new Sphere();
		Cylinder cyl = new Cylinder();
		
		MatrixBuilder.euclidean().translate(1,1,1).scale(1.5,0.5,2).assignTo(cmpSp);
		MatrixBuilder.euclidean().rotate(Math.PI/2,1,0,0).assignTo(cmpCyl);
		
		cmpCH.setGeometry(ch);
		cmpSp.setGeometry(sp);
		cmpCyl.setGeometry(cyl);
		
		geoNode.addChild(cmpCH);
		geoNode.addChild(cmpSp);
		geoNode.addChild(cmpCyl);
		
		ViewerApp.display(geoNode);		
	}

	private void animate() {
		Thread th = new Thread(this);
		th.start();		
	}
	
	public void run(){
		
		while (true) {
			t=t+speed;
			if(t>=1){ t=0;}					
			ch.setAlpha(t*Math.PI*2);
	    	try {
	    		Thread.sleep(sleeptime);
	    	} catch (InterruptedException e){
	    	}
	        
	     }		
	}
	
	
}
