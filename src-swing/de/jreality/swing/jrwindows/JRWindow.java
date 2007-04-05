package de.jreality.swing.jrwindows;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.JFakeFrame;
import de.jreality.tools.ActionTool;
import de.jreality.util.Secure;

/**
 * 
 * Manages a JFrame that may be immersed in the scene or an external JFrame on the
 * Desktop. Do not hold any reference to the given JFrame, but always get the reference
 * via getFrame().
 * 
 * @author bleicher, Steffen Weissmann
 *
 */

public class JRWindow {
	
  private boolean enableVertexPopUpTool=true;
	
  private static final Font TITLE_FONT = new Font("Sans Serif", Font.BOLD, 24);

  private int windowNumber;

  private IndexedFaceSet frameFace;
  private IndexedLineSet borders;
  private IndexedFaceSet decoControlFace;
  private IndexedFaceSet decoDragFace;
  private JFakeFrame frame;
  
  private JFrame externalFrame = new JFrame();
  private boolean inScene=true;
  ActionTool myActionTool=new ActionTool("PanelActivation");

  private SceneGraphComponent positionSgc; 
  private SceneGraphComponent frameSgc;
  private SceneGraphComponent borderSgc;
  private SceneGraphComponent decoControlSgc;  
  private SceneGraphComponent decoDragSgc;
  private final double[][] startCornerPos={{1,0,0},{1,-1,0},{-1,-1,0},{-1,0,0}};
  private double[][] cornerPos;
  private double[][] cornerPosBak;
  private boolean isSmall=false;
  
  private double borderRadius=0.01;
  private double cornerRadius;
  private double cornerRadiusPopUpFactor=3;
  private double decoBorderRadius=0.0033;
  private double translateFactor;
  
  private double decoSize=0.08;
  private final double decoControlSizeFactor=4;
  private double decoControlSize;
  private double[][] faceCorners;
  private double[][] decoControlCorners;
  private double[][] decoDragCorners;  
  private JPanel panel;
  private JButton killButton;
  private JButton maxButton;
  private JButton minButton;
  private final Color activeColor=new Color(62,139,210);
  private final Color inactiveColor=new Color(138,182,225);
  private final Color borderLineColor=activeColor;
  private final Color borderEdgeColor=inactiveColor;
  
  private double aspectRatio=1;
  
  private static final boolean FORBID_EXTERNAL_FRAME = "portal-remote".equals(Secure.getProperty("de.jreality.viewerapp.env"));

  protected JRWindow(int windowNumber){    
    myActionTool.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		getFrame().setVisible(!getFrame().isVisible());
    	}
    });
    this.windowNumber=windowNumber; 
    setBorderRadius(borderRadius);   
    setDecoSize(decoSize);
    cornerPos=new double[startCornerPos.length][startCornerPos[0].length];
    for(int n=0;n<cornerPos.length;n++)
      for(int c=0;c<cornerPos[0].length;c++)
        cornerPos[n][c]=startCornerPos[n][c];
    preCalculateFaceCorners();
    cornerPosBak=new double[cornerPos.length][cornerPos[0].length];
    initSgc();
    initFrame();
    initDecoration();

    /*TODO:
     * fix bug: if the panel had been moved before calling setSmall(true) the first time, it can not be dragged properly
     * hack:  */
    setSmall(true);
    setSmall(false);		
  }  
  
  private void initSgc(){
    positionSgc=new SceneGraphComponent("frame ["+windowNumber+"]");
    positionSgc.setVisible(false); // until the frame is visible
    positionSgc.setAppearance(new Appearance());
    positionSgc.getAppearance().setAttribute("pointShader.pickable", true);
    positionSgc.getAppearance().setAttribute("lineShader.pickable", true);
    positionSgc.getAppearance().setAttribute("polygonShader.pickable", true);  
  }   
  private void initFrame(){
    frameSgc=new SceneGraphComponent("content");
    positionSgc.addChild(frameSgc);    
    faceCorners=new double[cornerPos.length][cornerPos[0].length];
    calculateFaceCorners(faceCorners,cornerPos);    
    IndexedFaceSetFactory face=new IndexedFaceSetFactory();
    face.setVertexCount(4);
    face.setVertexCoordinates(faceCorners);
    face.setVertexTextureCoordinates(new double[][] {{1,0},{1,1},{0,1},{0,0}});
    face.setFaceCount(1);
    face.setFaceIndices(new int[][] {{0,1,2,3}});
    face.setGenerateEdgesFromFaces(false);
    face.setGenerateFaceNormals(true);
    face.setGenerateVertexNormals(true);
    face.update();
    this.frameFace=face.getIndexedFaceSet();     
    frame=new JFakeFrame();
    
    ComponentListener componentListener = new ComponentListener() {
			public void componentHidden(ComponentEvent e) {
				getSgc().setVisible(false);
			}
			public void componentMoved(ComponentEvent e) {
			}
			public void componentResized(ComponentEvent e) {
				updateAspectRatio();
				setCorner(0, cornerPos[0]);
			}
	
			public void componentShown(ComponentEvent e) {
				updateAspectRatio();
				if (!isSmall) setCorner(0, cornerPos[0]);
				else setSmall(true);
				getSgc().setVisible(true);
			}
			
			void updateAspectRatio() {
				double newAspectRatio=(double)frame.getWidth()/(double)frame.getHeight();
				if (newAspectRatio == 0 || Double.isNaN(newAspectRatio) || Double.isInfinite(newAspectRatio)) {
					System.out.println("ignoring new aspectRatio: "+newAspectRatio);				
					return;
				}
				aspectRatio=newAspectRatio;
			}
	    	
	    };
	frame.addComponentListener(componentListener);
	
    frame.addPropertyChangeListener("title", new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			updateFrameTitle();
			externalFrame.setTitle(frame.getTitle());
		}
    });
    
    frameSgc.setGeometry(frameFace);
    frameSgc.addTool(frame.getTool());
    frameSgc.setAppearance(frame.getAppearance());
    
    frameSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
  }
  
  private void initDecoration(){
    decoControlSgc=new SceneGraphComponent("controls");    
    positionSgc.addChild(decoControlSgc);      
    decoControlCorners=new double[cornerPos.length][cornerPos[0].length];
    calculateDecoControlCorners(decoControlCorners,cornerPos);    
    IndexedFaceSetFactory decoControlFace=new IndexedFaceSetFactory();
    decoControlFace.setVertexCount(4);
    decoControlFace.setVertexCoordinates(decoControlCorners);
    decoControlFace.setVertexTextureCoordinates(new double[][] {{1,0},{1,1},{0,1},{0,0}});
    decoControlFace.setFaceCount(1);
    decoControlFace.setFaceIndices(new int[][] {{0,1,2,3}});
    decoControlFace.setGenerateEdgesFromFaces(false);
    decoControlFace.setGenerateFaceNormals(true);
    decoControlFace.setGenerateVertexNormals(true);
    decoControlFace.update();
    this.decoControlFace=decoControlFace.getIndexedFaceSet();     
    JFakeFrame decoControlFrame=new JFakeFrame();    
    decoControlSgc.addTool(decoControlFrame.getTool());
    decoControlSgc.setAppearance(decoControlFrame.getAppearance());  
    decoControlSgc.setGeometry(this.decoControlFace);  
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,false);
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,true);
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,true); 
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS,decoBorderRadius);
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,borderLineColor);
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
    
    panel=new JPanel();
    panel.setBackground(inactiveColor);
    killButton=new JButton("X");
    killButton.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		frame.setVisible(false);
    	}
    });
    //killButton.setEnabled(false);
    maxButton=new JButton("O");
    maxButton.setEnabled(false);
    minButton=new JButton("_");
    panel.add(minButton);
    panel.add(maxButton);
    panel.add(killButton);
    decoControlFrame.getContentPane().add(panel);
    decoControlFrame.pack();
    decoControlFrame.setVisible(true);
    
    ////decoDragFace////////////////////////////////////
    decoDragSgc=new SceneGraphComponent("title");
    positionSgc.addChild(decoDragSgc);     
    decoDragCorners=new double[cornerPos.length][cornerPos[0].length];
    calculateDecoDragCorners(decoDragCorners,cornerPos);    
    IndexedFaceSetFactory decoDragFace=new IndexedFaceSetFactory();
    decoDragFace.setVertexCount(4);
    decoDragFace.setVertexCoordinates(decoDragCorners);
    decoDragFace.setVertexTextureCoordinates(new double[][] {{1,0},{1,1},{0,1},{0,0}});
    decoDragFace.setFaceCount(1);
    decoDragFace.setFaceIndices(new int[][] {{0,1,2,3}});
    decoDragFace.setGenerateEdgesFromFaces(false);
    decoDragFace.setGenerateFaceNormals(true);
    decoDragFace.setGenerateVertexNormals(true);
    decoDragFace.update();
    this.decoDragFace=decoDragFace.getIndexedFaceSet();      
    decoDragSgc.setGeometry(this.decoDragFace);  
    decoDragSgc.setAppearance(new Appearance());
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,false);
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,true);
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,true);
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS,decoBorderRadius);
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,borderLineColor);
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
    
    ////borders///////////////////////
    borderSgc=new SceneGraphComponent("border");
    positionSgc.addChild(borderSgc);
    IndexedLineSetFactory borders=new IndexedLineSetFactory();
    borders.setVertexCount(4);
    borders.setVertexCoordinates(cornerPos);
    borders.setLineCount(4);
    borders.setEdgeIndices(new int[][] {{0,1},{1,2},{2,3},{3,0}});
    borders.update();
    this.borders=borders.getIndexedLineSet();
    borderSgc.setGeometry(this.borders);
    borderSgc.setAppearance(new Appearance());
    borderSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,true);
    borderSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,true);
    borderSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,true);
    borderSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,true); 
    borderSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,borderLineColor);
    borderSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,borderEdgeColor);
    borderSgc.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS,cornerRadius);
    borderSgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS,borderRadius);
    borderSgc.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, true);
    
    if(enableVertexPopUpTool){
    	vertexPopUpTool=new VertexPopUpTool();
    	borderSgc.addTool(vertexPopUpTool);
    }
  }   
   
  protected void updateFrameTitle() {
	  BufferedImage img = LabelUtility.createImageFromString(frame.getTitle(), TITLE_FONT, Color.black, Color.white);
	  
	  double w = img.getWidth();
	  double h = img.getHeight()*1.5;
	  
	  double width = decoDragCorners[0][0]-decoDragCorners[2][0];
	  double height = decoDragCorners[0][1]-decoDragCorners[2][1];
	  
	  double lambda = h/height;
	  
	  double effW = lambda*width;
	  	  
	  if (effW <= w+h/3) effW=w+h/3;
	  BufferedImage effImg = new BufferedImage((int)effW, (int)h, BufferedImage.TYPE_INT_ARGB);
	  effImg.getGraphics().fillRect(0, 0, effImg.getWidth(), effImg.getHeight());
	  effImg.getGraphics().drawImage(img, (int) (int)(h/6), (int)(h/6), null);
	  
	  TextureUtility.createTexture(decoDragSgc.getAppearance(), "polygonShader", new ImageData(effImg));
  }

  protected void addActionListeners(ActionListener actionListener){
    //killButton.addActionListener(actionListener);    
    maxButton.addActionListener(actionListener);    
    minButton.addActionListener(actionListener);
    updateActionCommands();
  }
  private void updateActionCommands(){
    killButton.setActionCommand("X"+windowNumber);
    maxButton.setActionCommand("O"+windowNumber);
    minButton.setActionCommand("_"+windowNumber);
  }
  
  protected void setCornerPos(double[][] newCornerPos){ 
    if(newCornerPos[0][0]-newCornerPos[3][0]>=decoControlSize+borderRadius&&newCornerPos[0][1]-newCornerPos[1][1]>=decoSize+borderRadius){
      cornerPos=newCornerPos;
    }else if(newCornerPos[0][0]-newCornerPos[3][0]>=decoControlSize+borderRadius||newCornerPos[0][1]-newCornerPos[1][1]>=decoSize+borderRadius){ 
      if(newCornerPos[0][0]-newCornerPos[3][0]>decoControlSize+borderRadius){ //copy x   
        for(int n=0;n<cornerPos.length;n++)
          cornerPos[n][0]=newCornerPos[n][0];
      }else if(newCornerPos[0][1]-newCornerPos[1][1]>decoSize+borderRadius){  //copy y   
        for(int n=0;n<cornerPos.length;n++)
          cornerPos[n][1]=newCornerPos[n][1];
      }
    }else return;
    
    calculateFaceCorners(faceCorners,cornerPos);
    frameFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(faceCorners));
    calculateDecoControlCorners(decoControlCorners,cornerPos);
    decoControlFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(decoControlCorners));  
    calculateDecoDragCorners(decoDragCorners,cornerPos);
    decoDragFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(decoDragCorners));    
    borders.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(cornerPos));
    updateFrameTitle();
  }    
  
  void setCorner(int cornerIndex, double[] newPoint) {
		int oppositeCorner = (cornerIndex+2)%4;
		newPoint[1]-=decoSize; newPoint[2]=0;
		double[] diag = Rn.subtract(null, newPoint, cornerPos[oppositeCorner]);
		
		double newAsp = Math.abs(diag[0]/diag[1]);
		if (newAsp > aspectRatio) {
			// adapt width to height:
			diag[0] = Math.signum(diag[0])*aspectRatio*Math.abs(diag[1]);
		} else {
//			 adapt height to width:
			diag[1] = Math.signum(diag[1])*Math.abs(diag[0])/aspectRatio;
		}
		
		// do not allow less width than the deco needs:
		double decoWidth = Math.abs(decoControlCorners[2][0]-decoControlCorners[0][0]);
		if (Math.abs(diag[0]) < decoWidth) {
			double f = decoWidth/Math.abs(diag[0]);
			diag[0]*=f;
			diag[1]*=f;
		}
		
		// cancel if window would be flipped:
		if ((cornerIndex == 0) && (diag[0]<=0 || diag[1]<=0)) return;
		if ((cornerIndex == 1) && (diag[0]<=0 || diag[1]>=0)) return;
		if ((cornerIndex == 2) && (diag[0]>=0 || diag[1]>=0)) return;
		if ((cornerIndex == 3) && (diag[0]>=0 || diag[1]<=0)) return;
		
		
		Rn.add(cornerPos[cornerIndex], cornerPos[oppositeCorner], diag);
		
		if (cornerIndex == 3 || cornerIndex == 0) cornerPos[cornerIndex][1]+=decoSize;
		else cornerPos[cornerIndex][1]-=decoSize;
		
		// adjust other two corners:
		int nextCorner = (cornerIndex+1)%4;
		int prevCorner = (cornerIndex-1+4)%4;
		if (cornerIndex%2 == 0) {
			cornerPos[nextCorner][0]=cornerPos[cornerIndex][0];
			cornerPos[prevCorner][1]=cornerPos[cornerIndex][1];
		} else {
			cornerPos[nextCorner][1]=cornerPos[cornerIndex][1];
			cornerPos[prevCorner][0]=cornerPos[cornerIndex][0];
		}
		
		setCornerPos(cornerPos);
  }  
  
//  double[] windowSize;
//  double[] frameSize;
//  
//  private double[] setFrameSize(double[] windowSize, double factor){
//    double[] frameSize=new double[2];
//    frameSize[0]=windowSize[0]*factor;
//    frameSize[1]=windowSize[1]*factor;
//    frame.setSize((int)frameSize[0],(int)frameSize[1]);
//    frame.validate();
//    return frameSize;
//  }
//  
//  protected void updateFrameSize(){     
//    double[] newWindowSize=calculateWindowSize();
//
//    double factorWidth=newWindowSize[0]/windowSize[0];
//    double factorHeight=newWindowSize[1]/windowSize[1];  
//    double frameWidth=frameSize[0]*factorWidth;
//    double frameHeight=frameSize[1]*factorHeight;
//    if(frameWidth<1) frameWidth=1;
//    if(frameHeight<1) frameHeight=1;
//    
//    frame.setSize((int)frameWidth,(int)frameHeight); 
//    frame.validate();
//  }
//  
//  private double[] calculateWindowSize(){
//    double width=Rn.euclideanNorm(Rn.subtract(null,faceCorners[0],faceCorners[3]));
//    double height=Rn.euclideanNorm(Rn.subtract(null,faceCorners[0],faceCorners[1]));
//    return new double[] {width,height};
//  }
  
  
  double[] dirX;
  double[] dirY;
  private void preCalculateFaceCorners(){
    dirX=Rn.subtract(null,cornerPos[0],cornerPos[3]);
    Rn.normalize(dirX,dirX);
    Rn.times(dirX,borderRadius,dirX);
    dirY=Rn.subtract(null,cornerPos[0],cornerPos[1]);
    Rn.normalize(dirY,dirY);
    Rn.times(dirY,borderRadius,dirY);
  }  
  private void calculateFaceCorners(double[][] faceCorners, double[][] cornerPos){    
    Rn.subtract(faceCorners[0],cornerPos[1],cornerPos[0]);
    Rn.normalize(faceCorners[0],faceCorners[0]);
    Rn.times(faceCorners[0],decoSize+borderRadius,faceCorners[0]);
    Rn.add(faceCorners[0],cornerPos[0],faceCorners[0]); 
    Rn.add(faceCorners[0],faceCorners[0],Rn.times(null,-1,dirX));    
    Rn.add(faceCorners[1],cornerPos[1],Rn.times(null,-1,dirX));
    Rn.add(faceCorners[1],faceCorners[1],dirY);      
    Rn.add(faceCorners[2],cornerPos[2],dirX);
    Rn.add(faceCorners[2],faceCorners[2],dirY);    
    Rn.subtract(faceCorners[3],cornerPos[2],cornerPos[3]);
    Rn.normalize(faceCorners[3],faceCorners[3]);
    Rn.times(faceCorners[3],decoSize+borderRadius,faceCorners[3]);
    Rn.add(faceCorners[3],cornerPos[3],faceCorners[3]);
    Rn.add(faceCorners[3],faceCorners[3],dirX);
  } 
  private void calculateDecoControlCorners(double[][] decoControlCorners,double[][] cornerPos){      
    Rn.add(decoControlCorners[0],cornerPos[0],Rn.times(null,-1,dirX));
    Rn.add(decoControlCorners[0],decoControlCorners[0],Rn.times(null,-1,dirY));    
    Rn.subtract(decoControlCorners[1],cornerPos[1],cornerPos[0]);
    Rn.normalize(decoControlCorners[1],decoControlCorners[1]);
    Rn.times(decoControlCorners[1],decoSize+borderRadius,decoControlCorners[1]);
    Rn.add(decoControlCorners[1],cornerPos[0],decoControlCorners[1]); 
    Rn.add(decoControlCorners[1],decoControlCorners[1],Rn.times(null,-1,dirX));   
    double[] trans=new double[decoControlCorners[0].length];
    Rn.subtract(trans,cornerPos[3],cornerPos[0]);
    Rn.normalize(trans,trans);
    Rn.times(trans,decoControlSize,trans);    
    Rn.subtract(decoControlCorners[2],cornerPos[2],cornerPos[3]);
    Rn.normalize(decoControlCorners[2],decoControlCorners[2]);
    Rn.times(decoControlCorners[2],decoSize+borderRadius,decoControlCorners[2]);
    Rn.add(decoControlCorners[2],trans,decoControlCorners[2]);
    Rn.add(decoControlCorners[2],cornerPos[0],decoControlCorners[2]);
    Rn.add(decoControlCorners[3],trans,cornerPos[0]);
    Rn.add(decoControlCorners[3],decoControlCorners[3],Rn.times(null,-1,dirY));
  }
  private void calculateDecoDragCorners(double[][] decoDragCorners,double[][] cornerPos){  
    double[] trans=new double[decoDragCorners[0].length];
    Rn.subtract(trans,cornerPos[3],cornerPos[0]);
    Rn.normalize(trans,trans);
    Rn.times(trans,decoControlSize,trans);    
    Rn.add(decoDragCorners[0],trans,cornerPos[0]);
    Rn.add(decoDragCorners[0],decoDragCorners[0],Rn.times(null,-1,dirY));    
    Rn.subtract(decoDragCorners[1],cornerPos[2],cornerPos[3]);
    Rn.normalize(decoDragCorners[1],decoDragCorners[1]);
    Rn.times(decoDragCorners[1],decoSize+borderRadius,decoDragCorners[1]);
    Rn.add(decoDragCorners[1],trans,decoDragCorners[1]);
    Rn.add(decoDragCorners[1],cornerPos[0],decoDragCorners[1]);    
    Rn.subtract(decoDragCorners[2],cornerPos[2],cornerPos[3]);
    Rn.normalize(decoDragCorners[2],decoDragCorners[2]);
    Rn.times(decoDragCorners[2],decoSize+borderRadius,decoDragCorners[2]);
    Rn.add(decoDragCorners[2],cornerPos[3],decoDragCorners[2]); 
    Rn.add(decoDragCorners[2],decoDragCorners[2],dirX);   
    Rn.add(decoDragCorners[3],cornerPos[3],dirX);
    Rn.add(decoDragCorners[3],decoDragCorners[3],Rn.times(null,-1,dirY));
  }
  protected double[][] getCornerPos(){
    return cornerPos;
  }  

  private double[] smallCenter;
  private Tool mouseEventTool;
  private Tool vertexPopUpTool;
  
  protected void setSmall(boolean setSmall){    
	  if(setSmall&&!isSmall){
		  for(int i=0;i<cornerPos.length;i++)
			  Rn.copy(cornerPosBak[i],cornerPos[i]); 
		  if(smallCenter==null)
			  smallCenter=getCenter(cornerPos);
	  
		  double factorX = (decoControlSize+borderRadius)/borderRadius/2;
		  double factorY = factorX*Rn.euclideanDistance(cornerPos[0], cornerPos[1])/Rn.euclideanDistance(cornerPos[0], cornerPos[3]); 
		  Rn.add(cornerPos[0],smallCenter,Rn.times(null,factorX,dirX));
		  Rn.add(cornerPos[0],cornerPos[0],Rn.times(null,factorY,dirY));
		  Rn.add(cornerPos[1],smallCenter,Rn.times(null,factorX,dirX));
		  Rn.add(cornerPos[1],cornerPos[1],Rn.times(null,-factorY,dirY));
		  Rn.add(cornerPos[2],smallCenter,Rn.times(null,-factorX,dirX));
		  Rn.add(cornerPos[2],cornerPos[2],Rn.times(null,-factorY,dirY));
		  Rn.add(cornerPos[3],smallCenter,Rn.times(null,-factorX,dirX));
		  Rn.add(cornerPos[3],cornerPos[3],Rn.times(null,factorY,dirY));
	
		  mouseEventTool=frameSgc.getTools().get(0);
		  frameSgc.removeTool(mouseEventTool);
		  if(vertexPopUpTool!=null)
			  borderSgc.removeTool(vertexPopUpTool);
		  
		  setActiveColor(false);
		  minButton.setEnabled(false);
		  maxButton.setEnabled(true);
		  isSmall=true;
	  }else if((!setSmall)&&isSmall){
		  smallCenter=getCenter(cornerPos);      
		  for(int i=0;i<cornerPos.length;i++){
			  Rn.copy(cornerPos[i],cornerPosBak[i]);
		  }
		  
		  if(mouseEventTool!=null)
			  frameSgc.addTool(mouseEventTool);
		  if(vertexPopUpTool!=null)
			  borderSgc.addTool(vertexPopUpTool);
		  
		  setActiveColor(true);
		  minButton.setEnabled(true);
		  maxButton.setEnabled(false);
		  isSmall=false;
	  }
	  setCornerPos(cornerPos);
  }
  private double[] getCenter(double[][] box){
    double[] center={0,0,0};
    for(int n=0;n<box.length;n++)
      Rn.add(center,center,box[n]);
    Rn.times(center,1/(double)box.length,center);  
    return center;
  }
  protected boolean isSmall(){
    return isSmall;
  }
  
  protected void setInFront(boolean setInFront){
    if(setInFront){
      if(!isSmall)
        setActiveColor(true);	
      MatrixBuilder.euclidean().assignTo(positionSgc); 
    }else{
      if(!isSmall)
        setActiveColor(false);	
      MatrixBuilder.euclidean().translate(0,0,-translateFactor*(windowNumber+1)).assignTo(positionSgc); 
    }
  }
  
  private void setActiveColor(boolean active){
	  if(active){
		  decoDragSgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,activeColor);
		  borderSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,activeColor);
		  borderSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,activeColor);
		  panel.setBackground(activeColor);
	  }else{
		  decoDragSgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,inactiveColor);
		  borderSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,inactiveColor);
		  borderSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,inactiveColor);
		  panel.setBackground(inactiveColor);
	  }
  }

  protected void setBorderRadius(double r) {
    borderRadius=r;
    cornerRadius=borderRadius*1.75;
    translateFactor=1.1*cornerRadius;
    if(borderSgc!=null){
      borderSgc.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS,cornerRadius);
      borderSgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS,borderRadius);
      preCalculateFaceCorners();
      setCornerPos(cornerPos);
    } 
  }  
  protected void setDecoSize(double s){
    decoSize=s;
    decoControlSize=s*decoControlSizeFactor; 
    if(borderSgc!=null){
      setCornerPos(cornerPos);
    }
  }
  
  protected void setWindowNumber(int windowNumber){
    this.windowNumber=windowNumber;
    updateActionCommands();
  }
  
  protected int getWindowNumber(){
    return windowNumber;
  }  
  protected SceneGraphComponent getSgc(){
    return positionSgc;
  }  
  public JFrame getFrame() {
	  return inScene ? frame : externalFrame;
  }
  protected IndexedFaceSet getFrameFace(){
    return frameFace;
  }  
  protected IndexedFaceSet getDecoControlFace(){
    return decoControlFace;
  }
  protected IndexedFaceSet getDecoDragFace(){
    return decoDragFace;
  }
  protected IndexedLineSet getBorders(){
    return borders;
  }
  public void setInScene(boolean b) {
	  if (FORBID_EXTERNAL_FRAME) return;
	  if (b==inScene) return;
	  if (b) {
			inScene = b;
			boolean visible = externalFrame.isVisible();
			externalFrame.setVisible(false);
			frame.setContentPane(externalFrame.getContentPane());
			externalFrame.remove(externalFrame.getContentPane());
			frame.pack();
			frame.setVisible(visible);
		} else {
			getSgc().setVisible(false);
			boolean visible = frame.isVisible();
			frame.setVisible(false);
			inScene = b;
			externalFrame.setContentPane(frame.getContentPane());
			frame.remove(frame.getContentPane());
			externalFrame.pack();
			externalFrame.setVisible(visible);
		}
  }

	public ActionTool getPanelTool() {
		return myActionTool;
	}
	
	protected void popUpDragVertices(boolean popUp){
		borderSgc.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, popUp ? cornerRadius*cornerRadiusPopUpFactor : cornerRadius);	
	}
	private boolean isDragged=false;
	protected void setIsDragged(boolean isDragged){
		this.isDragged=isDragged;
	}
	
	private class VertexPopUpTool extends AbstractTool{
		
		private final InputSlot pointer = InputSlot.getDevice("PointerTransformation");
		
		protected VertexPopUpTool(){
			super(new InputSlot[] {null});
			addCurrentSlot(pointer);
		}
		
		public void perform(ToolContext tc){
			if(!isSmall){
				if(tc.getCurrentPick()!=null){
					if(tc.getCurrentPick().getPickPath().getLastComponent()==borderSgc){
						if(tc.getCurrentPick().getPickType()==PickResult.PICK_TYPE_POINT){
							popUpDragVertices(true);
						}else if(!isDragged) popUpDragVertices(false);
					}else if(!isDragged) popUpDragVertices(false);
				}else if(!isDragged) popUpDragVertices(false);
			}
		}
	}
  
}
