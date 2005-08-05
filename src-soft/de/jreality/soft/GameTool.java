/*
 * Created on 28.05.2004
 *
 * This file is part of the de.jreality.soft package.
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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import de.jreality.math.Rn;
import de.jreality.math.VecMat;
import de.jreality.scene.Viewer;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class GameTool extends MouseTool {
    double[] md =new double[3];
    double[] translation =new double[3];
    double xangle = 0;
    double yangle = 0;
    /**
     * @param v
     */
    public GameTool(Viewer v) {
        super(v);
        // TODO Auto-generated constructor stub
    }
    public void mouseDragged(MouseEvent e) {
        if(cameraPath.getLength()!= 0)
            this.transformation = cameraPath.getLastComponent().getTransformation();
        

        if (transformation == null)
            return;
        
        //double sign = node instanceof Camera ?1:-1;
        double sign = 1;
        
        //transformation.setNotifyChanges(false);
        int x = e.getX();
        int y = e.getY();
        
        tmpTrafo.setMatrix(Rn.identityMatrix(4));
        //viewer.getCamera().applyEffectiveTransformation(tmpTrafo);
        applyCameraTrafo(tmpTrafo);
        //...instead of
        //SceneGraphUtilities.applyEffectiveTransformation(tmpTrafo,camera,root);

        double width = viewer.getWidth();
        double height = viewer.getHeight();

        //
        // right button
        //
        if (e.isMetaDown()) {
            
            
                
                move(sign*0.1*((double) (x - oldX)));
                oldX = x;
                oldY = y;
            return;
        }
        //
        // middle button
        //
        if (e.isAltDown()) {
            
                //TODO the factor 3 is sort of heuristic. 2 would be the correct number 
                //for the viewing plane, but if objects are far, 
                //if feels like the objects move too slow. 
                VecMat.transformUnNormalized(tmpTrafo.getMatrix(),sign*8.*((double) (oldX - x)/width),sign*8.*(double)(y -oldY)/height,0,tmpV);
                
                double[] inv = new double[16];
                Rn.inverse(inv, tmpTrafo.getMatrix());
                VecMat.transformUnNormalized(inv, tmpV[0],tmpV[1],tmpV[2],tmpV);
                VecMat.assignTranslation(tmp, tmpV[0],tmpV[1],tmpV[2]);
                transformation.multiplyOnRight(tmp);
                center[0]-=tmpV[0];
                center[1]-=tmpV[1];
                center[2] -=tmpV[2];
            
            oldX = x;
            oldY = y;

            //transformation.setNotifyChanges(true);
            v.render();

            return;
        }
        //
        // left button
        //
        

            // get  the mouse points in world coordiantes
            //mouseToWorldCooradinates(oldX,oldY,mouseOld);
            //mouseToWorldCooradinates(x,y,mouseNew);
            md[0] = x-oldX;
            md[1] = y-oldY;
            md[2] = 0;
            
            //tmpTrafo.resetMatrix();
            //if((node instanceof Camera)) {
            //applyCameraParentTrafo(tmpTrafo);
            //...instead of
            //if(node.getParentNode()!= null) SceneGraphUtilities.applyEffectiveTransformation(tmpTrafo, ( (SceneGraphComponent) node.getParentNode()),root);

            //}
            //else node.applyEffectiveTransformation(matrix);

            //double[] inv = new double[16];
            //tmpTrafo.getInverseMatrix(inv);
            //Rn.inverse(inv, tmpTrafo.getMatrix());
            //VecMat.assignIdentity(inv);
            Rn.setIdentityMatrix(tmp);
            VecMat.normalize(md);
            
            yangle += 4*Math.asin(md[0]*.002);
            rotate(yangle,0,1,0);
            xangle += 4*Math.asin(md[1]*.002);
            rotate(xangle,1,0,0);
            transformation.setMatrix(tmp);
            transformation.setTranslation(translation);
            
                //VecMat.assignTranslation(tmp,c2[0],c2[1],c2[2]);
                //VecMat.invert(tmp,tmp);
                //transformation.multiplyOnRight(tmp);
                
            
//           transformation.setNotifyChanges(true);
            oldX = x;
            oldY = y;
            
            v.render();

       
    }
    
    private void move(double d) {
        tmpTrafo.setMatrix(Rn.identityMatrix(4));
        applyCameraTrafo(tmpTrafo);
        VecMat.transformUnNormalized(tmpTrafo.getMatrix(), 0,0,d,tmpV);
        
        translation[0]-=tmpV[0];
        //translation[1]-=tmpV[1];
        translation[2] -=tmpV[2];
        transformation.setTranslation(translation);
        
         //transformation.setNotifyChanges(true);
         v.render();
    }
    private void rotate(double angle, double xv, double yv, double zv) {
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        double t = 1 - c;
        
            double[] rotMat = { t * xv*xv+ c, t*xv*yv- s*zv, t*xv*zv + s*yv,0,
                    t*xv*yv + s*zv, t*yv*yv +c, t*yv*zv - s*xv,0,
                    t*xv*zv - s*yv, t*yv*zv + s*xv, t*zv*zv +c,0,
                    0,0,0,1.};

            //double[] c2 = new double[3];
            //c2 = transformation.getTranslation();
            //VecMat.transform(transformation.getMatrix(),
            //        center[0], center[1], center[2], c2);
            //VecMat.assignTranslation(tmp,-c2[0],-c2[1],-c2[2]);
            //transformation.multiplyOnLeft(tmp);
            
           //Rn.multiplyOnRight(rotMat);
           tmp = Rn.times(null,tmp,rotMat);
    }
    
    public void keyPressed(KeyEvent e) {
        if(e.getKeyChar()== 'w') {
            move(.5);
        }
        if(e.getKeyChar()== 's') {
            move(-.5);
        }
        if(e.getKeyChar()== 'a') {
            rotateY(1);
        }
        if(e.getKeyChar()== 'd') {
            rotateY(-1);
        }
    }
    
    private void rotateY(double a) {
        Rn.setIdentityMatrix(tmp);
        yangle += 4*Math.asin(a*.002);
        rotate(yangle,0,1,0);
        rotate(xangle,1,0,0);
        transformation.setMatrix(tmp);
        transformation.setTranslation(translation);
        v.render();
    }
    public void keyReleased(KeyEvent e) {
        
    }

   
    public void keyTyped(KeyEvent e) {
        
    }

}
