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

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.TestCase;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.IntArray;
import de.jreality.ui.viewerapp.ViewerApp;

public class IndexedFaceSetFactoryTest extends TestCase {

	IndexedFaceSetFactory factory;

	static double [] faceNormalse  = new double[] {
	 0,  0,  1,
	-1,  0,  0,
	 1,  0,  0,
	 0,  1,  0,
	-1,  0,  0,
	 0, -1,  0
	 };
	
	static double [] vertices  = new double[] {

	 0,  0,  0,
	 1,  0,  0,
	 1,  1,  0,
	 0,  1,  0,

	 0,  0,  1,
	 1,  0,  1,
	 1,  1,  1,
	 0,  1,  1,

	};

	static int [][] indices = new int [][] {

	{ 0, 1, 2, 3 }, 
	{ 7, 6, 5, 4 }, 
	{ 0, 1, 5, 4 }, 
	{ 1, 2, 6, 5 }, 
	{ 2, 3, 7, 6 }, 
	{ 3, 0, 4, 7 }, 

	};

	public void setUp() {
		factory = new IndexedFaceSetFactory();

	}
	
	public void tearDown() {
		
		
	}
	

	public void testWeirdProblemWithEdgeColors()	{
		
		double y = .25, z = 9/24.0, a = .16666;
		double[][] verts = {
				{-1,1,1}, {-1,1,0}, {-1,y,1}, {-1,y,z}, {-a,-a,a},{0,0,0}, {y,-z,1}
		};
		int[][] faceI =  {{2,3,4,6}, {5,4,3,1}};  
		Color[] fc = {new Color(.7f, .7f, 0, .6f), new Color(.5f, 0, .8f,.3f)}; 
		int[][] edgeI = {{1,3},{3,2},{3,4},{4,6}, {4,5}};
		Color edge1 = new Color(1f, 0, 0), edge2 = new Color(0,0,1f);
		Color[] edgeC = {edge2, edge1, edge1, edge1, edge2};
		for (int j = 0; j<2; ++j)	{
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			//ifsf.debug = true;
			ifsf.setVertexCount(verts.length);
			ifsf.setVertexCoordinates(verts);
			ifsf.setFaceCount(faceI.length);
			ifsf.setFaceIndices(faceI);
			ifsf.setFaceColors(fc);
			ifsf.setLineCount(edgeI.length);
			ifsf.setEdgeIndices(edgeI);
			if (j == 1) ifsf.setEdgeColors(edgeC);
			ifsf.setGenerateEdgesFromFaces(false);
			ifsf.setGenerateFaceNormals(true);
			ifsf.update();
			IndexedFaceSet ifs = ifsf.getIndexedFaceSet();
			int n = ifs.getNumEdges();
			System.err.println("IFS edgecount: "+n);
			for (int i = 0; i<n; ++i)	{
				IntArray ia = ifs.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray();
				System.err.print("Edge "+i+":\t");
				for (int k = 0; k<ia.getLength(); ++k)	
					System.err.print(ia.item(k)+"\t");
				System.err.println("");
			}			
			System.err.println("Created ifs "+(j==0 ? "without" : "with")+" edge colors");
		}
	}

	
	public void testBugInitialGetVertexCount() {
		factory.getFaceCount();
	}
	
	public void testFaceLabels()	{
		
		//factory.debug = true;
		
		factory.setVertexCount( vertices.length );
		factory.setVertexCoordinates( vertices );	
		
		factory.setFaceCount( indices.length );
		factory.setFaceIndices( indices );
		factory.setGenerateFaceLabels( true );
		factory.update();
		
		IndexedFaceSet ifs = factory.getIndexedFaceSet();
		
		String [] labels = ifs.getFaceAttributes(Attribute.LABELS).toStringArray(null);
		
		for( int i=0; i<labels.length; i++ ) {
			assertEquals( labels[i], new Integer( i ).toString());
		}
		
		factory.setGenerateFaceLabels( false );
		
		factory.update();
		
		assertEquals( ifs.getFaceAttributes(Attribute.LABELS), null );
		
		labels[0] = "gaga";
		
		factory.setFaceLabels( labels );
		
		factory.update();
		
		labels = ifs.getFaceAttributes(Attribute.LABELS).toStringArray(null);
		
		assertEquals( labels[0],  "gaga" );
		for( int i=1; i<labels.length; i++ ) {
			assertEquals( labels[i], new Integer( i ).toString());
		}
		
		// this should work
		factory.setGenerateFaceLabels( false );
		
		//this should fail
		try {
			factory.setGenerateFaceLabels( true );
		} catch( UnsupportedOperationException e ) {
		}
		
		factory.setFaceLabels( (String[])null );
		factory.setGenerateFaceLabels( true );
	}
	
	public void testFaceColors()	{
		double[][] jitterbugEdgeVerts = new double[][] {{0,0,0,1},{1,0,0,1},{1,1,0,1},{0,1,0,1}};
		int[][] jitterbugSegmentIndices1 = {{0,1},{2,3}}; //,{{{0,1,2,3,0,1},{4,5,6,7,4,5},{8,9,10,11,8,9}};
		int[][] jitterbugFaceIndices = {{0,1,2,3}};
		factory = new IndexedFaceSetFactory();
		//factory.debug = true;
		factory.setVertexCount(jitterbugEdgeVerts.length);
		factory.setVertexCoordinates(jitterbugEdgeVerts);	
		factory.setFaceCount(1);
		factory.setFaceIndices(jitterbugFaceIndices);	
		factory.setFaceColors(new double[][]{{0,1,0}});
		factory.setGenerateFaceNormals(true);
		factory.setLineCount(jitterbugSegmentIndices1.length);
		factory.setEdgeIndices(jitterbugSegmentIndices1);
		factory.update();
		
		IndexedFaceSet ifs = factory.getIndexedFaceSet();
			
		assertEquals( ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][0], 0, 0);
		assertEquals( ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][1], 1, 0);
		assertEquals( ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][2], 0, 0);
		//System.err.println("Alpha channel is "+borromeanRectFactory.getIndexedFaceSet().getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][3]);
		
		// now we try to change the alpha channel of the face color
		// just to be safe, we don't use the old array but create a new one.
		factory.setFaceColors(new double[][]{{1,0,0}});
		
		factory.update();
		
		assertEquals( 1, ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][0], 0);
		assertEquals( 0, ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][1], 0);
		assertEquals( 0, ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][2], 0);
		
		//System.err.println("Alpha channel is "+borromeanRectFactory.getIndexedFaceSet().getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][3]);
		
		//ViewerApp.display(factory.getIndexedFaceSet());
	}
	
	public void testGenerateEdgesFromFaces() {
		//factory.debug = true;
		factory.setVertexCount( vertices.length);
		factory.setVertexCoordinates( vertices );
		factory.setFaceCount( indices.length );
		factory.setFaceIndices( indices );
		
		factory.setGenerateEdgeLabels(true);
		factory.setGenerateEdgesFromFaces( true );
		factory.update();
		
		IndexedFaceSet ifs = factory.getIndexedFaceSet();
		
		int[][] edges = ifs.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
		
		assertEquals( 12, ifs.getNumEdges() );
		
		try {
		factory.setLineCount( edges.length );
		} catch( UnsupportedOperationException e ) {	
		}
		assertEquals( 12, ifs.getNumEdges() );
		factory.update();
		
		assertEquals( 12, ifs.getNumEdges() );
		
		try {
			factory.setEdgeIndices( edges );
		} catch( UnsupportedOperationException e ) {		
		}
		
		factory.update();
		
		assertEquals( 12, ifs.getNumEdges() );
		
		factory.setGenerateEdgesFromFaces( false );
		
		factory.update();
		
		assertEquals( 0, ifs.getNumEdges() );
		
		factory.setGenerateEdgesFromFaces( true );		
		factory.setGenerateEdgesFromFaces( false );
		
		factory.update();
		
		assertEquals( 0, ifs.getNumEdges() );
		
		factory.setGenerateEdgesFromFaces( true );		
		
		factory.update();
		
		assertEquals( 12, ifs.getNumEdges() );
		
		String [] edgeLabels = ifs.getEdgeAttributes(Attribute.LABELS).toStringArray(null);
		assertEquals( 12, edgeLabels.length );
		for( int i=0; i<edgeLabels.length; i++ )
				assertEquals( i+"", edgeLabels[i] );	
		
		factory.setGenerateEdgesFromFaces( false );
		
		factory.update();
		
		assertEquals( 0, ifs.getNumEdges() );
		
		factory.setLineCount( edges.length );
		factory.setEdgeIndices( edges );
	}
	
	
	
	
	 public void testStrangeError() {
         IndexedFaceSet ifs = Primitives.cube();
         IndexedFaceSetFactory ifsf=new IndexedFaceSetFactory();
         ifsf.setGenerateEdgesFromFaces(false);
         ifsf.setGenerateFaceNormals(true);
         ifsf.setGenerateVertexNormals(false);
         System.out.println(ifs.getEdgeAttributes());
         //       uebernehmen der Face Atribute:
         ifsf.setFaceCount(6);
         ifsf.setVertexCount(8);
//       ifsf.setLineCount(12);
//       ifsf.setVertexAttributes(ifs.getVertexAttributes());
         ifsf.setGenerateFaceNormals(false);
         ifsf.setFaceAttributes(ifs.getFaceAttributes());
//       ifsf.setEdgeAttributes(ifs.getEdgeAttributes());

         ifsf.setFaceIndices(ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null));

         ifsf.setVertexCoordinates(ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null));
         ifsf.setFaceNormals( (double[])null );
         ifsf.setGenerateFaceNormals(true);
         ifsf.update();
 }

	public static void main( String [] arg ) {

		IndexedFaceSetFactory factory = new IndexedFaceSetFactory();


		factory.setVertexCount( 8 );
		factory.setFaceCount( 6 );	
		factory.setVertexCoordinates( vertices );
		factory.setFaceIndices( indices );
		factory.setGenerateFaceNormals( true );
		factory.setGenerateVertexNormals( true );
		factory.setGenerateEdgesFromFaces( true );
		factory.setGenerateVertexLabels(true);
		factory.setLineCount( 1 );
		factory.setEdgeIndices( new int[][] {{0,1}} );
		factory.setFaceColors( new Color[] {Color.RED, Color.GREEN, Color.RED, Color.GREEN, Color.RED, Color.GREEN })  ;
		factory.update();
		ViewerApp.display(factory.getIndexedFaceSet());
		factory.setFaceColors( new Color[] {Color.RED, Color.YELLOW, Color.RED, Color.YELLOW, Color.RED, Color.YELLOW })  ;
		factory.update();
		ViewerApp.display(factory.getIndexedFaceSet());
	}
}
