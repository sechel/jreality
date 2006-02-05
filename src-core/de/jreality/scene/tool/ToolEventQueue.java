/*
 * Created on Mar 21, 2005
 *
 * This file is part of the de.jreality.scene.tool package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.scene.tool;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;

import de.jreality.util.LoggingSystem;

/**
 * 
 * TODO: check sync!
 * 
 * @author weissman
 *
 **/
public class ToolEventQueue {
    
    private ToolEventReceiver receiver;
    private LinkedList queue = new LinkedList();
    private final Object mutex = new Object();
    protected volatile boolean running = true;
    
    private Runnable eventThread = new Runnable() {
    	public void run() {
            ToolEvent event;
            while (true) {
                synchronized(mutex) {
                    while (queue.isEmpty()) {
                        try {
                            mutex.wait();
                            if (!running) return;
                        } catch (InterruptedException e) {
                            throw new Error();
                        }
                    }
                    event = (ToolEvent) queue.removeFirst();
                }
                receiver.processToolEvent(event);
            }
        }    
    };
    
    ToolEventQueue(ToolEventReceiver receiver) {
        this.receiver = receiver;
    }

    private volatile boolean started = false;
    void start() {
      if (started) throw new IllegalStateException("already started");
      started = true;
      Thread myThread = new Thread(eventThread);
      myThread.setName("jReality ToolSystem EventQueue");
      myThread.start();
    }
    
    /**
     * places the given event into the queue
     * if queue was started already
     * 
     * @param event the event to post
     * @return true if the event was added false if not
     */
    boolean addEvent(ToolEvent event) {
      if (!started) return false;
      placeEvent(event);
      return true;
    }
    
    /**
     * returns wether the event was added or if it replaced another event
     * @param event
     * @param senderWaits
     * @return true if the event was added to the queue, false if replaced an
     * already scheduled event
     */
    private boolean placeEvent(ToolEvent event) {
      synchronized(mutex) {
        // we replace the last possible event
        for (ListIterator i = queue.listIterator(queue.size()); i.hasPrevious(); ) {
            ToolEvent e = (ToolEvent) i.previous();
            if (event.canReplace(e)) {
                LoggingSystem.getLogger(this).log(e.getInputSlot() == InputSlot.getDevice("SystemTime") ? Level.FINEST:Level.FINER, "replacing ToolEvent {0} with {1}", new Object[]{e, event});
                e.replaceWith(event);
                return false;
            }
        }
        queue.addLast(event);
//        System.out.println(queue);
        mutex.notify();
      }
      return true;
    }
    
    public void dispose() {
        running = false;
        synchronized (mutex) {
            mutex.notifyAll();
            queue.clear();
        }
    }
}