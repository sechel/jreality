
package de.jreality.scene.event;


public final class LightEventMulticaster implements LightListener
{
  private final LightListener a, b;
  private LightEventMulticaster(LightListener a, LightListener b) {
      this.a = a; this.b = b;
  }
  private LightListener remove(LightListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    LightListener a2 = remove(a, oldl);
    LightListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static LightListener add(LightListener a, LightListener b)
  {
    final LightListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new LightEventMulticaster(a, b);
    return result;
  }
  public static LightListener remove(LightListener l, LightListener oldl)
  {
    final LightListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof LightEventMulticaster)
      result=((LightEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }
  /**
   * Recast the event.
   * @see de.jreality.scene.event.TransformationListener#transformationChanged(TransformationEvent)
   */
  public void lightChanged(LightEvent ev)
  {
    a.lightChanged(ev); b.lightChanged(ev);
  }
}
