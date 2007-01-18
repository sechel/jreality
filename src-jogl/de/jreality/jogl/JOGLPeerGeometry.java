/*
 * Created on Jan 14, 2007
 *
 */
package de.jreality.jogl;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.DefaultPolygonShader;
import de.jreality.jogl.shader.RenderingHintsShader;
import de.jreality.math.Pn;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.util.LoggingSystem;

public class JOGLPeerGeometry extends JOGLPeerNode	implements GeometryListener{
	public Geometry originalGeometry;
	IndexedFaceSet ifs;
	IndexedLineSet ils;
	PointSet ps;
	int refCount = 0;
	int signature = Pn.EUCLIDEAN;
	boolean isSurface = false;
	boolean forceRender = false;
	public JOGLPeerGeometry(Geometry g, JOGLRenderer jr)	{
		super(jr);
		originalGeometry = g;
		name = "JOGLPeer:"+g.getName();
		ifs = null; ils = null; ps = null;
		if (g instanceof IndexedFaceSet) ifs = (IndexedFaceSet) g;
		if (g instanceof IndexedLineSet) ils = (IndexedLineSet) g;
		if (g instanceof PointSet) ps = (PointSet) g;
		originalGeometry.addGeometryListener(this);
		if (ifs != null || g instanceof Sphere || g instanceof Cylinder) isSurface = true;
		Object foo = originalGeometry.getGeometryAttributes(JOGLConfiguration.FORCE_RENDER);
		if (foo != null) forceRender = true;
	}

	public void dispose()		{
		refCount--;
		if (refCount < 0)	{
			theLog.log(Level.WARNING,"Negative reference count!");
		}
		if (refCount == 0)	{
			theLog.log(Level.FINER,"Geometry is no longer referenced");
			originalGeometry.removeGeometryListener(this);
			jr.geometries.remove(originalGeometry);
		}
	}

	public void render(JOGLPeerComponent jpc) {
		RenderingHintsShader renderingHints = jpc.renderingHints;
		DefaultGeometryShader geometryShader = jpc.geometryShader;
		if (renderingHints == null) return;
		renderingHints.render(jr.renderingState);
		jr.renderingState.setCurrentGeometry(originalGeometry);
		if (forceRender && geometryShader.polygonShader instanceof DefaultPolygonShader)	{
			((DefaultPolygonShader) geometryShader.polygonShader).preRender(jr.renderingState);		
			return;
		}
		//theLog.fine("Rendering sgc "+jpc.getOriginalComponent().getName());
		//theLog.fine("vertex:edge:face:"+geometryShader.isVertexDraw()+geometryShader.isEdgeDraw()+geometryShader.isFaceDraw());
		if (geometryShader.isEdgeDraw() && ils != null)	{
			geometryShader.lineShader.render(jr.renderingState);
			geometryShader.lineShader.postRender(jr.renderingState);
		}
		if (geometryShader.isVertexDraw() && ps != null)	{
			geometryShader.pointShader.render(jr.renderingState);
			geometryShader.pointShader.postRender(jr.renderingState);
		}
		renderingHints.render(jr.renderingState);
		if (geometryShader.isFaceDraw() && isSurface) {
			geometryShader.polygonShader.render(jr.renderingState);
			geometryShader.polygonShader.postRender(jr.renderingState);
		}	
		if (geometryShader.isVertexDraw() && ps!=null && ps.getVertexAttributes(Attribute.LABELS) != null) {
			JOGLRendererHelper.drawPointLabels(jr, ps,  jpc.geometryShader.pointShader.getTextShader());
		}
		if (geometryShader.isEdgeDraw() &&ils != null && ils.getEdgeAttributes(Attribute.LABELS) != null) {
			JOGLRendererHelper.drawEdgeLabels(jr, ils, jpc.geometryShader.lineShader.getTextShader());
		}
		if (geometryShader.isFaceDraw() &&ifs != null && ifs.getFaceAttributes(Attribute.LABELS) != null) {
			JOGLRendererHelper.drawFaceLabels(jr, ifs,  jpc.geometryShader.polygonShader.getTextShader());
		}
		renderingHints.postRender(jr.renderingState);
	}

	public void geometryChanged(GeometryEvent ev) {
		if (ev.getChangedGeometryAttributes().size() > 0)	{
			Object foo = originalGeometry.getGeometryAttributes(GeometryUtility.SIGNATURE);
			if (foo != null) {
				Integer foo2 = (Integer) foo;
				signature = foo2.intValue();
			}				
		}

	}
}
