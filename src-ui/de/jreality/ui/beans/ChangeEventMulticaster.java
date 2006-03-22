/*
 * Created on Apr 30, 2004
 *
 */
package de.jreality.ui.beans;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public final class ChangeEventMulticaster implements ChangeListener
{
  private final ChangeListener a, b;
  private ChangeEventMulticaster(ChangeListener a, ChangeListener b) {
	  this.a = a; this.b = b;
  }
  private ChangeListener remove(ChangeListener oldl) {
	if(oldl == a)  return b;
	if(oldl == b)  return a;
	ChangeListener a2 = remove(a, oldl);
	ChangeListener b2 = remove(b, oldl);
	if(a2 == a && b2 == b) return this;
	return add(a2, b2);
  }
  public static ChangeListener add(ChangeListener a, ChangeListener b)
  {
	final ChangeListener result;
	if(a==null) result=b; else if(b==null) result=a;
	else result=new ChangeEventMulticaster(a, b);
	return result;
  }
  public static ChangeListener remove(ChangeListener l, ChangeListener oldl)
  {
	final ChangeListener result;
	if(l==oldl||l==null) result=null;
	else if(l instanceof ChangeEventMulticaster)
	  result=((ChangeEventMulticaster)l).remove(oldl);
	else result=l;
	return result;
  }
	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) {
	    a.stateChanged(arg0); b.stateChanged(arg0);
	}
}
