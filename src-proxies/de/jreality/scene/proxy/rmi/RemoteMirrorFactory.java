package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;
import java.util.List;

import de.jreality.geometry.QuadMeshShape;

/**
 * this class should work like the inherited copy factory but copying objects on remote places
 * 
 * TODO: we will possibly have to rewrite the copyAttr-Methods with casts to remote objects...
 * @author weissman
 */
public class RemoteMirrorFactory extends de.jreality.scene.proxy.CopyFactory {

	RemoteSceneGraphElementsFactory factory;
		
	public RemoteMirrorFactory(RemoteSceneGraphElementsFactory factory) {
		this.factory = factory;
	}
	
    Object created;

    public Object getProxy() {
        return created;
    }

    public void visit(de.jreality.scene.Appearance a) {
		try {
			RemoteAppearance newApp = factory.createRemoteAppearance();
			copyAttr(a, newApp);
	        created=newApp;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.Camera c) {
        try {
        	RemoteCamera newCamera = factory.createRemoteCamera();
	        copyAttr(c, newCamera);
	        created=newCamera;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.Cylinder c) {
        try {
        	RemoteCylinder newCyl = factory.createRemoteCylinder();
	        copyAttr(c, newCyl);
	        created=newCyl;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.DirectionalLight l) {
        try {
        	RemoteDirectionalLight newLight= factory.createRemoteDirectionalLight();
            copyAttr(l, newLight);
            created=newLight;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.IndexedFaceSet i) {
        try {
        	RemoteIndexedFaceSet newIFS= factory.createRemoteIndexedFaceSet();
	        copyAttr(i, newIFS);
	        created=newIFS;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.IndexedLineSet g) {
    	RemoteIndexedLineSet newILS;
		try {
			newILS = factory.createRemoteIndexedLineSet();
			copyAttr(g, newILS);
	        created=newILS;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.PointSet p) {
    	RemotePointSet newPS;
		try {
			newPS = factory.createRemotePointSet();
			copyAttr(p, newPS);
	        created=newPS;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


    /**
     * 
     * we need to implement the corresponding remote object?
     * 
     */
    public void visit(QuadMeshShape q) {
    	visit((de.jreality.scene.IndexedFaceSet) q);
    }
    

    public void visit(de.jreality.scene.SceneGraphComponent c) {
    	RemoteSceneGraphComponent newSGC;
		try {
			newSGC = factory.createRemoteSceneGraphComponent();
			copyAttr(c, newSGC);//Note: attributes does not include children
	        created=newSGC;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.Sphere s) {
    	RemoteSphere newSphere;
		try {
			newSphere = factory.createRemoteSphere();
			copyAttr(s, newSphere);
	        created=newSphere;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.SpotLight l) {
    	try {
    		RemoteSpotLight newLight=factory.createRemoteSpotLight();
    		copyAttr(l, newLight);
    		created=newLight;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.ClippingPlane c)
    {
    	try {
    		RemoteClippingPlane newCP= factory.createRemoteClippingPlane();
    		copyAttr(c, newCP);
    		created=newCP;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void visit(de.jreality.scene.PointLight l)
    {
    	try {
    		RemotePointLight newLight= factory.createRemotePointLight();
             copyAttr(l, newLight);
             created=newLight;
    	} catch (RemoteException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }

    public void visit(de.jreality.scene.Transformation t) {
    	try {
    		RemoteTransformation newTrans=factory.createRemoteTransformation();
        	copyAttr(t, newTrans);
        	created=newTrans;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void copyAttr(de.jreality.scene.SceneGraphNode src, RemoteSceneGraphNode dst) {
        try {
			dst.setName(src.getName());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }

      public void copyAttr(de.jreality.scene.SceneGraphComponent src, RemoteSceneGraphComponent dst) {
          copyAttr((de.jreality.scene.SceneGraphNode)src, (RemoteSceneGraphNode)dst);
      }

        public void copyAttr(de.jreality.scene.Appearance src, RemoteAppearance dst) {
            List lst= src.getChildNodes();
            for (int ix= 0, num= lst.size(); ix < num; ix++) {
                de.jreality.scene.AppearanceAttribute aa= (de.jreality.scene.AppearanceAttribute)lst.get(ix);
                try {
					dst.setAttribute(
					    aa.getAttributeName(),
					    aa.getValue(),
					    aa.getAttributeType());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            copyAttr((de.jreality.scene.SceneGraphNode)src, (RemoteSceneGraphNode)dst);
        }

      public void copyAttr(de.jreality.scene.Transformation src, RemoteTransformation dst) {
          try {
			dst.setMatrix(src.getMatrix());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          copyAttr((de.jreality.scene.SceneGraphNode)src, (RemoteSceneGraphNode)dst);
      }

      public void copyAttr(de.jreality.scene.Light src, RemoteLight dst) throws RemoteException {
			dst.setColor(src.getColor());
			dst.setIntensity(src.getIntensity());
          copyAttr((de.jreality.scene.SceneGraphNode)src, (RemoteSceneGraphNode)dst);
      }

      public void copyAttr(de.jreality.scene.DirectionalLight src, RemoteDirectionalLight dst) throws RemoteException {
          copyAttr((de.jreality.scene.Light)src, (RemoteLight)dst);
      }

      public void copyAttr(de.jreality.scene.SpotLight src, RemoteSpotLight dst) throws RemoteException {
			dst.setConeAngle(src.getConeAngle());
			dst.setConeDeltaAngle(src.getConeDeltaAngle());
			dst.setFalloffA0(src.getFalloffA0());
			dst.setFalloffA1(src.getFalloffA1());
			dst.setFalloffA2(src.getFalloffA2());
			dst.setDistribution(src.getDistribution());
			dst.setUseShadowMap(src.isUseShadowMap());
			dst.setShadowMapX(src.getShadowMapX());
			dst.setShadowMapY(src.getShadowMapY());
			dst.setShadowMap(src.getShadowMap());
			copyAttr((de.jreality.scene.Light)src, (RemoteLight)dst);
      }

      public void copyAttr(de.jreality.scene.Geometry src, RemoteGeometry dst) throws RemoteException {
    	  dst.setGeometryAttributes(src.getGeometryAttributes());
          copyAttr((de.jreality.scene.SceneGraphNode)src, (RemoteSceneGraphNode)dst);
      }

      public void copyAttr(de.jreality.scene.Sphere src, RemoteSphere dst) throws RemoteException {
          copyAttr((de.jreality.scene.Geometry)src, (RemoteGeometry)dst);
      }
      
      public void copyAttr(de.jreality.scene.Cylinder src, RemoteCylinder dst) throws RemoteException {
          copyAttr((de.jreality.scene.Geometry)src, (RemoteGeometry)dst);
      }

      public void copyAttr(de.jreality.scene.PointSet src, RemotePointSet dst) throws RemoteException {
          copyAttr((de.jreality.scene.Geometry)src, (RemoteGeometry)dst);
          dst.setVertexCountAndAttributes(src.getVertexAttributes());
      }

      public void copyAttr(de.jreality.scene.IndexedLineSet src, RemoteIndexedLineSet dst) throws RemoteException {
          copyAttr((de.jreality.scene.PointSet)src, (RemotePointSet)dst);
          dst.setEdgeCountAndAttributes(src.getEdgeAttributes());
      }

      public void copyAttr(de.jreality.scene.IndexedFaceSet src, RemoteIndexedFaceSet dst) throws RemoteException {
          copyAttr((de.jreality.scene.IndexedLineSet)src, (RemoteIndexedLineSet)dst);
          dst.setFaceCountAndAttributes(src.getFaceAttributes());
      }

//      public void copyAttr(QuadMeshShape src, RemoteQuadMeshShape dst) {
//          copyAttr((IndexedFaceSet)src, (RemoteIndexedFaceSet)dst);
//      }

      public void copyAttr(de.jreality.scene.Camera src, RemoteCamera dst) throws RemoteException {
      	dst.setAspectRatio(src.getAspectRatio());
      	dst.setEyeSeparation(src.getEyeSeparation());
      	dst.setFar(src.getFar());
      	dst.setFieldOfView(src.getFieldOfView());
      	dst.setFocus(src.getFocus());
      	dst.setNear(src.getNear());
      	dst.setOnAxis(src.isOnAxis());
      	dst.setOrientationMatrix(src.getOrientationMatrix());
      	dst.setPerspective(src.isPerspective());
      	dst.setSignature(src.getSignature());
      	dst.setStereo(src.isStereo());
      	dst.setViewPort(src.getViewPort().getX(), src.getViewPort().getY(), src.getViewPort().getWidth(), src.getViewPort().getHeight());
          copyAttr((de.jreality.scene.SceneGraphNode)src, (RemoteSceneGraphNode)dst);
      }

        public void visit(de.jreality.scene.SceneGraphNode m) {
            throw new IllegalStateException(
              m.getClass()+" not handled by "+getClass().getName());
        }
}
