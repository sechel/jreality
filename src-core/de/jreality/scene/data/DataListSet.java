
package de.jreality.scene.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * @author Holger
 */
public class DataListSet implements Serializable {
  private int dataSize;
  protected Map attr2attrDataList=new HashMap();
  protected transient Set keySet=Collections.unmodifiableSet(attr2attrDataList.keySet());
  /**
   * 
   */
  public DataListSet(int numDataItems) {
    if(numDataItems<0)
      throw new IllegalArgumentException("numDataItems "+numDataItems);
    dataSize=numDataItems;
  }

  /**
   * Clears the map and set the list length.
   */
  public void reset(int numDataItems) {
    if(numDataItems<0)
      throw new IllegalArgumentException("numDataItems "+numDataItems);
    attr2attrDataList.clear();
    dataSize=numDataItems;
  }

  public void remove(Attribute a)	{
   	attr2attrDataList.remove(a);
  }
  
  public DataList add(Attribute a, Object v) {
    DataList dList=a.getDefaultStorageModel()
                    .createWritableDataList(v).readOnlyList();
    attr2attrDataList.put(a, dList);
    return dList;
  }
  public WritableDataList addWritable(Attribute a) {
    return addWritable(a, a.getDefaultStorageModel());
  }
  public WritableDataList addWritable(Attribute a, StorageModel sm) {
    return addWritable(a, sm, sm.create(dataSize));
  }

  public WritableDataList addWritable(
    Attribute a, StorageModel sm, Object data) {
    WritableDataList wList= sm.createWritableDataList(data);
    attr2attrDataList.put(a, wList);
    return wList;
  }
  public void addReadOnly(Attribute a, StorageModel sm, Object data) {
    if (sm.getLength(data) != dataSize)
      throw new IllegalArgumentException("incompatible sizes");
    attr2attrDataList.put(a, sm.createReadOnly(data));
  }
  public boolean containsAttribute(Attribute attr ) {
	  return attr2attrDataList.containsKey(attr);
  }
  public DataList getList(Attribute attr) {
    DataList list= (DataList)attr2attrDataList.get(attr);
    return list!=null? list.readOnlyList(): null;
  }
  public WritableDataList getWritableList(Attribute attr) {
    try {
      return (WritableDataList)attr2attrDataList.get(attr);
    } catch (ClassCastException ex) {
      throw new IllegalStateException("readOnly");
    }
  }
  public DataItem get(Attribute attr, int index) {
    return getList(attr).item(index);
  }
  public Object set(Attribute attr, int index, Object value) {
    return getWritableList(attr).set(index, value);
  }
  public String toString() {
    return attr2attrDataList.toString();
  }

  public int getListLength() {
    return dataSize;
  }

  public int getNumAttributes() {
    return attr2attrDataList.size();
  }

  public Set storedAttributes() {
    return keySet;
  }

  public DataListSet readOnly()
  {
    return new RO(this);
  }

  static final class RO extends DataListSet
  {
    final DataListSet source;
    RO(DataListSet src)
    {
      super(src.getListLength());
      source=src;
      super.attr2attrDataList=Collections.unmodifiableMap(src.attr2attrDataList);
      super.keySet=src.keySet;
    }
    public int getListLength()
    {
      return source.getListLength();
    }
    public WritableDataList getWritableList(Attribute attr) {
      throw new IllegalStateException("readOnly");
    }
    public WritableDataList
    addWritable(Attribute a, StorageModel sm, Object data) {
      throw new IllegalStateException("readOnly");
    }
    public DataListSet readOnly()
    {
      return this;
    }
  }
  private void readObject(ObjectInputStream is)
    throws IOException, ClassNotFoundException {
      is.defaultReadObject();
      keySet=Collections.unmodifiableSet(attr2attrDataList.keySet());
  }
}
