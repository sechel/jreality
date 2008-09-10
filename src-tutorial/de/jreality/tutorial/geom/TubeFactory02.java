package de.jreality.tutorial.geom;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.SwingConstants;

import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tutorial.util.TextSlider;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class TubeFactory02 {

  static double R = 1, r = .25, tubeRadius = .04;
  private static SceneGraphComponent torussgc;
  
  public static void main(String[] args)  {
    torussgc = SceneGraphUtility.createFullSceneGraphComponent("torus knot");
    DefaultGeometryShader dgs = (DefaultGeometryShader) 
           ShaderUtility.createDefaultGeometryShader(torussgc.getAppearance(), true);
    dgs.setShowLines(false);
    dgs.setShowPoints(false);
    updateGeometry();
    ViewerApp va = ViewerApp.display(torussgc);
    Component insp = getInspector();
    va.addAccessory(insp);
    va.setFirstAccessory(insp);
    CameraUtility.encompass(va.getCurrentViewer());
  }

  private static void updateGeometry() {
    IndexedFaceSet tubedTorusKnot = tubedTorusKnot(R, r, tubeRadius); 
    torussgc.setGeometry(tubedTorusKnot);
  }

  private static IndexedFaceSet tubedTorusKnot(double R, double r, double tubeRadius) {
     IndexedLineSet torus1 = Primitives.discreteTorusKnot(R,r, 2, 9, 250);
     colorVertices(torus1, new double[] {1,0,0}, new double[] {0,1,0});
     // create a non-circular cross section for the tube
     int size = 16;
     double scale = 1;
     double[][] mysection = new double[size][3];
     for (int i = 0; i<size; ++i)  {
         double angle = (i/(size-1.0)) * Math.PI * 2;
         mysection[i][0] = scale * Math.cos(angle)  *(1.5+Math.cos(4*angle));
         mysection[i][1] = scale *  Math.sin(angle)  *(1.5+Math.cos(4*angle));
         mysection[i][2] = 0.0;
     }
     PolygonalTubeFactory ptf = new PolygonalTubeFactory(torus1, 0);
     ptf.setClosed(true);
     ptf.setVertexColorsEnabled(true);
     ptf.setRadius(tubeRadius);
     ptf.setCrossSection(mysection);
     ptf.setTwists(6);
     double[][] vcolors = torus1.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
     ptf.setVertexColors(vcolors);
     ptf.setGenerateEdges(true);
     ptf.update();
     IndexedFaceSet torus1Tubes = ptf.getTube();
     return torus1Tubes;
  }
  
  public static void colorVertices(IndexedLineSet ils, double[] color1, double[] color2)  {
    int nPts = ils.getNumPoints();
    double[][] colors = new double[nPts][3];
    double[][] vertices = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
    for (int i = 1; i<nPts-1; ++i)  {
      double[] v1 = Rn.subtract(null, vertices[i], vertices[i-1]);
      double t = 10 * Math.sqrt(Math.abs( v1[0]*v1[0] + v1[1] * v1[1]));
      t = t  - ((int) t);
      Rn.linearCombination(colors[i],t, color1, 1-t, color2);
    }
    System.arraycopy(colors[1], 0, colors[0], 0, 3);
    System.arraycopy(colors[nPts-2], 0, colors[nPts-1], 0, 3);
    ils.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
  }

  private static Component getInspector() {
    Box container = Box.createVerticalBox();
    final TextSlider.Double RSlider = new TextSlider.Double("R",  SwingConstants.HORIZONTAL, 0.0, 2, R);
    RSlider.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e)  {
        R = RSlider.getValue();
        updateGeometry();
      }
    });
    container.add(RSlider);
    final TextSlider.Double rSlider = new TextSlider.Double("r",  SwingConstants.HORIZONTAL, 0.0, 1, r);
    rSlider.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e)  {
        r = rSlider.getValue();
        updateGeometry();
      }
    });
    container.add(rSlider);
    final TextSlider.Double rtSlider = new TextSlider.Double("tube radius",  SwingConstants.HORIZONTAL, 0.0, 1, tubeRadius);
    rtSlider.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e)  {
        tubeRadius = rtSlider.getValue();
        updateGeometry();
      }
    });
    container.add(rtSlider);
    container.setName("Parameters");
    Dimension d = container.getPreferredSize();
    container.setPreferredSize(new Dimension(d.width/2,d.height));
    return container;
  }

}
