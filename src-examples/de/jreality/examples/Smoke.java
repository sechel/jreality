package de.jreality.examples;

import java.awt.Color;

import de.jreality.jogl.GpgpuUtility;
import de.jreality.jogl.GpgpuViewer;
import de.jreality.jogl.SmokeCalculation;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.ViewerSwitch;

public class Smoke extends SmokeCalculation {
  
  protected void calculationFinished() {
    super.calculationFinished();
    triggerCalculation();
  }
  
  public static void main(String[] args) {
    int sl = 3;
//    float[] f = GpgpuUtility.makeGradient(sl);
    float[] f = GpgpuUtility.makeSphere(sl*sl, null, 0, 1);
    int n=13;
    float[] data = new float[1+n*4*2];
    data[0]=0.1f;
    for (int i = 0; i<n; i++) {
      data[1+4*i]=(float) Math.sin(i*2*Math.PI/n);
      data[1+8*i]=(float) Math.sin(i*2*Math.PI/n);
      data[1+4*i+1]=(float) Math.cos(i*2*Math.PI/n);
      data[1+8*i+1]=(float) Math.cos(i*2*Math.PI/n);
      data[1+8*i+3]=data[1+4*i+3]=i==(n-1)?0f:1f;
    }

    if (false) {
      
      Smoke ev = new Smoke();
      ev.setDisplayTexture(true);
      ev.setReadData(true);
      ev.setValues(f);
      ev.setData(data);
      ev.triggerCalculation();
      ev.setA(0.1);
      GpgpuUtility.run(ev);
    } else {
      Appearance self = new Appearance();
      self.setAttribute("spheresDraw", false);
      self.setAttribute("specularExponent", 10.);
      self.setAttribute("lineShader", "particle");
      self.setAttribute("folder", "parts3");
      self.setAttribute("fileName", "particles");
      self.setAttribute("particles", f);
      self.setAttribute("size", 5.);
      self.setAttribute("sprites", true);
      self.setAttribute("pointSize", 10); //self.setAttribute("forthOrder", true);
      self.setAttribute("renderCheap", true);
      self.setAttribute("diffuseColor", new Color(255, 255, 255));
      self.setAttribute("write", false);
      self.setAttribute("a", 0.01);
  
      self.setAttribute("rungeKuttaData", data);
  
      try {
  //    ImageData img = TextureUtility.createPointSprite(64, new double[]{1,-1,1}, java.awt.Color.blue, java.awt.Color.white, 10);
      ImageData img = ImageData.load(Input.getInput("/net/MathVis/data/testData3D/textures/bubble.png"));
      Texture2D tex=(Texture2D)AttributeEntityUtility.createAttributeEntity(Texture2D.class, "pointSprite", self, true);
      tex.setImage(img);
  
      } catch (Exception e) { e.printStackTrace(); }
  
      SceneGraphComponent sgc = new SceneGraphComponent();
      sgc.setAppearance(self);
      sgc.setGeometry(new IndexedLineSet());
  
      
      String viewer=System.setProperty("de.jreality.scene.Viewer", "de.jreality.jogl.GpgpuViewer"); // de.jreality.portal.DesktopPortalViewer");
      ToolSystemViewer v = (ToolSystemViewer) ViewerApp.display(sgc)[1];
      ViewerSwitch vs = (ViewerSwitch) v.getDelegatedViewer();
      GpgpuViewer gv = (GpgpuViewer) vs.getCurrentViewer();
    }
  }

}
