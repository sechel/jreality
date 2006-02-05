package de.jreality.scene.proxy;

import de.jreality.scene.*;

/**
 */
public class SgRemove extends SceneGraphVisitor
{
    private SceneGraphComponent parent;

    public void remove(SceneGraphComponent sgParent, SceneGraphNode sgChild) {
    	parent=sgParent;
    	sgChild.accept(this);
    }

    public void visit(Appearance a) {
    	parent.setAppearance(null);
    }

    public void visit(Camera c) {
        parent.setCamera(null);
    }

    public void visit(Geometry g) {
        parent.setGeometry(null);
    }

    public void visit(Light l) {
        parent.setLight(null);
    }

    public void visit(Transformation t) {
        parent.setTransformation(null);
    }

    public void visit(SceneGraphComponent sgc) {
        parent.removeChild(sgc);
    }

}
