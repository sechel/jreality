package de.jreality.tutorial.geom;


import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentTools;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Secure;

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
		// If the immersion has parameters that affect the result of evaluate() then isImmutable()
		// should return false.
		public boolean isImmutable() { return true; }
	};
	
	public void doIt(boolean useViewerVR)	{		
		//initialize the parametric surface factory
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
		
		//put the result of the factory into the SceneGraphComponent sgc
		SceneGraphComponent sgc = new SceneGraphComponent("Swallowtail");
		sgc.setGeometry(psf.getIndexedFaceSet());
		
		// Finally show the example in the Viewer or ViewerVR.
		// to run the application inside of ViewerVR, set the VM argument 
		// -DuseViewerVR=true  
		// In eclipse: Run->Run..., click on "Arguments", enter this in "VM Arguments" text field
		String foo = Secure.getProperty("useViewerVR");
		if (useViewerVR || (foo != null && foo.equals("true")))	{
			JRViewer v = new JRViewer();
			v.addBasicUI();
			v.addVRSupport();
			v.addContentSupport(ContentType.TerrainAligned);
			v.setPropertiesFile("vrprefsParametricExample.xml");
			v.registerPlugin(new ContentAppearance());
			v.registerPlugin(new ContentTools());
			v.setContent(sgc);
			v.startup();
		} else {
			JRViewer.display(sgc);		
		}
	}
	
	public static void main(String[] args) {
		ParametricSurfaceExample pse = new ParametricSurfaceExample();
		pse.doIt(true);
	}
}
