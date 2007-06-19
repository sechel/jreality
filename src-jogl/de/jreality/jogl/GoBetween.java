/*
 * Created on Jan 14, 2007
 *
 */
package de.jreality.jogl;

import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.logging.Level;

import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.Lock;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.scene.event.LightEvent;
import de.jreality.scene.event.LightListener;
import de.jreality.scene.event.SceneGraphComponentEvent;
import de.jreality.scene.event.SceneGraphComponentListener;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.LoggingSystem;

// register for geometry change events
//static Hashtable goBetweenTable = new Hashtable();

public class GoBetween extends JOGLPeerNode implements 
	GeometryListener, 
	TransformationListener, 
	AppearanceListener,
	SceneGraphComponentListener, LightListener	{
	SceneGraphComponent originalComponent;
	ArrayList<JOGLPeerComponent> peers = new ArrayList<JOGLPeerComponent>();
	JOGLPeerGeometry peerGeometry;
	Lock peersLock = new Lock();
	boolean singlePeer = false;

	public GoBetween()	{
		super();
	}
	public GoBetween(SceneGraphComponent sgc, JOGLRenderer jr)	{
		super();
		init(sgc, jr);
	}
	
	public void init (SceneGraphComponent sgc, JOGLRenderer jr)	{
		this.jr = jr;
		originalComponent = sgc;
		if (originalComponent.getGeometry() != null)  {
			peerGeometry = jr.getJOGLPeerGeometryFor(originalComponent.getGeometry());
			peerGeometry.refCount++;
			originalComponent.getGeometry().addGeometryListener(this);
		} else peerGeometry = null;
		originalComponent.addSceneGraphComponentListener(this);
		if (originalComponent.getAppearance() != null)  {
			originalComponent.getAppearance().addAppearanceListener(this);
			Object foo = originalComponent.getAppearance().getAttribute("singlePeer",Boolean.class);
			if (foo != null && foo instanceof Boolean && ((Boolean)foo).booleanValue()) {
				singlePeer = true;
//				if (originalComponent.getChildComponentCount() > 1)
//					throw new IllegalStateException("Don't allow single peer for many children");
			}
		}
		if (originalComponent.getTransformation() != null)
			originalComponent.getTransformation().addTransformationListener(this);
		if (originalComponent.getLight() != null)
			originalComponent.getLight().addLightListener(this);
	}


	public void dispose()	{
		originalComponent.removeSceneGraphComponentListener(this);
		if (originalComponent.getAppearance() != null) 
			originalComponent.getAppearance().removeAppearanceListener(this);
		if (peerGeometry != null)		{
			originalComponent.getGeometry().removeGeometryListener(this);
			peerGeometry.dispose();
		}
		if (originalComponent.getTransformation() != null)
			originalComponent.getTransformation().removeTransformationListener(this);
		if (originalComponent.getLight() != null)
			originalComponent.getLight().removeLightListener(this);
	}

	public boolean isSinglePeer()	{
		return singlePeer;
	}
	
	public JOGLPeerComponent getSinglePeer()	{
		return (peers.size() == 0 ? null : peers.get(0));
	}

	public void addJOGLPeer(JOGLPeerComponent jpc)	{
		if (peers.contains(jpc)) return;
		peersLock.writeLock();
		peers.add(jpc);
		peersLock.writeUnlock();
	}

	public void removeJOGLPeer(JOGLPeerComponent jpc)	{
		if (!peers.contains(jpc)) return;
		peersLock.writeLock();
		peers.remove(jpc);
		peersLock.writeUnlock();

		if (peers.size() == 0)	{
			theLog.log(Level.FINE,"GoBetween for "+originalComponent.getName()+" has no peers left");
			jr.goBetweenTable.remove(originalComponent);
			dispose();
		}
	}

	public JOGLPeerGeometry getPeerGeometry() {
		return peerGeometry;
	}

	public void geometryChanged(GeometryEvent ev) {
		peersLock.readLock();
		for ( JOGLPeerComponent peer: peers)	{
			peer.setDisplayListDirty();
		}
		peersLock.readUnlock();
	}

	public void transformationMatrixChanged(TransformationEvent ev) {
//		System.err.println("TForm event for "+originalComponent.getName());
		peersLock.readLock();
		for (JOGLPeerComponent peer : peers)	{
			peer.transformationMatrixChanged(ev);				
		}
		peersLock.readUnlock();
	}

	public void appearanceChanged(AppearanceEvent ev) {
		String key = ev.getKey();
		LoggingSystem.getLogger(this).fine("sgc "+originalComponent.getName()+" Appearance changed "+key);
		int changed = 0;
		boolean propagates = true;
		// TODO shaders should register keywords somehow and which geometries might be changed
		if (key.indexOf("implodeFactor") != -1 ) changed |= (JOGLPeerComponent.FACES_CHANGED);
		else if (key.indexOf("transparency") != -1) changed |= (JOGLPeerComponent.POINTS_CHANGED | JOGLPeerComponent.LINES_CHANGED | JOGLPeerComponent.FACES_CHANGED);
		else if (key.indexOf(CommonAttributes.SMOOTH_SHADING) != -1) changed |= (JOGLPeerComponent.POINTS_CHANGED | JOGLPeerComponent.LINES_CHANGED | JOGLPeerComponent.FACES_CHANGED);
		else if (key.indexOf("tubeRadius") != -1) changed |= (JOGLPeerComponent.LINES_CHANGED);
		else if (key.indexOf("pointRadius") != -1) changed |= (JOGLPeerComponent.POINTS_CHANGED);
		else if (key.indexOf("anyDisplayLists") != -1) changed |= (JOGLPeerComponent.POINTS_CHANGED | JOGLPeerComponent.LINES_CHANGED | JOGLPeerComponent.FACES_CHANGED);
		else if (key.endsWith("Shader")) changed |= JOGLPeerComponent.ALL_SHADERS_CHANGED;
		else if (key.endsWith("Shadername")) changed |= JOGLPeerComponent.ALL_SHADERS_CHANGED;
		// there are some appearances which we know aren't inherited, so don't propagate change event.
		else if (key.indexOf("texture2d") != -1) changed |= (JOGLPeerComponent.FACES_CHANGED);
		else if (key.indexOf("lightMap") != -1) changed |= (JOGLPeerComponent.FACES_CHANGED);
		if (key.indexOf("pickable") != -1 || key.indexOf(CommonAttributes.BACKGROUND_COLOR) != -1	||
				key.indexOf("fog") != -1 
				) propagates = false;

		peersLock.readLock();
		for ( JOGLPeerComponent peer: peers)	{
			if (propagates) peer.appearanceChanged(ev);
			if (changed != 0) peer.propagateGeometryChanged(changed);
		}
		peersLock.readUnlock();
		//theLog.log(Level.FINER,"setting display list dirty flag: "+changed);
	}
	public void childAdded(SceneGraphComponentEvent ev) {
		theLog.log(Level.FINE,"GoBetween: Container Child added to: "+originalComponent.getName());
		if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
			if (peerGeometry != null)	{
				((Geometry) ev.getOldChildElement()).removeGeometryListener(this);						
				peerGeometry.dispose();
				jr.geometryRemoved = true;
				theLog.log(Level.WARNING, "Adding geometry while old one still valid");
				peerGeometry=null;
			}
			if (originalComponent.getGeometry() != null)  {
				peerGeometry = jr.getJOGLPeerGeometryFor(originalComponent.getGeometry());
				originalComponent.getGeometry().addGeometryListener(this);
				peerGeometry.refCount++;
			} 
		}
		else if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION) {
			if (originalComponent.getTransformation() != null)
				originalComponent.getTransformation().addTransformationListener(this);
		}
		else if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE) {
			if (originalComponent.getAppearance() != null)
				originalComponent.getAppearance().addAppearanceListener(this);
		}
		peersLock.readLock();
		for ( JOGLPeerComponent peer: peers)	{
			//peer.addSceneGraphComponentEvent(ev);
			peer.childAdded(ev);
		}
		peersLock.readUnlock();
	}

	public void childRemoved(SceneGraphComponentEvent ev) {
		if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
			if (peerGeometry != null) {
				((Geometry) ev.getOldChildElement()).removeGeometryListener(this);						
				peerGeometry.dispose();		// really decreases reference count
				peerGeometry = null;
				jr.geometryRemoved = true;
			}
//			return;
		}
		else if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION) {
			if (originalComponent.getTransformation() != null)
				((Transformation) ev.getOldChildElement()).removeTransformationListener(this);
		}
		else if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE) {
			if (originalComponent.getAppearance() != null)
				((Appearance) ev.getOldChildElement()).removeAppearanceListener(this);
		}
		peersLock.readLock();
		for ( JOGLPeerComponent peer: peers)	{
			peer.childRemoved(ev);
			//peer.addSceneGraphComponentEvent(ev);
		}
		peersLock.readUnlock();
	}

	public void childReplaced(SceneGraphComponentEvent ev) {
		if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
			if (peerGeometry != null && peerGeometry.originalGeometry == originalComponent.getGeometry()) return;		// no change, really
			if (peerGeometry != null) {
				((Geometry) ev.getOldChildElement()).removeGeometryListener(this);						
				peerGeometry.dispose();
				jr.geometryRemoved=true;
				peerGeometry = null;
			}
			if (originalComponent.getGeometry() != null)  {
				originalComponent.getGeometry().addGeometryListener(this);
				peerGeometry = jr.getJOGLPeerGeometryFor(originalComponent.getGeometry());
				peerGeometry.refCount++;
			} 
		}
		else if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION) {
			if (originalComponent.getTransformation() != null)
				originalComponent.getTransformation().addTransformationListener(this);
		}
		else if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE) {
			if (originalComponent.getAppearance() != null)
				originalComponent.getAppearance().addAppearanceListener(this);
		}
		peersLock.readLock();
		for ( JOGLPeerComponent peer: peers)	{
			//peer.addSceneGraphComponentEvent(ev);
			peer.childReplaced(ev);
		}				
		peersLock.readUnlock();
	}

	public SceneGraphComponent getOriginalComponent() {
		return originalComponent;
	}

	public void visibilityChanged(SceneGraphComponentEvent ev) {
		peersLock.readLock();
		for ( JOGLPeerComponent peer: peers)	{
			//peer.addSceneGraphComponentEvent(ev);
			peer.visibilityChanged(ev);
		}				
		peersLock.readUnlock();
	}
	public void lightChanged(LightEvent ev) {
		jr.lightsChanged=true;
	}

}
