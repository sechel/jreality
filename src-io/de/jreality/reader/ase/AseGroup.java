/*
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
package de.jreality.reader.ase;


//import com.xith3d.scenegraph.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import de.jreality.reader.vecmath.Vector3f;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;

/**
 * Object for the ASE node *GROUP.
 * It is a collection of subgroups, child geom nodes and group attributes (stored in a helper object).
 *
 * @author William Denniss
 */
public class AseGroup extends AseGeom {
    
    public AseHelper helper = null;
    public HashMap objects = new HashMap();
    
    /**
     * Creats an empty AseGroup
     */
    public AseGroup (Input in) {
        super(in);
    }
    
    /**
     * Returns a TransformGroup containing all child geometry and groups.
     *
     * @return a TransformGroup containing all child geometry and groups.
     */
    public SceneGraphComponent getShape(AseFile file) {
    
        
        Iterator i = objects.values().iterator();
        
        SceneGraphComponent branch = new SceneGraphComponent();
        
        
        // iterates though all child geometry adding them to root TG
        while (i.hasNext()) {
            Object nextObj = i.next();
            branch.addChild( ((AseGeom) nextObj).getShape(file));
        }

        branch.setName(name);
	
        return branch;
    }

    /**
     * Recursivly adds the sub nodes of this group to the passed hashmap.
     * Note the concept of the "group" is not maintained - all nodes are flat listed
     *  in the hashmap.
     * @deprecated Use either getModel to get the mesh or getTransformGroupTree to maintain the concept of groups and named nodes.
     *
     */
    public void getNamedNodes(java.util.Hashtable namedNodes, AseFile file) {
        Iterator i = objects.values().iterator();
        
        // iterates though all child geometry
        while (i.hasNext()) {
            AseGeom current = (AseGeom) i.next();
	    
	        current.getNamedNodes(namedNodes, file);
        }
        
    }
    
    /**
     * Returns a TransformGroup containing the TransformGroups of all sub nodes (including groups).
     *
     */
    public SceneGraphComponent getTransformGroup (AseFile file, Vector3f pivotOffset) {
        
        Iterator i = objects.values().iterator();
        
        SceneGraphComponent tg = new SceneGraphComponent();
        
        
        // iterates though all child geometry adding them to root TG
        while (i.hasNext()) {
            Object nextObj = i.next();
            tg.addChild( ((AseGeom) nextObj).getTransformGroup(file, getPivot()));
        }

        applyPivotTranslation(tg, pivotOffset);
       
        tg.setName(name);
	
        return tg;
        
    }
    
    
    /**
     * Recursivly builds the TransformGroup Tree from the child nodes.
     *
     * @param tree The current TransformGroup branch to which this Node will be added
     * @param namedNodes map of the node names to their TransformGroup to which the created TransformGroup will be added
     * @param file the parent AseFile
     * @param pivotOffset the amount which the translation is offset by (this is usually the pivot point of the parent node)
     * @param flags various options
     *
     * @see AseGeom#createTransformGroupTree(TransformGroup, java.util.Map, AseFile, Vector3f, boolean)
     */
    public void createTransformGroupTree (SceneGraphComponent tree, java.util.Map namedNodes, AseFile file, Vector3f pivotOffset, int flags) {
        super.createTransformGroupTree(tree, namedNodes, file, pivotOffset, flags);
    }
    
    /**
     * Recursivly adds the geometry associated with this node to the passed TransformGroup.
     * The geometry is created relitive to this nodes pivot point.  
     *
     * @param geom the TransformGroup to which the geometry data for this node will be added.
     *
     * @see AseGeom#createTransformGroupTree(TransformGroup, java.util.Map, AseFile, Vector3f, boolean)
     * @see AseGeom#addGeometry(TransformGroup, java.util.Map, AseFile, boolean)
     */
    protected void addGeometry (SceneGraphComponent branch, java.util.Map namedNodes, AseFile file, Vector3f geomPivot, int flags) {
        // Loops through all child geom and groups, recursivly adding them to the Geometry TransformGroup
        Iterator i = objects.values().iterator();
        while (i.hasNext()) {
            AseGeom geom = (AseGeom) i.next();
            geom.createTransformGroupTree(branch, namedNodes, file, geomPivot, flags);
        }
        branch.setName(name);
    }    
    
    public void parse(AseReader in) {
        // for this to work, blocks have to open on the same line as the
        // property definition.
        if (debug) {
            LoggingSystem.getLogger(this).log(Level.FINER, "  parsing " + this.getClass().getName());
        }
    
        boolean inBlock = in.startBlock;
    
        // Read until the end of this node
        while (in.readAseLine()) {
            
            // Parses child geometry
            if (in.key.equalsIgnoreCase("*GEOMOBJECT")) {
                LoggingSystem.getLogger(this).log(Level.FINER, "Geom Object Starting");
    
                AseGeom a = new AseGeom(input);
                a.parse(in);
                objects.put(a.name, a);
                LoggingSystem.getLogger(this).log(Level.FINER, "Geom Object " + a.name + " parsed");
                
            // Parses child groups
            } else if (in.key.equalsIgnoreCase("*GROUP")) {
            
                AseGroup g = new AseGroup(input);
                g.parse(in);
                objects.put(g.name, g);
                LoggingSystem.getLogger(this).log(Level.FINER, "Geom Object " + g.name + " parsed");
        
            // Parses the Group's helper object
            } else if (in.key.equalsIgnoreCase("*HELPEROBJECT")) {
                LoggingSystem.getLogger(this).log(Level.FINER, "Group Helper Object Starting");

                AseHelper h = new AseHelper();
                h.parse(in);
                helper = h;

                LoggingSystem.getLogger(this).log(Level.FINER, "Helper Object " + h.name + " parsed");              
                
                // Set the groups name and transform to that of the helper object
                this.name = h.name;
        
                this.transform = h.transform;
            
            // Ignores any extra blocks
            } else if (in.startBlock) {
                trashBlock(in);
            }
            
            // Ends parsing of group
            if (inBlock && (in.endBlock)) {
                break;
            }
        }
    
        if (inBlock) {
            in.endBlock = false;
        }
     }
     
     /**
      * Returns this objects pivot point (in the Java3D coordinate space).
      * This node is taken from the group's *HELPEROBJECT if present
      *
      */
     public Vector3f getPivot () {
       /*
        * Creates the Vector3f that the geometry data will be offset by.
        * This has the effect of making the geometry coords realitive to it's pivot point (but still in the correct
        * location due to the translation)
        */
        return super.getPivot();
    }
}
