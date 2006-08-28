package de.jreality.swing.jrwindows;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
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
import de.jreality.swing.JFakeFrame;

/**
 * @author bleicher
 *
 */

public class JRWindow {  
  private static final long serialVersionUID = 1800202311242061598L;
  
  private final double borderRadius=0.01;
  private final double cornerRadius=borderRadius*1.5;
  private final double translateFactor=1.1*cornerRadius;
  
  private int windowNumber;
  
  private IndexedFaceSet frameFace;
  private IndexedLineSet borders;
  private IndexedFaceSet decoControlFace;
  private IndexedFaceSet decoDragFace;
  private JFakeFrame frame;
  private JFakeFrame decoControlFrame;
  private SceneGraphComponent positionSgc;
  private SceneGraphComponent orientationSgc; 
  private SceneGraphComponent frameSgc;
  private SceneGraphComponent borderSgc;
  private SceneGraphComponent decoControlSgc;  
  private SceneGraphComponent decoDragSgc;
  private final double[][] startCornerPos={{1,0,0},{1,-1,0},{-1,-1,0},{-1,0,0}};
  private double[][] cornerPos;
  private final double decoSize=(startCornerPos[0][1]-startCornerPos[1][1])/10;
  private final double decoControlSize=4*decoSize; 
  private double[][] faceCorners;
  private double[][] decoControlCorners;
  private double[][] decoDragCorners;
  private final double[][] foldAwayCornerPos={{1,0,-1},{1,0,0},{-1,0,0},{-1,0,-1}};
  private double[][] foldAwayFaceCorners;
  private double[][] foldAwayDecoControlCorners;
  private double[][] foldAwayDecoDragCorners;

  
  public JRWindow(int windowNumber){    
    super();  
    this.windowNumber=windowNumber; 
    cornerPos=new double[startCornerPos.length][startCornerPos[0].length];
    for(int n=0;n<cornerPos.length;n++)
      for(int c=0;c<cornerPos[0].length;c++)
        cornerPos[n][c]=startCornerPos[n][c];
    foldAwayFaceCorners=new double[cornerPos.length][cornerPos[0].length];    calculateFaceCorners(foldAwayFaceCorners,foldAwayCornerPos);
    foldAwayDecoControlCorners=new double[cornerPos.length][cornerPos[0].length];    calculateDecoControlCorners(foldAwayDecoControlCorners,cornerPos);
    foldAwayDecoDragCorners=new double[cornerPos.length][cornerPos[0].length];    calculateDecoDragCorners(foldAwayDecoDragCorners,cornerPos);
    initSgc();
    initFrame();
    initDecoration();    
  }  
  
  private void initSgc(){
    positionSgc=new SceneGraphComponent();
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
    frame.setVisible(true);
    
    frameSgc.addTool(frame.getTool());
    frameSgc.setAppearance(frame.getAppearance());  
    frameSgc.setGeometry(frameFace); 
    frameSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
    frameSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,false);
  }
  
  private JButton killButton;
  private JButton maximizeButton;
  private JButton foldAwayButton;
  private final Color activeColor=new Color(0,10,20);
  private final Color inactiveColor=new Color(70,70,70);
  
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
    
    decoControlFrame=new JFakeFrame();
    
    decoControlSgc.addTool(decoControlFrame.getTool());
    decoControlSgc.setAppearance(decoControlFrame.getAppearance());  
    decoControlSgc.setGeometry(this.decoControlFace);  
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW,false);
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,true);
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,true); 
    decoControlSgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS,borderRadius/3);
    
    JMenuBar menuBar=new JMenuBar();    
    menuBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    JToolBar toolBar=new JToolBar(); 
    toolBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);    
    killButton = new JButton("X");
    toolBar.add(killButton);
    maximizeButton=new JButton("O");
    toolBar.add(maximizeButton);
    foldAwayButton = new JButton("_");
    toolBar.add(foldAwayButton); 
    menuBar.add(toolBar);
    decoControlFrame.setJMenuBar(menuBar);    
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
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS,borderRadius/3);
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,activeColor);
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(200,200,200));
    decoDragSgc.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(200,200,200));
    
  
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
    borderSgc.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS,cornerRadius);
    borderSgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,true);
    borderSgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW,true);
    borderSgc.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS,borderRadius); 
    borderSgc.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR,new Color(200,200,200));
  }   
  
  private void calculateFaceCorners(double[][] faceCorners, double[][] cornerPos){
    Rn.subtract(faceCorners[0],cornerPos[1],cornerPos[0]);
    Rn.normalize(faceCorners[0],faceCorners[0]);
    Rn.times(faceCorners[0],decoSize,faceCorners[0]);
    Rn.add(faceCorners[0],cornerPos[0],faceCorners[0]);    
    faceCorners[1]=cornerPos[1];
    faceCorners[2]=cornerPos[2];
    Rn.subtract(faceCorners[3],cornerPos[2],cornerPos[3]);
    Rn.normalize(faceCorners[3],faceCorners[3]);
    Rn.times(faceCorners[3],decoSize,faceCorners[3]);
    Rn.add(faceCorners[3],cornerPos[3],faceCorners[3]); 
  }    
  private void calculateDecoControlCorners(double[][] decoControlCorners,double[][] cornerPos){      
    decoControlCorners[0]=cornerPos[0];
    
    Rn.subtract(decoControlCorners[1],cornerPos[1],cornerPos[0]);
    Rn.normalize(decoControlCorners[1],decoControlCorners[1]);
    Rn.times(decoControlCorners[1],decoSize,decoControlCorners[1]);
    Rn.add(decoControlCorners[1],cornerPos[0],decoControlCorners[1]); 
   
    double[] trans=new double[decoControlCorners[0].length];
    Rn.subtract(trans,cornerPos[3],cornerPos[0]);
    Rn.normalize(trans,trans);
    Rn.times(trans,decoControlSize,trans);
    
    Rn.subtract(decoControlCorners[2],cornerPos[2],cornerPos[3]);
    Rn.normalize(decoControlCorners[2],decoControlCorners[2]);
    Rn.times(decoControlCorners[2],decoSize,decoControlCorners[2]);
    Rn.add(decoControlCorners[2],trans,decoControlCorners[2]);
    Rn.add(decoControlCorners[2],cornerPos[0],decoControlCorners[2]);

    Rn.add(decoControlCorners[3],trans,cornerPos[0]);
  }
  private void calculateDecoDragCorners(double[][] decoDragCorners,double[][] cornerPos){  
    double[] trans=new double[decoDragCorners[0].length];
    Rn.subtract(trans,cornerPos[3],cornerPos[0]);
    Rn.normalize(trans,trans);
    Rn.times(trans,decoControlSize,trans);
    
    Rn.add(decoDragCorners[0],trans,cornerPos[0]);
    
    Rn.subtract(decoDragCorners[1],cornerPos[2],cornerPos[3]);
    Rn.normalize(decoDragCorners[1],decoDragCorners[1]);
    Rn.times(decoDragCorners[1],decoSize,decoDragCorners[1]);
    Rn.add(decoDragCorners[1],trans,decoDragCorners[1]);
    Rn.add(decoDragCorners[1],cornerPos[0],decoDragCorners[1]);
    
    Rn.subtract(decoDragCorners[2],cornerPos[2],cornerPos[3]);
    Rn.normalize(decoDragCorners[2],decoDragCorners[2]);
    Rn.times(decoDragCorners[2],decoSize,decoDragCorners[2]);
    Rn.add(decoDragCorners[2],cornerPos[3],decoDragCorners[2]); 

    decoDragCorners[3]=cornerPos[3];    
  }
    
  public void addActionListeners(ActionListener actionListener){
    killButton.addActionListener(actionListener);    
    maximizeButton.addActionListener(actionListener);    
    foldAwayButton.addActionListener(actionListener);
    updateActionCommands();
  }
  private void updateActionCommands(){
    killButton.setActionCommand("X"+windowNumber);
    maximizeButton.setActionCommand("O"+windowNumber);
    foldAwayButton.setActionCommand("_"+windowNumber);
  }
  
  public int getWindowNumber(){
    return windowNumber;
  }  
  public SceneGraphComponent getSgc(){
    return positionSgc;
  }  
  public JFrame getFrame(){
    return frame;
  }  
  public IndexedFaceSet getFrameFace(){
    return frameFace;
  }  
  public IndexedFaceSet getDecoControlFace(){
    return decoControlFace;
  }
  public IndexedFaceSet getDecoDragFace(){
    return decoDragFace;
  }
  public IndexedLineSet getBorders(){
    return borders;
  }

  public void setInFront(boolean setInFront){
    if(setInFront){
      decoDragSgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,activeColor);
      MatrixBuilder.euclidean().assignTo(positionSgc); 
    }else{
      decoDragSgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,inactiveColor);
      MatrixBuilder.euclidean().translate(0,0,-translateFactor*(windowNumber+1)).assignTo(positionSgc); 
    }
  }
  
  private final double distance=JRWindowManager.windowPos[2]/2;
  private final double alpha=Math.PI/3;
  private boolean isFoldAway;
  
  public void foldAway(int foldAwayNum, int foldAwayWindowCount, boolean foldAway){
    this.isFoldAway=foldAway;
    if(foldAway){      
      double beta=alpha-alpha/foldAwayWindowCount;     
      if(foldAwayWindowCount==1)
        MatrixBuilder.euclidean().translate(0,-1.1*distance/3,0).translate(0,0,-distance).scale(0.9*distance*Math.tan(alpha/2)/foldAwayCornerPos[0][0]).assignTo(orientationSgc);
      else
        MatrixBuilder.euclidean().translate(0,-distance/3,0).rotate(beta/2-foldAwayNum*beta/(foldAwayWindowCount-1),0,1,0).translate(0,0,-distance).scale(distance*Math.tan(beta/(foldAwayWindowCount-1)/2)/foldAwayCornerPos[0][0]).assignTo(orientationSgc);
      frameFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(foldAwayFaceCorners));  
      decoControlFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(foldAwayDecoControlCorners));  
      decoDragFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(foldAwayDecoDragCorners));  
      borders.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(foldAwayCornerPos)); 
    }else{      
      MatrixBuilder.euclidean().assignTo(orientationSgc);
      frameFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(faceCorners));
      decoControlFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(decoControlCorners));
      decoDragFace.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(decoDragCorners));  
      borders.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(cornerPos)); 
    }
  }  
  public boolean isFoldAway(){
    return isFoldAway;
  }
  
  public void setCornerPos(double[][] newCornerPos){ 
    if(newCornerPos[0][0]-newCornerPos[3][0]>=decoControlSize&&newCornerPos[0][1]-newCornerPos[1][1]>=decoSize){
      cornerPos=newCornerPos;    
    }else if(newCornerPos[0][0]-newCornerPos[3][0]>=decoControlSize||newCornerPos[0][1]-newCornerPos[1][1]>=decoSize){ 
      if(newCornerPos[0][0]-newCornerPos[3][0]>decoControlSize){ //copy x   
        for(int n=0;n<cornerPos.length;n++)
          cornerPos[n][0]=newCornerPos[n][0];
      }else if(newCornerPos[0][1]-newCornerPos[1][1]>decoSize){  //copy y   
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
    
  }
  public double[][] getCornerPos(){
    return cornerPos;
  }
  
  public void setStartSize(){    
    double[] startCenter={0,0,0};
    for(int n=0;n<startCornerPos.length;n++)
      Rn.add(startCenter,startCenter,startCornerPos[n]);
    Rn.times(startCenter,1/(double)startCornerPos.length,startCenter);      
    double[] center={0,0,0};
    for(int n=0;n<cornerPos.length;n++)
      Rn.add(center,center,cornerPos[n]);
    Rn.times(center,1/(double)cornerPos.length,center);      
    Rn.subtract(center,center,startCenter);        
    for(int n=0;n<cornerPos.length;n++)
      Rn.add(cornerPos[n],center,startCornerPos[n]);     
    setCornerPos(cornerPos);
  }
  
  public void setWindowNumber(int windowNumber){
    this.windowNumber=windowNumber;
    updateActionCommands();
  }
  
}
