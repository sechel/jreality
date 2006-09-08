/*
 * Created on 07.09.2006
 *
 * This file is part of the de.jreality.soft package.
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
package de.jreality.softviewer;

public abstract class AbstractPolygon {

    public static final int WX = 0;
    public static final int WY = 1;
    public static final int WZ = 2;
    public static final int WW = 3;
    public static final int SX = 4;
    public static final int SY = 5;
    public static final int SZ = 6;
    public static final int SW = 7;
    public static final int R = 8;
    public static final int G = 9;
    public static final int B = 10;
    public static final int A = 11;
    public static final int U = 12;
    public static final int V = 13;
    public static final int NX = 14;
    public static final int NY = 15;
    public static final int NZ = 16;
    public static final int VERTEX_LENGTH = 17;

    private double[] center = new double[VERTEX_LENGTH];
    
    public AbstractPolygon() {
        super();
    }

    public double[] getCenter() {
        return center;
    }

    public final Triangle[] triangulate(Triangle[] ta, ArrayStack<Triangle> stack) {
        final int length = getLength()-2;
        if(ta == null|| ta.length < length) ta = new Triangle[length];
        double[] start = getPoint(length+1);
        double[] next = getPoint(0);
        for (int i = 0; i < length; i++) {
            Triangle t = stack.pop();
            if(t== null) t = new Triangle();
            t.setShadingFrom(this);
            ta[i] = t;
            
            t.setPointFrom(0,start);
            t.setPointFrom(1,next);
            next = getPoint((i+1));
            t.setPointFrom(2,next);
        }
        return ta;
    }
    

    
    private double transparency;

    public final void setTransparency(final double t) {
        transparency = t;
    }

    public final double getTransparency() {
        return transparency;
    }

    
    private Texture texture;

    public final Texture getTexture() {
        return texture;
    }

    public final void setTexture(Texture texture) {
        this.texture = texture;
    }
    
    private boolean interpolateColor;
    
    public boolean isInterpolateColor() {
        return interpolateColor;
    }

    public void setInterpolateColor(boolean interpolateColor) {
        this.interpolateColor = interpolateColor;
    }

    private boolean interpolateAlpha;
    
    public boolean isInterpolateAlpha() {
        return interpolateAlpha;
    }

    public void setInterpolateAlpha(boolean interpolateAlpha) {
        this.interpolateAlpha = interpolateAlpha;
    }
    
    public void setShadingFrom(AbstractPolygon p) {
        setInterpolateAlpha(p.isInterpolateAlpha());
        setInterpolateColor(p.isInterpolateColor());
        setTransparency(p.getTransparency());
        setTexture(p.getTexture());
        setCenterFrom(p.getCenter());
            
    }
    
    public abstract double[] getPoint(int i);
    public abstract int getLength();
    
    public void setPointFrom(int i, double[] data) {
        System.arraycopy(data,0,getPoint(i),0,VERTEX_LENGTH);
    }
    public void setCenterFrom(double[] data) {
        System.arraycopy(data,0,getCenter(),0,VERTEX_LENGTH);
    }
    
    private boolean isSkybox;
    
    public boolean isSkybox() {
        return isSkybox;
    }

    public void setSkybox(boolean isSkybox) {
        this.isSkybox = isSkybox;
    }

}
