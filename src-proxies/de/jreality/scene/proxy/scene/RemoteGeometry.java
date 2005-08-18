package de.jreality.scene.proxy.scene;

import java.util.Map;
import de.jreality.scene.data.Attribute;

public interface RemoteGeometry extends RemoteSceneGraphNode {
    public abstract void setGeometryAttributes(Map dls);
    public abstract void setGeometryAttributes(Attribute attr, Object o);
}