/*
 * Created on Apr 15, 2004
 *
 */
package de.jreality.worlds;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import de.jreality.geometry.LabelSet;
import de.jreality.geometry.WingedEdge;
import de.jreality.jogl.inspection.FancySlider;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;


/**
 * @author gunn
 *
 */
public class LabelSetDemo extends AbstractJOGLLoadableScene {
	SceneGraphComponent  oloidkit, label;
	double a=.4,b=2.5;
	public SceneGraphComponent makeWorld()	{
		
		
		WingedEdge oloid = makeOloid();
		oloidkit = new SceneGraphComponent();
		oloidkit.setGeometry(oloid);
		SceneGraphComponent theWorld = SceneGraphUtilities.createFullSceneGraphComponent("oloidWorld");
		theWorld.addChild(oloidkit);
		
		label = SceneGraphUtilities.createFullSceneGraphComponent("labels");
		label.getAppearance().setAttribute("textShader"+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
		LabelSet ls = LabelSet.labelSetFactory(oloid, null);
		label.setGeometry(ls);
		theWorld.addChild(label);
		return theWorld;
	}

	
	/**
	 * @param oloid
	 */
	private WingedEdge makeOloid() {
		WingedEdge oloid = new WingedEdge(20.0d);
		int num = 100;
		for (int i = 0; i<=num; ++i)  	{
			double angle = 2.0 * Math.PI * ( i/((double) num));
			double[] plane = {Math.cos(angle), Math.sin(angle), a * Math.cos(b*angle), -1d};
			oloid.cutWithPlane(plane);
		} 
		oloid.update();
		return oloid;
	}


	public int getSignature() {
		// TODO Auto-generated method stub
		return Pn.EUCLIDEAN;
	}
	
	public boolean isEncompass()	{return true; }
	public boolean addBackPlane()	{ return true; }
	
	public void setConfiguration(ConfigurationAttributes config) {
		// TODO Auto-generated method stub

	}

	Viewer viewer;
	public void customize(JMenuBar menuBar, Viewer viewer) {
//		try {
//			viewer.getSceneRoot().getAppearance().setAttribute("backgroundTexture", new Texture2D("/homes/geometer/gunn/Pictures/grabs/arch-solids.jpg"));
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		this.viewer = viewer;
	}
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		FancySlider aSlider = new FancySlider.Double("a",  SwingConstants.HORIZONTAL, 0.0, 2.5, a);
	    aSlider.textField.addPropertyChangeListener(new PropertyChangeListener()	{
		    public void propertyChange(PropertyChangeEvent e) {
		        if ("value".equals(e.getPropertyName())) {
		            Number value = (Number)e.getNewValue();
		            if (value != null) {
		                setA(value.doubleValue());
		            }
		        }
		    }	       	
	       });
		container.add(aSlider);
		FancySlider bSlider = new FancySlider.Double("b",  SwingConstants.HORIZONTAL, 0.0, 2.5, b);
	    bSlider.textField.addPropertyChangeListener(new PropertyChangeListener()	{
		    public void propertyChange(PropertyChangeEvent e) {
		        if ("value".equals(e.getPropertyName())) {
		            Number value = (Number)e.getNewValue();
		            if (value != null) {
		                setB(value.doubleValue());
		            }
		        }
		    }	       	
	       });
		container.add(bSlider);

		container.add(Box.createVerticalGlue());
		return container;
	}

    private void update()	{
    		WingedEdge we = makeOloid();
		oloidkit.setGeometry(we);
		LabelSet ls = LabelSet.labelSetFactory(we, null);
		label.setGeometry(ls);
		viewer.render();

    }
	/**
	 * @param d
	 */
	protected void setB(double d) {
		b = d;
		update();
	}


	/**
	 * @param d
	 */
	protected void setA(double d) {
		a = d;
		update();
	}

}
