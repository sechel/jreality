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


package de.jreality.soft;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import java.util.List;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Rn;
import de.jreality.renderman.RIBVisitor;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.util.Rectangle3D;
import de.jreality.util.RenderTrigger;
import de.jreality.util.SceneGraphUtility;

/**
 * This class provides the basic mouse interaction of the viewers.
 * Left mouse button drag: rotation<br>
 * Middle mouse button drag: dragging up/down left/right<br>
 * Right mouse button drag: dragging far/near.
 * Shift klick: select.
 * @version 1.0
 * @author <a href="mailto:timh@math.umass.edu">Tim Hoffmann</a>
 *
 */
public class MouseTool
	implements MouseListener, MouseMotionListener, Serializable, KeyListener {

public static final String MOUSE_DONE = "mouseDone";
	protected int oldX;
    protected int oldY;
    protected double theta;
    protected double phi;


	//private SceneGraphNode node;
    protected FactoredMatrix transformation;
    protected double[] tmp = new double[16];
    protected Component viewer;
    protected Camera camera;
    protected SceneGraphComponent root;
	
    protected SceneGraphPath cameraPath;
	
	private static final boolean simpleMode = false;
	
	// for the non simple mode:
    protected int screenWidth = 0;
    protected int screenHeight = 0;
    protected double[] screenPoint = new double[3];
    protected double[] objectPoint = new double[3];
    protected double[] screenNormal = new double[3];
    protected double[] mouseOld = new double[3];
    protected double[] mouseNew = new double[3];
    protected double[] tmpV = new double[3];
    protected Transformation tmpTrafo = new Transformation();

    protected Viewer v;
    private RenderTrigger trigger =new RenderTrigger();
    
    // Experimental Pick:
    protected PickViewer pickViewer = new PickViewer();
    SceneGraphComponent pick; 
    
	/**
	 * 
	 */
	public MouseTool(Viewer v) {
		super();
		this.v  = v;
        trigger.addViewer(v);
        setViewer(v.getViewingComponent());
	}

    private void pickAction(MouseEvent e) {
            if(pick == null){
                pick = new SceneGraphComponent();
                FactoredMatrix t = new FactoredMatrix();
                t.setStretch(.15);
                pick.setTransformation(new Transformation(t.getArray()));
                
                pick.setGeometry(new Sphere());
            } else {
//            if(root.isAncestor(pick))
                root.removeChild(pick);
//            System.out.println("removed");
            }
            pickViewer.initializeFrom(v);
            pickViewer.setPickPoint(e.getX(), e.getY());
            pickViewer.render();
            List list = pickViewer.getHitList();
            if(list.size() >0) {
                int i = 0;
//                while(((PickVisitor.Hit) list.get(i)).getPath().getLastComponent() == pick){
//                    i++;
//                }   
                Transformation t = pick.getTransformation();
                t.setTranslation(((PickVisitor.Hit) list.get(i)).getWorldCoordinates());
                /*
                double d[] = ((PickVisitor.Hit) list.get(i)).getPointNDC();
                    System.out.println("length "+list.size()+" pic at "+d[0]+" "+d[1]+" "+d[2]);
                    SceneGraphPath p = ((PickVisitor.Hit) list.get(i)).getPath();
                    p.pop();
                    Appearance a = p.getLastComponent().getAppearance();
                    if(a!=null)
                            System.out.println("appearance name is "+a.getName());
                            */
            }
            root.addChild(pick);

           trigger.forceRender();
           
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	   
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
        if(e.isShiftDown()) {
             pickAction(e);
            } else {
                oldX = e.getX();
                oldY = e.getY();
            }
		//Renderer.setDragging(true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		//transformation.changed(MOUSE_DONE,null,null);
		//System.out.println(transformation);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	 public void mouseDragged(MouseEvent e) {
         if(e.isShiftDown()) {
             //pickAction(e);
            return;
            } 
		 if(cameraPath.getLength()!= 0)
             this.transformation = new FactoredMatrix(cameraPath.getLastComponent().getTransformation().getMatrix());
         //this.transformation = ((SceneGraphComponent)cameraPath.getLastElement()).getTransformation();
         
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
			 if(simpleMode) {

			 VecMat.transformUnNormalized(tmpTrafo.getMatrix(), 0,0,sign*0.1*((double) (x - oldX)),tmpV);

			 double mag = sign *0.1;
			 tmpTrafo.resetMatrix();
			 
             applyCameraParentTrafo(tmpTrafo);

             //...instead of
			 //if(node.getParentNode()!= null) SceneGraphUtilities.applyEffectiveTransformation(tmpTrafo, ( (SceneGraphComponent) node.getParentNode()),root);
			
			 //node.applyEffectiveTransformation(tmpTrafo);
			 //System.out.println("tr "+tmpTrafo);
			 double[] inv = new double[16];
			 Rn.inverse(inv, tmpTrafo.getMatrix());
			 //tmpTrafo.getInverseMatrix(inv);
			 VecMat.transformUnNormalized(inv, tmpV[0],tmpV[1],tmpV[2],tmpV);
			 VecMat.assignTranslation(tmp, tmpV[0],tmpV[1],tmpV[2]);
			 transformation.multiplyOnLeft(tmp);
			 //System.out.println((0.1*tmpV[0])+" "+(0.1*tmpV[1])+" "+(0.1*tmpV[2]));
             //center[2] -=VecMat.norm(tmpV);
             //Rn.add(center,center,tmpV);
             center[0]-=tmpV[0];
             center[1]-=tmpV[1];
             center[2] -=tmpV[2];
             System.out.println("tmpV "+tmpV[0]+" "+tmpV[1]+" "+tmpV[2]);
             }
			 else {
				 
                 VecMat.transformUnNormalized(tmpTrafo.getMatrix(), 0,0,sign*0.1*((double) (x - oldX)),tmpV);

                 double mag = sign *0.1;
                 
                 double[] inv = new double[16];
                 Rn.inverse(inv, tmpTrafo.getMatrix());
                 
                 VecMat.transformUnNormalized(inv, tmpV[0],tmpV[1],tmpV[2],tmpV);
                 VecMat.assignTranslation(tmp, tmpV[0],tmpV[1],tmpV[2]);
                 transformation.multiplyOnRight(tmp);
                 
                 center[0]-=tmpV[0];
                 center[1]-=tmpV[1];
                 center[2] -=tmpV[2];
			 }
			 oldX = x;
			 oldY = y;
             ((SceneGraphComponent)cameraPath.getLastComponent()).getTransformation().setMatrix(transformation.getArray());
			 //transformation.setNotifyChanges(true);
			 trigger.forceRender();

			 return;
		 }
		 //
		 // middle button
		 //
		 if (e.isAltDown()) {
			 if(simpleMode) {
				 VecMat.transformUnNormalized(tmpTrafo.getMatrix(),sign*0.05*((double) (oldX - x)),sign*0.05*(double)(y -oldY),0,tmpV);

				 tmpTrafo.resetMatrix();
				 applyCameraParentTrafo(tmpTrafo);
				 //...instead of
				 //if(node.getParentNode()!= null) SceneGraphUtilities.applyEffectiveTransformation(tmpTrafo, ( (SceneGraphComponent) node.getParentNode()),root);


				 double[] inv = new double[16];
				 Rn.inverse(inv, tmpTrafo.getMatrix());
				 VecMat.transformUnNormalized(inv, tmpV[0],tmpV[1],tmpV[2],tmpV);
				 VecMat.assignTranslation(tmp, tmpV[0],tmpV[1],tmpV[2]);
				 transformation.multiplyOnLeft(tmp);
                 center[0]-=tmpV[0];
                 center[1]-=tmpV[1];
                 center[2] -=tmpV[2];
                 //Rn.add(center,center,tmpV);
                 System.out.println("tmpV "+tmpV[0]+" "+tmpV[1]+" "+tmpV[2]);
                 
			 }
			 else {
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
			 }
			 oldX = x;
			 oldY = y;
             ((SceneGraphComponent)cameraPath.getLastComponent()).getTransformation().setMatrix(transformation.getArray());
			 //transformation.setNotifyChanges(true);
			 trigger.forceRender();

			 return;
		 }
		 //
		 // left button
		 //
		 if (false) {
			 
		 } 
		 else { 

			 // get  the mouse points in world coordiantes
			 mouseToWorldCooradinates(oldX,oldY,mouseOld);
			 mouseToWorldCooradinates(x,y,mouseNew);
			
			 tmpTrafo.resetMatrix();
			 //if((node instanceof Camera)) {
			 applyCameraParentTrafo(tmpTrafo);
			 //...instead of
			 //if(node.getParentNode()!= null) SceneGraphUtilities.applyEffectiveTransformation(tmpTrafo, ( (SceneGraphComponent) node.getParentNode()),root);

			 //}
			 //else node.applyEffectiveTransformation(matrix);

			 double[] inv = new double[16];
			 //tmpTrafo.getInverseMatrix(inv);
			 Rn.inverse(inv, tmpTrafo.getMatrix());
			 //VecMat.assignIdentity(inv);


//	TWO WAYS: EITHER
			 VecMat.normalize(mouseOld);
			 VecMat.normalize(mouseNew);
			 VecMat.cross(mouseNew, mouseOld,tmpV);
			 //TODO finetune the factor 4 for angle and maybe make it a property...
			 double angle = 4*Math.asin(VecMat.norm(tmpV));
			 // reverse direction if we are not turning  a camera:
			 if(false) 
				 angle *= -1;
			 VecMat.transformNormal(inv,tmpV[0],tmpV[1],tmpV[2],tmpV);
			
//	OR:
//			 VecMat.transformNormal(inv,mouseOld[0],mouseOld[1],mouseOld[2],mouseOld);
//			 VecMat.transformNormal(inv,mouseNew[0],mouseNew[1],mouseNew[2],mouseNew);
//
// //			double dist1 = VecMat.norm(mouseOld);
// //			double dist2 = VecMat.norm(mouseNew);
//			
//
//			 VecMat.cross(mouseNew, mouseOld,tmpV);
//
// //			double angle = 2*Math.asin(VecMat.norm(tmpV)/(dist1*dist2));
//			 double angle = 2*Math.asin(VecMat.norm(tmpV));
//			 VecMat.normalize(tmpV);

			 double s = Math.sin(angle);
			 double c = Math.cos(angle);
			 double t = 1 - c;
             
			 double xv = tmpV[0];
			 double yv = tmpV[1];
			 double zv = tmpV[2];
			 
			 //double[][] transmat = transformation.getMatrix();

			 if(simpleMode){
                 FactoredMatrix tr =new FactoredMatrix();
                 //tr.resetMatrix();
                
                 double[] c2 = new double[3];
                 c2[0] =-center[0];
                 c2[1] =-center[1];
                 c2[2] =-center[2];
                 tr.setCenter(center,false);
                 
                 //tr.setUseCenter(true);
                 System.out.println(" angle "+angle);
                 System.out.println(" ongle "+transformation.getRotationAngle());
                 tr.setRotation((angle ),tmpV);
                 transformation.multiplyOnRight(tr);
                 //transformation.setTranslation(0,0,distance);
             }
             else {
			 double[] rotMat = { t * xv*xv+ c, t*xv*yv- s*zv, t*xv*zv + s*yv,0,
				 t*xv*yv + s*zv, t*yv*yv +c, t*yv*zv - s*xv,0,
				 t*xv*zv - s*yv, t*yv*zv + s*xv, t*zv*zv +c,0,
				 0,0,0,1.};

             double[] c2 = new double[3];
             c2 = transformation.getTranslation();
             VecMat.transform(transformation.getArray(),
                     center[0], center[1], center[2], c2);
                     //0, 0, -distance, c2);
             
             VecMat.assignTranslation(tmp,-c2[0],-c2[1],-c2[2]);
             transformation.multiplyOnLeft(tmp);
             
             transformation.multiplyOnLeft(rotMat);

             //VecMat.assignTranslation(tmp,c2[0],c2[1],c2[2]);
             VecMat.invert(tmp,tmp);
             transformation.multiplyOnLeft(tmp);
             
             
             }
             
//			 transformation.setNotifyChanges(true);
			 oldX = x;
			 oldY = y;
             ((SceneGraphComponent)cameraPath.getLastComponent()).getTransformation().setMatrix(transformation.getArray());
			 trigger.forceRender();

		 }
	 }

	/**
	 * @param v the vector to transform
	 * @param c the center of the sphere
	 * @param r the radius of the sphere
	 */
	private void project(double[] v, double[] c, double r) {
		v[0] -=c[0];
		v[1] -=c[1];
		v[2] -=c[2];
		double normSqr = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
		normSqr = r*r/normSqr;
		v[0] = v[0]*normSqr +c[0];
		v[1] = v[1]*normSqr +c[1];
		v[2] = v[2]*normSqr +c[2];
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * @return Transformation
	 */
	public Transformation getTransformation() {
		return new Transformation(transformation.getArray());
	}

//	/**
//	 * Sets the transformation.
//	 * @param transformation The transformation to set
//	 */
//	public void setTransformation(Transformation transformation) {
//		this.transformation = transformation;
//	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == 'c') {
			center[0] = 0;
            center[1] = 0;
		}
		if (e.getKeyChar() == 'i') {
			transformation.assignIdentity();
		}
//		if (e.getKeyChar() == 't') {
//			simpleMode = !simpleMode;
//		}
		
        if (e.getKeyChar() == 'e') {
            encompass();
        }
        if (e.getKeyChar() == 's') {
            SVGViewer svgv =new SVGViewer("test.svg");
//            svgv.setSceneRoot(v.getSceneRoot());
//            svgv.setCameraPath(v.getCameraPath());
//            svgv.setWidth(v.getViewingComponent().getWidth());
//            svgv.setHeight(v.getViewingComponent().getHeight());
            svgv.initializeFrom(v);
            System.out.print(" Rendering SVG into test.svg..");
            svgv.render();
            System.out.println(".done.");
            
        }
        if (e.getKeyChar() == 'p') {
            PSViewer psv =new PSViewer("test.eps");
//            psv.setSceneRoot(v.getSceneRoot());
//            psv.setCameraPath(v.getCameraPath());
//            psv.setWidth(v.getViewingComponent().getWidth());
//            psv.setHeight(v.getViewingComponent().getHeight());
            psv.initializeFrom(v);
            System.out.print(" Rendering PS into test.eps..");
            psv.render();
            System.out.println(".done.");
            
        }
        
         if (e.getKeyChar() == 'r') {
            RIBVisitor rv =new RIBVisitor();
            rv.setWidth(v.getViewingComponent().getWidth());
            rv.setHeight(v.getViewingComponent().getHeight());
            System.out.print(" Rendering RIB file into test.rib..");
            rv.visit(v.getSceneRoot(),v.getCameraPath(),"test");
            System.out.println(".done.");          
        }
	}

	double distance =0;
    double [] center = new double[3];
    public void encompass() {
        
        if(cameraPath.getLength()!= 0)
            this.transformation = new FactoredMatrix(cameraPath.getLastComponent().getTransformation().getMatrix());

        if (transformation == null)
            return;
        cameraPath.getInverseMatrix(tmp);
//        double[] bla = cameraPath.getMatrix(null);
//        VecMat.invert(bla,tmp);
        Rectangle3D bt = GeometryUtility.calculateBoundingBox(tmp, root);
        center = bt.getCenter();
        

        System.out.println("encompass center "+center[0]+" "+center[1]+" "+center[2]);
        
       
        //transformation.setUseCenter(true);
        //transformation.setCenter(center);
        double w =v.getViewingComponent().getWidth();
        double h =v.getViewingComponent().getHeight();
        double fl = 1/Math.tan((Math.PI/180.0)*(camera).getFieldOfView()/2);
        
        distance = 0.5*(bt.getMaxZ() -bt.getMinZ());
        double wc = .5*(bt.getMaxX() -bt.getMinX())*fl;
        double hc = .5*(bt.getMaxY() -bt.getMinY())*fl;

        distance += Math.max(wc,hc);
        System.out.println("dist"+wc+" "+hc);
        
        center[2] += distance;
        VecMat.assignTranslation(tmp,center);
        transformation.multiplyOnRight(tmp);
        //transformation.setTranslation(center);
             
        center[0] = 0;
        center[1] = 0;
        center[2] = -distance;
        double d =.8*(bt.getMaxZ() - bt.getMinZ());
        System.out.println("far "+camera.getFar()+" near "+camera.getNear());
        camera.setFar(d+distance);
        camera.setNear(-d+distance);
        System.out.println("far "+camera.getFar()+" near "+camera.getNear());
        trigger.forceRender();
    }

    /* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {

	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {

	}

	/**
	 * @return Viewer the current viewer.
	 */
	public Component getViewer() {
		return viewer;
	}

	/**
	 * The mouse tool needs to know the viewer, to get the camera position for screen to world transformations
	 * 
	 * Sets the Viewer.
	 * @param viewer The viewer to set
	 */
	public void setViewer(Component viewer) {
		if(this.viewer !=null) {
			this.viewer.removeMouseListener(this);
            this.viewer.removeMouseMotionListener(this);
            this.viewer.removeKeyListener(this);
        }
		this.viewer = viewer;
		viewer.addMouseListener(this);
        viewer.addMouseMotionListener(this);
        viewer.addKeyListener(this);
    }

//	public void setScreenSize(int x, int y) {
//		screenWidth = x;
//		screenHeight = y;
//	}
    protected final void mouseToWorldCooradinates(int x, int y, double[] w) {
		tmpTrafo.resetMatrix();
		//viewer.getCamera().
		applyCameraTrafo(tmpTrafo);
		//...instead of
		//SceneGraphUtilities.applyEffectiveTransformation(tmpTrafo,camera,root);
		double width = viewer.getWidth();
		double height = viewer.getHeight();
		double xd = x - width/2.;
		double yd = -(y -height/2.);
		double diag = (width*width + height*height)/4;
		double dist = xd*xd + yd*yd;
		//System.out.println("x "+xd+" y "+yd+" diag "+diag+" dist "+dist+" -> "+Math.sqrt(diag - dist));
		//System.out.println(Transformation.toString(matrix.getMatrix()));
		VecMat.transformNormal(tmpTrafo.getMatrix(),(xd),(yd),diag>dist?Math.sqrt(diag - dist):0,w);
	}
	/**
	 * @return Node
	 */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * Sets the node.
	 * @param node The node to set
	 */
	public void setCamera(Camera node) {
		
		camera = node;
//		if(root!= null && camera !=null) {
//            cameraPath = SceneGraphPath.getFirstPathBetween(root,camera);
//            
//            //TODO make cameraParentPath obsolete
//            // since we have the new Path now.
//			int l = cameraPath.getLength(); 
//			if(l > 1)
//			    cameraParentPath = SceneGraphPath.getFirstPathBetween(root,cameraPath.getElementAt(l-2));
//            else
//                //cameraParentPath = new Path(root,root);
//                cameraParentPath = SceneGraphPath.getFirstPathBetween(root,root);
//            guessCenter();
//        }
//		//this.transformation = node.getTransformation();
        findPath();
	}

	/**
     * 
     */
    private void guessCenter() {
        if(cameraPath.getLength()!= 0)
            this.transformation = new FactoredMatrix(cameraPath.getLastComponent().getTransformation().getMatrix());

        if (transformation == null)
            return;
        double[] d = transformation.getTranslation();
        center[0] = -d[0];
        center[1] = -d[1];
        center[2] = -d[2];
        System.out.println("GUESS");
    }

    /**
	 * @return SceneGraphComponent
	 */
	public SceneGraphComponent getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 * @param root The root to set
	 */
	public void setRoot(SceneGraphComponent root) {
		this.root = root;
		findPath();
	}

	private void findPath() {
        if(root!= null && camera !=null) {
			//cameraPath = new Path(root,camera); 
			cameraPath = (SceneGraphPath) SceneGraphUtility.getPathsBetween(root, camera).get(0);
			int l = cameraPath.getLength(); 
            guessCenter();
        }
    }

    protected void applyCameraTrafo(Transformation tmpTrafo) {
        //cameraPath.applyEffectiveTransformation(tmpTrafo);
        cameraPath.getMatrix(tmp);
        tmpTrafo.multiplyOnLeft(tmp);
	}
    protected void applyCameraParentTrafo(Transformation tmpTrafo) {
		//cameraParentPath.applyEffectiveTransformation(tmpTrafo);
        cameraPath.getMatrix(tmp,0,cameraPath.getLength()-3);
        //cameraParentPath.getMatrix(tmp);
        tmpTrafo.multiplyOnLeft(tmp);
		// is a replacement for:
		//if(node.getParentNode()!= null) SceneGraphUtilities.applyEffectiveTransformation(tmpTrafo, ( (SceneGraphComponent) node.getParentNode()),root);

	}
    public void addToTriggerList(Viewer v) {
        trigger.addViewer(v);
    }
    public void removeFromTriggerList(Viewer v) {
        trigger.removeViewer(v);
    }
    
}
