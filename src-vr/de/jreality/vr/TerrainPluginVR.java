package de.jreality.vr;

import java.awt.BorderLayout;
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
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Secure;

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

	private IndexedFaceSet nonflatTerrain;
	private IndexedFaceSet flatTerrain = Primitives.plainQuadMesh(3, 3, 100, 100);
	private double[][] flatTerrainPoints;
	
	private SceneGraphComponent terrainNode;

	private Terrain terrain;

	public TerrainPluginVR() {
		super("terrain");

		// terrain
		terrain = new Terrain();

		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				makeTerrainTextureFileChooser();
				return null;
			}
		});
		
		// terrain
		terrainNode = AccessController.doPrivileged(new PrivilegedAction<SceneGraphComponent>() {
			public SceneGraphComponent run() {
				try {
					return Readers.read(Input.getInput(ViewerVR.class.getResource("terrain.3ds"))).getChildComponent(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		
		nonflatTerrain = (IndexedFaceSet) terrainNode.getGeometry();
		MatrixBuilder.euclidean().scale(1.1 / 3.).translate(0, 7, 0).assignTo(terrainNode);
		terrainNode.setName("terrain");
		IndexedFaceSet terrainGeom = (IndexedFaceSet) terrainNode.getGeometry();
		GeometryUtility.calculateAndSetNormals(terrainGeom);
		terrainGeom.setName("terrain Geometry");
		PickUtility.assignFaceAABBTree(terrainGeom);
		flatTerrainPoints = flatTerrain.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int n = flatTerrainPoints.length;
		for (int j=0; j<n; j++) {
			double y = flatTerrainPoints[j][1];
			//double z = flatTerrainPoints[j][2];
			flatTerrainPoints[j][1] = -8.5;
			flatTerrainPoints[j][2] = y;
		}
		flatTerrain.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(flatTerrainPoints, 3));
		Random r = new Random();
		for (int j=0; j<n; j++) {			
			flatTerrainPoints[j][0] /= 3;
			flatTerrainPoints[j][0] += (-.5+r.nextDouble())*1E-1;
			flatTerrainPoints[j][1] = -1.5;
			flatTerrainPoints[j][2] /= 3;
			flatTerrainPoints[j][2] += (-.5+r.nextDouble())*1E-1;
		}
		GeometryUtility.calculateAndSetNormals(flatTerrain);
		flatTerrain.setName("flat terrain Geometry");
		PickUtility.assignFaceAABBTree(flatTerrain);
		makeTerrainTab();
	}

	@Override
	public void setViewerVR(ViewerVR vvr) {
		super.setViewerVR(vvr);
		getViewerVR().setTerrain(terrainNode);
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
		terrainPanel = new JPanel(new BorderLayout());
		Box terrainBox = new Box(BoxLayout.X_AXIS);
		JPanel geom = terrain.getGeometrySelection();
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Geometry");
		geom.setBorder(title);
		terrainBox.add(geom);
		JPanel tex = new JPanel(new BorderLayout());
		tex.add(BorderLayout.CENTER, terrain.getTexureSelection());
		title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Texture");
		tex.setBorder(title);
		terrainBox.add(tex);
		
		final JButton textureLoadButton = new JButton("load ...");
		textureLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTerrainTextureBrowser();
			}
		});

		textureLoadButton.setEnabled(terrain.getTextureType() == Terrain.TextureType.CUSTOM);
		
		terrain.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateTerrain();
				textureLoadButton.setEnabled(terrain.getTextureType() == Terrain.TextureType.CUSTOM);
			}
		});
		
		tex.add(BorderLayout.SOUTH, textureLoadButton);
		
		terrainPanel.add(BorderLayout.WEST, geom);
		terrainPanel.add(BorderLayout.EAST, tex);
		
		Box texScaleBox = new Box(BoxLayout.X_AXIS);
		texScaleBox.setBorder(new EmptyBorder(70, 5, 5, 0));
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

		terrainPanel.add(BorderLayout.SOUTH, texScaleBox);
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

	private void setTerrainTexture(ImageData tex) {
		if (tex != null) {
			terrainTex = TextureUtility.createTexture(getViewerVR().getTerrainAppearance(), "polygonShader", tex);
		} else {
			TextureUtility.removeTexture(getViewerVR().getTerrainAppearance(), "polygonShader");
		}
	}

	private void updateTerrain() {
		boolean flat;
		switch (terrain.getGeometryType()) {
		case FLAT:
			flat = true;
			break;
		case NON_FLAT:
			flat = false;
			break;
		default:
			flat = getViewerVR().getEnvironment().isFlatTerrain();
		}
		terrainNode.setGeometry(flat ? flatTerrain : nonflatTerrain);
		//if (last != terrainNode.getGeometry()) computeShadow();
		
		// XXX:
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
