/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples;

import java.util.Vector;

import de.jreality.util.VecMat;

public class GreatArc extends Arc {

	public GreatArc(double p0[], double p1[]) {
		this(p0, p1, 1.);
	}
	
	public GreatArc(double[] pp0, double[] pp1, double res) {
		double p0[]=new double[3];
		double p1[]=new double[3];
	
		res*=Math.PI/180.;
		VecMat.vecAssign(p0, pp0);
		VecMat.vecAssign(p1, pp1);
		VecMat.normalize(p0);
		VecMat.normalize(p1);
		double pn[]=VecMat.difference(p1, VecMat.product(p0, VecMat.dot(p0, p1)));
		VecMat.normalize(pn);
		double theta=Math.acos(VecMat.dot(p0, p1));
		double phi=0.;
		Vector pts=new Vector();
		while (phi<theta) {
			pts.add(VecMat.sum(VecMat.product(p0, Math.cos(phi)),
							   VecMat.product(pn, Math.sin(phi))));
			phi+=res;
		}
		pts.add(p1);
		coords=new double[pts.size()][];
		for(int i=0; i<pts.size(); i++)
			coords[i]=(double[]) pts.get(i);
		
		int idx[][]=new int[1][coords.length];
		for(int i=0; i<idx[0].length; i++) idx[0][i]=i;
	}
}
