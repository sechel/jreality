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
