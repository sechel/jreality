package de.jreality.swing.jrwindows;

import java.awt.Color;
import java.awt.Font;
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
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.JFakeFrame;

/**
 * @author bleicher
 *
 */

class JRWindow {
	
  private static final Font TITLE_FONT = new Font("Sans Serif", Font.BOLD, 24);

private int windowNumber;  

  private IndexedFaceSet frameFace;
  private IndexedLineSet borders;
  private IndexedFaceSet decoControlFace;
  private IndexedFaceSet decoDragFace;
  private JFakeFrame frame;
  private SceneGraphComponent positionSgc;
  private SceneGraphComponent orientationSgc; 
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
  private double decoBorderRadius=0.0033;
  private double translateFactor;
  
//  private double windowFrameFactor=200;
  
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
  
  protected JRWindow(int windowNumber){    
    super();  
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

//    windowSize=calculateWindowSize();
//    frameSize=setFrameSize(windowSize,windowFrameFactor);
  }  
  
  private void initSgc(){
    positionSgc=new SceneGraphComponent();
    positionSgc.setAppearance(new Appearance());
    positionSgc.getAppearance().setAttribute("pointShader.pickable", true);
    positionSgc.getAppearance().setAttribute("lineShader.pickable", true);
    positionSgc.getAppearance().setAttribute("polygonShader.pickable", true);
    orientationSgc=new SceneGraphComponent();
    positionSgc.addChild(orientationSgc);         
  }   
  private void initFrame(){
    frameSgc=new SceneGraphComponent();
    orientationSgc.addChild(frameSgc);    
    faceCorners=new double[cornerPos.length][cornerPos[0].length];
    calculateFaceCorners(faceCorners,cornerPos);    
    IndexedFaceSetFactory face=new IndexedFaceSetFactory();
    face.setVertexCount(4);
    face.setVertexCoordinates(faceCorners);
    face.setVertexTextureCoordinates(new double[][] {{1,0},{1,1},{0,1},{0,0}});
    face.setFaceCount(1);
    face.setFaceIndices(new int[][] {{0,1,2,3}});
    face.setGenerateEdgesFromFaces(true);
    face.setGenerateFaceNormals(true);
    face.setGenerateVertexNormals(true);
    face.update();
    this.frameFace=face.getIndexedFaceSet();     
    frame=new JFakeFrame();
    frame.addComponentListener(new ComponentListener() {
		public void componentHidden(ComponentEvent e) {
		}
		public void componentMoved(ComponentEvent e) {
		}
		public void componentResized(ComponentEvent e) {
			updateAspectRatio();
			setCorner(0, cornerPos[0]);
		}

		public void componentShown(ComponentEvent e) {
			updateAspectRatio();
			setCorner(0, cornerPos[0]);
		}
		
		void updateAspectRatio() {
			double newAspectRatio=(double)frame.getWidth()/(double)frame.getHeight();
			if (newAspectRatio == 0 || Double.isNaN(newAspectRatio) || Double.isInfinite(newAspectRatio)) {
				System.out.println("ignoring new aspectRatio: "+newAspectRatio);				
				return;
			}
			aspectRatio=newAspectRatio;
		}
    	
    });

    frame.addPropertyChangeListener("title", new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			updateFrameTitle();
		}
    });
    
    frameSgc.addTool(frame.getTool());
    frameSgc.setAppearance(frame.getAppearance());  
    frameSgc.setGeometry(frameFace); 
    frameSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
  }
  
  private void initDecoration(){
    decoControlSgc=new SceneGraphComponent();    
    orientationSgc.addChild(decoControlSgc);      
    decoControlCorners=new double[cornerPos.length][cornerPos[0].length];
    calculateDecoControlCorners(decoControlCorners,cornerPos);    
    IndexedFaceSetFactory decoControlFace=new IndexedFaceSetFactory();
    decoControlFace.setVertexCount(4);
    decoControlFace.setVertexCoordinates(decoControlCorners);
    decoControlFace.setVertexTextureCoordinates(new double[][] {{1,0},{1,1},{0,1},{0,0}});
    decoControlFace.setFaceCount(1);
    decoControlFace.setFaceIndices(new int[][] {{0,1,2,3}});
    decoControlFace.setGenerateEdgesFromFaces(true);
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
    killButton.setEnabled(false);
    maxButton=new JButton("O");
    minButton=new JButton("_");
    panel.add(minButton);
    panel.add(maxButton);
    panel.add(killButton);
    decoControlFrame.getContentPane().add(panel);
    decoControlFrame.pack();
    decoControlFrame.setVisible(true);
    
    ////decoDragFace////////////////////////////////////
    decoDragSgc=new SceneGraphComponent();
    orientationSgc.addChild(decoDragSgc);     
    decoDragCorners=new double[cornerPos.length][cornerPos[0].length];
    calculateDecoDragCorners(decoDragCorners,cornerPos);    
    IndexedFaceSetFactory decoDragFace=new IndexedFaceSetFactory();
    decoDragFace.setVertexCount(4);
    decoDragFace.setVertexCoordinates(decoDragCorners);
    decoDragFace.setVertexTextureCoordinates(new double[][] {{1,0},{1,1},{0,1},{0,0}});
    decoDragFace.setFaceCount(1);
    decoDragFace.setFaceIndices(new int[][] {{0,1,2,3}});
    decoDragFace.setGenerateEdgesFromFaces(true);
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
    borderSgc=new SceneGraphComponent();
    orientationSgc.addChild(borderSgc);    
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
    borderSgc.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
  }   
   
  protected void updateFrameTitle() {
	  BufferedImage img = LabelUtility.createImageFromString(frame.getTitle(), TITLE_FONT, Color.black, Color.white);
	  
	  double w = img.getWidth();
	  double h = img.getHeight()*1.5;
	  
	  double width = decoDragCorners[0][0]-decoDragCorners[2][0];
	  double height = decoDragCorners[0][1]-decoDragCorners[2][1];
	  
	  double lambda = h/height;
	  
	  double effW = lambda*width;
	  	  
	  if (effW <= w) effW=w+h/3;
	  BufferedImage effImg = new BufferedImage((int)effW, (int)h, BufferedImage.TYPE_INT_ARGB);
	  effImg.getGraphics().fillRect(0, 0, effImg.getWidth(), effImg.getHeight());
	  effImg.getGraphics().drawImage(img, (int) (int)(h/6), (int)(h/6), null);
	  
	  TextureUtility.createTexture(decoDragSgc.getAppearance(), "polygonShader", new ImageData(effImg));
  }

  protected void addActionListeners(ActionListener actionListener){
    killButton.addActionListener(actionListener);    
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
  
  public void setCorner(int cornerIndex, double[] newPoint) {
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
		Rn.add(cornerPos[cornerIndex], cornerPos[oppositeCorner], diag);
		
		cornerPos[cornerIndex][1]+=decoSize;
		
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
  
  protected void setSmall(boolean setSmall){    
    if(setSmall&&!isSmall){
      for(int i=0;i<cornerPos.length;i++)
        Rn.copy(cornerPosBak[i],cornerPos[i]); 
      if(smallCenter==null)
        smallCenter=getCenter(cornerPos);
      Rn.add(cornerPos[0],smallCenter,Rn.times(null,(decoControlSize+borderRadius)/borderRadius,dirX));
      Rn.add(cornerPos[0],cornerPos[0],Rn.times(null,(decoSize/2+borderRadius)/borderRadius,dirY));
      Rn.add(cornerPos[1],smallCenter,Rn.times(null,(decoControlSize+borderRadius)/borderRadius,dirX));
      Rn.add(cornerPos[1],cornerPos[1],Rn.times(null,-((decoSize/2+borderRadius)/borderRadius),dirY));
      Rn.add(cornerPos[2],smallCenter,Rn.times(null,-(decoControlSize+borderRadius)/borderRadius,dirX));
      Rn.add(cornerPos[2],cornerPos[2],Rn.times(null,-(decoSize/2+borderRadius)/borderRadius,dirY));
      Rn.add(cornerPos[3],smallCenter,Rn.times(null,-(decoControlSize+borderRadius)/borderRadius,dirX));
      Rn.add(cornerPos[3],cornerPos[3],Rn.times(null,(decoSize/2+borderRadius)/borderRadius,dirY));
      isSmall=true;
    }else if((!setSmall)&&isSmall){
      smallCenter=getCenter(cornerPos);      
      for(int i=0;i<cornerPos.length;i++){
       Rn.copy(cornerPos[i],cornerPosBak[i]);
      }
      isSmall=false;
    }
    frame.setVisible(!isSmall);
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
      decoDragSgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,activeColor);
      panel.setBackground(activeColor);
      MatrixBuilder.euclidean().assignTo(positionSgc); 
    }else{
      decoDragSgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,inactiveColor);
      panel.setBackground(inactiveColor);
      MatrixBuilder.euclidean().translate(0,0,-translateFactor*(windowNumber+1)).assignTo(positionSgc); 
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
  protected JFrame getFrame(){
    return frame;
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

}
