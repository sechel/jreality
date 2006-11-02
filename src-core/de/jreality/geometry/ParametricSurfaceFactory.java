/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.geometry;

import java.awt.Dimension;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;

public class ParametricSurfaceFactory extends AbstractQuadMeshFactory {

	final OoNode uMin = node( new Double(0), "uMin" );
	final OoNode uMax = node( new Double(1), "uMax" );
	final OoNode vMin = node( new Double(0), "vMin" );
	final OoNode vMax = node( new Double(1), "vMax" );
	
	final OoNode immersion = node( "immersion" );
	
	ParametricSurfaceFactory( Immersion immersion, double uMin, double uMax, double vMin, double vMax ) {
		super();
		
		setUMin( uMin );
		setUMax( uMax );
		setVMin( vMin );
		setVMax( vMax );
		
		setImmersion( immersion );
	}
	
	ParametricSurfaceFactory( Immersion immersion ) {
		this( immersion, 0, 1, 0, 1 );
	}
	
	public ParametricSurfaceFactory() {
		this( new Immersion() {

			public int getDimensionOfAmbientSpace() {
				return 3;
			}
			public void evaluate(double u, double v, double[] xyz, int index) {
				xyz[index+0]=u; 
				xyz[index+1]=v;				
			}
			public boolean isImmutable() {
				return true;
			}			
		});
	}
	
	public interface Immersion {
		public boolean isImmutable();
		public int getDimensionOfAmbientSpace();
		public void evaluate(double u, double v, double[] xyz, int index);
	}


	{
		vertexCoordinates.addIngr(vLineCount);
		vertexCoordinates.addIngr(uLineCount);
		vertexCoordinates.addIngr(uMin);
		vertexCoordinates.addIngr(uMax);
		vertexCoordinates.addIngr(vMin);
		vertexCoordinates.addIngr(vMax);
		vertexCoordinates.addIngr(immersion);
		vertexCoordinates.setUpdateMethod(
				new OoNode.UpdateMethod() {
					public Object update( Object object) {	
						return generateVertexCoordinates( (double[][])object);	
					}					
				}
		);
	}
	
	double [][] generateVertexCoordinates( double [][] vertexCoordinates ) {
		
		log( "compute", Attribute.COORDINATES, "vertex ");
		
		final Immersion immersion = getImmersion();
		
		if( vertexCoordinates == null || vertexCoordinates.length != nov() )
			vertexCoordinates = new double[nov()][immersion.getDimensionOfAmbientSpace()];
			
		final int vLineCount = getVLineCount();
		final int uLineCount = getULineCount();
		
		final double dv=(getVMax() - getVMin()) / (vLineCount - 1);
		final double du=(getUMax() - getUMin()) / (uLineCount - 1);
		
		final double uMin = getUMin();
		final double vMin = getVMin();
		
		double v=vMin;
		for(int iv=0, firstIndexInULine=0;
		iv < vLineCount;
		iv++, v+=dv, firstIndexInULine+=uLineCount) {
			double u=uMin;
			for(int iu=0; iu < uLineCount; iu++, u+=du) {
				final int indexOfUV=firstIndexInULine + iu;
				immersion.evaluate(u, v, vertexCoordinates[ uLineCount*iv + iu ], 0 ); //indexOfUV], 0);
			}
		}
			
		
		return vertexCoordinates;
	}

	public Immersion getImmersion() {
		return (Immersion)immersion.getObject();
	}

	public void setImmersion(Immersion f) {
		if( f==null)
			throw new IllegalArgumentException( "Immersion cannot set to null." );
		immersion.setObject(f);
	}

	public double getUMax() {
		return ((Double)uMax.getObject()).doubleValue();
	}

	public void setUMax(double max) {
		uMax.setObject(new Double(max));
	}

	public double getUMin() {
		return ((Double)uMin.getObject()).doubleValue();
	}

	public void setUMin(double min) {
		uMin.setObject(new Double(min));
	}

	public double getVMax() {
		return ((Double)vMax.getObject()).doubleValue();
	}

	public void setVMax(double max) {
		vMax.setObject(new Double(max));
	}

	public double getVMin() {
		return ((Double)vMin.getObject()).doubleValue();
	}

	public void setVMin(double min) {
		vMin.setObject(new Double(min));
	}


	void recompute() {
		if( !getImmersion().isImmutable() )
			immersion.outdate();

		super.recompute();

		
	}
	
	protected void updateImpl() {
	
		super.updateImpl();
	
		if( nodeWasUpdated(vertexCoordinates) ) { 
			log( "set", Attribute.COORDINATES, "vertex" );
			ifs.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(vertexCoordinates()));
		}
		// TODO figure out why I had to put this here (not getting done in super -- gunn
		ifs.setGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE, new Dimension( getULineCount(), getVLineCount() ));
		
	}

  /**
   * An immersion in 3-space. override the abstract
   * method evaluate and assign the protected variables
   * x, y, and z there depending on the given u, v values.
   * 
   */
	public abstract static class DefaultImmersion implements Immersion {

		public boolean isImmutable() {
			return false;
		}

		public int getDimensionOfAmbientSpace() {
			return 3;
		}

		public void evaluate(double u, double v, double[] xyz, int index) {
			evaluate( u, v );
			xyz[3*index+0] = x;
			xyz[3*index+1] = y;
			xyz[3*index+2] = z;
		}
		
		protected double x, y, z;
		
    /**
     * Assign the protected variables x, y, z here.
     * 
     * @param u 
     * @param v
     */
		abstract public void evaluate( double u, double v );
	}
}
