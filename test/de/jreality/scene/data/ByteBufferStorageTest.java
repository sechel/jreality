
package de.jreality.scene.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import junit.framework.TestCase;

/**
 * 
 */
public class ByteBufferStorageTest extends TestCase
{
  public ByteBufferStorageTest()
  {
    super("ByteBufferStorage");
  }

  DataList src;
  WritableDataList dst, cmp;

  public void testDoubleArray()
  {
    double[] data={ 1, 2, 3, 42, 0.815, -123456.789 };
    setupTarget(data.length*8);
    check(StorageModel.DOUBLE_ARRAY, data);
  }
  public void testDoubleDoubleInlinedArray()
  {
    double[] data={ 1, 2, 3, 42, 0.815, -123456.789 };
    setupTarget(data.length*8);
    check(StorageModel.DOUBLE_ARRAY.inlined(3), data);
  }
  public void testDoubleDoubleArray()
  {
    double[][] data={ {1, 2, 3}, {42, 0.815, -123456.789} };
    setupTarget(data.length*8*3);
    check(StorageModel.DOUBLE_ARRAY.array(3), data);
  }
  public void testConversion()
  {
    double[][] data={ {1, 2, 3}, {42, 0.815, -123456.789} };
    src=new DoubleArrayArray.Array(data, 3);
    setupTarget(data.length*8*3);
    System.out.println(src);
    src.copyTo(dst);

    double[] inlined=new double[data.length*3];
    cmp=StorageModel.DOUBLE_ARRAY.inlined(3).createWritableDataList(inlined);
    dst.copyTo(cmp);
    System.out.println(cmp);
    assertEquals(src, cmp);
  }
  public void testIntArray()
  {
    int[] data={ 1, 2, 3, 42, 0xcafebabe, -123456 };
    setupTarget(data.length*4);
    check(StorageModel.INT_ARRAY, data);
  }
  public void testIntIntInlinedArray()
  {
    int[] data={ 1, 2, 3, 4, 2, 4, 6, 8, 3, 6, 9, 12 };
    setupTarget(data.length*4);
    check(StorageModel.INT_ARRAY.inlined(4), data);
  }
  public void testIntIntArray()
  {
    int[][] data={ {1, 2, 3, 4}, {2, 4, 6, 8}, {3, 6, 9, 12} };
    setupTarget(data.length*4);
    check(StorageModel.INT_ARRAY.array(4), data);
  }
  private void check(StorageModel sm, Object data)
  {
    src=sm.createReadOnly(data);
//    System.out.println(src);
    src.copyTo(dst);
//    System.out.println(dst);
    cmp=sm.createWritableDataList(sm.create(src.size()));
    dst.copyTo(cmp);
//    System.out.println(cmp);
    assertEquals(src, cmp);
  }
  private void setupTarget(int size)
  {
    ByteBuffer bb= ByteBuffer.allocateDirect(size)
      .order(ByteOrder.nativeOrder());
    dst=ByteBufferStorage.MODEL.createWritableDataList(bb);
  }
}
