/*
 * Created on Feb 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.examples.jogl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import de.jreality.jogl.InteractiveViewerDemo;
import discreteGroup.TriangleGroup;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoadableSceneDemo extends InteractiveViewerDemo {

    public static void main(String[] args) throws Exception {
		LoadableSceneDemo iv = new LoadableSceneDemo();
		iv.initializeScene();
		if (args != null) iv.loadScene(args[0]);
		else iv.loadScene(null);
    }
    String root = "de.jreality.worlds.";
    String[] loadableScenes = {"de.jreality.worlds.AlexDemo",
    			"de.jreality.worlds.AnimationDemo",
 			"de.jreality.worlds.DebugLattice",
			"de.jreality.worlds.HopfFibration",
			"de.jreality.worlds.ElephantTrunk",
			"de.jreality.worlds.Icosahedra",
			"de.jreality.worlds.ImplodedTori",
			"de.jreality.worlds.LabelSetDemo",
  			"de.jreality.worlds.JOGLSkyBox",
    			"de.jreality.worlds.ReflectionMapDemo",
    			"de.jreality.worlds.StandardDemo",
			"de.jreality.worlds.TestClippingPlane",
			"de.jreality.worlds.TestSphereDrawing",
			"de.jreality.worlds.TestTubes",
  			"discreteGroup.demo.ArchimedeanSolids",
  			"discreteGroup.demo.Cell120",
			"discreteGroup.demo.SoccerBall"};
   
    Class c;
    public void lookupClasses()	{
    		Class c = null;
			try {
				c = Class.forName("de.jreality.util.LoadableScene");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
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
}
