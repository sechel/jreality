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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.jreality.scene.Appearance;


/**
 *
 * This class handles a whole set of Attributes, that belong together somehow.
 * A typical application is i.e. a Texture object, that consists of many attributes,
 * that should be set and get from an appearance as one entity.
 * 
 * Define the entity by a an interface that consists of set/get method pairs - 
 * all these attributes will be handled by the entity.
 *
 * @author weissman
 *
 */
public class AttributeEntityFactory implements InvocationHandler {
  
  private final transient Object proxy;
  private final HashMap values = new HashMap();
  private final HashMap valueTypes = new HashMap();
  private final HashMap namesForAppearance = new HashMap();
  private final Class declaringInterface;
  
  private final StringBuffer sb = new StringBuffer();
  
  private static final Method register;
  private static final Method unregister;
  private static final Method hashCode;
  private static final Method equals;
  private static final Method toString;
  
  static {
    try {
      register = AttributeEntity.class.getDeclaredMethod("registerAppearance", new Class[]{Appearance.class, String.class});
      unregister = AttributeEntity.class.getDeclaredMethod("unregisterAppearance", new Class[]{Appearance.class});
      hashCode = Object.class.getDeclaredMethod("hashCode", null);
      equals = Object.class.getDeclaredMethod("equals", new Class[]{Object.class});
      toString = Object.class.getDeclaredMethod("toString", null);
    } catch (Exception e) {
      throw new ExceptionInInitializerError();
    }
  }
  private final LinkedList appearances = new LinkedList();
  
  public static Object createEntity(Class clazz) {
      return new AttributeEntityFactory(clazz).getProxy();
  }
  
  private AttributeEntityFactory(Class declaringInterface) {
    proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{declaringInterface}, this);
    this.declaringInterface = declaringInterface;
  }
  
  public Object getProxy() {
    return proxy;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().startsWith("get")) {
      sb.append(getAttributeName(method.getName()));
      Object ret = values.get(sb.toString());
      sb.delete(0, sb.length());
      return ret;
    }
    if (method.getName().startsWith("set")) {
      String attr = sb.append(getAttributeName(method.getName())).toString();
      updateValue(attr, args[0], method.getParameterTypes()[0]);
      sb.delete(0, sb.length());
      return null;
    }
    if (method.getReturnType().equals(declaringInterface)) { // namespace
      sb.append(method.getName()).append('.');
      return proxy;
    }
    if (method.equals(register)) {
      registerAppearance((Appearance) args[0], (String)args[1]);
      return null;
    }
    if (method.equals(unregister)) {
      unregisterAppearance((Appearance) args[0]);
      return null;
    }
    if (method.equals(hashCode)) {
      return new Integer(hashCode());
    }
    if (method.equals(equals)) {
      return Boolean.valueOf(args[0]==proxy);
    }
    if (method.equals(toString)) {
      return new String("AttributeEntity: "+declaringInterface.getName());
    }
    throw new IllegalStateException("only set/get-Methods allowed ["+method+"]");
  }
  
  private void updateValue(String attr, Object value, Class type) {
    values.put(attr, value);
    valueTypes.put(attr, type);
    for (Iterator i = appearances.iterator(); i.hasNext(); ) {
      Appearance app = (Appearance)i.next();
      String attrName=(String) namesForAppearance.get(app);
      app.setAttribute(getPrefix(app)+attr, value, type);
    }
  }

  private String getAttributeName(String methodName) {
    StringBuffer sb = new StringBuffer();
    sb.append(Character.toLowerCase(methodName.charAt(3)));
    sb.append(methodName.substring(4));
    return sb.toString();
//    return methodName.substring(3).replaceAll("([a-z,0-9]+)([A-Z]{1})", "$1_$2").toUpperCase();
  }
  
  private void registerAppearance(Appearance a, String attrName) {
    namesForAppearance.put(a, attrName);
    for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
      String name = getPrefix(a)+(String)i.next();
      a.setAttribute(name, values.get(name), (Class) valueTypes.get(name));
    }
    appearances.add(a);
  }
  
  private String getPrefix(Appearance a) {
    String pre = (String) namesForAppearance.get(a);
    StringBuffer sb = new StringBuffer();
    if (!pre.equals("")) sb.append(pre).append('.');
    return sb.toString();
  }

  private void unregisterAppearance(Appearance a) {
    String attrName = (String) namesForAppearance.get(a);
    for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
      String name = getPrefix(a)+(String)i.next();
      a.setAttribute(name, Appearance.INHERITED);
    }
    appearances.remove(a);
  }
}
