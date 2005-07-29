/*
 * Created on Aug 17, 2004
 *
  */
package de.jreality.worlds;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.Timer;

import de.jreality.geometry.SphereUtility;
import de.jreality.jogl.anim.AnimationUtility;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.util.SceneGraphUtilities;
/**
 * @author gunn
 *
 */
public class AnimationDemo extends 	AbstractJOGLLoadableScene  {
	SceneGraphComponent c1, c2, ct, theWorld;
	Viewer viewer;
	
	public void customize(JMenuBar theMenuBar,  Viewer v)	{
		viewer = v;
		JMenu testM = new JMenu("Actions");
		ButtonGroup bg = new ButtonGroup();
		JMenuItem jm = testM.add(new JRadioButtonMenuItem("run animation"));
		jm.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				animate();
				viewer.getViewingComponent().requestFocus();
			}
		});
		bg.add(jm);

		theMenuBar.add(testM);
	}
	
	
	public SceneGraphComponent makeWorld()	{
		c1 = SceneGraphUtilities.createFullSceneGraphComponent("c1");
		c1.addChild(SphereUtility.tessellatedCubes[9]);
		c2 = SceneGraphUtilities.createFullSceneGraphComponent("c2");
		c2.addChild(SphereUtility.tessellatedCubes[9]);
		c2.getTransformation().setTranslation(2d,0d,0d);
		c2.getTransformation().setStretch(.8d, .8d, .5d);
		c2.getTransformation().setRotation(Math.PI/3.0, 1d, 1d, 1d);
		ct = SceneGraphUtilities.createFullSceneGraphComponent("ct");
		ct.addChild(SphereUtility.tessellatedCubes[9]);
		theWorld = SceneGraphUtilities.createFullSceneGraphComponent("theWorld");
		theWorld.addChild(c1);
		theWorld.addChild(c2);
		return theWorld;
	}

	final int numSteps = 100;
	double totalTime = 1.0;
	Timer anim = null;
	boolean animating = false;
	public void animate()	{
		animating = !animating;
		if (!animating )  {
			if (anim != null) anim.stop();
			return;
		}
		
		if (!theWorld.isDirectAncestor(ct)) theWorld.addChild(ct);
		final double dt = totalTime/(numSteps );
		final Transformation t1 = c1.getTransformation();
		final Transformation t2 = c2.getTransformation();
		final Transformation tt = ct.getTransformation();
		System.out.println("animating");
		if (anim == null)	{
			anim = new javax.swing.Timer(30, new ActionListener()	{
				int k = 0;
				public void actionPerformed(ActionEvent e) {tick(); } 
				public void tick()	{
					double t = (k>numSteps) ?   (1.0 -(k-numSteps)*dt): k * dt;
					AnimationUtility.linearInterpolation(tt, t1, t2, t);
					viewer.render();
					k++;
					if (k== 2*numSteps) k = 0;
				}
			} );
		}
		anim.start();
		//theWorld.removeChild(ct);
	}
	public boolean isEncompass()	{return true;}
	public boolean addBackPlane()	{return false;}
	


}
