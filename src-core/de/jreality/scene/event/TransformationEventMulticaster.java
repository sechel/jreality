
package de.jreality.scene.event;


public final class TransformationEventMulticaster implements TransformationListener
{
  private final TransformationListener a, b;
  private TransformationEventMulticaster(TransformationListener a, TransformationListener b) {
      this.a = a; this.b = b;
  }
  private TransformationListener remove(TransformationListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    TransformationListener a2 = remove(a, oldl);
    TransformationListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static TransformationListener add(TransformationListener a, TransformationListener b)
  {
    final TransformationListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new TransformationEventMulticaster(a, b);
    return result;
  }
  public static TransformationListener remove(TransformationListener l, TransformationListener oldl)
  {
    final TransformationListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof TransformationEventMulticaster)
      result=((TransformationEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }
  /**
   * Recast the event.
   * @see de.jreality.scene.event.TransformationListener#transformationMatrixChanged(TransformationEvent)
   */
  public void transformationMatrixChanged(TransformationEvent ev)
  {
    a.transformationMatrixChanged(ev); b.transformationMatrixChanged(ev);
  }
}
