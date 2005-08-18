package de.jreality.scene.proxy.scene;

import java.awt.Color;

public interface RemoteLight extends RemoteSceneGraphNode {
    public abstract void setColor(Color color);
    public abstract void setIntensity(double intensity);
    public abstract void setGlobal(boolean global);
}