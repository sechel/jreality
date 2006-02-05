
package de.jreality.scene;

import java.util.*;

import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceEventMulticaster;
import de.jreality.scene.event.AppearanceListener;

/**
 * The appearance node. Contains more specific attribute nodes of
 * type {@link AppearanceAttribute}.
 * @see AppearanceAttribute
 * 
 * TODO: remove AppearanceAttribute
 */
public class Appearance extends SceneGraphNode
{
  public static final Object DEFAULT = new Object() {
    public String toString() { return "default"; }
  };
  public static final Object INHERITED = new Object() {
    public String toString() { return "inherited"; }
  };
  private transient AppearanceListener appearanceListener;
  HashMap attributes=new HashMap();
  Set storedAttributes = Collections.unmodifiableSet(attributes.keySet());
  
  public Appearance()
  {
    super();
  }

  public Object getAttribute(String key)
  {
    Object aa=attributes.get(key);
    return aa!=null? aa : INHERITED;
  }

  public Object getAttribute(String key, Class type)
  {
    Object val=getAttribute(key);
    if(val==DEFAULT||type.isInstance(val)) return val;
    return INHERITED;
  }

  public void setAttribute(String key, Object value)
  {
    setAttribute(key, value, value.getClass());
  }

  public void setAttribute(String key, Object value, Class declaredType)
  {
    checkReadOnly();
    Object old=null;
    
    if(declaredType==null||value==null) throw new NullPointerException();
    if(value==INHERITED)
    {
      old=attributes.remove(key);
    }
    else
    {
      // TODO: is this check ok? (cheap enough?)
      if (AttributeEntity.class.isAssignableFrom(value.getClass()))
        throw new IllegalArgumentException("no proxies allowed");
      old=attributes.put(key, value);
    }
    fireAppearanceChanged(key, old);
  }
  public void setAttribute(String key, double value)
  {
    setAttribute(key, new Double(value));
  }
  public void setAttribute(String key, float value)
  {
    setAttribute(key, new Float(value));
  }
  public void setAttribute(String key, int value)
  {
    setAttribute(key, new Integer(value));
  }
  public void setAttribute(String key, long value)
  {
    setAttribute(key, new Long(value));
  }
  public void setAttribute(String key, boolean value)
  {
    setAttribute(key, Boolean.valueOf(value));
  }
  public void setAttribute(String key, char value)
  {
    setAttribute(key, new Character(value));
  }
//  public void addAppearanceAttribute(AppearanceAttribute aa)
//  {
//    checkReadOnly();
//    String name=aa.getAttributeName();
//    if(attributes.containsKey(name))
//      throw new IllegalStateException(name+" already defined");
//    attributes.put(name, aa);
//  }

  public Object getAppearanceAttribute(String name) {
    return attributes.get(name);
  }

   // add event handling for Appearance events
   public void addAppearanceListener(AppearanceListener listener) {
	   appearanceListener=
		 AppearanceEventMulticaster.add(appearanceListener, listener);
   }
   public void removeAppearanceListener(AppearanceListener listener) {
	   appearanceListener=
		 AppearanceEventMulticaster.remove(appearanceListener, listener);
   }

   /**
	* Tell the outside world that this appearance has changed.
	* This methods takes no parameters and is equivalent
	* to "everything has/might have changed".
	*/
   protected void fireAppearanceChanged(String key, Object old) {
	 final AppearanceListener l=appearanceListener;
	 if(l != null) l.appearanceChanged(new AppearanceEvent(this, key, old));
   }

   public Set getStoredAttributes() {
     return storedAttributes;
   }
   
  public void accept(SceneGraphVisitor v) {
    v.visit(this);
  }
  static void superAccept(Appearance a, SceneGraphVisitor v) {
    a.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v) {
    super.accept(v);
  }

}
