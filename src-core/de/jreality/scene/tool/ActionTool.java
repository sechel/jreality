//JTEM - Java Tools for Experimental Mathematics
//Copyright (C) 2002 JEM-Group
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.jreality.scene.tool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ulrich
 */

public class ActionTool extends Tool {

    private int numListener;
    private ActionListener[] listener;
    private Object key;
    List activationSlots = new LinkedList();
    
    public ActionTool(InputSlot activationSlot) {
        numListener=0;
        listener = new ActionListener[10];
        key=new Object();
        activationSlots.add(activationSlot);
    }
    
    public ActionTool(String activationSlotName) {
        this(InputSlot.getDevice(activationSlotName));
    }
    
    public List getActivationSlots() {
        return activationSlots;
    }

    public List getCurrentSlots() {
        return Collections.EMPTY_LIST;
    }

    public List getOutputSlots() {
        return Collections.EMPTY_LIST;
    }

    public void activate(ToolContext tc) {
        fire(tc);
    }
    
    public void perform(ToolContext tc) {}

    public void deactivate(ToolContext tc) {}
 
    public void fire(Object obj) {
        synchronized(key) {
            ActionEvent ev = new ActionEvent(obj,0,"ActionTool");
            for(int i=0; i<numListener; i++) {
                listener[i].actionPerformed(ev);
            }
        }
	}
	public void addActionListener(ActionListener l) {
        synchronized(key) {
            if(numListener==listener.length) {
                ActionListener[] newListener=new ActionListener[numListener+10];
                System.arraycopy(listener,0,newListener,0,numListener);
                listener=newListener;
          	}
            listener[numListener++]=l;
        }
	}
	
	public void removeActionListener(ActionListener l) {
        synchronized(key) {
            int i;
            find: {
                for(i=0; i<numListener; i++)
                    if(listener[i]==l) break find;
                return;
            }
            numListener--;
            if(i!=numListener) {
                System.arraycopy(listener,i+1,listener,i,numListener-i);
            }
            listener[numListener]=null;
        }
	}
}
