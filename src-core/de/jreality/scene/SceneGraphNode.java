
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
    if (new Exception().getStackTrace()[1].getMethodName() != "enqueueWriter") throw new IllegalStateException("only allowed via event");
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
  protected void setReadOnly(boolean newReadOnlyState) {
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
    if (nodeLock.canSwitch()) {
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
    } else {
      nodeLock.writeUnlock();
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
    nodeLock.readLock();
  }
  
  /**
   * this method is called after a sequence of read operations
   * are executed.
   */
  protected final void finishReader() {
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
}
