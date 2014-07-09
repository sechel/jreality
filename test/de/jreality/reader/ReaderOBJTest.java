package de.jreality.reader;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;

public class ReaderOBJTest {

	public static double delta = 1E-10;
	
	@Test
	public void testPointSetRead() throws IOException{
		URL url = this.getClass().getResource("points.obj");
        ReaderOBJ reader = new ReaderOBJ();
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 0,0,0,0,1,0,1,0,0,1,1,0 };
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
	}
	
	@Test
	public void testSignedPointSetRead() throws IOException{
		URL url = this.getClass().getResource("signs.obj");
        ReaderOBJ reader = new ReaderOBJ();
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 1, -1, 1E-10 };
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
	}
	
	@Test
	public void testSignedSmallEPointSetRead() throws IOException{
		URL url = this.getClass().getResource("signs_e.obj");
        ReaderOBJ reader = new ReaderOBJ();
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 1, -1, 0.1 };
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
	}
	
	@Test
	public void testQuad2DRead() throws IOException{
		URL url = this.getClass().getResource("quad_2d.obj");
        ReaderOBJ reader = new ReaderOBJ();
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 0,0,0,0,1,0,1,0,0,1,1,0 };
        
        
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
        
        int[] faces = {0,1,2,3};
        Assert.assertArrayEquals(faces, ifs.getFaceAttributes(Attribute.INDICES).toIntArray(null));
        
        int[][] edges = {{0,1},{1,2},{2,3},{3,0}};
        int[][] edgesIFS = ifs.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
        
        Assert.assertEquals(edges.length, edgesIFS.length);
        
        for(int i = 0; i < edges.length; ++i) {
        	Arrays.sort(edges[i]);
        	Arrays.sort(edgesIFS[i]);
        }
        Comparator<int[]> cmp = new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				for(int i = 0; i < o1.length; ++i) {
					if(o1[i] != o2[i]) {
						return o1[i] - o2[i];
					}
				}
				return 0;
			}};
        Arrays.sort(edges,cmp);
        Arrays.sort(edgesIFS, cmp);
        
        for(int i = 0; i < edges.length; ++i) {
        	Assert.assertArrayEquals(edges[i], edgesIFS[i]);
        }
	}
	
	@Test
	public void testUseAllRead() throws IOException{
		URL url = this.getClass().getResource("tri_4verts.obj");
        ReaderOBJ reader = new ReaderOBJ();
        reader.setUseAllVertices(true);
        reader.setGenerateEdgesFromFaces(true);
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 0,0,0,0,1,0,1,0,0,1,1,0 };
        
        
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
        
        int[] faces = {0,1,2};
        Assert.assertArrayEquals(faces, ifs.getFaceAttributes(Attribute.INDICES).toIntArray(null));
        
        int[][] edges = {{0,1},{1,2},{2,0}};
        int[][] edgesIFS = ifs.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
        
        Assert.assertEquals(edges.length, edgesIFS.length);
        
        for(int i = 0; i < edges.length; ++i) {
        	Arrays.sort(edges[i]);
        	Arrays.sort(edgesIFS[i]);
        }
        Comparator<int[]> cmp = new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				for(int i = 0; i < o1.length; ++i) {
					if(o1[i] != o2[i]) {
						return o1[i] - o2[i];
					}
				}
				return 0;
			}};
        Arrays.sort(edges,cmp);
        Arrays.sort(edgesIFS, cmp);
        
        for(int i = 0; i < edges.length; ++i) {
        	Assert.assertArrayEquals(edges[i], edgesIFS[i]);
        }
	}
	
	@Test
	public void testUseAllVerticesFalseRead() throws IOException{
		URL url = this.getClass().getResource("tri_4verts.obj");
        ReaderOBJ reader = new ReaderOBJ();
        
        reader.setUseAllVertices(false);
        reader.setGenerateEdgesFromFaces(true);
        
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 0,0,0,0,1,0,1,0,0 };
        
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
        
        int[] faces = {0,1,2};
        Assert.assertArrayEquals(faces, ifs.getFaceAttributes(Attribute.INDICES).toIntArray(null));
        
        int[][] edges = {{0,1},{1,2},{2,0}};
        int[][] edgesIFS = ifs.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
        
        testIntArrayArray(edges, edgesIFS);
	}
	
	@Test
	public void testUseAllVerticesFalseGenerateEdgesFalseRead() throws IOException{
		URL url = this.getClass().getResource("tri_4verts.obj");
        ReaderOBJ reader = new ReaderOBJ();
        
        reader.setUseAllVertices(false);
        reader.setGenerateEdgesFromFaces(false);
        
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 0,0,0,0,1,0,1,0,0 };
        
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
        
        int[] faces = {0,1,2};
        Assert.assertArrayEquals(faces, ifs.getFaceAttributes(Attribute.INDICES).toIntArray(null));
        
        Assert.assertNull(ifs.getEdgeAttributes(Attribute.INDICES));
	}
	
	@Test
	public void testLinesAndFacesGenerateEdgesFalseRead() throws IOException{
		URL url = this.getClass().getResource("trilines.obj");
        ReaderOBJ reader = new ReaderOBJ();
        
        reader.setUseAllVertices(false);
        reader.setGenerateEdgesFromFaces(false);
        
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 0,0,0,0,1,0,1,0,0 };
        
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
        
        int[] faces = {0,1,2};
        Assert.assertArrayEquals(faces, ifs.getFaceAttributes(Attribute.INDICES).toIntArray(null));
        
        int[][] edges = {{0,1},{1,2},{2,3}};
        int[][] edgesIFS = ifs.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
        
        testIntArrayArray(edges, edgesIFS);
	}
	
	@Test
	public void testUseMultipleTexAndNormalsTrueRead() throws IOException{
		URL url = this.getClass().getResource("multiTex.obj");
        ReaderOBJ reader = new ReaderOBJ();
        
        reader.setUseMultipleTexAndNormalCoords(true);
        
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[][] vertices = { {0,0,0},{0,1,0},{1,0,0},{1,1,0},{0,1,0},{1,0,0} };
        double[][] verticesIFS = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
        testVertexCoordinates(vertices, verticesIFS);
        
        int[][] faces = {{0,1,2},{3,4,5}};
        int[][] facesIFS = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
        testIntArrayArray(faces, facesIFS);
        
        int[][] edges = {{0,1},{1,2},{2,0},{3,4},{4,5},{3,5}};
        int[][] edgesIFS = ifs.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
        
        testIntArrayArray(edges, edgesIFS);
	}

	@Test
	public void testUseMultipleTexAndNormalsFalseRead() throws IOException{
		URL url = this.getClass().getResource("multiTex.obj");
        ReaderOBJ reader = new ReaderOBJ();
        
        reader.setUseMultipleTexAndNormalCoords(false);
        
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[][] vertices = { {0,0,0},{0,1,0},{1,0,0},{1,1,0}};
        double[][] verticesIFS = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
        testVertexCoordinates(vertices, verticesIFS);
        
        int[][] faces = {{0,1,2},{1,2,3}};
        int[][] facesIFS = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
        testIntArrayArray(faces, facesIFS);
        
        int[][] edges = {{0,1},{1,2},{2,0},{2,3},{3,1}};
        int[][] edgesIFS = ifs.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
        
        testIntArrayArray(edges, edgesIFS);
	}

	private void testVertexCoordinates(double[][] vertices, double[][] verticesIFS) {
		Assert.assertEquals(vertices.length, verticesIFS.length);
		
		Comparator<double[]> cmp = new Comparator<double[]>() {
			@Override
			public int compare(double[] o1, double[] o2) {
				for(int i = 0; i < o1.length; ++i) {
					if(o1[i] != o2[i]) {
						return Double.compare(o1[i], o2[i]);
					}
				}
				return 0;
			}};
			
        Arrays.sort(vertices, cmp);
        Arrays.sort(verticesIFS,cmp);
        for(int i = 0; i < vertices.length; ++i) {
        	Assert.assertArrayEquals(vertices[i], verticesIFS[i], delta);
        }
	}
	
	

	private void testIntArrayArray(int[][] edges, int[][] edgesIFS) {
		Assert.assertEquals(edges.length, edgesIFS.length);
        
        for(int i = 0; i < edges.length; ++i) {
        	Arrays.sort(edges[i]);
        	Arrays.sort(edgesIFS[i]);
        }
        Comparator<int[]> cmp = new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				for(int i = 0; i < o1.length; ++i) {
					if(o1[i] != o2[i]) {
						return o1[i] - o2[i];
					}
				}
				return 0;
			}};
        Arrays.sort(edges,cmp);
        Arrays.sort(edgesIFS, cmp);
        
        for(int i = 0; i < edges.length; ++i) {
        	Assert.assertArrayEquals(edges[i], edgesIFS[i]);
        }
	}
}
