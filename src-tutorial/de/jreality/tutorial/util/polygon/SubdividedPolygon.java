package de.jreality.tutorial.util.polygon;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;

public class SubdividedPolygon implements ChangeListener, PointSequence {

	int subdivisionSteps = 3;
	
	PointSequence controlPointSet;
	private double[][] pts;
	
	public SubdividedPolygon(PointSequence dragPS) {
		controlPointSet=dragPS;
		controlPointSet.addChangeListener(this);
		update();
	}
		
	private void update() {
		pts = computeSpline();
		fireChange();
	}
	
	private double[][] computeSpline() {
		double[][] cur = controlPointSet.getPoints();
		for (int i=0; i<subdivisionSteps; i++) {
			int n = controlPointSet.isClosed() ? cur.length : cur.length-1;
			double[][] sub = new double[cur.length+n][];
			for (int j=0; j<n; j++) {
				sub[2*j] = cur[j];
				sub[2*j+1] = subdivide(point(cur, j-1), point(cur, j), point(cur, j+1), point(cur, j+2));
			}
			if (!controlPointSet.isClosed()) {
				sub[2*n]=cur[n];
			}
			cur = sub;
		}
		return cur;
	}
	
	private double[] point(double[][] pts, int j) {
		int n=pts.length;
		if (j>=0 && j<n) return pts[j];
		if (controlPointSet.isClosed()) return pts[(j+n)%n];
		if (j==-1) {
			double[] p0 = pts[0];
			double[] p1 = pts[1];
			double[] ret = Rn.linearCombination(null, 2, p0, -1, p1);
			return ret;
		}
		if (j==n) {
			double[] p0 = pts[n-2];
			double[] p1 = pts[n-1];
			double[] ret = Rn.linearCombination(null, 2, p1, -1, p0);
			return ret;
		}
		throw new IllegalArgumentException();
	}

	private static double[] subdivide(double[] v1, double[] v2, double[] v3, double[] v4) {
		double[] ret = new double[3];
    	for (int j=0; j<3; j++) ret[j] = (9.0*(v2[j]+v3[j])-v1[j]-v4[j])/16.0;
    	return ret;
	}

	public double[][] getPoints() {
		return pts;
	}
	
	public void stateChanged(ChangeEvent e) {
		update();
	}

	private List<ChangeListener> listeners = new LinkedList<ChangeListener>();
	
	private void fireChange() {
		final ChangeEvent ev = new ChangeEvent(this);
		synchronized (listeners ) {
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

	public void setSubdivisionLevel(int n) {
		this.subdivisionSteps=n;
		update();
	}
	
	public boolean isClosed() {
		return controlPointSet.isClosed();
	}
	
}
