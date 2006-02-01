package de.jreality.jogl;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;

public class GpgpuViewer extends Viewer {

  AbstractCalculation calculation;
  private boolean calculationInited;
  
  public void setCalculation(AbstractCalculation calculation) {
    this.calculation = calculation;
    calculationInited=false;
  }
  
  public void init(GLDrawable arg0) {
    if (calculation != null) {
      calculationInited=true;
      calculation.init(arg0);
    }
    super.init(arg0);
  }
  
  public void display(GLDrawable arg0) {
    if (calculation != null) {
      if (!calculationInited) {
        calculationInited=true;
        calculation.init(arg0);
      }
      arg0.getGL().glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
      calculation.display(arg0);
      arg0.getGL().glPopAttrib();
    }
    arg0.getGL().glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
    super.display(arg0);
    arg0.getGL().glPopAttrib();
  }
  
  public void reshape(GLDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
    if (calculation != null) calculation.reshape(arg0, arg1, arg2, arg3, arg4);
    super.reshape(arg0, arg1, arg2, arg3, arg4);
  }
  
  public AbstractCalculation getCalculation() {
    return calculation;
  }

}
