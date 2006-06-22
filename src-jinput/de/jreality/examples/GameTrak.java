
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

package de.jreality.examples;
import net.java.games.input.*;

public class GameTrak {

    private Controller gameTrak;
    private Component phi1;
    private Component phi2;
    private Component theta1;
    private Component theta2;
    private Component height1;
    private Component height2;
    public GameTrak() {
        ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cts = env.getControllers();
        
        for(int i = 0; i< cts.length; i++) {
            String name = cts[i].getName();
            System.out.print(" controller "+i+"'s name is "+name);
            if(contains(name, "Game-Trak")) {
                gameTrak = cts[i];
                System.out.println(" <- we use this one!");
            } 
            else System.out.println(".");
        }
        if(gameTrak == null)
            throw new RuntimeException("no Game-Trak found!");
        Component[] a = gameTrak.getComponents(); 
        phi1 = a[0];
        theta1 = a[1];
        height1 = a[2];

        phi2 = a[3];
        theta2 = a[4];
        height2 = a[5];
        
}

    private boolean contains(String name, String string) {
		return name.indexOf(string) != -1;
	}

	public void poll() {
        gameTrak.poll();
    }
    
    public float getPhi1() {
        return phi1.getPollData();
    }

    public float getPhi2() {
        return phi2.getPollData();
    }

    public float getTheta1() {
        return theta1.getPollData();
    }
    public float getTheta2() {
        return theta2.getPollData();
    }

    public float getHeight1() {
        return height1.getPollData();
    }
    public float getHeight2() {
        return height2.getPollData();
    }
    
    /**
     * first (left) point.
     * @param p
     */
    private static final float DISPLACEMENT = .05f;
    public final void getPoint1(float[] p) {
        float phi = (float)( (getPhi1())*Math.PI/4.f);
        float theta = (float)( (getTheta1())*Math.PI/4.f);
        float h = 1-getHeight1();
        p[0] =  - DISPLACEMENT+(float) (h*Math.sin(phi)*Math.cos(theta));
        p[1] = (float) (h*Math.cos(phi)*Math.cos(theta));
        p[2] = (float) (-h*Math.sin(theta));
        
    }
    /**
     * second (right) point.
     * @param p
     */
    public final void getPoint2(float[] p) {
        float phi = (float)( (getPhi2())*Math.PI/4.f);
        float theta = (float)( (getTheta2())*Math.PI/4.f);
        float h = 1-getHeight2();
        p[0] =  DISPLACEMENT +(float) (h*Math.sin(phi)*Math.cos(theta));
        p[1] = (float) (h*Math.cos(phi)*Math.cos(theta));
        p[2] = (float) (-h*Math.sin(theta));
    }
}
