package de.jreality.swing.jrwindows;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JFrame;

import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.FaceDragEvent;
import de.jreality.tools.FaceDragListener;
import de.jreality.tools.LineDragEvent;
import de.jreality.tools.LineDragListener;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;

/**
 * @author bleicher
 *
 */

public class JRWindowManager implements ActionListener{
  
  private final double[] defaultDesktopWindowPos={0,0,-5};
  private double defaultDesktopBorderRadius=0.01;
  private double defaultDesktopDecoSize=0.08;
  
  private final double[] defaultPortalWindowPos={0,1.5,-1.2};
  private double defaultPortalBorderRadius=0.05;
  private double defaultPortalDecoSize=0.15;
  
  private SceneGraphComponent sgc;
  private ArrayList<JRWindow> windowList;
  private DragEventTool dragTool;
  
  public JRWindowManager(SceneGraphComponent avatar){
    sgc=new SceneGraphComponent();
    sgc.setAppearance(new Appearance());
    sgc.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);    
    avatar.addChild(sgc);
    windowList=new ArrayList<JRWindow>();
    setPosition(defaultDesktopWindowPos);
    initDragTool(); 
  } 
  
  private void initDragTool(){
    dragTool=new DragEventTool("PrimaryAction");
    dragTool.addPointDragListener(new PointDragListener(){
      private int windowNum;
      private double[][] points;
      public void pointDragStart(PointDragEvent e) {
        windowNum=searchWindowNum(e.getPointSet());
        if(windowNum==-1) return;
        if(windowList.get(windowNum).isSmall()) return;
        setWindowInFront(windowNum);
        points=windowList.get(windowNum).getCornerPos();
      }
      public void pointDragged(PointDragEvent e) { 
        if(windowNum==-1) return;
        if(windowList.get(windowNum).isSmall()) return;
        double[] translation={e.getPosition()[0]-points[e.getIndex()][0],e.getPosition()[1]-points[e.getIndex()][1],0,0};    //stimmt nicht ganz..
        double[][] newPoints=new double[points.length][points[0].length];
        for(int i=0; i<points.length;i++){
          for(int j=0;j<points[0].length;j++)
            if((points[i][j]==points[e.getIndex()][j]))
              newPoints[i][j]=points[i][j]+translation[j];
            else
              newPoints[i][j]=points[i][j];
        }   
        windowList.get(windowNum).setCornerPos(newPoints);
      }
      public void pointDragEnd(PointDragEvent e) {
      }});   
    dragTool.addLineDragListener(new LineDragListener(){
      private int windowNum;
      private double[][] points;
      private int[] lineIndices;
      private double[][] line;      
      public void lineDragStart(LineDragEvent e) {
        windowNum=searchWindowNum(e.getIndexedLineSet());
        if(windowNum==-1) return;
        if(windowList.get(windowNum).isSmall()) return;
        setWindowInFront(windowNum);
        points=windowList.get(windowNum).getCornerPos();        
        lineIndices=e.getLineIndices();
        line=e.getLineVertices();        
      }
      public void lineDragged(LineDragEvent e) {
        if(windowNum==-1) return;
        if(windowList.get(windowNum).isSmall()) return;       
        double[][] newPoints=new double[points.length][points[0].length];
        for(int n=0;n<newPoints.length;n++)
          for(int c=0;c<newPoints[0].length;c++)
            newPoints[n][c]=points[n][c];
        double[] translation=Pn.dehomogenize(new double[]{0,0,0},e.getTranslation());
        if(Math.abs(points[lineIndices[0]][0]-points[lineIndices[1]][0])==0){
          for(int i=0;i<lineIndices.length;i++){
            newPoints[lineIndices[i]][0]=line[i][0]+translation[0];
          }
        }else if(Math.abs(points[lineIndices[0]][1]-points[lineIndices[1]][1])==0){
          for(int i=0;i<lineIndices.length;i++){
            newPoints[lineIndices[i]][1]=line[i][1]+translation[1];
          }
        }
        windowList.get(windowNum).setCornerPos(newPoints);
        }
      public void lineDragEnd(LineDragEvent e) {
    }});    
    dragTool.addFaceDragListener(new FaceDragListener(){ 
      private int windowNum;
      private double[][] points;   
      public void faceDragStart(FaceDragEvent e) { 
        windowNum=searchWindowNum(e.getIndexedFaceSet()); 
        if(windowNum==-1) return;        
        setWindowInFront(windowNum);      
        points=windowList.get(windowNum).getCornerPos();
      }
      public void faceDragged(FaceDragEvent e) {
        if(windowNum==-1) return;
        double[] translation=e.getTranslation();
        double[][] newPoints=new double[points.length][points[0].length];
        Pn.dehomogenize(translation,translation);        
        translation[2]=0; //!no z-dragging! use sgc-draggingtool instead (middle mouse-button)        
        if(translation.length==4) translation[3]=0;        
        for(int i=0;i<points.length;i++){
          Rn.add(newPoints[i],points[i],translation);
        }          
        windowList.get(windowNum).setCornerPos(newPoints);
      }
      public void faceDragEnd(FaceDragEvent e) {        
      }});
    
    sgc.addTool(dragTool);
  }
  
  private int searchWindowNum(Geometry matchedGeo){
    int matchedWindowNum=0;
    for(JRWindow win : windowList){      
      if(matchedGeo.equals(win.getBorders())||matchedGeo.equals(win.getDecoDragFace())){
        return matchedWindowNum; 
      }
      matchedWindowNum++;
    }
    return -1; 
  }
  private void setWindowInFront(int windowNum){
    int i=0;
    for(JRWindow win : windowList){
      if(i==windowNum) win.setInFront(true);
      else win.setInFront(false);   
      i++;
    }
  }
  
  public void actionPerformed(ActionEvent e) {    
    if(e.getActionCommand().startsWith("X")){
      String command=e.getActionCommand();
      command=command.replaceFirst(String.valueOf(command.charAt(0)),"");
      int windowNum=Integer.parseInt(command); 
      kill(windowNum);  
    }
    else if(e.getActionCommand().startsWith("O")){
      String command=e.getActionCommand();
      command=command.replaceFirst(String.valueOf(command.charAt(0)),"");
      int windowNum=Integer.parseInt(command);    
      if(windowList.get(windowNum).isSmall()){
        windowList.get(windowNum).setSmall(false);
        setWindowInFront(windowNum);
      }
    } 
    else if(e.getActionCommand().startsWith("_")){
      String command=e.getActionCommand();
      command=command.replaceFirst(String.valueOf(command.charAt(0)),"");
      int windowNum=Integer.parseInt(command);    
      if(!windowList.get(windowNum).isSmall())
        windowList.get(windowNum).setSmall(true);
    } 
  }  
  private void kill(int windowNum){
    JRWindow win2kill=windowList.get(windowNum);     
    sgc.removeChild(win2kill.getSgc());    
    int windowCount=0;
    for(JRWindow win : windowList){
      if(windowCount>windowNum){
        win.setWindowNumber(windowCount-1);
      }
      windowCount++;
    }      
    windowList.remove(windowNum); 
    win2kill=null;
  }
  
  public JFrame createFrame(){ 
    JRWindow window=new JRWindow(getWindowCount());
    window.addActionListeners(this);    
    sgc.addChild(window.getSgc());     
    windowList.add(window);
    setWindowInFront(getWindowCount()-1);
    return window.getFrame();
  }   
  public JFrame getFrame(int i){
    return (JFrame)(windowList.get(i).getFrame());
  }
  public int getWindowCount(){
    return windowList.size();
  }
  public void pack(){
    for(JRWindow win : windowList)
      win.getFrame().pack();
  }    
  
  public void setBorderRadius(double r){
    defaultDesktopBorderRadius=r;
    for(JRWindow win : windowList)
      win.setBorderRadius(r);
  }
  public void setDecoSize(double s){
    defaultDesktopDecoSize=s;
    for(JRWindow win : windowList)
      win.setDecoSize(s);
  }  
  public void setPosition(double[] pos){
    MatrixBuilder.euclidean().translate(pos).assignTo(sgc);
  }
  public void setDragAllWindowsTool(){
    sgc.addTool(new DraggingTool());
  }
  public void setDesktopDefaultValues(){
    setBorderRadius(defaultDesktopBorderRadius);
    setDecoSize(defaultDesktopDecoSize);
    setPosition(defaultDesktopWindowPos);
  }  
  public void setPortalDefaultValues(){
    setBorderRadius(defaultPortalBorderRadius);
    setDecoSize(defaultPortalDecoSize);
    setPosition(defaultPortalWindowPos);
    setDragAllWindowsTool();
  }
}
