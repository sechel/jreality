/*
 * Created on 27.05.2004
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
package de.jreality.soft;

import de.jreality.scene.Geometry;
import de.jreality.scene.Texture2D;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class SkyboxPolygonShader implements PolygonShader {
    private boolean interpolateColor=true;
    private VertexShader vertexShader;
    private boolean outline = false;
    private Texture texture;

    public SkyboxPolygonShader() {
        super();
        vertexShader = new DefaultVertexShader();
    }
    
    public SkyboxPolygonShader(VertexShader v) {
        super();
        vertexShader = v;
    }
    
    public SkyboxPolygonShader(VertexShader v,boolean outline) {
        super();
        vertexShader = v;
        this.outline = outline;
    }

    public final void shadePolygon(Polygon p, double vertexData[], Environment environment) {
        double[] matrix =environment.getMatrix();
        double x = -matrix[0+3];
        double y = -matrix[4+3];
        double z = -matrix[8+3];
        for(int i = 0; i< p.length;i++) {
            int pos = p.vertices[i];
            vertexData[pos+Polygon.SX] = vertexData[pos+Polygon.SX] + x;
            vertexData[pos+Polygon.SY] = vertexData[pos+Polygon.SY] + y;
            vertexData[pos+Polygon.SZ] = vertexData[pos+Polygon.SZ] + z;
            vertexShader.shadeVertex(vertexData,p.vertices[i],environment);
        }
    }

    public final VertexShader getVertexShader() {
        return vertexShader;
    }

    public final void setVertexShader(VertexShader s) {
        if(vertexShader!=s) {
            vertexShader = s;
            interpolateColor=!(s instanceof ConstantVertexShader);
        }
    }

    public final boolean interpolateColor() {
        return interpolateColor;
    }

    public boolean interpolateAlpha() {
        return false;
    }

    public boolean isOutline() {
        return outline;
    }

    public void setOutline(boolean outline) {
        this.outline = outline;
    }

    public void setup(EffectiveAppearance eAppearance, String name) {
        outline = eAppearance.getAttribute(NameSpace.name(name, "outline"), outline);
        setVertexShader(ShaderLookup.getVertexShaderAttr(eAppearance, name, "vertexShader"));

        Texture2D tex = (Texture2D)eAppearance.getAttribute(NameSpace.name(name, "texture"),null,Texture2D.class);
        if(tex != null) texture = new SimpleTexture(tex);
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
        return (getVertexShader().getTransparency() != 0.)||hasTexture()||interpolateAlpha(); 
    }
}
