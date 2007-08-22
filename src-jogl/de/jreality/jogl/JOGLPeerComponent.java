/*
 * Created on Jan 14, 2007
 *
 */
package de.jreality.jogl;

import java.util.Vector;
import java.util.logging.Level;

import javax.media.opengl.GL;

import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.RenderingHintsShader;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Lock;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
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
	protected int childIndex, signature = Pn.EUCLIDEAN;
	protected GoBetween goBetween;
	double determinant = 0.0;
	double[] cachedTform = new double[16];
	boolean useTformCaching = true;

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
		renderRunnableDirty = true,
		isVisible = true;
	int geometryDirtyBits  = ALL_GEOMETRY_CHANGED, displayList = -1;
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
	public static int count = 0;
	static boolean debug = false;
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
		name = "JOGLPeer:"+goBetween.originalComponent.getName();
		children = new Vector<JOGLPeerComponent>();
		parent = p;
		updateTransformationInfo();
	 	isVisible = goBetween.originalComponent.isVisible();
		count++;
	}
	
	protected void updateRenderRunnable() {
		setDisplayListDirty();
		geometryDirtyBits = ALL_GEOMETRY_CHANGED;
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
		if (!isVisible) {
			return;
		}
		preRender();
		renderChildren();
		postRender();
	}

	boolean mustPop = false, oldFlipped;
	private void preRender() {
		if (renderRunnableDirty) updateRenderRunnable();
		jr.currentPath.push(goBetween.originalComponent);
		if (debug) theLog.finer("prerender: "+name);
		if (useTformCaching)	{
			if (cachedTform != null && !isIdentity)  {
				pushTransformation(cachedTform); //thisT.getMatrix());
				mustPop = true;
			} 
		} else if (goBetween.originalComponent.getTransformation() != null){
			pushTransformation(goBetween.originalComponent.getTransformation().getMatrix());
			mustPop = true;
		}

		oldFlipped = jr.renderingState.flipped;
		jr.renderingState.flipped = isReflection ^ jr.renderingState.flipped;
		if (oldFlipped != jr.renderingState.flipped) {
			jr.globalGL.glFrontFace(jr.renderingState.flipped ? GL.GL_CW : GL.GL_CCW);
		}

		if (geometryDirtyBits  != 0)	handleChangedGeometry();
		if (originalAppearanceDirty) propagateAppearanceChanged();
		if (appearanceDirty || effectiveAppearanceDirty)  	handleAppearanceChanged();
		jr.renderingState.currentSignature = signature;
		if (renderGeometry != null )	{
			Scene.executeReader(goBetween.peerGeometry.originalGeometry, renderGeometry );
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
		if (mustPop) {
			popTransformation();			
			mustPop = false;
		}
		if (jr.renderingState.flipped != oldFlipped)	{
			jr.renderingState.flipped = oldFlipped;
			jr.globalGL.glFrontFace(jr.renderingState.flipped ? GL.GL_CW : GL.GL_CCW);
		}
		jr.currentPath.pop();
	}

	protected void pushTransformation(double[] m) {
		if ( jr.stackDepth < JOGLRenderer.MAX_STACK_DEPTH) {
			jr.globalGL.glPushMatrix();
			jr.globalGL.glMultTransposeMatrixd(m,0);
		}
		else {
//			System.err.println("o2c: "+Rn.matrixToString(jr.context.getObjectToCamera()));
			int stackCounter = jr.stackDepth - JOGLRenderer.MAX_STACK_DEPTH;
			if (stackCounter == 0)	{
				jr.matrixStack[0] = new Matrix(jr.context.getObjectToCamera());	
			} else {
				if (stackCounter >= jr.matrixStack.length)	{
				Matrix[] newstack = new Matrix[jr.matrixStack.length*2];
				System.arraycopy(jr.matrixStack, 0, newstack, 0, jr.matrixStack.length);
				jr.matrixStack = newstack;
			}
				if (jr.matrixStack[stackCounter] == null) jr.matrixStack[stackCounter] = new Matrix();
		    	Rn.times(jr.matrixStack[stackCounter].getArray(), jr.matrixStack[stackCounter-1].getArray(), cachedTform);				
			}
//			jr.globalGL.glLoadTransposeMatrixd(jr.context.getObjectToCamera(),0);	
			jr.globalGL.glLoadTransposeMatrixd(jr.matrixStack[stackCounter].getArray(),0);	
		}
		jr.stackDepth++;
	}

	protected void popTransformation() {
		if (jr.stackDepth <= JOGLRenderer.MAX_STACK_DEPTH) {
			jr.globalGL.glPopMatrix();			
		}
		jr.stackDepth--;
	}

	protected void setIndexOfChildren()	{
		childlock.readLock();
		int n = goBetween.originalComponent.getChildComponentCount();
		for (int i = 0; i<n; ++i)	{
			SceneGraphComponent sgc = goBetween.originalComponent.getChildComponent(i);
			JOGLPeerComponent jpc = getPeerForChildComponent(sgc);
			if (jpc == null)	{
				theLog.log(Level.WARNING,"No peer for sgc "+sgc.getName());
				jpc.childIndex = -1;
			} else jpc.childIndex = i;
		}									
		childlock.readUnlock();

	}

	private JOGLPeerComponent getPeerForChildComponent(SceneGraphComponent sgc) {
//		childlock.readLock();
		for (JOGLPeerComponent jpc : children)	{
			if ( jpc.goBetween.originalComponent == sgc) { // found!
				return jpc;
			}
		}
//		childlock.readUnlock();
		return null;
	}

	public void appearanceChanged(AppearanceEvent ev) {
		//LoggingSystem.getLogger(this).finer("JOGLPeerComponent: appearance changed: "+goBetween.getOriginalComponent().getName());
		originalAppearanceDirty = true;
	}

	protected void propagateAppearanceChanged()	{
		if (debug) LoggingSystem.getLogger(this).finer("JOGLPeerComponent: propagate: "+name);
		appearanceDirty = true;
		for (JOGLPeerComponent child : children) {
			if (effectiveAppearanceDirty) child.effectiveAppearanceDirty=true;
			child.propagateAppearanceChanged();
		}	
		originalAppearanceDirty = false;
	}

	private void handleAppearanceChanged() {
		Appearance thisAp = goBetween.originalComponent.getAppearance(); 
		if (parent == null)	{
			if (eAp == null || eAp.getAppearanceHierarchy().indexOf(thisAp) == -1) {
				eAp = EffectiveAppearance.create();
				if (thisAp != null )	
					eAp = eAp.create(thisAp);
			} 
		} else {
			if ( parent.eAp == null)	{
				throw new IllegalStateException("Parent must have effective appearance"+name);
			}
			if (effectiveAppearanceDirty || eAp == null)	{
				if (debug) theLog.finer("updating eap for "+name);
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
		signature = eAp.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
		if (goBetween.originalComponent.getGeometry() == null) return;
		Appearance thisAp = goBetween.originalComponent.getAppearance(); 
		if (thisAp == null && goBetween.originalComponent.getGeometry() == null && parent != null)	{
			geometryShader = parent.geometryShader;
			renderingHints = parent.renderingHints;

		} else  {		
			if (debug) theLog.log(Level.FINER,"Updating shaders for "+name);
			if (geometryShader == null)
				geometryShader = DefaultGeometryShader.createFromEffectiveAppearance(eAp, "");
			else 
				geometryShader.setFromEffectiveAppearance(eAp, "");

			if (renderingHints == null)
				renderingHints = RenderingHintsShader.createFromEffectiveAppearance(eAp, "");
			else
				renderingHints.setFromEffectiveAppearance(eAp, "");								
		}
//		System.err.println(goBetween.getOriginalComponent().getName()+" signature is "+signature);
//		System.err.println("Clip to camera is "+clipToCamera);
	}

	protected boolean someSubNodeIsDirty()	{
		if (isVisible && goBetween.peerGeometry != null && geometryDirtyBits != 0) return true;
		for (JOGLPeerComponent child : children) {
			if (child.someSubNodeIsDirty()) return true;
		}
		return false;
	}
	
//	protected boolean shadersAreDirty()	{
//		if (geometryShader == null) return false;
//		boolean ret = false;
//		if (geometryDirtyBits != 0) ret = true;
//		else if (geometryShader.isFaceDraw() && geometryShader.polygonShader != null &&
//			geometryShader.polygonShader.displayListsDirty()) ret = true;
//		else if (geometryShader.isEdgeDraw() && geometryShader.lineShader != null &&
//			geometryShader.lineShader.displayListsDirty()) ret = true;
//		else if (geometryShader.isVertexDraw() && geometryShader.pointShader != null &&
//			geometryShader.pointShader.displayListsDirty()) ret = true;
//		if (ret) System.err.println(name+" shaders are dirty");
//		return ret;
//	}
	public void childAdded(SceneGraphComponentEvent ev) {
		if (debug) theLog.finest("JOGLPeerComponent: Container Child added to: "+name);
		//theLog.log(Level.FINE,"Event is: "+ev.toString());
		switch (ev.getChildType() )	{
		case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
			renderRunnableDirty = true;
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
			if (debug) theLog.log(Level.FINE,"Propagating geometry change due to added appearance");
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
		// to be safe, we turn on clipping planes dirty
		jr.clippingPlanesDirty = true;
	}

	public void childRemoved(SceneGraphComponentEvent ev) {
		if (debug) theLog.finest("Container Child removed from: "+name);
		switch (ev.getChildType() )	{
		case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
			renderRunnableDirty = true;
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
			if (debug) theLog.log(Level.FINE,"Propagating geometry change due to removed appearance");
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
		jr.clippingPlanesDirty = true;
	}

	public void childReplaced(SceneGraphComponentEvent ev) {
		if (debug) theLog.finest("Container Child replaced at: "+name);
		switch(ev.getChildType())	{
		case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
			renderRunnableDirty = true; 
			break;

		case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
			handleNewAppearance();
			if (debug) theLog.log(Level.INFO,"Propagating geometry change due to replaced appearance");
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
		jr.clippingPlanesDirty = true;
	}

	public void transformationMatrixChanged(TransformationEvent ev) {
		updateTransformationInfo();
	}

	/**
	 * 
	 */
	protected void updateTransformationInfo() {
		if (goBetween.originalComponent.getTransformation() != null) {
			isReflection = Rn.determinant(goBetween.originalComponent.getTransformation().getMatrix()) < 0;
			isIdentity = Rn.isIdentityMatrix(goBetween.originalComponent.getTransformation().getMatrix(), 10E-8);
			cachedTform = goBetween.originalComponent.getTransformation().getMatrix(cachedTform);
		} else {
			determinant  = 0.0;
			isReflection = false;
			isIdentity = true;
			cachedTform = null;
		}
	}

	private void handleNewAppearance() {
		if (debug) LoggingSystem.getLogger(this).finer("handle new appearance "+name);
		propagateGeometryChanged(ALL_CHANGED);
		appearanceDirty = true;
		effectiveAppearanceDirty=true;
	}

	public void propagateGeometryChanged(int changed) {
		geometryDirtyBits  = changed;
		childlock.readLock();
		for (JOGLPeerComponent child: children){		
			child.propagateGeometryChanged(changed);
		}	
		childlock.readUnlock();

	}

	protected void handleChangedGeometry() {
		if (goBetween.peerGeometry != null)	{
			if (geometryShader == null) updateShaders();
			if (geometryShader == null) return;
			//theLog.fine("Handling bits: "+geometryDirtyBits+" for "+name);
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
		return goBetween.originalComponent;
	}

	public void visibilityChanged(SceneGraphComponentEvent ev) {
		isVisible = ev.getSceneGraphComponent().isVisible();
	}

}
