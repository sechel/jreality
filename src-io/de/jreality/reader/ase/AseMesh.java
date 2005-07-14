/*
 * Copyright (c) 2000,2001 David Yazel, Teseract Software, LLP
 * Copyright (c) 2003-2004, Xith3D Project Group
 * All rights reserved.
 *
 * Portions based on the Java3D interface, Copyright by Sun Microsystems.
 * Many thanks to the developers of Java3D and Sun Microsystems for their
 * innovation and design.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the 'Xith3D Project Group' nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 *
 */
package de.jreality.reader.ase;

//import com.xith3d.scenegraph.*;

import java.util.logging.Level;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.reader.vecmath.Vector3f;
import de.jreality.util.LoggingSystem;


/**
 * The ASE mesh object holds the information for a single mesh.  It is parsed
 * a little differently from other ASE nodes because I don't want to create seperate
 * nodes for every vertex and UV mapping.  So to be a little more efficient the geometry
 * will be stored in big arrays.  Arrays which can be used to populate a geometry array.
 *
 * An unfortunate storage difference makes ASE indexed format different than Java3d
 * indexed format. The normals are stored separate from the vertex list.  This means
 * that for the same vertex, there could be several sets of normals, depending on
 * what face they belong to.
 *
 * @author David Yazel
 *
 */
public class AseMesh extends AseNode {
    final int MAX_MATERIALS = 100;
    public int numVertices = 0;
    public int numFaces = 0;
    public int numTexVertices = 0;
    public int numTexFaces = 0;

    // data storage for mesh
    public float[] vertices = null;
    public int[] faces = null;
    public float[] texVertices = null;
    public int[] texFaces = null;
    public float[] normals = null;
    public int[] normalsIndices = null;
    public float[] faceNormals = null;
    public int[] faceNormalsIndices = null;
    public int[] faceMat = null;
    public int[] totals = null;

    private boolean convertMeshCoordinates = true;

    public AseMesh() {
        properties.put("*MESH_NUMVERTEX", "numVertices");
        properties.put("*MESH_NUMFACES", "numFaces");
        properties.put("*MESH_NUMTVFACES", "numTexFaces");
        properties.put("*MESH_NUMTVERTEX", "numTexVertices");
    }

    /**
     * Utility function which converts the mesh into a java3d triangle array
     */
    public IndexedFaceSet getTriangleArray(int matID, AseMap map) {
        return getTriangleArray(matID, map, new Vector3f(0,0,0));
    }
    
    /**
     * Converts the mesh into a TrangleArray, offsetting the coordinates by the given
     * vector.
     *
     * @param pivotPoint Vector by which the array will be offset
     */
    public IndexedFaceSet getTriangleArray(int matID, AseMap map, Vector3f pivotPoint) {
        try {
            int nFaces;

            if (matID < 0) {
                nFaces = numFaces;
            } else {
                nFaces = totals[matID];
            }
            
            IndexedFaceSet ifs = new IndexedFaceSet();

            LoggingSystem.getLogger(this).log(Level.FINER, "Getting mesh for sub-material " + matID +
                " with " + nFaces + " faces");

            // convert the faces and vertices into a vertex list.  For each face
            // we have 3 vertices, each of which has 3 floats
            double[] coords = new double[nFaces * 3 * 3];
            
            //      float norms[] = new float[nFaces*3*3];
            int n = 0;

	    // Builds the geometry, offsetting it by the passed pivot point
            for (int f = 0; f < numFaces; f++)
                if ((faceMat[f] == matID) || (matID < 0)) {
                    for (int v = 0; v < 3; v++) {
                        if (convertMeshCoordinates) {
                            coords[(((n * 3) + v) * 3) + 0] = vertices[(faces[(f * 3) +
                                v] * 3) + 1] - pivotPoint.x; // x1
                            coords[(((n * 3) + v) * 3) + 1] = vertices[(faces[(f * 3) +
                                v] * 3) + 2] - pivotPoint.y; // y2
                            coords[(((n * 3) + v) * 3) + 2] = vertices[(faces[(f * 3) +
                                v] * 3) + 0] - pivotPoint.z; // z0
                        } else {
                            coords[(((n * 3) + v) * 3) + 0] = vertices[(faces[(f * 3) +
                                v] * 3) + 0] - pivotPoint.x; // x0
                            coords[(((n * 3) + v) * 3) + 1] = vertices[(faces[(f * 3) +
                                v] * 3) + 1] - pivotPoint.y; // y1
                            coords[(((n * 3) + v) * 3) + 2] = vertices[(faces[(f * 3) +
                                v] * 3) + 2] - pivotPoint.z; // z2
                        }
                    }

                    /*
                       v1.sub(b.p.point, a.p.point);
                       v2.sub(c.p.point, a.p.point);
                       faceNormal.cross(v1, v2);
                     */
                    n++;
                }

            if (n != nFaces) {
                throw new Error("Invalid number of vertices initialized");
            }

            ifs.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE3_INLINED.createReadOnly(coords));

            // convert the texture coordinates.  For each face we have
            // three texture vertices, each of which has 2 floats
            if ((texVertices != null) && (texFaces != null) && (map !=null)){
                n = 0;

                double[] texcoords = new double[nFaces * 3 * 2];

                for (int f = 0; f < numFaces; f++)
                    if ((faceMat[f] == matID) || (matID < 0)) {
                        for (int v = 0; v < 3; v++) {
                            texcoords[(((n * 3) + v) * 2) + 0] = texVertices[(texFaces[(f * 3) +
                                v] * 2) + 0] / map.uTiling; // x
                            texcoords[(((n * 3) + v) * 2) + 1] = texVertices[(texFaces[(f * 3) +
                                v] * 2) + 1] / map.vTiling; // y
                        }

                        n++;
                    }

                ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.inlined(2).createReadOnly(texcoords));
            }

            // convert the normals coordinates.  For each face we have
            // three normal vertices, each of which has 3 floats
            if (normals != null) {
                n = 0;

                double[] norms = new double[nFaces * 3 * 3];

                for (int f = 0; f < numFaces; f++)
                    if ((faceMat[f] == matID) || (matID < 0)) {
                        for (int v = 0; v < 3; v++) {
                            if (convertMeshCoordinates) {
                                norms[(((n * 3) + v) * 3) + 0] = -normals[(((f * 3) +
                                    v) * 3) + 2]; // x
                                norms[(((n * 3) + v) * 3) + 1] = normals[(((f * 3) +
                                    v) * 3) + 1]; // y
                                norms[(((n * 3) + v) * 3) + 2] = normals[(((f * 3) +
                                    v) * 3) + 0]; // z
                            } else {
                                norms[(((n * 3) + v) * 3) + 0] = normals[(((f * 3) +
                                    v) * 3) + 0]; // x
                                norms[(((n * 3) + v) * 3) + 1] = normals[(((f * 3) +
                                    v) * 3) + 1]; // y
                                norms[(((n * 3) + v) * 3) + 2] = normals[(((f * 3) +
                                    v) * 3) + 2]; // z
                            }

                            /*
                               if (f==265) {
                                  LoggingSystem.getLogger(this).log(Level.FINER, "Normal for "+f+":"+v+" = "+
                                     norms[(n*3+v)*3+0] + "," +
                                     norms[(n*3+v)*3+1] + "," +
                                     norms[(n*3+v)*3+2]);
                               }
                             */
                        }

                        n++;
                    }

                ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(norms));
            }
            int[] faceIndex = new int[nFaces*3];
            for (int i = 0; i < faceIndex.length; i++) faceIndex[i]=i;
            ifs.setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.inlined(3).createReadOnly(faceIndex));
//            IndexedFaceSet ret = new ReaderGeometryUtilities.TriangulationSimplifier(ifs).getSimplifiedGeometryForFlatTriangulation();
//            IndexedFaceSet ret = new ReaderGeometryUtilities.TriangulationSimplifier(ifs).getSimplifiedGeometry();
//            return ret;
            return ifs;
        } catch (Exception e) {
            e.printStackTrace();
//            Log.log.print(e);
            throw new Error(e);
        }
    }

//    /**
//     * Return a wireframe for the mesh.  This is mostly for debugging than anything.
//     */
//    public Shape3D getWireframe(AseMap map) {
//        Appearance a = new Appearance();
//        PolygonAttributes pa = new PolygonAttributes();
//        pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
//
//        Material m = new Material();
//        m.setAmbientColor(new Color3f(1, 1, 1));
//
//        a.setMaterial(m);
//        a.setPolygonAttributes(pa);
//
//        Shape3D shape = new Shape3D(getTriangleArray(0, map), a);
//
//        return shape;
//    }

    /**
     * Parses a mesh vertex list.  There should be numVertices lines to
     * define this
     */
    public void parseVertexList(AseReader in) {
        vertices = new float[numVertices * 3];

        int n = 0;

        while (in.readAseLine()) {
            if (in.endBlock) {
                break;
            }

            if (!in.key.equalsIgnoreCase("*MESH_VERTEX")) {
                throw new Error("Expecting *MESH_VERTEX at line " + in.lineNo + " instead of " + in.key);
            }

            vertices[n++] = Float.parseFloat(in.params[1]);
            vertices[n++] = Float.parseFloat(in.params[2]);
            vertices[n++] = Float.parseFloat(in.params[3]);
        }

        if ((n / 3) != numVertices) {
            throw new Error("Vertex list does not match declared amount : " +
                in.lineNo);
        }

        in.endBlock = false;
    }

    /**
     * Parses a mesh face list.  There should be numFaces lines to
     * define this
     */
    public void parseFaceList(AseReader in) {
        faces = new int[numFaces * 3];
        faceMat = new int[numFaces];
        totals = new int[MAX_MATERIALS];

        int n = 0;
        int f = 0;

        while (in.readAseLine()) {
            if (in.endBlock) {
                break;
            }

            if (!in.key.equalsIgnoreCase("*MESH_FACE")) {
                throw new Error("Expecting *MESH_FACE at line " + in.lineNo);
            }

            faces[n++] = Integer.parseInt(in.params[2]);
            faces[n++] = Integer.parseInt(in.params[4]);
            faces[n++] = Integer.parseInt(in.params[6]);

            // scan and get the material ID
            int matID = 0;

            for (int i = 0; i < in.numParams; i++) {
                if (in.params[i].equalsIgnoreCase("*MESH_MTLID")) {
                    matID = Integer.parseInt(in.params[i + 1]);

                    //               if (matID>=in.file.materialCount) matID = in.file.materialCount-1;
                    break;
                }
            }

            faceMat[f++] = matID;

            if (matID >= MAX_MATERIALS) {
                throw new Error("Invalid MatID " + matID + " on line " +
                    in.lineNo);
            }

            totals[matID]++;
        }

        if ((n / 3) != numFaces) {
            throw new Error("Face list does not match declared amount : " +
                in.lineNo);
        }

        in.endBlock = false;

        for (int i = 0; i < 10; i++)
            if (totals[i] != 0) {
                LoggingSystem.getLogger(this).log(Level.FINER, "" + totals[i] + " faces use material " + i);
            }
    }

    /**
     * Parses a texture vertex list.  There should be numTexVertices lines to
     * define this
     */
    public void parseTexVertexList(AseReader in) {
        texVertices = new float[numTexVertices * 2];

        int n = 0;

        while (in.readAseLine()) {
            if (in.endBlock) {
                break;
            }

            if (!in.key.equalsIgnoreCase("*MESH_TVERT")) {
                throw new Error("Expecting *MESH_FACE at line " + in.lineNo);
            }

            texVertices[n++] = Float.parseFloat(in.params[1]);
            texVertices[n++] = Float.parseFloat(in.params[2]);
        }

        if ((n / 2) != numTexVertices) {
            throw new Error(
                "Texture vertex list does not match declared amount : " +
                in.lineNo);
        }

        in.endBlock = false;
    }

    /**
     * Parses a mesh tex face list.  There should be numFaces lines to
     * define this
     */
    public void parseTexFaceList(AseReader in) {
        if (numTexFaces != numFaces) {
            throw new Error(
                "Number of tex faces does not equal number of faces " +
                in.lineNo);
        }

        texFaces = new int[numTexFaces * 3];

        int n = 0;

        while (in.readAseLine()) {
            if (in.endBlock) {
                break;
            }

            if (!in.key.equalsIgnoreCase("*MESH_TFACE")) {
                throw new Error("Expecting *MESH_TFACE at line " + in.lineNo);
            }

            texFaces[n++] = Integer.parseInt(in.params[1]);
            texFaces[n++] = Integer.parseInt(in.params[2]);
            texFaces[n++] = Integer.parseInt(in.params[3]);
        }

        if ((n / 3) != numTexFaces) {
            throw new Error("tex face list does not match declared amount : " +
                in.lineNo);
        }

        in.endBlock = false;
    }

    /**
     * Reads in a single normal and its face and put their into the normals and normalIndices arrays
     */
    public void parseNormal(AseReader in, int n) {
        in.readAseLine();

        if (!in.key.equalsIgnoreCase("*MESH_VERTEXNORMAL")) {
            throw new Error("Expecting *MESH_VERTEXNORMAL at line " +
                in.lineNo);
        }

        //      LoggingSystem.getLogger(this).log(Level.FINER, "normal : "+in.params[1]+","+in.params[2]+","+in.params[3]);
	normalsIndices[n/3] = Integer.parseInt(in.params[0]);
        normals[n++] = Float.parseFloat(in.params[1]);
        normals[n++] = Float.parseFloat(in.params[2]);
        normals[n++] = Float.parseFloat(in.params[3]);
    }

    /**
     * Parses a mesh normal list. There is one normal per vertex in each face.
     * This equates to 9 floats per vertex for the normals.
     * Also determines faceNormals and faceNormalsIndices values. There is one faceNormal per face.
     */
    public void parseNormalsList(AseReader in) {
        normals = new float[numFaces * 9];
	normalsIndices = new int[numFaces * 3];	
        faceNormals = new float[numFaces * 3];
        faceNormalsIndices = new int[numFaces];

        int n = 0;

        while (in.readAseLine()) {
            if (in.endBlock) {
                break;
            }

            if (!in.key.equalsIgnoreCase("*MESH_FACENORMAL")) {
                throw new Error("Expecting *MESH_FACENORMAL at line " +
                    in.lineNo);
            }

            faceNormalsIndices[n/9] = Integer.parseInt(in.params[0]);
            faceNormals[n/3]   = Float.parseFloat(in.params[1]);
            faceNormals[n/3+1] = Float.parseFloat(in.params[2]);
            faceNormals[n/3+2] = Float.parseFloat(in.params[3]);

            parseNormal(in, n);
            n += 3;
            parseNormal(in, n);
            n += 3;
            parseNormal(in, n);
            n += 3;
        }

        if ((n / 9) != numFaces) {
            throw new Error("normal list does not match declared amount : " +
                in.lineNo);
        }

        in.endBlock = false;
    }

    /**
     * Override the default parse method because we are going to parse the
     * entire mesh in thos node, rather than recusing into further node types.
     */
    public void parse(AseReader in) {
        this.convertMeshCoordinates = in.convertMeshCoordinates;
        // for this to work, blocks have to open on the same line as the
        // property definition.
        while (in.readAseLine()) {
            if (!parseProperty(in)) {
                // check for the various special types
                if (in.key.equalsIgnoreCase("*MESH_VERTEX_LIST")) {
                    parseVertexList(in);
                } else if (in.key.equalsIgnoreCase("*MESH_FACE_LIST")) {
                    parseFaceList(in);
                } else if (in.key.equalsIgnoreCase("*MESH_TVERTLIST")) {
                    parseTexVertexList(in);
                } else if (in.key.equalsIgnoreCase("*MESH_TFACELIST")) {
                    parseTexFaceList(in);
                } else if (in.key.equalsIgnoreCase("*MESH_NORMALS")) {
                    parseNormalsList(in);
                } else if (in.startBlock) {
                    trashBlock(in);
                }
            }

            if (in.endBlock) {
                break;
            }
        }

        in.endBlock = false;
    }
}
