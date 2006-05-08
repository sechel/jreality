package de.jreality.scene.tool;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import bsh.This;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.jogl.Viewer;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Cylinder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jreality.scene.proxy.scene.Sphere;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;

public class DirectionDisplay extends SceneGraphComponent{
	
	private de.jreality.scene.SceneGraphComponent parent;
	private SceneGraphComponent lineNode;
	private SceneGraphComponent sphereNode;
	private boolean activ=false;
	private final double flashSize=0.2;
	private final double lineRadius=flashSize/9;
	private final int flashRes=40;
	private final int sphereRes=40;
	private final int sphereCirclesCount=10;
	private final double size=0.13;
	private final double[] startDir = {0,0,-(1-flashSize+0.0001)};
	private double lineSize=20;
	private String type="line";
	private Matrix objWorldTrans=new Matrix();
	
	public DirectionDisplay(){	
		super();
		if(type.equals("line")){
			initGeometryLine();
		}else if(type.equals("sphere")){
			initGeometrySphere();
		}
	}
	
	private void setPosition(ToolContext tc){
		if(type.equals("line")){
			PointSet pointSet = (PointSet) tc.getCurrentPick().getPickPath().getLastElement();
			double[] point=pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(tc.getCurrentPick().getIndex()).toDoubleArray(null);
			objWorldTrans=new Matrix();
	        tc.getCurrentPick().getPickPath().getMatrix(objWorldTrans.getArray());
	        MatrixBuilder.euclidean(objWorldTrans).translate(point).assignTo(this);		        
		}else if(type.equals("sphere")){
			Camera cam=tc.getViewer().getCameraPath().getLastComponent().getCamera();
			double scale=Math.abs(cam.getNear()*Math.sin(cam.getFieldOfView()/2)*Math.cos(cam.getFieldOfView()/2)*size);
			MatrixBuilder.euclidean().translate(3*scale,-2.5*scale,-(cam.getNear()+scale)).scale(scale).assignTo(this);	
		}
	}
	
	private void setParent(ToolContext tc){
		if(type.equals("line")){
			tc.getViewer().getSceneRoot().addChild(this);
		}else if(type.equals("sphere")){	
			tc.getViewer().getCameraPath().getLastComponent().addChild(this);
		}
	}	
	
	private void removeParent(ToolContext tc){
		if(type.equals("line")){
			tc.getViewer().getSceneRoot().removeChild(this);
		}else if(type.equals("sphere")){	
			tc.getViewer().getCameraPath().getLastComponent().removeChild(this);
		}
	}
	
	private void initGeometryLine(){
		lineNode=new  SceneGraphComponent();
		this.addChild(lineNode);
		
		IndexedLineSetFactory line=new IndexedLineSetFactory();
		line.setVertexCount(2);
		line.setVertexCoordinates(new double[][] {new double[] {-lineSize/2*startDir[0],-lineSize/2*startDir[1],-lineSize/2*startDir[2]},new double[] {lineSize/2*startDir[0],lineSize/2*startDir[1],lineSize/2*startDir[2]}});
		line.setLineCount(1);
		line.setEdgeIndices(new int[][] {{0,1}});
		line.update();
		lineNode.setGeometry(line.getIndexedLineSet());
		
		Appearance lineApp=new Appearance();
		lineApp.setAttribute(CommonAttributes.TUBE_RADIUS,lineRadius);
		lineApp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.RED);
		lineApp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.RED);
		lineApp.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false);
		//lineApp.setAttribute(CommonAttributes.TRANSPARENCY,0.5);
		lineNode.setAppearance(lineApp);	
		
		this.setTransformation(new Transformation());
		lineNode.setTransformation(new Transformation());
	}
		
	private void initGeometrySphere(){
		//allNode=new SceneGraphComponent();
		sphereNode=new SceneGraphComponent();
		lineNode=new  SceneGraphComponent();
		SceneGraphComponent flashNode=new SceneGraphComponent();
		this.addChild(sphereNode);
		this.addChild(lineNode);
		lineNode.addChild(flashNode);
		//MatrixBuilder.euclidean().translate(0,0,0).assignTo(this);		
		
		IndexedLineSetFactory line=new IndexedLineSetFactory();
		line.setVertexCount(2);
		line.setVertexCoordinates(new double[][] {{0,0,0},startDir});
		line.setLineCount(1);
		line.setEdgeIndices(new int[][] {{0,1}});
		line.update();
		lineNode.setGeometry(line.getIndexedLineSet());
		Appearance lineApp=new Appearance();
		lineApp.setAttribute(CommonAttributes.TUBE_RADIUS,lineRadius);
		lineApp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.RED);
		lineApp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.RED);
		lineApp.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false);
		lineNode.setAppearance(lineApp);	
		
		IndexedFaceSetFactory flash=new IndexedFaceSetFactory();
		double[][] flashCircleVerts=getCircle(1, flashRes);
		double[][] flashVerts=new double[flashCircleVerts.length+1][3];
		for(int i=0;i<flashCircleVerts.length;i++){
			flashVerts[i]=flashCircleVerts[i];
		}
		flashVerts[flashCircleVerts.length]=new double[] {0,0,-1};		
		int[][] flashFaces=new int[flashCircleVerts.length+1][3];
		for(int i=0;i<flashCircleVerts.length-1;i++){
			flashFaces[i][0]=i;
			flashFaces[i][1]=i+1;
			flashFaces[i][2]=flashVerts.length-1;
		}
		flashFaces[flashCircleVerts.length-1]=new int[] {flashCircleVerts.length-1,0,flashVerts.length-1};
		flashFaces[flashCircleVerts.length]=new int[flashCircleVerts.length];
		for(int i=0; i<flashCircleVerts.length;i++){
			flashFaces[flashCircleVerts.length][i]=i;
		}		
		flash.setVertexCount(flashVerts.length);
		flash.setVertexCoordinates(flashVerts);
		flash.setFaceCount(flashFaces.length);
		flash.setFaceIndices(flashFaces);
		flash.setGenerateEdgesFromFaces(true);
		flash.setGenerateFaceNormals(true);
		flash.setGenerateVertexNormals(true);
		flash.update();
		flashNode.setGeometry(flash.getIndexedFaceSet());		
		MatrixBuilder.euclidean().translate(0,0,-(1-flashSize)).scale(flashSize/2,flashSize/2,flashSize).assignTo(flashNode);
		Appearance flashApp=new Appearance();
		flashApp.setAttribute(CommonAttributes.EDGE_DRAW,false);
		flashApp.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false);
		flashNode.setAppearance(flashApp);		

		SceneGraphComponent sphereCirclesNodes[]=new SceneGraphComponent[sphereCirclesCount*2];
		
		int[][] sphereCircleEdgeIndices=new int[sphereRes][2];
		for(int i=0;i<sphereRes-1;i++){
			sphereCircleEdgeIndices[i]=new int[] {i,i+1};
		}
		sphereCircleEdgeIndices[sphereRes-1]=new int[] {sphereRes-1,0};
		
		IndexedLineSetFactory sphereCircle = new IndexedLineSetFactory();
		sphereCircle.setVertexCount(sphereRes);		
		sphereCircle.setVertexCoordinates(getCircle(1,sphereRes));
		sphereCircle.setLineCount(sphereRes);
		sphereCircle.setEdgeIndices(sphereCircleEdgeIndices);
		sphereCircle.update();		
		double r;
		double h;
		int j=0;
		for(int i=0; i<Math.ceil(((double)sphereCirclesCount)/2);i++){
			h=i/Math.ceil(((double)sphereCirclesCount)/2);
			r=Math.sqrt(1-Math.pow(h,2));
			sphereCirclesNodes[j]=new SceneGraphComponent();
			sphereCirclesNodes[j].setGeometry(sphereCircle.getIndexedLineSet());
			MatrixBuilder.euclidean().translate(0,h,0).scale(r).rotate(Math.PI/2,1,0,0).assignTo(sphereCirclesNodes[j]);
			sphereNode.addChild(sphereCirclesNodes[j]);
			j++;
		}
		for(int i=1; i<Math.ceil(((double)sphereCirclesCount)/2);i++){
			h=-i/Math.ceil(((double)sphereCirclesCount)/2);
			r=Math.sqrt(1-Math.pow(h,2));
			sphereCirclesNodes[j]=new SceneGraphComponent();
			sphereCirclesNodes[j].setGeometry(sphereCircle.getIndexedLineSet());
			MatrixBuilder.euclidean().translate(0,h,0).scale(r).rotate(Math.PI/2,1,0,0).assignTo(sphereCirclesNodes[j]);
			sphereNode.addChild(sphereCirclesNodes[j]);
			j++;
		}
		for(int i=0; i<sphereCirclesCount;i++){
			sphereCirclesNodes[j]=new SceneGraphComponent();
			sphereCirclesNodes[j].setGeometry(sphereCircle.getIndexedLineSet());
			MatrixBuilder.euclidean().rotate(i*2*Math.PI/sphereCirclesCount,0,1,0).assignTo(sphereCirclesNodes[j]);
			sphereNode.addChild(sphereCirclesNodes[j]);
			j++;
		}		
		//Sphere sphere=new Sphere();
		//sphereNode.setGeometry(sphere);
		Appearance sphereApp=new Appearance();
		sphereApp.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false);
		sphereApp.setAttribute(CommonAttributes.TUBE_RADIUS,flashSize/20);
		sphereApp.setAttribute(CommonAttributes.VERTEX_DRAW,false);
		sphereApp.setAttribute(CommonAttributes.SPHERES_DRAW,false);
		sphereApp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.BLACK);
		sphereNode.setAppearance(sphereApp);
		
		this.setTransformation(new Transformation());
		lineNode.setTransformation(new Transformation());
	}
	
	private double[][] getCircle(double r, int res){
		double[][] circle=new double[res][3];
		for(int i=0;i<res;i++){
			circle[i][0]=r*Math.cos(i/(double)res*Math.PI*2);
			circle[i][1]=r*Math.sin(i/(double)res*Math.PI*2);
			circle[i][2]=0;
		}
		return circle;
	}

	public void setDirTransformation(Matrix dirTrans){
		if(activ){
			getDirTransformation().setMatrix(dirTrans.getArray());
		}
	}
	public Transformation getDirTransformation(){
		return lineNode.getTransformation();
	}
	public double[] getDir(){
		Matrix m=new Matrix();
		MatrixBuilder.euclidean(lineNode.getTransformation()).assignTo(m);
		objWorldTrans.setRow(3, new double[4]);
		objWorldTrans.setColumn(3, new double[]{0,0,0,1});	//pseudo-Rotation
		m.multiplyOnLeft(objWorldTrans);
		return Rn.normalize(null,m.multiplyVector(startDir));
	}
		
	public void activate(ToolContext tc){
		setPosition(tc);
		setParent(tc);
		activ=true;	
	}
	
	public void deactivate(ToolContext tc){
		activ=false;
		removeParent(tc);
	}
	
	public void setType(String type){
		this.type=type;	
		if(sphereNode!=null) this.removeChild(sphereNode);
		if(lineNode!=null) this.removeChild(lineNode);
		if(type.equals("line")){
			initGeometryLine();
		}else if(type.equals("sphere")){
			initGeometrySphere();
		}
	}

}
