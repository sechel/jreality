package de.jreality.scene;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;


/**
 * @author pietsch + weissman
 */
public final class Lock
{
  private final Object mutex=new Object();
  private Thread writer;
  Thread lastWriter;
  private int writeNestCount;
  private int lastWriterReadNestCount;
  private int readNestCount;

  public void readLock() {
    if(Thread.currentThread()==writer) {
      readNestCount++;
      //push();
      return;
    }
    synchronized(mutex) {
      while(writeNestCount!=0) try {
        mutex.wait();
      } catch (InterruptedException e) {}
      if (Thread.currentThread() == lastWriter) lastWriterReadNestCount++;
      readNestCount++;
      //push();
    } 
  }
  
  public void writeLock() {
    Thread thread=Thread.currentThread();
    if(thread==writer) {
      writeNestCount++;
      return;
    }
    synchronized(mutex) {
      while(writeNestCount!=0||readNestCount!=0) try {
        //System.out.println("waiting to write-lock writer="+writer+" wnc="+writeNestCount+" rnc="+readNestCount);
        //new Exception().printStackTrace(System.out);
        mutex.wait();
      } catch (InterruptedException e) {}
      writer=thread;
      writeNestCount++;
      //debug();
    }
  }

  public void readUnlock() {
    if(writer!=null && Thread.currentThread()!=writer) 
      throw new IllegalMonitorStateException("not owner");
    synchronized(mutex) {
      int cur = readNestCount;
      try {
        switch(readNestCount) {
          case 0:
            throw new IllegalMonitorStateException("too many unlocks");
          case 1:
            readNestCount=0;
            lastWriterReadNestCount=0;
            lastWriter = null;
            //pop();
            mutex.notifyAll();
            //debug();
            break;
          default:
            readNestCount--;
            //pop();
            if (Thread.currentThread() == lastWriter) lastWriterReadNestCount--;
            if (readNestCount == 1 && lastWriterReadNestCount == 1) {
              mutex.notifyAll();
            }
              
        } 
      } finally {
        assert (cur-1 == readNestCount);
      }
    }
  }

  public void writeUnlock() {
    if(Thread.currentThread()!=writer)
      throw new IllegalMonitorStateException("not the writeLock owner");
    switch(writeNestCount) {
      case 0:
        throw new IllegalMonitorStateException("too many unlocks");
      case 1:
        synchronized(mutex) {
          writeNestCount=0;
          writer=null;
          mutex.notifyAll();
          //debug();
          return;
        }
      default:
        writeNestCount--;
    }
  }
  
  boolean canSwitch() {
    assert (Thread.currentThread() == writer);
    assert (lastWriterReadNestCount == 0);
    return writeNestCount==1 && readNestCount==0;
  }
  
  void switchToReadLock() {
    if (!canSwitch()) throw new IllegalStateException("cannot switch - not owner or nested writes");
    synchronized(mutex) {
      lastWriter=writer;
      writeNestCount--;
      writer=null;
      readLock();
      assert (writeNestCount == 0);
      assert (readNestCount == 1);
      assert (lastWriterReadNestCount == 1);
      mutex.notifyAll();
    }
  }
  
  boolean canSwitchBack() {
    synchronized (mutex) {
      return Thread.currentThread() == lastWriter && lastWriterReadNestCount == 1;
    }
  }
  
  void switchBackToWriteLock() {
    synchronized(mutex) {
      if (!canSwitchBack()) throw new IllegalMonitorStateException("not last writing thread");
      while(readNestCount>1) try {
        //System.out.println("waiting for switch back: readers="+readNestCount+" lastWriterReadNests="+lastWriterReadNestCount);
        //dump();
        mutex.wait();
      } catch (InterruptedException e) {}
      writer=lastWriter;
      lastWriter=null;
      writeNestCount++;
      assert (readNestCount == 1);
      readNestCount=0;
      //pop();
      assert (writeNestCount == 1);
    }
  }

  private void push() {
    Stack s = (Stack) readers.get(Thread.currentThread());
    if (s == null) {
      s = new Stack();
      readers.put(Thread.currentThread(), s);
    }
    s.push(new Exception());
  }
  
  private void pop() {
    Stack s = (Stack) readers.get(Thread.currentThread());
    s.pop();
  }

  private void dump() {
    for (Iterator i = readers.entrySet().iterator(); i.hasNext(); ) {
      Entry e = (Entry) i.next();
      Stack s = (Stack) e.getValue();
      if (s.isEmpty()) continue;
      System.out.println(e.getKey());
      for (Iterator i2 = s.iterator(); i2.hasNext(); )
        ((Exception)i2.next()).printStackTrace(System.out);
    }
  }
  
//  private static int id;
//  private final String name="lock "+(id++);
  private Map readers = Collections.synchronizedMap(new HashMap());
//
//  private final void debug() {
////    if (!EventQueue.isDispatchThread()) return;
//    Exception exception = new Exception();
//    final String m=exception.getStackTrace()[1].getMethodName();
//    final boolean sw=m.startsWith("switch"), read=m.startsWith("read"), free=m.indexOf('U')!=-1;
//    final String t=Thread.currentThread().getName();
//    String head="\033[3"+(read? 3: sw? 6 : 5)+'m';
//    System.out.println(head+name+' '+t+" "+m+" read=("+readNestCount+')'+" write=("+writeNestCount+')'+"\033[39m");
//    exception.printStackTrace(System.out);
//  }
}
