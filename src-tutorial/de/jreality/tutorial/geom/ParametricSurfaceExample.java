package de.jreality.tutorial.geom;

import java.awt.Color;
import java.io.IOException;
import java.util.prefs.InvalidPreferencesFormatException;

import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Secure;
import de.jreality.vr.ViewerVR;

public class ParametricSurfaceExample {
	// to use the ParametricSurfaceFactory one needs an instance of immersion
	// That is, a function that maps  (u,v) values into a 3- or 4-space
	Immersion myImmersion = new Immersion() {
		public void evaluate(double u, double v, double[] xyz, int index) {
			xyz[index]= 10*(u-v*v);
			xyz[index+1]= 10*u*v;
			xyz[index+2]= 10*(u*u-4*u*v*v);
		}
		// how many dimensions in the image space?
		public int getDimensionOfAmbientSpace() { return 3;	}
		// Does evaluate() always put the same value into xyz for a given pair (u,v)?
		// If the immersion has parameters that affect the result evaluate() then isImmutable()
		// should return false.
		public boolean isImmutable() { return true; }
	};
	
	public void doIt(		boolean useViewerVR)	{
		ParametricSurfaceFactory psf = new ParametricSurfaceFactory(myImmersion);
		psf.setUMin(-.3);
		psf.setUMax(.3);
		psf.setVMin(-.4);
		psf.setVMax(.4);
		psf.setULineCount(20);
		psf.setVLineCount(20); 
		psf.setGenerateEdgesFromFaces(true);
		psf.setGenerateVertexNormals(true);
		psf.update();
		
		SceneGraphComponent sgc = new SceneGraphComponent("Swallowtail");
		sgc.setGeometry(psf.getIndexedFaceSet());
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.yellow);
		sgc.setAppearance(ap);
		// to run the application inside of ViewerVR, set the VM argument 
		// -DuseViewerVR=true  
		// In eclipse: Run->Run..., click on "Arguments", enter this in "VM Arguments" text field
		String foo = Secure.getProperty("useViewerVR");
		if (foo != null && foo.equals("true")) useViewerVR = true;
		if (useViewerVR)	{
			ViewerVR vr=ViewerVR.createDefaultViewerVR(null);
			vr.setContent(sgc);
			
			ViewerApp vapp=vr.initialize();
			try {
				vr.importPreferences(ParametricSurfaceExample.class
						.getResourceAsStream("vrprefsParametricExample.xml"));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidPreferencesFormatException e) {
				e.printStackTrace();
			}
			vr.setAvatarPosition(0, 0, 6);
			vapp.update();
			vapp.display();			
		} else {
			ViewerApp.display(sgc);		
		}
	}
	
	public static void main(String[] args) {
		ParametricSurfaceExample pse = new ParametricSurfaceExample();
		pse.doIt(false);
	}
}
