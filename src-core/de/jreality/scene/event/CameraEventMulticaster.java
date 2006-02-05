
package de.jreality.scene.event;


public final class CameraEventMulticaster implements CameraListener
{
  private final CameraListener a, b;
  private CameraEventMulticaster(CameraListener a, CameraListener b) {
      this.a = a; this.b = b;
  }
  private CameraListener remove(CameraListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    CameraListener a2 = remove(a, oldl);
    CameraListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static CameraListener add(CameraListener a, CameraListener b)
  {
    final CameraListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new CameraEventMulticaster(a, b);
    return result;
  }
  public static CameraListener remove(CameraListener l, CameraListener oldl)
  {
    final CameraListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof CameraEventMulticaster)
      result=((CameraEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }
  /**
   * Recast the event.
   * @see de.jreality.scene.event.TransformationListener#transformationChanged(TransformationEvent)
   */
  public void cameraChanged(CameraEvent ev)
  {
    a.cameraChanged(ev); b.cameraChanged(ev);
  }
}
