/*
 * Created on 11.02.2006
 *
 * This file is part of the de.jreality.swing package.
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
package de.jreality.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.scene.Geometry;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;

/**
 * A tool to use with the JRJComponent. It transforms tool events 
 * into Java awt MouseEvents and sends them to the component given a
 * creation time for dispatching. The mouse coordinates are derived form 
 * the texture coordinates of the geometriy this tool is attached to.
 * 
 * TODO: add support for different mouse buttons and keyboard events.
 * TODO: add support for texture transformation
 * 
 * @version 1.0
 * @author timh
 *
 */
public class MouseEventTool extends Tool {
    private Point oldPoint;
    private Component comp;

    private Geometry current;

//    private InputSlot drag = InputSlot.getDevice("RotateActivation");
//    private InputSlot drag = InputSlot.getDevice("SecondaryAction");
    private InputSlot drag = InputSlot.getDevice("PrimarySelection");

    private List activationSlots = new LinkedList();

    private List usedSlots = new LinkedList();

    private int width;

    private int height;
    private boolean dispatchLater = true;
    
    public MouseEventTool(Component c,boolean dispatchLater) {
        super();
        this.comp = c;
        this.dispatchLater = dispatchLater;
    }

    public MouseEventTool(Component c) {
        this(c,true);
    }
    
    {
        activationSlots.add(drag);
        usedSlots.add(InputSlot.getDevice("PointerTransformation"));
    }

    public List getOutputSlots() {
        return Collections.EMPTY_LIST;
    }

    public List getActivationSlots() {
        return activationSlots;
    }

    public List getCurrentSlots() {
        return usedSlots;
    }

    public void activate(ToolContext e) {
        System.out.println(" pick activate in MouseEventTool");
        try {
            current = (Geometry) e.getCurrentPick().getPickPath()
                    .getLastElement();
        } catch (Exception ex) {
        }
        Point newPoint = generatePoint(e.getCurrentPick());
        oldPoint = newPoint;
        dispatchMouseEvent(newPoint, MouseEvent.MOUSE_PRESSED);
    }

    public void perform(ToolContext e) {
        try {
            if (current == (Geometry) e.getCurrentPick().getPickPath()
                    .getLastElement()) {
                Point newPoint = generatePoint(e.getCurrentPick());
                //oldPoint = newPoint;
                dispatchMouseEvent(newPoint, MouseEvent.MOUSE_DRAGGED);
            }
        } catch (Exception ex) {
        }
    }

    public void deactivate(ToolContext e) {
        Point newPoint = generatePoint(e.getCurrentPick());
        dispatchMouseEvent(newPoint, MouseEvent.MOUSE_RELEASED);
        if(oldPoint.equals(newPoint))
            dispatchMouseEvent(newPoint, MouseEvent.MOUSE_CLICKED);
        current = null;
    }

    private Point generatePoint(PickResult pr) {
        Point newPoint = null;
        if(pr != null) {
            double tc[] = pr.getTextureCoordinates();
            
            // if there are no texture coordinates use x, y in object space:
            if(tc== null || tc.length<2) {
                tc =  pr.getObjectCoordinates();
            newPoint = new Point((int) ((1 - tc[0]) * width),
                    (int) ((1 - tc[1]) * height));
            } else
                newPoint = new Point((int) ((tc[0]) * width),
                        (int) ((tc[1]) * height));
            } else // TODO better to remember the last Point and use that?
            newPoint = new Point(0,0);
        return newPoint;
    }    
        
    void dispatchMouseEvent(Point newPoint, int type) {
        final MouseEvent newEvent = new MouseEvent(comp,
                (int) type, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, newPoint.x,
                newPoint.y, 1, false, MouseEvent.BUTTON1);
        //System.out.println("dispatching "+newEvent);
        this.dispatchEvent(newEvent);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * this method is safe as it executes in the DispatchThread.
     * @param e
     */
    public void dispatchEvent(final AWTEvent e) {
        if(dispatchLater)
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    comp.dispatchEvent(e);
                }
            });
        else
            comp.dispatchEvent(e);
    }

}
