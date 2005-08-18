package de.jreality.scene.proxy.scene;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public interface RemoteIndexedFaceSet extends RemoteIndexedLineSet
{
  public abstract void setFaceAttributes(DataListSet dls);
  public abstract void setFaceAttributes(Attribute attr, DataList dl);
  public abstract void setFaceCountAndAttributes(Attribute attr, DataList dl);
  public abstract void setFaceCountAndAttributes(DataListSet dls);
}