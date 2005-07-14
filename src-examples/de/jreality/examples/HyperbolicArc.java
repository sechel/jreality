/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples;

import java.util.Iterator;
import java.util.Vector;

import de.jreality.util.VecMat;

public class HyperbolicArc extends Arc {
	private static double[] lineofsight(double x0[], double y0[], double x1[], double y1[],
					double p0[]) {
		return VecMat.crossproduct(
			VecMat.crossproduct(VecMat.difference(x0, p0), VecMat.difference(y0, x0)),
			VecMat.crossproduct(VecMat.difference(x1, p0), VecMat.difference(y1, x1))
		);
	}
					
	public HyperbolicArc(double x0[], double y0[], double x1[], double y1[],
					double p0[], double p1[]) {
		Vector v=new Vector();
		computePoints(x0, y0, x1, y1, p0, p1, v);
		int n=0;
		for(Iterator i=v.iterator(); i.hasNext();) {
			GreatArc ga=(GreatArc) i.next();
			n+=ga.size()-1;
		}
		coords=new double[n+1][];
		int m=0;
		for(int i=0; i<v.size(); i++) {
			GreatArc ga=(GreatArc) v.get(i);
			for(int j=0; j<ga.size()-1; j++) {
				coords[m]=ga.get(j);
				m++;
			}
		}
		coords[m]=p1;
	}
	
	private void computePoints(double x0[], double y0[], double x1[], double y1[],
					double p0[], double p1[], Vector v) {
		double pm[]=VecMat.product(VecMat.sum(p0, p1), .5);
		double l0[]=lineofsight(x0, y0, x1, y1, p0);
		double l2[]=lineofsight(x0, y0, x1, y1, p1);
		double lm[]=lineofsight(x0, y0, x1, y1, pm);
		double l1[]=VecMat.product(VecMat.sum(l0, l2), .5);
		if (VecMat.dot(lm, l1)<.99*VecMat.norm(lm)*VecMat.norm(l1)) {
			computePoints(x0, y0, x1, y1, p0, pm, v);
			computePoints(x0, y0, x1, y1, pm, p1, v);
		}
		else
			v.add(new GreatArc(l0, l2));
	}
}
