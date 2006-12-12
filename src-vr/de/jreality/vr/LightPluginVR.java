package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.beans.SimpleColorChooser;

public class LightPluginVR extends AbstractPluginVR {

	// defaults for light panel
	private static final double DEFAULT_SUN_LIGHT_INTENSITY = 1;
	private static final double DEFAULT_HEAD_LIGHT_INTENSITY = .3;
	private static final double DEFAULT_SKY_LIGHT_INTENSITY = .2;
	
	// light tab
	private JPanel lightPanel;
	private SimpleColorChooser sunLightColorChooser;
	private SimpleColorChooser headLightColorChooser;
	private SimpleColorChooser skyLightColorChooser;
	private JSlider sunLightIntensitySlider;
	private JSlider headLightIntensitySlider;
	private JSlider skyLightIntensitySlider;

	// default lights
	private DirectionalLight sunLight = new DirectionalLight();
	private PointLight headLight;
	private DirectionalLight skyLight;

	private ActionListener closeListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			getViewerVR().switchToDefaultPanel();
		}
	};
	
	public LightPluginVR() {
		super("light");
		makeLightTab();
	}
	
	@Override
	public void setViewerVR(ViewerVR vvr) {
		super.setViewerVR(vvr);
		
//		 lights
		sunLight = new DirectionalLight();
		sunLight.setName("sun light");
		SceneGraphComponent lightNode = new SceneGraphComponent("sun");
		lightNode.setLight(sunLight);
		MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 },
				//new double[] { 0, 1, 1 }).assignTo(lightNode);
				//new double[] { 0.39, .24, 0.89 }).assignTo(lightNode);
				new double[] { 0.39, Math.sqrt(.39*.39+0.89*0.89), 0.89 }).assignTo(lightNode);
		getViewerVR().getSceneRoot().addChild(lightNode);
		
		SceneGraphComponent skyNode = new SceneGraphComponent();
		skyLight = new DirectionalLight();
		skyLight.setAmbientFake(true);
		skyLight.setName("sky light");
		skyNode.setLight(skyLight);
		MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 },
				new double[] { 0, 1, 0 }).assignTo(skyNode);
		getViewerVR().getSceneRoot().addChild(skyNode);
		
		headLight = new PointLight();
		headLight.setAmbientFake(true);
		headLight.setFalloff(1, 0, 0);
		headLight.setName("camera light");
		headLight.setColor(new Color(255,255,255,255));
		getViewerVR().getCameraPath().getLastComponent().setLight(headLight);
	    
		setHeadLightIntensity(DEFAULT_HEAD_LIGHT_INTENSITY);
		setSunIntensity(DEFAULT_SUN_LIGHT_INTENSITY);
		setSkyLightIntensity(DEFAULT_SKY_LIGHT_INTENSITY);
	}
	
	private void makeLightTab() {
		lightPanel = new JPanel(new BorderLayout());
		Box lightBox = new Box(BoxLayout.Y_AXIS);
		
		sunLightColorChooser = new SimpleColorChooser();
		sunLightColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		sunLightColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSunLightColor(sunLightColorChooser.getColor());
			}
		});
		headLightColorChooser = new SimpleColorChooser();
		headLightColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		headLightColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setHeadLightColor(headLightColorChooser.getColor());
			}
		});
		headLightColorChooser.addActionListener(closeListener);
		
		skyLightColorChooser = new SimpleColorChooser();
		skyLightColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		skyLightColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSkyLightColor(skyLightColorChooser.getColor());
			}
		});
		skyLightColorChooser.addActionListener(closeListener);
		
		// sun
		Box sunBox = new Box(BoxLayout.X_AXIS);
		sunBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				new TitledBorder("sun")));
		JLabel sunLabel = new JLabel("intensity");
		sunBox.add(sunLabel);
		sunLightIntensitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		sunLightIntensitySlider.setPreferredSize(new Dimension(70,20));
		sunLightIntensitySlider.setBorder(new EmptyBorder(0,5,0,0));
		sunLightIntensitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSunIntensity(.01*sunLightIntensitySlider.getValue());
			}
		});
		sunBox.add(sunLightIntensitySlider);
		JButton sunColorButton = new JButton("color");
		sunColorButton.setMargin(new Insets(0,5,0,5));
		sunColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToSunLightColorChooser();
			}
		});
		sunBox.add(sunColorButton);
		lightBox.add(sunBox);
		
		// head light
		Box headLightBox = new Box(BoxLayout.X_AXIS);
		headLightBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				new TitledBorder("head light")));
		JLabel headLightLabel = new JLabel("intensity");
		headLightBox.add(headLightLabel);
		headLightIntensitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		headLightIntensitySlider.setPreferredSize(new Dimension(70,20));
		headLightIntensitySlider.setBorder(new EmptyBorder(0,5,0,0));
		headLightIntensitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setHeadLightIntensity(.01*headLightIntensitySlider.getValue());
			}
		});
		headLightBox.add(headLightIntensitySlider);
		JButton headLightColorButton = new JButton("color");
		headLightColorButton.setMargin(new Insets(0,5,0,5));
		headLightColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToHeadLightColorChooser();
			}
		});
		headLightBox.add(headLightColorButton);
		lightBox.add(headLightBox);
		
		// sky light
		Box skyLightBox = new Box(BoxLayout.X_AXIS);
		skyLightBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				new TitledBorder("sky light")));
		JLabel skyLightLabel = new JLabel("intensity");
		skyLightBox.add(skyLightLabel);
		skyLightIntensitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		skyLightIntensitySlider.setPreferredSize(new Dimension(70,20));
		skyLightIntensitySlider.setBorder(new EmptyBorder(0,5,0,0));
		skyLightIntensitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSkyLightIntensity(.01*skyLightIntensitySlider.getValue());
			}
		});
		skyLightBox.add(skyLightIntensitySlider);
		JButton skyLightColorButton = new JButton("color");
		skyLightColorButton.setMargin(new Insets(0,5,0,5));
		skyLightColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToSkyLightColorChooser();
			}
		});
		skyLightBox.add(skyLightColorButton);
		lightBox.add(skyLightBox);
		
		lightPanel.add(lightBox);
	}

	@Override
	public JPanel getPanel() {
		return lightPanel;
	}
	
	private void switchToSunLightColorChooser() {
		getViewerVR().switchTo(sunLightColorChooser);
	}
	
	private void switchToHeadLightColorChooser() {
		getViewerVR().switchTo(headLightColorChooser);
	}
	
	private void switchToSkyLightColorChooser() {
		getViewerVR().switchTo(skyLightColorChooser);
	}
		
	public Color getSunLightColor() {
		return sunLight.getColor();
	}
	
	public void setSunLightColor(Color c) {
		sunLight.setColor(c);
	}
	
	public Color getHeadLightColor() {
		return headLight.getColor();
	}
	
	public void setHeadLightColor(Color c) {
		headLight.setColor(c);
	}
	
	public Color getSkyLightColor() {
		return skyLight.getColor();
	}
	
	public void setSkyLightColor(Color c) {
		skyLight.setColor(c);
	}
	
	public double getSunIntensity() {
		return sunLight.getIntensity();
	}
	
	public void setSunIntensity(double x) {
		sunLightIntensitySlider.setValue((int) (100*x));
		sunLight.setIntensity(x);
	}
	
	public double getHeadLightIntensity() {
		return headLight.getIntensity();
	}
	
	public void setHeadLightIntensity(double x) {
		headLightIntensitySlider.setValue((int) (100*x));
		headLight.setIntensity(x);
	}
	
	public double getSkyLightIntensity() {
		return skyLight.getIntensity();
	}
	
	public void setSkyLightIntensity(double x) {
		skyLightIntensitySlider.setValue((int) (100*x));
		skyLight.setIntensity(x);
	}
	
	public void setLightIntensity(double intensity) {
		sunLight.setIntensity(intensity);
	}

	public double getLightIntensity() {
		return sunLight.getIntensity();
	}


	
}
