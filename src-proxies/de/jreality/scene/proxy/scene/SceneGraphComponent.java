package de.jreality.scene.proxy.scene;

import de.jreality.scene.proxy.SgAdd;
import de.jreality.scene.proxy.SgRemove;

/**
 * Note: all remote references passed in must be hosted on the same machine as
 * this implementation.
 */
public class SceneGraphComponent extends de.jreality.scene.SceneGraphComponent
        implements RemoteSceneGraphComponent {

    public void setGeometry(RemoteGeometry g) {
        super.setGeometry((de.jreality.scene.Geometry) g);
    }

    public void addChild(RemoteSceneGraphComponent sgc) {
        if (sgc == null) throw new NullPointerException("child cant be null");
        super.addChild((de.jreality.scene.SceneGraphComponent) sgc);
    }

    public RemoteSceneGraphComponent getRemoteChildComponent(int index) {
        return (RemoteSceneGraphComponent) super.getChildComponent(index);
    }

    public void removeChild(RemoteSceneGraphComponent sgc) {
        super.removeChild((de.jreality.scene.SceneGraphComponent) sgc);
    }

    public void setTransformation(RemoteTransformation newTrans) {
        super.setTransformation((de.jreality.scene.Transformation) newTrans);
    }

    public void setAppearance(RemoteAppearance newApp) {
        super.setAppearance((de.jreality.scene.Appearance) newApp);
    }

    public RemoteAppearance getRemoteAppearance() {
      return (RemoteAppearance) super.getAppearance();
    }

    public RemoteCamera getRemoteCamera() {
        return (RemoteCamera) super.getCamera();
    }

    public void setCamera(RemoteCamera newCamera) {
        super.setCamera((de.jreality.scene.Camera) newCamera);
    }

    public RemoteLight getRemoteLight() {
        return (RemoteLight) super.getLight();
    }

    public void setLight(RemoteLight newLight) {
        super.setLight((de.jreality.scene.Light) newLight);
    }

    public RemoteTransformation getRemoteTransformation() {
        return (RemoteTransformation) super.getTransformation();
    }

    public RemoteGeometry getRemoteGeometry() {
        return (RemoteGeometry) super.getGeometry();
    }

    public void add(RemoteSceneGraphNode newChild) {
        new SgAdd().add(this, (de.jreality.scene.SceneGraphNode) newChild);
    }

    public void remove(RemoteSceneGraphNode newChild) {
        new SgRemove()
                .remove(this, (de.jreality.scene.SceneGraphNode) newChild);
    }
}
