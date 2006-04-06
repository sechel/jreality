
package de.jreality.shader;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;

/**
 * Manages effective attributes.
 */
public class EffectiveAppearance {
  private EffectiveAppearance parentApp;
  private Appearance app;

  private EffectiveAppearance(EffectiveAppearance parent, Appearance app)
  {
    parentApp=parent;
    this.app=app;
  }
  public static EffectiveAppearance create()
  {
    return new EffectiveAppearance(null, new Appearance());
  }
  public static EffectiveAppearance create(SceneGraphPath p) {
    EffectiveAppearance eap = create();
    for (Iterator i = p.iterator(); i.hasNext(); ) {
      SceneGraphNode n = (SceneGraphNode) i.next();
      if (n instanceof SceneGraphComponent) {
        SceneGraphComponent sgc = (SceneGraphComponent) n;
        Appearance app = sgc.getAppearance();
        if (app != null) eap = eap.create(app);
      }
    }
    return eap;
  }
  
  public EffectiveAppearance create(Appearance app)
  {
    return new EffectiveAppearance(this, app);
  }
  
  public Object getAttribute(String key, Object defaultValue)
  {
    return getAttribute(key, defaultValue, defaultValue.getClass());
  }

  public Object getAttribute(String key, Object defaultValue, Class class1)
  {
    int lastDot=key.lastIndexOf('.');
    String lastKeyPart=key.substring(lastDot+1);
    for(int dot=lastDot; dot!=-1; dot=key.lastIndexOf('.', dot-1))
    {
      String localKey=key.substring(0, dot+1)+lastKeyPart;
      Object value = getAttribute1(localKey, defaultValue, class1);
      if(value!=Appearance.INHERITED) return value;
    }
    Object value = getAttribute1(lastKeyPart, defaultValue, class1);
    if(value==Appearance.INHERITED) value=defaultValue;
    return value;
  }
  private Object getAttribute1(String key, Object defaultValue, Class class1)
  {
    Object value = app.getAttribute(key, class1);
    if(value==Appearance.DEFAULT) return defaultValue;
    if(value!=Appearance.INHERITED) return value; // null not allowed
    return parentApp==null? Appearance.INHERITED:
      parentApp.getAttribute1(key, defaultValue, class1);
  }
  public double getAttribute(String key, double value)
  {
    return ((Double)getAttribute(key, new Double(value))).doubleValue();
  }
  public float getAttribute(String key, float value)
  {
    return ((Float)getAttribute(key, new Float(value))).floatValue();
  }
  public int getAttribute(String key, int value)
  {
    return ((Integer)getAttribute(key, new Integer(value))).intValue();
  }
  public long getAttribute(String key, long value)
  {
    return ((Long)getAttribute(key, new Long(value))).longValue();
  }
  public boolean getAttribute(String key, boolean value)
  {
    return ((Boolean)getAttribute(key, Boolean.valueOf(value))).booleanValue();
  }
  public char getAttribute(String key, char value)
  {
    return ((Character)getAttribute(key, new Character(value))).charValue();
  }
  
  public static boolean matches(final EffectiveAppearance eap, final SceneGraphPath p) {
    EffectiveAppearance ea=eap;
    for (Iterator li = p.reverseIterator(); li.hasNext(); ) {
      SceneGraphNode n = (SceneGraphNode) li.next();
      if (n instanceof SceneGraphComponent) {
        SceneGraphComponent sgc = (SceneGraphComponent) n;
        Appearance app = sgc.getAppearance();
        if (app != null) {
          if (ea.app != app) return false;
          ea = ea.parentApp; 
        }
      }
    }
    assert(ea != null); // must not happen since .create() 
                                  // creates a new Appearance that
                                  // nobody else can see
    if (ea.parentApp != null) {
      // eap has a non trivial prefix before path
      return false;
    }
    return true;
  }
  
  public List getAppearanceHierarchy()	{
	  Vector v = new Vector();
	  EffectiveAppearance pa = this;
	  if (pa.app != null) v.add(pa.app);
	  while ( (pa = pa.parentApp) != null)	{
		  if (pa.app != null) v.add(pa.app);
	  }
	  return v;
  }
}
