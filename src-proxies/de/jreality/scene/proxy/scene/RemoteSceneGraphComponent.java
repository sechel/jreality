package de.jreality.scene.proxy.scene;

public interface RemoteSceneGraphComponent extends RemoteSceneGraphNode {
    public void setVisible(boolean value);
    public abstract void add(RemoteSceneGraphNode newChild);
    public abstract void remove(RemoteSceneGraphNode newChild);
}