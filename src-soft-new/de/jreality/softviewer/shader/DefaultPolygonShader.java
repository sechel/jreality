/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.softviewer.shader;


import de.jreality.scene.Geometry;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.softviewer.Environment;
import de.jreality.softviewer.Polygon;
import de.jreality.softviewer.SimpleTexture;
import de.jreality.softviewer.Texture;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class DefaultPolygonShader extends PolygonShader {
    private boolean interpolateColor=true;
    private VertexShader vertexShader;
    protected boolean outline = false;
    private final boolean smooth;
    
    final private double[] d1;
    final private double[] d2;
    final private double[] d3;
    //final private double[] v;
    
    final de.jreality.shader.DefaultPolygonShader ps;
    public  DefaultPolygonShader() {
        this(new DefaultVertexShader());
    }
    public DefaultPolygonShader(VertexShader v) {
        super();
        vertexShader= v;
        smooth = true;
        d1 = null;
        d2 = null;
        d3 = null;
        //v = null;
        ps = null;
    }
    
    public DefaultPolygonShader(de.jreality.shader.DefaultPolygonShader ps) {
        super();
		vertexShader = new DefaultVertexShader(ps.getDiffuseColor(),ps.getSpecularCoefficient(),ps.getSpecularExponent(),ps.getTransparency());
        this.ps = ps;
        Texture2D tex = ps.getTexture2d();
        if (tex != null && tex.getImage() != null) texture = new SimpleTexture(tex);
        //outline = eAppearance.getAttribute(ShaderUtility.nameSpace(name, "outline"), outline);
        if(smooth = ps.getSmoothShading()) {
            d1 = null;
            d2 = null;
            d3 = null;
            //v = null;
        } else {
            d1 = new double[3];
            d2 = new double[3];
            d3 = new double[3];
            //v = new double[Polygon.VERTEX_LENGTH]; 
        }
        
    }
    
    
    public void shadePolygon(Polygon p, Environment environment, boolean vertexColors) {
        p.setTransparency(vertexShader.getTransparency());
        p.setTexture(texture);
        p.setInterpolateAlpha(interpolateAlpha());
        p.setInterpolateColor(interpolateColor());
        p.setSkybox(false);
        int n = p.getLength();
		if(smooth) {
            for(int i = 0; i< n;i++) {
                vertexShader.shadeVertex(p.getPoint(i),environment,vertexColors);
            }
        } else {
            double[] v = p.getCenter();
            v[Polygon.SX] = 0;
            v[Polygon.SY] = 0;
            v[Polygon.SZ] = 0;
            //v[Polygon.R] = 0;
            //v[Polygon.G] = 0;
            //v[Polygon.B] = 0;
            //v[Polygon.A] = 0;
            //v[Polygon.NX] = 0;
            //v[Polygon.NY] = 0;
            //v[Polygon.NZ] = 0;
            for(int i = 0; i< n;i++) {
                double vertexData[] = p.getPoint(i);
                v[Polygon.SX] += vertexData[Polygon.SX];
                v[Polygon.SY] += vertexData[Polygon.SY];
                v[Polygon.SZ] += vertexData[Polygon.SZ];
                v[Polygon.R] += vertexData[Polygon.R];
                v[Polygon.G] += vertexData[Polygon.G];
                v[Polygon.B] += vertexData[Polygon.B];
                v[Polygon.A] += vertexData[Polygon.A];
            }
            v[Polygon.SX] /=n;
            v[Polygon.SY] /=n;
            v[Polygon.SZ] /=n;
            v[Polygon.R ] /=n;
            v[Polygon.G ] /=n;
            v[Polygon.B ] /=n;
            v[Polygon.A ] /=n;
            double[] vertexData = p.getPoint(0);

            /*
            v[Polygon.NX] = vertexData[Polygon.NX];
            v[Polygon.NY] = vertexData[Polygon.NY];
            v[Polygon.NZ] = vertexData[Polygon.NZ];
            */
            
            vertexShader.shadeVertex(v,environment,vertexColors);
            
            for(int i = 0; i< n;i++) {
                vertexData = p.getPoint(i);
                vertexData[Polygon.R] = v[Polygon.R];
                vertexData[Polygon.G] = v[Polygon.G];
                vertexData[Polygon.B] = v[Polygon.B];
            }
        }
    }

    public final VertexShader getVertexShader() {
        return vertexShader;
    }

    public final boolean interpolateColor() {
        return smooth ||vertexShader.isVertexColors();
    }

    public boolean interpolateAlpha() {
        return vertexShader.interpolateAlpha();
    }

    public boolean isOutline() {
        return outline;
    }

    public void setOutline(boolean outline) {
        this.outline = outline;
    }

    public Texture getTexture() {
        return texture;
    }
    
    
    public boolean hasTexture() {
        return texture != null;
    }

    public void startGeometry(Geometry geom)
    {
        if(vertexShader!=null) vertexShader.startGeometry(geom);
    }

    public boolean needsSorting() {
        return (vertexShader.getTransparency() != 0.)||hasTexture()||interpolateAlpha(); 
    }

    @Override
    public void setColor(double r, double g, double b) {
        vertexShader.setColor(r,g,b);
        
    }

    @Override
    public double getBlue() {
        return vertexShader.getBlue();
    }

    @Override
    public double getGreen() {
        return vertexShader.getGreen();
    }

    @Override
    public double getRed() {
        return vertexShader.getRed();
    }

}
