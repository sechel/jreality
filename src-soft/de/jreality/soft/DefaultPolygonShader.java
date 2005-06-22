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
import de.jreality.shader.Texture2D;
import de.jreality.util.AttributeEntityFactory;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class DefaultPolygonShader implements PolygonShader {
    private boolean interpolateColor=true;
    private VertexShader vertexShader;
    protected boolean outline = false;
    protected Texture texture;

    public DefaultPolygonShader() {
        super();
		vertexShader = new DefaultVertexShader();
    }
    
	public DefaultPolygonShader(VertexShader v) {
		 super();
		 vertexShader = v;
	 }
	 
	public DefaultPolygonShader(VertexShader v,boolean outline) {
		 super();
		 vertexShader = v;
		 this.outline = outline;
	 }

    public final void shadePolygon(Polygon p, double vertexData[], Environment environment) {
		for(int i = 0; i< p.length;i++) {
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
    outline = eAppearance.getAttribute(NameSpace.name(name, "outline"), outline);
    setVertexShader(ShaderLookup.getVertexShaderAttr(eAppearance, name, "vertexShader"));

    de.jreality.scene.Texture2D tex = (de.jreality.scene.Texture2D)eAppearance.getAttribute(NameSpace.name(name, "texture"),null,de.jreality.scene.Texture2D.class);
    if(tex != null) texture = new SimpleTexture(tex);

    if (AttributeEntityFactory.hasAttributeEntity(Texture2D.class, NameSpace.name(name,"texture2d"), eAppearance))
      texture = new SimpleTexture((Texture2D) AttributeEntityFactory.createAttributeEntity(Texture2D.class, NameSpace.name(name,"texture2d"), eAppearance));
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
