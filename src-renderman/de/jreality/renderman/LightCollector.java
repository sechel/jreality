/*
 * Created on 06.05.2004
 *
 * This file is part of the de.jreality.renderman package.
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
package de.jreality.renderman;

import java.util.HashMap;

import de.jreality.scene.*;
import de.jreality.util.math.Rn;
import de.jreality.util.math.VecMat;

/**
 * This is a utility for the RIBVisitor. It collects all the lights in the scene
 * and writes them directly.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class LightCollector extends SceneGraphVisitor {
    double[]  initialTrafo,   currentTrafo;
//    private   Transformation  initialTransformation;
    protected LightCollector reclaimableSubcontext;
    /**
     * 
     */
    public LightCollector(SceneGraphComponent root) {
        super();
        initialTrafo = new double[16];
        //currentTrafo = new double[16];
        Rn.setIdentityMatrix(initialTrafo);
        //Rn.setIdentityMatrix(currentTrafo);
        currentTrafo =initialTrafo;
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
        currentTrafo=initialTrafo=parentContext.currentTrafo;

    }
    
    public void visit(SceneGraphComponent c) {
        c.childrenAccept(subContext());
    }
    
    public void visit(Transformation t) {
        if (initialTrafo == currentTrafo)
            currentTrafo= new double[16];
        VecMat.copyMatrix(initialTrafo, currentTrafo);
        VecMat.multiplyFromRight(currentTrafo, t.getMatrix());
    }
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.DirectionalLightSoft)
     */
    public void visit(DirectionalLight l) {
        Ri.transformBegin();
        // write the transform for this light:
        //double[] mat = t.getMatrix();
        double[] mat = currentTrafo;
        float[] tmat = new float[16];
        for (int i = 0; i < 4; i++) 
            for (int j = 0;j<4;j++){
                tmat[i + 4*j] = (float) mat[j+4*i];
            }
        Ri.concatTransform(tmat);
        // now write the light:
        HashMap map =new HashMap();
        map.put("intensity",new Float(l.getIntensity()));
        map.put("lightcolor",l.getColorAsFloat());
        map.put("from",new float[] {0f,0f,1f});
        map.put("to",new float[] {0f,0f,0f});
        Ri.lightSource("distantlight",map);
        
        Ri.transformEnd();
        //super.visit(l);
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.SpotLightSoft)
     */
    public void visit(SpotLight l) {
        Ri.transformBegin();
        // write the transform for this light:
        //double[] mat = t.getMatrix();
        double[] mat = currentTrafo;
        float[] tmat = new float[16];
        for (int i = 0; i < 4; i++) 
            for (int j = 0;j<4;j++){
                tmat[i + 4*j] = (float) mat[j+4*i];
            }
        Ri.concatTransform(tmat);
        // now write the light:
        HashMap map =new HashMap();
        map.put("intensity",new Float(l.getIntensity()));
        map.put("lightcolor",l.getColorAsFloat());
        map.put("from",new float[] {0f,0f,1f});
        map.put("to",new float[] {0f,0f,0f});
        map.put("coneangle",new Float(l.getConeAngle()));
        map.put("conedeltaangle",new Float(l.getConeDeltaAngle()));
        map.put("beamdistribution",new Float(l.getDistribution()));
        if(RIBVisitor.fullSpotLight ) {
            map.put("float a0", new Float(l.getFalloffA0()));
            map.put("float a1", new Float(l.getFalloffA1()));
            map.put("float a2", new Float(l.getFalloffA2()));
            Ri.lightSource("spotlightFalloff",map);
        } else
            Ri.lightSource("spotlight",map);
        
        Ri.transformEnd();
        //super.visit(l);
    }

}
