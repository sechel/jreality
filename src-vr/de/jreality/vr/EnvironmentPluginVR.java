package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jtem.beans.SimpleColorChooser;

public class EnvironmentPluginVR extends AbstractPluginVR {

	// defaults for env panel
	private static final String DEFAULT_ENVIRONMENT = "snow";
	private static final boolean DEFAULT_SKYBOX_HIDDEN = false;
	private static final Color DEFAULT_BACKGROUND_COLOR = Color.white;

	// env tab
	private JPanel envPanel;
	private JCheckBox skyBoxHidden;
	private SimpleColorChooser backgroundColorChooser;
	private JButton backgroundColorButton;

	private Landscape landscape;
	
	private ActionListener closeListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			getViewerVR().switchToDefaultPanel();
		}
	};
		
	public EnvironmentPluginVR() {
		super("env");
		landscape = new Landscape();
		makeEnvTab();
	}
	
	@Override
	public void setViewerVR(ViewerVR vvr) {
		super.setViewerVR(vvr);
	}
	
	private void makeEnvTab() {
		backgroundColorChooser = new SimpleColorChooser();
		backgroundColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		backgroundColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setBackgroundColor(backgroundColorChooser.getColor());
			}
		});
		backgroundColorChooser.addActionListener(closeListener);

		landscape.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				getViewerVR().setEnvironment(landscape.getCubeMap());
				updateEnv();
			}
		});	
		
		Insets insets = new Insets(0,5,0,5);
		
		envPanel = new JPanel(new BorderLayout());
		envPanel.setBorder(new EmptyBorder(0,0,0,0));
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.setBorder(
				new CompoundBorder(
						new EmptyBorder(0, 5, 2, 3),
						BorderFactory.createTitledBorder(
								BorderFactory.createEtchedBorder(),
								"Cube map"
						)
				)
		);
		selectionPanel.add(landscape.getSelectionComponent(), BorderLayout.CENTER);
		envPanel.add(selectionPanel, BorderLayout.CENTER);
		
		Box envControlBox = new Box(BoxLayout.X_AXIS);
		envControlBox.setBorder(
				new CompoundBorder(
						new EmptyBorder(0, 5, 2, 3),
						BorderFactory.createTitledBorder(
								BorderFactory.createEtchedBorder(),
								"Background"
						)
				)
		);
		skyBoxHidden = new JCheckBox("flat background");
		skyBoxHidden.setBorder(new EmptyBorder(0,5,0,10));
		skyBoxHidden.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateEnv();
			}
		});
		envControlBox.add(skyBoxHidden);
		
		backgroundColorButton = new JButton("color");
		backgroundColorButton.setMargin(insets);
		backgroundColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTopColorChooser();
			}
		});
		envControlBox.add(backgroundColorButton);
		envPanel.add(envControlBox, BorderLayout.SOUTH);
	}

	@Override
	public JPanel getPanel() {
		return envPanel;
	}
	
	public void updateEnv() {
		ImageData[] imgs = skyBoxHidden.isSelected() ? null : getViewerVR().getEnvironment();
		TextureUtility.createSkyBox(getViewerVR().getRootAppearance(), imgs);
	}

	private void switchToTopColorChooser() {
		getViewerVR().switchTo(backgroundColorChooser);
	}

	public Color getBackgroundColor() {
		return backgroundColorChooser.getColor();
	}

	public void setBackgroundColor(Color color) {
		backgroundColorChooser.setColor(color);
		getViewerVR().getRootAppearance().setAttribute(
				CommonAttributes.BACKGROUND_COLOR,
				getBackgroundColor()
		);
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
	
	@Override
	public void storePreferences(Preferences prefs) {
		prefs.put("environment", getEnvironment());
		prefs.putBoolean("skyBoxHidden", isSkyBoxHidden());
		Color c = getBackgroundColor();
		prefs.putInt("backgroundColorRed", c.getRed());
		prefs.putInt("backgroundColorGreen", c.getGreen());
		prefs.putInt("backgroundColorBlue", c.getBlue());	
	}
	
	@Override
	public void restoreDefaults() {
		setEnvironment(DEFAULT_ENVIRONMENT);
		setSkyBoxHidden(DEFAULT_SKYBOX_HIDDEN);
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
	}
	
	@Override
	public void restorePreferences(Preferences prefs) {
		setEnvironment(prefs.get("environment", DEFAULT_ENVIRONMENT));
		setSkyBoxHidden(prefs.getBoolean("skyBoxHidden", DEFAULT_SKYBOX_HIDDEN));
		int r = prefs.getInt("backgroundColorRed", DEFAULT_BACKGROUND_COLOR.getRed());
		int g = prefs.getInt("backgroundColorGreen", DEFAULT_BACKGROUND_COLOR.getGreen());
		int b = prefs.getInt("backgroundColorBlue", DEFAULT_BACKGROUND_COLOR.getBlue());
		setBackgroundColor(new Color(r,g,b));
	}
}
