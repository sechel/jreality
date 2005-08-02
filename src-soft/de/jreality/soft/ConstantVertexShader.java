/*
 * Created on Dec 9, 2003
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

import java.awt.Color;

import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Geometry;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class ConstantVertexShader implements VertexShader {

	private double red;
	private double green;
	private double blue;
	private double transparency = 0;

	public ConstantVertexShader() {
		super();
	}

	public ConstantVertexShader(double r, double g, double b) {
		super();
		red = r;
		green = g;
		blue = b;
	}
	
	public ConstantVertexShader(double[] color, double transparency) {
		super();
		red = color[0];
		green = color[1];
		blue = color[2];
		this.transparency = transparency;
	}

	public void shadeVertex(
		double[] vertex,
		int pos,
		Environment environment) {
			vertex[pos+Polygon.R] = red;
			vertex[pos+Polygon.G] = green;
			vertex[pos+Polygon.B] = blue;
	}

    public boolean interpolateAlpha() {
        return false;
    }

	public double getTransparency() {

		return transparency;
	}

	public double getBlue() {
		return blue;
	}

	public double getGreen() {
		return green;
	}

	public double getRed() {
		return red;
	}

	public void setBlue(double blue) {
		this.blue = blue;
	}

	public void setGreen(double green) {
		this.green = green;
	}

	public void setRed(double red) {
		this.red = red;
	}

    public void setTransparency(double transparency) {
        this.transparency = transparency;
    }

    public void setup(EffectiveAppearance eAppearance, String name) {
      transparency = eAppearance.getAttribute(ShaderUtility.nameSpace(name, "transparency"), transparency);
      Color c = (Color)eAppearance.getAttribute(ShaderUtility.nameSpace(name, "color"), Color.BLACK);
      c = (Color)eAppearance.getAttribute(ShaderUtility.nameSpace(name, CommonAttributes.DIFFUSE_COLOR), c);
      float[] rgb=c.getComponents(null);
      red=rgb[0];
      green=rgb[1];
      blue=rgb[2];
    }
    public void startGeometry(Geometry geom) {}
}
