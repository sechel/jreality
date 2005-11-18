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

import de.jreality.geometry.*;
import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.reader.Parser3DS;
import de.jreality.scene.*;
import de.jreality.scene.proxy.treeview.SceneTreeViewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.soft.DefaultViewer;
import de.jreality.soft.GameTool;
import de.jreality.soft.MouseTool;
import de.jreality.util.SceneGraphUtility;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class SoftTextureTest implements KeyListener {
  private static final int SPEED_TEST_ITERATIONS = 0;//set to 0 to disable
  private static final boolean SHOW_CATENOIDS = true;
  private static final boolean SHOW_SPHERES   = false;
  private static final boolean USE_DOUBLE_RASTERIZER = false;
  private static final boolean SHOW_SKY = true;

  public static void main(String[] args) {
    Logger.getLogger("de.jreality").setLevel(Level.INFO);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
    SoftTextureTest t= new SoftTextureTest();
  }

  private DefaultViewer v;
  private Frame         frame;
  private MouseTool     mouseTool;
  private Camera        firstCamera;

  private CatenoidHelicoid catenoid;
  public SoftTextureTest() {
    v= new DefaultViewer(USE_DOUBLE_RASTERIZER);
    frame= new Frame();
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    frame.setLayout(new BorderLayout());
    frame.add(v, BorderLayout.CENTER);

    
    
    v.getViewingComponent().addKeyListener(this);

    //
    // the scene root;
    //
    SceneGraphComponent root = new SceneGraphComponent();
    buildView(root);
    buildScene(root);
    //v.setBackground(1,1,1);		
    v.setSceneRoot(root);
    v.setCameraPath(((SceneGraphPath) SceneGraphUtility.getPathsBetween(root, firstCamera).get(0)));

    frame.add(SceneTreeViewer.getViewerComponent(root, v), BorderLayout.WEST);
    frame.setSize(780, 580);
    frame.validate();
    frame.setVisible(true);

    v.render();
    if(SPEED_TEST_ITERATIONS>0)
    {
      frame.setEnabled(false);
      frame.setTitle("Speed testing");
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
  
      v.renderSync();
      v.renderSync();
      v.renderSync();//ensure constant loading and jit state
      System.gc();
      long time= System.currentTimeMillis();
      for (int i= 0; i < SPEED_TEST_ITERATIONS; i++) {
        v.renderSync();
      }
      time= System.currentTimeMillis() - time;
      System.out.println("\033[36m"+SPEED_TEST_ITERATIONS + " times render took "
        + time+" ms, avg. "+((double)time/SPEED_TEST_ITERATIONS)+" ms\033[39m");
      frame.setTitle(null);
      frame.setEnabled(true);
    }

  }

  /**
     * @return
     */
  private SceneGraphComponent buildScene(SceneGraphComponent root) {

      if(SHOW_SKY) {
            v.setBackground(Color.RED);
//            SceneGraphComponent skybox =SkyBoxFactory.createSkyBox(
//          "/home/timh/tmp/seaofdreams/seaofdreamsbox3front.jpg",
//          "/home/timh/tmp/seaofdreams/seaofdreamsbox3back.jpg",
//          "/home/timh/tmp/seaofdreams/seaofdreamsbox3left.jpg",
//          "/home/timh/tmp/seaofdreams/seaofdreamsbox3right.jpg",
//          "/home/timh/tmp/seaofdreams/seaofdreamsbox3top.jpg",
//          null
//          );
          
            SceneGraphComponent skybox =SkyBoxFactory.createSkyBox(
                    "/home/timh/tmp/boxes/desertstorm/desertstorm_ft.JPG",
                    "/home/timh/tmp/boxes/desertstorm/desertstorm_bk.JPG",
                    "/home/timh/tmp/boxes/desertstorm/desertstorm_lf.JPG",
                    "/home/timh/tmp/boxes/desertstorm/desertstorm_rt.JPG",
                    "/home/timh/tmp/boxes/desertstorm/desertstorm_up.JPG",
                    "/home/timh/tmp/boxes/desertstorm/desertstorm_dn.JPG"
            );
            
//            SceneGraphComponent skybox =SkyBoxFactory.createSkyBox(
//                    "/home/timh/tmp/boxes/snowblind/snowblind_ft.JPG",
//                    "/home/timh/tmp/boxes/snowblind/snowblind_bk.JPG",
//                    "/home/timh/tmp/boxes/snowblind/snowblind_lf.JPG",
//                    "/home/timh/tmp/boxes/snowblind/snowblind_rt.JPG",
//                    "/home/timh/tmp/boxes/snowblind/snowblind_up.JPG",
//                    "/home/timh/tmp/boxes/snowblind/snowblind_dn.JPG"
//            );

//            SceneGraphComponent skybox =SkyBoxFactory.createSkyBox(
//                    "/home/timh/tmp/boxes/hexagon/hexagon512_ft.png",
//                    "/home/timh/tmp/boxes/hexagon/hexagon512_bk.png",
//                    "/home/timh/tmp/boxes/hexagon/hexagon512_lf.png",
//                    "/home/timh/tmp/boxes/hexagon/hexagon512_rt.png",
//                    "/home/timh/tmp/boxes/hexagon/hexagon512_up.png",
//                    "/home/timh/tmp/boxes/hexagon/hexagon512_dn.png"
//            );
           
          root.addChild(skybox);
          
          if(true) {
              double s = 40;
              double h = -5;
              SceneGraphComponent bottomC = new SceneGraphComponent();
              IndexedFaceSet bottom = Primitives.texturedSquare(new double[] {
                      -s, h, s, 
                      -s, h,-s, 
                       s, h,-s,
                       s, h, s, 
                      
              });
              bottomC.setGeometry(bottom);
              bottomC.setName("terrain");
              
              Logger.getLogger("de.jreality").setLevel(Level.SEVERE);
              SceneGraphComponent s1 = Parser3DS.readFromFile("/home/timh/tmp/terrain.3ds");
              bottomC.setGeometry(s1.getChildComponent(0).getGeometry());
              GeometryUtility.calculateVertexNormals(s1);
              Transformation t =new Transformation();
              t.setStretch(.2);
              t.setTranslation(0,-5,0);
              bottomC.setTransformation(t);
              Appearance ap = new Appearance();
              bottomC.setAppearance(ap);
              Texture2D texture;
            try {
                texture = new Texture2D("/usr/lib/openoffice/share/gallery/www-back/sand.jpg");
                texture.setSScale(8);
                texture.setTScale(8);
                ap.setAttribute("polygonShader","default");
                ap.setAttribute("polygonShader.vertexShader","default");
                ap.setAttribute("polygonShader.diffuseColor", new Color(.9f,.8f,.5f));
                //ap.setAttribute("polygonShader.texture",texture);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
              
              skybox.addChild(bottomC);
          }
          mouseTool= new GameTool(v);
      } else {
        mouseTool= new MouseTool(v);   
      }

      mouseTool.setRoot(root);
      mouseTool.setViewer(v.getViewingComponent());
      checkMouseWheel();
      mouseTool.setCamera(firstCamera);
      
      
    if (SHOW_CATENOIDS) {
      catenoid= new CatenoidHelicoid(50);

      catenoid.buildEdgesFromFaces();

      Transformation gt;
      
      Appearance ap;
      
      // 3.
  
      SceneGraphComponent globeNode3= new SceneGraphComponent();
      gt= new Transformation();
      gt.setTranslation(0., .0, 1.2);
      gt.setStretch(.7);
      gt.setRotation(Math.PI/2.,1,0,0);
      globeNode3.setTransformation(gt);
      globeNode3.setGeometry(catenoid);
  
      ap= new Appearance();
      globeNode3.setAppearance(ap);

        //ap.setAttribute("polygonShader", "flat");
          //ap.setAttribute("polygonShader.vertexShader", "constant");
      try {
          //SimpleTexture texture = new SimpleTexture("/home/timh/daytar/art/clonegiz/cloneGiz.jpg");
          //SimpleTexture texture = new SimpleTexture("/home/timh/page.jpg");
          //InterpolatingTexture texture = new InterpolatingTexture("/home/timh/tmp/roseR.png");
          //SimpleTexture texture = new SimpleTexture("/home/timh/tmp/roseR.png");
//          Texture2D texture = new Texture2D("/usr/lib/openoffice/share/gallery/www-back/marble.jpg");
          Texture2D texture = new Texture2D("/home/timh/images/grid256rgba.png");
          //texture.setMinFilter(Texture2D.GL_NEAREST);
          texture.setMinFilter(Texture2D.GL_LINEAR);
          //SimpleTexture texture = new SimpleTexture("/usr/lib/openoffice/share/gallery/www-back/imitation_leather.jpg");
          texture.setSScale(3);
          texture.setTScale(3);
          ap.setAttribute("polygonShader.texture",texture);
          //InterpolatingTexture texture = new InterpolatingTexture("/home/timh/daytar/art/clonegiz/cloneGiz.jpg");
          //InterpolatingTexture texture = new InterpolatingTexture("/home/timh/page.jpg");
          //ap.setAttribute("polygonShader.texture",texture);
      } catch (MalformedURLException e) {
        
        e.printStackTrace();
    }
      ap.setAttribute("polygonShader.transparency", 0.0);
      ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(.4f, .4f, .4f));
      //ap.setAttribute("color", new Color(.4f, .4f, .4f));
      ap.setAttribute("phongSize", 3.);
      ap.setAttribute("phong", 12.);
      //ap.setAttribute("outline", true);
      ap.setAttribute("showLines", false);
      ap.setAttribute("showPoints", false);
      
      ap.setAttribute("outline", false);
      ap.setAttribute("lineShader.polygonShader", "flat");
      ap.setAttribute("lineShader.lineWidth", 0.02);
      ap.setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR,new Color(0.2f, 0.f, 0.f) );
      ap.setAttribute("pointShader.vertexShader", "constant");
      ap.setAttribute("pointShader.pointRadius", 0.1);
      ap.setAttribute("pointShader.outlineFraction", 0.);
      
      
      root.addChild(globeNode3);
      
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
    firstCamera.setFieldOfView(60);
    firstCamera.setFar(50);
    firstCamera.setNear(3);
    cameraNode.setCamera(firstCamera);

    SceneGraphComponent lightNode= new SceneGraphComponent();
    Transformation lt= new Transformation();
    lt.setRotation(-Math.PI / 4, 1, 1, 0);
    lightNode.setTransformation(lt);
    DirectionalLight light= new DirectionalLight();
    lightNode.setLight(light);
    cameraNode.addChild(lightNode);

    SceneGraphComponent lightNode2= new SceneGraphComponent();
    Transformation lt2= new Transformation();
    //   lt2.assignScale(-1);
    lt.setRotation(-Math.PI / 4, 1, 1, 0);
    lightNode2.setTransformation(lt2);
    DirectionalLight light2= new DirectionalLight();
    lightNode2.setLight(light2);
    cameraNode.addChild(lightNode2);


    Appearance ap= new Appearance();
    ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 0f, 0f));
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
    //   psc.addGeometry(ps);

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
    if(!SHOW_CATENOIDS) return;
    try
    {
      Class listenerClass=Class.forName("java.awt.event.MouseWheelListener");
      Class eventClass=Class.forName("java.awt.event.MouseWheelEvent");
      final Class[] listenerClassArray={listenerClass};
      java.lang.reflect.Method install=v.getClass()
        .getMethod("addMouseWheelListener", listenerClassArray);
      final java.lang.reflect.Method getValue=eventClass
        .getMethod("getWheelRotation", null);
      install.invoke(v, new Object[]{java.lang.reflect.Proxy.newProxyInstance(
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
      v.render();
    }
  }

  
  public void keyPressed(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {
  }

  public void keyTyped(KeyEvent e) {

  }


  
}
