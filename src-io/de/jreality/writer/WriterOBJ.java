package de.jreality.writer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.IntArray;
import de.jreality.util.LoggingSystem;

/**
 * @author schmies
 * 
 */
public class WriterOBJ {

	public static void write( IndexedFaceSet ifs, OutputStream out ) {
		write( ifs, null, new PrintWriter( out ));
	}
	
	static void write( PrintWriter out, double [][] array, String prefix ) {
		if( array==null) return;
		String seperator = " ";
		for( int i=0; i<array.length; i++ ) {
			out.print(prefix);
			out.print( seperator );
			WriterSTL.write(out, array[i], seperator );
			out.println();
		}
	}


	static void write( Geometry geom, String groupName, OutputStream out ) {
		if( geom == null ) return;
		
		if( geom instanceof IndexedFaceSet ) {
			write( (IndexedFaceSet)geom, groupName, out);
		} else {
			 LoggingSystem.getLogger(GeometryUtility.class).log(Level.WARNING, 
					 	"ignoring scene graph component " + groupName );
		}
	}
	
	public static void write( SceneGraphComponent sgc , OutputStream out ) {
		
		SceneGraphComponent flat = GeometryUtility.flatten(sgc);
		
		write( flat.getGeometry(), flat.getName(), out );
		
		final int noc = flat.getChildComponentCount();
			
		for( int i=0; i<noc; i++ ) {
			SceneGraphComponent child=flat.getChildComponent(i);
			write( child.getGeometry(), child.getName(), out );
		}
	}
	
	static void writeFaceIndex( PrintWriter out, int index, boolean hasTexture, boolean hasNormals ) {
		out.print(index+1);
		if( !hasTexture && !hasNormals ) return;
		out.print("/");
		if( hasTexture ) out.print(index+1);
		if( !hasNormals ) return;
		out.print("/");
		out.print(index+1);
	}

	static void write( IndexedFaceSet ifs, String groupName, PrintWriter out ) {
		
		if( groupName != null ) {
			out.println();	
			out.println( "g " + groupName );
		    out.println();
		}

		final double [][] points = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().toDoubleArrayArray(null);
		
        final double [][] normals;
		if( ifs.getVertexAttributes( Attribute.NORMALS ) != null ) {
			normals = ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray().toDoubleArrayArray(null);
		} else {
			normals = null;
		}
		final double [][] texture;
		if( ifs.getVertexAttributes( Attribute.TEXTURE_COORDINATES ) != null ) {
			texture = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray().toDoubleArrayArray(null);
		} else {
			texture = null;
		}
		
		write( out, points, "v" );
		write( out, texture, "vt" );
		write( out, normals, "vn" );

		out.println();

		DataList indices = ifs.getFaceAttributes(Attribute.INDICES  );
		
		for (int i= 0; i < ifs.getNumFaces(); i++) {
			out.print( "f  ");
			IntArray faceIndices=indices.item(i).toIntArray();
			writeFaceIndex( out, faceIndices.getValueAt(0), texture!=null, normals!=null );	
			for (int j= 1; j < faceIndices.size(); j++) {
				out.print( " " );
				writeFaceIndex( out, faceIndices.getValueAt(j), texture!=null, normals!=null );
			}

			out.println();	
		}

		out.flush();
	}
	
	static public void main( String [] arg ) {
		write( new CatenoidHelicoid(10), System.out );

        try {
			FileOutputStream stream = new FileOutputStream("/tmp/gaga.obj");
			write( new CatenoidHelicoid(10), stream );
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
