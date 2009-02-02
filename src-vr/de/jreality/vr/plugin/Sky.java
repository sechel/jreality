package de.jreality.vr.plugin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import de.jreality.ui.plugin.View;
import de.jreality.ui.plugin.image.ImageHook;
import de.jreality.util.Input;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class Sky extends ShrinkPanelPlugin {

	private static String sideNames= "rt,lf,up,dn,bk,ft";

	private static String[][] defaultSkyBoxes = {
		{"snow","textures/jms_hc/jms_hc_", sideNames, ".png",},
		{"grace cross", "textures/grace_cross/grace_cross_", sideNames, ".jpg",},
		{"desert","textures/desert/desert_", sideNames, ".jpg",},
		{"emerald","textures/emerald/emerald_", sideNames, ".jpg",},
		{"custom", null},
		{"none", null}
	};
	
	private View view;
	
	private String[][] skyBoxes;

	private HashMap<String,Integer> envToIndex = new HashMap<String,Integer>();
	private HashMap<String,ButtonModel> envToButton = new HashMap<String,ButtonModel>();
	private ButtonGroup buttonGroup;
	private JCheckBox showSkyCheckBox;

	private ImageData[] cubeMap;
	private ImageData[] customCubeMap;

	private JPanel panel;
	private JButton loadButton;

	private JFileChooser fileChooser;
	private int selectionIndex;

	private String environment = "none";

	public Sky() {
		this(defaultSkyBoxes);
	}

	public Sky(String[][] skyBoxes) {
		this.skyBoxes = skyBoxes;
		makePanel();
		Secure.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				makeFileChooser();
				return null;
			}
		});
		setInitialPosition(SHRINKER_RIGHT);
	}

	public void install(View sceneView) {
		this.view = sceneView;
		setEnvironment(getEnvironment());
		setShowSky(isShowSky());
	}

	public JPanel getPanel() {
		return panel;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String name) {
		environment = name;
		ButtonModel model = envToButton.get(environment);
		if (model == null) {
			throw new IllegalArgumentException("unknown environment "+environment);
		}
		buttonGroup.setSelected(model, true);
		if (view != null) {
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
			ImageData[] cm = environment == "custom" ? customCubeMap : cubeMap;
			loadButton.setEnabled(getEnvironment() == "custom");
			environment =  skyBoxes[selectionIndex][0];
			setCubeMap(cm);
		}
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
		panel = new JPanel(new GridBagLayout());
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
			panel.add(button, gbc);
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
		panel.add(loadButton, gbc);
		showSkyCheckBox = new JCheckBox("show  sky");
		showSkyCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setShowSky(showSkyCheckBox.isSelected());
			}
		});
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(showSkyCheckBox, gbc);
	}

	private void loadSkyBox() {
		File file = null;
		Component parent = view.getViewer().getViewingComponent();
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

	public boolean isShowSky() {
		return showSkyCheckBox.isSelected();
	}

	public void setShowSky(boolean b) {
		showSkyCheckBox.setSelected(b);
		if (view != null) {
			Appearance rootAppearance = view.getSceneRoot().getAppearance();
			ImageData[] cm = b ? cubeMap : null;
			TextureUtility.createSkyBox(rootAppearance, cm);
		}
	}

	private void setCubeMap(ImageData[] cubeMap) {
		this.cubeMap = cubeMap;
		Appearance rootAppearance = view.getSceneRoot().getAppearance();
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

	@Override
	public void install(Controller c) throws Exception {
		view = c.getPlugin(View.class);
		install(view);
		
		setShowSky(isShowSky());
		setEnvironment(getEnvironment());
		
		panel.setPreferredSize(new Dimension(10, 120));
		panel.setMinimumSize(new Dimension(10, 120));

		shrinkPanel.setLayout(new GridLayout());
		shrinkPanel.add(panel); 
		super.install(c);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		setCubeMap(null);
		setShowSky(false);
		super.uninstall(c);
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Sky";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("radioactive1.png");
		return info; 
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		setEnvironment(c.getProperty(getClass(), "environment", getEnvironment()));
		setShowSky(c.getProperty(getClass(), "showSky", isShowSky()));
		setCurrentDirectory(c.getProperty(getClass(), "currentDirectory", getCurrentDirectory()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "environment", getEnvironment());
		c.storeProperty(getClass(), "showSky", isShowSky());
		c.storeProperty(getClass(), "currentDirectory", getCurrentDirectory());
		super.storeStates(c);
	}
	
	public String getCurrentDirectory() {
		return fileChooser.getCurrentDirectory().getAbsolutePath();
	}

	public void setCurrentDirectory(String directory) {
		File dir = new File(directory);
		if (dir.exists() && dir.isDirectory()) {
			fileChooser.setCurrentDirectory(dir);
		} else {
			System.out.println(
					"failed to restore Sky directory "+directory
			);
		}
	}
}