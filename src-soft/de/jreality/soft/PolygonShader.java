/*
 * Created on Dec 5, 2003
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

/**
 * This is what the PolygonPipeline uses to shade a polygon.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public interface PolygonShader extends AbstractShader {
	public  void shadePolygon(final Polygon p, double[] vertexData, final Environment environment);
	public VertexShader getVertexShader();
	public void setVertexShader(VertexShader s);
	public boolean interpolateColor();
	public boolean isOutline();
    public Texture getTexture();
    public boolean hasTexture();
    public boolean interpolateAlpha();
    public boolean needsSorting();
}
