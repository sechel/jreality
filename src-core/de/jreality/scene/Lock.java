package de.jreality.scene;


/**
 * @author pietsch + weissman
 */
public final class Lock
{
  private final Object mutex=new Object();
  private Thread writer, lastWriter;
  private int writeNestCount;
  private int readNestCount;

  public void readLock() {
    if(Thread.currentThread()==writer) {
      readNestCount++;
      //debug();
      return;
    }
    synchronized(mutex) {
      while(writeNestCount!=0) try {
        mutex.wait();
      } catch (InterruptedException e) {}
      readNestCount++;
      //debug();
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
        mutex.wait();
      } catch (InterruptedException e) {}
      writer=thread;
      writeNestCount++;
      //debug();
    }
  }

  public void readUnlock() {
    if(Thread.currentThread()==writer) {
      if(readNestCount == 0)
        throw new IllegalMonitorStateException("inconsistent r/w lock/unlocks");
      readNestCount--;
      return;
    }
    synchronized(mutex) {
      switch(readNestCount) {
        case 0:
          throw new IllegalMonitorStateException("too many unlocks");
        case 1:
          readNestCount=0;
          lastWriter = null;
          mutex.notifyAll();
          //debug();
          break;
        default:
          readNestCount--;
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
    return Thread.currentThread() == writer && writeNestCount==1 && readNestCount==0;
  }
  
  void switchToReadLock() {
    if (!canSwitch()) throw new IllegalStateException("cannot switch - not owner or nested writes");
    readLock();
    lastWriter=writer;
    writeUnlock();
  }
  
  boolean canSwitchBack() {
    return Thread.currentThread() == lastWriter;
  }
  
  void switchBackToWriteLock() {
    if (!canSwitchBack()) throw new IllegalMonitorStateException("not last writing thread");
    assert (writeNestCount == 0);
    synchronized(mutex) {
      while(readNestCount>1) try {
        mutex.wait();
      } catch (InterruptedException e) {}
      writer=lastWriter;
      lastWriter=null;
      writeNestCount++;
      readNestCount=0;
    }
  }
  
//  private static int id;
//  private final String name="lock "+(id++);
//  private LinkedList readers=new LinkedList();
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
