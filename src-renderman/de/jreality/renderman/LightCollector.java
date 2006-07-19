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


package de.jreality.renderman;

import java.util.HashMap;

import de.jreality.scene.Appearance;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.SpotLight;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;

/**
 * This is a utility for the RIBVisitor. It collects all the lights in the scene
 * and writes them directly.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class LightCollector extends SceneGraphVisitor {
    double[]  initialTrafo,   currentTrafo;
    RIBVisitor ribv = null;
//    private   Transformation  initialTransformation;
    float[] zdirection = new float[] {0f,0f,-1f};
    float[] fCurrentTrafo = null;
    protected LightCollector reclaimableSubcontext;
    EffectiveAppearance eAppearance = null;
    boolean shadowEnabled = false;
    SceneGraphPath currentPath = null;
    /**
     * 
     */
    public LightCollector(SceneGraphComponent root, RIBVisitor v) {
        super();
        ribv = v;
        currentTrafo = new double[16];
        eAppearance=EffectiveAppearance.create();
        currentPath = new SceneGraphPath();
        visit(root);
    }
    protected LightCollector(LightCollector parentContext) {
        initializeFromParentContext(parentContext);
    }
    LightCollector subContext() {
        if (reclaimableSubcontext != null) {
            reclaimableSubcontext.initializeFromParentContext(this);
            return reclaimableSubcontext;
        } else
            return reclaimableSubcontext= new LightCollector(this);
    }
    
    protected void initializeFromParentContext(LightCollector parentContext) {
        LightCollector p=parentContext;
        currentTrafo=parentContext.currentTrafo;
        ribv = parentContext.ribv;

    }
    
    public void visit(SceneGraphComponent c) {
        EffectiveAppearance tmp =eAppearance;
        Appearance a = c.getAppearance();
        if(a!= null ) eAppearance = eAppearance.create(a);
        currentPath.push(c);
        shadowEnabled = eAppearance.getAttribute(CommonAttributes.RMAN_SHADOWS_ENABLED, false);
        c.childrenAccept(this); //subContext());
        eAppearance= tmp;
        currentPath.pop();
    }
    
//    public void visit(Transformation t) {
//        if (initialTrafo == currentTrafo)
//            currentTrafo= new double[16];
//		Rn.copy(currentTrafo, initialTrafo);
//		Rn.times(currentTrafo, currentTrafo, t.getMatrix());
//    }
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.DirectionalLightSoft)
     */
    private static double[] zdir = {0,0,-1,0};
    public void visit(DirectionalLight l) {
        Ri.transformBegin();
        // write the transform for this light:
        //double[] mat = t.getMatrix();
       
//        float[] tmat = RIBVisitor.fTranspose(currentTrafo);/        Ri.concatTransform(tmat);
    	// it looks like directional lights don't get handled correctly when the 
    	// transformation is used with "to"=(0,0,1).  Instead, calculate "to" explicitly.
//		double[] direction = Rn.matrixTimesVector(null, currentTrafo, zdir);
        // now write the light:
        HashMap map =new HashMap();
        handleCommon(l, map);
        Ri.concatTransform(fCurrentTrafo);
//        map.put("to", direction);
       Ri.lightSource(shadowEnabled ? "shadowdistant":"distantlight",map);
        
        Ri.transformEnd();
        //super.visit(l);
    }
	private void handleCommon(Light l, HashMap map) {
		currentPath.getMatrix(currentTrafo);
        fCurrentTrafo = RIBVisitor.fTranspose(currentTrafo);
		map.put("intensity",new Float(l.getIntensity()));
        map.put("lightcolor",l.getColor().getRGBColorComponents(null));
        map.put("from",new float[] {0f,0f,0f});
        map.put("to",zdirection);
        if (shadowEnabled)
        	map.put("string shadowname", "raytrace");
	}

    public void visit(PointLight l) {
        Ri.transformBegin();
        // write the transform for this light:
        //double[] mat = t.getMatrix();
        // now write the light:
        HashMap map =new HashMap();
        handleCommon(l, map);
        Ri.concatTransform(fCurrentTrafo);
        Ri.lightSource(shadowEnabled ? "shadowpoint":"pointlight",map);
        
        Ri.transformEnd();
    }
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.SpotLightSoft)
     */
    public void visit(SpotLight l) {
        Ri.transformBegin();
        // write the transform for this light:
        //double[] mat = t.getMatrix();
        // now write the light:
        HashMap map =new HashMap();
        handleCommon(l, map);
        Ri.concatTransform(fCurrentTrafo);
        map.put("coneangle",new Float(l.getConeAngle()));
        map.put("conedeltaangle",new Float(l.getConeDeltaAngle()));
        map.put("beamdistribution",new Float(l.getDistribution()));
        if(RIBVisitor.fullSpotLight ) {
            map.put("float a0", new Float(l.getFalloffA0()));
            map.put("float a1", new Float(l.getFalloffA1()));
            map.put("float a2", new Float(l.getFalloffA2()));
            Ri.lightSource("spotlightFalloff",map);
        } else
            Ri.lightSource(shadowEnabled ? "shadowspot": "spotlight",map);
        
        Ri.transformEnd();
        //super.visit(l);
    }

}
