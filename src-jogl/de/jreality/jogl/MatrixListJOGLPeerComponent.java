package de.jreality.jogl;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import sun.awt.PeerEvent;

import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.SceneGraphComponentEvent;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SystemProperties;

/**
 * This is an alternative class for use in conjunction with special sorts of
 * scene graphs with many identical children, as one finds in discrete groups.
 * 
 * To activate use, run java with "-DdiscreteGroup.copycat=true" set
 * @author Charles Gunn
 *
 */
public class MatrixListJOGLPeerComponent extends JOGLPeerComponent {

	boolean displayListDirty = true;
//    boolean displayListDirtyUp = false;
//    boolean childrenDLDirty = true;
	boolean isCopyCat = false,
		isTopCat = false,
		isCameraRepn = false,
		visibilityChanged = false,
		useOldMatrices = false;
	boolean insideDL = false;
	double[][] matrices = null;
	boolean[] accepted;
	SceneGraphPath w2camrepn = null;
	double[] world2CameraRepn = new double[16], camera2CameraRepn = new double[16];
	int framecount = -1;
	protected boolean[] matrixIsReflection = null;
	GL oldGL;
	public static int instanceCount;
	MatrixListData theDropBox;
	public MatrixListJOGLPeerComponent()	{
		super();
		instanceCount++;
	}
	
	public MatrixListJOGLPeerComponent(SceneGraphPath sgp, JOGLPeerComponent p, JOGLRenderer jr)		{
		super(sgp, p, jr);
		instanceCount++;
	}
	
	@Override
	public void init(SceneGraphPath sgp, JOGLPeerComponent p, JOGLRenderer jr) {
		super.init(sgp,p,jr);
		Geometry trojanHorse = goBetween.originalComponent.getGeometry();
		isCopyCat = (trojanHorse != null &&  
				trojanHorse instanceof MatrixListData);
		if (isCopyCat)	{
			goBetween.peerGeometry = null;		// this isn't really a geometry
			theDropBox = (MatrixListData) trojanHorse;
			readMatrices();
		}
	}

	private void readMatrices() {
		matrices = theDropBox.matrixList;
		matrixIsReflection = new boolean[matrices.length];
		accepted = new boolean[matrices.length];
		for (int i = 0; i<matrices.length; ++i)	{
			matrixIsReflection[i] = Rn.determinant(matrices[i]) < 0.0;
		}
		
//		System.err.println("Updating matrices length "+matrices.length);
	}

	@Override
	public void render() throws GLException {
		if (!isVisible) return;
		if (childCount == 0 && goBetween.peerGeometry == null) return;
		insideDL = false;
		useOldMatrices = true;
		if (isCopyCat) {
//			System.err.println("Matrices length "+((PointSet) trojanHorse).getVertexAttributes(Attribute.COLORS).size());
			if (matrices.length != theDropBox.matrixList.length) {
				readMatrices();				
				displayListDirty = true;			
			}
//			if (displayListDirty) setDisplayListDirty();
			// figure out whether to use display lists
			if (jr.beginRenderTime - currentTime > theDropBox.delay)	{
				currentTime = jr.beginRenderTime;
				useOldMatrices = false;
				displayListDirty = true;
			}
			jr.renderingState.componentDisplayLists = theDropBox.componentDisplayLists;
			if (jr.renderingState.componentDisplayLists  && 
				jr.renderingState.useDisplayLists &&
				!jr.offscreenMode) {
				if (oldGL != jr.globalGL)	{
					displayListDirty = true;
					oldGL = jr.globalGL;
				}
				//System.err.println("dirty subgraph = "+dirtySubGraph);
				if (!someSubNodeIsDirty()) {  // we can't use the current display list AND we can't generate a new one
					if (!displayListDirty)	{
						jr.globalGL.glCallList(displayList);
						//System.err.println("dgjoglpc: Calling dlist");
						return;
					}
					if (jr.renderingState.insideDisplayList) {
						//throw new IllegalStateException("Already inside display list");
						printUpState();
					}
					if (displayList < 0) displayList = jr.globalGL.glGenLists(1);
					jr.globalGL.glNewList(displayList, GL.GL_COMPILE);
					insideDL = jr.renderingState.insideDisplayList = true;
//					theLog.fine("Turning on dlist for "+goBetween.getOriginalComponent().getName());
				}	
			}
		}
		try {
			super.render();			
		} catch (GLException e) {
			System.err.println("Got exception "+name);
//			e.printStackTrace();
			if (theDropBox != null) setDisplayListDirty();
			else throw e;
		}
		if (insideDL)	{
			jr.globalGL.glEndList();
			jr.globalGL.glCallList(displayList);
//	displayListDirty = jr.renderingState.insideDisplayList = false;
		} 
		displayListDirty = false;
//		else if (!isCopyCat) {
//			if (goBetween.peerGeometry != null && !goBetween.peerGeometry.displayListsDirty) 
//				geometryDLDirty = false;
//			displayListDirty = childrenDLDirty || geometryDLDirty;	
//			if (displayListDirty) setDisplayListDirty();
//		}
	}
	@Override
	protected boolean someSubNodeIsDirty()	{
		if (!isVisible) return false;
		if (!isCopyCat) 	{
			if (displayListDirty) {
				//System.err.println(name+" is dldirty");
				return true;
			}
			if (goBetween.peerGeometry != null && geometryDirtyBits != 0) {
				return true;
			}
			for (JOGLPeerComponent child : children) {
				if (child.someSubNodeIsDirty()) return true;
			}
			return false;
		}
		JOGLPeerComponent child = children.get(0);
		return (child.someSubNodeIsDirty());
	}
	private void printUpState()	{
		if (debug) theLog.info("dld\tcld:\t"+displayListDirty+"\t"+"\t"+name);
		if (parent!=null)((MatrixListJOGLPeerComponent) parent).printUpState();
	}
	int signature;
	double[] o2c, o2ndc;
	@Override
	protected void renderChildren() {
		if (!jr.offscreenMode && isCopyCat)		{
			signature = jr.renderingState.currentSignature;
			if (debug) theLog.fine("In renderChildren()"+name);
			boolean isReflectionBefore = jr.renderingState.flipped; //cumulativeIsReflection;

			int nn = matrices.length;
			if (theDropBox.clipToCamera && !useOldMatrices)	{
				o2ndc = jr.context.getObjectToNDC();
				o2c = jr.context.getObjectToCamera();	
				inverseDMin = Pn.inverseDistance(theDropBox.minDistance, signature);
				inverseDMax = Pn.inverseDistance(theDropBox.maxDistance, signature);
//				System.err.println("min max: "+inverseDMin+" "+inverseDMax);
			}

			int count = 0;
			MatrixListJOGLPeerComponent child = (MatrixListJOGLPeerComponent) children.get(0);
			for (int j = 0; j<nn; ++j)	{
				if (theDropBox.clipToCamera)	{
					if (!useOldMatrices) 
						accepted[j] = accept(matrices[j]);
					if (!accepted[j]) 	continue; 
				}
				count++;
				cumulativeIsReflection = (isReflectionBefore ^ matrixIsReflection[j]);
				if (cumulativeIsReflection != jr.renderingState.flipped)	{
					jr.globalGL.glFrontFace(cumulativeIsReflection ? GL.GL_CW : GL.GL_CCW);
					jr.renderingState.flipped  = cumulativeIsReflection;
				}
				pushTransformation(matrices[j]);
				child.render();
				popTransformation();
			}
//	childrenDLDirty = child.isVisible ? child.displayListDirty : false;
			theDropBox.count = count;
//			theLog.fine("Rendered "+count);
			jr.renderingState.flipped = isReflectionBefore;
			jr.globalGL.glFrontFace(jr.renderingState.flipped ? GL.GL_CW : GL.GL_CCW);
			jr.renderingState.componentDisplayLists = false;

		} else {
//	childrenDLDirty = false;		// think positive!
			int n = childCount; //children.size();
			for (int i = 0; i<n; ++i)	{	
				JOGLPeerComponent child = children.get(i);					
				if (jr.pickMode)	jr.globalGL.glPushName(JOGLPickAction.SGCOMP_BASE+child.childIndex);
				child.render();
				MatrixListJOGLPeerComponent r = ((MatrixListJOGLPeerComponent) child);
//		if ( r.isVisible ? r.displayListDirty : false) childrenDLDirty = true;
				if (jr.pickMode)	jr.globalGL.glPopName();
			}
		}
	}
	@Override
	protected void pushTransformation(double[] m) {
		if (w2camrepn != null )	{
			// recalculate the matrix if we haven't yet done it this frame
			if (framecount != jr.frameCount) {
				w2camrepn.getInverseMatrix(world2CameraRepn);
				framecount = jr.frameCount;
				Rn.times(camera2CameraRepn,world2CameraRepn,  jr.renderingState.cameraToWorld);
				Rn.times(camera2CameraRepn, camera2CameraRepn , CameraUtility.inverseCameraOrientation.getArray());

//				System.err.println("Setting up final thing");
			}
			super.pushTransformation(camera2CameraRepn);
		} else super.pushTransformation(m);
	}

	@Override
	protected void propagateAppearanceChanged() {
		displayListDirty = true;
		super.propagateAppearanceChanged();
	}

	@Override
	protected void updateShaders() {
		super.updateShaders();
		if (eAp == null )return;
		if (goBetween.originalComponent.getAppearance() != null)	{
			Object foo = goBetween.originalComponent.getAppearance().getAttribute("discreteGroup.cameraRep", SceneGraphPath.class);
			if (foo != null && foo instanceof SceneGraphPath) {
				w2camrepn = (SceneGraphPath) foo;
				LoggingSystem.getLogger(this).finer("Found path in "+w2camrepn);
				useTformCaching = false;
			} else w2camrepn = null;			
		}
		if (!isCopyCat) return;
		isTopCat = (isCopyCat && !existsHigherCat());
	}

	private boolean existsHigherCat()	{
		if (parent != null) 
			if (((MatrixListJOGLPeerComponent) parent).isCopyCat) return true;
			else return ((MatrixListJOGLPeerComponent)parent).existsHigherCat();
		else return false;
	}
	@Override
	protected void updateTransformationInfo() {
		//propagateSGCDisplayListDirtyUp();
		displayListDirty = true;
		super.updateTransformationInfo();
	}

	@Override
	void setDisplayListDirty() {
	    displayListDirty = true;
		super.setDisplayListDirty();
	}

	@Override
	public void visibilityChanged(SceneGraphComponentEvent ev) {
		if (debug) theLog.fine("Visibility changed: "+name);
		//propagateSGCDisplayListDirtyUp();
		displayListDirty = true;
		super.visibilityChanged(ev);
	}
	//o2ndc, o2c, minDistance, maxDistance, matrices[j], jr.renderingState.currentSignature)) continue;
	private double[] mat = new double[16], mat2, mat3;
	private double[] tmp2 = new double[4];
	private double inverseDMin, inverseDMax;
	protected  boolean accept(final double[] m) { //double[] objectToNDC, double[] o2c, double minDistance, double maxDistance, double[] m, int signature) {
			mat3 = m;
			mat2 = o2c; if (mat2 == null) return false; 
			fastTimes();
			tmp2[0] = mat[3];  tmp2[1] = mat[7];  tmp2[2] = mat[11];  tmp2[3] = mat[15];
			double d = Pn.inverseDistanceToOrigin(tmp2,  signature);
//			System.err.println("coshd = "+d);
			if (theDropBox.minDistance > 0.0 &&  d < inverseDMin) return true;
			if (theDropBox.maxDistance > 0 && d > inverseDMax) return false;
			mat2 = o2ndc; fastTimes();
			tmp2[0] = mat[3];  tmp2[1] = mat[7];  tmp2[2] = mat[11];  tmp2[3] = mat[15];
			Pn.dehomogenize(tmp2,tmp2);
			if (tmp2[0] > theDropBox.ndcFudgeFactor || tmp2[0] < -theDropBox.ndcFudgeFactor) return false;
			if (tmp2[1] > theDropBox.ndcFudgeFactor || tmp2[1] < -theDropBox.ndcFudgeFactor) return false;
			if (tmp2[2] > 1.0 || tmp2[2] < -1.0) return false;
			return true;
		}

	private void fastTimes()	{
		for (int i=0; i<4; ++i)	{	
			for (int j=0; j<4; ++j)	{
				mat[i*4+j] = 0.0;
				for (int k=0; k<4; ++k)		{
					// the (i,j)th position is the inner product of the ith row and 
					// the jth column of the two factors
					mat[i*4+j] += mat2[i*4+k]*mat3[k*4+j];
				}
			}
		}

	}


}
