
package de.jreality.scene.proxy.rmi;

import de.jreality.scene.proxy.SgAdd;
import de.jreality.scene.proxy.SgRemove;

/**
 * Note: all remote references passed in must be hosted on the same machine
 * as this implementation.
 */
public class SceneGraphComponent extends de.jreality.scene.SceneGraphComponent
  implements RemoteSceneGraphComponent
{
  public void setGeometry(RemoteGeometry g)
  {
    super.setGeometry((Geometry)g);
  }
  public void addChild(RemoteSceneGraphComponent sgc)
  {
    super.addChild((SceneGraphComponent)sgc);
  }
  public RemoteSceneGraphComponent getRemoteChildComponent(int index)
  {
    return (RemoteSceneGraphComponent)super.getChildComponent(index);
  }
  public void removeChild(RemoteSceneGraphComponent sgc)
  {
    super.removeChild((SceneGraphComponent)sgc);
  }
  public void setTransformation(RemoteTransformation newTrans)
  {
    super.setTransformation((Transformation)newTrans);
  }
  public void setAppearance(RemoteAppearance newApp)
  {
    super.setAppearance((Appearance)newApp);
  }
  public RemoteAppearance getRemoteAppearance()
  {
    return (RemoteAppearance)super.getAppearance();
  }
  public RemoteCamera getRemoteCamera()
  {
    return (RemoteCamera)super.getCamera();
  }
  public void setCamera(RemoteCamera newCamera)
  {
    super.setCamera((Camera)newCamera);
  }
  public RemoteLight getRemoteLight()
  {
    return (RemoteLight)super.getLight();
  }
  public void setLight(RemoteLight newLight)
  {
    super.setLight((Light)newLight);
  }
  public RemoteTransformation getRemoteTransformation()
  {
    return (RemoteTransformation)super.getTransformation();
  }
  public RemoteGeometry getRemoteGeometry()
  {
    return (RemoteGeometry)super.getGeometry();
  }
  public void add(RemoteSceneGraphNode newChild) {
  	new SgAdd().add(this, (de.jreality.scene.SceneGraphNode) RemoteSceneGraphElementsFactoryImpl.getLocal(newChild));
  }
  public void remove(RemoteSceneGraphNode newChild) {
   	new SgRemove().remove(this, (de.jreality.scene.SceneGraphNode) RemoteSceneGraphElementsFactoryImpl.getLocal(newChild));
  }
}
