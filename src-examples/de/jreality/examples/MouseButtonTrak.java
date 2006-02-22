package de.jreality.examples;
import net.java.games.input.*;
import net.java.games.input.Controller;
import net.java.games.input.LinuxDevice;
import net.java.games.input.LinuxEnvironmentPlugin;

/*
 * Created on 14.01.2006
 *
 * This file is part of the  package.
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

public class MouseButtonTrak {
    private static final LinuxEnvironmentPlugin LEP =new LinuxEnvironmentPlugin();
    private Controller mouse;
    private Component b1;
    private Component b2;
    private Component b3;
    
    public MouseButtonTrak() {
        super();Controller[] cts = LEP.getControllers();
        
        for(int i = 0; i< cts.length; i++) {
            String name = cts[i].getName();
            System.out.print(" controller "+i+"'s name is "+name);
            if(name.contains("USB")&& name.contains("Mouse")) {
                mouse = cts[i];
                System.out.println(" <- we use this one!");
            } 
            else System.out.println(".");
        }
        if(mouse == null)
            throw new RuntimeException("no USB mouse found!");
        mouse = mouse.getControllers()[1];
        Component[] a = mouse.getComponents(); 
        b1 = a[0];
        b2 = a[1];
        b3 = a[2];
        
        
        if(mouse instanceof LinuxDevice) {
            LinuxDevice ld = (LinuxDevice) mouse;
            int bts[] = new int[20];
            
        }
}

    public final void poll() {
        mouse.poll();
    }
    
    public boolean pollButton1() {
        poll();
        return b1.getPollData()>0;
    }

    public boolean pollButton2() {
        poll();
        return b2.getPollData()>0;
    }

    public boolean pollButton3() {
        poll();
        return b3.getPollData()>0;
    }

    public boolean getButton1() {
        return b1.getPollData()>0;
    }

    public boolean getButton2() {
        return b2.getPollData()>0;
    }

    public boolean getButton3() {
        return b3.getPollData()>0;
    }

}
