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
public class AttributeEntity implements InvocationHandler {
  
  private final transient Object proxy;
  private final HashMap values = new HashMap();
  private final HashMap valueTypes = new HashMap();
  private final Class declaringInterface;
  private final String attributeName;
  
  private final LinkedList appearances = new LinkedList();
  
  public AttributeEntity(Class declaringInterface, String attributeName) {
    proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{declaringInterface}, this);
    this.declaringInterface = declaringInterface;
    this.attributeName = attributeName;
  }
  
  public Object getProxy() {
    return proxy;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().startsWith("get")) {
      return values.get(getAttributeName(method.getName()));
    }
    // set the attribute to Attribute
    if (method.getName().startsWith("set")) {
      String attr = getAttributeName(method.getName());
      updateValue(attr, args[0], method.getParameterTypes()[0]);
      return null;
    }
    throw new IllegalStateException("only set/get-Methods allowed");
  }
  
  private void updateValue(String attr, Object value, Class type) {
    values.put(attr, value);
    valueTypes.put(attr, type);
    for (Iterator i = appearances.iterator(); i.hasNext(); ) {
      Appearance app = (Appearance)i.next();
      app.setAttribute(attr, value, type);
    }
  }

  private String getAttributeName(String methodName) {
    return attributeName+"_"+methodName.substring(3).replaceAll("([a-z,0-9]+)([A-Z]{1})", "$1_$2").toUpperCase();
  }
  
  public void registerAppearance(Appearance a) {
    for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
      String name = (String)i.next();
      a.setAttribute(name, values.get(name), (Class) valueTypes.get(name));
    }
    appearances.add(a);
  }

  public void unRegisterAppearance(Appearance a) {
    for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
      a.setAttribute((String)i.next(), Appearance.INHERITED);
    }
    appearances.remove(a);
  }
}
