package de.jreality.scene.proxy.scene;

import java.util.List;

public interface RemoteSceneGraphComponent extends RemoteSceneGraphNode {

    public abstract void setGeometry(RemoteGeometry g);
    public abstract void addChild(RemoteSceneGraphComponent sgc);
    public abstract RemoteSceneGraphComponent getRemoteChildComponent(int index);
    public abstract int getChildComponentCount();
    public abstract void removeChild(RemoteSceneGraphComponent sgc);
    public abstract void setTransformation(RemoteTransformation newTrans);
    public abstract void setAppearance(RemoteAppearance newApp);
    public abstract RemoteAppearance getRemoteAppearance();
    public abstract RemoteCamera getRemoteCamera();
    public abstract void setCamera(RemoteCamera newCamera);
    public abstract RemoteLight getRemoteLight();
    public abstract void setLight(RemoteLight newLight);
    public abstract RemoteTransformation getRemoteTransformation();
    public abstract List getChildNodes();
    public abstract RemoteGeometry getRemoteGeometry();
    //  public abstract void addTool(Tool tool);
    //  public abstract void removeTool(Tool tool);
    //  public abstract List getTools();
    public abstract void add(RemoteSceneGraphNode newChild);
    public abstract void remove(RemoteSceneGraphNode newChild);
}