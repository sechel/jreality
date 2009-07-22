package de.jreality.tutorial.gui;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactory.Immersion;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ViewShrinkPanelPlugin;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentTools;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tutorial.geom.ParametricSurfaceExample;
import de.jtem.beans.InspectorPanel;

/** Extends {@link ParametricSurfaceExample} by an inspector plugin panel.
 * 
 * @author G. Paul Peters, 22.07.2009
 *
 */
public class BeanInspectorExample {
	public static Immersion swallowtailImmersion = new Immersion() {
		public void evaluate(double u, double v, double[] xyz, int index) {
			xyz[index]= 10*(u-v*v);
			xyz[index+1]= 10*u*v;
			xyz[index+2]= 10*(u*u-4*u*v*v);
		}
		public int getDimensionOfAmbientSpace() { return 3;	}
		public boolean isImmutable() { return true; }
	};
	
	public static void main(String[] args) {
		final ParametricSurfaceFactory psf = new ParametricSurfaceFactory(swallowtailImmersion);
		psf.setUMin(-.3);psf.setUMax(.3);psf.setVMin(-.4);psf.setVMax(.4);
		psf.setULineCount(20);psf.setVLineCount(20);
		psf.setGenerateEdgesFromFaces(true);
		psf.setGenerateVertexNormals(true);
		psf.update();
		
		SceneGraphComponent sgc = new SceneGraphComponent("Swallowtail");
		sgc.setGeometry(psf.getIndexedFaceSet());
		
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		v.registerPlugin(new ContentAppearance());
		v.registerPlugin(new ContentTools());
		v.setContent(sgc);

		//create an Inspector for the domain
		InspectorPanel inspector = new InspectorPanel();
		inspector.setObject(psf, null);
		JButton updateButton=new JButton("update");
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				psf.update();
			}
		});		
		inspector.add(updateButton,BorderLayout.SOUTH);
		inspector.revalidate();
		//add the inspector to the viewer
		ViewShrinkPanelPlugin plugin = new ViewShrinkPanelPlugin("Domain");
		plugin.getShrinkPanel().add(inspector);
		v.registerPlugin(plugin);
		
		//Start the viewer
		v.startup();
		
	}	
}
