package de.jreality.ui.sceneview;

//import java.awt.BorderLayout;
//import java.awt.Dimension;
//import java.awt.GridLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//import java.security.PrivilegedAction;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//
//import javax.swing.BorderFactory;
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.ButtonGroup;
//import javax.swing.ButtonModel;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JFileChooser;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JSlider;
//import javax.swing.SwingConstants;
//import javax.swing.border.CompoundBorder;
//import javax.swing.border.EmptyBorder;
//import javax.swing.border.TitledBorder;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//
//import de.jreality.math.MatrixBuilder;
//import de.jreality.scene.Appearance;
//import de.jreality.scene.SceneGraphComponent;
//import de.jreality.shader.CommonAttributes;
//import de.jreality.shader.ImageData;
//import de.jreality.shader.Texture2D;
//import de.jreality.shader.TextureUtility;
//import de.jreality.util.Input;
//import de.jreality.util.PickUtility;
//import de.jreality.util.Secure;
//import de.jreality.vr.BigMesh;
//import de.jreality.vr.Terrain.GeometryType;
//import de.jtem.beans.SimpleColorChooser;

public class Terrain {
//	
//	// maximal value of texture scale
//	private static final double MAX_TEX_SCALE = 100;
//	
//	// ratio of maximal value and minimal value of texture scale
//	private static final double TEX_SCALE_RANGE = 800;
//	
//	private SceneView sceneView;
//	private Content content;
//	private CameraManager cameraManager;
//	private SceneGraphComponent terrain;
//	private Appearance terrainAppearance;
//	private JPanel panel;
//	private SceneGraphComponent plane;
//	private static String[][] defaultLandscapes = {
//		{"grid","textures/grid.jpeg"},
//		{"black grid","textures/gridBlack.jpg"},
//		{"tiles","textures/recycfloor1_clean2.png"},
//		{"rust","textures/outfactory3.png"},
//	};
//	private HashMap<GeometryType, ButtonModel> geometryButtons = new HashMap<GeometryType, ButtonModel>();
//	private HashMap<String, ImageData> imageMap = new HashMap<String, ImageData>();
//	private HashMap<String, JRadioButton> texNameToButton = new HashMap<String, JRadioButton>();
//	private JSlider terrainTexScaleSlider;
//	private JCheckBox terrainTexScaleEnabled;
//	private JPanel terrainPanel;
//
//	private Texture2D terrainTex;
//	private JFileChooser terrainTexFileChooser;
//	private File terrainTexFile;
//	private SimpleColorChooser faceColorChooser;
//	private JCheckBox facesReflecting;
//	private JSlider faceReflectionSlider;
//	private JCheckBox transparency;
//	private JSlider transparencySlider;
//
//	
//	public Terrain(SceneView sceneView, Content content, CameraManager cameraManager) {
//		this.sceneView = sceneView;
//		this.content = content;
//		this.cameraManager = cameraManager;
//		
//		terrain = new SceneGraphComponent("terrain");
//		terrainAppearance = new Appearance("terrain appearance");
//		terrain.setAppearance(terrainAppearance);
//		
//		plane = new SceneGraphComponent("plane");
//		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(plane);
//		plane.setGeometry(BigMesh.bigMesh(50, 50, 2000));
//		plane.getGeometry().setGeometryAttributes("infinite plane", Boolean.TRUE);
//		PickUtility.assignFaceAABBTrees(plane);
//		terrain.addChild(plane);
//		
//		makePanel();
//	}
//	
//	public double getTerrainTextureScale() {
//		double d = .01 * terrainTexScaleSlider.getValue();
//		return Math.exp(Math.log(TEX_SCALE_RANGE) * d)/TEX_SCALE_RANGE * MAX_TEX_SCALE;
//	}
//
//	public void setTerrainTextureScale(double d) {
//		terrainTexScaleSlider.setValue(
//				(int)(Math.log(d / MAX_TEX_SCALE * TEX_SCALE_RANGE)/Math.log(TEX_SCALE_RANGE)*100)
//			);
//		if (terrainTex != null) {
//			terrainTex.setTextureMatrix(MatrixBuilder.euclidean().scale(d).getMatrix());
//		}
//	}
//
//	private ImageData loadTexture(final String resourceName) {
//		return Secure.doPrivileged(new PrivilegedAction<ImageData>() {
//			public ImageData run() {
//				try {
//					return ImageData.load(Input.getInput(resourceName));
//				} catch (Exception e) {
//					// error
//				}
//				return null;
//			}
//		});
//	}
//	
//	private void makePanel() {
//		panel = new JPanel(new BorderLayout());
//		Insets insets = new Insets(0, 5, 0, 5);
//		Box selections = new Box(BoxLayout.X_AXIS);
//		selections.setBorder(new EmptyBorder(0,5,0,2));
//		
//		JPanel buttonPanel = new JPanel(new GridLayout(2,1,0,5));
//		buttonPanel.setBorder(new EmptyBorder(5,5,5,5));
//	
//		Box tex = new Box(BoxLayout.Y_AXIS);
//		tex.setBorder(
//				BorderFactory.createTitledBorder(
//						BorderFactory.createEtchedBorder(),
//						"Texture"
//				)
//		);
//		
//		ButtonGroup terrainTextureSelection = new ButtonGroup();
//
//		List<JRadioButton> buttons = new LinkedList<JRadioButton>();
//		JRadioButton button;
//		for (String[] texture : defaultLandscapes) {
//			final ImageData id = loadTexture(texture[1]);
//			if (id == null) continue;
//			imageMap.put(texture[0], id);
//			button = new JRadioButton(texture[0]);
//			texNameToButton.put(texture[0], button);
//			button.getModel().addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					setTexture(id);
//				}
//			});
//			terrainTextureSelection.add(button);
//			buttons.add(button);
//		}
//		
//		int numRows = Math.round(.1f+(imageMap.size()+2)*.5f);
//		JPanel texureSelection = new JPanel(new GridLayout(numRows, 2));
//		
//		for (JRadioButton b : buttons) {
//			texureSelection.add(b);
//		}
//		
//		button = new JRadioButton("none");
//		button.getModel().addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				setTexture(null);
//			}
//		});
//		terrainTextureSelection.add(button);
//		texureSelection.add(button);
//		texNameToButton.put("none", button);
//
//		button = new JRadioButton("custom");
//		button.getModel().addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				setCustomTexture();
//			}
//		});
//		terrainTextureSelection.add(button);
//		texureSelection.add(button);
//		texNameToButton.put("custom", button);
//
//		
//		JPanel texLoadPanel = new JPanel(new GridLayout(1,1));
//		texLoadPanel.setBorder(new EmptyBorder(5,5,0,5));
//		
//		final JButton textureLoadButton = new JButton("load");
//		textureLoadButton.setMargin(insets);
//		textureLoadButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				switchToTerrainTextureBrowser();
//			}
//		});
//		texLoadPanel.add(textureLoadButton);
//		
//		tex.add(texLoadPanel);
//		
//		Box texScaleBox = new Box(BoxLayout.X_AXIS);
//		texScaleBox.setBorder(new EmptyBorder(10, 5, 5, 0));
//		JLabel texScaleLabel = new JLabel("scale");
//		terrainTexScaleSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
//		terrainTexScaleSlider.setPreferredSize(new Dimension(70,20));
//		terrainTexScaleSlider.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent arg0) {
//				double d = .01 * terrainTexScaleSlider.getValue();
//				setTerrainTextureScale(Math.exp(Math.log(TEX_SCALE_RANGE) * d)/TEX_SCALE_RANGE * MAX_TEX_SCALE);
//			}
//		});
//
//		texScaleBox.add(texScaleLabel);
//		texScaleBox.add(terrainTexScaleSlider);
//		tex.add(texScaleBox);
//
//		selections.add(tex);
//		
//		JPanel bottom = new JPanel(new BorderLayout());
//		
//		terrainPanel.add(selections);
//		terrainPanel.add(BorderLayout.SOUTH, bottom);
//		
//		
//		// faces
//		faceColorChooser = new SimpleColorChooser();
//		faceColorChooser.setBorder(new EmptyBorder(8,8,8,8));
//		faceColorChooser.addChangeListener( new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				setFaceColor(faceColorChooser.getColor());
//			}
//		});
//		faceColorChooser.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				getViewerVR().switchToDefaultPanel();
//			}
//		});
//		
//		Box faceBox = new Box(BoxLayout.Y_AXIS);
//		faceBox.setBorder(
//				new CompoundBorder(
//						new EmptyBorder(0, 5, 2, 3),
//						BorderFactory.createTitledBorder(
//								BorderFactory.createEtchedBorder(),
//								"Appearance"
//						)
//				)
//		);
//		Box faceButtonBox = new Box(BoxLayout.X_AXIS);
//		//faceButtonBox.setBorder(boxBorder);
//		facesReflecting = new JCheckBox("reflect");
//		facesReflecting.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				setFacesReflecting(isFacesReflecting());
//			}
//		});
//		faceButtonBox.add(facesReflecting);
//		faceReflectionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
//		faceReflectionSlider.setPreferredSize(new Dimension(100,20));
//		faceReflectionSlider.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				setFaceReflection(getFaceReflection());
//			}
//		});
//		faceButtonBox.add(faceReflectionSlider);
//		JButton faceColorButton = new JButton("color");
//		faceBox.add(faceButtonBox);
//
//		Box transparencyBox = new Box(BoxLayout.X_AXIS);
//		transparencyBox.setBorder(new EmptyBorder(0,0,0,8));
//		transparency = new JCheckBox("transp");
//		transparency.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				setTransparencyEnabled(transparency.isSelected());
//			}
//		});
//		transparencySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 1);
//		transparencySlider.setPreferredSize(new Dimension(70,20));
//		transparencySlider.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent arg0) {
//				setTransparency(getTransparency());
//			}
//		});
//		transparencyBox.add(transparency);
//		transparencyBox.add(transparencySlider);
//		faceColorButton.setMargin(new Insets(0,5,0,5));
//		faceColorButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				switchToColorChooser();
//			}
//		});
//		transparencyBox.add(faceColorButton);
//		faceBox.add(transparencyBox);
//		
//		bottom.add(faceBox);
//		
//	}
//	
//	
//	public boolean isFacesReflecting() {
//		return facesReflecting.isSelected();
//	}
//	
//	public void setFacesReflecting(boolean b) {
//		facesReflecting.setSelected(b);
//		if (!isFacesReflecting()) {
//			if (cmFaces != null) {
//				TextureUtility.removeReflectionMap(getAppearance(), CommonAttributes.POLYGON_SHADER);
//				cmFaces = null;
//			}
//		} else {
//			cmFaces = TextureUtility.createReflectionMap(getAppearance(), CommonAttributes.POLYGON_SHADER, cubeMap);
//			setFaceReflection(getFaceReflection());
//		}
//	}
//	protected void setTexture(ImageData id) {
//		customTexture = false;
//		texture = id;
//	}
//	
//	protected void setCustomTexture() {
//		customTexture = true;
//		texture = null;
//	}
}
