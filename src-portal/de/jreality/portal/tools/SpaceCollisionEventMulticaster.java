package de.jreality.portal.tools;


public final class SpaceCollisionEventMulticaster implements SpaceCollisionListener
{
  private final SpaceCollisionListener a, b;
  private SpaceCollisionEventMulticaster(SpaceCollisionListener a, SpaceCollisionListener b) {
      this.a = a; this.b = b;
  }
  private SpaceCollisionListener remove(SpaceCollisionListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    SpaceCollisionListener a2 = remove(a, oldl);
    SpaceCollisionListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static SpaceCollisionListener add(SpaceCollisionListener a, SpaceCollisionListener b)
  {
    final SpaceCollisionListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new SpaceCollisionEventMulticaster(a, b);
    return result;
  }
  public static SpaceCollisionListener remove(SpaceCollisionListener l, SpaceCollisionListener oldl)
  {
    final SpaceCollisionListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof SpaceCollisionEventMulticaster)
      result=((SpaceCollisionEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }
	public void enter(SpaceCollisionEvent e) {
		a.enter(e); b.enter(e);
	}
	
	public void leave(SpaceCollisionEvent e) {
		a.leave(e); b.leave(e);
	}

}
