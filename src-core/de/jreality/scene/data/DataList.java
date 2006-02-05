
package de.jreality.scene.data;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * A data list using a {@link StorageModel storage model} for the data.
 * This list is readonly. There are several methods allowing to open
 * another view to the same list using a different storage model or using
 * an explicitly declared storage model for higher performance.
 * These methods might return the same instance using a more specific declared
 * type if the storage model matches that type. They might convert the data
 * into a different but compatible storage model. And they throw a
 * {@link java.util.UnsupportedOperationException} for incompatible models.<br>
 * The subclasses with explicit storage models can be used to create
 * effectively immutable arrays. The point about these wrapped arrays is that a
 * good JIT compiler optimizes access to these to be not slower than for direct
 * array access but writing is still reserved to the creator.
 * @see java.util.List
 * @see StorageModel
 * @see #Item
 * @author Holger
 */
public class DataList extends DataItem implements Serializable {
  final StorageModel format;
  //final Object data; // inherited
  //final int offset;  // inherited
  final int length; 

  /**
   * use <code>sm.createReadOnly(list)</code>
   */
  DataList(StorageModel sm, Object list) {
    this(sm, list, 0, sm.getLength(list));
//    checkCaller();
  }
  
  /**
   * use <code>sm.createReadOnly(list, off, len)</code>
   */
  DataList(StorageModel sm, Object list, int off, int len) {
    super(list, off);
    if(sm==null) throw new NullPointerException();
    format= sm;
    length=len;
  }

//  private void checkCaller()
//  {
//      StackTraceElement[] callStack=new Exception().getStackTrace();
//      if(callStack==null) return;
//      int level=2;
//      Class dl=DataList.class, actual=getClass();
//      for(; actual!=dl; actual=actual.getSuperclass()) level++;
//      if(callStack.length<level) return;
//      StackTraceElement caller=callStack[level];
//      if(caller.getClassName().startsWith("de.jreality.scene.data."))
//        return;
//      System.err.println(caller);
//  }

  public StorageModel getStorageModel() {
    return format;
  }

  public Object get(int index) {
    return item(index);
  }

  public DataItem item(int index) {
    return format.item(data, index+offset);
  }

  public int size() {
    return length;
  }

  public IntArray toIntArray() {
    //XXX: this must respect offset & length
    return format.toIntArray(data);
  }

  public IntArrayArray toIntArrayArray() {
    //XXX: this must respect offset & length
    return format.toIntArrayArray(data);
  }

  public DoubleArray toDoubleArray() {
    //XXX: this must respect offset & length
    return format.toDoubleArray(data);
  }

  public DoubleArrayArray toDoubleArrayArray() {
    //XXX: this must respect offset & length
    return format.toDoubleArrayArray(data);
  }

  /**
   * Return a read only view to this list. This will return <code>this</code>
   * for DataList classes that do not provide any method with modifying side
   * effects. Writeable instances will create a read only view using the same
   * underlying data reference thus changes to the writable list will propagate
   * immediately to these views. The views might be cached and reused.
   * @return
   */
  public DataList readOnlyList()
  {
    return this;
  }

  public String toString() {
    StringBuffer sb= new StringBuffer().append('[');
    int num=length;
    if (num > 0)
      format.toStringImpl(data, offset, sb);
    for (int ix= 1; ix < num; ix++) {
      sb.append(", ");
      format.toStringImpl(data, ix+offset, sb);
    }
    sb.append(']');
    return sb.toString();
  }

  static final ThreadLocal pendingIO=new ThreadLocal();
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeObject(format);
    format.exportData(out, this);
  }
  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
      StorageModel sm=(StorageModel)in.readObject();
      Object data=sm.importData(in);
      WritableDataList wdl=sm.createWritableDataList(data);
      DataList dl=(this instanceof WritableDataList)? wdl: wdl.readOnlyList();
      IdentityHashMap map=(IdentityHashMap)pendingIO.get();
      if(map==null) pendingIO.set(map=new IdentityHashMap());
      map.put(this, dl);
  }
  //must be accessible by subclasses
  //Pending: should subclasses outside of this package be allowed?
  /*package-private*/final Object readResolve() throws ObjectStreamException
  {
    IdentityHashMap map=(IdentityHashMap)pendingIO.get();
    return map.remove(this);
  }
}
