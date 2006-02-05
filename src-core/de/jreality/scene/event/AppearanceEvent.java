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
	String key;		// tell listeners what the attribute being set was/is
  Object old;
  Class oldType;
    
  public AppearanceEvent(Appearance source, String key, Object old) {
    this(source, key, old, old == null ? null : old.getClass());    
  }
    
	/**
	 * @param source
	 */
	private AppearanceEvent(Appearance source, String key, Object old, Class oldType) {
		super(source);
		this.key = key;
		this.old=old;
    this.oldType=oldType;
	}
	public String getKey()	{
		return key;
	}
    public Object getOldValue() {
      return old;
    }
    public Class getOldAttributeType() {
      return oldType;
    }
}
