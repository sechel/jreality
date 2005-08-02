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
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class ImplodePolygonShader implements PolygonShader {
    private VertexShader vertexShader;
    private boolean outline;
    double implodeFactor;
    private boolean interpolateColor;
    private Texture texture;

    public ImplodePolygonShader() {
		this(new DefaultVertexShader(),0.8, false);
    }
    
    public ImplodePolygonShader(VertexShader v, double implodeFactor, boolean outline) {
		super();
    	vertexShader = v;
    	this.implodeFactor = implodeFactor;
    	setOutline(outline);
    }

    public final void shadePolygon(Polygon p, double vertexData[], Environment environment) {
		double centerX = 0;
		double centerY = 0;
		double centerZ = 0;
		for(int i = 0; i< p.length;i++) {
			int pos = p.vertices[i];
			centerX += vertexData[pos+Polygon.SX];
			centerY += vertexData[pos+Polygon.SY];
			centerZ += vertexData[pos+Polygon.SZ];
		}

		double oml = (1-implodeFactor)/p.length;
		centerX *= oml;
		centerY *= oml;
		centerZ *= oml;
		
		for(int i = 0; i< p.length;i++) {
			int pos = p.vertices[i];
			vertexData[pos+Polygon.SX] = implodeFactor * vertexData[pos+Polygon.SX] + centerX;
			vertexData[pos+Polygon.SY] = implodeFactor * vertexData[pos+Polygon.SY] + centerY;
			vertexData[pos+Polygon.SZ] = implodeFactor * vertexData[pos+Polygon.SZ] + centerZ;
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
        return vertexShader.interpolateAlpha();
    }

    public boolean isOutline() {
        return outline;
    }

    public void setOutline(boolean outline) {
        this.outline = outline;
    }

    public void setup(EffectiveAppearance eAppearance, String name) {
      outline = eAppearance.getAttribute(ShaderUtility.nameSpace(name, "outline"), outline);
      implodeFactor = eAppearance.getAttribute(ShaderUtility.nameSpace(name, "implodeFactor"), implodeFactor);
      setVertexShader(ShaderLookup.getVertexShaderAttr(eAppearance, name, "vertexShader"));
//    String textureFile = (String) eAppearance.getAttribute(NameSpace.name(name, "texture"), "");
//    try {
//        texture = new SimpleTexture(textureFile);
//    } catch (MalformedURLException e) {
//        Logger.getLogger("de.jreality").log(Level.FINEST, "attempt to load textur {0} failed", textureFile);
//        texture =null;
//    }
      texture =(Texture)eAppearance.getAttribute(ShaderUtility.nameSpace(name, "texture"),null,Texture.class);
      
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
