package de.jreality.scene.proxy.scene;

import java.awt.Color;

public interface RemoteLight extends RemoteSceneGraphNode {

    public abstract Color getColor();
    public abstract void setColor(Color color);
    public abstract double getIntensity();
    public abstract void setIntensity(double intensity);
    public abstract boolean isGlobal();
    public abstract void setGlobal(boolean global);
}