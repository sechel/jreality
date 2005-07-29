/*
 * Created on Feb 8, 2005
 *
 */
package de.jreality.examples.jogl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.inspection.FancySlider;

/**
 * @author gunn
 *
 */
public class LoadableSceneDemo extends InteractiveViewerDemo {

	/**
	 * @param b
	 */
	public LoadableSceneDemo(boolean b) {
		
		super( b);
	}

	/**
	 * 
	 */
	public LoadableSceneDemo() {
		
		super();
	}

	/**
	 * @param split_pane
	 * @param b
	 */
//	public LoadableSceneDemo(int split_pane, boolean b) {
//		super(split_pane, b);
//	}

	public static void main(String[] args) throws Exception {
//		System.out.println(LoadableSceneDemo.class.getResource("/net/java/games/jogl/impl/NativeLibLoader.class"));
//		System.out.println(System.getProperty("sun.boot.class.path").replaceAll(":", "\n"));
//		System.out.println(net.java.games.jogl.impl.NativeLibLoader.class.getClassLoader());
//
		JOGLConfiguration.theLog.log(Level.INFO, System.getProperty("java.library.path"));
		LoadableSceneDemo iv = new LoadableSceneDemo(false);//InteractiveViewerDemo.SPLIT_PANE, false);
		iv.initializeScene();
		if (args != null && args.length > 0) iv.loadScene(args[0]);
		else iv.loadScene("de.jreality.worlds.AnimationDemo");
    }
    String root = "de.jreality.worlds.";
    String[] loadableScenes = {//"de.jreality.worlds.AlexDemo",
    			"de.jreality.worlds.AnimationDemo",
			"de.jreality.worlds.BouncingSpheres",
 			"de.jreality.worlds.DebugLattice",
			"de.jreality.worlds.HopfFibration",
			"de.jreality.worlds.ElephantTrunk",
			"de.jreality.worlds.Icosahedra",
			"de.jreality.worlds.ImplodedTori",
			"de.jreality.worlds.JOGLSkyBox",
			"de.jreality.worlds.LabelSetDemo",
  			"de.jreality.worlds.MakeWeave",
   			"de.jreality.worlds.StandardDemo",
			"de.jreality.worlds.TestClippingPlane",
			"de.jreality.worlds.TestSphereDrawing",
			"de.jreality.worlds.TestTubes",
  			"discreteGroup.demo.ArchimedeanSolids",
  			"discreteGroup.demo.Cell120",
			"discreteGroup.demo.SoccerBall",
            //"de.jreality.worlds.Quake3Demo"
			};
   
    Class c;
    public void lookupClasses()	{
    		Class c = null;
			try {
				c = Class.forName("de.jreality.util.LoadableScene");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println("Interface is "+c.toString());
		Class[] classes = c.getDeclaredClasses();
    }
    
	public JMenuBar createMenuBar() {
		JMenuBar menuBar =  super.createMenuBar();
		//lookupClasses();
		JMenu sceneM = new JMenu("Scenes");
		menuBar.add(sceneM);
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i<loadableScenes.length; ++i)	{
			final int j = i;
			JMenuItem jm = sceneM.add(new JRadioButtonMenuItem(loadableScenes[i]));
			jm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					unloadScene();
					loadScene(loadableScenes[j]);
					viewer.render();
					viewer.getViewingComponent().requestFocus();
				}
			});
			bg.add(jm);
		}
		return menuBar;
	}
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		FancySlider aSlider = new FancySlider.Integer("w",  SwingConstants.HORIZONTAL,0, 4096, width);
	    aSlider.textField.addPropertyChangeListener(new PropertyChangeListener()	{
		    public void propertyChange(PropertyChangeEvent e) {
		        if ("value".equals(e.getPropertyName())) {
		            Number value = (Number)e.getNewValue();
		            if (value != null) {
		                width = value.intValue();
		            }
		        }
		    }	       	
	       });
		container.add(aSlider);
		FancySlider bSlider = new FancySlider.Integer("h",  SwingConstants.HORIZONTAL, 0, 4096, height);
	    bSlider.textField.addPropertyChangeListener(new PropertyChangeListener()	{
		    public void propertyChange(PropertyChangeEvent e) {
		        if ("value".equals(e.getPropertyName())) {
		            Number value = (Number)e.getNewValue();
		            if (value != null) {
		                height=value.intValue();
		            }
		        }
		    }	       	
	       });
		container.add(bSlider);

		container.add(Box.createVerticalGlue());
		return container;
	}

}
