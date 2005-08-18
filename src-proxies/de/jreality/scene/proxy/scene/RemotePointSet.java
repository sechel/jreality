package de.jreality.scene.proxy.scene;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public interface RemotePointSet extends RemoteGeometry {
    public abstract void setVertexAttributes(DataListSet dls);
    public abstract void setVertexAttributes(Attribute attr, DataList dl);
    public abstract void setVertexCountAndAttributes(Attribute attr, DataList dl);
    public abstract void setVertexCountAndAttributes(DataListSet dls);
}