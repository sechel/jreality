/*
 * Created on 28-Feb-2005
 *
 * This file is part of the jReality package.
 *
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.reader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.*;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;

/**
 *
 * simple reader for the OBJ file format. 
 * 
 * smoothening group support is poor (what is a smoothining group?)
 * 
 * @author weissman
 *
 */
public class ReaderOBJ extends AbstractReader {

  private HashMap materials = new HashMap();
  private HashMap groups = new HashMap();
  private List v, vNorms, vTexs, currentGroups;

  public ReaderOBJ() {
    v = new ArrayList(1000);
    vNorms = new ArrayList(1000);
    vTexs = new ArrayList(1000);
    currentGroups = new LinkedList();
    currentGroups.add("default");
    groups.put("default", new Group("default"));
    root = new SceneGraphComponent();
    root.setAppearance(ParserMTL.createDefault());
  }

  public void setInput(Input input) throws IOException {
    super.setInput(input);
    load();
  }

  private StreamTokenizer globalSyntax(StreamTokenizer st) {
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

  private StreamTokenizer filenameSyntax(StreamTokenizer st) {
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

  private void load() throws IOException {
    StreamTokenizer st = new StreamTokenizer(input.getReader());
    globalSyntax(st);
    while (st.nextToken() != StreamTokenizer.TT_EOF) {
      if (st.ttype == StreamTokenizer.TT_WORD) {
        String word = st.sval;
        if (word.equalsIgnoreCase("v")) { // vertex
          addVertex(st);
          continue;
        }
        if (word.equalsIgnoreCase("vp")) { // vertex parameter data
          ignoreTag(st);
          continue;
        }
        if (word.equalsIgnoreCase("vn")) { // vertex normal
          addVertexNormal(st);
          continue;
        }
        if (word.equalsIgnoreCase("vt")) { // vertex texture coordinate
          addVertexTextureCoordinate(st);
          continue;
        }
        if (word.equalsIgnoreCase("g")) { // grouping
          addGroup(st);
          continue;
        }
        if (word.equalsIgnoreCase("s")) { // smoothening group
          setSmootheningGroup(st);
          continue;
        }
        if (word.equalsIgnoreCase("p")) { // polygon v1 v2 v3 ...
          ignoreTag(st);
          continue;
        }
        if (word.equalsIgnoreCase("l")) { // lines v1/vt1 v2/vt2 ...
          ignoreTag(st);
          continue;
        }
        if (word.equalsIgnoreCase("f")) { // facet v1/vt1/vn1 v2/vt2/vn2 ...
          addFace(st);
          continue;
        }
        if (word.equalsIgnoreCase("mtllib")) { // facet v1/vt1/vn1 v2/vt2/vn2 ...
          addMaterial(st);
          continue;
        }
        if (word.equalsIgnoreCase("usemtl")) { // facet v1/vt1/vn1 v2/vt2/vn2 ...
          setCurrentMaterial(st);
          continue;
        }
        LoggingSystem.getLogger(this).fine("unhandled tag: " + word);
        while (st.nextToken() != StreamTokenizer.TT_EOL) {
          if (st.ttype == StreamTokenizer.TT_NUMBER)
            LoggingSystem.getLogger(this).fine("" + st.nval);
          else
            LoggingSystem.getLogger(this).fine(st.sval);
        }
        LoggingSystem.getLogger(this).fine("unhandled tag: " + word + " end");
      }
    }
    for (Iterator i = groups.values().iterator(); i.hasNext();) {
      Group g = (Group) i.next();
      if (g.hasGeometry())
        root.addChild(g.createComponent());
      else
        LoggingSystem.getLogger(this).fine(
            "Ignoring group " + g.name + " [has no geometry]");
    }
  }

  private boolean smoothShading = false;

  private void setSmootheningGroup(StreamTokenizer st) throws IOException {
    st.nextToken();
    if (st.ttype == StreamTokenizer.TT_NUMBER) {
      smoothShading = (st.nval > 0);
    }
    if (st.ttype == StreamTokenizer.TT_WORD) {
      if ("off".equals(st.sval)) smoothShading = false;
    }
    while (st.nextToken() != StreamTokenizer.TT_EOL)
      ;
    for (Iterator i = currentGroups.iterator(); i.hasNext();) {
      //System.out.println("Appearance ["+matName+"]: "+materials.get(matName));
      Group current = (Group) (groups.get(i.next()));
      current.setSmoothening(smoothShading);
    }
  }

  private void ignoreTag(StreamTokenizer st) throws IOException {
    while (st.nextToken() != StreamTokenizer.TT_EOL)
      ;
  }

  private void addVertex(StreamTokenizer st) throws IOException {
    double[] coords = new double[3];
    coords[0]=ParserUtil.parseNumber(st);
    coords[1]=ParserUtil.parseNumber(st);
    coords[2]=ParserUtil.parseNumber(st);
    v.add(coords);
  }
  
  private void addVertexTextureCoordinate(StreamTokenizer st)
      throws IOException {
    double[] coords = new double[2];
    coords[0]=ParserUtil.parseNumber(st);
    coords[1]=ParserUtil.parseNumber(st);
    vTexs.add(coords);
  }

  private void addVertexNormal(StreamTokenizer st) throws IOException {
    double[] coords = new double[3];
    coords[0]=ParserUtil.parseNumber(st);
    coords[1]=ParserUtil.parseNumber(st);
    coords[2]=ParserUtil.parseNumber(st);
    vNorms.add(coords);
  }

  private void addMaterial(StreamTokenizer st) throws IOException {
    filenameSyntax(st);
    while (st.nextToken() != StreamTokenizer.TT_EOL) {
      String fileName = st.sval;
      if (fileName == null) continue;
      try {
        List app = ParserMTL.readAppearences(input
            .resolveInput(fileName));
        for (Iterator i = app.iterator(); i.hasNext(); ) {
          Appearance a = (Appearance) i.next();
          materials.put(a.getName(), a);
        }
      } catch (FileNotFoundException fnfe) {
        LoggingSystem.getLogger(this).info(
            "couldnt find material file: " + fileName);
      }
    }
    globalSyntax(st);
  }

  private Appearance currMat = ParserMTL.createDefault();

  private void setCurrentMaterial(StreamTokenizer st) throws IOException {
    while (st.nextToken() != StreamTokenizer.TT_EOL) {
      String matName = st.sval;
      currMat = (Appearance) materials.get(matName);
      if (currMat == null)
        System.err.println("Warning: " + matName + " [Material name] is null");
      else
        for (Iterator i = currentGroups.iterator(); i.hasNext();) {
          Group current = (Group) (groups.get(i.next()));
          current.setMaterial(currMat);
        }
    }
  }

  private int[][] temp = new int[3][1000];
  {
    for (int i = 0; i < 1000; i++) {
      temp[0][i]=-1;
      temp[1][i]=-1;
      temp[2][i]=-1;
    }
      
  }
  int count = 0;
  private void addFace(StreamTokenizer st) throws IOException {
    int ix = 0; // side counter
    int jx = 0; // vertex/vertex-texture/vertex-normal index
    boolean lastWasNumber = false;
    while (st.nextToken() != StreamTokenizer.TT_EOL) {
      if (st.ttype == '/') {
        jx++;
        lastWasNumber = false;
        continue;
      }
      if (st.ttype == StreamTokenizer.TT_NUMBER) {
        if (lastWasNumber) {
          ix++;
          jx = 0;
        }
        //System.out.println("adding ["+jx+"]["+ix+"]="+(int)st.nval);
        if (st.nval > 0)
          temp[jx][ix] = (int) (st.nval - 1);
        else {
          // count backwards
          System.err.println("OBJReader.addFace() negative face");
          temp[jx][ix] = v.size() + (int) st.nval;
        }
        lastWasNumber = true;
      } else {
        System.out.println("unknown tag " + st.sval + " " + st.ttype);
      }
    }
    ix++;
    int[] faceV = new int[ix];
    int[] faceVT = new int[ix];
    int[] faceVN = new int[ix];
    System.arraycopy(temp[0], 0, faceV, 0, ix);
    System.arraycopy(temp[1], 0, faceVT, 0, ix);
    System.arraycopy(temp[2], 0, faceVN, 0, ix);
    
    // clean dirty entries in temp
    for (int i = 0; i < ix; i++) {
      temp[0][i]=-1;
      temp[1][i]=-1;
      temp[2][i]=-1;
    }

    // TODO what means that? adding face to all groups?
    for (Iterator i = currentGroups.iterator(); i.hasNext();) {
      Group g = ((Group) groups.get(i.next()));
      g.addFace(faceV, faceVT, faceVN);
    }
  }

  private void addGroup(StreamTokenizer st) throws IOException {
    // till now only the first groupname gets parsed
    currentGroups.clear();
    st.nextToken();
    String gName = "default";
    if (st.ttype == StreamTokenizer.TT_EOL) {
      LoggingSystem.getLogger(this).fine("Warning: empty group name");
      st.pushBack();
    } else
      gName = st.sval;
    //        System.out.println("adding "+gName+" to current groups. ["+st.nval+","+st.sval+","+st.ttype+"]");
    currentGroups.add(gName);
    if (groups.get(gName) == null) {
      Group g = new Group(gName);
      groups.put(gName, g);
    }
    while (st.nextToken() != StreamTokenizer.TT_EOL) {
    }
  }

  private class Group {

    final List faces;
    final String name;
    final Appearance material;
    boolean smooth;
    
    boolean hasTex, hasNorms;
    
    FaceData fd;

    Group(String name) {
      this.name = name;
      faces = new ArrayList(11);
      material = ParserMTL.createDefault();
      setSmoothening(smoothShading);
      setMaterial(currMat);
      fd=new FaceData();
    }

    void addFace(int[] verts, int[] texs, int[] norms) {
      int[] face = new int[verts.length];
      for (int i = 0; i < verts.length; i++) {
        face[i] = fd.getID(verts[i], texs[i], norms[i]);
      }
      faces.add(face);
    }
    
    void setSmoothening(boolean smoothShading) {
      // TODO: check what smoothening should do...
      if (true) return;
      smooth = smoothShading;
      material.setAttribute(CommonAttributes.POLYGON_SHADER + "."
          + CommonAttributes.SMOOTH_SHADING, smooth);
    }

    public boolean hasGeometry() {
      return faces.size() > 0;
    }

    void setMaterial(Appearance a) {
      if (a == null) {
        System.err.println("Warning: current app==null");
        return;
      }
      List lst = a.getChildNodes();
      for (int ix = 0, num = lst.size(); ix < num; ix++) {
        de.jreality.scene.AppearanceAttribute aa = (de.jreality.scene.AppearanceAttribute) lst
            .get(ix);
        material.setAttribute(aa.getAttributeName(), aa.getValue(), aa
            .getAttributeType());
      }
      setSmoothening(smooth);
    }

    Geometry createGeometry() {
      ArrayList vertices = extractVertices();
      ArrayList vertexTex = extractTexCoords();
      ArrayList vertexNorms = extractNormals();

      IndexedFaceSet ifs = new IndexedFaceSet();

      // data is definitly available
      ifs.setVertexCountAndAttributes(Attribute.COORDINATES,
          StorageModel.DOUBLE3_ARRAY.createReadOnly(vertices.toArray(new double[vertices.size()][])));
      ifs.setFaceCountAndAttributes(Attribute.INDICES,
          StorageModel.INT_ARRAY_ARRAY.createReadOnly(faces.toArray(new int[faces
              .size()][])));

      // check if texture coordinates are available and if size fits
      if (vertexTex != null) {
        ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES,
            StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(
                vertexTex.toArray(new double[vertexTex.size()][])));
          System.err.println("Using TEX coordinates!");
      } if (vertexNorms != null) {
        ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE3_ARRAY.createReadOnly(vertexNorms.toArray(new double[vertexNorms.size()][])));
        System.err.println("Using VERTEX normals!");
      }
      boolean hasVertexNormals = ifs.getVertexAttributes(Attribute.NORMALS) != null;
      System.out.println("hasVertexNormals="+hasVertexNormals);
      if (!hasVertexNormals && smooth) {
        GeometryUtility.calculateAndSetVertexNormals(ifs);
      }
      if (!smooth && !hasVertexNormals) {
        System.err.println("using face normals!");
        GeometryUtility.calculateAndSetFaceNormals(ifs);
      }
      ifs.buildEdgesFromFaces();
      return ifs;
    }

    private ArrayList extractNormals() {
      if (fd.normalId(0) == -1) return null;
      ArrayList list = new ArrayList(fd.size());
      for (int i = 0; i < fd.size(); i++) {
        list.add(i, vNorms.get(fd.normalId(i)));
      }
      return list;
    }

    private ArrayList extractTexCoords() {
      if (fd.texId(0) == -1) return null;
      ArrayList list = new ArrayList(fd.size());
      for (int i = 0; i < fd.size(); i++) {
        list.add(i, vTexs.get(fd.texId(i)));
      }
      return list;
    }

    private ArrayList extractVertices() {
      ArrayList list = new ArrayList(fd.size());
      for (int i = 0; i < fd.size(); i++) {
        list.add(i, v.get(fd.vertexId(i)));
      }
      return list;
    }

    SceneGraphComponent createComponent() {
      SceneGraphComponent ret = new SceneGraphComponent();
      ret.setName(name);
      ret.setAppearance(material);
      ret.setGeometry(createGeometry());
      return ret;
    } 
  }

  /**
   * creates indices for triples of vertex/tex/normal
   */
  private static class FaceData {
    private IdentityHashMap storedData=new IdentityHashMap();
    private ArrayList list=new ArrayList();
    
    private class Triple {
      int v, t, n;
      Triple(int vId, int tId, int nId) {
        v=vId;
        t=tId;
        n=nId;
      }
    }
    
    private FaceData(){ }
    private int idCounter;
    int getID(int vertexIndex, int texIndex, int normalIndex) {
      final String key=vertexIndex+"::"+texIndex+"::"+normalIndex;
      Integer ret = (Integer) storedData.get(key);
      if (ret == null) {
        ret = new Integer(idCounter++);
        storedData.put(key, ret);
        list.add(new Triple(vertexIndex, texIndex, normalIndex));
      }
      return ret.intValue();
    }
    void reset() {
      storedData.clear();
    }
    int size() {
      return storedData.size();
    }
    int vertexId(int id) {
      return ((Triple)list.get(id)).v;
    }
    int texId(int id) {
      return ((Triple)list.get(id)).t;
    }
    int normalId(int id) {
      return ((Triple)list.get(id)).n;
    }
  }
}
