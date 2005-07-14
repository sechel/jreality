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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.jreality.reader.Input;
import de.jreality.scene.SceneGraphComponent;

/**
 * <p>Loader for the ase file format.  Once the data is loaded, several methods can be
 * used to build the Xith3D Scenegraph object.  If the ase file represents a single
 * object which is always treated as one (i.e. no moving parts within it) then the
 * getModel method is the simplest way.  Alternativly, to load each ase node as it's
 * own Group with it's geometry relitive to it's pivot point (allowing for easy rotation)
 * use the getNamedNodesMap and getTransformGroupTree methods.  The former gives you
 * a map containing just the named GEOM nodes, infact it just calls getTransformGroupTree
 * passing a few flags to get this behaviour.  The most useful and customisable method is
 * getTransformGroupTree.  All groups and geometry are loaded relitive to their pivot points,
 * transformed into place and grouped into TransformGroupS.  This is the most 
 * true representation of the file data as the pivots group concepts are preserved.</p>
 *
 * <p>The <a href="http://xith.org/tutes.php#GettingStarted">Getting Started Guide</a>
 * has some tutorials to illustrate how to use the ASE loader, including a chapter on
 * <a href="http://xith.org/tutes/GettingStarted/html/transformgroup_trees_with_t.html">
 * TransformGroup trees</a> by the author of that method which diagramatically describes
 * how that method works.
 * </p>
 *
 * @author David Yazel
 * @author William Denniss
 *
 */
public class AseFile extends AseNode {
    
    /**
     * Field used by the parser to store ASE information
     */
    public float version;

    /**
     * Field used by the parser to store ASE information
     */
    public String comment = "";

    /**
     * Field used by the parser to store the ase scene
     */
    public AseScene scene;

    /**
     * Field used by the parser to store all Ase objects
     */
    public HashMap objects = new HashMap();

    /**
     * Field used by the parser to store materials
     */
    public ArrayList materials = new ArrayList();

    /**
     * Field used by the parser to store ASE information
     */
    public int materialCount;

    /**
     * <p>Flag used with the getTransformGroupTree method, indicates that the returned tree will consist
     * only of the root TransformGroup and one TransformGroup for each GROUP node (from the ASE file).</p>
     
     * <p>The difference being that a TransformGroup is usually created for each GEOM node
     * which contains (as a child) the Shape3D of the model data whose coordinates
     * are relative to it's pivot point.  A translation is then applied to that TransformGroup
     * to move it into place.  Using this flag will cause the said Shape3D
     * consisting of the GEOM node data to be added directly to it's parent TransformGroup
     * and cause it's coordinates to be relative to it's parents pivot point rather than it's own (thus
     * not needing a translation to move it into place).  The parent of a GEOM node in this context 
     * is either the root of the model as a whole (centered at (0,0,0)) or it's parent GROUP node.</p>
     */
    public static final int TGT_GROUPS_ONLY = 1;
    
    
    /**
     * Flag used with the getTransformGroupTree method, indicates that the returned tree will consist
     * only of the root TransformGroup and one TransformGroup for each GEOM node (including those
     * which are members of a GROUP node).
     */
    public static final int TGT_NO_GROUPS = 2;
    
    /**
     * Flag used with the getTransformGroupTree method, indicates that the translation
     * of the ase node (GROUP or GEOM) to its correct location will be applied directly to it's 
     * TransformGroup rather than creating a new TransformGroup just to contain the
     * translation and the nodes TransformGroup.  Use this flag to cut down on the number of
     * TransformGroupS created but take caution when applying further translations to the
     * TransformGroup that you don't accidently apply the identity matrix.
     *
     */
    public static final int TGT_NO_TRANSLATE_TG = 4;
    
    /**
     * Flag used with the getTransformGroupTree method, causes the x attribute of the
     * orientationAngle vector (which can be passed to getTransformGroupTree) to have
     * 90 degrees added to it.  The main use of this flag is to cater for the difference in
     * axis orientation between 3D Studio MAX and Xith3D.
     *
     */
    public static final int TGT_ROTATE90 = 8;
    
    /**
     * Unless this flag is set, the root TransformGroup of the tree which is returned
     * by the getTransformGroupTree method will also be added to the named nodes 
     * with the name "Root" for conveniance.
     */
    public static final int TGT_NO_ROOT = 16;
    
    /**
     * <p>Creates an empty AseFile object to which data from the file will
     * be read into.  To read the data, use the parse method passing
     * an AseReader object.</p>
     *
     * <p><b>Example:</b>
     * <code>
     *   AseFile ase = new AseFile();
     *   ase.parse(new BufferedReader(new FileReader("CUBE.ASE"));
     * </code>
     * </p>
     *
     */
    public AseFile(Input in) {
        input = in;
        scene = new AseScene();
        properties.put("*3DSMAX_ASCIIEXPORT", "version");
        properties.put("*COMMENT", "comment");
        properties.put("*SCENE", "scene");
        properties.put("*MATERIAL_COUNT", "materialCount");
    }

    private Input input;

    /**
     * Returns the model as a single BranchGroup.  Geometry within
     * groups are added, but the concept of groups is not maintained.
     * All objects are relitive to the origin rather than their own
     * pivot points which is unsuitable when the child objects will be
     * transformed upon independantly, but well suited for cases when the
     * model is treated always as a whole.  If the concept of groups are
     * important as well as object geometyr being relitive to their pivot
     * points, the getTransformGroupTree method is recommended.
     * 
     * @see #getTransformGroupTree
     * @return the model represented by this AseFile
     */
    public SceneGraphComponent getModel() {

        SceneGraphComponent root = new SceneGraphComponent();

        Collection c = objects.values();
        Iterator i = c.iterator();

        while (i.hasNext()) {
            AseGeom g = (AseGeom) i.next();
            
            // Gets the geometry data
            SceneGraphComponent shape = g.getShape(this);
            
            root.addChild(shape);
        }

        return root;
    }
    
    /**
     * Returns a table of all named nodes.  In the case of groups, the group as a whole is returned
     * containing all subgroups and subnodes.  The subgroups and subnodes are not recursivly added
     * as named nodes and will NOT be present in the resulting Hashtable.  The <code>getTransformGroupTree</code> 
     * method is generally more useful for handling groups where you need to access the subgroups and nodes.
     *
     * @return a Hashtable of the names mapped to the node data
     * 
     * @see java.util.Hashtable
     * @see java.util.Hashtable#keys()
     * @see java.util.Enumeration
     * @see #getNamedNodesMap
     * @deprecated use getNamedNodesMap instead
     *             
     */
    public java.util.Hashtable getNamedNodes () {
        java.util.Collection c = this.objects.values();
        java.util.Iterator i = c.iterator();
        
        java.util.Hashtable toReturn = new java.util.Hashtable ();
        
        
        while (i.hasNext()) {
            AseGeom g = (AseGeom) i.next();
            g.getNamedNodes(toReturn, this);
        }
		
		return toReturn;
    }
    
//    /**
//     * Conveniance method to return a Map of TransformGroupS, one for each 
//     * named GEOM node in the ASE file.
//     * If there are groups in the ASE file, then the group structure itself is ignored
//     * and the GEOM nodes within those groups are treated simply as if they weren't in
//     * any group.  This method calls getTransformGroupTree passing the 
//     * TGT_NO_TRANSLATE_TG, TGT_NO_GROUPS and TGT_NO_ROOT flags and returns the generated Map.
//     * It exists as an easy upgrade path from the deprecated getNamedNodes method.  Unlike
//     * with getTransformGroupTree, the tree strucutre is ignored.  The returned nodes
//     * are parentless.  If the heirachial tree strucutre is desired, for for more customised
//     * options, use getTransformGroupTree instead.
//     * 
//     * @see #getTransformGroupTree
//     * @see #TGT_NO_GROUPS
//     * @see #TGT_NO_TRANSLATE_TG
//     * @see #TGT_NO_ROOT
//	 * @see #removeParents
//     */
//    public Map getNamedNodesMap () {
//        Map namedNodes = new HashMap();
//        getTransformGroupTree(namedNodes, TGT_NO_TRANSLATE_TG | TGT_NO_GROUPS | TGT_NO_ROOT);
//	
//		removeParents(namedNodes);
//		
//        return namedNodes;
//        
//    }
//    
//	/**
//	 * Iterates though the given Map, removing the nodes from their 
//	 * parent groups
//	 */
//    public void removeParents(Map nodes) {
//		for (Iterator i = nodes.values().iterator(); i.hasNext();) {
//			((Node) i.next()).removeFromParentGroup();
//		}
//    }
//    
//    
//    /**
//     * Constructs a TransformGroup tree with the default settings, obeying groupings, 
//     * and group/node pivot points.
//     * Groups and Geometry with names are also added to the passed hashmap.
//     *
//     * @param namedNodes map to which the named nodes will be added
//     * @param encapsulateTranslation if set, all nodes will create a parent TransformGroup for the translation
//     * 		component and a child TransformGroup with the geometry (so that the child TransformGroup
//     * 		can be used for transformations relative to the pivot point)
//     * @return root TransformGroup in the tree
//     *
//     * @see #getTransformGroupTree(Map, int)
//     */
//    public TransformGroup getTransformGroupTree (Map namedNodes) {
//	    return getTransformGroupTree(namedNodes, 0);
//    }
//    
//    /**
//     * Constructs a TransformGroup tree, obeying groupings, and group/node pivot points.
//     * Groups and Geometry with names are also added to the passed hashmap.
//     *
//     * @param namedNodes map to which the named nodes will be added
//     * @param encapsulateTranslation if set, all nodes will create a parent TransformGroup for the translation
//     * 		component and a child TransformGroup with the geometry (so that the child TransformGroup
//     * 		can be used for transformations relative to the pivot point)
//     * @param flags
//     * @return root TransformGroup in the tree
//     *
//     * @see #getTransformGroupTree(Map, int, Vector3f)
//     */
//     public TransformGroup getTransformGroupTree (Map namedNodes, int flags) {
//         return getTransformGroupTree(namedNodes, flags, new Vector3f(0,0,0));
//     }
//     
//    /**
//     * <p>Constructs a tree of TransformGroupS, obeying groupings, and group/node pivot points.
//     * A TransformGroup is created for each GROUP and GEOM objects.  The former will have
//     * as it's children, all GEOM TransformGroups following the structure from the ase file
//     * and the latter will simply contain the Geomtry data (adjusted relitive to the pivot point)
//     * translated into it's correct place.  Thus, just as in the modelling program if the
//     * group is transformed, it's children will be transformed with it, but if a child is
//     * transformed then only it will be transformed.  The resulting tree is returned, and
//     * for conveniance (to avoid parsing the Scenegraph), all nodes which had name in
//     * the ase file are added to a Map as well as the resultant TransformGroup which is 
//     * added with the name "Root".  This method is recommended when the group structure
//     * is important and there are "moving parts" i.e. models within the ase file which
//     * will need to be transformed independantly from each other.  If this isn't required
//     * and just the "flat" model is needed, then getModel is better to use.</p>
//     *
//     * <p>Much of the behaviour of this method can be tweaked by passing various flags.
//     * For exmaple, groups can be ignored.  See the documentation for the respective flags
//     * for details on what they cause</p>
//     *
//     * @param namedNodes map to which the named nodes will be added
//     * @param flags (see the AseGeom file)
//     * @param orientationAngle XYZ angles to which final TG will be roatated (in addition to TGT_ROTATE90 if set)
//     * @return root TransformGroup in the tree
//     *
//     * @see #TGT_GROUPS_ONLY
//     * @see #TGT_NO_GROUPS
//     * @see #TGT_NO_TRANSLATE_TG
//     * @see #TGT_ROTATE90
//     * @see #TGT_NO_ROOT     
//     */
//     public TransformGroup getTransformGroupTree (Map namedNodes, int flags, Vector3f orientationAngle) {
//        if (namedNodes == null) {
//            throw new IllegalArgumentException ("Map can't be null");
//        }
//        if (((flags & AseFile.TGT_GROUPS_ONLY) == AseFile.TGT_GROUPS_ONLY) && ((flags & TGT_NO_GROUPS) == TGT_NO_GROUPS)) {
//            throw new IllegalArgumentException ("Can't use both AseFile.TGT_GROUPS_ONLY and AseFile.TGT_NOGROUPS together.  Perhaps you want the AseFile.getModel() method");
//        }
//        
//
//        TransformGroup root = new TransformGroup();
//	    
//        // Adds all objects to the tree
//        Collection c = objects.values();
//        Iterator i = c.iterator();
//        while (i.hasNext()) {
//            
//            AseGeom g = (AseGeom) i.next();
//            
//            g.createTransformGroupTree(root, namedNodes, this, new Vector3f(0,0,0), flags);
//            
//        }
//
//		// Add in 90 degree X-axis rotation if set
//        if ((flags & TGT_ROTATE90) == TGT_ROTATE90) {
//			orientationAngle.x += (float)Math.toRadians(90);
//		}
//    
//        // Rotates the model and encapsulates this rotation in a child TG
//    	if (!orientationAngle.equals(new Vector3f(0,0,0))) {
//            
//			Transform3D rot90 = new Transform3D ();
//            root.getTransform(rot90);
//            rot90.rotXYZ(orientationAngle.x,orientationAngle.y,orientationAngle.z);
//            root.setTransform(rot90);
//            root.setName("RootROT");
//            
//            if ((flags & TGT_NO_ROOT) != TGT_NO_ROOT) {
//                namedNodes.put("RootROT", root);
//            }
//            
//            TransformGroup rotate = root;
//            root = new TransformGroup();
//            root.addChild(rotate);
//            
//        }
//        
//        // Add root TG to named nodes for convenience
//        root.setName("Root");
//        if ((flags & TGT_NO_ROOT) != TGT_NO_ROOT) {
//            namedNodes.put("Root", root);
//        }
//	
//        return root;
//    }
    
    public void parse (Reader in) {
        parse(new AseReader(in));
    }
    
    /**
     * Overrides the default parse method because the top level of an ASE file
     * has various nodes of different types, and its easier to handle them
     * explicitly
     */
    public void parse(AseReader in) {
        // for this to work, blocks have to open on the same line as the
        // property definition.
        try {
            while (in.readAseLine()) {
                if (!parseProperty(in)) {
                    // check for the various special types
                    if (in.key.equalsIgnoreCase("*GEOMOBJECT")) {
                        System.out.println("Geom Object Starting");

                        AseGeom a = new AseGeom(input);
                        a.parse(in);
                        objects.put(a.name, a);
                        System.out.println("Geom Object " + a.name + " parsed");
                    } else if (in.key.equalsIgnoreCase("*GROUP")) {
                    
                        AseGroup g = new AseGroup(input);
                        g.parse(in);
                        objects.put(g.name, g);
                        System.out.println("Geom Object " + g.name + " parsed");
                    } else if (in.key.equalsIgnoreCase("*MATERIAL_LIST")) {
                        System.out.println("Parsing material list");
                    } else if (in.key.equalsIgnoreCase("*MATERIAL")) {
                        int n = Integer.parseInt(in.params[0]);

                        if (n != materials.size()) {
                            throw new Error(
                                    "Material index does not match material list");
                        }

                        AseMaterial m = new AseMaterial(input);
                        m.parse(in);
                        System.out.println("   Parsed material " + m.name);
                        materials.add(m);
                    } else if (in.startBlock) {
                        trashBlock(in);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
}
