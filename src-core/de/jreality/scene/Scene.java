
package de.jreality.scene;

import java.util.*;

/**
 * Scene control.
 */
public class Scene
{
  private Scene() {}

  private static final ThreadLocal ownedReadLocks=new ThreadLocal() {
    protected Object initialValue() {
      return new HashSet();
    }
  };
  private static final Comparator CANONICAL=new Comparator() {
    public int compare(Object o1, Object o2) {
      return System.identityHashCode(o1)-System.identityHashCode(o2);
    }
  };
  /**
   * Perform a piece of code to query a {@link SceneGraphNode} in a read-locked
   * state of the node. The code is encapsulated in a {@link Runnable} and can
   * safely assume that different query methods on that node return valid values
   * representing the same unmodified state.
   * @see #executeReader(Collection,Runnable)
   */
  public static void executeReader(SceneGraphNode toRead, Runnable reader) {
//    executeReader(Collections.singleton(toRead), reader);
    //HOTFIX, until executeReader(Collection, Runnable) works
    toRead.startReader();
    try {
      reader.run();
    } finally {
      toRead.finishReader();
    }
  }
  /**
   * <b style="text-color:red">Not implemented yet.</b>
   */
  public static void executeReader(Collection toRead, Runnable reader) {
    if (true) throw new UnsupportedOperationException("not yet implemented");
    HashSet owned=(HashSet)ownedReadLocks.get();
    if(owned.containsAll(toRead)) {//no need for any protection
      reader.run();
      return;
    }
    TreeSet toLock=new TreeSet(toRead);
    toLock.removeAll(owned);
    //TODO: continue
  }
  /**
   * Perform a piece of code to manipulate a {@link SceneGraphNode} in a
   * write-locked state of the node. The code is encapsulated in a
   * {@link Runnable} and no other code can get a read-lock while it's
   * {@link Runnable#run() run} method is being executed. So no-one will
   * read an in-between state.
   * @see #executeWriter(Collection,Runnable)
   */
  public static void executeWriter(SceneGraphNode toWriteIn, Runnable writer) {
//    executeWriter(Collections.singleton(toWriteIn), writer);
    //HOTFIX, until executeWriter(Collection, Runnable) works
    toWriteIn.startWriter();
    try {
      writer.run();
    } finally {
      toWriteIn.finishWriter();
    }
  }
  /**
   * <b style="text-color:red">Not implemented yet.</b>
   */
  public static void executeWriter(Collection toRead, Runnable writer) {
    if (true) throw new UnsupportedOperationException("not yet implemented");
  }
}
