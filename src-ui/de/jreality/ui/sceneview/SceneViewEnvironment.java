package de.jreality.ui.sceneview;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

public class SceneViewEnvironment {

	private static String sideNames= "rt,lf,up,dn,bk,ft";
	
	private static String[][] defaultSkyBoxes = {
		{"snow","textures/jms_hc/jms_hc_", sideNames, ".png",},
		{"grace cross", "textures/grace_cross/grace_cross_", sideNames, ".jpg",},
		{"desert","textures/desert/desert_", sideNames, ".jpg",},
		{"emerald","textures/emerald/emerald_", sideNames, ".jpg",},
		{"custom", null},
		{"none", null}
	};
	
	private String[][] skyBoxes;
	
	private HashMap<String,Integer> envToIndex = new HashMap<String,Integer>();
	private HashMap<String,ButtonModel> envToButton = new HashMap<String,ButtonModel>();
	private ButtonGroup buttonGroup;
	private JCheckBox showSkyCheckBox;

	private ImageData[] cubeMap;
	private ImageData[] customCubeMap;

	private JPanel envPanel;
	private JButton loadButton;

	private JFileChooser fileChooser;
	private int selectionIndex;

	private SceneView sceneView;

	public SceneViewEnvironment() {
		this(defaultSkyBoxes);
	}
	
	public SceneViewEnvironment(String[][] skyBoxes) {
		this.skyBoxes = skyBoxes;
		makePanel();
		Secure.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				makeFileChooser();
				return null;
			}
		});
	}

	public void install(SceneView sceneView) {
		this.sceneView = sceneView;
		setEnvironment("none");
	}

	public JPanel getPanel() {
		return envPanel;
	}
	
	public String getEnvironment() {
		return skyBoxes[selectionIndex][0];
	}

	public void setEnvironment(final String environment) {
		if (environment != getEnvironment()) {
			ButtonModel model = envToButton.get(environment);
			if (model == null) {
				throw new IllegalArgumentException("unknown environment "+environment);
			}
			buttonGroup.setSelected(model, true);
			Secure.doPrivileged(new PrivilegedAction<Object>() {
				public Object run() {
					selectionIndex = ((Integer)envToIndex.get(environment)).intValue();
					try {
						String[] selectedSkyBox = skyBoxes[selectionIndex];
						if (selectedSkyBox[1] != null) {
							cubeMap=TextureUtility.createCubeMapData(selectedSkyBox[1], selectedSkyBox[2].split(","), selectedSkyBox[3]);
						} else {
							cubeMap = null;
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			});
		}
		ImageData[] cm = environment == "custom" ? customCubeMap : cubeMap;
		loadButton.setEnabled(getEnvironment() == "custom");
		setCubeMap(cm);
	}
	
	private void makeFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		FileFilter ff = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".zip");
			}
			@Override
			public String getDescription() {
				return "ZIP archives";
			}
		};
		String texDir = ".";
		String dataDir = Secure.getProperty(SystemProperties.JREALITY_DATA);
		if (dataDir!= null) texDir = dataDir;
		File defaultDir = new File(texDir);
		fileChooser = new JFileChooser(!defaultDir.exists() ? view.getHomeDirectory() : defaultDir, view);
		fileChooser.setFileFilter(ff);
	}

	private void makePanel() {
		envPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0,5,0,5);
		gbc.ipadx = 20;
		gbc.ipady = 2;

		buttonGroup = new ButtonGroup();
		for (int i = 0; i < skyBoxes.length; i++) {
			final String name = skyBoxes[i][0];
			JRadioButton button = new JRadioButton(skyBoxes[i][0]);
			envToButton.put(skyBoxes[i][0], button.getModel());
			button.getModel().addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setEnvironment(name);
				}
			});
			gbc.gridx = i%2;
			gbc.gridy = i/2;
			envPanel.add(button, gbc);
			buttonGroup.add(button);
			envToIndex.put(skyBoxes[i][0], new Integer(i));
		}

		loadButton = new JButton("load");
		loadButton.setMargin(new Insets(0,0,0,0));
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadSkyBox();
			}
		});

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		envPanel.add(loadButton, gbc);
		showSkyCheckBox = new JCheckBox("show  sky");
		showSkyCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setShowSky(showSkyCheckBox.isSelected());
			}
		});
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		envPanel.add(showSkyCheckBox, gbc);
	}
	
	private void loadSkyBox() {
		File file = null;
		Component parent = sceneView.getViewer().getViewingComponent();
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		if (file != null) {
			try {
				customCubeMap = TextureUtility.createCubeMapData(
						Input.getInput(file)
				);
				setCubeMap(customCubeMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setShowSky(boolean b) {
		Appearance rootAppearance = sceneView.getSceneRoot().getAppearance();
		ImageData[] cm = b ? cubeMap : null;
		TextureUtility.createSkyBox(rootAppearance, cm);
		showSkyCheckBox.setSelected(b);
	}
	
	private void setCubeMap(ImageData[] cubeMap) {
		this.cubeMap = cubeMap;
		Appearance rootAppearance = sceneView.getSceneRoot().getAppearance();
		if (showSkyCheckBox.isSelected()) {
			TextureUtility.createSkyBox(rootAppearance, cubeMap);
		}
		TextureUtility.createReflectionMap(
				rootAppearance,
				CommonAttributes.POLYGON_SHADER,
				cubeMap
		);
		TextureUtility.createReflectionMap(
				rootAppearance,
				CommonAttributes.LINE_SHADER,
				cubeMap
		);
		TextureUtility.createReflectionMap(
				rootAppearance,
				CommonAttributes.POINT_SHADER,
				cubeMap
		);
	}
}