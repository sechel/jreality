/*
 * Created on Jan 14, 2007
 *
 */
package de.jreality.jogl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.swing.Timer;

import sun.awt.PeerEvent;

import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.RenderingHintsShader;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Lock;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.SceneGraphComponentEvent;
import de.jreality.scene.event.SceneGraphComponentListener;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.LoggingSystem;


//protected JOGLPeerComponent constructPeerForSceneGraphComponent(final SceneGraphComponent sgc, final JOGLPeerComponent p) {
//	if (sgc == null) return null;
//	final JOGLPeerComponent[] peer = new JOGLPeerComponent[1];
//	ConstructPeerGraphVisitor constructPeer = new ConstructPeerGraphVisitor( sgc, p);
//	peer[0] = (JOGLPeerComponent) constructPeer.visit();
//	return peer[0];
//}
//

public class JOGLPeerComponent extends JOGLPeerNode implements TransformationListener, AppearanceListener,SceneGraphComponentListener {

	protected EffectiveAppearance eAp;
	protected Vector<JOGLPeerComponent> children;
	protected JOGLPeerComponent parent;
	protected int childIndex;
	protected GoBetween goBetween;
	double determinant = 0.0;

	RenderingHintsShader renderingHints;
	DefaultGeometryShader geometryShader;

	Lock childlock = new Lock();
	protected Runnable renderGeometry = null;
	final JOGLPeerComponent self = this;
	protected boolean appearanceDirty = true, originalAppearanceDirty = false;
	boolean isReflection = false,
		isIdentity = false,
		cumulativeIsReflection = false,
		effectiveAppearanceDirty = true,
		geometryIsDirty = true,
		boundIsDirty = true,
		renderRunnableDirty = true;
	int geometryDirtyBits  = 0, displayList = -1;
	// copycat related fields
	long currentTime = 0;
	protected final static int POINTS_CHANGED = 1;
	protected final static int LINES_CHANGED = 2;
	protected final static int FACES_CHANGED = 4;
	protected final static int ALL_GEOMETRY_CHANGED = 7;
	protected final static int POINT_SHADER_CHANGED = 8;
	protected final static int LINE_SHADER_CHANGED = 16;
	protected final static int POLYGON_SHADER_CHANGED = 32;
	protected final static int ALL_SHADERS_CHANGED = POINT_SHADER_CHANGED | LINE_SHADER_CHANGED | POLYGON_SHADER_CHANGED;
	protected final static int ALL_CHANGED = ALL_GEOMETRY_CHANGED | ALL_SHADERS_CHANGED;

	// need an empty constructor in order to allow 
	public JOGLPeerComponent()	{
		super();
	}

	public JOGLPeerComponent(SceneGraphPath sgp, JOGLPeerComponent p, JOGLRenderer jr)		{
		super();
		init(sgp, p, jr);
	}
	
	public void init(SceneGraphPath sgp, JOGLPeerComponent p, JOGLRenderer jr) {
		this.jr = jr;
		if (sgp == null || !(sgp.getLastElement() instanceof SceneGraphComponent))  {
			throw new IllegalArgumentException("Not a valid SceneGraphComponenet");
		}
		goBetween = jr.goBetweenFor(sgp.getLastComponent());
		goBetween.addJOGLPeer(this);
		name = "JOGLPeer:"+goBetween.getOriginalComponent().getName();
		Geometry foo = goBetween.getOriginalComponent().getGeometry();
		children = new Vector<JOGLPeerComponent>();
		parent = p;
		updateTransformationInfo();
	}
	
	protected void updateRenderRunnable() {
		setDisplayListDirty();
		if (goBetween.peerGeometry == null) renderGeometry = null;
		else	 renderGeometry = new Runnable() {
			public void run() {
				goBetween.peerGeometry.render(self);
			}
		};
		renderRunnableDirty = false;
	}

	public void dispose()	{

		int n = children.size();
		for (int i = n-1; i>=0; --i)	{
			JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
			child.dispose();
		}	
		goBetween.removeJOGLPeer(this);
	}

	public void render()		{
		if (!goBetween.getOriginalComponent().isVisible()) {
			return;
		}
		preRender();
		renderChildren();
		postRender();
	}


	private void preRender() {
		if (renderRunnableDirty) updateRenderRunnable();
		jr.currentPath.push(goBetween.getOriginalComponent());
		jr.context.setCurrentPath(jr.currentPath);
		Transformation thisT = goBetween.getOriginalComponent().getTransformation();

		if (thisT != null && !isIdentity)
			pushTransformation(thisT.getMatrix());

		if (eAp != null) {
			jr.currentSignature = eAp.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
			jr.renderingState.setCurrentSignature(jr.currentSignature);
		}
		if (parent != null) cumulativeIsReflection = (isReflection != parent.cumulativeIsReflection);
		else cumulativeIsReflection = (isReflection != jr.globalIsReflection);
		if (cumulativeIsReflection != jr.renderingState.flipped)	{
			jr.globalGL.glFrontFace(cumulativeIsReflection ? GL.GL_CW : GL.GL_CCW);
			jr.renderingState.flipped  = cumulativeIsReflection;
		}

		if (geometryDirtyBits  != 0)	handleChangedGeometry();
		if (originalAppearanceDirty) propagateAppearanceChanged();
		if (appearanceDirty || effectiveAppearanceDirty)  	handleAppearanceChanged();
		if (goBetween != null && goBetween.peerGeometry != null && goBetween.peerGeometry.originalGeometry != null )	{
			Scene.executeReader(goBetween.peerGeometry.originalGeometry, renderGeometry );
//			if (jr.renderingState.insideDisplayList) 
//				theLog.finer("rendering geometry: "+goBetween.peerGeometry.originalGeometry.getName());
		}
	}
	protected void renderChildren() {
//		theLog.finest("Processing sgc, clipToCamera is "+goBetween.originalComponent.getName()+" "+clipToCamera);
		int n = children.size();

		for (int i = 0; i<n; ++i)	{	
			JOGLPeerComponent child = children.get(i);					
			if (jr.pickMode)	jr.globalGL.glPushName(JOGLPickAction.SGCOMP_BASE+child.childIndex);
			child.render();
			if (jr.pickMode)	jr.globalGL.glPopName();
		}
	}

	private void postRender() {
		if (goBetween.getOriginalComponent().getTransformation() != null && !isIdentity) 
			popTransformation();			
		jr.currentPath.pop();
	}

	protected void pushTransformation(double[] m) {
		if ( jr.stackDepth <= JOGLRenderer.MAX_STACK_DEPTH) {
			jr.globalGL.glPushMatrix();
			jr.globalGL.glMultTransposeMatrixd(m,0);
			jr.stackDepth++;
		}
		else {
			jr.globalGL.glLoadTransposeMatrixd(jr.context.getObjectToCamera(),0);	
		}
	}

	protected void popTransformation() {
		if (jr.stackDepth <= JOGLRenderer.MAX_STACK_DEPTH) {
			jr.globalGL.glPopMatrix();			
			jr.stackDepth--;
		}
	}

	protected void setIndexOfChildren()	{
		childlock.readLock();
		int n = goBetween.getOriginalComponent().getChildComponentCount();
		for (int i = 0; i<n; ++i)	{
			SceneGraphComponent sgc = goBetween.getOriginalComponent().getChildComponent(i);
			JOGLPeerComponent jpc = getPeerForChildComponent(sgc);
			if (jpc == null)	{
				theLog.log(Level.WARNING,"No peer for sgc "+sgc.getName());
				jpc.childIndex = -1;
			} else jpc.childIndex = i;
		}									
		childlock.readUnlock();

	}

	private JOGLPeerComponent getPeerForChildComponent(SceneGraphComponent sgc) {
		childlock.readLock();
		for (JOGLPeerComponent jpc : children)	{
			if ( jpc.goBetween.getOriginalComponent() == sgc) { // found!
				return jpc;
			}
		}
		childlock.readUnlock();
		return null;
	}

	public void appearanceChanged(AppearanceEvent ev) {
		originalAppearanceDirty = true;
	}

	protected void propagateAppearanceChanged()	{
		appearanceDirty = true;
		for (JOGLPeerComponent child : children) {
			if (effectiveAppearanceDirty) child.effectiveAppearanceDirty=true;
			child.propagateAppearanceChanged();
		}	
		originalAppearanceDirty = false;
	}

	private void handleAppearanceChanged() {
		Appearance thisAp = goBetween.getOriginalComponent().getAppearance(); 
		if (parent == null)	{
			if (eAp == null || eAp.getAppearanceHierarchy().indexOf(thisAp) == -1) {
				eAp = EffectiveAppearance.create();
				if (goBetween.getOriginalComponent().getAppearance() != null )	
					eAp = eAp.create(goBetween.getOriginalComponent().getAppearance());
			} 
		} else {
			if ( parent.eAp == null)	{
				throw new IllegalStateException("Parent must have effective appearance");
			}
			if (effectiveAppearanceDirty || eAp == null)	{
				theLog.finer("updating eap for "+goBetween.getOriginalComponent().getName());
				if (thisAp != null )	{
					eAp = parent.eAp.create(thisAp);
				} else {
					eAp = parent.eAp;	
				}
			}
		}
		updateShaders();
		effectiveAppearanceDirty = false;
		appearanceDirty = false;
	}

	/**
	 * @param thisAp
	 */
	protected void updateShaders() {
//		can happen that the effective appearance isn't initialized yet; skip
		if (eAp == null) return; 
		Appearance thisAp = goBetween.getOriginalComponent().getAppearance(); 
		if (thisAp == null && goBetween.getOriginalComponent().getGeometry() == null && parent != null)	{
			geometryShader = parent.geometryShader;
			renderingHints = parent.renderingHints;

		} else  {		
			theLog.log(Level.FINER,"Updating shaders for "+goBetween.originalComponent.getName());
			if (geometryShader == null)
				geometryShader = DefaultGeometryShader.createFromEffectiveAppearance(eAp, "");
			else 
				geometryShader.setFromEffectiveAppearance(eAp, "");

			if (renderingHints == null)
				renderingHints = RenderingHintsShader.createFromEffectiveAppearance(eAp, "");
			else
				renderingHints.setFromEffectiveAppearance(eAp, "");								
		}
//		System.err.println("Clip to camera is "+clipToCamera);
	}

	public void childAdded(SceneGraphComponentEvent ev) {
		theLog.log(Level.FINE,"JOGLPeerComponent: Container Child added to: "+goBetween.getOriginalComponent().getName());
		//theLog.log(Level.FINE,"Event is: "+ev.toString());
		switch (ev.getChildType() )	{
		case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
			updateRenderRunnable();
			break;

		case SceneGraphComponentEvent.CHILD_TYPE_COMPONENT:
			SceneGraphComponent sgc = (SceneGraphComponent) ev.getNewChildElement();
			JOGLPeerComponent pc = jr.constructPeerForSceneGraphComponent(sgc, this);
			//childlock.writeLock();
			//theLog.log(Level.FINE,"Before adding child count is "+children.size());
			children.add(pc);						
			//childlock.writeUnlock();
			//theLog.log(Level.FINE,"After adding child count is "+children.size());
			setIndexOfChildren();
			jr.lightListDirty = true;
			break;
		case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
			handleNewAppearance();
			theLog.log(Level.FINE,"Propagating geometry change due to added appearance");
			break;				
		case SceneGraphComponentEvent.CHILD_TYPE_LIGHT:
			jr.lightListDirty = true;
			break;
		case SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION:
			updateTransformationInfo();
			break;
		default:
			theLog.log(Level.INFO,"Taking no action for addition of child type "+ev.getChildType());
		break;
		}
	}

	public void childRemoved(SceneGraphComponentEvent ev) {
		theLog.log(Level.FINE,"Container Child removed from: "+goBetween.getOriginalComponent().getName());
		switch (ev.getChildType() )	{
		case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
			updateRenderRunnable();
			break;

		case SceneGraphComponentEvent.CHILD_TYPE_COMPONENT:
			SceneGraphComponent sgc = (SceneGraphComponent) ev.getOldChildElement();
			JOGLPeerComponent jpc = getPeerForChildComponent(sgc);
			if (jpc == null) return;
			//childlock.writeLock();
			children.remove(jpc);						
			//childlock.writeUnlock();
			//theLog.log(Level.FINE,"After removal child count is "+children.size());
			//jpc.dispose();		// there are no other references to this child
			setIndexOfChildren();
			jr.lightListDirty = true;
			break;
		case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
			handleNewAppearance();
			theLog.log(Level.FINE,"Propagating geometry change due to removed appearance");
			break;				
		case SceneGraphComponentEvent.CHILD_TYPE_LIGHT:
			jr.lightListDirty = true;
			break;
		case SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION:
			updateTransformationInfo();
			break;			
		default:
			theLog.log(Level.INFO,"Taking no action for removal of child type "+ev.getChildType());
		break;
		}
	}

	public void childReplaced(SceneGraphComponentEvent ev) {
		theLog.log(Level.FINE,"Container Child replaced at: "+goBetween.getOriginalComponent().getName());
		switch(ev.getChildType())	{
		case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
			renderRunnableDirty = true; 
			break;

		case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
			handleNewAppearance();
			theLog.log(Level.INFO,"Propagating geometry change due to replaced appearance");
			break;
		case SceneGraphComponentEvent.CHILD_TYPE_LIGHT:
			jr.lightListDirty = true;
			break;
		case SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION:
			updateTransformationInfo();
			break;
		default:
			theLog.log(Level.INFO,"Taking no action for replacement of child type "+ev.getChildType());
		break;
		}
	}

	public void transformationMatrixChanged(TransformationEvent ev) {
		updateTransformationInfo();
	}

	/**
	 * 
	 */
	protected void updateTransformationInfo() {
		if (goBetween.getOriginalComponent().getTransformation() != null) {
//			isReflection = goBetween.getOriginalComponent().getTransformation().getIsReflection();
			isReflection = Rn.determinant(goBetween.getOriginalComponent().getTransformation().getMatrix()) < 0;
			isIdentity = Rn.isIdentityMatrix(goBetween.getOriginalComponent().getTransformation().getMatrix(), 10E-8);
		} else {
			determinant  = 0.0;
			isReflection = false;
		}
	}

	private void handleNewAppearance() {
		LoggingSystem.getLogger(this).finer("handle new appearance "+goBetween.originalComponent.getName());
		propagateGeometryChanged(ALL_CHANGED);
		appearanceDirty = true;
		effectiveAppearanceDirty=true;
	}

	public void propagateGeometryChanged(int changed) {
//		if (jr.renderingState.manyDisplayLists) _propagateGeometryChanged(ALL_CHANGED);
//		else jr.displayListsDirty=true;
//	}
//	
//	public void _propagateGeometryChanged(int changed)	{
		geometryDirtyBits  = changed;
//		handleChangedGeometry();
		childlock.readLock();
		for (JOGLPeerComponent child: children){		
			child.propagateGeometryChanged(changed);
		}	
		childlock.readUnlock();

	}

	protected void handleChangedGeometry() {
		if (geometryShader != null)	{
//			theLog.fine("Handling bits: "+geometryDirtyBits+" for "+goBetween.originalComponent.getName());
			if (geometryShader.pointShader != null && (geometryDirtyBits  & POINTS_CHANGED) != 0) 
				geometryShader.pointShader.flushCachedState(jr);
			if (geometryShader.lineShader != null && (geometryDirtyBits  & LINES_CHANGED) != 0) 
				geometryShader.lineShader.flushCachedState(jr);
			if (geometryShader.polygonShader != null && (geometryDirtyBits  & FACES_CHANGED) != 0) 
				geometryShader.polygonShader.flushCachedState(jr);				
			if ((geometryDirtyBits  & POINT_SHADER_CHANGED) != 0)geometryShader.pointShader = null;
			if ((geometryDirtyBits  & LINE_SHADER_CHANGED) != 0) geometryShader.lineShader = null;
			if ((geometryDirtyBits  & POLYGON_SHADER_CHANGED) != 0) geometryShader.polygonShader = null;
			if ((geometryDirtyBits  & ALL_SHADERS_CHANGED) != 0)updateShaders();
            geometryDirtyBits  = 0;
		}
	}

	void setDisplayListDirty()	{
        geometryDirtyBits = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
 	}

	public SceneGraphComponent getOriginalComponent() {
		return goBetween.getOriginalComponent();
	}

	public void visibilityChanged(SceneGraphComponentEvent ev) {
	}
}
