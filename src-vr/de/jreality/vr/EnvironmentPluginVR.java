package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.Secure;
import de.jtem.beans.SimpleColorChooser;

public class EnvironmentPluginVR extends AbstractPluginVR {

	// defaults for env panel
	private static final String DEFAULT_ENVIRONMENT = "snow";
	private static final boolean DEFAULT_SKYBOX_HIDDEN = false;
	private static final Color DEFAULT_BACKGROUND_COLOR = Color.white;

	private File customCubeMapFile;
	private ImageData[] customCubeMap;
	
	// env tab
	private JPanel envPanel;
	private JCheckBox skyBoxHidden;
	private SimpleColorChooser backgroundColorChooser;
	private JButton backgroundColorButton;
	private JButton envLoadButton;

	private JFileChooser cubeMapFileChooser;
	
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
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				makeCubeMapFileChooser();
				return null;
			}
		});
	}
	
	private void makeCubeMapFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		String texDir = ".";
		String dataDir = Secure.getProperty("jreality.data");
		if (dataDir!= null) texDir = dataDir;
		File defaultDir = new File(texDir);
		cubeMapFileChooser = new JFileChooser(!defaultDir.exists() ? view.getHomeDirectory() : defaultDir, view);
		cubeMapFileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File file = cubeMapFileChooser.getSelectedFile();
				try {
					if (ev.getActionCommand() == JFileChooser.APPROVE_SELECTION
							&& file != null) {
						customCubeMapFile = file;
						customCubeMap = TextureUtility.createCubeMapData(Input.getInput(customCubeMapFile));
						getViewerVR().setEnvironment(customCubeMap);
						updateEnv();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				getViewerVR().switchToDefaultPanel();
			}
		});
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
				getViewerVR().setEnvironment(
						landscape.isCustomEnvironment() ? customCubeMap : landscape.getCubeMap()
						);
				envLoadButton.setEnabled(landscape.isCustomEnvironment());
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
		
		envLoadButton = new JButton("load");
		selectionPanel.add(BorderLayout.SOUTH, envLoadButton);
		envLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchToEnvLoadPanel();
			}
		});
		
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
		skyBoxHidden.setBorder(new EmptyBorder(0,5,5,10));
		skyBoxHidden.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSkyBoxHidden(isSkyBoxHidden());
			}
		});
		envControlBox.add(skyBoxHidden);
		
		backgroundColorButton = new JButton("color");
		backgroundColorButton.setMargin(insets);
		Box colorBox = new Box(BoxLayout.X_AXIS);
		colorBox.setBorder(new EmptyBorder(0,0,5,0));
		backgroundColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTopColorChooser();
			}
		});
		colorBox.add(backgroundColorButton);
		envControlBox.add(colorBox);
		envPanel.add(envControlBox, BorderLayout.SOUTH);
	}

	protected void switchToEnvLoadPanel() {
		getViewerVR().switchToFileChooser(cubeMapFileChooser);
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
		backgroundColorButton.setEnabled(b);
		updateEnv();
	}
	
	public String getEnvironment() {
		return landscape.getEnvironment();
	}

	public void setEnvironment(String environment) {
		landscape.setEvironment(environment);
		envLoadButton.setEnabled(landscape.isCustomEnvironment());
	}
	
	@Override
	public void storePreferences(Preferences prefs) {
		prefs.put("environment", getEnvironment());
		System.out.println("storing env: "+getEnvironment());
		if ("custom".equals(getEnvironment())) {
			if (customCubeMapFile != null) {
				prefs.put("environmentFile", customCubeMapFile.getAbsolutePath());
			}
		}		
		prefs.putBoolean("skyBoxHidden", isSkyBoxHidden());
		Color c = getBackgroundColor();
		prefs.putInt("backgroundColorRed", c.getRed());
		prefs.putInt("backgroundColorGreen", c.getGreen());
		prefs.putInt("backgroundColorBlue", c.getBlue());	
	}
	
	@Override
	public void restoreDefaults() {
		customCubeMapFile = null;
		customCubeMap = null;
		setEnvironment(DEFAULT_ENVIRONMENT);
		setSkyBoxHidden(DEFAULT_SKYBOX_HIDDEN);
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
	}
	
	@Override
	public void restorePreferences(Preferences prefs) {
		String customFile = prefs.get("environmentFile", null);
		if (customFile != null) {
			File f = new File(customFile);
			if (f.exists()) customCubeMapFile = f;
			try {
				customCubeMap = TextureUtility.createCubeMapData(Input.getInput(f));
			} catch (IOException e) {
				e.printStackTrace();
				customCubeMapFile = null;
			}
		}

		setEnvironment(prefs.get("environment", DEFAULT_ENVIRONMENT));
		setSkyBoxHidden(prefs.getBoolean("skyBoxHidden", DEFAULT_SKYBOX_HIDDEN));
		int r = prefs.getInt("backgroundColorRed", DEFAULT_BACKGROUND_COLOR.getRed());
		int g = prefs.getInt("backgroundColorGreen", DEFAULT_BACKGROUND_COLOR.getGreen());
		int b = prefs.getInt("backgroundColorBlue", DEFAULT_BACKGROUND_COLOR.getBlue());
		setBackgroundColor(new Color(r,g,b));
	}
	
}
