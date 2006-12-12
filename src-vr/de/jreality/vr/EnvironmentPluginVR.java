package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jtem.beans.SimpleColorChooser;

public class EnvironmentPluginVR extends AbstractPluginVR {

	// defaults for env panel
	private static final String DEFAULT_ENVIRONMENT = "snow";
	private static final boolean DEFAULT_TERRAIN_TRANSPARENT = false;
	private static final boolean DEFAULT_SKYBOX_HIDDEN = false;
	private static final Color DEFAULT_TOP_COLOR = new Color(80,80,120);
	private static final Color DEFAULT_BOTTOM_COLOR = Color.black;
	private static final boolean DEFAULT_BACKGROUND_FLAT = false;

	// env tab
	private JPanel envPanel;
	private JCheckBox terrainTransparent;
	private JCheckBox skyBoxHidden;
	private JCheckBox backgroundFlat;
	private SimpleColorChooser bottomColorChooser;
	private SimpleColorChooser topColorChooser;
	private JButton bottomColorButton;

	private Landscape landscape;
	
	private ActionListener closeListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			getViewerVR().switchToDefaultPanel();
		}
	};
		
	public EnvironmentPluginVR() {
		super("env");
		// landscape
		landscape = new Landscape();
	}
	
	@Override
	public void setViewerVR(ViewerVR vvr) {
		super.setViewerVR(vvr);
		makeEnvTab();
		getViewerVR().setAvatarPosition(0, landscape.isTerrainFlat() ? -.5 : -.13, 28);
	}
	
	private void makeEnvTab() {
		topColorChooser = new SimpleColorChooser();
		topColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		topColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setTopColor(topColorChooser.getColor());
			}
		});
		topColorChooser.addActionListener(closeListener);
		bottomColorChooser = new SimpleColorChooser();
		bottomColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		bottomColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setBottomColor(bottomColorChooser.getColor());
			}
		});
		bottomColorChooser.addActionListener(closeListener);
		landscape.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Environment env = new Environment(landscape.getCubeMap(), landscape.getTerrainTexture(), landscape.getTerrainTextureScale(), landscape.isTerrainFlat());
				getViewerVR().setEnvironment(env);
				updateEnv();
			}
		});	
		
		Insets insets = new Insets(0,2,0,2);
		
		envPanel = new JPanel(new BorderLayout());
		envPanel.setBorder(new EmptyBorder(0,0,0,0));
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.setBorder(new EmptyBorder(0,5,0,5));
		selectionPanel.add(landscape.getSelectionComponent(), BorderLayout.CENTER);
		envPanel.add(selectionPanel, BorderLayout.CENTER);
		
		Box envControlBox = new Box(BoxLayout.Y_AXIS);
		JPanel terrainTransparentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		terrainTransparent = new JCheckBox("transparent terrain");
		terrainTransparent.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				setTerrainTransparent(terrainTransparent.isSelected());
			}
		});
		terrainTransparentPanel.add(terrainTransparent);
		skyBoxHidden = new JCheckBox("no sky");
		skyBoxHidden.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateEnv();
			}
		});
		terrainTransparentPanel.add(skyBoxHidden);
		
		envControlBox.add(terrainTransparentPanel);
		
		JPanel backgroundColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JLabel backgroundLabel = new JLabel("background:");
		backgroundLabel.setBorder(new EmptyBorder(0,5,0,10));
		backgroundColorPanel.add(backgroundLabel);
		
		JButton topColorButton = new JButton("top");
		topColorButton.setMargin(insets);
		topColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTopColorChooser();
			}
		});
		backgroundColorPanel.add(topColorButton);
		bottomColorButton = new JButton("bottom");
		bottomColorButton.setMargin(insets);
		bottomColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToBottomColorChooser();
			}
		});
		backgroundColorPanel.add(bottomColorButton);
		backgroundFlat = new JCheckBox("flat");
		backgroundFlat.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setBackgroundFlat(backgroundFlat.isSelected());
			}
		});
		backgroundColorPanel.add(backgroundFlat);
		envControlBox.add(backgroundColorPanel);
		envPanel.add(envControlBox, BorderLayout.SOUTH);
	}

	@Override
	public JPanel getPanel() {
		return envPanel;
	}
	
	public void updateEnv() {
		ImageData[] imgs = skyBoxHidden.isSelected() ? null : getViewerVR().getEnvironment().getCubeMap();
		TextureUtility.createSkyBox(getViewerVR().getRootAppearance(), imgs);
	}

	private void switchToTopColorChooser() {
		getViewerVR().switchTo(topColorChooser);
	}
	
	private void switchToBottomColorChooser() {
		getViewerVR().switchTo(bottomColorChooser);
	}
		
	private void updateBackground() {
		Color topColor = getTopColor();
		Color bottomColor = getBottomColor();
		Color down = backgroundFlat.isSelected() ? topColor : bottomColor;
		getViewerVR().getRootAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, new Color[]{
				topColor, topColor, down, down
		});
	}

	public Color getTopColor() {
		return topColorChooser.getColor();
	}

	public void setTopColor(Color color) {
		topColorChooser.setColor(color);
		updateBackground();
	}

	public boolean isSkyBoxHidden() {
		return skyBoxHidden.isSelected();
	}
	
	public void setSkyBoxHidden(boolean b) {
		skyBoxHidden.setSelected(b);
	}
	
	public String getEnvironment() {
		return landscape.getEnvironment();
	}

	public void setEnvironment(String environment) {
		landscape.setEvironment(environment);
	}

	public boolean getBackgroundFlat() {
		return backgroundFlat.isSelected();
	}
	
	public Color getBottomColor() {
		return bottomColorChooser.getColor();
	}

	public void setBottomColor(Color color) {
		bottomColorChooser.setColor(color);
		updateBackground();
	}
	
	public boolean isBackgroundFlat() {
		return backgroundFlat.isSelected();
	}
	
	public void setBackgroundFlat(boolean b) {
		backgroundFlat.setSelected(b);
		bottomColorButton.setEnabled(!backgroundFlat.isSelected());
		updateBackground();
	}
	
	public boolean isTerrainTransparent() {
		return terrainTransparent.isSelected();
	}
	
	public void setTerrainTransparent(boolean b) {
		terrainTransparent.setSelected(b);
		getViewerVR().getTerrainAppearance().setAttribute(CommonAttributes.TRANSPARENCY, b ? 1.0 : 0.0);
	}
	
	@Override
	public void storePreferences(Preferences prefs) {
		// env panel
		prefs.put("environment", getEnvironment());
		prefs.putBoolean("terrainTransparent", isTerrainTransparent());
		prefs.putBoolean("skyBoxHidden", isSkyBoxHidden());
		Color c = getTopColor();
		prefs.putInt("topColorRed", c.getRed());
		prefs.putInt("topColorGreen", c.getGreen());
		prefs.putInt("topColorBlue", c.getBlue());
		c = getBottomColor();
		prefs.putInt("bottomColorRed", c.getRed());
		prefs.putInt("bottomColorGreen", c.getGreen());
		prefs.putInt("bottomColorBlue", c.getBlue());
		prefs.putBoolean("backgroundFlat", isBackgroundFlat());		
	}
	
	@Override
	public void restoreDefaults() {
		// env panel
		setEnvironment(DEFAULT_ENVIRONMENT);
		setTerrainTransparent(DEFAULT_TERRAIN_TRANSPARENT);
		setSkyBoxHidden(DEFAULT_SKYBOX_HIDDEN);
		setTopColor(DEFAULT_TOP_COLOR);
		setBottomColor(DEFAULT_BOTTOM_COLOR);
		setBackgroundFlat(DEFAULT_BACKGROUND_FLAT);
	}
	
	@Override
	public void restorePreferences(Preferences prefs) {
		// env panel
		setEnvironment(prefs.get("environment", DEFAULT_ENVIRONMENT));
		setTerrainTransparent(prefs.getBoolean("terrainTransparent", DEFAULT_TERRAIN_TRANSPARENT));
		setSkyBoxHidden(prefs.getBoolean("skyBoxHidden", DEFAULT_SKYBOX_HIDDEN));
		int r = prefs.getInt("topColorRed", DEFAULT_TOP_COLOR.getRed());
		int g = prefs.getInt("topColorGreen", DEFAULT_TOP_COLOR.getGreen());
		int b = prefs.getInt("topColorBlue", DEFAULT_TOP_COLOR.getBlue());
		setTopColor(new Color(r,g,b));
		r = prefs.getInt("bottomColorRed", DEFAULT_BOTTOM_COLOR.getRed());
		g = prefs.getInt("bottomColorGreen", DEFAULT_BOTTOM_COLOR.getGreen());
		b = prefs.getInt("bottomColorBlue", DEFAULT_BOTTOM_COLOR.getBlue());
		setBottomColor(new Color(r,g,b));
		setBackgroundFlat(prefs.getBoolean("backgroundFlat", DEFAULT_BACKGROUND_FLAT));		
	}
}
