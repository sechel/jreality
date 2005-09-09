package de.jreality.writer;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.IntArray;

/**
 * @author schmies
 * 
 */
public class WriterSTL {

	final static String t1 = "   ";
	final static String t2 = t1+t1;
	
	public static void write( IndexedFaceSet ifs, OutputStream out ) {
		writeSolid( ifs, new PrintWriter( out ));
	}
	
	static void write( PrintWriter out, double [] array, String seperator ) {
		if( array==null || array.length==0) return;
		out.print(array[0]);
		for( int i=1; i<array.length; i++ ) {
			out.print(seperator);
			out.print(array[i]);
		}
	}
	
	public static void writeSolid(  IndexedFaceSet ifs, OutputStream out ) {
		writeSolid( ifs,  new PrintWriter( out ));
	}
	
	static void writeSolid( IndexedFaceSet ifs, PrintWriter out ) {
		
		out.println( "solid" );
		
		double [][] points = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().toDoubleArrayArray(null);
		
		double [][] normals;
		if( ifs.getFaceAttributes( Attribute.NORMALS ) == null ) {
			normals = GeometryUtility.calculateFaceNormals(ifs);
		} else {
			normals = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray().toDoubleArrayArray(null);
		}
		
		DataList indices = ifs.getFaceAttributes(Attribute.INDICES  );
		
		for (int i= 0; i < ifs.getNumFaces(); i++) {
			out.print( "facet normal ");
			write( out, normals[i], " " ); out.println();
			out.println( t1+"outer loop");
			IntArray faceIndices=indices.item(i).toIntArray();
			for (int j= 0; j < faceIndices.size(); j++) {
				out.print( t2+"vertex " );
				write( out, points[faceIndices.getValueAt(j)], " " );
				out.println();			
			}
			out.println( t1+"endloop");
			out.println( t1+"endfacet" );
		}
		
		out.println( "endsolid" );
		out.flush();
	}
	
	public static void write( SceneGraphComponent sgc, OutputStream out ) {
		write( sgc, new PrintWriter( out ));
	}
	
	public static void write( SceneGraphComponent sgc, PrintWriter out ) {
		
		List ifsList = new Vector(0);
		
		SceneGraphComponent flat = GeometryUtility.flatten(sgc);
		
		write( flat.getGeometry(), out, ifsList );
		
		final int noc = flat.getChildComponentCount();
			
		for( int i=0; i<noc; i++ ) {
			SceneGraphComponent child=flat.getChildComponent(i);
			write( child.getGeometry(), out, ifsList );
		}
		
		IndexedFaceSet [] ifs = new IndexedFaceSet[ifsList.size()];
		
		for( int i=0; i<ifs.length; i++ ) {
			ifs[i] = (IndexedFaceSet)ifsList.get(i);
		}
		
		writeSolid( IndexedFaceSetUtility.mergeIndexedFaceSets(ifs), out);
	}
	
	private static void write(Geometry geometry, PrintWriter out, List ifsList) {
		if( !(geometry instanceof IndexedFaceSet) )
			return;
		
		ifsList.add( geometry );
	}

	static public void main( String [] arg ) {
		writeSolid( new IndexedFaceSet(), System.out );
	}
}
