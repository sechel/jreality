/**
 * Copyright (c) 2003, Xith3D Project Group
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
 * Neither the name of the 'Xith3D Project Group' nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
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

package de.jreality.reader.quake3;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;


import de.jreality.math.Matrix;
import de.jreality.reader.AbstractReader;
import de.jreality.reader.quake3.lumps.tBSPFace;
import de.jreality.reader.quake3.lumps.tBSPLeaf;
import de.jreality.reader.quake3.lumps.tBSPVertex;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.reader.vecmath.Vector3f;
import de.jreality.util.*;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphComponentSwitch;

/**
 * Takes the data from a Quake3Loader and converts it into a Xith3d scenegraph.  There is
 * obvisouly many ways to do this, so this can be thought of one way to render a quake map.
 * <p/>
 * Originally Coded by David Yazel on Jan 4, 2004 at 6:39:53 PM.
 */
public class Quake3Converter {

    final static double worldScale = 0.03f;

    private SceneGraphComponent leaf[];         // all the leaves in the BSP file
    private SceneGraphComponent cluster[];      // all the clusters in the BSP file (contains leaves)
    private SceneGraphComponent clusterSwitch;       // massive switch for all the clusters

    private Texture2D lightTextures[];
    private Texture2D textures[];
    private Quake3Loader loader;
    private int numShapes;
    private int numUniqueShapes;
    private int leafToCluster[];
    private int clusterLeaf[];

    private double planes[];
    private int nodes[];
    private BSPLeaf leafs[];
    private SceneGraphComponent faces[];
    private SceneGraphComponentSwitch faceSwitch;
    private BitSet faceBitset;
    private ArrayList[] clusterLeafs;
    private SceneGraphComponent modelBG;

    private Input input;
    
    private SceneGraphComponent root;
    
    public Quake3Converter(Input input) {
      this.input = input;
      root = new SceneGraphComponent();
    }
    
    /**
     * Creates the indexed geometry array for the BSP face.  The lightmap tex coords are stored in
     * unit 1, the regular tex coords are stored in unit 2
     * 
     * @param face 
     * @return 
     */
    private IndexedFaceSet convertToIndexed(tBSPFace face) {
        double[] coords = new double[face.numOfVerts*3];
        double[] texCoords = new double[face.numOfVerts*2];
        double[] lightTexCoords = new double[face.numOfVerts*2];
        double[] colors = new double[face.numOfVerts*4];
        //        double scale = 1f;
        for (int i = 0; i < face.numOfVerts; i++) {
            int j = face.vertexIndex + i;
            coords[3*i] = loader.vertices[j].position.x * worldScale;
            coords[3*i+1] =  loader.vertices[j].position.z * worldScale;
            coords[3*i+2] = -loader.vertices[j].position.y * worldScale;
            //ga.setTexCoord(1, loader.vertices[j].lightTexCoord);
            texCoords[2*i] = loader.vertices[j].texCoord.x;
            texCoords[2*i+1] = loader.vertices[j].texCoord.y;
            lightTexCoords[2*i] = loader.vertices[j].lightTexCoord.x;
            lightTexCoords[2*i+1] = loader.vertices[j].lightTexCoord.y;
            
            colors[4*i] = loader.vertices[j].color.x;
            colors[4*i+1] = loader.vertices[j].color.y;
            colors[4*i+2] = loader.vertices[j].color.z;
            colors[4*i+3] = loader.vertices[j].color.w;
            // if (loader.vertices[j].color.x<0) throw new Error("illegal color + "+loader.vertices[j].color);

//            ga.setColor((double) loader.vertices[j].color.x / 255f, (double) loader.vertices[j].color.y / 255f,
//                    (double) loader.vertices[j].color.z / 255f);
        }
        IndexedFaceSet ret = new IndexedFaceSet();
        ret.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE3_INLINED.createReadOnly(coords));
        ret.setVertexAttributes(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.inlined(2).createReadOnly(texCoords));
        ret.setVertexAttributes(Attribute.attributeForName("lightmap coordinates"), StorageModel.DOUBLE_ARRAY.inlined(2).createReadOnly(lightTexCoords));
        //ret.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.inlined(4).createReadOnly(colors));
        int index[] = new int[face.numMeshVerts];
        System.arraycopy(loader.meshVertices, face.meshVertIndex, index, 0, face.numMeshVerts);
        ret.setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.inlined(3).createReadOnly(index));
        //ret.buildEdgesFromFaces();
        return ret;
    }

    private IndexedFaceSet convertSurfacePatch(tBSPFace face) {
        tBSPVertex control[] = new tBSPVertex[face.numOfVerts];
        for (int i=0;i<face.numOfVerts;i++)
            control[i] = loader.vertices[face.vertexIndex+i];
        PatchSurface ps = new PatchSurface(control,face.numOfVerts,face.size[0],face.size[1]);

        double[] coords = new double[ps.mPoints.length*3];
        double[] texCoords = new double[ps.mPoints.length*2];
        double[] lightTexCoords = new double[ps.mPoints.length*2];
        double[] colors = new double[ps.mPoints.length*4];
        
        for (int i = 0; i < ps.mPoints.length; i++) {
            coords[3*i]=ps.mPoints[i].position.x * worldScale;
            coords[3*i+1]=ps.mPoints[i].position.z * worldScale;
            coords[3*i+2]=-ps.mPoints[i].position.y * worldScale;
            //ga.setTexCoord(1, ps.mPoints[i].lightTexCoord);
            texCoords[2*i] = ps.mPoints[i].texCoord.x;
            texCoords[2*i+1] = ps.mPoints[i].texCoord.y;
            lightTexCoords[2*i] = ps.mPoints[i].lightTexCoord.x;
            lightTexCoords[2*i+1] = ps.mPoints[i].lightTexCoord.y;
//            ga.setColor(ps.mPoints[i].color);
//                    (double) loader.vertices[j].color.z / 255f);
        }
        IndexedFaceSet ret = new IndexedFaceSet();
        ret.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE3_INLINED.createReadOnly(coords));
        ret.setVertexAttributes(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.inlined(2).createReadOnly(texCoords));
        ret.setVertexAttributes(Attribute.attributeForName("lightmap coordinates"), StorageModel.DOUBLE_ARRAY.inlined(2).createReadOnly(lightTexCoords));
        //ret.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.inlined(4).createReadOnly(colors));
        ret.setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.inlined(3).createReadOnly(ps.mIndices));
        //ret.buildEdgesFromFaces();
        return ret;
    }



    /**
     * Converts a BSP face definition into a Shape3D
     * 
     * @param face 
     * @return 
     */
    private SceneGraphComponent convert(tBSPFace face) {
        Geometry ga = null;
        switch (face.type) {

        // regular mesh
        case 1:
            ga = convertToIndexed(face);
            break;
        case 2:
            try {
                ga = convertSurfacePatch(face);
            } catch (Exception e) {
                LoggingSystem.getLogger(this).info("patch failed! ");
                return new SceneGraphComponent();
            }
            break;
        case 3:
            ga = convertToIndexed(face);
            break;
        default:
            return null;

        }

        Appearance a = new Appearance();
        a.setAttribute(CommonAttributes.EDGE_DRAW, false);
        if (face.textureID<0) {
            LoggingSystem.getLogger(this).finer("no texture, skipping");
            return null;
        }

        if (face.lightmapID<0) {

//            TextureAttributes bta = new TextureAttributes();
//            bta.setTextureMode(TextureAttributes.REPLACE);
//            a.setTextureAttributes(bta);
//            a.setTexture(textures[face.textureID]);

//            if (loader.textures[face.textureID].indexOf("flame1side")>=0 ||
//                loader.textures[face.textureID].indexOf("flame1dark")>=0) {
//                bta.setTextureMode(TextureAttributes.MODULATE);
//                TransparencyAttributes ta = new TransparencyAttributes(TransparencyAttributes.BLENDED,0);
//                ta.setSrcBlendFunction(TransparencyAttributes.BLEND_ONE);
//                ta.setDstBlendFunction(TransparencyAttributes.BLEND_ONE);
//                a.setTransparencyAttributes(ta);
//            }
//        } else {
//
//            // build the lightmap texture unit
//            TextureUnitState light = new TextureUnitState();
//            light.setTexture(lightTextures[face.lightmapID]);
//            TextureAttributes lta = new TextureAttributes();
//            lta.setTextureMode(TextureAttributes.COMBINE);
//            lta.setCombineRgbMode(TextureAttributes.COMBINE_MODULATE);
//            lta.setCombineRgbSource(0,TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE);
//            lta.setCombineRgbFunction(0,TextureAttributes.COMBINE_SRC_COLOR);
//
//            lta.setCombineRgbSource(1,TextureAttributes.COMBINE_TEXTURE_COLOR);
//            lta.setCombineRgbFunction(1,TextureAttributes.COMBINE_SRC_COLOR);
//
//            lta.setCombineRgbScale(2);
//
//            light.setTextureAttributes(lta);
//
//            // build the base texture unit
//            TextureUnitState base = new TextureUnitState();
//            base.setTexture(textures[face.textureID]);
//            TextureAttributes bta = new TextureAttributes();
//            bta.setTextureMode(TextureAttributes.REPLACE);
//            base.setTextureAttributes(bta);
//
//            a.setTextureUnitState(new TextureUnitState[]{base, light});
        }
        
        if (textures[face.textureID] != null) a.setAttribute(CommonAttributes.TEXTURE_2D, textures[face.textureID]);
        if (loader.textures[face.textureID].indexOf("flame1side")>=0 ||
                loader.textures[face.textureID].indexOf("flame1dark")>=0) {
            textures[face.textureID].setCombineMode(Texture2D.GL_MODULATE);
        }
        if (face.lightmapID >=0 && lightTextures[face.lightmapID] != null) a.setAttribute("lightMap", lightTextures[face.lightmapID]);
        
        a.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.AMBIENT_COLOR, new Color(1,1,1));
        a.setAttribute(CommonAttributes.LIGHTING_ENABLED, true);

        SceneGraphComponent shape = new SceneGraphComponent();
        shape.setGeometry(ga);
        shape.setAppearance(a);
        return shape;
    }

    private void changeGamma(DirectBufferedImage im, double factor) {

        byte[] pImage = im.getBackingStore();
        int psize = 3;
        if (im.getType() == DirectBufferedImage.DIRECT_RGBA) psize=4;
//  This function was taken from a couple engines that I saw,
// which most likely originated from the Aftershock engine.
// Kudos to them!  What it does is increase/decrease the intensity
// of the lightmap so that it isn't so dark.  Quake uses hardware to
// do this, but we will do it in code.

        int gtable[] = new int[256];
         for (int i=0; i<256; i++)
            {
                double y = (double)(i)/255.0;
                y = Math.pow(y, 1.0/factor);
                gtable[i] = (int) Math.floor(255.0 * y + 0.5);
            }

        // Go through every pixel in the lightmap
        final int size = im.getWidth() * im.getHeight();
        for (int i = 0; i < size; i++) {

            // extract the current RGB values
            int rr = (int) pImage[i * psize + 0];
            int gg = (int) pImage[i * psize + 1];
            int bb = (int) pImage[i * psize + 2];

            if (rr<0) rr= (rr | 1<<8) & 0xff;
            if (gg<0) gg= (gg | 1<<8) & 0xff;
            if (bb<0) bb= (bb | 1<<8) & 0xff;

            pImage[i * psize + 0] = (byte) (gtable[rr]);
            pImage[i * psize + 1] = (byte) (gtable[gg]);
            pImage[i * psize + 2] = (byte) (gtable[bb]);

        }

    }

    /**
     * converts the nodes and planes for the BSP
     */
    private void convertNodes() {

        nodes = new int[loader.nodes.length*3];
        for (int i=0;i<loader.nodes.length;i++) {
            final int j = i*3;
            nodes[j+0] = loader.nodes[i].plane;
            nodes[j+1] = loader.nodes[i].front;
            nodes[j+2] = loader.nodes[i].back;
        }

        // now convert the planes

        planes = new double[loader.planes.length*4];
        for (int i=0;i<loader.planes.length;i++) {
            final int j = i * 4;
//            loader.planes[i].normal.normalize();
            planes[j+0] = loader.planes[i].normal.x;
            planes[j+1] = loader.planes[i].normal.z;
            planes[j+2] = -loader.planes[i].normal.y;
            planes[j+3] = loader.planes[i].d * worldScale;

        }

    }

    /**
     * Builds image components for all the lightmaps.
     */
    private void convertLightMaps() {

        lightTextures = new Texture2D[loader.lightmaps.length];
        LoggingSystem.getLogger(this).finer("Converting "+loader.lightmaps.length+" lightmaps.");
        for (int i = 0; i < loader.lightmaps.length; i++) {
            changeGamma(loader.lightmaps[i],1.2f);
            lightTextures[i] = new Texture2D(loader.lightmaps[i]);
            lightTextures[i].setCombineMode(Texture2D.GL_MODULATE);
        }

    }

    private static Texture2D defTexture;

    private static Texture2D getDefaultTexture() {
        if (defTexture != null) return defTexture;
        BufferedImage im = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = im.getGraphics();
        g.setColor(Color.gray);
        g.fillRect(0, 0, 256, 256);
        g.dispose();
        defTexture = new Texture2D(im);
        return defTexture;
    }

    private void convertTextures() {
        textures = new Texture2D[loader.textures.length];
        for (int i = 0; i < loader.textures.length; i++) {
            String tFull = loader.textures[i];
            String tFile = tFull.substring(tFull.lastIndexOf("/") + 1);
            Input in = null;
            try {
                try {
                    in = input.resolveInput(tFull+".jpg");
                } catch (FileNotFoundException nfe) {
                    in = input.resolveInput(tFile+".jpg");
                }
                textures[i] = new Texture2D(in);
            } catch (IOException ioe) {
                try {
                    try {
                        in = input.resolveInput(tFull+".tga");
                    } catch (FileNotFoundException nfe) {
                        in = input.resolveInput(tFile+".tga");
                    }
                } catch (IOException ioe2){
                    LoggingSystem.getLogger(this).info("couldn't load texture: "+tFile);
                }
                if (in == null) {
                    textures[i] = getDefaultTexture();
                } else {
                    LoggingSystem.getLogger(this).finer("reading tga file: "+in.toString());
                    BufferedImage bufferedImage = TargaFile.getBufferedImage(in.getInputStream());
                    textures[i] = new Texture2D(bufferedImage);
                }
            }
            textures[i].setApplyMode(Texture2D.GL_REPLACE);
        }
    }

    /**
     * Takes all the faces in the leaf and adds them to the cluster
     * 
     * @param leaf 
     */
    private BSPLeaf convertLeaf(tBSPLeaf leaf) {

        if (leaf.numOfLeafFaces == 0) {
            //System.out.println("no faces, but "+leaf.numOfLeafBrushes+" brushes");
            return new BSPLeaf();
        }

        BSPLeaf l = new BSPLeaf();
        l.faces = new int[leaf.numOfLeafFaces];
        for (int i = 0; i < leaf.numOfLeafFaces; i++) {
            l.faces[i] = loader.leafFaces[i+leaf.leafface];
        }
        clusterLeaf[leaf.cluster]++;
        if (clusterLeafs[leaf.cluster]==null)
            clusterLeafs[leaf.cluster] = new ArrayList(20);
        clusterLeafs[leaf.cluster].add(l);

        return l;
//        System.out.println("there are "+smap.size()+" unique textures in "+leaf.numOfLeafFaces+" faces");
    }


    /**
     * Converts the information stored in the loader into a Xith3d scenegraph
     * 
     * @param loader 
     */
    public void convert(Quake3Loader loader) {
        this.loader = loader;

        convertLightMaps();
        convertTextures();

        // create one branch group per cluster

        clusterSwitch = new SceneGraphComponent();
        clusterLeaf = new int[loader.leafs.length];
        cluster = new SceneGraphComponent[loader.visData.numOfClusters];
        for (int i = 0; i < loader.visData.numOfClusters; i++) {
            cluster[i] = new SceneGraphComponent();
            clusterSwitch.addChild(cluster[i]);
        }

        // create all the leaves

        faceBitset = new BitSet(loader.faces.length);
        faceBitset.set(0,loader.faces.length-1);
        faceSwitch = new SceneGraphComponentSwitch();
        faceSwitch.applyMask(faceBitset);
        faces = new SceneGraphComponent[loader.faces.length];
        root.addChild(faceSwitch);

        LoggingSystem.getLogger(this).finer("Converting faces...");
        long st = System.currentTimeMillis();
        for (int i = 0; i < loader.faces.length; i++) {
            faces[i] = convert(loader.faces[i]);
            if (faces[i] != null) {
                faceSwitch.addChild(faces[i]);
            } else {
                faceSwitch.addChild(new SceneGraphComponent());
                LoggingSystem.getLogger(this).finer("adding empty component [needed for working bitmask]");
            }
        }

        /*
        System.out.println("Converting models...");
        modelBG = new SceneGraphComponent();
        for (int i = 1; i < loader.models.length; i++) {
            SceneGraphComponent model = new SceneGraphComponent();
            for (int j=0;j<loader.models[i].numOfFaces;j++) {
                SceneGraphComponent shape = convert(loader.faces[loader.models[i].faceIndex+j]);
                if (shape != null) model.addChild(shape);

            }
            modelBG.addChild(model);
        }
        */

        leafToCluster = new int[loader.leafs.length];
        leafs = new BSPLeaf[loader.leafs.length];
        clusterLeafs = new ArrayList[loader.visData.numOfClusters];
        LoggingSystem.getLogger(this).finer("Converting leafs...");
        for (int i = 0; i < loader.leafs.length; i++) {
            leafs[i] = convertLeaf(loader.leafs[i]);
            leafToCluster[i] = loader.leafs[i].cluster;
        }

        LoggingSystem.getLogger(this).finer("Converting nodes...");
        convertNodes();

//        root.addChild(modelBG);

        // create collision for all the clusters.  Doing it at this level should work well since
        // collision is only aginst live nodes and the cluster switch will only have viable ones
        // set at any one time.

        // default the switch to everyone

        /*
        BitSet bs = new BitSet(loader.visData.bytesPerCluster*8);
        bs.set(0,bs.size());
        clusterSwitch.setChildMask(bs);
        */
//        this.loader = null;
        LoggingSystem.getLogger(this).finer("Shapes = "+numShapes);
        LoggingSystem.getLogger(this).finer("Unique Shapes = "+numUniqueShapes);

        int numLeaves = 0;
        for (int i=0;i<clusterLeaf.length;i++)
            numLeaves += clusterLeaf[i];
        LoggingSystem.getLogger(this).finer("total referenced leaves = "+numLeaves);
        LoggingSystem.getLogger(this).finer("total leaves = "+loader.leafs.length);
        LoggingSystem.getLogger(this).finer("total faces = "+loader.faces.length);
    }

    private Vector3f normal = new Vector3f();

    /**
     * Calculates which cluster the camera position is in
     * @param pos
     * @return
     */
    private int getCluster( Vector3f pos ) {

        int index = 0;

        while (index >= 0) {

            final int node = index * 3;
            final int planeIndex = nodes[node+0]*4;
            normal.x = planes[planeIndex+0];
            normal.y = planes[planeIndex+1];
            normal.z = planes[planeIndex+2];
            double d = planes[planeIndex+3];

            // Distance from point to a plane
            final double distance =
            normal.dot(pos) - d;

            if (distance > 0.0001) {
                index = nodes[node+1];
            } else {
                index = nodes[node+2];
            }
        }

        return leafToCluster[-(index + 1)];

    }

    private boolean isClusterVisible(int visCluster, int testCluster) {

        if ((loader.visData.pBitsets == null) || (visCluster < 0)) {
            return true;
        }

        int i = (visCluster * loader.visData.bytesPerCluster) + (testCluster /8);
        return (loader.visData.pBitsets[i] & (1 << (testCluster & 0x07))) != 0;
    }


    private Vector3f camPos = new Vector3f();
    private int lastCluster = -2;

    public void setVisibility( double[] campos ) {

        camPos = new Vector3f((double)campos[0], (double)campos[1], (double)campos[2]);
        int c = getCluster(camPos);
        if (lastCluster != c) {
            LoggingSystem.getLogger(this).finer("new cluster is "+c);
            lastCluster = c;
        } else return;

        int numVis = 0;
        faceBitset.clear(0,faceBitset.size()-1);
        int numFacesVis = 0;
        int numClusters = 0;
        int numLeaves = 0;
        HashMap map = new HashMap();
        for (int i=0;i<loader.visData.numOfClusters;i++) {
            boolean isVisible = isClusterVisible(c,i);
            if (clusterLeafs[i] != null) numClusters++;
            if (isVisible) {
                if (clusterLeafs[i] != null) {
                    boolean hasFaces = false;
                    Iterator j = clusterLeafs[i].iterator();
                    while (j.hasNext()) {
                        BSPLeaf l = (BSPLeaf) j.next();
                        if (l.faces != null)
                            if (l.faces.length>0) numLeaves++;
                            for (int k=0;k<l.faces.length;k++)
                                if (!faceBitset.get(l.faces[k])) {
                                    faceBitset.set(l.faces[k]);
                                    Long key = new Long(i<<24 | loader.faces[l.faces[k]].textureID << 8 |
                                            loader.faces[l.faces[k]].lightmapID);
                                    if (map.get(key)==null) map.put(key,key);
                                    numFacesVis++;
                                    hasFaces = true;
                                }
                    }
                    if (hasFaces) numVis++;
                }
            }
        }
        LoggingSystem.getLogger(this).finer("num cluster is visible is "+numVis+" out of "+numClusters);
        LoggingSystem.getLogger(this).finer("num leaves visible is "+numLeaves+" out of "+loader.leafs.length);
        LoggingSystem.getLogger(this).finer("num faces visible is "+numFacesVis+" out of "+faces.length);
        LoggingSystem.getLogger(this).finer("num unique shapes is "+map.size());
        LoggingSystem.getLogger(this).finer("applying bitmask...");
        faceSwitch.applyMask(faceBitset);
        LoggingSystem.getLogger(this).finer("...done.");
    }

    public class BSPLeaf {
        int faces[];
    }

    /**
     * @return the root node for the read bsp scene
     */
    public SceneGraphComponent getComponent() {
      return root;
    }
}
