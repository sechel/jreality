package de.jreality.tutorial.util.polygon;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.PointSetFactory;
import de.jreality.shader.DefaultPointShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;

/**
 * A sequence of points that can be dragged. Reports any changes of the point positions to
 * {@link ChangeListener}s that can be attached via {@link #addChangeListener(ChangeListener)}.
 * 
 * @author Steffen Weissmann.
 *
 */
public class DragPointSet implements PointSequence {

	SceneGraphComponent base = new SceneGraphComponent("DragPointSet");
	PointSetFactory psf = new PointSetFactory();
	DragEventTool dragTool = new DragEventTool();
	
	double[][] vertices;
	private List<ChangeListener> listeners = new LinkedList<ChangeListener>();
	
	private boolean closed=true;
	
	public DragPointSet(double[][] initialVertices) {
		base.setGeometry(psf.getGeometry());
		base.addTool(dragTool);
		dragTool.addPointDragListener(new PointDragListener() {
			public void pointDragEnd(PointDragEvent e) {
			}
			public void pointDragStart(PointDragEvent e) {
			}
			public void pointDragged(PointDragEvent e) {
				DragPointSet.this.pointDragged(e);
			}
		});
		initPoints(initialVertices);
		base.setAppearance(new Appearance());
		DefaultGeometryShader dps = (DefaultGeometryShader) ShaderUtility.createDefaultGeometryShader(base.getAppearance(), false);
		DefaultPointShader ps = (DefaultPointShader) dps.getPointShader();
		ps.setPointRadius(0.05);
		ps.setDiffuseColor(Color.yellow);
	}

	public void initPoints(double[][] initialVertices) {
		vertices = new double[initialVertices.length][];
		for (int i=0; i<initialVertices.length; i++) {
			vertices[i]=initialVertices[i].clone();
		}
		psf.setVertexCount(initialVertices.length);
		psf.setVertexCoordinates(vertices);
		psf.update();
	}
	
	public void pointDragged(PointDragEvent e) {
		vertices[e.getIndex()][0]=e.getX();
		vertices[e.getIndex()][1]=e.getY();
		vertices[e.getIndex()][2]=e.getZ();
		psf.setVertexCoordinates(vertices);
		psf.update();
		fireChange();
	}
	
	private void fireChange() {
		final ChangeEvent ev = new ChangeEvent(this);
		synchronized (listeners) {
			for (ChangeListener cl : listeners) cl.stateChanged(ev);
		}
	}

	public void addChangeListener(ChangeListener cl) {
		synchronized (listeners) {
			listeners.add(cl);
		}
	}
	
	public void removeChangeListener(ChangeListener cl) {
		synchronized (listeners) {
			listeners.remove(cl);
		}
	}
	
	public SceneGraphComponent getBase() {
		return base;
	}
	
	public double[][] getPoints() {
		return vertices;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}
}
