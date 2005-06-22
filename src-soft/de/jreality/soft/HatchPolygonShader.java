/*
 * Created on 09.06.2005
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

import de.jreality.shader.Texture2D;
import de.jreality.util.AttributeEntityFactory;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;


/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class HatchPolygonShader extends DefaultPolygonShader {

    /**
     * 
     */
    public HatchPolygonShader() {
        super();
        texture = new HatchTexture();
    }

    public void setup(EffectiveAppearance eAppearance, String name) {
        outline = eAppearance.getAttribute(NameSpace.name(name, "outline"), outline);
        setVertexShader(ShaderLookup.getVertexShaderAttr(eAppearance, name, "vertexShader"));
      }
    
 
}
