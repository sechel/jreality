package de.jreality.jogl;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SwtFrame implements Runnable {

  private Display display;
  private Shell shell;
  
  private final Object initLock=new Object();
  
  public SwtFrame() {
    new Thread(this).start();
    synchronized(initLock) {
      try {
        initLock.wait();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public Display getDisplay() {
    return display;
  }

  public Shell getShell() {
    return shell;
  }

  public void run() {
    synchronized (initLock) {
      display = new Display();
      shell = new Shell(display);
      initLock.notify();
    }
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }
  
}
