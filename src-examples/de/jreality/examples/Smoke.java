package de.jreality.examples;

import de.jreality.jogl.GpgpuUtility;
import de.jreality.jogl.SmokeCalculation;

public class Smoke extends SmokeCalculation {
  
  protected void calculationFinished() {
    triggerCalculation();
  }
  
  public static void main(String[] args) {
    Smoke ev = new Smoke();
    ev.setDisplayTexture(true);
    int sl = 16;
    float[] f = GpgpuUtility.makeGradient(sl);
    ev.setValues(f);
    int n=12;
    float[] data = new float[1+n*4*2];
    data[0]=0.001f;
    for (int i = 0; i<n; i++) {
      data[1+8*i]=data[1+4*i]=(float) Math.sin(i*2*Math.PI/n);
      data[1+8*i+1]=data[1+4*i+1]=(float) Math.cos(i*2*Math.PI/n);
      data[1+8*i+3]=data[1+4*i+3]=1f;
    }
    ev.setData(data);
    ev.triggerCalculation();
    GpgpuUtility.run(ev);
  }

}
