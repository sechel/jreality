package de.jreality.scene.proxy.scene;

public interface RemoteTransformation extends RemoteSceneGraphNode {

    public abstract double[] getMatrix();
    public abstract double[] getMatrix(double[] aMatrix);
    public abstract void setMatrix(double[] aMatrix);
}