package de.jreality.tutorial.geom;

import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.vr.ViewerVR;

public class Swallowtail {
	// to use the ParametricSurfaceFactory one needs an instance of immersion
	// That is, a function that maps  (u,v) values into a 3- or 4-space
	static Immersion myImmersion = new Immersion() {
		public void evaluate(double u, double v, double[] xyz, int index) {
			xyz[index]=(u-v*v);
			xyz[index+1]=u*v;
			xyz[index+2]=(u*u-4*u*v*v);
		}
		// how many dimensions in the image space?
		public int getDimensionOfAmbientSpace() { return 3;	}
		// not sure what this implies
		public boolean isImmutable() { return true; }
	};
	
	public static void main(String[] args) {
		// allocate and set values in a parametric surface factory
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

		ViewerVR vvr=ViewerVR.createDefaultViewerVR(null);
		vvr.setContent(sgc);
		
		ViewerApp vapp=vvr.initialize();
		vapp.update();
		vapp.display();			
	}
}
