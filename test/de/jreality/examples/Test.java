/*
 * Created on Dec 6, 2003
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.jogl.Viewer;
import de.jreality.reader.Parser3DS;
import de.jreality.scene.*;
import de.jreality.scene.proxy.treeview.SceneTreeViewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.soft.DefaultViewer;
import de.jreality.soft.MouseTool;
import de.jreality.util.RenderTrigger;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class Test implements KeyListener {
  private  int SPEED_TEST_ITERATIONS = 0;//set to 0 to disable
  private  boolean SHOW_CATENOIDS = true;
  private  boolean SHOW_ONE_CATENOID = false;
  private  boolean SHOW_SPHERES   = false;
  private  boolean SHOW_MODEL   = false;
  private  boolean SHOW_VERTEX_COLOR = false;
  private  boolean SHOW_CLIPPING = false;
  
  private boolean useSoftViewer = true;
  private boolean USE_DOUBLE_RASTERIZER = false;
  private boolean useJOGLViewer = true;
  
  public static void main(String[] args) {
    Logger.getLogger("de.jreality").setLevel(Level.INFO);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
    Test t= new Test();
  }

  private DefaultViewer softViewer;
  protected Viewer joglViewer;
  
  private Frame         frameSoft;
  private Frame         frameJOGL;
  private Frame         frameTree;
  
  private MouseTool     mouseTool;
  private Camera        firstCamera;

  private CatenoidHelicoid catenoid;
  private RenderTrigger trigger = new RenderTrigger();
  public Test() {
    //
    // the scene root;
    //
    SceneGraphComponent root = new SceneGraphComponent();
    Appearance a =new Appearance();
    a.setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.WHITE); //new Color(.4f,.5f,.8f));
    //testing new feature of JOGL backend to generate unique dl's for each occurrance of Geometry
    root.setAppearance(a);
    buildView(root);
    buildScene(root);
    root.getAppearance().setAttribute(CommonAttributes.MANY_DISPLAY_LISTS, true); 
   trigger.addSceneGraphComponent(root);
    if(useSoftViewer) {
        softViewer= new DefaultViewer(USE_DOUBLE_RASTERIZER);
        softViewer.setBackground(new Color(.4f,.5f,.8f));
        trigger.addViewer(softViewer);
        frameSoft= new Frame();
        frameSoft.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frameSoft.setLayout(new BorderLayout());
        frameSoft.add(softViewer, BorderLayout.CENTER);

        mouseTool= new MouseTool(softViewer);
        mouseTool.setViewer(softViewer.getViewingComponent());
        mouseTool.setRoot(root);
        mouseTool.setCamera(firstCamera);
        
        checkMouseWheel();
    
        softViewer.getViewingComponent().addKeyListener(this);
        softViewer.setSceneRoot(root);
        softViewer.setCameraPath(SceneGraphPath.getFirstPathBetween(root,firstCamera));

    //frameSoft.add(SceneTreeViewer.getViewerComponent(root, softViewer), BorderLayout.WEST);
    frameSoft.setSize(780, 580);
    frameSoft.validate();
    frameSoft.setVisible(true);

    softViewer.render();
    if(SPEED_TEST_ITERATIONS>0)
    {
      frameSoft.setEnabled(false);
      frameSoft.setTitle("Speed testing");
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
  
      softViewer.renderSync();
      softViewer.renderSync();
      softViewer.renderSync();//ensure constant loading and jit state
      System.gc();
      long time= System.currentTimeMillis();
      for (int i= 0; i < SPEED_TEST_ITERATIONS; i++) {
        softViewer.renderSync();
      }
      time= System.currentTimeMillis() - time;
      System.out.println("\033[36m"+SPEED_TEST_ITERATIONS + " times render took "
        + time+" ms, avg. "+((double)time/SPEED_TEST_ITERATIONS)+" ms\033[39m");
      frameSoft.setTitle(null);
      frameSoft.setEnabled(true);
    }
    }
    //softViewer.setBackground(1,1,1);		

    if(useJOGLViewer) {
    	   root.getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.WHITE); //new Color(.4f,.5f,.8f));

        joglViewer= new Viewer(SceneGraphPath.getFirstPathBetween(root,firstCamera),root);
        trigger.addViewer(joglViewer);
        frameJOGL= new Frame();
        frameJOGL.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frameJOGL.setLayout(new BorderLayout());
        frameJOGL.add(joglViewer.getViewingComponent(), BorderLayout.CENTER);

//        mouseTool= new MouseTool(joglViewer);
//        mouseTool.setViewer(joglViewer.getViewingComponent());
//        mouseTool.setRoot(root);
//        mouseTool.setCamera(firstCamera);
        mouseTool.addToTriggerList(joglViewer);
        //checkMouseWheel();
        
        joglViewer.getViewingComponent().addKeyListener(this);
        //joglViewer.setSceneRoot(root);
        //joglViewer.setCameraPath(SceneGraphPath.getFirstPathBetween(root,firstCamera));

        frameJOGL.add(SceneTreeViewer.getViewerComponent(root, softViewer), BorderLayout.WEST);
        frameJOGL.setSize(780, 580);
        frameJOGL.validate();
        frameJOGL.setVisible(true);
        //joglViewer.getRenderer().setUseDisplayLists(false);
        joglViewer.render();
    }
    
//    mouseTool.removeFromTriggerList(softViewer);
//    mouseTool.addToTriggerList(softViewer);
    }

  /**
     * @return
     */
  private SceneGraphComponent buildScene(SceneGraphComponent root) {
//    TestScenarioBuilder b=new TestScenarioBuilder();
////    b.createThreeSpheres();
////    b=(TestScenarioBuilder)b.up().translate(0, 0, 2).setStretch(.3);
////    b.createFourCatenoids();
//    b.createSceneA();
//    root.addChild(b.root());
//    b.add(b.createDefaultPolygonShader()).addChild(Gimmick.createWelcome());
//    root.addChild(b.root());

    if(SHOW_MODEL) {
        //Logger.getLogger("de.jreality").setLevel(Level.SEVERE);
        SceneGraphComponent s1 = Parser3DS.readFromFile("/home/timh/tmp/ulrichData/schwarz.3ds");
        //SceneGraphComponent s1 = Parser3DS.readFromFile("/home/timh/tmp/scherk4.3ds");
        
        //GeometryUtility.calculateVertexNormals(s1);
        Transformation scale = new Transformation();
        scale.setStretch(.05);
        s1.setTransformation(scale);
        Appearance a =new Appearance();
        a.setAttribute(CommonAttributes.POLYGON_SHADER, "flat");
        Texture2D tex =null;
        try {
            //tex = new Texture2D("/home/timh/tmp/ulrichData/schwarzBlue.png");
            tex = new Texture2D("/home/timh/tmp/ulrichData/schwarz.png");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        a.setAttribute("polygonShader.texture", tex);
        a.setAttribute("texture2d", tex);
        a.setAttribute("diffuseColor", new Color(0.4f, 0.4f, 0.4f));
        a.setAttribute("showLines", false);
        a.setAttribute("showPoints", false);
        //a.setAttribute("outline", true);
        s1.setAppearance(a);
        s1.getChildComponent(0).setAppearance(a);
        root.addChild(s1);
    }
    if (SHOW_SPHERES) {
      

      // 
      // the first sphere
      //
      SceneGraphComponent sphereNode= new SceneGraphComponent();
      sphereNode.setName("spheres");

      Transformation tr= new Transformation();
      //tr.setTranslation(-1.2, -1.2, -0.6);
      //tr.setStretch(.2);
      sphereNode.setTransformation(tr);

      Appearance ap= new Appearance();
      sphereNode.setAppearance(ap);

      ap.setAttribute(CommonAttributes.LEVEL_OF_DETAIL, 14);
      //ap.setAttribute("transparency", 0.5);
      ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(0.2f, 0.2f, 0.6f));
      //ap.setAttribute("outline", true);

      TestScenarioBuilder tstBuilder =new TestScenarioBuilder();
      SceneGraphComponent s = tstBuilder.createFourSpheres();
      sphereNode.addChild(s);
      root.addChild(sphereNode);
    }

    if (SHOW_CATENOIDS) {
        
        /*

    //root.addChild(Gimmick.createMessage("J R"));
     
     */
        SceneGraphComponent catNode= new SceneGraphComponent();
        catNode.setName("catenoids");

        Transformation tr= new Transformation();
       tr.setRotation(Math.PI, 0,1,0);
        //tr.setStretch(.2);
        catNode.setTransformation(tr);

        Appearance ap= new Appearance();
        catNode.setAppearance(ap);

        //ap.setAttribute("transparency", 0.5);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(0.2f, 0.2f, 0.6f));
        ap.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, 0.0);
        //ap.setAttribute("outline", true);

        TestScenarioBuilder tstBuilder =new TestScenarioBuilder();
        SceneGraphComponent s = tstBuilder.createFourCatenoids();
        //currently jogl backend requires unique geometries to get the proxy geometry right
//        for (int i = 1; i<4; ++i)  {
//        		s.getChildComponent(i).getChildComponent(0).setGeometry(tstBuilder.createCatenoid());
//           	s.getChildComponent(i).getChildComponent(0).removeChild(s.getChildComponent(i).getChildComponent(0).getChildComponent(0));
//        }
        //catenoid =(CatenoidHelicoid) s.getChildComponent(0).getChildComponent(0).getGeometry();
        //System.out.println("catenoid" + catenoid);
        catNode.addChild(s);
        
        
        root.addChild(catNode);
        
        
    }
    if (SHOW_ONE_CATENOID) {
        
        /*

         //root.addChild(Gimmick.createMessage("J R"));
         
         */
        SceneGraphComponent catNode= new SceneGraphComponent();
        catNode.setName("one catenoid");

        Transformation tr= new Transformation();
        //tr.setTranslation(-1.2, -1.2, -0.6);
        //tr.setStretch(.2);
        catNode.setTransformation(tr);

        Appearance ap= new Appearance();
        catNode.setAppearance(ap);

        //ap.setAttribute("transparency", 0.5);
        ap.setAttribute(CommonAttributes.FACE_DRAW, true);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1.0f, 1.0f, 1.0f));
        ap.setAttribute(CommonAttributes.POLYGON_SHADER, "flat");
        ap.setAttribute(CommonAttributes.POLYGON_SHADER+".vertexShader", "constant");
        
        
        
        ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(.0f, .0f, .0f));
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER, "flat");
        ap.setAttribute(CommonAttributes.LINE_SHADER+".vertexShader", "constant");
        ap.setAttribute("lineWidth", .025);
        
        
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);

        TestScenarioBuilder tstBuilder =new TestScenarioBuilder();
        catenoid =(CatenoidHelicoid) tstBuilder.createCatenoid();
        catNode.setGeometry(catenoid);
        
        
        root.addChild(catNode);
        
        
    }
    if (SHOW_VERTEX_COLOR) {
        
        SceneGraphComponent catNode= new SceneGraphComponent();
        catNode.setName("vertex color");

        Transformation tr= new Transformation();
        catNode.setTransformation(tr);

        Appearance ap= new Appearance();
        catNode.setAppearance(ap);

        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(0.2f, 0.2f, 0.6f));
       
        
        TestScenarioBuilder tstBuilder =new TestScenarioBuilder();
        SceneGraphComponent s =tstBuilder.createVertexColorCatenoid();
        catenoid =(CatenoidHelicoid) s.getChildComponent(0).getChildComponent(0).getGeometry();
        //System.out.println("catenoid " + catenoid);
        catNode.addChild(s);
        
        root.addChild(catNode);
        
        
    }
    if(SHOW_CLIPPING) {
        ClippingPlane cp =new ClippingPlane();
        SceneGraphComponent cpc = new SceneGraphComponent();
        Transformation trans= new Transformation();
        //trans.setRotation(-.2,0,1,0);
        trans.setTranslation(0,0,0);
        cpc.setTransformation(trans);
        cpc.setGeometry(cp);
        root.addChild(cpc);
        
        cpc = new SceneGraphComponent();
        trans= new Transformation();
        trans.setRotation(Math.PI,0,1,0);
        trans.setTranslation(0,0,-6);
        cpc.setTransformation(trans);
        cpc.setGeometry(cp);
        root.addChild(cpc);
        
    }
    return root;
  }
  private SceneGraphComponent buildView(SceneGraphComponent root) {

    //
    // camera
    //
    SceneGraphComponent cameraNode= new SceneGraphComponent();
    Transformation ct= new Transformation();
    ct.setTranslation(0, 0, 16);
    cameraNode.setTransformation(ct);
    firstCamera= new Camera();
    firstCamera.setFieldOfView(30);
    firstCamera.setFar(50);
    firstCamera.setNear(3);
    cameraNode.setCamera(firstCamera);

    SceneGraphComponent lightNode= new SceneGraphComponent();
    Transformation lt= new Transformation();
    lt.setRotation(-Math.PI / 4, 1, 1, 0);
    lightNode.setTransformation(lt);
    DirectionalLight light= new DirectionalLight();
    lightNode.setLight(light);
    root.addChild(lightNode);

    SceneGraphComponent lightNode2= new SceneGraphComponent();
    Transformation lt2= new Transformation();
    //   lt2.assignScale(-1);
    lt.setRotation(-Math.PI / 4, 1, 1, 0);
    lightNode2.setTransformation(lt2);
    DirectionalLight light2= new DirectionalLight();
    lightNode2.setLight(light2);
    root.addChild(lightNode2);


    Appearance ap= new Appearance();
    ap.setAttribute("diffuseColor", new Color(1f, 0f, 0f));
    ap.setAttribute("lightingEnabled", true);
    
    root.setAppearance(ap);

    //   Graph ps = new Graph();
    //   ps.setNumPoints(4);
    //   ps.setPoint(0,-.4,-.4,0);
    //   ps.setPoint(1,.4,.4,0);
    //   ps.setPoint(2,.6,-.6,0);
    //   ps.setPoint(3,-.6,.6,0);
    //   ps.setNumEdges(5);
    //   ps.setEdge(0,0,1);
    //   ps.setEdge(1,1,2);
    //   ps.setEdge(2,2,3);
    //   ps.setEdge(3,3,0);
    //   ps.setEdge(4,0,2);
    //
    //   SceneGraphComponent psc = new SceneGraphComponent();
    //   psc.setGeometry(ps);

    root.addChild(cameraNode);
    //   root.addChild(psc);

    //   System.out.println("--->>>"+SceneGraphUtilities.effectiveAttribute((SceneGraphComponent)sphereNode,"material"));  
    return root;
  }

  public static void print(double[] d) {
    for (int i= 0; i < 4; i++) {
      System.out.println("| "+d[0+4*i]+"\t "+d[1+4*i]+"\t "
        +d[2+4*i]+"\t "+d[3+4*i]+ "\t |");
    }
  }


  /**
   * Install mouse wheel handler if supported (read: jdk1.4 or higher).
   */
  private void checkMouseWheel()
  {
    if(!(SHOW_CATENOIDS||SHOW_VERTEX_COLOR)) return;
    try
    {
      Class listenerClass=Class.forName("java.awt.event.MouseWheelListener");
      Class eventClass=Class.forName("java.awt.event.MouseWheelEvent");
      final Class[] listenerClassArray={listenerClass};
      java.lang.reflect.Method install=softViewer.getClass()
        .getMethod("addMouseWheelListener", listenerClassArray);
      final java.lang.reflect.Method getValue=eventClass
        .getMethod("getWheelRotation", null);
      install.invoke(softViewer, new Object[]{java.lang.reflect.Proxy.newProxyInstance(
        listenerClass.getClassLoader(), listenerClassArray,
        new java.lang.reflect.InvocationHandler()
        {
          public Object invoke(Object proxy, java.lang.reflect.Method method,
            Object[] args) throws Throwable
          {
            final Object v=getValue.invoke(args[0], null);
            mouseWheelMoved(((Integer)v).doubleValue());
            return null;
          }
        })});
    }
    catch (ClassNotFoundException e){}
    catch (NoSuchMethodException e){}
    catch (IllegalArgumentException e){}
    catch (IllegalAccessException e){}
    catch (java.lang.reflect.InvocationTargetException e){}
  }
  void mouseWheelMoved(double wheelRotation) {
    if(catenoid != null) {
      double alpha=catenoid.getAlpha()+wheelRotation*Math.PI/40;
      catenoid.setAlpha(alpha);
      trigger.forceRender();
    }
  }

  
  public void keyPressed(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {
  }

  public void keyTyped(KeyEvent e) {
//      if (e.getKeyChar() == 's') {
//          SVGViewer svgv =new SVGViewer("test.svg");
//          svgv.setSceneRoot(softViewer.getSceneRoot());
//          svgv.setCamera(softViewer.getCamera());
//          svgv.setWidth(softViewer.getWidth());
//          svgv.setHeight(softViewer.getHeight());
//          System.out.print(" Rendering SVG into test.svg..");
//          svgv.render();
//          System.out.println(".done.");
//          
//      }
//      if (e.getKeyChar() == 'p') {
//          PSViewer psv =new PSViewer("test.eps");
//          psv.setSceneRoot(softViewer.getSceneRoot());
//          psv.setCamera(softViewer.getCamera());
//          psv.setWidth(softViewer.getWidth());
//          psv.setHeight(softViewer.getHeight());
//          System.out.print(" Rendering PS into test.eps..");
//          psv.render();
//          System.out.println(".done.");
//          
//      }
//      if (e.getKeyChar() == 'a') {
//          RIBVisitor rv =new RIBVisitor();
//          rv.setWidth(softViewer.getWidth());
//          rv.setHeight(softViewer.getHeight());
//          System.out.print(" Rendering RIB into test.rib..");
//          rv.visit(softViewer.getSceneRoot(),softViewer.getCamera(),"test.rib");
//          System.out.println(".done.");          
//      }
  }
  
}
