/*
 * Created on 09-Nov-2004
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.scene.proxy.rmc;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;

import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.proxy.SgAdd;
import de.jreality.scene.proxy.SgRemove;

/**
 * @author weissman
 *
 * This interface provides creation of remote SceneGraphNodes. A client that wants to distribute its
 * sceneGraph can create a mirror with that Factory and gets the remote references to these objects back.
  */
public class RmcMirrorFactoryClient {

	private static HashMap localCopies = new HashMap();
    final String channel_name="jRealityRmcMirror";
    RpcDispatcher disp;
    Channel channel;		

    private boolean debug;
    private boolean debugData;

    public RmcMirrorFactoryClient() {
        try {
            channel=new JChannel(RmcMirrorFactory.props);
            //channel.setOpt(Channel.GET_STATE_EVENTS, Boolean.TRUE);
            disp=new RpcDispatcher(channel, null, null, this);
            disp.setMarshaller(new TimedMarshaller());
            channel.connect(channel_name);
            System.out.println("\nRmcMirrorFactoryClient started at " + new Date());
            System.out.println("Joined channel '" + channel_name + "' (" + channel.getView().size() + " members)");
            System.out.println("Ready to serve requests");
        }
        catch(Exception e) {
            System.err.println("RmcMirrorFactoryClient() : " + e);
            System.exit(-1);
        }

    }
    
    public void createRmcAppearance(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new Appearance());
	}
	
	public void createRmcCamera(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new Camera());
	}
	
	public void createRmcClippingPlane(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new ClippingPlane());		
	}
	
	public void createRmcCylinder(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new Cylinder());
	}
	
	public void createRmcDirectionalLight(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new DirectionalLight());
	}
		
	public void createRmcIndexedFaceSet(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new IndexedFaceSet());
	}
	
	public void createRmcIndexedLineSet(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new IndexedLineSet());
	}
	
	public void createRmcPointLight(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new PointLight());
	}

	public void createRmcPointSet(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new PointSet());
	}

	public void createRmcSceneGraphComponent(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new SceneGraphComponent());
	}

	public void createRmcSphere(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new Sphere());
	}

	public void createRmcSpotLight(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new SpotLight());
	}

	public void createRmcTransformation(int idHashKey) {
		if (debug) debug();
		localCopies.put(new Integer(idHashKey), new Transformation());
	}
	
	public void setName(int idHashKey, String name) {
		if (debug) debug();
		getLocal(idHashKey).setName(name);
	}
	
	public void setAppearanceAttribute(int id, String name, Object val, Class type) {
		if (debug) debug();
		((Appearance)getLocal(id)).setAttribute(name, val, type);
	}
	
	public void setMatrix(int id, double[] matrix) {
		if (debug) debug();
		if (debugData) System.out.println("Setting matrix for id: "+id);
		((Transformation)getLocal(id)).setMatrix(matrix);
	}
	
	public void setLightAttributes(int id, java.awt.Color color, double intensity) {
		if (debug) debug();
		Light l = (Light)getLocal(id);
		l.setColor(color);
		l.setIntensity(intensity);
	}
	
	
	public void setSpotLightAttributes(int id, double ca, double cda,
			double fa0, double fa1, double fa2, double dist, boolean useSh,
			int mapX, int mapY, String mapZ) {
		SpotLight l = (SpotLight)getLocal(id);
	  	l.setConeAngle(ca);
	  	l.setConeDeltaAngle(cda);
	  	l.setFalloffA0(fa0);
	  	l.setFalloffA1(fa1);
	  	l.setFalloffA2(fa2);
	  	l.setDistribution(dist);
	  	l.setUseShadowMap(useSh);
	  	l.setShadowMapX(mapX);
	  	l.setShadowMapY(mapY);
	  	l.setShadowMap(mapZ);
		if (debug) debug();
	}
	
	public void setGeometryAttributes(int id, Map ga) {
		if (debug) debug();
		Geometry g = (Geometry) getLocal(id);
		g.setGeometryAttributes(ga);
	}
	public void setVertexCountAndAttributes(int id, DataListSet ga) {
		if (debug) debug();
		if (debugData) System.out.println(ga);
		PointSet ps = (PointSet) getLocal(id);
		ps.setVertexCountAndAttributes(ga);
	}
	public void setEdgeCountAndAttributes(int id, DataListSet ga) {
		if (debug) debug();
		if (debugData) System.out.println(ga);
		IndexedLineSet ls = (IndexedLineSet) getLocal(id);
		ls.setEdgeCountAndAttributes(ga);
	}
	public void setFaceCountAndAttributes(int id, DataListSet ga) {
		if (debug) debug();
		if (debugData)System.out.println(ga);
		IndexedFaceSet fs = (IndexedFaceSet) getLocal(id);
		fs.setFaceCountAndAttributes(ga);
	}
	
	public void setAndCheckVertexCountAndAttributes(int id, Attribute a, DataList ga) {
		if (debug) debug();
		if (debugData) System.out.println(ga);
		PointSet ps = (PointSet) getLocal(id);
		if (ps.getVertexAttributes().getListLength() == ga.size())
			ps.setVertexAttributes(a, ga);
		else
			ps.setVertexCountAndAttributes(a, ga);
	}
	public void setAndCheckEdgeCountAndAttributes(int id, Attribute a, DataList ga) {
		if (debug) debug();
		if (debugData) System.out.println(ga);
		IndexedLineSet ls = (IndexedLineSet) getLocal(id);
		if (ls.getEdgeAttributes().getListLength() == ga.size())
			ls.setEdgeAttributes(a, ga);
		else
			ls.setEdgeCountAndAttributes(a, ga);
	}
	public void setAndCheckFaceCountAndAttributes(int id, Attribute a, DataList ga) {
		if (debug) debug();
		if (debugData)System.out.println(ga);
		IndexedFaceSet fs = (IndexedFaceSet) getLocal(id);
		if (fs.getFaceAttributes().getListLength() == ga.size())
			fs.setFaceAttributes(a, ga);
		else
			fs.setFaceCountAndAttributes(a, ga);
	}

	public void add(int parendID, int  childID) {
		if (debug) debug();
		new SgAdd().add((SceneGraphComponent)getLocal(parendID), getLocal(childID));
	}
	public void remove(int parendID, int  childID) {
		if (debug) debug();
		new SgRemove().remove((SceneGraphComponent)getLocal(parendID), getLocal(childID));
	}
	
	public static SceneGraphNode getLocal(int idHashKey) {
		return ((SceneGraphNode) localCopies.get(new Integer(idHashKey)));
	}
	
	private void debug() {
		System.out.println(new Exception().getStackTrace()[1]);
	}

	public static List convertToLocal(int[] list) {
		LinkedList ret = new LinkedList();
		for (int i = 0; i < list.length; i++) ret.add(getLocal(list[i]));
		return ret;
	}
	
}
