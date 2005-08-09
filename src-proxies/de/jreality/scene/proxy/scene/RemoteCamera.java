package de.jreality.scene.proxy.scene;

import java.awt.geom.Rectangle2D;

public interface RemoteCamera extends RemoteSceneGraphNode {

//    public abstract double getAspectRatio();
    public abstract double getFar();
    public abstract double getFieldOfView();
    public abstract double getFocus();
    public abstract double getNear();
    public abstract Rectangle2D getViewPort();
//    public abstract void setAspectRatio(double ar);
    public abstract void setNear(double d);
    public abstract void setFar(double d);
    public abstract void setFieldOfView(double d);
    public abstract void setFocus(double d);
    public abstract void setViewPort(double x, double y, double w, double h);
    public abstract boolean isOnAxis();
    public abstract boolean isPerspective();
    public abstract void setOnAxis(boolean b);
    public abstract void setPerspective(boolean b);
//    public abstract int getSignature();
//   public abstract void setSignature(int i);
    public abstract double getEyeSeparation();
    public abstract void setEyeSeparation(double eyeSeparation);
    /**
     * The orientation matrix describes the transformation in camera coordinate
     * system which describes the orientation of the head; the "standard"
     * position is that the eyes are on the x-axis, up is the y-axis, and z is
     * the direction of projection The orientation matrix is used for cameras
     * such as those in the PORTAL.
     * 
     * @return the orientationMatrix.
     */
    public abstract double[] getOrientationMatrix();
    public abstract void setOrientationMatrix(double[] orientationMatrix);
    public abstract boolean isStereo();
    public abstract void setStereo(boolean isStereo);
}