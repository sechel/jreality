package de.jreality.scene.tool;


public final class PointDragEventMulticaster implements PointDragListener
{
  private final PointDragListener a, b;
  private PointDragEventMulticaster(PointDragListener a, PointDragListener b) {
      this.a = a; this.b = b;
  }
  private PointDragListener remove(PointDragListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    PointDragListener a2 = remove(a, oldl);
    PointDragListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static PointDragListener add(PointDragListener a, PointDragListener b)
  {
    final PointDragListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new PointDragEventMulticaster(a, b);
    return result;
  }
  public static PointDragListener remove(PointDragListener l, PointDragListener oldl)
  {
    final PointDragListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof PointDragEventMulticaster)
      result=((PointDragEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }

	public void pointDragStart(PointDragEvent e) {
		a.pointDragStart(e); b.pointDragStart(e);
	}

	public void pointDragged(PointDragEvent e) {
		a.pointDragged(e); b.pointDragged(e);
	}

	public void pointDragEnd(PointDragEvent e) {
		a.pointDragEnd(e); b.pointDragEnd(e);
	}

}
