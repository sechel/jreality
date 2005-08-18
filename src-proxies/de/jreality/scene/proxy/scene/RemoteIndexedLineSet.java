package de.jreality.scene.proxy.scene;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public interface RemoteIndexedLineSet extends RemotePointSet {
    public abstract void setEdgeAttributes(DataListSet dls);
    public abstract void setEdgeAttributes(Attribute attr, DataList dl);
    public abstract void setEdgeCountAndAttributes(Attribute attr, DataList dl);
    public abstract void setEdgeCountAndAttributes(DataListSet dls);
}