package de.jreality.portal.tools;


public final class SpaceDragEventMulticaster implements SpaceDragListener
{
  private final SpaceDragListener a, b;
  private SpaceDragEventMulticaster(SpaceDragListener a, SpaceDragListener b) {
      this.a = a; this.b = b;
  }
  private SpaceDragListener remove(SpaceDragListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    SpaceDragListener a2 = remove(a, oldl);
    SpaceDragListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static SpaceDragListener add(SpaceDragListener a, SpaceDragListener b)
  {
    final SpaceDragListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new SpaceDragEventMulticaster(a, b);
    return result;
  }
  public static SpaceDragListener remove(SpaceDragListener l, SpaceDragListener oldl)
  {
    final SpaceDragListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof SpaceDragEventMulticaster)
      result=((SpaceDragEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }

	public void dragStart(SpaceDragEvent e) {
		a.dragStart(e); b.dragStart(e);
	}

	public void drag(SpaceDragEvent e) {
		a.drag(e); b.drag(e);
	}

	public void dragEnd(SpaceDragEvent e) {
		a.dragEnd(e); b.dragEnd(e);
	}

}
