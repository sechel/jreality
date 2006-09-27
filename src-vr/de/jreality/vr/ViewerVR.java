package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.ScenePanel;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;

public class ViewerVR {
	
	protected static final double PI2 = Math.PI/2;
	
	private SceneGraphComponent sceneRoot=new SceneGraphComponent(),
	sceneNode=new SceneGraphComponent(),
	avatarNode=new SceneGraphComponent(),
	camNode=new SceneGraphComponent(),
	lightNode=new SceneGraphComponent(),
	terrainNode;
	
	private SceneGraphComponent currentContent;
	
	private Appearance terrainAppearance=new Appearance(),
	rootAppearance=new Appearance(),
	contentAppearance=new Appearance();
	
	private DirectionalLight light = new DirectionalLight();
	
	private SceneGraphPath cameraPath, avatarPath, emptyPickPath;
	
	private double diam=20, offset=.3;
	
	Landscape landscape = new Landscape();

	private JFileChooser chooser;

	private JSlider sizeSlider;

	private JSlider groundSlider;

	private ScenePanel sp;
	
	public ViewerVR() throws IOException {
		
		boolean portal = "portal".equals(System.getProperty("de.jreality.scene.tool.Config"));
		
		sceneRoot.setName("root");
		sceneNode.setName("scene");
		avatarNode.setName("avatar");
		camNode.setName("camNode");
		lightNode.setName("sun");
		sceneRoot.addChild(sceneNode);
		
		rootAppearance.setName("root app");
		ShaderUtility.createRootAppearance(rootAppearance);
		rootAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.AMBIENT_COEFFICIENT, 0.1);
		rootAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.AMBIENT_COEFFICIENT, 0.03);
		rootAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.PICKABLE, false);
		rootAppearance.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE, false);
		sceneRoot.setAppearance(rootAppearance);
		
		terrainAppearance.setAttribute("showLines", false);
		terrainAppearance.setAttribute("showPoints", false);
		terrainAppearance.setAttribute("diffuseColor", Color.white);
		
		Camera cam = new Camera();
		cam.setNear(0.01);
		cam.setFar(1500);
		
		if (portal) {
			cam.setOnAxis(false);
			cam.setStereo(true);
		}
		
		// lights
		light.setIntensity(2);
		lightNode.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{0,1,1}).assignTo(lightNode);
		sceneRoot.addChild(lightNode);
		
		// prepare paths
		sceneRoot.addChild(avatarNode);
		avatarNode.addChild(camNode);
		camNode.setCamera(cam);
		cameraPath = new SceneGraphPath();
		cameraPath.push(sceneRoot);
		emptyPickPath=cameraPath.pushNew(sceneNode);
		cameraPath.push(avatarNode);
		cameraPath.push(camNode);
		avatarPath=cameraPath.popNew();
		cameraPath.push(cam);
		
		MatrixBuilder.euclidean().translate(0,1.7,0).assignTo(camNode);
		
		// add tools
		ShipNavigationTool shipNavigationTool = new ShipNavigationTool();
		avatarNode.addTool(shipNavigationTool);
		if (portal) shipNavigationTool.setPollingDevice(false);
		
		if (!portal) camNode.addTool(new HeadTransformationTool());
		else {
			try {
				Tool t = (Tool) Class.forName("de.jreality.tools.PortalHeadMoveTool").newInstance();
				camNode.addTool(t);
			} catch (Throwable t) {
				// XXX
			}
		}
		
		contentAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		contentAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.red);
		sceneNode.setAppearance(contentAppearance);
		
		sceneRoot.addTool(new PickShowTool(null, 0.005));
		setAvatarPosition(0, 0.7, 30);
		
		//avatarNode.addTool(new PointerDisplayTool());
		
		//sceneNode.addTool(new RotateTool());
		//DraggingTool draggingTool = new DraggingTool();
		//draggingTool.setMoveChildren(true);
		//sceneNode.addTool(draggingTool);
		
		init();
	}
	
	private void init() throws IOException {
		// prepare terrain
		terrainNode = Readers.read(Input.getInput("de/jreality/vr/terrain.3ds")).getChildComponent(0);
		MatrixBuilder.euclidean().scale(1/3.).translate(0,9,0).assignTo(terrainNode);
		terrainNode.setName("terrain");
		IndexedFaceSet terrainGeom = (IndexedFaceSet) terrainNode.getGeometry();
		GeometryUtility.calculateAndSetNormals(terrainGeom);
		terrainGeom.setName("terrain Geometry");
		PickUtility.assignFaceAABBTree(terrainGeom);
		
		terrainNode.setAppearance(terrainAppearance);
		sceneRoot.addChild(terrainNode);
		
		Landscape l = new Landscape();
		l.setToolScene(this);
		sp = new ScenePanel();
		sp.setPanelWidth(1);
		JTabbedPane tabs = new JTabbedPane();
		
		JPanel placementPanel = new JPanel(new BorderLayout());
		placementPanel.setBorder(new EmptyBorder(5,5,5,5));
		Box placementBox = new Box(BoxLayout.X_AXIS);
		Box sizeBox = new Box(BoxLayout.Y_AXIS);
		sizeBox.setBorder(new EmptyBorder(5,5,5,5));
		JLabel sizeLabel = new JLabel("size");
		sizeSlider = new JSlider(SwingConstants.VERTICAL,100,10000,(int)(diam*100));
		sizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setDiam(0.01*sizeSlider.getValue());
				alignContent(diam, offset, null);
			}
		});
		sizeSlider.setMaximumSize(new Dimension(20,160));
		sizeBox.add(sizeLabel);
		sizeBox.add(sizeSlider);
		Box groundBox = new Box(BoxLayout.Y_AXIS);
		groundBox.setBorder(new EmptyBorder(5,5,5,5));
		JLabel groundLabel = new JLabel("level");
		groundSlider = new JSlider(SwingConstants.VERTICAL,0,200,(int)(offset*100));
		groundSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setOffset(0.01*groundSlider.getValue());
				alignContent(diam, offset, null);
			}
		});
		groundSlider.setMaximumSize(new Dimension(20,160));
		groundBox.add(groundLabel);
		groundBox.add(groundSlider);
		URL imgURL = ViewerVR.class.getResource("rotleft.gif");
		ImageIcon rotateLeft = new ImageIcon(imgURL);
		imgURL = ViewerVR.class.getResource("rotright.gif");
		ImageIcon rotateRight = new ImageIcon(imgURL);
		
		JPanel rotateBox = new JPanel(new GridLayout(3,3));
		rotateBox.setBorder(new EmptyBorder(5,5,5,5));
		
		JButton xRotateLeft = new JButton(rotateLeft);
		xRotateLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateX(-PI2).getMatrix());
			}
		});
		Insets insets = new Insets(0,0,0,0);
		Dimension dim = new Dimension(25,22);
		xRotateLeft.setMargin(insets);
		xRotateLeft.setMaximumSize(dim);
		rotateBox.add(xRotateLeft);
		JLabel label = new JLabel("x");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		rotateBox.add(label);
		JButton xRotateRight = new JButton(rotateRight);
		xRotateRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateX(PI2).getMatrix());
			}
		});
		xRotateRight.setMargin(insets);
		xRotateRight.setMaximumSize(dim);
		rotateBox.add(xRotateRight);
		
		JButton yRotateLeft = new JButton(rotateLeft);
		yRotateLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateY(-PI2).getMatrix());
			}
		});
		yRotateLeft.setMargin(insets);
		yRotateLeft.setMaximumSize(dim);
		rotateBox.add(yRotateLeft);
		label = new JLabel("y");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		rotateBox.add(label);
		JButton yRotateRight = new JButton(rotateRight);
		yRotateRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateY(PI2).getMatrix());
			}
		});
		yRotateRight.setMargin(insets);
		yRotateRight.setMaximumSize(dim);
		rotateBox.add(yRotateRight);
		
		JButton zRotateLeft = new JButton(rotateLeft);
		zRotateLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateZ(-PI2).getMatrix());
			}
		});
		zRotateLeft.setMargin(insets);
		zRotateLeft.setMaximumSize(dim);
		rotateBox.add(zRotateLeft);
		label = new JLabel("z");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		rotateBox.add(label);
		JButton zRotateRight = new JButton(rotateRight);
		zRotateRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateZ(PI2).getMatrix());
			}
		});
		zRotateRight.setMargin(insets);
		zRotateRight.setMaximumSize(dim);
		rotateBox.add(zRotateRight);
		
		JPanel p = new JPanel(new BorderLayout());
		JPanel dummy = new JPanel(new BorderLayout());
		JButton loadButton = new JButton("load ...");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				load();
			}
		});
		loadButton.setMargin(insets);
		dummy.add(loadButton);
//		Dimension  d = new Dimension(50, 60);
//		dummy.setPreferredSize(d);
//		dummy.setMaximumSize(d);
//		dummy.setMinimumSize(d);
		dummy.setBorder(new EmptyBorder(5,30,20,30));
		p.add("North", dummy );
		dummy = new JPanel();
//		dummy.setPreferredSize(d);
//		dummy.setMaximumSize(d);
//		dummy.setMinimumSize(d);
		p.add("South", dummy );
		p.add("Center", rotateBox);
		
		placementBox.add(sizeBox);
		placementBox.add(groundBox);
		placementBox.add(p);
		placementPanel.add(placementBox);
		tabs.add("placement", placementPanel);
		
		tabs.add("landscape",l.getSelectionComponent());
		
		sp.getFrame().getContentPane().add(tabs);
		sp.getFrame().setSize(200,190);
		
		getTerrainNode().addTool(sp.getPanelTool());
		
		chooser = FileLoaderDialog.createFileChooser();
		
		chooser.addActionListener(new ActionListener() {
			Container oldCmp = sp.getFrame().getContentPane();
			public void actionPerformed(ActionEvent ev) {
				File file = chooser.getSelectedFile();
				try {
					if (ev.getActionCommand() == JFileChooser.APPROVE_SELECTION && file != null) setContent(Readers.read(Input.getInput(file)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sp.getFrame().setVisible(false);
				sp.setPanelWidth(1);
				sp.getFrame().setContentPane(oldCmp);
				sp.getFrame().setSize(200,190);
				sp.getFrame().setVisible(true);
			}
		});
	}
	
	public void load() {
		sp.getFrame().setVisible(false);
		sp.setPanelWidth(2);
		sp.getFrame().setContentPane(chooser);
		sp.getFrame().pack();
		sp.getFrame().setVisible(true);
	}
	
	public void setTerrainTexture(ImageData tex, double scale) {
		Texture2D t = TextureUtility.createTexture(terrainAppearance, "polygonShader", tex);
		t.setTextureMatrix(MatrixBuilder.euclidean().scale(scale).getMatrix());
	}
	
	public void setSkyBox(ImageData[] imgs) {
		TextureUtility.createSkyBox(rootAppearance, imgs);
		CubeMap cm = TextureUtility.createReflectionMap(contentAppearance, "polygonShader", imgs);
		cm.setBlendColor(new java.awt.Color(1.0f, 1.0f, 1.0f, 0.6f));
	}
	
	public void setContent(SceneGraphComponent content) {
		if (currentContent != null && sceneNode.getChildNodes().contains(currentContent)) {
			sceneNode.removeChild(currentContent);
		}
		currentContent=content;
		sceneNode.addChild(currentContent);
		alignContent(diam, offset, null);
		PickUtility.assignFaceAABBTrees(currentContent);
		currentContent.accept(new SceneGraphVisitor() {
			@Override
			public void visit(SceneGraphComponent c) {
				c.childrenWriteAccept(this, false, false, false, false, true, false);
			}
			@Override
			public void visit(IndexedFaceSet i) {
				//JoinGeometry.meltFaceHack(i);
				if (i.getVertexAttributes(Attribute.NORMALS) == null) {
					GeometryUtility.calculateAndSetVertexNormals(i);
				}
			}
		});
	}
	
	public double getDiam() {
		return diam;
	}

	public void setDiam(double diam) {
		this.diam = diam;
		sizeSlider.setValue((int) (diam*100));
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
		groundSlider.setValue((int) (offset*100));
	}

	private void alignContent(final double diam, final double offset, final Matrix rotation) {
		Scene.executeWriter(sceneNode, new Runnable() {
			public void run() {
				if (rotation != null) MatrixBuilder.euclidean(rotation).times(new Matrix(sceneNode.getTransformation())).assignTo(sceneNode);
				Rectangle3D bounds = GeometryUtility.calculateBoundingBox(sceneNode);
				// scale
				double[] extent = bounds.getExtent();
				double maxExtent = Math.max(extent[0], extent[2]);
				double scale = diam/maxExtent;
				
				double[] translation = bounds.getCenter();
				translation[1] = -scale*bounds.getMinY()+offset;
				translation[0] *= -scale;
				translation[2] *= -scale;
				
				MatrixBuilder mb = MatrixBuilder.euclidean().translate(translation).scale(scale);
				if (sceneNode.getTransformation() != null) mb.times(sceneNode.getTransformation().getMatrix());
				mb.assignTo(sceneNode);
			}
		});
	}
	
	public ViewerApp display() {
		return new ViewerApp(sceneRoot, cameraPath, emptyPickPath, avatarPath);
	}
	
	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}
	
	public void setLightIntensity(double intensity) {
		light.setIntensity(intensity);
	}
	
	public double getLightIntensity() {
		return light.getIntensity();
	}
	
	public SceneGraphComponent getTerrainNode() {
		return terrainNode;
	}
	
	public void setAvatarPosition(double x, double y, double z) {
		MatrixBuilder.euclidean().translate(x, y, z).assignTo(avatarNode);
	}
	
	public static void main(String[] args) throws IOException {
		
		//System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
		//System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
		//System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
		System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
		
		ViewerVR tds = new ViewerVR();
		
		ViewerApp vApp = tds.display();
		vApp.setAttachNavigator(true);
		vApp.setAttachBeanShell(true);
		//vApp.setShowMenu(true);
		vApp.update();
		vApp.display();
	}
}