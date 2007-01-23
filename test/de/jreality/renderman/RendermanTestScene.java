package de.jreality.renderman;

import java.awt.Color;
import java.io.IOException;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.renderman.RIBViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;

public class RendermanTestScene {
  
  static final int testNr=6;   //testNr: 0..7
  static final int rendererType = RIBViewer.TYPE_PIXAR;
  static final String ribPath = "";
  static final String ribFileName = "";
  static final String globalIncludeFile = "";
  
  public static void main(String[] args) {    
    if(ribPath.equals("") || ribFileName.equals("")){
      System.err.println("RendermanTestScene: ribPath and/or ribFileName not set\n-> set them in the global fields at the top of the class");
      return;
    }
    
    SceneGraphComponent sgc=new SceneGraphComponent();
    
    IndexedFaceSetFactory ifs=new IndexedFaceSetFactory();
    ifs.setVertexCount(8);
    ifs.setVertexCoordinates(new double[][] {{-2,2,0},{-2,-2,0},{2,-2,0},{2,2,0},{-3,3,1},{-3,0,1},{0,0,1},{0,3,1}});
    ifs.setFaceCount(2);
    ifs.setFaceIndices(new int[][]{{0,1,2,3},{4,5,6,7}});
    ifs.setGenerateEdgesFromFaces(true);
    ifs.setVertexTextureCoordinates(new double[][] {{0,0},{0,1},{1,1},{1,0},{0,0},{0,1},{1,1},{1,0}});
    ifs.setVertexColors(new Color[] {Color.BLACK,Color.BLUE,Color.CYAN,Color.GRAY,Color.GREEN,Color.MAGENTA,Color.ORANGE,Color.PINK});
    ifs.setGenerateFaceNormals(true);
    ifs.setGenerateFaceNormals(true);
    ifs.update();

    SceneGraphComponent faceSetNode=new SceneGraphComponent();
    faceSetNode.setGeometry(ifs.getGeometry());
    faceSetNode.setAppearance(new Appearance());
    faceSetNode.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,true);
    faceSetNode.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, true);
    faceSetNode.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, 0.2);
    faceSetNode.getAppearance().setAttribute(CommonAttributes.POINT_SIZE, 0.2);
    faceSetNode.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
    faceSetNode.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, true);
    faceSetNode.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, 0.1);
    faceSetNode.getAppearance().setAttribute(CommonAttributes.LINE_WIDTH, 0.1);
    faceSetNode.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
    sgc.addChild(faceSetNode);
    
    IndexedLineSetFactory ils=new IndexedLineSetFactory();
    ils.setVertexCount(8);
    ils.setVertexCoordinates(new double[][] {{-2,2,0},{-2,-2,0},{2,-2,0},{2,2,0},{-3,3,1},{-3,0,1},{0,0,1},{0,3,1}});
    ils.setLineCount(8);
    ils.setEdgeIndices(new int[][]{{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4}});
    ils.setVertexTextureCoordinates(new double[][] {{0,0},{0,1},{1,1},{1,0},{0,0},{0,1},{1,1},{1,0}});
    ils.setVertexColors(new Color[] {Color.BLACK,Color.BLUE,Color.CYAN,Color.GRAY,Color.GREEN,Color.MAGENTA,Color.ORANGE,Color.PINK});
    ils.setEdgeColors(new Color[] {Color.RED,Color.BLUE,Color.CYAN,Color.GRAY,Color.GREEN,Color.MAGENTA,Color.ORANGE,Color.PINK});
    ils.update();
    
    SceneGraphComponent lineSetNode=new SceneGraphComponent();
    lineSetNode.setGeometry(ils.getGeometry());
    MatrixBuilder.euclidean().translate(3.5,0,0).scale(-0.4,0.4,0.4).assignTo(lineSetNode);
    faceSetNode.addChild(lineSetNode);
    
    ImageData img=null;
    try {
      img = ImageData.load(Input.getInput("textures/outfactory3.png"));
    } catch (IOException e) {e.printStackTrace();}
    
    switch(testNr){
    case 0: break;
    case 1:{
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.7);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
      break;
    }
    case 2:{
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.7);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, false);
      break;
    }
    case 3:{
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.7);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
      break;
    }
    case 4:{
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.7);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, false);
      break;
    }
    case 5:{
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.7);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true); 
      faceSetNode.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, 0.35);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, 0.3);
      Texture2D tex = TextureUtility.createTexture(faceSetNode.getAppearance(), "polygonShader", img, false);
      break;
    }
    case 6:{
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.7);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);  
      faceSetNode.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, 0.35);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, 0.3);
      Texture2D tex = TextureUtility.createTexture(faceSetNode.getAppearance(), "polygonShader", img, false);
      break;
    }
    case 7:{
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.7);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, 0.35);
      faceSetNode.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, 0.3);
      try {
          TextureUtility.createReflectionMap(faceSetNode.getAppearance(), "polygonShader", "textures/emerald/emerald_", new String[]{"rt","lf","up","dn","bk","ft"},".jpg");
        } catch (IOException e) {e.printStackTrace();}
        break;
    }    
    }
    
    ViewerApp va=new ViewerApp(sgc);
    va.update();
    va.display();
    
    RIBViewer ribv=new RIBViewer();
    ribv.initializeFrom(va.getViewer());
    ribv.setRendererType(rendererType);
    ribv.setFileName(ribPath+ribFileName);
    if(ribv.getSceneRoot().getAppearance()==null)  ribv.getSceneRoot().setAppearance(new Appearance());
    if(!globalIncludeFile.equals(""))  ribv.getSceneRoot().getAppearance().setAttribute(CommonAttributes.RMAN_GLOBAL_INCLUDE_FILE, globalIncludeFile);
    ribv.render();
  }
}
