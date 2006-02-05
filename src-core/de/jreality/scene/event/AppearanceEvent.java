/*
 * Created on Apr 30, 2004
 *
 */
package de.jreality.scene.event;

import de.jreality.scene.Appearance;

/**
 * @author Charles Gunn
 *
 */
public class AppearanceEvent extends SceneEvent {
  
	String key;
  Object old;
    
  public AppearanceEvent(Appearance source, String key, Object old) {
    super(source);
    this.key = key;
    this.old=old;    
  }
    
	public String getKey()	{
		return key;
	}
    public Object getOldValue() {
      return old;
    }
}
