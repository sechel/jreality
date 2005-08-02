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

import de.jreality.scene.Geometry;
import de.jreality.scene.Texture2D;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class FlatPolygonShader implements PolygonShader {
    //private boolean interpolateColor = false;
    private VertexShader vertexShader;
    private double[] v = new double[Polygon.VERTEX_LENGTH]; 
    private boolean outline = false;
    private Texture texture;

    private double[] d1 =new double[3];
    private double[] d2 =new double[3];
    private double[] d3 =new double[3];
    
    public FlatPolygonShader() {
        super();
        vertexShader = new DefaultVertexShader();
    }
    
    public  FlatPolygonShader(VertexShader v, boolean outline) {
        super();
        vertexShader =  v;
        setOutline(outline);
    }

    //TODO Make this use the face normals.
    // for that to work the pipeline should set those if the
    // polygonshader is flat and the facenormals are present...
    public final void shadePolygon(Polygon p, double vertexData[], Environment environment) {
		v[Polygon.SX] = 0;
		v[Polygon.SY] = 0;
		v[Polygon.SZ] = 0;
		v[Polygon.R] = 0;
		v[Polygon.G] = 0;
        v[Polygon.B] = 0;
        v[Polygon.A] = 0;
        v[Polygon.NX] = 0;
		v[Polygon.NY] = 0;
		v[Polygon.NZ] = 0;
		for(int i = 0; i< p.length;i++) {
			int pos = p.vertices[i];
			v[Polygon.SX] += vertexData[pos+Polygon.SX];
			v[Polygon.SY] += vertexData[pos+Polygon.SY];
			v[Polygon.SZ] += vertexData[pos+Polygon.SZ];
			v[Polygon.R] += vertexData[pos+Polygon.R];
			v[Polygon.G] += vertexData[pos+Polygon.G];
            v[Polygon.B] += vertexData[pos+Polygon.B];
            v[Polygon.A] += vertexData[pos+Polygon.A];
//			v[Polygon.NX] += vertexData[pos+Polygon.NX];
//			v[Polygon.NY] += vertexData[pos+Polygon.NY];
//			v[Polygon.NZ] += vertexData[pos+Polygon.NZ];
		}
        v[Polygon.SX] /=p.length;
        v[Polygon.SY] /=p.length;
        v[Polygon.SZ] /=p.length;
        v[Polygon.R ] /=p.length;
        v[Polygon.G ] /=p.length;
        v[Polygon.B ] /=p.length;
        v[Polygon.A ] /=p.length;
        
//        d1[0] = vertexData[p.vertices[2]+Polygon.SX]-vertexData[p.vertices[1]+Polygon.SX];
//        d1[1] = vertexData[p.vertices[2]+Polygon.SY]-vertexData[p.vertices[1]+Polygon.SY];
//        d1[2] = vertexData[p.vertices[2]+Polygon.SZ]-vertexData[p.vertices[1]+Polygon.SZ];
//        
//        d2[0] = vertexData[p.vertices[0]+Polygon.SX]-vertexData[p.vertices[1]+Polygon.SX];
//        d2[1] = vertexData[p.vertices[0]+Polygon.SY]-vertexData[p.vertices[1]+Polygon.SY];
//        d2[2] = vertexData[p.vertices[0]+Polygon.SZ]-vertexData[p.vertices[1]+Polygon.SZ];
//        
//        
//        VecMat.cross(d1,d2,d3);
//        v[Polygon.NX] = d3[0];
//        v[Polygon.NY] = d3[1];
//        v[Polygon.NZ] = d3[2];
//        VecMat.normalize(v,Polygon.NX);
        int pos = p.vertices[0];
        v[Polygon.NX] = vertexData[pos+Polygon.NX];
        v[Polygon.NY] = vertexData[pos+Polygon.NY];
        v[Polygon.NZ] = vertexData[pos+Polygon.NZ];
        
        
		vertexShader.shadeVertex(v,0,environment);
		
		for(int i = 0; i< p.length;i++) {
		    pos = p.vertices[i];
			vertexData[pos+Polygon.R] = v[Polygon.R];
			vertexData[pos+Polygon.G] = v[Polygon.G];
			vertexData[pos+Polygon.B] = v[Polygon.B];
		}
    }

    public final VertexShader getVertexShader() {
        return vertexShader;
    }

    public final void setVertexShader(VertexShader s) {
		vertexShader = s;
    }

    public final boolean interpolateColor() {
        //return interpolateColor;
        return false;
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
      outline = eAppearance.getAttribute(ShaderUtility.nameSpace(name, "outline"), outline);
      vertexShader=ShaderLookup.getVertexShaderAttr(eAppearance, name, "vertexShader");
      // since the shading is done only once per polygon at the moment
      // we do not need to interpolate the colors:
      //interpolateColor=!(vertexShader instanceof ConstantVertexShader);

      Texture2D tex = (Texture2D)eAppearance.getAttribute(ShaderUtility.nameSpace(name, "texture"),null,Texture2D.class);
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
