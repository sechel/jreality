/*
 * Created on 27.04.2004
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
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class DefaultLineShader implements LineShader {
    private double lineWidth =0.01;
    PolygonShader polygonShader =null;
    /**
     * 
     */
    public DefaultLineShader() {
        super();
    }
    public DefaultLineShader(PolygonShader p) {
        super();
        polygonShader = p;
    }

    /* (non-Javadoc)
     * @see de.jreality.soft.LineShader#getPolygonShader()
     */
    public PolygonShader getPolygonShader() {
        return polygonShader;
    }

    /* (non-Javadoc)
     * @see de.jreality.soft.LineShader#getLineWidth()
     */
    public double getLineWidth() {
        return lineWidth;
    }

    /* (non-Javadoc)
     * @see de.jreality.soft.AbstractShader#setup(de.jreality.util.EffectiveAppearance, java.lang.String)
     */
    public void setup(EffectiveAppearance eAppearance, String shaderName) {
        lineWidth = eAppearance.getAttribute(ShaderUtility.nameSpace(shaderName, "tubeRadius"), lineWidth);
        polygonShader=ShaderLookup.getPolygonShaderAttr(eAppearance, shaderName, "polygonShader");
    }
    public void startGeometry(Geometry geom)
    {
        if(polygonShader!=null) polygonShader.startGeometry(geom);
    }
}
