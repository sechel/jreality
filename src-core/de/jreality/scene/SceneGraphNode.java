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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import de.jreality.scene.event.SceneEvent;
import de.jreality.util.LoggingSystem;

/**
 * Base class for scene graph member nodes.
 * Common features of all scene graph nodes are an optional name
 * and a read-only flag.
 */
public class SceneGraphNode {
  private static int UNNAMED_ID;
  /** PENDING: <b>work in progress</b>, the lock for this component,
   * subclasses should always use try{}finally{} statements when
   * executing guarded code.
   **/
  private final transient Lock nodeLock= new Lock();
  
  private boolean readOnly;
  private String  name;
  private transient List writers=Collections.EMPTY_LIST, writersSwap=Collections.EMPTY_LIST;
  
  /**
   * use this ONLY from inside a listener via event.enqueueWriter( runner );
   *
   * @param event
   * @param runnable
   */
  public void enqueueWriter(SceneEvent event, Runnable runnable) {
    if (Thread.currentThread() != nodeLock.lastWriter) throw new IllegalStateException("only allowed via event");
    if (writers == Collections.EMPTY_LIST) writers = new LinkedList();
    writers.add(runnable);
  }
  
  /**
   * Returns the readOnly flag
   * @return boolean
   */
  public boolean isReadOnly() {
    startReader();
    try {
      return readOnly;
    } finally {
      finishReader();
    }
  }

  /**
   * Sets the readOnly flag
   * @param newReadOnlyState the desired readOnly flag value
   */
  public void setReadOnly(boolean newReadOnlyState) {
    startWriter();
    readOnly=newReadOnlyState;
    finishWriter();
  }

  protected final void checkReadOnly() {
    if(readOnly) throw new IllegalStateException("readOnly");
  }

  public String getName() {
    nodeLock.readLock();
    try {
      return name!=null? name: (name="Unnamed "+(UNNAMED_ID++));
    } finally {
      nodeLock.readUnlock();
    }
  }

  public void setName(String string) {
    checkReadOnly();
    nodeLock.writeLock();
    name= string;
    nodeLock.writeUnlock();
  }

  /**
   * this method is called berfore a sequence of write operations
   * are executed. So the changed object can collect the changed information
   * and broadcast changes via events when calling @see finishWriter;
   */
  protected final void startWriter() {
    nodeLock.writeLock();
  }
  
  /**
   * this method is called after a sequence of write operations
   * are executed. in this call the corresponding events should 
   * be generated
   */
  protected final void finishWriter() {
    if (!nodeLock.canSwitch()) {
      nodeLock.writeUnlock();
      return;
    }
    nodeLock.switchToReadLock();
    try {
      writingFinished(); // broadcast events
    } finally {
      if (!writers.isEmpty()) {
        if (!nodeLock.canSwitchBack()) throw new IllegalStateException("sth wrong");
        nodeLock.switchBackToWriteLock();
        final List w=writers;
        writers=writersSwap;
        try {
          processWriters(w);            
        } finally {
          w.clear();
          writersSwap=w;
          finishWriter();
        }
      } else {
        nodeLock.readUnlock();
      }
    }
  }
  
  private void processWriters(List w) {
    for (Iterator i = w.iterator(); i.hasNext(); ) {
      try {
        ((Runnable)i.next()).run();
      } catch (Exception e) {
        LoggingSystem.getLogger(this).log(Level.SEVERE, "writer failed", e);
      }
    }
  }

  protected void writingFinished() {
  }

  /**
   * this method is called berfore a sequence of read operations
   * are executed. So the state of the node will not be changed
   * during the read operation @see finishReader;
   */
  protected final void startReader() {
	  if (!threadsafe)   return;
      nodeLock.readLock();
  }
  
  /**
   * this method is called after a sequence of read operations
   * are executed.
   */
  protected final void finishReader() {
	if (!threadsafe)   return;
    nodeLock.readUnlock();
  }
  
  /** 
   * The accept method for a SceneGraphVisitor.
   * @param a visitor {@see SceneGraphVisitor}
   */
  public void accept(SceneGraphVisitor v) {
    v.visit(this);
  }
  
  /**
   * Return a string representation of the current state. Only for debugging
   * purposes.
   */
  /* old: Emmit an XML representation, subject of further discussions.*/
  public String toString() {
    StringBuffer sb= new StringBuffer(200);
    toStringImpl(sb, new HashSet());
    return sb.toString();
  }

  private void toStringImpl(StringBuffer sb, HashSet trace) {
    trace.add(this);
    nodeLock.readLock();
    try {
      Class idClass= getClass();
      sb.append(idClass.getName()).append('[');
      for(Class cl= idClass; cl != null; cl= cl.getSuperclass()) {
        java.lang.reflect.Field[] f= cl.getDeclaredFields();
        java.lang.reflect.AccessibleObject.setAccessible(f, true);
        for(int i= 0, n= f.length; i < n; i++) try {
          final java.lang.reflect.Field field= f[i];
          if(java.lang.reflect.Modifier.isStatic(field.getModifiers()) || java.lang.reflect.Modifier.isTransient(field.getModifiers())) continue;
          final Object value=field.get(this);
          if(!trace.contains(value)) {
            sb.append(field.getName()).append(" = ");
            if(value instanceof SceneGraphNode)
              ((SceneGraphNode)value).toStringImpl(sb, trace);
            else sb.append(value);
            sb.append(", ");
          }
        } catch (IllegalAccessException e) {} // will never happen
      }
      int l= sb.length();
      if(sb.charAt(l - 1) == ' ' && sb.charAt(l - 2) == ',')
        sb.setLength(l - 2);
      sb.append(']');
      trace.remove(this);
    } finally {
      nodeLock.readUnlock();
    }
  }
  
  // currently cost of threadsafe for non-euclidean manifold demos is a factor of 2:
  // 60 fps not thread safe   vs  30 fps for thread-safe
  // We want to show these at Lange Nacht on May 13: please leave following method in place until then
  // -gunn
  private static boolean threadsafe = true;
  public static void setThreadSafe(boolean b)	{
	  threadsafe = b;
  }
  public static boolean getThreadSafe()	{
	  return threadsafe;
  }

}
