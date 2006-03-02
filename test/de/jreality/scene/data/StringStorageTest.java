package de.jreality.scene.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

public class StringStorageTest extends TestCase {

  private DataList sm;
  private StringArray sa;

  protected void setUp() throws Exception {
    String[] data={ "bla1", "bla2", "bla3", "bla4", "bla5" };
    sm = StorageModel.STRING_ARRAY.createReadOnly(data);    
    sa = (StringArray) sm;
  }
  
  public void testRead()
  {
    System.out.println(sm);
    for (int i = 0; i < sa.getLength(); i++)
      System.out.println(i+"="+sa.getValueAt(i));
  }

  public void testCopyOut() {
    String[] sarray = sa.toStringArray(null);
    for (int i = 0; i < sarray.length; i++)
      System.out.println(i+"="+sarray[i]);
  }

  public void testCopyIn() {
    Object cmp = checkStream(sm);
    String[] l2 = ((StringArray)cmp).toStringArray(null);
    assertEquals(sa.getLength(), l2.length);
    for (int i = 0; i < l2.length; i++)
      assertEquals(sa.getValueAt(i), l2[i]);
  }

  private Object checkStream(Object ifs) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(ifs);
      oos.flush();
      oos.close();
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
      Object read = ois.readObject();
      ois.close();
      return read;
    } catch (Exception e) {
      throw new Error(e);
    }
  }

}
