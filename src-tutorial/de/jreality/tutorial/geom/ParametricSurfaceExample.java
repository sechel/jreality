package de.jreality.tutorial.geom;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tutorial.util.TextSlider;
import de.jreality.util.Secure;

public class ParametricSurfaceExample {
	// to use the ParametricSurfaceFactory one needs an instance of immersion
	// That is, a function that maps  (u,v) values into a 3- or 4-space
	Immersion myImmersion = new Immersion() {
		public void evaluate(double u, double v, double[] xyz, int index) {
			xyz[index]= u;
			xyz[index+1]= v;
			xyz[index+2]= Math.sin(c*u)*Math.sin(v);
		}
		// how many dimensions in the image space?
		public int getDimensionOfAmbientSpace() { return 3;	}
		// Does evaluate() always put the same value into xyz for a given pair (u,v)?
		// If the immersion has parameters that affect the result evaluate() then isImmutable()
		// should return false.
		public boolean isImmutable() { return true; }
	};
	double c = 1.0;
	private ParametricSurfaceFactory psf;
	public void doIt(		boolean useViewerVR)	{
		psf = new ParametricSurfaceFactory(myImmersion);
		psf.setUMin(0);
		psf.setUMax(Math.PI*2);
		psf.setVMin(0);
		psf.setVMax(Math.PI*2);
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
			JRViewer v = new JRViewer();
			v.addBasicUI();
			v.addAudioSupport();
			v.addVRSupport();
			v.addContentSupport(ContentType.TerrainAligned);
			v.setPropertiesFile("vrprefsParametricExample.xml");
			v.registerPlugin(new ContentAppearance());
			v.setContent(sgc);
			v.startup();
		} else {
			JRViewer.display(sgc);		
		}
	}
	private  Component getInspector() {
		Box container = Box.createVerticalBox();
		container.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Surface parameters")));

		final TextSlider.Double RSlider = new TextSlider.Double("c",
				SwingConstants.HORIZONTAL, 0.0, 10, c);
		RSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				c = RSlider.getValue();
				psf.update();
			}
		});
		container.add(RSlider);
		return container;
	}
	
	public static void main(String[] args) {
		ParametricSurfaceExample pse = new ParametricSurfaceExample();
		pse.doIt(false);
	}
}
