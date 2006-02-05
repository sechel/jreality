
package de.jreality.scene.proxy;

import de.jreality.scene.*;

/**
 */
public class SgAdd extends SceneGraphVisitor
{
    private SceneGraphComponent parent;

    public void add(SceneGraphComponent sgParent, SceneGraphNode sgChild) {
    	parent=sgParent;
    	sgChild.accept(this);
    }

    public void visit(Appearance a) {
    	parent.setAppearance(a);
    }

    public void visit(Camera c) {
        parent.setCamera(c);
    }

    public void visit(Geometry g) {
        parent.setGeometry(g);
    }

    public void visit(Light l) {
        parent.setLight(l);
    }

    public void visit(Transformation t) {
        parent.setTransformation(t);
    }

    public void visit(SceneGraphComponent sgc) {
        parent.addChild(sgc);
    }

}
