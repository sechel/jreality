package de.jreality.swing;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JRootPane;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;

public class JFakeFrameWithGeometry extends JFakeFrame {
	 
	SceneGraphComponent windowComponent;
	IndexedFaceSetFactory quadFactory;
	
	
	public JFakeFrameWithGeometry() {
		super();
	}

	public JFakeFrameWithGeometry(GraphicsConfiguration gc) {
		super(gc);
	}
	
	public JFakeFrameWithGeometry(String title, GraphicsConfiguration gc) {
		super(title, gc);
	}
	
	public JFakeFrameWithGeometry(String title) throws HeadlessException {
		super(title);
	}
	
	 private static InputSlot drag0 = InputSlot.getDevice("PanelAction");
	  private static InputSlot drag2 = InputSlot.getDevice("PanelSelection");
	  private static InputSlot drag1 = InputSlot.getDevice("PanelMenu");

	protected void init() {
		
    	setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        

        Tool leftMouseButtonTool = new PlanarMouseEventTool(drag0, 0, this);
        Tool centerMouseButtonTool = new PlanarMouseEventTool(drag1, 1, this);
        Tool rightMouseButtonTool = new PlanarMouseEventTool(drag2, 2, this);
		
		  appearance = new Appearance();
		  appearance.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
		  appearance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		  appearance.setAttribute(CommonAttributes.EDGE_DRAW, false);
		  appearance.setAttribute(CommonAttributes.TUBES_DRAW, false);
		  appearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		  
        quadFactory = new IndexedFaceSetFactory();
		quadFactory.setVertexCount(4);
		quadFactory.setFaceCount(1);
		quadFactory.setGenerateFaceNormals(true);
		quadFactory.setFaceIndices(new int[][]{{0,1,2,3}});
		quadFactory.setVertexTextureCoordinates(new double[][]{{0,0},{1,0},{1,1},{0,1}});
		windowComponent = new SceneGraphComponent();
		windowComponent.addTool(leftMouseButtonTool);
		windowComponent.addTool(centerMouseButtonTool);
		windowComponent.addTool(rightMouseButtonTool);
		//windowComponent.addTool(tool);
		windowComponent.setAppearance(getAppearance());
		windowComponent.setGeometry(quadFactory.getGeometry());
		
		setBounds(getBounds());
		
	}

	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		if (windowComponent != null) {
			MatrixBuilder.euclidean().translate(x,y,0).assignTo(windowComponent);
			double[][] loc = new double[][]{{0,0,0},{w,0,0},{w,h,0},{0,h,0}};
			quadFactory.setVertexCoordinates(loc);
			quadFactory.update();
		}
	}
	
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (windowComponent != null) windowComponent.setVisible(b);
	}

	public SceneGraphComponent getSceneGraphComponent() {
		return windowComponent;
	}
	
}
