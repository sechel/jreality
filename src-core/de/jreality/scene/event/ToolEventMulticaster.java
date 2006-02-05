
package de.jreality.scene.event;


public final class ToolEventMulticaster implements ToolListener
{
  private final ToolListener a, b;
  private ToolEventMulticaster(ToolListener a, ToolListener b) {
      this.a = a; this.b = b;
  }
  private ToolListener remove(ToolListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    ToolListener a2 = remove(a, oldl);
    ToolListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static ToolListener add(ToolListener a, ToolListener b)
  {
    final ToolListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new ToolEventMulticaster(a, b);
    return result;
  }
  public static ToolListener remove(ToolListener l, ToolListener oldl)
  {
    final ToolListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof ToolEventMulticaster)
      result=((ToolEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }
  /**
   * @see de.jreality.scene.event.ToolListener#childAdded(ToolEvent)
   */
  public void toolAdded(ToolEvent ev)
  {
    a.toolAdded(ev); b.toolAdded(ev);
  }

  /**
   * @see de.jreality.scene.event.ToolListener#childRemoved(ToolEvent)
   */
  public void toolRemoved(ToolEvent ev)
  {
    a.toolRemoved(ev); b.toolRemoved(ev);
  }
}
