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
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class DefaultPointShader implements PointShader {
    private PolygonShader coreShader = null;
    private PolygonShader outlineShader = null;
    //private double pointRadius =0.025;
    private double pointRadius =CommonAttributes.POINT_RADIUS_DEFAULT;
    private double outlineFraction =1/3.;
    
    public DefaultPointShader() {
        super();
    }
    public DefaultPointShader(PolygonShader coreShader, PolygonShader outlineShader) {
        super();
        this.coreShader = coreShader;
        this.outlineShader = outlineShader;
    }

    public PolygonShader getCoreShader() {
        return coreShader;
    }

    public PolygonShader getOutlineShader() {
        return outlineShader;
    }

    public double getPointRadius() {
        return pointRadius;
    }

    public double getOutlineFraction() {
        return outlineFraction;
    }

    public void setup(EffectiveAppearance eAppearance, String shaderName) {
        pointRadius = eAppearance.getAttribute(ShaderUtility.nameSpace(shaderName, CommonAttributes.POINT_RADIUS), pointRadius);
        outlineFraction = eAppearance.getAttribute(ShaderUtility.nameSpace(shaderName, "outlineFraction"), outlineFraction);
        coreShader=ShaderLookup.getPolygonShaderAttr(eAppearance, shaderName, "coreShader");
        outlineShader=ShaderLookup.getPolygonShaderAttr(eAppearance, shaderName, "outlineShader");
    }
    public void startGeometry(Geometry geom)
    {
        if(coreShader!=null) coreShader.startGeometry(geom);
        if(outlineShader!=null) outlineShader.startGeometry(geom);
    }
}
