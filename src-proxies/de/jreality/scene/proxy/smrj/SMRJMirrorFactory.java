package de.jreality.scene.proxy.smrj;

import java.io.IOException;

import de.jreality.geometry.QuadMeshShape;
import de.jreality.scene.proxy.rmi.*;
import de.smrj.RemoteFactory;

/**
 * this class should work like the inherited copy factory but copying objects on remote places
 * 
 * TODO: we will possibly have to rewrite the copyAttr-Methods with casts to remote objects...
 * @author weissman
 */
public class SMRJMirrorFactory extends RemoteMirrorFactory {

    RemoteFactory rf;
    
    public SMRJMirrorFactory(RemoteFactory rf) {
        super(null);
       this.rf = rf;
    }
	
    private Object createRemote(Class clazz) {
        try {
            return rf.createRemote(clazz);
        } catch (IOException ie) {
            throw new IllegalStateException("IO Error");
        }
    }

    public void visit(de.jreality.scene.Appearance a) {
        created=createRemote(Appearance.class);
        copyAttr(a, (RemoteAppearance) created);
    }

    public void visit(de.jreality.scene.Camera c) {
        created=createRemote(Camera.class);
        copyAttr(c, (RemoteCamera)created);
    }

    public void visit(de.jreality.scene.Cylinder c) {
        created=createRemote(Cylinder.class);
        copyAttr(c, (RemoteCylinder)created);
    }

    public void visit(de.jreality.scene.DirectionalLight l) {
        created=createRemote(DirectionalLight.class);
        copyAttr(l, (RemoteDirectionalLight) created);
    }

    public void visit(de.jreality.scene.IndexedFaceSet i) {
        created=createRemote(IndexedFaceSet.class);
        copyAttr(i, (RemoteIndexedFaceSet)created);
    }

    public void visit(de.jreality.scene.IndexedLineSet ils) {
        created=createRemote(IndexedLineSet.class);
        copyAttr(ils, (RemoteIndexedLineSet)created);
    }

    public void visit(de.jreality.scene.PointSet p) {
        created=createRemote(PointSet.class);
        copyAttr(p, (RemotePointSet)created);
    }


    /**
     * 
     * TODO: we need to implement the corresponding remote object?
     * 
     */
    public void visit(QuadMeshShape q) {
    	visit((de.jreality.scene.IndexedFaceSet) q);
    }
    

    public void visit(de.jreality.scene.SceneGraphComponent c) {
        created=createRemote(SceneGraphComponent.class);
        copyAttr(c, (RemoteSceneGraphComponent)created);
    }

    public void visit(de.jreality.scene.Sphere s) {
        created=createRemote(Sphere.class);
        copyAttr(s, (RemoteSphere)created);
    }

    public void visit(de.jreality.scene.SpotLight l) {
        created=createRemote(SpotLight.class);
        copyAttr(l, (RemoteSpotLight)created);
    }

    public void visit(de.jreality.scene.ClippingPlane c) {
        created=createRemote(ClippingPlane.class);
        copyAttr(c, (RemoteClippingPlane)created);
    }

    public void visit(de.jreality.scene.PointLight l) {
        created=createRemote(PointLight.class);
        copyAttr(l, (RemotePointLight)created);
    }

    public void visit(de.jreality.scene.Transformation t) {
        created=createRemote(Transformation.class);
        copyAttr(t, (RemoteTransformation)created);
    }
    
}
