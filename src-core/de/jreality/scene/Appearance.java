/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.scene;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
