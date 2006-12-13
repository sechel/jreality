package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Secure;
import de.jtem.beans.SimpleColorChooser;

public class TerrainPluginVR extends AbstractPluginVR {

	// maximal value of texture scale
	private static final double MAX_TEX_SCALE = 400;
	
	// ratio of maximal value and minimal value of texture scale
	private static final double TEX_SCALE_RANGE = 400;

	// terrain tab
	private JSlider terrainTexScaleSlider;
	private JPanel terrainPanel;

	private Texture2D terrainTex;
	private JFileChooser terrainTexFileChooser;
	private File terrainTexFile;

	private SceneGraphComponent nonflatTerrain;
	private SceneGraphComponent flatTerrain;
	
	private Terrain terrain;

	private JFileChooser terrainFileChooser;

	protected SceneGraphComponent customTerrain;

	protected SceneGraphComponent terrainNode = new SceneGraphComponent("terrain alignment");
	
	private RotateBox rotateBox = new RotateBox();
	
	protected File terrainFile;

	private JPanel rotatePanel;

	private SimpleColorChooser colorChooser;

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
		
		// terrain
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
		flatTerrain = new SceneGraphComponent("flat terrain");
		MatrixBuilder.euclidean().translate(0,0,20).rotateX(-Math.PI/2).assignTo(flatTerrain);
		flatTerrain.setGeometry(Primitives.plainQuadMesh(0.1, 0.1, 100, 100));
		PickUtility.assignFaceAABBTrees(flatTerrain);
		
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
		updateTerrain();
	}
	
	private void makeTerrainTab() {
		
		colorChooser = new SimpleColorChooser();
		colorChooser.setBorder(new EmptyBorder(8,8,8,8));
		colorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setColor(colorChooser.getColor());
			}
		});
		colorChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getViewerVR().switchToDefaultPanel();
			}
		});

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
		JPanel geomPanel = new JPanel(new BorderLayout());
		JPanel geom = terrain.getGeometrySelection();
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Geometry");
		geomPanel.setBorder(title);
		geomPanel.add(BorderLayout.CENTER, geom);
		final JButton terrainLoadButton = new JButton("load");
		terrainLoadButton.setMargin(insets);
		terrainLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTerrainBrowser();
			}
		});

		final JButton rotateButton = new JButton("rotate");
		rotateButton.setMargin(insets);
		rotateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToRotateBrowser();
			}
		});

		JPanel terrainLoadPanel = new JPanel(new FlowLayout());
		terrainLoadPanel.add(rotateButton);
		terrainLoadPanel.add(terrainLoadButton);
		terrainLoadButton.setEnabled(terrain.getGeometryType() == Terrain.GeometryType.CUSTOM);
		rotateButton.setEnabled(terrain.getGeometryType() == Terrain.GeometryType.CUSTOM);
		geomPanel.add(BorderLayout.SOUTH, terrainLoadPanel);
		
		JPanel tex = new JPanel(new BorderLayout());
		tex.add(BorderLayout.CENTER, terrain.getTexureSelection());
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Texture");
		tex.setBorder(title);

		JPanel texLoadPanel = new JPanel(new FlowLayout());
		final JButton textureLoadButton = new JButton("load");
		textureLoadButton.setMargin(insets);
		textureLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTerrainTextureBrowser();
			}
		});

		textureLoadButton.setEnabled(terrain.getTextureType() == Terrain.TextureType.CUSTOM);
		
		texLoadPanel.add(textureLoadButton);

		final JButton colorButton = new JButton("color");
		colorButton.setMargin(insets);
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToColorBrowser();
			}
		});
		
		
		tex.add(BorderLayout.SOUTH, texLoadPanel);
		texLoadPanel.add(colorButton);
		
		selections.add(geomPanel);
		selections.add(tex);
		
		terrain.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateTerrain();
				textureLoadButton.setEnabled(terrain.getTextureType() == Terrain.TextureType.CUSTOM);
				terrainLoadButton.setEnabled(terrain.getGeometryType() == Terrain.GeometryType.CUSTOM);
				rotateButton.setEnabled(terrain.getGeometryType() == Terrain.GeometryType.CUSTOM);
			}
		});

		Box texScaleBox = new Box(BoxLayout.X_AXIS);
		texScaleBox.setBorder(new EmptyBorder(10, 5, 5, 0));
		JLabel texScaleLabel = new JLabel("scale");
		terrainTexScaleSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,0);
		terrainTexScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double d = .01 * terrainTexScaleSlider.getValue();
				setTerrainTextureScale(Math.exp(Math.log(TEX_SCALE_RANGE) * d)/TEX_SCALE_RANGE * MAX_TEX_SCALE);
			}
		});
		texScaleBox.add(texScaleLabel);
		texScaleBox.add(terrainTexScaleSlider);

		terrainPanel.add(selections);
		terrainPanel.add(BorderLayout.SOUTH, texScaleBox);
	}
	
	protected void setColor(Color color) {
		colorChooser.setColor(color);
		String attribute = CommonAttributes.POLYGON_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR;
		getViewerVR().getTerrainAppearance().setAttribute(attribute,color);
	}

	protected void switchToColorBrowser() {
		getViewerVR().switchTo(colorChooser);
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
						setTerrainTextureScale(terrainTexScaleSlider.getValue()*0.1);
						
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
		boolean flat=false;
		// remove last terrain
		while (terrainNode.getChildComponentCount() > 0) terrainNode.removeChild(terrainNode.getChildComponent(0));
		switch (terrain.getGeometryType()) {
		case FLAT:
			terrainNode.addChild(flatTerrain);
			flat = true;
			new Matrix().assignTo(terrainNode);
			break;
		case NON_FLAT:
			terrainNode.addChild(nonflatTerrain);
			new Matrix().assignTo(terrainNode);
			break;
		case CUSTOM:
			if (customTerrain != null) terrainNode.addChild(customTerrain);
			break;
		default:
			flat = getViewerVR().getEnvironment().isFlatTerrain();
			new Matrix().assignTo(terrainNode);
			terrainNode.addChild(flat ? flatTerrain : nonflatTerrain);
		}

		getViewerVR().setTerrain(terrainNode);
		getViewerVR().getTerrainAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, flat);

		Environment env = getViewerVR().getEnvironment();
		switch (terrain.getTextureType()) {
		case NONE:
			setTerrainTexture(null);
			break;
		case TILES:
			try {
				setTerrainTexture(ImageData.load(Input.getInput("textures/recycfloor1_fin.png")));
				setTerrainTextureScale(50.);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case CUSTOM:
			try {
					setTerrainTexture(terrainTexFile == null ? null : ImageData.load(Input.getInput(terrainTexFile)));
					setTerrainTextureScale(getTerrainTextureScale());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
		default:
		ImageData terrainTex = env.getTexture();
		setTerrainTexture(terrainTex);
		setTerrainTextureScale(env.getTextureScale());
		}
	}
}
