package de.jreality.jogl;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;

public class NewGpgpuViewer extends Viewer {

  AbstractCalculation calculation;
  
  public NewGpgpuViewer(AbstractCalculation calc) {
    this.calculation=calc;
  }
  
  public void init(GLDrawable arg0) {
    calculation.init(arg0);
    super.init(arg0);
  }
  
  public void display(GLDrawable arg0) {
    calculation.display(arg0);
    arg0.getGL().glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
    super.display(arg0);
    arg0.getGL().glPopAttrib();
  }
  
  public AbstractCalculation getCalculation() {
    return calculation;
  }

}
