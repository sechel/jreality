package de.jreality.examples.jsyzygy;

import java.awt.Color;

import de.jreality.geometry.QuadMeshShape;
import de.jreality.scene.*;

/**
 * An simple wand.
 */
public class Wand extends QuadMeshShape
{
  double bR, sR;
  double xMax, yMax;
  
  public Wand(double r1, double r2, int xMax, int yMax)
  {	
    super(xMax, yMax, false, true);
    bR=r1;
    sR=r2;
    this.xMax = 2*Math.PI;
    this.yMax = -4;
  }
    
  public void setWandPoints() {
      double[] immersion=new double[getMaxU() * getMaxV() * 3];
      System.out.println("array length:"+immersion.length);
      for (int i = 0; i < getMaxV(); i++)
          for (int j = 0; j < getMaxU(); j++) {
              immersionOf(i,j,immersion, i*getMaxU()+j);
          }
//TODO:      setPoints(new Double3VectorArray.Linear(immersion));
      calculateNormals();
  }
  
  public void immersionOf(int m, int n, double [] targetArray, int pNo)
  {
    int arrayLocation=3*pNo;
    double x = xMax/((double)getMaxV()) * ((double)m);
    double y = yMax/((double)getMaxU()) * ((double)n);
    double lambda = y/yMax;
    targetArray[arrayLocation  ] = ((1-lambda)*bR + lambda * sR) * Math.cos(x);
    targetArray[arrayLocation+1] = ((1-lambda)*bR + lambda * sR) * Math.sin(x);
    targetArray[arrayLocation+2] = y;
//    System.out.println("m="+m+" n="+n+" x="+x+" y="+y+" nop:"+pNo+" ["+ arrayLocation +","+ (arrayLocation+1) +","+ (arrayLocation+2) +"]");
//    System.out.println("["+targetArray[arrayLocation  ]+","+targetArray[arrayLocation+1]+","+y+"]");
  }

  public String toString()
  {
    return "Wand[bR="+bR+",sR="+sR+",quad("+getMaxU()+','+getMaxV()+")]";
  }
  
  public static SceneGraphComponent makeWandComponent(SceneGraphComponent wandComponent) {
  wandComponent = new SceneGraphComponent();
  Appearance ap= new Appearance();
  wandComponent.setAppearance(ap);

  ap.setAttribute("transparency", 0.);
  ap.setAttribute(CommonAttributes.POLYGON_SHADER+".diffuseColor", new Color(234,82,17));
  ap.setAttribute("showPoints", true);
  //ap.setAttribute("outline", true);
  ap.setAttribute("lineShader.polygonShader", "flat");
  ap.setAttribute("lineShader.lineWidth", 0.04);
  ap.setAttribute("lineShader.diffuseColor",new Color(0.2f, 0.2f, 0.4f) );
  
  FactoredTransformation wandTransformation = new FactoredTransformation();
  //gt.setTranslation(-1.2, -1.2, 1.2);
  //gt.setStretch(.3);
  wandComponent.setTransformation(wandTransformation);
  Wand wand = new Wand(0.05, 0.05, 2, 4);
  wand.setWandPoints();
  wandComponent.setGeometry(wand);
  return wandComponent;
  }
}
