/*
 * Created on Apr 20, 2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.Annotation;
import java.util.*;
import java.util.logging.Level;

import de.jreality.scene.Appearance;


/**
 *
 * This class handles a whole set of Attributes, that belong together somehow.
 * A typical application is i.e. a Texture object, that consists of many attributes,
 * that should be set and get from an appearance as one entity.
 * 
 * Define the entity by a an interface that consists of set/get method pairs - 
 * all these attributes will be handled by the Reader/Writer proxy.
 *
 * Use this interface either as a Writer on an Appearance or a set of Appearances:
 * 
 * <code>
 * <b><pre>
 * Appearance app = new Appearance();
 * MyEntityInterface mif = (MyEntityInterface) AttributeEntityFactory.createWriter(MyEntityInterface.class, "myEntityName", app);
 * mif.setAttribute1(value1);
 * mif.setAttribute2(value2);
 * </pre></b>
 * </code>
 * 
 * And use it to read the values from an EffectiveAppearance:
 * <code>
 * <b><pre>
 * EffectiveAppearance ea = ...
 * MyEntityInterface mif = (MyEntityInterface) AttributeEntityFactory.createReader(MyEntityInterface.class, "myEntityName", ea);
 * value1 = mif.getAttribute1();
 * value2 = mif.getAttribute2();
 * </pre></b>
 * </code>
 * <p>
 * <b>Note: Writer can call set- and get-Methods, Reader can ONLY use get-Methods.</b>
 * <p>
 * The given prefix is used as a name prefix for the single attributes.
 * i.e. in the above example the attribute "attribute1" is stored in
 * the Appearance(s) as setAttribute("myEntityName:attribute1");
 * 
 * To read the values again one needs to use the same prefix as used while writing.
 * 
 * <p>
 * 
 * <h1>Naming conventions</h1>
 * <li> Attributes are named just like the
 * methods without the prefix set or get and with a
 * small first character. i.e. set/getTestDouble -> testDouble
 * </li>
 * <li> Default values are defined as constants in the interface.
 * the name for a default constant is defined as follows:<br>
 * every capital letter L that follows a number or a small letter
 * is replaced by _L. Then the resulting string is converted to upper case.
 * then _DEFAULT is appended.
 * <br>
 * examples:
 * <br>
 * testDouble -> TEST_DOUBLE_DEFAULT<br>
 * testURL -> TEST_URL_DEFAULT<br>
 * 23AttrTest42String -> 23_ATTR_TEST42_STRING_DEFAULT<br>
 * 
 * @author weissman
 */
public class AttributeEntityFactory {
  
  private final transient Object proxy;
  
  private static final Method hashCode;
  private static final Method equals;
  private static final Method toString;
  private static final Method writeDefaults;
  
  private static final Object NO_DEFAULT = new Object();
  
  
  static {
    try {
      hashCode = Object.class.getDeclaredMethod("hashCode", null);
      equals = Object.class.getDeclaredMethod("equals", new Class[]{Object.class});
      toString = Object.class.getDeclaredMethod("toString", null);
      writeDefaults = AttributeEntityWriter.class.getDeclaredMethod("writeDefaults", null);
    } catch (Exception e) {
      throw new ExceptionInInitializerError();
    }
  }
  private final LinkedList appearances = new LinkedList();
  
  public static Object createReader(Class clazz, String prefix, EffectiveAppearance ea) {
    return new AttributeEntityFactory(clazz, prefix, ea).getProxy();
  }

  public static Object createWriter(Class clazz, String prefix, Appearance a) {
    return new AttributeEntityFactory(clazz, prefix, Collections.singleton(a)).getProxy();
  }

  public static Object createWriter(Class clazz, String prefix, Collection apps) {
    return new AttributeEntityFactory(clazz, prefix, apps).getProxy();
  }

  private AttributeEntityFactory(Class declaringInterface, String prefix, EffectiveAppearance ea) {
    this(declaringInterface, prefix, new Reader(declaringInterface, ea, prefix));
  }

  private AttributeEntityFactory(Class declaringInterface, String prefix, Collection apps) {
    this(declaringInterface, prefix, new Writer(declaringInterface, apps, prefix));
  }
  
  private AttributeEntityFactory(Class declaringInterface, String prefix, InvocationHandler handler) {
    proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{declaringInterface}, handler);
  }
  
  public Object getProxy() {
    return proxy;
  }

  private static class Reader implements InvocationHandler {
    
    final EffectiveAppearance ea;
    final String prefix;
    final Class declaringInterface;
    final HashMap defaultValues = new HashMap();
    final HashMap defaultTypes = new HashMap();
    
    public Reader(Class declaringInterface, EffectiveAppearance ea, String prefix) {
      this.ea = ea;
      this.prefix = prefix;
      this.declaringInterface=declaringInterface;
      if (!ea.getAttribute(prefix, Object.class, Class.class).equals(declaringInterface)) {
        throw new IllegalStateException("no ["+declaringInterface.getName()+"] assigned");
      }
      readDefaultValues();
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getName().startsWith("get")) {
        String attrName = getAttributeName(method.getName());
        String key = prefix+":"+attrName;
        
        Object value = ea.getAttribute(key, defaultValues.get(attrName), (Class)defaultTypes.get(attrName));
        if (value == null && method.getReturnType().isPrimitive())
          throw new IllegalStateException("default value for primitives missing ["+attrName+"]");
        LoggingSystem.getLogger(this).log(Level.FINEST, "read {0} with value {1} as type {2}", new Object[]{key, value, method.getReturnType()});
        return value;
      }
      if (method.equals(hashCode)) {
        return new Integer(hashCode());
      }
      if (method.equals(equals)) {
        return Boolean.valueOf(args[0]==proxy);
      }
      if (method.equals(toString)) {
        return toString();
      }
      throw new IllegalStateException("only get-Methods allowed for reader instance ["+method+"]");
    }
    
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("\n\nAttributeEntity [Reader]: "+declaringInterface.getName()).append("\n");
      Method[] methods = declaringInterface.getDeclaredMethods();
      HashSet set = new HashSet();
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getName().startsWith("get"))
          set.add(methods[i].getName());
      }
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getName().startsWith("set")) {
          String getterName = "get"+methods[i].getName().substring(3);
          String attrName = getAttributeName(methods[i].getName());
          sb.append(attrName).append("\t");
          sb.append("val="+ea.getAttribute(prefix+":"+attrName, defaultValues.get(attrName), methods[i].getParameterTypes()[0])).append("\t");
          sb.append("getter: "+(set.remove(getterName) ? "Yes" : "No")).append("\n");        
        }
      }
      for (Iterator i = set.iterator(); i.hasNext(); ) {
        sb.append("get without setter: "+i.next()).append("\n");
      }
      return sb.append("\n").toString();
    }
    
    private void readDefaultValues() {
      Method[] methods = declaringInterface.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++) {
        String methodName = methods[i].getName();
        if (methodName.startsWith("get")) {
          String attrName = getAttributeName(methodName);
          Object defaultValue = getDefault(attrName, declaringInterface);
          Class ret = methods[i].getReturnType();
          if (ret.isPrimitive()) {
            if (defaultValue == null) {
              LoggingSystem.getLogger(this).log(Level.WARNING, "no default value for primitive attribute {0}."+
                  "\npublic static final {1} {2} = <defaultValue>;", new Object[]{attrName, ret.getName(), defaultFieldName(attrName)});
            }
            if (ret == Boolean.TYPE) ret = Boolean.class;
            else if (ret == Double.TYPE) ret = Double.class;
            else if (ret == Integer.TYPE) ret = Integer.class;
            else if (ret == Long.TYPE) ret = Long.class;
            else if (ret == Float.TYPE) ret = Float.class;
            else if (ret == Character.TYPE) ret = Character.class;
            else if (ret == Byte.TYPE) ret = Byte.class;
            else if (ret == Short.TYPE) ret = Short.class;
            else if (ret == Void.TYPE) ret = Void.class;
          }
          defaultValues.put(attrName, defaultValue);
          defaultTypes.put(attrName, ret);
        }
      }
    }

  }
  
  private static class Writer implements InvocationHandler {
    final Collection apps;
    final Appearance a;
    final String prefix;
    final Class declaringInterface;
    public Writer(Class declaringInterface, Collection apps, String prefix) {
     this.apps=apps;
     this.a = (Appearance) apps.iterator().next();
     this.prefix = prefix;
     this.declaringInterface=declaringInterface;
     setAttribute(prefix, declaringInterface, Class.class);
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getName().startsWith("set")) {
        String attr = getAttributeName(method.getName());
        String key = prefix+":"+attr;
        setAttribute(key, args[0], method.getParameterTypes()[0]);
        LoggingSystem.getLogger(this).log(Level.FINEST, "wrote {0} with value {1} as type {2}", new Object[]{key, args[0], method.getParameterTypes()[0]});
        return null;
      }
      if (method.getName().startsWith("get")) {
        String attrName = getAttributeName(method.getName());
        String key = prefix+":"+attrName;
        Object ret = getAttribute(key);
        if (ret == null && method.getReturnType().isPrimitive())
          throw new IllegalStateException("default value for primitive not defined ["+attrName+"]");
      }
      if (method.equals(writeDefaults)) {
        writeDefaults();
        return null;
      }
      if (method.equals(hashCode)) {
        return new Integer(hashCode());
      }
      if (method.equals(equals)) {
        return Boolean.valueOf(args[0]==proxy);
      }
      if (method.equals(toString)) {
        return toString();
      }
      throw new IllegalStateException("only set-Methods allowed for writer instance ["+method+"]");
    }
    
    private void writeDefaults() {
      Method[] methods = declaringInterface.getMethods();
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getName().startsWith("set")) {
          String attrName = getAttributeName(methods[i].getName());
          String key = prefix+":"+attrName;
          Object defaultValue = getDefault(attrName, declaringInterface);
          if (defaultValue != null)
            setAttribute(key, defaultValue, methods[i].getParameterTypes()[0]);
        }
      }
    }

    private Object getAttribute(String key) {
      return a.getAttribute(key);
    }
    
    private void setAttribute(String key, Object value, Class type) {
      for (Iterator i = apps.iterator(); i.hasNext(); )
        ((Appearance)i.next()).setAttribute(key, value, type);
    }
    
    public String toString() {
      Appearance a = (Appearance) apps.iterator().next();
      StringBuffer sb = new StringBuffer();
      sb.append("\n\nAttributeEntity [Writer]: "+declaringInterface.getName()).append("\n");
      Method[] methods = declaringInterface.getDeclaredMethods();
      HashSet set = new HashSet();
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getName().startsWith("get"))
          set.add(methods[i].getName());
      }
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getName().startsWith("set")) {
          String getterName = "get"+methods[i].getName().substring(3);
          String attrName = getAttributeName(methods[i].getName());
          sb.append(attrName).append("\t");
          sb.append(a.getAttribute(prefix+":"+attrName)).append("\t");
          sb.append("getter: "+(set.remove(getterName) ? "Yes" : "No")).append("\n");        
        }
      }
      for (Iterator i = set.iterator(); i.hasNext(); ) {
        sb.append("get without setter: "+i.next()).append("\n");
      }
      return sb.append("\n").toString();
    } 
  }
  
  /**
   * returns the default value for the given attribute
   * @param attrName attribute name
   * @return the default value or null
   */
  private static Object getDefault(String attrName, Class declaringInterface) {
    try {
      String defaultField = defaultFieldName(attrName);
      LoggingSystem.getLogger(AttributeEntityFactory.class).log(Level.FINEST, "try to get default field {0}", defaultField);
      return declaringInterface.getDeclaredField(defaultField).get(null);
    } catch (Exception e) {
      LoggingSystem.getLogger(AttributeEntityFactory.class).log(Level.FINER, "No default value defined [{0}]", attrName);
      return null;
    }
  }

  /**
   * converts the method name (of a set/get-Method) into 
   * the corresponding attribute name
   * i.e.: getMyCoolParameter -> myCoolParameter
   * @param methodName the name of the get/set-Method
   * @return attribute name
   */
  private static String getAttributeName(String methodName) {
    StringBuffer sb = new StringBuffer();
    sb.append(Character.toLowerCase(methodName.charAt(3)));
    sb.append(methodName.substring(4));
    return sb.toString();
  }
  
  /**
   * converts an attribute name into the field name for its default value
   * i.e.: myCoolParameter -> MY_COOL_PARAMETER_DEFAULT 
   * @param attrName the attribute name
   * @return default field name
   */
  private static String defaultFieldName(String attrName) {
    return attrName.replaceAll("([a-z,0-9]+)([A-Z]{1})", "$1_$2").toUpperCase()+"_DEFAULT";
  }
  
}
