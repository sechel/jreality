/*
 * Created on Dec 18, 2003
 *
 * This file is part of the jReality package.
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

import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class TwoSidePolygonShader implements PolygonShader {
    private PolygonShader front;
    private PolygonShader back;
    
    public TwoSidePolygonShader() {
        super();
        front = new DefaultPolygonShader();
        back = new DefaultPolygonShader();
    }
    
	 
	public TwoSidePolygonShader(PolygonShader f, PolygonShader b) {
		 super();
		 front =f;
         back = b;
	 }

    public final void shadePolygon(Polygon p, double vd[], Environment environment) {
        int pos = p.vertices[0];
        double px = vd[pos+Polygon.SX];
        double py = vd[pos+Polygon.SY];
        double pz = vd[pos+Polygon.SZ];
        double[] d1 = new double[3];
        pos =p.vertices[1];
        d1[0] =vd[pos+Polygon.SX] -px;
        d1[1] =vd[pos+Polygon.SY] -py;
        d1[2] =vd[pos+Polygon.SZ] -pz;
        double[] d2 = new double[3];
        pos =p.vertices[p.length-1];
        d2[0] =vd[pos+Polygon.SX] -px;
        d2[1] =vd[pos+Polygon.SY] -py;
        d2[2] =vd[pos+Polygon.SZ] -pz;
        
        d1 = Rn.crossProduct(null,d1,d2);
        
		boolean faceforward = (px * d1[0] + py * d1[1] + pz * d1[2]) <= 0 ;
        if(faceforward){
            front.shadePolygon(p,vd,environment);
            p.setShader(front);
        } else {
            back.shadePolygon(p,vd,environment);
            p.setShader(back);
        }
    }

    public final VertexShader getVertexShader() {
        return null;
    }

    public final void setVertexShader(VertexShader s) {
     
    }

    public final boolean interpolateColor() {
        return false;
    }

    public boolean interpolateAlpha() {
        return false;
    }

    public boolean isOutline() {
        return false;
    }

    public void setOutline(boolean outline) {
    }

  public void setup(EffectiveAppearance eAppearance, String shaderName) {
      front=ShaderLookup.getPolygonShaderAttr(eAppearance, shaderName, "front");
      back=ShaderLookup.getPolygonShaderAttr(eAppearance, shaderName, "back");
  }


    public Texture getTexture() {
        return null;
    }
    
    
    public boolean hasTexture() {
        return false;
    }

    public void startGeometry(Geometry geom)
    {
        front.startGeometry(geom);
        back.startGeometry(geom);
    }
    public boolean needsSorting() {
        return false; 
    }
}
