/*
 * Created on Mar 17, 2005
 *
 */
package de.jreality.worlds;

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

import de.jreality.geometry.Primitives;
import de.jreality.jogl.Snake;
import de.jreality.jogl.inspection.FancySlider;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 */
public class BouncingSpheres extends AbstractJOGLLoadableScene {
	/* (non-Javadoc)
	 * @see de.jreality.util.LoadableScene#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (updateTimer != null)	{
			updateTimer.stop();
			updateTimer = null;
		}
		if (midi != null) midi.close();
	}
	double[][] positions;
	double[][] directions;
	double[][] colors;
	double[] speeds;
	Snake snake;
	Object lockObject = new Object();
	int framesPerSecond = 60;
	int ballCount = 50;
	boolean useSnake = false;
	boolean doSound = true;
	MyMidiSynth midi = new MyMidiSynth();
	javax.swing.Timer updateTimer = null;
	int[] indices = {8,9,10,11,12,13};
	SceneGraphComponent world = SceneGraphUtilities.createFullSceneGraphComponent("bouncingSpheres");
	public BouncingSpheres()		{
		super();
		setDoSound(doSound);
	}
	
	public Geometry createSpheres(int length)	{
		positions = new double[length][3];
		directions = new double[length][3];
		colors = new double[length][3];
		speeds = new double[length];
		for (int i = 0; i<length; ++i)	{
			for (int j = 0; j<3; ++j)	{
				positions[i][j] = 2 * (-.5 + Math.random());
				directions[i][j]  = -.5 + Math.random();
				Rn.normalize(directions[i], directions[i]);
				Rn.times(directions[i], Math.random(), directions[i]);
				colors[i][j]  = Math.random();
			}
			speeds[i] = Math.random();
		}
		snake = new Snake(positions);
		snake.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
		//System.out.println("VC is originally: "+snake.getVertexAttributes(Attribute.COLORS));
		if (!useSnake) snake.activate(false);
		return snake;
	}
	/* (non-Javadoc)
	 * @see de.jreality.util.LoadableScene#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		Appearance ap  = world.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		world.setGeometry(createSpheres(ballCount));
		SceneGraphComponent frame = SceneGraphUtilities.createFullSceneGraphComponent("Frame");
		ap = frame.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, .9);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		frame.setGeometry(Primitives.cube());
		
		updateTimer = new javax.swing.Timer(1000/framesPerSecond, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updatePositions(); } } );
		updateTimer.setCoalesce(true);
		updateTimer.start();
		world.addChild(frame);
		return world;
	}
    Viewer viewer;
    
    
	/* (non-Javadoc)
	 * @see de.jreality.util.LoadableScene#customize(javax.swing.JMenuBar, de.jreality.scene.Viewer)
	 */
	public void customize(JMenuBar menuBar, Viewer viewer) {
		super.customize(menuBar, viewer);
		this.viewer = viewer;
	}
	double globalSpeed = 1.0;
	boolean firstTime = true;
	boolean wallSound = true;
	public void updatePositions()	{
		if (firstTime) { firstTime = false; return; }
		double factor = globalSpeed/framesPerSecond;
		synchronized(lockObject)	{
			for (int i = 0; i<ballCount; ++i)	{
				Rn.add(positions[i], positions[i], Rn.times(null, factor, directions[i]));
				for (int j = 0; j<3; ++j)		{
					if (positions[i][j] > 1)		{
						positions[i][j] -= 2*(positions[i][j] -1 );
						directions[i][j] *= -1.0;
//						if (wallSound)	{
//							midi.synthesizer.loadInstrument(midi.instruments[indices[2*j]]);
//							midi.channels[2*j].channel.programChange(indices[2*j]);			
//						} else {
//							midi.synthesizer.loadInstrument(midi.instruments[i]); //indices[2*j]]);
//							midi.channels[2*j].channel.programChange(i); //indices[2*j]);		
//						}
						if (doSound) midi.channels[2*j].channel.noteOn((int) (24+speeds[i]*64), midi.channels[2*j].velocity);
						//System.out.print("+"+j);
						break;
					}
					else if (positions[i][j] < -1)		{
						positions[i][j] -= 2*(positions[i][j] + 1);
						directions[i][j] *= -1.0;
//						if (wallSound)	{
//							//midi.synthesizer.loadInstrument(midi.instruments[indices[2*j+1]]);
//							midi.channels[2*j+1].channel.programChange(indices[2*j+1]);			
//						} else {
//							//midi.synthesizer.loadInstrument(midi.instruments[i]); //indices[2*j]]);
//							midi.channels[2*j+1].channel.programChange(i); //indices[2*j]);		
//						}
						if (doSound) midi.channels[2*j+1].channel.noteOn((int) (24+speeds[i]*64), midi.channels[2*j+1].velocity);
						//System.out.print("-"+j);
						break;
					}
				}
			}
			if (!useSnake) snake.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(positions));
			snake.fireChange();
			
		}
		viewer.render();
	}
	
	public boolean isEncompass() {return true; }
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		FancySlider ballCount = new FancySlider.Integer("ballCount", SwingConstants.HORIZONTAL, 0, 250, 100);
	    ballCount.textField.addPropertyChangeListener(new PropertyChangeListener()	{
		    public void propertyChange(PropertyChangeEvent e) {
		        if ("value".equals(e.getPropertyName())) {
		            Number value = (Number)e.getNewValue();
		            if (value != null) {
		                setBalllCount(value.intValue());
		            }
		        }
		    }	       	
	       });
	    //ballCount.setAlignmentX(1.0f);
		container.add(ballCount);
		FancySlider frameTime = new FancySlider.Integer("frameRate", SwingConstants.HORIZONTAL, 0, 250, 60);
	    frameTime.textField.addPropertyChangeListener(new PropertyChangeListener()	{
		    public void propertyChange(PropertyChangeEvent e) {
		        if ("value".equals(e.getPropertyName())) {
		            Number value = (Number)e.getNewValue();
		            if (value != null) {
		                setFramesPerSecond(value.intValue());
		            }
		        }
		    }	       	
	       });

	   //frameTime.setAlignmentX(1.0f);
		container.add(frameTime);
		FancySlider globalSpeed = new FancySlider.Double("globalSpeed",  SwingConstants.HORIZONTAL, 0.0, 10.0, 1.0);
	    globalSpeed.textField.addPropertyChangeListener(new PropertyChangeListener()	{
		    public void propertyChange(PropertyChangeEvent e) {
		        if ("value".equals(e.getPropertyName())) {
		            Number value = (Number)e.getNewValue();
		            if (value != null) {
		                setGlobalSpeed(value.doubleValue());
		            }
		        }
		    }	       	
	       });

	    //globalSpeed.setAlignmentX(1.0f);
		container.add(globalSpeed);
		
		final JCheckBox doSoundBox = new JCheckBox("Audio", doSound );
		doSoundBox.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				doSound = doSoundBox.isSelected();
				setDoSound(doSound);
				viewer.render();
				viewer.getViewingComponent().requestFocus();
			}

		});
	    doSoundBox.setAlignmentX(0.0f);
	    Box sb = new Box(BoxLayout.LINE_AXIS);
	    sb.add(doSoundBox);
	    sb.add(Box.createHorizontalGlue());
		container.add(sb);
		
		container.add(Box.createVerticalGlue());
		return container;
	}
	
	private void setDoSound(boolean doSound) {
		if (doSound)	{
			midi.open();
			for (int j = 0; j<indices.length; ++j)	{
				midi.synthesizer.loadInstrument(midi.instruments[indices[j]]);
				midi.channels[j].channel.programChange(indices[j]);			
			}
		} else midi.close();
	}
	
	private void setBalllCount(int n)	{
		ballCount = n;
		synchronized(lockObject)	{
			world.setGeometry( createSpheres(ballCount));
		}
	}
	
	private void setFramesPerSecond(int n)	{
		framesPerSecond = n;
		updateTimer.setDelay(1000/framesPerSecond);
	}
	private void setGlobalSpeed(double n)	{
		globalSpeed = n;
	}
}
