
package de.jreality.scene.event;


public final class SceneGraphComponentEventMulticaster implements SceneGraphComponentListener
{
  private final SceneGraphComponentListener a, b;
  private SceneGraphComponentEventMulticaster(SceneGraphComponentListener a, SceneGraphComponentListener b) {
      this.a = a; this.b = b;
  }
  private SceneGraphComponentListener remove(SceneGraphComponentListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    SceneGraphComponentListener a2 = remove(a, oldl);
    SceneGraphComponentListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static SceneGraphComponentListener add(SceneGraphComponentListener a, SceneGraphComponentListener b)
  {
    final SceneGraphComponentListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new SceneGraphComponentEventMulticaster(a, b);
    return result;
  }
  public static SceneGraphComponentListener remove(SceneGraphComponentListener l, SceneGraphComponentListener oldl)
  {
    final SceneGraphComponentListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof SceneGraphComponentEventMulticaster)
      result=((SceneGraphComponentEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }
  /**
   * @see de.jreality.scene.event.SceneGraphComponentListener#childAdded(SceneContainerEvent)
   */
  public void childAdded(SceneGraphComponentEvent ev)
  {
    a.childAdded(ev); b.childAdded(ev);
  }

  /**
   * @see de.jreality.scene.event.SceneGraphComponentListener#childRemoved(SceneContainerEvent)
   */
  public void childRemoved(SceneGraphComponentEvent ev)
  {
    a.childRemoved(ev); b.childRemoved(ev);
  }
  /**
   * @see de.jreality.scene.event.SceneGraphComponentListener#childReplaced(SceneContainerEvent)
   */
  public void childReplaced(SceneGraphComponentEvent ev)
  {
    a.childReplaced(ev); b.childReplaced(ev);
  }
  public void visibilityChanged(SceneGraphComponentEvent ev) {
    a.visibilityChanged(ev); b.visibilityChanged(ev);
  }
}
