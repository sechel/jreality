package de.jreality.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;

public class OBJModel {
	
	private static Logger logger = LoggingSystem.getLogger(OBJModel.class.getSimpleName());

	private HashMap<String, Group> groups = new HashMap<String, Group>();
	private List<Group> activeGroups = new LinkedList<Group>();
	private Group defaultGroup = new Group("default");
	private HashMap<String, Appearance> materials = new HashMap<String, Appearance>();

	private List<double[]> vertexCoords = new ArrayList<double[]>();
	private List<double[]> textureCoords = new ArrayList<double[]>();
	private List<double[]> normalCoords = new ArrayList<double[]>();
	
	public OBJModel() {
		groups.put(defaultGroup.getName(), defaultGroup);
		activeGroups.add(defaultGroup);
	}
		
	private class Group {

		private String 
			name = "NN";
		private Appearance 
			material = ParserMTL.createDefault();
		private boolean 
			smooth = false;
		private List<Vertex> 
			points = new ArrayList<Vertex>();
		private List<List<Vertex>> 
			lines = new ArrayList<List<Vertex>>();
		private List<List<Vertex>> 
			faces = new ArrayList<List<Vertex>>();

		public Group(String name) {
			this.name = name;
		}

		public void addAllPoints(List<Vertex> pts) {
			for(Vertex v : pts) {
				points.add(v);
			}
		}
		
		public void addLine(List<Vertex> line) {
			lines.add(line);
		}

		public void addFace(List<Vertex> face) {
			faces.add(face);
		}

		public String getName() {
			return name;
		}

//		void addFace(int[] verts, int[] texs, int[] norms) {
//			int[] face = new int[verts.length];
//			for (int i = 0; i < verts.length; i++) {
//				if(!useMultipleTexAndNormalCoords) {
//					face[i] = vd.getID(verts[i], -1, -1);
//				} else {
//					face[i] = vd.getID(verts[i], texs[i], norms[i]);
//				}
//			}
//			faces.add(face);
//		}
		
		void setSmoothening(boolean smoothShading) {
			// TODO: check what smoothening should do...
			if (true) {
				return;
			}
//			smooth = smoothShading;
//			material.setAttribute(CommonAttributes.POLYGON_SHADER + "."
//					+ CommonAttributes.SMOOTH_SHADING, smooth);
		}

		public boolean hasGeometry() {
			return faces.size() > 0 || lines.size() > 0 || points.size() > 0;
		}

		void setMaterial(Appearance a) {
			if (a == null) {
				logger.warning("Trying to set material appearance to null");
				return;
			}
			Set<String> lst = a.getStoredAttributes();
			for (Iterator<String> i = lst.iterator(); i.hasNext();) {
				String aName = (String) i.next();
				material.setAttribute(aName, a.getAttribute(aName));
			}
			setSmoothening(smooth);
		}

		public Appearance getMaterial() {
			return material;
		}

		public List<List<Vertex>> getLines() {
			return Collections.unmodifiableList(lines);
		}

		public List<Vertex> getPoints() {
			return Collections.unmodifiableList(points);
		}

		public List<List<Vertex>> getFaces() {
			return Collections.unmodifiableList(faces);
		}

	}
	
	protected static class Vertex {
		private int vertexIndex = 0;
		private int textureIndex = 0;
		private int normalIndex = 0;
	
		public Vertex() {}

		public Vertex(int i, int j, int k) {
			vertexIndex = i;
			textureIndex = j;
			normalIndex = k;
		}
		
		public int getVertexIndex() {
			return vertexIndex;
		}
		
		public void setVertexIndex(int vertexIndex) {
			this.vertexIndex = vertexIndex;
		}
		
		public int getTextureIndex() {
			return textureIndex;
		}
		
		public void setTextureIndex(int textureIndex) {
			this.textureIndex = textureIndex;
		}
		
		public int getNormalIndex() {
			return normalIndex;
		}
		
		public void setNormalIndex(int normalIndex) {
			this.normalIndex = normalIndex;
		}
		
		public boolean equalIndices(Vertex v1) {
			return vertexIndex == v1.getVertexIndex() && textureIndex == v1.getTextureIndex() && normalIndex == v1.getNormalIndex();
		}
		
		public String toString() {
			if(textureIndex == 0 && normalIndex == 0) {
				return ""+vertexIndex;
			} else
			if(textureIndex != 0 && normalIndex == 0) {
				return vertexIndex+"/"+textureIndex;
			} else 
			if(textureIndex == 0 && normalIndex != 0) {
				return vertexIndex+"//"+normalIndex;
			} 
			return vertexIndex+"/"+textureIndex +"/"+normalIndex;
		}
	}
	
	/**
	 * creates indices for triples of vertex/tex/normal
	 */
	static class VertexData {
		
		private TreeMap<Vertex, Integer> 
			vertexIndexMap = null;
		
		private ArrayList<Vertex> 
			list = new ArrayList<Vertex>();

		private boolean
			useMultipleTexAndNormalCoords = true;
		
		public VertexData(List<Vertex> points, List<List<Vertex>> lines, List<List<Vertex>> faces, boolean useMultipleTexAndNormalCoords) {
			
			this.useMultipleTexAndNormalCoords = useMultipleTexAndNormalCoords;
			vertexIndexMap = new TreeMap<Vertex, Integer>(new VertexComparator(useMultipleTexAndNormalCoords));
			
			if(points != null) {
				for(Vertex v : points) {
					addVertex(v);
				}
			}
			
			if(lines != null) {
				for(List<Vertex> line : lines) {
					for(Vertex v : line) {
						addVertex(v);
					}
				}
			}
			
			if(faces != null) {
				for(List<Vertex> face : faces) {
					for(Vertex v : face) {
						addVertex(v);
					}
				}
			}
		}

		public int getId(Vertex v) {
			Integer index = vertexIndexMap.get(v); 
			if(useMultipleTexAndNormalCoords) {
				return index;
			} else {
				if(index != null) {
					return index;
				} else {
					return vertexIndexMap.get(new Vertex(v.getVertexIndex(), 0,0));
				}
			}
		}

		public List<Integer> extractVertexIndices() {
			List<Integer> vertexIndices = new ArrayList<Integer>();
			for(Vertex v : list) {
				vertexIndices.add(v.getVertexIndex());
			}
			return vertexIndices;
		}
		
		public List<Integer> extractTextureIndices() {
			List<Integer> textureIndices = new ArrayList<Integer>();
			for(Vertex v : list) {
				textureIndices.add(v.getTextureIndex());
			}
			return textureIndices;		
		}

		public List<Integer> extractNormalIndices() {
			List<Integer> normalIndices = new ArrayList<Integer>();
			for(Vertex v : list) {
				normalIndices.add(v.getNormalIndex());
			}
			return normalIndices;		
		}
		
		private int addVertex(Vertex v) {
			if(useMultipleTexAndNormalCoords) {
				Integer index = vertexIndexMap.get(v);
				if(index == null) {
					return createID(v);
				} else {
					return index;
				}
			} else {
				Integer sameVertexIndex = vertexIndexMap.get(new Vertex(v.getVertexIndex(), 0, 0));
				if(sameVertexIndex != null) {
					Vertex knownVertex = list.get(sameVertexIndex);
					if(mergeableVertices(v, knownVertex)) {
						mergeVertices(knownVertex, v);
						vertexIndexMap.put(knownVertex, sameVertexIndex);
						list.set(sameVertexIndex, knownVertex);
					} else { 
						logger.warning("Discarding normal and texture of vertex "+v+", since vertex "+knownVertex+" is already known.");
					}
					return sameVertexIndex;
				} else {
					return createID(v);
				}
			}
		}

		private int createID(Vertex v) {
			int vIndex = list.size();
			vertexIndexMap.put(v, vIndex);
			list.add(v);
			return vIndex;
		}
		
//		int getID(int vertexIndex, int texIndex, int normalIndex) {
//			Triple newKey = new Triple(vertexIndex, texIndex, normalIndex);
//			Integer index = vertexIndexMap.get(newKey);
//			if (index == null) {
//				index = new Integer(idCounter++);
//				vertexIndexMap.put(newKey, index);
//				list.add(new Triple(vertexIndex, texIndex, normalIndex));
//			} else {
//				Triple oldKey = list.get(index);
//				mergeVertices(oldKey, newKey);
//				vertexIndexMap.put(oldKey, index);
//			}
//			return index.intValue();
//		}

		private class VertexComparator implements Comparator<Vertex> {

			private boolean
				strict = false;
			
			public VertexComparator(boolean strict) {
				this.strict = strict;
			}

			@Override
			public int compare(Vertex o1, Vertex o2) {
				if(strict) {
					return strongCompare(o1, o2);
				} else {
					return weakCompare(o1, o2);
				}
			}

			private int strongCompare(Vertex o1, Vertex o2) {
				int compareV = o1.getVertexIndex() - o2.getVertexIndex();
				int compareT = o1.getTextureIndex() - o2.getTextureIndex();
				int compareN = o1.getNormalIndex() - o2.getNormalIndex();
				if(compareV != 0) {
					return compareV;
				} else if(compareT != 0) {
					return compareT;
				} else if(compareN != 0) {
					return compareN;
				} else {
					return 0;
				}
			}

			private int weakCompare(Vertex o1, Vertex o2) {
				int compareV = compareIndex(o1.getVertexIndex(),o2.getVertexIndex());
				int compareT = compareIndex(o1.getTextureIndex(),o2.getTextureIndex());
				int compareN = compareIndex(o1.getNormalIndex(),o2.getNormalIndex());
				if(compareV != 0) {
					return compareV;
				} else if(compareT != 0) {
					return compareT;
				} else if(compareN != 0) {
					return compareN;
				} else {
					return 0;
				}
			}
		}

		static int compareIndex(int o1, int o2) {
			if(o1 == 0 || o2 == 0) {
				return 0;
			} else {
				return o1-o2;
			}
		}

		boolean mergeVertices(Vertex v1, Vertex v2) {
			
			if( !mergeableVertices(v1, v2) ) {
				return false;
			}
			if(v1.getTextureIndex() == 0) {
				v1.setTextureIndex(v2.getTextureIndex());
			}
			if(v1.getNormalIndex() == 0) {
				v1.setNormalIndex(v2.getNormalIndex());
			}
			return true;
		}

		private boolean mergeableVertices(Vertex v1, Vertex v2) {
			return v1.getVertexIndex() == v2.getVertexIndex() &&
				compareIndex(v1.getTextureIndex(),v2.getTextureIndex()) == 0 &&
				compareIndex(v1.getNormalIndex(),v2.getNormalIndex()) == 0;
		}

		void reset() {
			vertexIndexMap.clear();
		}

		int size() {
			return vertexIndexMap.size();
		}

		int vertexId(int id) {
			return list.get(id).getVertexIndex();
		}

		int texId(int id) {
			return list.get(id).getTextureIndex();
		}

		int normalId(int id) {
			return list.get(id).getNormalIndex();
		}

		public ArrayList<int[]> extractIndicesList(List<List<Vertex>> faces) {
			ArrayList<int[]> faceIndices = new ArrayList<int[]>();
			for(List<Vertex> face : faces) {
				faceIndices.add(extractIndices(face));
			}
			return faceIndices;
		}

		private int[] extractIndices(List<Vertex> face) {
			int[] indices = new int[face.size()];
			int i = 0;
			for(Vertex v : face) {
				indices[i++] = getId(v);
			}
			return indices;
		}
	}

	public void addVertexCoords(double[] coords) {
		vertexCoords.add(coords);
	}

	public void addNormalCoords(double[] n) {
		normalCoords.add(n);
	}

	public void addTextureCoords(double[] tex) {
		textureCoords.add(tex);
	}

	public void setActiveGroups(List<String> groupNames) {
		activeGroups.clear();
		if(groupNames.size() == 0) {
			logger.fine("Empty group statement. Setting default group active.");
			activeGroups.add(defaultGroup);
		} else {
			for(String name : groupNames) {
				Group g = groups.get(name);
				if(g == null) {
					g = new Group(name);
					groups.put(name,g);
				}
				activeGroups.add(g);
			}
		}
	}

	public void addPoints(List<Vertex> points) {
		for(Group g : activeGroups) {
			g.addAllPoints(points);
		}
	}

	public void addLine(List<Vertex> l) {
		for(Group g : activeGroups) {
			g.addLine(l);
		}
	}

	public void addFace(List<Vertex> face) {
		for(Group g : activeGroups) {
			g.addFace(face);
		}
	}

	public void addMaterial(Appearance a) {
		materials.put(a.getName(), a);
	}

	public void useMaterial(String mtlName) {
		Appearance mtl = materials.get(mtlName);
		if (mtl == null) {
			logger.warning("Warning: Unknown material with name [" + mtlName + "].");
		} else {
			for(Group g: activeGroups) {
				g.setMaterial(mtl);
			}
		}
	}

	public List<SceneGraphComponent> getComponents(boolean useMultipleTexAndNormalCoords, boolean generateEdgesFromFaces) {
		List<SceneGraphComponent> cps = new LinkedList<SceneGraphComponent>();
		for (Group g : groups.values()) {
			if (g.hasGeometry()) {
				cps.add(createComponent(g,useMultipleTexAndNormalCoords,generateEdgesFromFaces));
			} else {
				LoggingSystem.getLogger(this).fine("Ignoring group " + g.name + " [has no geometry]");
			}
		}
		return cps;
	}
	
	private SceneGraphComponent createComponent(Group g, boolean useMultipleTexAndNormalCoords, boolean generateEdgesFromFaces) {
		SceneGraphComponent ret = new SceneGraphComponent();
		ret.setName(g.getName());
		ret.setAppearance(g.getMaterial());
		ret.setGeometry(createGeometry(g, useMultipleTexAndNormalCoords,generateEdgesFromFaces));
		return ret;
	}
	
	private Geometry createGeometry(Group g, boolean useMultipleTexAndNormalCoords, boolean generateEdgesFromFaces) {
		
		List<List<Vertex>> lines = g.getLines();
		List<Vertex> points = g.getPoints();
		List<List<Vertex>> faces = g.getFaces();
		VertexData vd = new VertexData(points, lines, faces, useMultipleTexAndNormalCoords);
		
		IndexedFaceSet ifs = new IndexedFaceSet();

		//Vertices
		ArrayList<double[]> vertices = extractCoords(vertexCoords, vd.extractVertexIndices()); 
		ifs.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE3_ARRAY.createReadOnly(vertices.toArray(new double[vertices.size()][])));
		
		//Faces
		ArrayList<int[]> faceIndices = vd.extractIndicesList(faces); 
		ifs.setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(faceIndices.toArray(new int[faceIndices.size()][])));
		
		// check if texture coordinates are available and if size fits
		if(textureCoords.size() != 0) {
			ArrayList<double[]> vertexTex = extractCoords(textureCoords,vd.extractTextureIndices());
			if (vertexTex != null) {
				double[][] vTexArray = new double[vertexTex.size()][];
				vertexTex.toArray(vTexArray);
				int numPerEntry = 2;
				if (vTexArray.length != 0) {
					numPerEntry = vTexArray[0].length;
				}
				ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.array(numPerEntry).createReadOnly(vTexArray));
			}
		}
		
		if(normalCoords.size() != 0) {
			ArrayList<double[]> vertexNorms = extractCoords(normalCoords, vd.extractNormalIndices());
			if (!vertexNorms.isEmpty()) {
					ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE3_ARRAY.createReadOnly(vertexNorms.toArray(new double[vertexNorms.size()][])));
			}
		}	

		boolean hasVertexNormals = ifs.getVertexAttributes(Attribute.NORMALS) != null;
		
		if (!hasVertexNormals) {
			IndexedFaceSetUtility.calculateAndSetVertexNormals(ifs);
		}
		
		if (!hasVertexNormals) {
			IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs);
		}
		
		//Lines
		List<int[]> lineIndices = new ArrayList<int[]>();
		if(lines.size() > 0) {
			lineIndices.addAll(vd.extractIndicesList(lines)); 
		}
		if(faces.size() > 0 && generateEdgesFromFaces) {
			if(lines.size() == 0) {
				IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
			} else {
				int[][] edges = IndexedFaceSetUtility.edgesFromFaces(faceIndices.toArray(new int[faceIndices.size()][])).toIntArrayArray(null);
				for(int i = 0; i < edges.length; ++i) {
					lineIndices.add(edges[i]);
				}
			}
		}
		
		if (lineIndices.size() != 0) {
			ifs.setEdgeCountAndAttributes(Attribute.INDICES,StorageModel.INT_ARRAY_ARRAY.createReadOnly(lineIndices.toArray(new int[lineIndices.size()][])));
		}
		return ifs;
	}
	
	private ArrayList<double[]> extractCoords(List<double[]> coords, List<Integer> indices) {
		ArrayList<double[]> list = new ArrayList<double[]>();
		for (Integer i : indices) {
			if(i == 0) {
				list.add(new double[]{0,0,0});
			} else if(i > 0) {
				list.add(coords.get(i-1));
			} else {
				list.add(coords.get(coords.size()+i));
			}
		}
		return list;
	}
}
