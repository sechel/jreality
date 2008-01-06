package de.jreality.examples;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.writer.WriterOBJ;

/**
 * Creates an Oloid.
 * 
 * @author schmies
 *
 */
public class Oloid {

	/**
	 * Creates an Oloid.
	 * 
	 * @param N discretization of the oloid.
	 * @return Oloid
	 */
	public static IndexedFaceSet createOloid( int N ) {

		if( N<2 )
			throw new IllegalArgumentException("n neets to be greater then 1" );
		
		QuadMeshFactory factory = new QuadMeshFactory();
		
		
		double [][][] coords = new double [5][2*N-1][];
		
		for( int i=0; i<N; i++) {
			double alpha = Math.PI/2 * i / (N-1);
			double beta  = Math.asin( -  Math.cos(alpha) / ( Math.cos(alpha)+1));
			//System.out.println( alpha + " " + beta );
			
			coords[0][i] = new double[]{ -0.5-Math.cos(alpha), -Math.sin(alpha), 0 };
			coords[1][i] = new double[]{  0.5+Math.sin(beta),   0,  Math.cos(beta) };
			coords[2][i] = new double[]{ -0.5-Math.cos(alpha),  Math.sin(alpha), 0 };
			coords[3][i] = new double[]{  0.5+Math.sin(beta),   0, -Math.cos(beta) };
			coords[4][i] = coords[0][i];
			
			if( i > N-2 ) continue;
			coords[3][2*N-2-i] = new double[]{  0.5+Math.cos(alpha), 0,-Math.sin(alpha) };
			coords[2][2*N-2-i] = new double[]{ -0.5-Math.sin(beta),   Math.cos(beta), 0 };
			coords[1][2*N-2-i] = new double[]{  0.5+Math.cos(alpha), 0, Math.sin(alpha) };
			coords[0][2*N-2-i] = new double[]{ -0.5-Math.sin(beta),  -Math.cos(beta), 0 };
			coords[4][2*N-2-i] = coords[0][2*N-2-i];
		}
		
		factory.setVLineCount(5);
		factory.setULineCount(2*N-1);
		
		
		factory.setVertexCoordinates(coords);
		
		factory.setGenerateFaceNormals(true);
		factory.setGenerateTextureCoordinates(true);
		
		factory.update();
		
		return factory.getIndexedFaceSet();
	}
	
	public static void main(String[] args) {
				
//		try {
//			WriterOBJ.write(factory.getIndexedFaceSet(), new FileOutputStream("/tmp/Oloid200.obj"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		ViewerApp.display( createOloid( 50 ) );

	}

}
