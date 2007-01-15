/*
 * Created on Jan 14, 2007
 *
 */
package de.jreality.jogl;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL;

import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.RenderingHintsShader;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
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

	public int[] bindings = new int[2];
	protected EffectiveAppearance eAp;
	protected Vector<JOGLPeerComponent> children;
	protected JOGLPeerComponent parent;
	protected int childIndex;
	protected GoBetween goBetween;

	protected boolean isReflection = false;
	boolean isCopyCat = false;
	protected boolean cumulativeIsReflection = false;
	double determinant = 0.0;

	RenderingHintsShader renderingHints;
	DefaultGeometryShader geometryShader;

	Lock childlock = new Lock();
	protected Runnable renderGeometry = null;
	final JOGLPeerComponent self = this;
	double[][] matrices = null;
	double minDistance = -1, maxDistance = -1;
	protected boolean appearanceDirty = true, originalAppearanceDirty = false;
	boolean effectiveAppearanceDirty = true,
	geometryIsDirty = true,
	boundIsDirty = true,
	clipToCamera = true;
	int geometryDirtyBits  = 0;
	protected boolean renderRunnableDirty = true;
	boolean[] matrixIsReflection = null;
	double[] tform = new double[16];		// for optimized access to matrix
	Vector<SceneGraphComponentEvent> newSGCEvents = new Vector<SceneGraphComponentEvent>();

	protected final static int POINTS_CHANGED = 1;
	protected final static int LINES_CHANGED = 2;
	protected final static int FACES_CHANGED = 4;
	protected final static int ALL_GEOMETRY_CHANGED = 7;
	protected final static int POINT_SHADER_CHANGED = 8;
	protected final static int LINE_SHADER_CHANGED = 16;
	protected final static int POLYGON_SHADER_CHANGED = 32;
	protected final static int ALL_SHADERS_CHANGED = POINT_SHADER_CHANGED | LINE_SHADER_CHANGED | POLYGON_SHADER_CHANGED;
	protected final static int ALL_CHANGED = ALL_GEOMETRY_CHANGED | ALL_SHADERS_CHANGED;

	public JOGLPeerComponent(SceneGraphPath sgp, JOGLPeerComponent p, JOGLRenderer jr)		{
		super(jr);
		if (sgp == null || !(sgp.getLastElement() instanceof SceneGraphComponent))  {
			throw new IllegalArgumentException("Not a valid SceneGraphComponenet");
		}
		goBetween = jr.goBetweenFor(sgp.getLastComponent());
		goBetween.addJOGLPeer(this);
		name = "JOGLPeer:"+goBetween.getOriginalComponent().getName();
//		isCopyCat = goBetween.getOriginalComponent() instanceof JOGLMultipleComponent;
		Geometry foo = goBetween.getOriginalComponent().getGeometry();
		isCopyCat = (foo != null &&  foo instanceof PointSet && foo.getGeometryAttributes(JOGLConfiguration.COPY_CAT) != null);
		if (isCopyCat)	{
			matrices = ((PointSet) foo).getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
			matrixIsReflection = new boolean[matrices.length];
			for (int i = 0; i<matrices.length; ++i)	matrixIsReflection[i] = Rn.determinant(matrices[i]) < 0.0;
		}
		children = new Vector<JOGLPeerComponent>();		// always have a child list, even if it's empty
		parent = p;
		updateTransformationInfo();
	}

	protected void addSceneGraphComponentEvent(SceneGraphComponentEvent ev)	{
		newSGCEvents.add(ev);
	}
	
	protected void updateRenderRunnable() {
		setDisplayListDirty();
//		updateShaders();
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
		if (!goBetween.getOriginalComponent().isVisible()) return;
		Transformation thisT = preRender();
		//System.err.println(goBetween.getOriginalComponent().getName()+"Signature is "+currentSignature);
		renderChildren();
		postRender(thisT);
	}


	private Transformation preRender() {
		if (goBetween.originalComponent.getAppearance() != null) 
			theLog.finest("Processing appearance "+goBetween.originalComponent.getAppearance().getName());
		jr.nodeCount++;
		if (renderRunnableDirty) updateRenderRunnable();
		jr.currentPath.push(goBetween.getOriginalComponent());
		jr.context.setCurrentPath(jr.currentPath);
		Transformation thisT = goBetween.getOriginalComponent().getTransformation();

		if (thisT != null) {
			pushTransformation(thisT.getMatrix());
		}

		if (eAp != null) {
			jr.currentSignature = eAp.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
			jr.renderingState.setCurrentSignature(jr.currentSignature);
			//System.err.println(goBetween.getOriginalComponent().getName()+" Setting sig to "+currentSignature);
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
		if (!isCopyCat && goBetween != null && goBetween.peerGeometry != null && goBetween.peerGeometry.originalGeometry != null )	{
			Scene.executeReader(goBetween.peerGeometry.originalGeometry, renderGeometry );
		}
		return thisT;
	}
	protected void renderChildren() {
		if (newSGCEvents.size() != 0)	{
			for (SceneGraphComponentEvent ev : newSGCEvents)	{
				int type = ev.getEventType();
				if (type == SceneGraphComponentEvent.EVENT_TYPE_ADDED)
					childAdded(ev);
				else if (type == SceneGraphComponentEvent.EVENT_TYPE_REMOVED)
					childRemoved(ev);
				else if (type == SceneGraphComponentEvent.EVENT_TYPE_REPLACED)
					childReplaced(ev);
			}
			newSGCEvents.clear();
		}
		int n = children.size();

		if (isCopyCat)		{
			boolean isReflectionBefore = cumulativeIsReflection;
			if (JOGLConfiguration.testMatrices) {
				minDistance = eAp.getAttribute("discreteGroup.minDistance", minDistance);
				maxDistance = eAp.getAttribute("discreteGroup.maxDistance", maxDistance);
				clipToCamera = eAp.getAttribute("discreteGroup.clipToCamera", clipToCamera);					
			}

			int nn = matrices.length;
			double[] o2ndc = jr.context.getObjectToNDC();
			double[] o2c = jr.context.getObjectToCamera();
			int count = 0;
			for (int j = 0; j<nn; ++j)	{
				if (JOGLConfiguration.testMatrices) 
					if (clipToCamera && !JOGLRendererHelper.accept(o2ndc, o2c, minDistance, maxDistance, matrices[j], jr.currentSignature)) continue;
				count++;
				cumulativeIsReflection = (isReflectionBefore != matrixIsReflection[j]);
				if (cumulativeIsReflection != jr.renderingState.flipped)	{
					jr.globalGL.glFrontFace(cumulativeIsReflection ? GL.GL_CW : GL.GL_CCW);
					jr.renderingState.flipped  = cumulativeIsReflection;
				}
				pushTransformation(matrices[j]);

				for (int i = 0; i<n; ++i)	{		
					JOGLPeerComponent child = children.get(i);					
					child.render();
				}				
				popTransformation();
			}
			//System.err.println("Matrix count: "+count);
		} else {
			for (int i = 0; i<n; ++i)	{		
				JOGLPeerComponent child = children.get(i);					
				if (jr.pickMode)	jr.globalGL.glPushName(JOGLPickAction.SGCOMP_BASE+child.childIndex);
				child.render();
				if (jr.pickMode)	jr.globalGL.glPopName();
			}								
		}
	}

	private void postRender(Transformation thisT) {
		if (thisT != null) popTransformation();			
		jr.currentPath.pop();
	}


	protected void popTransformation() {
		if (jr.stackDepth <= JOGLRenderer.MAX_STACK_DEPTH) {
			jr.globalGL.glPopMatrix();			
			jr.stackDepth--;
		}
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
		//childlock.readUnlock();
		//appearanceDirty=false;
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
	private void updateShaders() {
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

	private void handleNewAppearance() {
		LoggingSystem.getLogger(this).finer("handle new appearance "+goBetween.originalComponent.getName());
		int changed = ALL_CHANGED;
		propagateGeometryChanged(changed);	
		appearanceDirty = true;
		effectiveAppearanceDirty=true;
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
			jpc.dispose();		// there are no other references to this child
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
		// TODO notify ancestors that their bounds are no longer valid
		updateTransformationInfo();
	}

	/**
	 * 
	 */
	private void updateTransformationInfo() {
		if (goBetween.getOriginalComponent().getTransformation() != null) {
//			isReflection = goBetween.getOriginalComponent().getTransformation().getIsReflection();
			isReflection = Rn.determinant(goBetween.getOriginalComponent().getTransformation().getMatrix()) < 0;
		} else {
			determinant  = 0.0;
			isReflection = false;
		}
	}

	public void propagateGeometryChanged(int changed) {
//		theLog.finer("set bits to "+changed);
		geometryDirtyBits  = changed;
		childlock.readLock();
		for (JOGLPeerComponent child: children){		
			child.propagateGeometryChanged(changed);
		}	
		childlock.readUnlock();

	}

	private void handleChangedGeometry() {
		if (geometryShader != null)	{
//			theLog.fine("Handling bits: "+geometryDirtyBits+" for "+goBetween.originalComponent.getName());
			if (geometryShader.pointShader != null && (geometryDirtyBits  & POINTS_CHANGED) != 0) geometryShader.pointShader.flushCachedState(jr);
			if (geometryShader.lineShader != null && (geometryDirtyBits  & LINES_CHANGED) != 0) geometryShader.lineShader.flushCachedState(jr);
			if (geometryShader.polygonShader != null && (geometryDirtyBits  & FACES_CHANGED) != 0) geometryShader.polygonShader.flushCachedState(jr);				
			if ((geometryDirtyBits  & POINT_SHADER_CHANGED) != 0) geometryShader.pointShader = null;
			if ((geometryDirtyBits  & LINE_SHADER_CHANGED) != 0) geometryShader.lineShader = null;
			if ((geometryDirtyBits  & POLYGON_SHADER_CHANGED) != 0) geometryShader.polygonShader = null;
			if ((geometryDirtyBits  & ALL_SHADERS_CHANGED) != 0)updateShaders();
			// set the dirty flag to clean again
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
