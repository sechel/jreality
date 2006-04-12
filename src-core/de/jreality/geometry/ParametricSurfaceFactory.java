package de.jreality.geometry;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArrayArray;

public class ParametricSurfaceFactory extends AbstractQuadMeshFactory {

	final OoNode uMin = new OoNode( new Double(0), "uMin" );
	final OoNode uMax = new OoNode( new Double(1), "uMax" );
	final OoNode vMin = new OoNode( new Double(0), "vMin" );
	final OoNode vMax = new OoNode( new Double(1), "vMax" );
	
	final OoNode immersion = new OoNode( "immersion" );
	
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
				immersion.evaluate(u, v, vertexCoordinates[indexOfUV], 0);
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

		vertexCoordinates.update();
		
		super.recompute();
		
		
	}
	
	protected void updateImpl() {
	
		super.updateImpl();
	
		if( nodeWasUpdated(vertexCoordinates) ) { 
			log( "set", Attribute.COORDINATES, "vertex" );
			ifs.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(vertexCoordinates()));
		}
		
	}

	public abstract static class DefaultImmersion implements Immersion {

		public boolean isImmutable() {
			return false;
		}

		public int getDimensionOfAmbientSpace() {
			return 3;
		}

		public void evaluate(double u, double v, double[] xyz, int index) {
			x=u;
			y=v;
			z=evaluate( u, v );
			xyz[3*index+0] = x;
			xyz[3*index+1] = y;
			xyz[3*index+2] = z;
		}
		
		protected double x, y, z;
		
		abstract public double evaluate( double u, double v );
	}
}
