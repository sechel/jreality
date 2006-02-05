
package de.jreality.scene;

import java.util.*;

import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceEventMulticaster;
import de.jreality.scene.event.AppearanceListener;

/**
 * The appearance node. Contains attributes of arbitrary type.
 * 
 * TODO: fire ONE single event that reports all changed attributes
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
  private HashMap attributes=new HashMap();
  private Set storedAttributes = Collections.unmodifiableSet(attributes.keySet());
  
  private transient Set changedAttributes=new HashSet();
  
  public Object getAttribute(String key)
  {
    startReader();
    try {
      Object aa=attributes.get(key);
      return aa!=null? aa : INHERITED;
    } finally {
      finishReader();
    }
  }

  public Object getAttribute(String key, Class type)
  {
    startReader();
    try {
      Object val=getAttribute(key);
      if(val==DEFAULT||type.isInstance(val)) return val;
      return INHERITED;
    } finally {
      finishReader();
    }
  }

  public void setAttribute(String key, Object value)
  {
    setAttribute(key, value, value.getClass());
  }

  public void setAttribute(String key, Object value, Class declaredType)
  {
    checkReadOnly();
    startWriter();
    try {
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
    } finally {
      finishWriter();
    }
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

   public void addAppearanceListener(AppearanceListener listener) {
     startReader();
	   appearanceListener=AppearanceEventMulticaster.add(appearanceListener, listener);
     finishReader();
   }
   public void removeAppearanceListener(AppearanceListener listener) {
     startReader();
	   appearanceListener=AppearanceEventMulticaster.remove(appearanceListener, listener);
     finishReader();
   }

 /**
	* Tell the outside world that this appearance has changed.
	*/
   protected void writingFinished() {
     try {
       for (Iterator i = changedAttributes.iterator(); i.hasNext(); ) {
         appearanceListener.appearanceChanged((AppearanceEvent) i.next());
         i.remove();
       }
     } finally {
       changedAttributes.clear();
     }
   };
   
   protected void fireAppearanceChanged(String key, Object old) {
	 	 if(appearanceListener != null) changedAttributes.add(new AppearanceEvent(this, key, old));
   }

   public Set getStoredAttributes() {
     return storedAttributes;
   }
   
  public void accept(SceneGraphVisitor v) {
    startReader();
    try {
      v.visit(this);
    } finally {
      finishReader();
    }
  }
  static void superAccept(Appearance a, SceneGraphVisitor v) {
    a.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v) {
    super.accept(v);
  }

}
