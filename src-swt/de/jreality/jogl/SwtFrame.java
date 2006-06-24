package de.jreality.jogl;

import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SwtFrame implements Runnable {

  private static SwtFrame instance = new SwtFrame();
  
  private Display display;
  
  private boolean inited = false;
  private final Object initLock=new Object();
  
  public static SwtFrame getInstance() {
    return instance;
  }
  
  private SwtFrame() {
    new Thread(this).start();
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

  public Shell createShell() {
    final Shell[] shell = new Shell[1];
    Runnable r = new Runnable() {
      public void run() {
        shell[0] = new Shell(display);
      };
    };
    display.syncExec(r);
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
      synchronized (tasks) {
        if (tasks.size() > 0)
          task = (Runnable) tasks.removeFirst();
      }
      if (task != null) {
        task.run();
        synchronized(finishedTasks) {
          finishedTasks.add(task);
          task = null;
          finishedTasks.notifyAll();
        }
      }
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }
  
  private LinkedList tasks = new LinkedList();
  private HashSet finishedTasks = new HashSet();
  
  public void waitFor(Runnable r) {
    synchronized (tasks) {
      tasks.addLast(r);
      display.wake();
    }
    while (true) synchronized (finishedTasks) {
      if (finishedTasks.remove(r)) return;
      try {
        finishedTasks.wait();
      } catch (InterruptedException e) {
        throw new Error();
      }
    }
  }
}
