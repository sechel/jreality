
package de.jreality.scene.data;

/**
 * @author Holger
 */
public class WritableDataList extends DataList
{
  private transient DataList readOnly;

  public WritableDataList(StorageModel sm, Object list)
  {
    super(sm, list);
  }

  public WritableDataList(StorageModel sm, Object list, int off, int len)
  {
    super(sm, list, off, len);
  }

  /**
   * Get the contained data <em>by reference</em>. The object's class depends
   * on the storage model.
   */
  public Object getData()
  {
    return data;
  }

  public DataList readOnlyList()
  {
    return readOnly==null?
      (readOnly=getStorageModel().createReadOnly(data, offset, length)):
      readOnly;
  }

  public DoubleArray toDoubleArray() {
    return readOnlyList().toDoubleArray();
  }

  public IntArray toIntArray() {
     return readOnlyList().toIntArray();
  }

  public IntArrayArray toIntArrayArray() {
    return readOnlyList().toIntArrayArray();
  }

  public DoubleArrayArray toDoubleArrayArray()
  {
    return readOnlyList().toDoubleArrayArray();
  }

}
