package de.jreality.swing;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.util.Arrays;

import javax.swing.JRootPane;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

public class JFakeFrameWithGeometry extends JFakeFrame {

	SceneGraphComponent windowComponent;
	IndexedFaceSetFactory quadFactory;
	private PlanarMouseEventTool myTool;
	
	
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
	
	protected void init() {
		
    	setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        myTool = new PlanarMouseEventTool(this);
		tool = new MouseEventTool(this);
		
		  appearance = new Appearance();
		  appearance.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
		  appearance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		  appearance.setAttribute(CommonAttributes.EDGE_DRAW, false);
		  appearance.setAttribute(CommonAttributes.TUBES_DRAW, false);

        quadFactory = new IndexedFaceSetFactory();
		quadFactory.setVertexCount(4);
		quadFactory.setFaceCount(1);
		quadFactory.setGenerateFaceNormals(true);
		quadFactory.setFaceIndices(new int[][]{{0,1,2,3}});
		quadFactory.setVertexTextureCoordinates(new double[][]{{0,0},{1,0},{1,1},{0,1}});
		windowComponent = new SceneGraphComponent();
		windowComponent.addTool(myTool);
		//windowComponent.addTool(tool);
		windowComponent.setAppearance(getAppearance());
		windowComponent.setGeometry(quadFactory.getGeometry());
	}

	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		MatrixBuilder.euclidean().translate(x,y,0).assignTo(windowComponent);
//		double[][] loc = new double[][]{{x,y,0},{x+w,y,0},{x+w,y+h,0},{x,y+h,0}};
		double[][] loc = new double[][]{{0,0,0},{w,0,0},{w,h,0},{0,h,0}};
		System.out.println(Arrays.deepToString(loc));
		quadFactory.setVertexCoordinates(loc);
		quadFactory.update();
	}
	
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		windowComponent.setVisible(b);
	}

	public SceneGraphComponent getSceneGraphComponent() {
		return windowComponent;
	}
	
}
