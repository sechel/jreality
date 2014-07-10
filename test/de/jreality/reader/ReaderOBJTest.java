package de.jreality.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.reader.OBJModel.Vertex;
import de.jreality.reader.OBJModel.VertexData;
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
        reader.setGenerateEdgesFromFaces(true);
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 0,0,0,0,1,0,1,0,0};
        
        
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
        
        reader.setGenerateEdgesFromFaces(false);
        
        reader.read(url);
        SceneGraphComponent sgc = reader.getComponent();
        IndexedFaceSet ifs = (IndexedFaceSet) sgc.getChildComponent(0).getGeometry();
        double[] vertices = { 0,0,0,0,1,0,1,0,0,1,1,0 };
        
        Assert.assertArrayEquals(vertices, ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null), delta);
        
        int[] faces = {0,1,2};
        Assert.assertArrayEquals(faces, ifs.getFaceAttributes(Attribute.INDICES).toIntArray(null));
        
        int[][] edges = {{0,1,2,3}};
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
        
        double[][] texCoords = {{0,0,0},{0,1,0},{1,0,0},{0,0,0},{0,1,0},{1,0,0}};
        double[][] texIFS = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(null);
        testDoubleArrayArray(texCoords,texIFS);
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
        
        double[][] texture = {{0,0,0},{0,1,0},{1,0,0},{1,0,0}};
        double[][] textureIFS = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(null);
        testDoubleArrayArray(texture, textureIFS);
	}
	
	@Test
	public void testCompareIndex() {
		Assert.assertEquals(OBJModel.VertexData.compareIndex(0,1), 0);
		Assert.assertEquals(OBJModel.VertexData.compareIndex(1,1), 0);
		Assert.assertEquals(OBJModel.VertexData.compareIndex(1,0), 0);
		Assert.assertEquals(OBJModel.VertexData.compareIndex(1,2), -1);
		Assert.assertEquals(OBJModel.VertexData.compareIndex(15,12), 3);
	}

	@Test
	public void testGetID_MergeTextureAndNormals() {
		List<Vertex> points = new LinkedList<Vertex>();
		Vertex v1 = new Vertex(1,1,1);
		points.add(v1);
		Vertex v2 = new Vertex(1,0,0);
		points.add(v2);
		Vertex v3 = new Vertex(1,2,0);
		points.add(v3);
		Vertex v4 = new Vertex(2,2,0);
		points.add(v4);
		
		VertexData vd = new VertexData(points,null,null,false);
		Assert.assertEquals(0,vd.getId(v1));
		Assert.assertEquals(0,vd.getId(v2));
		Assert.assertEquals(0,vd.getId(v3));
		Assert.assertEquals(1,vd.getId(v4));
	}
	
	@Test
	public void testGetID_SplitTextureAndNormals() {
		List<Vertex> points = new LinkedList<Vertex>();
		Vertex v1 = new Vertex(1,1,1);
		points.add(v1);
		Vertex v2 = new Vertex(1,0,0);
		points.add(v2);
		Vertex v3 = new Vertex(1,2,0);
		points.add(v3);
		Vertex v4 = new Vertex(2,2,0);
		points.add(v4);
		
		VertexData vd = new VertexData(points,null,null,true);
		Assert.assertEquals(0,vd.getId(v1));
		Assert.assertEquals(1,vd.getId(v2));
		Assert.assertEquals(2,vd.getId(v3));
		Assert.assertEquals(3,vd.getId(v4));
	}
	
	@Test
	public void testParseVertex_TN() throws IOException {
		Vertex v = new Vertex(1,2,3);
		String str = v.toString();
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		Vertex nv = ReaderOBJ.parseVertex(st);
		Assert.assertTrue(v.equalIndices(nv));
	}
	
	@Test
	public void testParseVertex_T() throws IOException {
		Vertex v = new Vertex(1,2,0);
		String str = v.toString();
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		Vertex nv = ReaderOBJ.parseVertex(st);
		Assert.assertTrue(v.equalIndices(nv));
	}
	
	@Test
	public void testParseVertex_N() throws IOException {
		Vertex v = new Vertex(1,0,2);
		String str = v.toString();
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		Vertex nv = ReaderOBJ.parseVertex(st);
		Assert.assertTrue(v.equalIndices(nv));
	}
	
	@Test
	public void testParseVertex() throws IOException {
		Vertex v = new Vertex(1,2,3);
		String str = v.toString();
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		Vertex nv = ReaderOBJ.parseVertex(st);
		Assert.assertTrue(v.equalIndices(nv));
	}
	
	@Test
	public void testParseLine_TN() throws IOException {
		Vertex v1 = new Vertex(1,1,1);
		Vertex v2 = new Vertex(2,2,2);
		Vertex v3 = new Vertex(3,3,3);
		List<Vertex> list = new LinkedList<Vertex>();
		list.add(v1);
		list.add(v2);
		list.add(v3);
		String str = "1/1/1 2/2/2 3/3/3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		List<Vertex> line = ReaderOBJ.parseVertexList(st);
		int i = 0;
		for(Vertex v : line) {
			Assert.assertTrue(v.equalIndices(list.get(i++)));
		}
	}
	
	@Test
	public void testParseLine_T() throws IOException {
		Vertex v1 = new Vertex(1,1,0);
		Vertex v2 = new Vertex(2,2,0);
		Vertex v3 = new Vertex(3,3,0);
		List<Vertex> list = new LinkedList<Vertex>();
		list.add(v1);
		list.add(v2);
		list.add(v3);
		String str = "1/1 2/2 3/3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		List<Vertex> line = ReaderOBJ.parseVertexList(st);
		int i = 0;
		for(Vertex v : line) {
			Assert.assertTrue(v.equalIndices(list.get(i++)));
		}
	}
	
	@Test
	public void testParseLine_N() throws IOException {
		Vertex v1 = new Vertex(1,0,1);
		Vertex v2 = new Vertex(2,0,2);
		Vertex v3 = new Vertex(3,0,3);
		List<Vertex> list = new LinkedList<Vertex>();
		list.add(v1);
		list.add(v2);
		list.add(v3);
		String str = "1//1 2//2 3//3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		List<Vertex> line = ReaderOBJ.parseVertexList(st);
		int i = 0;
		for(Vertex v : line) {
			Assert.assertTrue(v.equalIndices(list.get(i++)));
		}
	}
	
	@Test
	public void testParseLine() throws IOException {
		Vertex v1 = new Vertex(1,0,0);
		Vertex v2 = new Vertex(2,0,0);
		Vertex v3 = new Vertex(3,0,0);
		List<Vertex> list = new LinkedList<Vertex>();
		list.add(v1);
		list.add(v2);
		list.add(v3);
		String str = "1 2 \\ 3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		List<Vertex> line = ReaderOBJ.parseVertexList(st);
		int i = 0;
		for(Vertex v : line) {
			Assert.assertTrue(v.equalIndices(list.get(i++)));
		}
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
	
	private void testDoubleArrayArray(double[][] coords, double[][] coordsIFS) {
		Assert.assertEquals(coords.length, coordsIFS.length);
        
        for(int i = 0; i < coords.length; ++i) {
        	Assert.assertArrayEquals(coords[i], coordsIFS[i],1E-10);
        }
	}

	private StreamTokenizer createOBJStreamTokenizer(Reader r) {
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.eolIsSignificant(true);
		st.wordChars('0', '9');
		st.wordChars('A', 'Z');
		st.wordChars('a', 'z');
		st.wordChars('_', '_');
		st.wordChars('.', '.');
		st.wordChars('-', '-');
		st.wordChars('+', '+');
		st.wordChars('\u00A0', '\u00FF');
		st.whitespaceChars('\u0000', '\u0020');
		st.commentChar('#');
		st.ordinaryChar('/');
		st.parseNumbers();
		return st;
	}
	
	
}
