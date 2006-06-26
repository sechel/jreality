package de.jreality.jogl;

import java.util.LinkedList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SwtQueue implements Runnable {

  private static SwtQueue instance = new SwtQueue();
  
  private Display display;
  
  private boolean inited = false;
  private final Object initLock=new Object();
  
  private final Thread swtThread;
  
  public static SwtQueue getInstance() {
    return instance;
  }
  
  private SwtQueue() {
    swtThread = new Thread(this);
    swtThread.start();
    synchronized(initLock) {
      try {
        while (!inited) initLock.wait();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public Display getDisplay() {
    return display;
  }

  int shellCnt=0;
  
  public Shell createShell() {
    final Shell[] shell = new Shell[1];
    Runnable r = new Runnable() {
      public void run() {
        shell[0] = new Shell(display);
        shellCnt++;
        shell[0].addDisposeListener(new DisposeListener() {
          public void widgetDisposed(DisposeEvent arg0) {
            shellCnt--;
            if (shellCnt==0) display.dispose();
          }
        });
      };
    };
    waitFor(r);
    return (shell[0]);
  }

  public void run() {
    synchronized (initLock) {
      display = new Display();
      inited = true;
      initLock.notify();
    }
    Runnable task = null;
    while (!display.isDisposed()) {
      while (true) {
        synchronized (tasks) {
          if (tasks.size() > 0)
            task = (Runnable) tasks.removeFirst();
        }
        if (task != null) { task.run(); task=null; }
        else break;
      }
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
    System.exit(0);
  }
  
  private LinkedList tasks = new LinkedList();
  
  public void invokeLater(Runnable r) {
    synchronized (tasks) {
      tasks.addLast(r);
      display.wake();
    }
  }
   
  public void waitFor(Runnable r) {
    if (Thread.currentThread() == swtThread) r.run();
    else {
      TrackedRunnable rr = new TrackedRunnable(r);
      synchronized (tasks) {
        tasks.addLast(rr);
        display.wake();
      }
      rr.waitFor();
    }
  }
  
  private static final class TrackedRunnable implements Runnable {

    private final Runnable task;
    private boolean done=false;
    
    TrackedRunnable(Runnable task) {
      this.task = task;
    }
    
    public void run() {
      task.run();
      synchronized (task) {
        done=true;
        task.notify();
      }
    }
    
    void waitFor() {
      synchronized (task) {
        while (!done) {
          try {
            task.wait();
          } catch (InterruptedException e) {
            throw new Error();
          }
        }
      }
    }
    
  }
}
