/*
 * Created on Jan 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl;

import java.util.Vector;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereHelper;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Graphics3D;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.SceneGraphPath.PathMatrixChanged;
import de.jreality.scene.pick.PickPoint;
import de.jreality.util.P3;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SelectionManager implements SceneGraphPath.PathMatrixListener {
	private SceneGraphPath defaultSelection;
	private SceneGraphPath theSelection;
	private PickPoint pickPoint;
	private boolean renderSelection = false, renderPick;
	private boolean selectionEditable = false;
	private Appearance boundAppearance, pickPointAppearance;
	static boolean debug = true;
	SceneGraphComponent boundKit, pickPointKit, selectionKit, sphereKit;
	private Appearance selectedAppearance;
	private double sphereSize;
	private boolean useSphere = true;
	/**
	 * 
	 */
	public SelectionManager() {
		super();
		boundKit = SceneGraphUtilities.createFullSceneGraphComponent("boundKit");
		boundAppearance = boundKit.getAppearance();
		boundAppearance.setAttribute(CommonAttributes.EDGE_DRAW,true);
		boundAppearance.setAttribute(CommonAttributes.FACE_DRAW,false);
		boundAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,true);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_FACTOR, 1.0);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE_PATTERN, 0x6666);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH,2.0);
		boundAppearance.setAttribute(CommonAttributes.LEVEL_OF_DETAIL,0.0);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);

		pickPointKit = SceneGraphUtilities.createFullSceneGraphComponent("pickPointKit");
		pickPointAppearance = pickPointKit.getAppearance();
		if (useSphere)	{
			pickPointAppearance.setAttribute(CommonAttributes.VERTEX_DRAW,false);
			pickPointAppearance.setAttribute(CommonAttributes.EDGE_DRAW,false);
			pickPointAppearance.setAttribute(CommonAttributes.FACE_DRAW,true);
		} else {
			pickPointAppearance.setAttribute(CommonAttributes.VERTEX_DRAW,true);
			pickPointAppearance.setAttribute(CommonAttributes.EDGE_DRAW,false);
			pickPointAppearance.setAttribute(CommonAttributes.FACE_DRAW,false);
			pickPointAppearance.setAttribute(CommonAttributes.SPHERES_DRAW,true);			
			pickPointAppearance.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,0.01);
		}
		pickPointAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
		pickPointAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING,true);
		//pickPointAppearance.setAttribute(CommonAttributes.LEVEL_OF_DETAIL,0.0);
		pickPointAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);

		selectionKit = SceneGraphUtilities.createFullSceneGraphComponent("selectionKit");
		pickPointKit.setVisible(false);
		selectionKit.addChild(pickPointKit);
		boundKit.setVisible(false);
		selectionKit.addChild(boundKit);
		//renderSelection = true;
		renderPick = true;
		sphereSize = .02;
	}

	/**
	 * @return
	 */
	public SceneGraphPath getSelection() {
		return theSelection;
	}

	/**
	 * @param path
	 */
	public void setSelection(SceneGraphPath path) {
		// TODO add SelectionChanged event and listener support, etc
		if (path == null)	{
			theSelection = defaultSelection;
			System.out.println("Default sel: "+theSelection.toString());
		} else {
			if (theSelection!=null) theSelection.removePathMatrixListener(this);
			theSelection = path;
		}
			theSelection.addPathMatrixListener(this);
			if (theSelection != null) { 
				System.err.println("sel: "+theSelection.toString());
				Object tail = theSelection.getLastElement();
				if (tail instanceof SceneGraphComponent)	{
					Transformation t =  ((SceneGraphComponent) tail).getTransformation();
					if (t != null && t.getIsEditable())	{
						if (debug) System.err.println("SceneGraphComponent is editable");
						selectionEditable = true;
					} else {
						if (debug) System.err.println("SceneGraphComponent is not editable");
						selectionEditable = false;
						} 
				}
				else {
					if (debug) System.err.println("Not a SceneGraphComponent");
					selectionEditable = false;
					} 
			} 
			else if (debug) System.err.println("SelectionManager: empty selection");
				
			if (theSelection != null)	{
				selectedAppearance = null;
				for (int i = theSelection.getLength()-1; i>=0; i--)	{
					Object selt = theSelection.getElementAt(i);
					if (selt != null)	{
						if (selt instanceof Appearance)  selectedAppearance = (Appearance) selt;
						else if ( selt instanceof SceneGraphComponent)	{
							selectedAppearance = ((SceneGraphComponent)selt).getAppearance() ;
							} 		 
						if (selectedAppearance != null) break;
					}
				}
			}
			
		
		broadcastChange();
	}

	/**
	 * @param ds
	 */
	public void setDefaultSelection(SceneGraphPath ds) {
		defaultSelection = ds;
		
	}

	/**
	 * @return
	 */
	public SceneGraphPath getDefaultSelection() {
		return defaultSelection;
	}

	/**
	 * @return
	 */
	public PickPoint getPickPoint() {
		return pickPoint;
	}

	/**
	 * @param point
	 */
	public void setPickPoint(PickPoint point) {
		pickPoint = point;
		// probably should have a separate event and listener list for this
		//System.out.println("Setting pick point");
		broadcastChange();
		//System.out.println("Did broadcast change");
	}



	public SceneGraphComponent representSelectionAsSceneGraph(Viewer v)
	{				
		if (pickPoint != null && renderPick)	{
			// TODO: How big should the sphere representing the pick point be?
			//System.out.println("Representing pick point");
			if (useSphere)	{
				if (sphereKit == null)	{
					sphereKit = Primitives.sphere(1.0, pickPoint.getPointObject() );
					pickPointKit.addChild(sphereKit);
				} else {
					sphereKit.getTransformation().setTranslation(pickPoint.getPointObject());
				}				
			}
			else
				pickPointKit.setGeometry(Primitives.point( pickPoint.getPointObject() ) );

			double[] mm = pickPoint.getPickPath().getMatrix(null);
			pickPointKit.getTransformation().setMatrix( mm);
			//System.out.println("PPTranslation is : "+Rn.toString(sphereKit.getTransformation().getTranslation()));
			
			Graphics3D gc = new Graphics3D(v);
			gc.setObjectToWorld(mm);
			// TODO fix this!  It doesn't do what it's supposed to do
			double r = P3.getXYScale(gc.getObjectToNDC(), pickPoint.getPointObject());
			r = (r == 0.0) ? 1.0 : 1.0/r;
			r =  sphereSize *r;
			r = .02;
			if (useSphere)
				sphereKit.getTransformation().setStretch(r);
			else
				pickPointAppearance.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,r);
			
			pickPointKit.setVisible(true);
			
		} else pickPointKit.setVisible(false);
		
		if (renderSelection && theSelection != null ) 	{
			boundKit.setVisible(true);
			boundKit.getTransformation().setMatrix(theSelection.getMatrix(null));
			boundAppearance = boundKit.getAppearance();
			if (selectionEditable)    {
				boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,false);
				//System.out.println("Turning off stippling");
			}
			else	{
				boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,true);
				//System.out.println("Turning on stippling");
			}
			SceneGraphNode sgn = ((SceneGraphNode) theSelection.getLastElement());
			Rectangle3D bbox = null;
			// TODO implement bounding box business
			if (sgn instanceof SceneGraphComponent) bbox = GeometryUtility.calculateChildrenBoundingBox((SceneGraphComponent) sgn);
			else if (sgn instanceof PointSet) bbox = GeometryUtility.calculateBoundingBox( (PointSet) sgn);
			else if (sgn instanceof Sphere) bbox = SphereHelper.getSphereBoundingBox();
			else {
				System.out.println("Unknown selection class: "+sgn.getClass().toString());
				return selectionKit;
			}
			//System.out.println("BBox is "+bbox.toString());
			//else bbox = sgn.getBoundingBox();
			IndexedFaceSet boxRep = GeometryUtility.representAsSceneGraph(bbox);
			boundKit.setGeometry(boxRep );
			
		} else boundKit.setVisible(false);
		
		return selectionKit;
	}
	public static class Changed extends java.util.EventObject	{

		/**
		 * @param source
		 */
		public Changed(Object source) {
			super(source);
		}
	}
	
	Vector listeners;
	
	public interface Listener extends java.util.EventListener	{
		public void selectionChanged(SelectionManager.Changed e);
	}

	public void addSelectionListener(SelectionManager.Listener l)	{
		if (listeners == null)	listeners = new Vector();
		if (listeners.contains(l)) return;
		listeners.add(l);
		//System.err.println("SelectionManager: Adding geometry listener"+l+"to this:"+this);
	}
	
	public void removeSelectionListener(SelectionManager.Listener l)	{
		if (listeners == null)	return;
		listeners.remove(l);
	}

	public void broadcastChange()	{
		if (listeners == null) return;
		//System.err.println("SelectionManager: broadcasting"+listeners.size()+" listeners");
		if (!listeners.isEmpty())	{
			SelectionManager.Changed e = new SelectionManager.Changed(this);
			//System.out.println("SelectionManager: broadcasting"+listeners.size()+" listeners");
			for (int i = 0; i<listeners.size(); ++i)	{
				SelectionManager.Listener l = (SelectionManager.Listener) listeners.get(i);
				l.selectionChanged(e);
			}
		}
	}
	
	public void matrixChanged(PathMatrixChanged e) {
		if (renderSelection && theSelection  != null && boundKit != null) {
			if (boundKit.getTransformation() != null)
				// TODO straighten out the bounding box complications, like here
				//boundKit.getTransformation().setMatrix(theSelection.getMatrix(null, 0, theSelection.getLength()-2));
				boundKit.getTransformation().setMatrix(theSelection.getMatrix(null));
		} 
		if (renderPick && pickPoint!= null && pickPointKit != null)	{
			//System.out.println("matrixChanged callback");
			double[] mm = pickPoint.getPickPath().getMatrix(null);
			pickPointKit.getTransformation().setMatrix( mm);			
		}
	}

	/**
	 * @return
	 */
	public Appearance getSelectedAppearance() {
		return selectedAppearance;
	}

	/**
	 * @param b
	 */
	public void setShowPick(boolean b) {
		renderPick = b;
		
	}

	public boolean isRenderPick() {
		return renderPick;
	}
	public void setRenderPick(boolean renderPick) {
		this.renderPick = renderPick;
		broadcastChange();
	}
	public boolean isRenderSelection() {
		return renderSelection;
	}
	public void setRenderSelection(boolean renderSelection) {
		this.renderSelection = renderSelection;
		System.out.println("Render bound: "+renderSelection);
		broadcastChange();
	}
}
