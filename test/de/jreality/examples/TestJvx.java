/*
 * Created on 01.12.2004
 *
 * This file is part of the de.jreality.examples package.
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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.Viewer;
import de.jreality.math.FactoredMatrix;
import de.jreality.reader.ReaderJVX;
import de.jreality.renderman.RIBVisitor;
import de.jreality.renderman.SLShader;
import de.jreality.scene.*;
import de.jreality.shader.CommonAttributes;
import de.jreality.soft.DefaultViewer;
import de.jreality.soft.MouseTool;
import de.jreality.util.RenderTrigger;
import de.jreality.util.SceneGraphUtility;

/**
 * Test for the jvx reader. 
 * Needs the name of a jvx file at the command line. 
 * @version 1.0
 * @author timh
 *
 */
public class TestJvx implements KeyListener{

    /**
     * main expects to get the name of a jvx file to read
     * @param args 
     */
    public static void main(String[] args) {
        Logger.getLogger("de.jreality").setLevel(Level.INFO);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
        TestJvx t= new TestJvx(args[0]);
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
      private boolean USE_DOUBLE_RASTERIZER = false;
    private String filename; 
    
      public TestJvx(String fn) {
          
          filename = fn;
        //
        // the scene root;
        //
        SceneGraphComponent root = new SceneGraphComponent();
        buildView(root);
        buildScene(root);
        Appearance a = root.getAppearance();
        if(a == null) {
            new Appearance();
            root.setAppearance(a);
        }
        // the background color:
        a.setAttribute(CommonAttributes.BACKGROUND_COLOR,new Color(.4f,.5f,.8f));
        //needed for jogl viewer at the moment:
        a.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,true);
        
        trigger.addSceneGraphComponent(root);
 

        
            softViewer= new DefaultViewer(USE_DOUBLE_RASTERIZER );
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
            softViewer.setCameraPath(((SceneGraphPath) SceneGraphUtility.getPathsBetween(root, firstCamera).get(0)));

        //frameSoft.add(SceneTreeViewer.getViewerComponent(root, softViewer), BorderLayout.WEST);
        frameSoft.setSize(780, 580);
        frameSoft.validate();
        frameSoft.setVisible(true);

        softViewer.render();

        
//        mouseTool.removeFromTriggerList(softViewer);
//        mouseTool.addToTriggerList(softViewer);
        }

      /**
         * @return
         */
      private SceneGraphComponent buildScene(SceneGraphComponent root) {
          
          SceneGraphComponent c=null;
          try {
            c = new ReaderJVX().read(new File(filename));
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          Appearance ap = c.getAppearance();
          if(ap == null ) {
              ap = new Appearance();
              c.setAppearance(ap);
          }
          HashMap m = new HashMap();
          HashMap vm = new HashMap();
          vm.put("trace",new float[]{1});
          m.put("visibility",vm);
          ap.setAttribute("rendermanAttribute",m);
          root.addChild(c);
          
        return root;
      }
      
      
      private SceneGraphComponent buildRoom() {
          SceneGraphComponent room = new SceneGraphComponent();
          room.setName("room");
          double s = 5;
          double h = 2;
          SceneGraphComponent frontC = new SceneGraphComponent();
          frontC.setName("front");
          IndexedFaceSet front = Primitives.texturedSquare(new double[] {
                  -s, h+s,-s, 
                   s, h+s,-s, 
                   s, h-s,-s, 
                  -s, h-s,-s 

          });
          frontC.setGeometry(front);
          
          SceneGraphComponent leftC = new SceneGraphComponent();
          leftC.setName("left");
          IndexedFaceSet left = Primitives.texturedSquare(new double[] {
                  s, h+s,-s, 
                  s, h+s, s, 
                  s, h-s, s, 
                  s, h-s,-s 
          });
          leftC.setGeometry(left);
          
          SceneGraphComponent bottomC = new SceneGraphComponent();
          bottomC.setName("bottom");
          
          IndexedFaceSet bottom = Primitives.texturedSquare(new double[] {
                  -s, h-s, s, 
                  -s, h-s,-s, 
                   s, h-s,-s,
                   s, h-s, s, 
                  
          });
          bottomC.setGeometry(bottom);
          
          room.addChild(frontC);
          room.addChild(leftC);
          room.addChild(bottomC);
          
          Transformation t = new Transformation();
          FactoredMatrix fm = new FactoredMatrix(t);
		fm.setRotation(.5* Math.PI/2, 0, 1, 0);
		t.setMatrix(fm.getArray());
          Transformation t2 = new Transformation();
          fm = new FactoredMatrix(t2);
		fm.setRotation(.5* Math.PI/4, 1, 0, 0);
		t2.setMatrix(fm.getArray());
          t.multiplyOnLeft(t2.getMatrix());
          room.setTransformation(t);
          
          Appearance ap = new Appearance();
          ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.GRAY);
          ap.setAttribute(CommonAttributes.TRANSPARENCY, 0);
          SLShader shader = new SLShader("mirror");
          shader.addParameter("roughness", new Float(0));
          ap.setAttribute(CommonAttributes.POLYGON_SHADER+".rendermanSurface", shader);

          
          shader = new SLShader("dented");
          shader.addParameter("float Km", new Float(0.05));
          shader.addParameter("float frequency", new Float(0.5));
          ap.setAttribute(CommonAttributes.POLYGON_SHADER+".rendermanDisplacement", shader);

          HashMap m = new HashMap();
          HashMap vm = new HashMap();
          vm.put("float sphere",new float[]{.1f});
          vm.put("string coordinatesystem","shader");
          m.put("displacementbound",vm);
          ap.setAttribute("rendermanAttribute",m);
          
          RIBVisitor.shaderPath = "/home/timh/renderers/shader";
          room.setAppearance(ap);

          return room;
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
        FactoredMatrix fm = new FactoredMatrix(lt);
		fm.setRotation(-Math.PI / 4, 1, 1, 0);
		lt.setMatrix(fm.getArray());
        lightNode.setTransformation(lt);
        DirectionalLight light= new DirectionalLight();
        lightNode.setLight(light);
        cameraNode.addChild(lightNode);

        SceneGraphComponent lightNode2= new SceneGraphComponent();
        Transformation lt2= new Transformation();
        //   lt2.assignScale(-1);
        fm = new FactoredMatrix(lt);
		fm.setRotation(-Math.PI / 4, 1, 1, 0);
		lt.setMatrix(fm.getArray());
        lightNode2.setTransformation(lt2);
        DirectionalLight light2= new DirectionalLight();
        lightNode2.setLight(light2);
        cameraNode.addChild(lightNode2);


        Appearance ap= new Appearance();
        ap.setAttribute("diffuseColor", new Color(1f, 0f, 0f));
        ap.setAttribute("lightingEnabled", true);
        
        root.setAppearance(ap);


        root.addChild(cameraNode);
        
        cameraNode.addChild(buildRoom());
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

      }
      
}
