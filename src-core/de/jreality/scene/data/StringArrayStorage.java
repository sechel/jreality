
package de.jreality.scene.data;

import java.io.ObjectStreamException;

/**
 * Storage model using a int array.
 */
final class StringArrayStorage extends StorageModel {

  StringArrayStorage() {
    super("String[]");
  }

  public Object getAsObject(Object data, int index) {
    String[] a= (String[])data;
    return a == null ? null : a[index];
  }

  public int getLength(Object data) {
    return ((String[])data).length;
  }

  public void setAsObject(Object data, int index, Object value) {
    final String[] a= (String[])data;
    a[index]= value.toString();
  }

  public Object create(int size) {
    return new String[size];
  }

  public int getNumberOfDimensions() {
    return 1;
  }

  public int[] getDimensions(int[] dim, int d) {
    return dim;
  }

  public boolean isArray() {
    return true;
  }


  public StorageModel getComponentModel() {
    return StorageModel.ObjectType.STRING;
  }

  public void toStringImpl(Object data, int index, StringBuffer target) {
    final String[] a= (String[])data;
    target.append(a[index]);
  }

  public DataList createReadOnly(final Object data, int off, int len) {
    return new StringArray((String[])data, off, len);
  }

  void copy(Object from, int srcOff, StorageModel toFmt, Object to,
    int dstOff, int len) {
    final String[] source=(String[])from;
    if(toFmt==this) {
      final String[] target=(String[])to;
      for(int src=srcOff, dst=0; dst<len; src++, dst++)
        target[dst+dstOff]=source[src];
    } else super.copy(from, srcOff, toFmt, to, dstOff, len);
    //throw new UnsupportedOperationException("int[] => "+toFmt);
  }

  public DataItem item(Object data, int i) {
    final int[] iarray=(int[])data;
    return new DataItem(iarray, i) {
      public StorageModel getStorageModel() {
        return StorageModel.Primitive.INT;
      }
      public Object get(int arg0) {
        return new Integer(iarray[offset]);
      }
      public int size() {
        return 1;
      }
    };
  }
  /**
   * {@inheritdoc}
   */
  public StorageModel inlined(int numPerEntry) {
    if(numPerEntry<1) throw new IllegalArgumentException();
    if(inlined!=null&&inlined.length>numPerEntry&&inlined[numPerEntry]!=null)
      return inlined[numPerEntry];
    return new StorageModel.IAI(numPerEntry);
  }
  /**
   * {@inheritdoc}
   */
  public StorageModel array(int numPerEntry) {
    if(arrayof!=null&&arrayof.length>numPerEntry&&arrayof[numPerEntry]!=null)
      return arrayof[numPerEntry];
    return new StorageModel.IAA(numPerEntry);
  }
  /**
   * {@inheritdoc}
   */
  public StorageModel array() {
    StorageModel sm=arrayof!=null? arrayof[0]: null;
    return sm!=null? sm: new StorageModel.IAA();
  }
  Object readResolve() throws ObjectStreamException
  {
    return StorageModel.STRING_ARRAY;
  }
}
