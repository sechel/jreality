package de.jreality.scene.proxy.rmc;

import java.util.List;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RpcDispatcher;

import de.jreality.geometry.QuadMeshShape;

/**
 * this class should work like the inherited copy factory but copying objects on remote places
 * 
 * TODO: we will possibly have to rewrite the copyAttr-Methods with casts to remote objects...
 * @author weissman
 */
public class RmcMirrorFactory extends de.jreality.scene.proxy.ProxyFactory {

    final String channel_name="jRealityRmcMirror";
    RpcDispatcher disp;
    Channel channel;		
/*
    String props="UDP(mcast_addr=228.10.9.8;mcast_port=5678):" +
    "PING(num_initial_members=2;timeout=3000):" +
    "FD:" +
    "pbcast.PBCAST(gossip_interval=5000;gc_lag=50):" +
    "UNICAST:" +
    "FRAG:" +
    "pbcast.GMS:" +
    "pbcast.STATE_TRANSFER";
    String props="UDP(mcast_addr=228.10.9.8;mcast_port=45566;ip_ttl=3;" +
    "mcast_send_buf_size=20000;mcast_recv_buf_size=80000000):" +
    "PING(timeout=2000;num_initial_members=3):" +
    "MERGE2(min_interval=5000;max_interval=10000):" +
    "FD_SOCK:" +
    "VERIFY_SUSPECT(timeout=1500):" +
    "pbcast.NAKACK(max_xmit_size=8096;gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):" +
    "UNICAST(timeout=5000):" +
    "pbcast.STABLE(desired_avg_gossip=20000):" +
    "FRAG(frag_size=8096;down_thread=false;up_thread=false):" +
    "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;" +
    "shun=false;print_local_addr=true):" +
    "pbcast.STATE_TRANSFER";
*/
    static String props = "UDP(mcast_addr=228.1.2.3;mcast_port=45566;"
    	+"mcast_send_buf_size=10000000;ucast_recv_buf_size=10000000;ip_ttl=1):"
    	+"PING(timeout=5000;num_initial_members=2):"
    	+"FD_SOCK:"
    	+"VERIFY_SUSPECT(timeout=1500):"
    	+"pbcast.NAKACK(gc_lag=5;retransmit_timeout=3000):"
    	+"UNICAST(timeout=5000):"
    	+"pbcast.STABLE(stability_delay=1000;desired_avg_gossip=20000;down_thread=false;max_bytes=0;up_thread=false):"
    	+"FRAG(frag_size=8192;down_thread=false;up_thread=false):"
    	+"pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)";


    public RmcMirrorFactory() {
        try {
            channel=new JChannel(props);
            channel.setOpt(Channel.LOCAL, Boolean.FALSE);
            disp=new RpcDispatcher(channel, null, null, this);
            disp.setMarshaller(new TimedMarshaller());
            channel.connect(channel_name);
        }
        catch(Exception e) {
            System.err.println("QuoteClient(): " + e);
        }
	}
	
    Object created;
	private static final long TIMEOUT = 0;
	private static final int RESPONSETYPE = GroupRequest.GET_ALL;

    public Object getProxy() {
        return created;
    }

    public void visit(de.jreality.scene.Appearance a) {
    	int newApp = System.identityHashCode(a);
    	execute("createRmcAppearance", new Object[]{new Integer(newApp)});		
        created=new Integer(newApp);
        copyAttr(a);
    }

    public void visit(de.jreality.scene.Camera c) {
		int newCam = System.identityHashCode(c);
		execute("createRmcCamera", new Object[]{new Integer(newCam)});		
        created=new Integer(newCam);
        copyAttr(c);
    }

    public void visit(de.jreality.scene.Cylinder c) {
		int newCyl = System.identityHashCode(c);
		execute("createRmcCylinder", new Object[]{new Integer(newCyl)});		
        created=new Integer(newCyl);
        copyAttr(c);
    }

    public void visit(de.jreality.scene.DirectionalLight l) {
		int newDL = System.identityHashCode(l);
		execute("createRmcDirectionalLight", new Object[]{new Integer(newDL)});		
        created=new Integer(newDL);
        copyAttr(l);
    }

    public void visit(de.jreality.scene.IndexedFaceSet i) {
		int newIFS = System.identityHashCode(i);
		execute("createRmcIndexedFaceSet", new Object[]{new Integer(newIFS)});		
        created=new Integer(newIFS);
        copyAttr(i);
    }

    public void visit(de.jreality.scene.IndexedLineSet ils) {
		int newILS = System.identityHashCode(ils);
		execute("createRmcIndexedLineSet", new Object[]{new Integer(newILS)});
		created=new Integer(newILS);
        copyAttr(ils);
    }

    public void visit(de.jreality.scene.PointSet p) {
		int newPS = System.identityHashCode(p);
		execute("createRmcPointSet", new Object[]{new Integer(newPS)});
		created=new Integer(newPS);
        copyAttr(p);
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
		int newSG = System.identityHashCode(c);
		execute("createRmcSceneGraphComponent", new Object[]{new Integer(newSG)});        created=new Integer(newSG);
        copyAttr(c);
    }

    public void visit(de.jreality.scene.Sphere s) {
		int newSp = System.identityHashCode(s);
		execute("createRmcSphere", new Object[]{new Integer(newSp)});
        created=new Integer(newSp);
        copyAttr(s);
    }

    public void visit(de.jreality.scene.SpotLight l) {
		int newSl = System.identityHashCode(l);
		execute("createRmcSpotLight", new Object[]{new Integer(newSl)});
        created=new Integer(newSl);
        copyAttr(l);
    }

    public void visit(de.jreality.scene.ClippingPlane c) {
		int newCp = System.identityHashCode(c);
		execute("createRmcClippingPlane", new Object[]{new Integer(newCp)});		
		created=new Integer(newCp);
        copyAttr(c);
    }

    public void visit(de.jreality.scene.PointLight l) {
		int newPl = System.identityHashCode(l);
		execute("createRmcPointLight", new Object[]{new Integer(newPl)});
        created=new Integer(newPl);
        copyAttr(l);
    }

    public void visit(de.jreality.scene.Transformation t) {
		int newT = System.identityHashCode(t);
    	execute("createRmcTransformation", new Object[]{new Integer(newT)});		
        created=new Integer(newT);
        copyAttr(t);
    }
    
    MethodCall mc = new MethodCall();
    void execute(String methodName, Object local, Object[] params) {
    	Object[] compParams = new Object[params.length+1];
    	compParams[0] = new Integer(System.identityHashCode(local));
    	System.arraycopy(params, 0, compParams, 1, params.length);
    	execute(methodName, compParams);
    }
    void execute(String methodName, Object[] params) {
		//System.out.println("executing: "+methodName);
    	mc.setName(methodName);
    	mc.setArgs(params);
    	disp.callRemoteMethods(null, mc, RESPONSETYPE, TIMEOUT);
    }

    public void copyAttr(de.jreality.scene.SceneGraphNode src) {
    	execute("setName", src, new Object[]{src.getName()});
      }

      public void copyAttr(de.jreality.scene.SceneGraphComponent src) {
          copyAttr((de.jreality.scene.SceneGraphNode)src);
      }

        public void copyAttr(de.jreality.scene.Appearance src) {
            List lst= src.getChildNodes();
            for (int ix= 0, num= lst.size(); ix < num; ix++) {
                de.jreality.scene.AppearanceAttribute aa= (de.jreality.scene.AppearanceAttribute)lst.get(ix);
				execute("setAppearanceAttribute", src, 
					new Object[]{
						aa.getAttributeName(),
					    aa.getValue(),
					    aa.getAttributeType()
				});
            }
            copyAttr((de.jreality.scene.SceneGraphNode)src);
        }

      public void copyAttr(de.jreality.scene.Transformation src) {
      	execute("setMatrix", src, new Object[]{src.getMatrix()});
        copyAttr((de.jreality.scene.SceneGraphNode)src);
      }

      public void copyAttr(de.jreality.scene.Light src) {
			execute("setLightAttributes", src, new Object[] {
					src.getColor(), new Double(src.getIntensity())
			});
          copyAttr((de.jreality.scene.SceneGraphNode)src);
      }

      public void copyAttr(de.jreality.scene.DirectionalLight src)  {
          copyAttr((de.jreality.scene.Light)src);
      }

      public void copyAttr(de.jreality.scene.SpotLight src) {
      	execute("setSpotLightAttributes", src, new Object[] {
    			new Double(src.getConeAngle()),
    			new Double(src.getConeDeltaAngle()),
    			new Double(src.getFalloffA0()),
    			new Double(src.getFalloffA1()),
    			new Double(src.getFalloffA2()),
    			new Double(src.getDistribution()),
    			new Boolean(src.isUseShadowMap()),
    			new Integer(src.getShadowMapX()),
    			new Integer(src.getShadowMapY()),
    			src.getShadowMap()
    	});
      	copyAttr((de.jreality.scene.Light)src);
      }

      public void copyAttr(de.jreality.scene.Geometry src) {
        copyAttr((de.jreality.scene.SceneGraphNode)src);
    	execute("setGeometryAttributes", src, new Object[]{src.getGeometryAttributes()});
      }

      public void copyAttr(de.jreality.scene.Sphere src) {
          copyAttr((de.jreality.scene.Geometry)src);
      }
      
      public void copyAttr(de.jreality.scene.Cylinder src) {
          copyAttr((de.jreality.scene.Geometry)src);
      }

      public void copyAttr(de.jreality.scene.PointSet src) {
          copyAttr((de.jreality.scene.Geometry)src);
          execute("setVertexCountAndAttributes", src, new Object[]{src.getVertexAttributes()});
      }

      public void copyAttr(de.jreality.scene.IndexedLineSet src) {
          copyAttr((de.jreality.scene.PointSet)src);
          execute("setEdgeCountAndAttributes", src, new Object[]{src.getEdgeAttributes()});
      }

      public void copyAttr(de.jreality.scene.IndexedFaceSet src) {
          copyAttr((de.jreality.scene.IndexedLineSet)src);
          execute("setFaceCountAndAttributes", src, new Object[]{src.getFaceAttributes()});
      }

//      public void copyAttr(QuadMeshShape src, RemoteQuadMeshShape dst) {
//          copyAttr((IndexedFaceSet)src, (RemoteIndexedFaceSet)dst);
//      }

      public void copyAttr(de.jreality.scene.Camera src) {
//      	dst.setAspectRatio(src.getAspectRatio());
//      	dst.setEyeSeparation(src.getEyeSeparation());
//      	dst.setFar(src.getFar());
//      	dst.setFieldOfView(src.getFieldOfView());
//      	dst.setFocus(src.getFocus());
//      	dst.setNear(src.getNear());
//      	dst.setOnAxis(src.isOnAxis());
//      	dst.setOrientationMatrix(src.getOrientationMatrix());
//      	dst.setPerspective(src.isPerspective());
//      	dst.setSignature(src.getSignature());
//      	dst.setStereo(src.isStereo());
//      	dst.setViewPort(src.getViewPort().getX(), src.getViewPort().getY(), src.getViewPort().getWidth(), src.getViewPort().getHeight());
        copyAttr((de.jreality.scene.SceneGraphNode)src);
      }

        public void visit(de.jreality.scene.SceneGraphNode m) {
            throw new IllegalStateException(
              m.getClass()+" not handled by "+getClass().getName());
        }
}
