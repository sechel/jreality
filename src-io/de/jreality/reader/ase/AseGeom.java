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

import java.awt.Color;
import java.util.logging.Level;

import de.jreality.math.MatrixBuilder;
import de.jreality.reader.vecmath.Vector3f;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;


/**
 * Holds an ASE geometry object
 *
 * @author David Yazel
 * @author William Denniss
 *
 */
public class AseGeom extends AseNode {
    public String name = "";
    public int materialRef = 0;
    public boolean motionBlur = false;
    public boolean castsShadow = false;
    public boolean receivesShadow = false;
    public Color wireframeColor = new Color(1, 1, 1);
    public AseTransform transform = new AseTransform();
    public AseMesh mesh = new AseMesh();

    /**
     * Flag to instruct getTransformGroupTree to use only one TransformGroup.
     * if set, all nodes will create a parent TransformGroup for the translation
     * component and a child TransformGroup with the geometry (so that the child TransformGroup
     * can be used for transformations relative to the pivot point)     
     * @deprecated use AseFile.TGT_NO_TRANSLATE_TG instead
     * @see AseFile.TGT_NO_TRANSLATE_TG
     */
    public static final int TGT_ENCAPSULATE_OFF = AseFile.TGT_NO_TRANSLATE_TG;

    /**
     * Flag to instruct getTransformGroupTree to only create TransformGroups
     * for GROUP nodes.
     * @deprecated use AseFile.TGT_GROUPS_ONLY instead
     * @see AseFile.TGT_GROUPS_ONLY
     */
    public static final int TGT_GROUP_ONLY = AseFile.TGT_GROUPS_ONLY;

    /**
     * Flag to rotate the model 90 degrees on the X axis
     * to cater for  3d Studio MAX using a different coordinate system.
     * @deprecated use AseFile.TGT_ROTATE90 instead
     * @see AseFile.TGT_ROTATE90
     */
    public static final int TGT_ROTATE90 = AseFile.TGT_ROTATE90;
    protected Input input;
    
    public AseGeom(Input in) {
        input = in;
        properties.put("*NODE_NAME", "name");
        properties.put("*MATERIAL_REF", "materialRef");
        properties.put("*PROP_MOTIONBLUR", "motionBlur");
        properties.put("*PROP_CASTSHADOW", "castsShadow");
        properties.put("*PROP_RECVSHADOW", "receivesShadow");
        properties.put("*WIREFRAME_COLOR", "wireframeColor");
        properties.put("*NODE_TM", "transform");
        properties.put("*MESH", "mesh");
    }

    /**
     * Adds this node to the passed hashmap
     *
     * @deprecated Use either getModel to get the mesh or getTransformGroupTree to maintain the concept of groups and named nodes.     
     */
    public void getNamedNodes(java.util.Hashtable namedNodes, AseFile file) {
        SceneGraphComponent current = new SceneGraphComponent ();
        
        SceneGraphComponent shape = this.getShape(file);
        current.addChild(shape);

        namedNodes.put(name, current);
        
    }
    
    /**
     * Returns a TransformGroup containing the geometry loaded relitive to
     * the objects pivot point and translated into place.
     *
     * @param file the parent AseFile
     * @param pivotOffset vector that the geometry array will be offset by (this is NOT
     *        a translation but rather the actual values the array of geometry data will be offset
     *        by.  This value is commonly the pivot point of the parent node
     * @return a TransformGroup containing the geometry loaded relitive to
     * the objects pivot point and translated into place.
     */
    public SceneGraphComponent getTransformGroup (AseFile file, Vector3f pivotOffset) {
	     
        SceneGraphComponent tg = new SceneGraphComponent();

        // Loads the GEOM
	    tg.addChild(getShape(file, getPivot()));
        
        // Applies translation using the passed pivot offset (ie. parent pivot point).
	    applyPivotTranslation(tg, pivotOffset);
        
	    return tg;
    
    }
    
    /**
     * Creates a TransformGroup to contain the geometry data (loaded relitive to the
     * pivot point).  A second TransformGroup is created and is made a parent of the first one
     * this one additionally is translated into place (the pivot point, less the pivotOffset of the parent)
     * If this node is named, the former TransformGroup is added to the map of named nodes.
     * The latter TransformGroup is added to the tree.
     * 
     * @param tree The current TransformGroup branch to which this Node will be added
     * @param namedNodes map of the node names to their TransformGroup to which the created TransformGroup will be added
     * @param file the parent AseFile
     * @param pivotOffset the amount which the translation is offset by (this is usually the pivot point of the parent node)
     * @param flags various options
     */
    public void createTransformGroupTree (SceneGraphComponent tree, java.util.Map namedNodes, AseFile file, Vector3f pivotOffset, int flags) {

        if (((flags & AseFile.TGT_GROUPS_ONLY) == AseFile.TGT_GROUPS_ONLY) && ((flags & AseFile.TGT_NO_GROUPS) == AseFile.TGT_NO_GROUPS)) {
            throw new IllegalArgumentException ("Can't use both AseFile.TGT_GROUPS_ONLY and AseFile.TGT_NOGROUPS together.  Perhaps you want the AseFile.getModel() method");
        }

	    
        // Geometry Transform
        SceneGraphComponent geom;
	
        // Pivot point that the geometry will be loaded relitive to
        Vector3f pivot;
        
        // Constructs a new TransformGroup for this nodes geometry and extracts the pivot if it's a Group and Groups are not disabled or it's a GEOM and it's not GROUP_ONLY
        if (((flags & AseFile.TGT_GROUPS_ONLY) != AseFile.TGT_GROUPS_ONLY) || (this instanceof AseGroup && ((flags & AseFile.TGT_NO_GROUPS) != AseFile.TGT_NO_GROUPS))) {
               
            geom = new SceneGraphComponent();
            
            // Adds the TransformGroup containing the shape to the map of named nodes
            if (!name.equals("")) {
                namedNodes.put(name, geom);
            }
        
            
            // The TransformGroup to which the translation will be applied
            SceneGraphComponent translation;
        
            // Uses the geom TransformGroup for the translation        
            if ((flags & AseFile.TGT_NO_TRANSLATE_TG) == AseFile.TGT_NO_TRANSLATE_TG) {
                
                translation = geom;
            
            // Creates a new TransformGroup to hold the translation and the geom TransformGroup as a child
            } else {
        
                translation = new SceneGraphComponent();
                translation.addChild(geom);
                
                // Adds translation TransformGroup to the map of named nodes
                if (!name.equals("")) {
                namedNodes.put(name + "Translation", translation);
                }
            }
            
            // Sets the pivot used to offset the loaded geometry to this objects pivot
            pivot = getPivot();
	    
            // Apply the translation to move this TransformGroup into it's correct place
            // thus compensating for the pivot offset when loading
            applyPivotTranslation(translation, pivotOffset);
            
            // Adds the resultant TransformGroup to the TransformGroup tree
            tree.addChild(translation);		
		
        // This Shape will be added to the parent TransformGroup and it will be loaded
        // relitive to the pivot of the parent
        } else {
            geom = tree;
            pivot = pivotOffset;
        }
        
        // Adds geometry to specified geometry group
        addGeometry(geom, namedNodes, file, pivot, flags);
	
    }

    /**
     * Adds the geometry associated with this node to the passed TransformGroup.
     * The geometry is created relitive to this nodes pivot point.
     * Used by createTransformGroupTree to add the associated geom to the tree.
     *
     * @param geom the TransformGroup to which the geometry data for this node will be added.
     */
    protected void addGeometry (SceneGraphComponent geom, java.util.Map namedNodes, AseFile file, Vector3f geomPivot, int flags) {
        // Gets the shape relitive to the nodes pivot point
        geom.addChild(getShape(file, geomPivot));
    }
    
    /**
     * Retrieves the shape representing the Ase Geom object.  The appearance will be
     * driven by the Material referenced.
     */
    public SceneGraphComponent getShape(AseFile file) {
	    return getShape(file, new Vector3f(0,0,0));
    }
    

    
    /**
     * Returns the shape relitive to a given offset (usually the object pivot point).
     *
     */
    private SceneGraphComponent getShape(AseFile file, Vector3f geomOffset) {
        LoggingSystem.getLogger(this).log(Level.FINER,
                "Shape " + name + " using material ref " + materialRef);

        AseMaterial m = new AseMaterial(input);

        if (file.materials.size() > 0)
                m = (AseMaterial) file.materials.get(materialRef);

        if (m.subMaterials.size() > 0) {
            SceneGraphComponent group = new SceneGraphComponent();
            LoggingSystem.getLogger(this).log(
                    Level.FINER,
                    "   Shape " + name
                            + " has sub-materials, building multiple objects");

            for (int i = 0; i < m.subMaterials.size(); i++) {
                try {
                    AseMaterial mat = (AseMaterial) m.subMaterials.get(i);
                    Appearance a = m.getAppearance(this);
                    Geometry g = mesh.getTriangleArray(i, mat.diffuseMap, geomOffset);

                    SceneGraphComponent shape = new SceneGraphComponent();
                    shape.setGeometry(g);
                    shape.setAppearance(a);

                    group.addChild(shape);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            group.setName(name);

            return group;

        } else {
            try {
                Appearance a = m.getAppearance(this);
                //GeometryArray g = mesh.getTriangleArray(-1, m.diffuseMap, geomOffset);
                
                Geometry g = mesh.getTriangleArray(-1, m.diffuseMap, geomOffset);

                SceneGraphComponent shape = new SceneGraphComponent();
                shape.setGeometry(g);
                shape.setAppearance(a);

                shape.setName(name);

                return shape;

            } catch (Throwable t) {
                t.printStackTrace();

                return new SceneGraphComponent();
            }
        }

    }
    
     /**
      * Returns this objects pivot point (in the Java3D coordinate space).
      *
      * @return this objects pivot point (in the Java3D coordinate space).
      */    
    public Vector3f getPivot () {
       /*
        * Creates the Vector3f that the geometry data will be offset by.
        * This has the effect of making the geometry coords realitive to it's pivot point (but still in the correct
        * location due to the translation)
        */
        return new Vector3f(transform.tmPos.y, transform.tmPos.z, transform.tmPos.x);

    }
    
    /**
     * Translates the given transform group by the sum of the objects pivot point less the
     * given offset (typically the parents pivot point).
     *
     * @param group the TransformGroup to which the translation will be applied
     * @param the amout this objects pivot point will be offset by.  The translation is
     *        this object's pivot point minus the pivot offset (which is normally the pivot
     *        point of the parent group.
     */
    public void applyPivotTranslation (SceneGraphComponent group, Vector3f pivotOffset) {
	
        Vector3f pivot = getPivot();
    
	    // Gets the translation from the origin from this object, negativly offsets it with the pivot point
        Vector3f translation = new Vector3f(pivot.x - pivotOffset.x, pivot.y - pivotOffset.y, pivot.z - pivotOffset.z);
        
        /*
         * Sets up a translation so that the objects will be in their correct palces.
         *
         */
        MatrixBuilder.euclidean().translate(translation.x, translation.y, translation.z).assignTo(group);
    }
}
