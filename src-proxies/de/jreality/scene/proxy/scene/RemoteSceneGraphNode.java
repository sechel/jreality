package de.jreality.scene.proxy.scene;

import java.util.List;

public interface RemoteSceneGraphNode {

    public abstract boolean isReadOnly();
    public abstract String getName();
    public abstract void setName(String string);
    public abstract List getChildNodes();
}