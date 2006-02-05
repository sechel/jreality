/*
 * Created on Dec 5, 2003
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
package de.jreality.soft;

import de.jreality.math.VecMat;
import de.jreality.scene.*;

/**
 * This class holds information about the environment---mainly lights and the camera at the moment.
 * TODO: Fix the treatment of the normal direcitons (needs transposeInverse matrix) 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public final class Environment extends SceneGraphVisitor {


    //protected EffectiveAppearance eAppearance;

    double[]  initialTrafo,   currentTrafo;
    private   Transformation  initialTransformation;
    protected Environment reclaimableSubcontext;

    
    //private Camera camera;
    private double[] matrix;
    //private double[] inverseCameraMatrix= new double[16];

    private Globals globals; 

    /**
     * 
     */
    public Environment() {
        super();
        globals = new Globals();
        //eAppearance=EffectiveAppearance.create();
    }

    protected Environment(Environment parentContext) {
        //eAppearance=parentContext.eAppearance;
        initializeFromParentContext(parentContext);
    }

    
    public final DirectionalLightSoft[] getDirectionalLights() {
        return globals.getDirectionalLights();
    }
    
    
    public final void addDirectionalLight(DirectionalLightSoft l) {
        globals.addDirectionalLight(l);

    }

    public final void removeDirectionalLight(DirectionalLightSoft l) {
        globals.removeDirectionalLight(l);
    }

    /**
     * @return int
     */
    public final int getNumDirectionalLights() {
        return globals.getNumDirectionalLights();
    }
    
    public final SpotLightSoft[] getSpotLights() {
        return globals.getSpotLights();
    }

    
    public final void addSpotLight(SpotLightSoft l) {
        globals.addSpotLight(l);

    }

    public final void removeSpotLight(SpotLightSoft l) {
        globals.removeSpotLight(l);
    }

    /**
     * @return int
     */
    public final int getNumSpotLights() {
        return globals.getNumSpotLights();
    }

    public final ClippingPlaneSoft[] getClippingPlanes() {
        return globals.getClippingPlanes();
    }

    
    public final void addClippingPlane(ClippingPlaneSoft c) {
        globals.addClippingPlane(c);
    }

    public final void removeClippingPlane(ClippingPlaneSoft c) {
        globals.removeClippingPlane(c);
    }

    /**
     * @return int
     */
    public final int getNumClippingPlanes() {
        return globals.getNumClippingPlanes();
    }
    
    
    public final void removeAll() {
        globals.removeAll();
    }
    
    
    
    //
    // traversal stuff
    //
    protected void initializeFromParentContext(Environment parentContext) {
        Environment p=parentContext;
        globals = parentContext.globals;
        
        currentTrafo=initialTrafo=parentContext.currentTrafo;
    }

    /**
     * Sets the initialTransformation.
     * @param initialTransformation The initialTransformation to set
     */
    public void setInitialTransformation(Transformation initialTransformation) {
        this.initialTransformation= initialTransformation;
    }

    Environment subContext() {
        if (reclaimableSubcontext != null) {
            reclaimableSubcontext.initializeFromParentContext(this);
            return reclaimableSubcontext;
        } else
            return reclaimableSubcontext= new Environment(this);
    }
    /**
     * This starts the traversal of a SceneGraph starting form root.
     * @param root
     */
    public void traverse(SceneGraphComponent root) {
        removeAll();
        if (initialTrafo == null)
            initialTrafo= new double[16];
        if (initialTransformation != null)
            initialTransformation.getMatrix(initialTrafo);
        else
            VecMat.assignIdentity(initialTrafo);
        currentTrafo= initialTrafo;
        
        visit(root);
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
    
    public void visit(DirectionalLight l) {
        super.visit(l);
        if(!l.isGlobal()) return;// local lights are added at the render traversal
        float[] color= l.getColor().getRGBColorComponents(null);
        double[] direction= new double[3];
        //VecMat.transformNormal(currentTrafo.getMatrix(),0,0,1,direction);
        VecMat.transformNormal(currentTrafo, 0, 0, 1, direction);
        VecMat.normalize(direction);
        addDirectionalLight(new DirectionalLightSoft(
                color[0], color[1], color[2], l.getIntensity(), direction));
    }

    public void visit(PointLight l) {
        super.visit(l);
        if(!l.isGlobal()) return; // local lights are added at the render traversal
        float[] color= l.getColor().getRGBColorComponents(null);
        double[] direction= new double[3];
        //VecMat.transformNormal(currentTrafo.getMatrix(),0,0,-1,direction);
        VecMat.transformNormal(currentTrafo, 0, 0, 1, direction);
        VecMat.normalize(direction);
        double[] src= new double[3];
        //VecMat.transform(currentTrafo.getMatrix(),0,0,0,src);
        VecMat.transform(currentTrafo, 0, 0, 0, src);
        addSpotLight(new SpotLightSoft(
                color[0], color[1], color[2], l.getIntensity(), direction,
                src,Math.PI,0, l.getFalloffA0(), l.getFalloffA1(), l.getFalloffA2()));

    }

    
    public void visit(SpotLight l) {
        //super.visit(l);
        if(!l.isGlobal()) return; // local lights are added at the render traversal
        float[] color= l.getColor().getRGBColorComponents(null);
        double[] direction= new double[3];
        //VecMat.transformNormal(currentTrafo.getMatrix(),0,0,-1,direction);
        VecMat.transformNormal(currentTrafo, 0, 0, 1, direction);
        VecMat.normalize(direction);
        double[] src= new double[3];
        //VecMat.transform(currentTrafo.getMatrix(),0,0,0,src);
        VecMat.transform(currentTrafo, 0, 0, 0, src);
        addSpotLight(new SpotLightSoft(
                color[0], color[1], color[2], l.getIntensity(), direction,
                src,l.getConeAngle(),l.getConeDeltaAngle(), l.getFalloffA0(), l.getFalloffA1(), l.getFalloffA2()));

    }
    
    public void visit(ClippingPlane c) {
        
        super.visit(c);

        double[] direction= new double[3];
        //VecMat.transformNormal(currentTrafo.getMatrix(),0,0,-1,direction);
        VecMat.transformNormal(currentTrafo, 0, 0, -1, direction);
        VecMat.normalize(direction);
        double[] src= new double[3];
        //VecMat.transform(currentTrafo.getMatrix(),0,0,0,src);
        VecMat.transform(currentTrafo, 0, 0, 0, src);
        addClippingPlane(new ClippingPlaneSoft(direction, src));
    }
    
    /**
     * A shader can ask for the current world to camera matrix using this method.
     * @return Returns the matrix.
     */
    public final double[] getMatrix() {
        return matrix;
    }

    /**
     * @param matrix The matrix to set.
     */
    public final void setMatrix(final double[] matrix) {
        this.matrix = matrix;
    }
    
    private class Globals{
        
        private SpotLightSoft[] spotLights= new SpotLightSoft[0];

        private int numSpotLights;
        
        private ClippingPlaneSoft[] clippingPlanes= new ClippingPlaneSoft[0];

        private int numClippingPlanes;

        private int numDirectionalLights;

        private DirectionalLightSoft[] directionalLights= new DirectionalLightSoft[0];
        
        
        public Globals() {
            super();
        }
        
        
        public final DirectionalLightSoft[] getDirectionalLights() {
            return directionalLights;
        }
        
        
        public final void addDirectionalLight(DirectionalLightSoft l) {
            if (l == null)
                return;
            if (numDirectionalLights == 0
                    || numDirectionalLights == directionalLights.length) {
                DirectionalLightSoft[] nl= new DirectionalLightSoft[numDirectionalLights + 5];
                if (numDirectionalLights != 0)
                    System.arraycopy(directionalLights, 0, nl, 0, numDirectionalLights);
                directionalLights= nl;
            }
            directionalLights[numDirectionalLights++]= l;

        }

        public final void removeDirectionalLight(DirectionalLightSoft l) {
            boolean on= false;
            for (int i= 0; i < numDirectionalLights - 1; i++) {
                if (!on && directionalLights[i] == l)
                    on= true;
                if (on)
                    directionalLights[i]= directionalLights[i + 1];
            }
            if (on) {
                directionalLights[numDirectionalLights - 1]= null;
                numDirectionalLights--;
            }
        }

        /**
         * @return int
         */
        public final int getNumDirectionalLights() {
            return numDirectionalLights;
        }
        
        public final SpotLightSoft[] getSpotLights() {
            return spotLights;
        }

        
        public final void addSpotLight(SpotLightSoft l) {
            if (l == null)
                return;
            if (numSpotLights == 0 || numSpotLights == spotLights.length) {
                SpotLightSoft[] nl= new SpotLightSoft[numSpotLights + 5];
                if (numSpotLights != 0)
                    System.arraycopy(spotLights, 0, nl, 0, numSpotLights);
                spotLights= nl;
            }
            spotLights[numSpotLights++]= l;

        }

        public final void removeSpotLight(SpotLightSoft l) {
            boolean on= false;
            for (int i= 0; i < numSpotLights - 1; i++) {
                if (!on && spotLights[i] == l)
                    on= true;
                if (on)
                    spotLights[i]= spotLights[i + 1];
            }
            if (on) {
                spotLights[numSpotLights - 1]= null;
                numSpotLights--;
            }
        }

        /**
         * @return int
         */
        public final int getNumSpotLights() {
            return numSpotLights;
        }

        public final ClippingPlaneSoft[] getClippingPlanes() {
            return clippingPlanes;
        }

        
        public final void addClippingPlane(ClippingPlaneSoft c) {
            if (c == null)
                return;
            if (numClippingPlanes == 0 || numClippingPlanes == clippingPlanes.length) {
                ClippingPlaneSoft[] nl= new ClippingPlaneSoft[numClippingPlanes + 5];
                if (numClippingPlanes != 0)
                    System.arraycopy(clippingPlanes, 0, nl, 0, numClippingPlanes);
                clippingPlanes= nl;
            }
            clippingPlanes[numClippingPlanes++]= c;

        }

        public final void removeClippingPlane(ClippingPlaneSoft c) {
            boolean on= false;
            for (int i= 0; i < numClippingPlanes - 1; i++) {
                if (!on && clippingPlanes[i] == c)
                    on= true;
                if (on)
                    clippingPlanes[i]= clippingPlanes[i + 1];
            }
            if (on) {
                clippingPlanes[numClippingPlanes - 1]= null;
                numClippingPlanes--;
            }
        }

        /**
         * @return int
         */
        public final int getNumClippingPlanes() {
            return numClippingPlanes;
        }
        
        
        public final void removeAll() {
            numSpotLights        = 0;
            numDirectionalLights = 0;
            numClippingPlanes    = 0;

            for (int i= 0; i < spotLights.length; i++) {
                spotLights[i]= null;
            }
            for (int i= 0; i < directionalLights.length; i++) {
                directionalLights[i]= null;
            }
            for (int i= 0; i < clippingPlanes.length; i++) {
                clippingPlanes[i]= null;
            }
        }
        
        
    }
}
