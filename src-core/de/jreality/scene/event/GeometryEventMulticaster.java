
package de.jreality.scene.event;

/**
 * Support class for geometry multicast events.
 * @see de.jreality.scene.Geometry
 * @author pietsch
 */
public final class GeometryEventMulticaster implements GeometryListener
{
  private final GeometryListener a, b;
  private GeometryEventMulticaster(GeometryListener a, GeometryListener b) {
      this.a = a; this.b = b;
  }
  private GeometryListener remove(GeometryListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    GeometryListener a2 = remove(a, oldl);
    GeometryListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static GeometryListener add(GeometryListener a, GeometryListener b)
  {
    final GeometryListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new GeometryEventMulticaster(a, b);
    return result;
  }
  public static GeometryListener remove(GeometryListener l, GeometryListener oldl)
  {
    final GeometryListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof GeometryEventMulticaster)
      result=((GeometryEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }
  /**
   * Recast the event.
   * @see de.jreality.scene.event.GeometryListener#geometryChanged(GeometryEvent)
   */
  public void geometryChanged(GeometryEvent ev)
  {
    a.geometryChanged(ev); b.geometryChanged(ev);
  }
}
