package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Secure;
import de.jreality.vr.Terrain.GeometryType;
import de.jtem.beans.SimpleColorChooser;

public class TerrainPluginVR extends AbstractPluginVR {

	// maximal value of texture scale
	private static final double MAX_TEX_SCALE = 10;
	
	// ratio of maximal value and minimal value of texture scale
	private static final double TEX_SCALE_RANGE = 400;

	// defaults
	private static final double DEFAULT_TEX_SCALE = .5;
	private static final String DEFAULT_TEXTURE = "grid";
	private static final String DEFAULT_TERRAIN = "flat";
	private static final Color DEFAULT_FACE_COLOR=Color.white;
	private static final boolean DEFAULT_REFLECTING=true;
	private static final double DEFAULT_REFLECTION=0.2;
	private static final boolean DEFAULT_TRANSPARENT=true;
	private static final double DEFAULT_TRANSPARENCY=0.3;

	
	// terrain tab
	private JSlider terrainTexScaleSlider;
	private JPanel terrainPanel;

	private Texture2D terrainTex;
	private JFileChooser terrainTexFileChooser;
	private File terrainTexFile;

	private SceneGraphComponent nonflatTerrain;
	
	static final SceneGraphComponent FLAT_TERRAIN;
	static {
		FLAT_TERRAIN = new SceneGraphComponent("flat terrain");
		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(FLAT_TERRAIN);
		//FLAT_TERRAIN.setGeometry(Primitives.plainQuadMesh(1, 1, 50, 50));
		FLAT_TERRAIN.setGeometry(BigMesh.bigMesh(50, 50, 2000));
		FLAT_TERRAIN.getGeometry().setGeometryAttributes("infinite plane", Boolean.TRUE);
		PickUtility.assignFaceAABBTrees(FLAT_TERRAIN);
	}
	
	private Terrain terrain;

	private JFileChooser terrainFileChooser;
	private JPanel rotatePanel;
	private RotateBox rotateBox = new RotateBox();

	protected SceneGraphComponent customTerrain;
	
	protected SceneGraphComponent terrainNode = new SceneGraphComponent("terrain alignment");
	
	
	protected File terrainFile;

	
	
	private SimpleColorChooser faceColorChooser;
	private JCheckBox facesReflecting;
	private JSlider faceReflectionSlider;
	private JCheckBox transparency;
	private JSlider transparencySlider;

	private CubeMap cmFaces;
	private ImageData[] cubeMap;

	public TerrainPluginVR() {
		super("terrain");

		// terrain
		terrain = new Terrain();

		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				makeTerrainTextureFileChooser();
				makeTerrainFileChooser();
				return null;
			}
		});
		
		nonflatTerrain = AccessController.doPrivileged(new PrivilegedAction<SceneGraphComponent>() {
			public SceneGraphComponent run() {
				try {
					return Readers.read(Input.getInput(ViewerVR.class.getResource("terrain.3ds")));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		PickUtility.assignFaceAABBTrees(nonflatTerrain);
		nonflatTerrain.accept(new SceneGraphVisitor() {
			public void visit(SceneGraphComponent c) {
				c.childrenWriteAccept(this, false, false, false, false, true, false);
			}
			public void visit(IndexedFaceSet i) {
				GeometryUtility.calculateAndSetVertexNormals(i);		
			}
		});
		
		rotateBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				rotateBox.getMatrix().assignTo(terrainNode);
				updateTerrain();
			}
		});
		makeTerrainTab();
	}

	@Override
	public void setViewerVR(ViewerVR vvr) {
		super.setViewerVR(vvr);
	}
	
	@Override
	public JPanel getPanel() {
		return terrainPanel;
	}
	
	@Override
	public void environmentChanged() {
		cubeMap=getViewerVR().getEnvironment();
		setFacesReflecting(isFacesReflecting());
	}
	
	private void makeTerrainTab() {
		
		Insets insets = new Insets(0, 5, 0, 5);
		
		// create rotate panel
		rotatePanel = new JPanel(new BorderLayout());
		rotatePanel.setBorder(new EmptyBorder(40, 40, 40, 40));
		rotatePanel.add(rotateBox);
		final JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getViewerVR().switchToDefaultPanel();
			}
		});
		rotatePanel.add(BorderLayout.SOUTH, okButton);
		
		terrainPanel = new JPanel(new BorderLayout());
		Box selections = new Box(BoxLayout.X_AXIS);
		selections.setBorder(new EmptyBorder(0,5,0,2));
		
		JPanel geomPanel = new JPanel(new BorderLayout());
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Geometry");
		geomPanel.setBorder(title);
		JPanel geom = terrain.getGeometrySelection();
		geomPanel.add(BorderLayout.NORTH,geom);
		JPanel buttonPanel = new JPanel(new GridLayout(2,1,0,5));
		buttonPanel.setBorder(new EmptyBorder(5,5,5,5));
		
		final JButton terrainLoadButton = new JButton("load");
		terrainLoadButton.setMargin(insets);
		terrainLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTerrainBrowser();
			}
		});
		buttonPanel.add(terrainLoadButton);

		final JButton rotateButton = new JButton("rotate");
		rotateButton.setMargin(insets);
		rotateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToRotateBrowser();
			}
		});
		buttonPanel.add(rotateButton);
		
		geomPanel.add(BorderLayout.CENTER, buttonPanel);

		terrainLoadButton.setEnabled(terrain.getGeometryType() == Terrain.GeometryType.CUSTOM);
		rotateButton.setEnabled(terrain.getGeometryType() == Terrain.GeometryType.CUSTOM);
		
		Box tex = new Box(BoxLayout.Y_AXIS);
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Texture");
		tex.setBorder(title);
		tex.add(terrain.getTexureSelection());
		
		JPanel texLoadPanel = new JPanel(new GridLayout(1,1));
		texLoadPanel.setBorder(new EmptyBorder(5,5,0,5));
		
		final JButton textureLoadButton = new JButton("load");
		textureLoadButton.setMargin(insets);
		textureLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTerrainTextureBrowser();
			}
		});
		textureLoadButton.setEnabled(terrain.isCustomTexture());
		texLoadPanel.add(textureLoadButton);
		
		tex.add(texLoadPanel);
		
		Box texScaleBox = new Box(BoxLayout.X_AXIS);
		texScaleBox.setBorder(new EmptyBorder(10, 5, 5, 0));
		JLabel texScaleLabel = new JLabel("scale");
		terrainTexScaleSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		terrainTexScaleSlider.setPreferredSize(new Dimension(70,20));
		terrainTexScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double d = .01 * terrainTexScaleSlider.getValue();
				setTerrainTextureScale(Math.exp(Math.log(TEX_SCALE_RANGE) * d)/TEX_SCALE_RANGE * MAX_TEX_SCALE);
			}
		});
		texScaleBox.add(texScaleLabel);
		texScaleBox.add(terrainTexScaleSlider);
		tex.add(texScaleBox);
		
		selections.add(geomPanel);
		selections.add(tex);
		
		terrain.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateTerrain();
				textureLoadButton.setEnabled(terrain.isCustomTexture());
				terrainLoadButton.setEnabled(terrain.getGeometryType() == Terrain.GeometryType.CUSTOM);
				rotateButton.setEnabled(terrain.getGeometryType() == Terrain.GeometryType.CUSTOM);
			}
		});

		JPanel bottom = new JPanel(new BorderLayout());
		
		terrainPanel.add(selections);
		terrainPanel.add(BorderLayout.SOUTH, bottom);
		
		
		// faces
		faceColorChooser = new SimpleColorChooser();
		faceColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		faceColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setFaceColor(faceColorChooser.getColor());
			}
		});
		faceColorChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getViewerVR().switchToDefaultPanel();
			}
		});
		
		Box faceBox = new Box(BoxLayout.Y_AXIS);
		faceBox.setBorder(
				new CompoundBorder(
						new EmptyBorder(0, 5, 2, 3),
						BorderFactory.createTitledBorder(
								BorderFactory.createEtchedBorder(),
								"Appearance"
						)
				)
		);
		Box faceButtonBox = new Box(BoxLayout.X_AXIS);
		//faceButtonBox.setBorder(boxBorder);
		facesReflecting = new JCheckBox("reflect");
		facesReflecting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFacesReflecting(isFacesReflecting());
			}
		});
		faceButtonBox.add(facesReflecting);
		faceReflectionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		faceReflectionSlider.setPreferredSize(new Dimension(100,20));
		faceReflectionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setFaceReflection(getFaceReflection());
			}
		});
		faceButtonBox.add(faceReflectionSlider);
		JButton faceColorButton = new JButton("color");
		faceBox.add(faceButtonBox);

		Box transparencyBox = new Box(BoxLayout.X_AXIS);
		transparencyBox.setBorder(new EmptyBorder(0,0,0,8));
		transparency = new JCheckBox("transp");
		transparency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setTransparencyEnabled(transparency.isSelected());
			}
		});
		transparencySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 1);
		transparencySlider.setPreferredSize(new Dimension(70,20));
		transparencySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setTransparency(getTransparency());
			}
		});
		transparencyBox.add(transparency);
		transparencyBox.add(transparencySlider);
		faceColorButton.setMargin(new Insets(0,5,0,5));
		faceColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToColorChooser();
			}
		});
		transparencyBox.add(faceColorButton);
		faceBox.add(transparencyBox);
		
		bottom.add(faceBox);

	}
	
	protected void switchToColorChooser() {
		getViewerVR().switchTo(faceColorChooser);
	}

	private Appearance getAppearance() {
		return getViewerVR().getTerrainAppearance();
	}
	
	public double getFaceReflection() {
		return .01 * faceReflectionSlider.getValue();
	}
	
	public void setFaceReflection(double d) {
		faceReflectionSlider.setValue((int)(100*d));
		if (cmFaces != null) cmFaces.setBlendColor(new Color(1f, 1f, 1f, (float) d));
	}
	
	public double getTransparency() {
		return .01 * transparencySlider.getValue();
	}
	
	public void setTransparency(double d) {
		transparencySlider.setValue((int)(100 * d));
		getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, d);
	}
		
	public boolean isFacesReflecting() {
		return facesReflecting.isSelected();
	}
	
	public void setFacesReflecting(boolean b) {
		facesReflecting.setSelected(b);
		if (!isFacesReflecting()) {
			if (cmFaces != null) {
				TextureUtility.removeReflectionMap(getAppearance(), CommonAttributes.POLYGON_SHADER);
				cmFaces = null;
			}
		} else {
			cmFaces = TextureUtility.createReflectionMap(getAppearance(), CommonAttributes.POLYGON_SHADER, cubeMap);
			setFaceReflection(getFaceReflection());
		}
	}
	
	public Color getFaceColor() {
		return (Color) getAppearance().getAttribute(
				CommonAttributes.POLYGON_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR
		);
	}
	
	public void setFaceColor(Color c) {
		faceColorChooser.setColor(c);
		String attribute = CommonAttributes.POLYGON_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR;
		getAppearance().setAttribute(attribute,c);
	}
	
	public boolean isTransparencyEnabled() {
		return transparency.isSelected();
	}
	
	public void setTransparencyEnabled(boolean b) {
		transparency.setSelected(b);
		getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, b);
	}

	private void makeTerrainTextureFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		String texDir = ".";
		String dataDir = Secure.getProperty("jreality.data");
		if (dataDir!= null) texDir = dataDir+"/textures";
		File defaultDir = new File(texDir);
		terrainTexFileChooser = new JFileChooser(!defaultDir.exists() ? view.getHomeDirectory() : defaultDir, view);
		terrainTexFileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File file = terrainTexFileChooser.getSelectedFile();
				try {
					if (ev.getActionCommand() == JFileChooser.APPROVE_SELECTION
							&& file != null) {
						terrainTexFile = file;
						ImageData img = ImageData.load(Input.getInput(terrainTexFile));
						//tex = TextureUtility.createTexture(contentAppearance, "polygonShader", img, false);
						setTerrainTexture(img);
						setTerrainTextureScale(getTerrainTextureScale());
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				getViewerVR().switchToDefaultPanel();
			}
		});
	}

	private void makeTerrainFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		String texDir = ".";
		String dataDir = Secure.getProperty("jreality.data");
		if (dataDir!= null) texDir = dataDir;
		File defaultDir = new File(texDir);
		terrainFileChooser = new JFileChooser(!defaultDir.exists() ? view.getHomeDirectory() : defaultDir, view);
		terrainFileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File file = terrainFileChooser.getSelectedFile();
				try {
					if (ev.getActionCommand() == JFileChooser.APPROVE_SELECTION
							&& file != null) {
						terrainFile = file;
						customTerrain = Readers.read(Input.getInput(terrainFile));
						getViewerVR().setTerrain(customTerrain);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				getViewerVR().switchToDefaultPanel();
			}
		});
	}

	public double getTerrainTextureScale() {
		double d = .01 * terrainTexScaleSlider.getValue();
		return Math.exp(Math.log(TEX_SCALE_RANGE) * d)/TEX_SCALE_RANGE * MAX_TEX_SCALE;
	}

	public void setTerrainTextureScale(double d) {
		terrainTexScaleSlider.setValue(
				(int)(Math.log(d / MAX_TEX_SCALE * TEX_SCALE_RANGE)/Math.log(TEX_SCALE_RANGE)*100)
			);
		if (terrainTex != null) {
			terrainTex.setTextureMatrix(MatrixBuilder.euclidean().scale(d).getMatrix());
		}
	}
	
	public void switchToTerrainTextureBrowser() {
		getViewerVR().switchToFileChooser(terrainTexFileChooser);
	}

	protected void switchToTerrainBrowser() {
		getViewerVR().switchToFileChooser(terrainFileChooser);
	}

	protected void switchToRotateBrowser() {
		getViewerVR().switchTo(rotatePanel);
	}


	private void setTerrainTexture(ImageData tex) {
		if (tex != null) {
			terrainTex = TextureUtility.createTexture(getViewerVR().getTerrainAppearance(), "polygonShader", tex);
		} else {
			TextureUtility.removeTexture(getViewerVR().getTerrainAppearance(), "polygonShader");
		}
	}

	private void updateTerrain() {
		// remove last terrain
		while (terrainNode.getChildComponentCount() > 0) terrainNode.removeChild(terrainNode.getChildComponent(0));
		switch (terrain.getGeometryType()) {
		case FLAT:
			terrainNode.addChild(FLAT_TERRAIN);
			new Matrix().assignTo(terrainNode);
			break;
		case NON_FLAT:
			terrainNode.addChild(nonflatTerrain);
			new Matrix().assignTo(terrainNode);
			break;
		default:
			if (customTerrain != null) terrainNode.addChild(customTerrain);
		}

		getViewerVR().setTerrain(terrainNode);

		if (terrain.isCustomTexture()) { 
			try {
				setTerrainTexture(terrainTexFile == null ? null : ImageData.load(Input.getInput(terrainTexFile)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			setTerrainTexture(terrain.getTexture());
		}
		setTerrainTextureScale(getTerrainTextureScale());
	}
	
	@Override
	public void storePreferences(Preferences prefs) {
		prefs.putDouble("terrain.texScale", getTerrainTextureScale());
		prefs.put("terrain.texture", terrain.getTextureName());
		if (terrain.isCustomTexture() && terrainTexFile != null) {
			prefs.put("terrain.customTex", terrainTexFile.getAbsolutePath());
		}
		
		prefs.put("terrain.geometry", terrain.getGeometryName());
		if (terrain.getGeometryType() == GeometryType.CUSTOM && terrainFile != null) {
			prefs.put("terrain.customTerrain", terrainFile.getAbsolutePath());
		}
		
		prefs.putInt("terrain.colorRed", getFaceColor().getRed());
		prefs.putInt("terrain.colorGreen", getFaceColor().getGreen());
		prefs.putInt("terrain.colorBlue", getFaceColor().getBlue());

		prefs.putDouble("terrain.reflection", getFaceReflection());
		prefs.putBoolean("terrain.reflecting", isFacesReflecting());
		prefs.putBoolean("terrain.transparencyEnabled", isTransparencyEnabled());
		prefs.putDouble("terrain.transparency", getTransparency());

	}
	
	@Override
	public void restoreDefaults() {
		setTerrainTextureScale(DEFAULT_TEX_SCALE);
		
		terrain.setTextureByName(DEFAULT_TEXTURE);
		terrainTexFile = null;
		
		terrain.setGeometryByName(DEFAULT_TERRAIN);
		terrainFile = null;
		
		setFaceColor(DEFAULT_FACE_COLOR);
		setFaceReflection(DEFAULT_REFLECTION);
		setFacesReflecting(DEFAULT_REFLECTING);
		setTransparencyEnabled(DEFAULT_TRANSPARENT);
		setTransparency(DEFAULT_TRANSPARENCY);

	}
	
	@Override
	public void restorePreferences(Preferences prefs) {
		setTerrainTextureScale(prefs.getDouble("terrain.texScale", DEFAULT_TEX_SCALE));
		
		String texName = prefs.get("terrain.texture", DEFAULT_TEXTURE);
		if ("custom".equals(texName)) {
			String fileName = prefs.get("terrain.customTex", null);
			if (fileName != null) {
				terrainTexFile = new File(fileName);
				if (!terrainTexFile.exists()) terrainTexFile = null;
			}
		}
		terrain.setTextureByName(texName);

		String terrainName = prefs.get("terrain.geometry", DEFAULT_TERRAIN);
		if ("custom".equals(terrainName)) {
			String fileName = prefs.get("terrain.customTerrain", null);
			System.out.println("terrainFile="+fileName);
			if (fileName != null) {
				terrainFile = new File(fileName);
				if (!terrainFile.exists()) terrainFile = null;
				else
					try {
						customTerrain = Readers.read(terrainFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		terrain.setGeometryByName(terrainName);
		
		int r = prefs.getInt("terrain.colorRed", DEFAULT_FACE_COLOR.getRed());
		int g = prefs.getInt("terrain.colorGreen", DEFAULT_FACE_COLOR.getGreen());
		int b = prefs.getInt("terrain.colorBlue", DEFAULT_FACE_COLOR.getBlue());
		setFaceColor(new Color(r,g,b));

		setFaceReflection(prefs.getDouble("terrain.reflection", DEFAULT_REFLECTION));
		setFacesReflecting(prefs.getBoolean("terrain.reflecting", DEFAULT_REFLECTING));
		setTransparencyEnabled(prefs.getBoolean("terrain.transparencyEnabled", DEFAULT_TRANSPARENT));
		setTransparency(prefs.getDouble("terrain.transparency", DEFAULT_TRANSPARENCY));
    }
}
