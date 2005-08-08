/*
 * Created on Jan 4, 2004
 *
 */
package de.jreality.jogl;

import java.awt.Color;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.geometry.*;
import de.jreality.jogl.pick.JOGLPickUtility;
import de.jreality.math.P3;
import de.jreality.scene.*;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.pick.PickPoint;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class SelectionManager implements TransformationListener {
	private SceneGraphPath defaultSelection;
	private SceneGraphPath theSelection;
	private SceneGraphPath currentCycleSelection;
  
  private SceneGraphPathObserver theSelectionObserver;
  
	private Vector selectionList;
	private PickPoint pickPoint;
	private boolean renderSelection = false, renderPick = false;
	private boolean selectionEditable = false;
	private Appearance boundAppearance, pickPointAppearance;
	static boolean debug = true;
	SceneGraphComponent boundKit, pickPointKit, selectionKit, sphereKit;
	private Appearance selectedAppearance;
	private double pickPointSize = .02;
	private boolean useSphere = true;
	/**
	 * 
	 */
	public SelectionManager() {
		super();
    theSelectionObserver = new SceneGraphPathObserver();
    theSelectionObserver.addTransformationListener(this);
    selectionList = new Vector();
		boundKit = SceneGraphUtility.createFullSceneGraphComponent("boundKit");
		boundAppearance = boundKit.getAppearance();
		boundAppearance.setAttribute(CommonAttributes.EDGE_DRAW,true);
		boundAppearance.setAttribute(CommonAttributes.FACE_DRAW,false);
		boundAppearance.setAttribute(CommonAttributes.VERTEX_DRAW,false);
		boundAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,true);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_FACTOR, 1.0);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE_PATTERN, 0x6666);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH,2.0);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
		boundAppearance.setAttribute(CommonAttributes.LEVEL_OF_DETAIL,0.0);
		boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);

		pickPointKit = SceneGraphUtility.createFullSceneGraphComponent("pickPointKit");
		pickPointAppearance = pickPointKit.getAppearance();
		pickPointAppearance.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, pickPointSize);
		if (useSphere)	{
			pickPointAppearance.setAttribute(CommonAttributes.VERTEX_DRAW,false);
			pickPointAppearance.setAttribute(CommonAttributes.EDGE_DRAW,false);
			pickPointAppearance.setAttribute(CommonAttributes.FACE_DRAW,true);
		} else {
			pickPointAppearance.setAttribute(CommonAttributes.VERTEX_DRAW,true);
			pickPointAppearance.setAttribute(CommonAttributes.EDGE_DRAW,false);
			pickPointAppearance.setAttribute(CommonAttributes.FACE_DRAW,false);
			pickPointAppearance.setAttribute(CommonAttributes.SPHERES_DRAW,true);			
		}
		pickPointAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
		pickPointAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING,true);
		//pickPointAppearance.setAttribute(CommonAttributes.LEVEL_OF_DETAIL,0.0);
		pickPointAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);

		selectionKit = SceneGraphUtility.createFullSceneGraphComponent("selectionKit");
		pickPointKit.setVisible(false);
		selectionKit.addChild(pickPointKit);
		boundKit.setVisible(false);
		selectionKit.addChild(boundKit);
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
		setSelection(path, false);
	}

	public void setSelection(SceneGraphPath path, boolean cycling) {
// TODO add SelectionChanged event and listener support, etc
		if (path == null)	{
			theSelection = defaultSelection;
			if (theSelection == null) {
				setRenderSelection(false);
				setRenderPick(false);
				return;
			}
			if (defaultSelection != null) JOGLConfiguration.theLog.log(Level.INFO,"Default sel: "+theSelection.toString());
		} else {
			theSelection = path;
		}
    theSelectionObserver.setPath(theSelection);
		if (theSelection != null) { 
			JOGLConfiguration.theLog.log(Level.INFO,"sel: "+theSelection.toString());
			Object tail = theSelection.getLastElement();
			if (tail instanceof SceneGraphComponent)	{
				Transformation t =  ((SceneGraphComponent) tail).getTransformation();
				if (t != null && t.getIsEditable())	{
					if (debug) JOGLConfiguration.theLog.log(Level.INFO,"Transformation is editable");
					selectionEditable = true;
				} else {
					if (debug) JOGLConfiguration.theLog.log(Level.INFO,"Transformation is not editable");
					selectionEditable = false;
					} 
			}
			else {
				if (debug) JOGLConfiguration.theLog.log(Level.INFO,"Not a SceneGraphComponent");
				selectionEditable = false;
				} 
		} 
		else if (debug) JOGLConfiguration.theLog.log(Level.INFO,"SelectionManager: empty selection");
		
			
		if (theSelection != null)	{
			selectedAppearance = null;
      for (Iterator lit = theSelection.reverseIterator(); lit.hasNext(); ) {
        Object selt = lit.next();
        if (selt != null) {
          if (selt instanceof Appearance)  selectedAppearance = (Appearance) selt;
          else if ( selt instanceof SceneGraphComponent)  {
            selectedAppearance = ((SceneGraphComponent)selt).getAppearance() ;
            }      
          if (selectedAppearance != null) break;
        }
      }
		}
		if (!cycling) {
			previousFullSelection = (SceneGraphPath) theSelection.clone();
			truncatedSelection = null;
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
		if (pickPoint == null) return;
		// probably should have a separate event and listener list for this
		//JOGLConfiguration.theLog.log(Level.INFO,"Setting pick point");
		Color pickPointColor = Color.BLUE;
		if (point.getPickType() == PickPoint.HIT_FACE) pickPointColor = Color.RED;
		else if (point.getPickType() == PickPoint.HIT_EDGE) pickPointColor = Color.YELLOW;
		else if (point.getPickType() == PickPoint.HIT_VERTEX) pickPointColor = Color.BLUE;

		pickPointAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, pickPointColor);		
		broadcastChange();
		JOGLConfiguration.theLog.log(Level.FINER,"Face number is "+point.getFaceNum());
	}



	public SceneGraphComponent representSelectionAsSceneGraph(Viewer v)
	{				
		if (pickPoint != null && renderPick)	{
			// TODO: How big should the sphere representing the pick point be?
			//JOGLConfiguration.theLog.log(Level.INFO,"Representing pick point");
			if (useSphere)	{
				if (sphereKit == null)	{
					sphereKit = Primitives.sphere(1.0, JOGLPickUtility.getPointObject(pickPoint, v) );
					pickPointKit.addChild(sphereKit);
				} else {
					sphereKit.getTransformation().setMatrix(P3.makeTranslationMatrix(null,JOGLPickUtility.getPointObject(pickPoint, v),v.getSignature()) );
				}				
			}
			else
				pickPointKit.setGeometry(Primitives.point( JOGLPickUtility.getPointObject(pickPoint, v)  ) );

			double[] mm = pickPoint.getPickPath().getMatrix(null);
			pickPointKit.getTransformation().setMatrix( mm);
			//JOGLConfiguration.theLog.log(Level.INFO,"PPTranslation is : "+Rn.toString(sphereKit.getTransformation().getTranslation()));
			
			Object val = pickPointAppearance.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, Double.class);
			double r;
			if (val instanceof Double)	{
				r = ((Double) val).doubleValue();
			} else r = .02;
			
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
				//JOGLConfiguration.theLog.log(Level.INFO,"Turning off stippling");
			}
			else	{
				boundAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,true);
				//JOGLConfiguration.theLog.log(Level.INFO,"Turning on stippling");
			}
			SceneGraphNode sgn =  theSelection.getLastElement();
			Rectangle3D bbox = null;
			// TODO implement bounding box business
			if (sgn instanceof SceneGraphComponent) bbox = GeometryUtility.calculateChildrenBoundingBox((SceneGraphComponent) sgn);
			else if (sgn instanceof PointSet) bbox = GeometryUtility.calculateBoundingBox( (PointSet) sgn);
			else if (sgn instanceof Sphere) bbox = SphereUtility.getSphereBoundingBox();
			else {
				JOGLConfiguration.theLog.log(Level.WARNING,"Unknown selection class: "+sgn.getClass().toString());
				return selectionKit;
			}
			//JOGLConfiguration.theLog.log(Level.INFO,"BBox is "+bbox.toString());
			//else bbox = sgn.getBoundingBox();
			IndexedFaceSet boxRep = IndexedFaceSetUtility.representAsSceneGraph(bbox);
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
		//JOGLConfiguration.theLog.log(Level.INFO,"SelectionManager: Adding geometry listener"+l+"to this:"+this);
	}
	
	public void removeSelectionListener(SelectionManager.Listener l)	{
		if (listeners == null)	return;
		listeners.remove(l);
	}

	public void broadcastChange()	{
		if (listeners == null) return;
		//SyJOGLConfiguration.theLog.log(Level.INFO,"SelectionManager: broadcasting"+listeners.size()+" listeners");
		if (!listeners.isEmpty())	{
			SelectionManager.Changed e = new SelectionManager.Changed(this);
			//JOGLConfiguration.theLog.log(Level.INFO,"SelectionManager: broadcasting"+listeners.size()+" listeners");
			for (int i = 0; i<listeners.size(); ++i)	{
				SelectionManager.Listener l = (SelectionManager.Listener) listeners.get(i);
				l.selectionChanged(e);
			}
		}
	}
	
  public void transformationMatrixChanged(TransformationEvent e) {
  	if (renderSelection && theSelection  != null && boundKit != null) {
			if (boundKit.getTransformation() != null)
				// TODO straighten out the bounding box complications, like here
				//boundKit.getTransformation().setMatrix(theSelection.getMatrix(null, 0, theSelection.getLength()-2));
				boundKit.getTransformation().setMatrix(theSelection.getMatrix(null));
		} 
		if (renderPick && pickPoint!= null && pickPointKit != null)	{
			//JOGLConfiguration.theLog.log(Level.INFO,"matrixChanged callback");
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


	public boolean isRenderPick() {
		return renderPick;
	}
	public void setRenderPick(boolean renderPick) {
		this.renderPick = renderPick;
		//broadcastChange();
	}
	public boolean isRenderSelection() {
		return renderSelection;
	}
	public void setRenderSelection(boolean renderSelection) {
		renderPick = this.renderSelection = renderSelection;
		JOGLConfiguration.theLog.log(Level.FINE,"Render bound: "+renderSelection);
		broadcastChange();
	}
	
	public Appearance getPickPointAppearance() {
		return pickPointAppearance;
	}
	
	SceneGraphPath truncatedSelection = null, previousFullSelection;
	public void cycleSelection()	{
		if (truncatedSelection == null || truncatedSelection.getLength()<=2) truncatedSelection = (SceneGraphPath) previousFullSelection.clone();
		else truncatedSelection.pop();
		setSelection(truncatedSelection, true);
		JOGLConfiguration.theLog.log(Level.INFO,"Cycling selection: "+renderSelection);
	}
	
	public void addSelection(SceneGraphPath p)	{
		Iterator iter = selectionList.iterator();
		while (iter.hasNext())	{
			SceneGraphPath sgp = (SceneGraphPath) iter.next();
			if (sgp.isEqual(p)) return;
		}
		selectionList.add(p);
		JOGLConfiguration.theLog.log(Level.FINE,"Adding path "+p.toString());
	}
		
	public void removeSelection(SceneGraphPath p)	{
		Iterator iter = selectionList.iterator();
		while (iter.hasNext())	{
			SceneGraphPath sgp = (SceneGraphPath) iter.next();
			if (sgp.isEqual(p)) {
				selectionList.remove(sgp);
				JOGLConfiguration.theLog.log(Level.FINE,"Removing path "+p.toString());
				return;
			}
		}
	}
		
	public void cycleSelectionPaths()	{
		int target = 0;
		if (selectionList == null || selectionList.size() == 0)		return;
		if (currentCycleSelection != null) {
			int which = selectionList.indexOf(currentCycleSelection);
			if (which != -1)  {
				target = (which + 1) % selectionList.size();
			}
		}
		currentCycleSelection = (SceneGraphPath) selectionList.get(target);
		setSelection(currentCycleSelection);
		JOGLConfiguration.theLog.log(Level.INFO,"Setting selection to "+currentCycleSelection.toString());
	}

}
