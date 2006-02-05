/*
 * Created on Apr 30, 2004
 *
 */
package de.jreality.scene.event;


public final class AppearanceEventMulticaster implements AppearanceListener
{
  private final AppearanceListener a, b;
  private AppearanceEventMulticaster(AppearanceListener a, AppearanceListener b) {
	  this.a = a; this.b = b;
  }
  private AppearanceListener remove(AppearanceListener oldl) {
	if(oldl == a)  return b;
	if(oldl == b)  return a;
	AppearanceListener a2 = remove(a, oldl);
	AppearanceListener b2 = remove(b, oldl);
	if(a2 == a && b2 == b) return this;
	return add(a2, b2);
  }
  public static AppearanceListener add(AppearanceListener a, AppearanceListener b)
  {
	final AppearanceListener result;
	if(a==null) result=b; else if(b==null) result=a;
	else result=new AppearanceEventMulticaster(a, b);
	return result;
  }
  public static AppearanceListener remove(AppearanceListener l, AppearanceListener oldl)
  {
	final AppearanceListener result;
	if(l==oldl||l==null) result=null;
	else if(l instanceof AppearanceEventMulticaster)
	  result=((AppearanceEventMulticaster)l).remove(oldl);
	else result=l;
	return result;
  }
  /**
   * Recast the event.
   * @see de.jreality.scene.event.AppearanceListener#appearanceChanged(AppearanceEvent)
   */
  public void appearanceChanged(AppearanceEvent ev)
  {
	a.appearanceChanged(ev); b.appearanceChanged(ev);
  }
}
