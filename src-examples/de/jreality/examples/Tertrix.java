/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples;
import java.io.IOException;

import de.jreality.util.VecMat;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

public class Tertrix extends KnotBase {
	private static final double EPS=1e-7;
	
	private class Plane {
		double x[];
		double n[];
		
		Plane(double a[], double b[], double c[]) {
			x=a;
			n=VecMat.crossproduct(VecMat.difference(b, a), VecMat.difference(c, a));
			VecMat.normalize(n);
		}
		Plane(double a[], double b[], double c[], double d[]) {
			this(a, b, c);
			if (!sideCheck(d))
				VecMat.multiply(n, -1.);
		}
		double intersect(double p0[], double p1[]) {
			return VecMat.dot(VecMat.difference(x, p0), n)/
				   VecMat.dot(VecMat.difference(p1, p0), n);
		}
		boolean sideCheck(double x[]) {
			return (VecMat.dot(n, VecMat.difference(x, this.x))>0);
		}
	}
	
	private double[] intersections(Plane p[], double p0[], double p1[]) {
		LinkedList l=new LinkedList();
		for(int i=0; i<p.length; i++) {
			double t=p[i].intersect(p0, p1);
			if (t>=0. && t<=1.0)
				l.add(new Double(t));
		}
		Collections.sort(l);
		double tt[]=new double[l.size()];
		for(int i=0; i<l.size(); i++) {
			tt[i]=((Double) l.get(i)).doubleValue();
		}
		return tt;
	}
	
	public Tertrix(Knot k) {
		super();
		
		Vector v=new Vector();
		
		int n=0;
		for(int i=0; i<k.size()-3; i++)
			for(int j=i+2; j<k.size()-2; j++)
				for(int m=j+2; m<i+k.size()-1; m++) {
					double p0[]=k.get(m-1);
					double p1[]=k.get(m);
					Plane list[]=new Plane[] {
							new Plane(k.get(i-1), k.get(i), k.get(j-1), k.get(j)),
							new Plane(k.get(i-1), k.get(i), k.get(j), k.get(j-1)),
							new Plane(k.get(i-1), k.get(j-1), k.get(j), k.get(i)),
							new Plane(k.get(i), k.get(j-1), k.get(j), k.get(i-1))
					};
					double ttt[]=intersections(list, p0, p1);
					double tt[]=new double[ttt.length+2];
					tt[0]=0.;
					tt[ttt.length+1]=1.;
					for(int ii=0; i<ttt.length; i++)
						tt[ii+1]=ttt[ii];
					double pts[][]=new double[tt.length][];
					pts[0]=p0;
					for(int ii=1; ii<tt.length; ii++) {
						pts[ii]=VecMat.sum(VecMat.product(p0, 1.-tt[ii]),
											VecMat.product(p1, tt[ii]));
						double mpt[]=VecMat.product(VecMat.sum(pts[ii], pts[ii-1]), .5);
						boolean flag0=list[0].sideCheck(mpt);
						boolean flag1=list[1].sideCheck(mpt);
						boolean flag2=list[2].sideCheck(mpt);
						boolean flag3=list[3].sideCheck(mpt);
						if (flag0==flag1 && flag2==flag3) {
							Arc a=new HyperbolicArc(k.get(i-1), k.get(i), k.get(j-1), k.get(j), pts[ii-1], pts[ii]);
							//Arc a=new GreatArc(pts[ii-1], pts[ii]);
							v.add(a);
							n+=a.size();
						}
					}
				}
		int n0=n;
		for(int i=0; i<k.size(); i++)
			for(int j=i+2; j<i+k.size()-2; j++) {
				double x0[]=k.get(i-2);
				double y0[]=k.get(i-1);
				double x1[]=k.get(i-1);
				double y1[]=k.get(i);
				double p0[]=k.get(j-1);
				double p1[]=k.get(j);
				Plane abc=new Plane(x0, y0, y1);
				double t=abc.intersect(p0, p1);
				if (t>=0. && t<=1.) {
					double xx[]=VecMat.sum(VecMat.product(p0, 1.-t), VecMat.product(p1, t));
					if (VecMat.norm(VecMat.difference(xx, x0))>EPS &&
						    VecMat.norm(VecMat.difference(xx, y0))>EPS &&
						    VecMat.norm(VecMat.difference(xx, y1))>EPS) {
						Plane left=new Plane(x0, y0,
								VecMat.crossproduct(VecMat.difference(y1, x1),
													VecMat.difference(y0, x0)), y1);
						Plane right=new Plane(x1, y1,
								VecMat.crossproduct(VecMat.difference(y0, x0),
													VecMat.difference(y1, x1)), x0);
						Plane bottom=new Plane(x0, y1,
								VecMat.crossproduct(VecMat.difference(y0, x0),
													VecMat.difference(y1, x1)), y0);
						if (left.sideCheck(xx) && !right.sideCheck(xx) && bottom.sideCheck(xx)) {
							Arc a=new GreatArc(VecMat.difference(xx, x0),
											VecMat.difference(xx, y0));
							v.add(a);
							n+=a.size();
						}
						if (left.sideCheck(xx) && !right.sideCheck(xx) && !bottom.sideCheck(xx)) {
							Arc a=new GreatArc(VecMat.difference(xx, y1),
											VecMat.difference(xx, x1));
							v.add(a);
							n+=a.size();
						}
						if (!left.sideCheck(xx) && right.sideCheck(xx) && bottom.sideCheck(xx)) {
							Arc a=new GreatArc(VecMat.difference(xx, y1),
											VecMat.difference(xx, x1));
							v.add(a);
							n+=a.size();
						}
						if (!left.sideCheck(xx) && right.sideCheck(xx) && !bottom.sideCheck(xx)) {
							Arc a=new GreatArc(VecMat.difference(xx, x0),
											VecMat.difference(xx, y0));
							v.add(a);
							n+=a.size();
						}
						if (left.sideCheck(xx) && right.sideCheck(xx) && bottom.sideCheck(xx)) {
							Arc a=new GreatArc(VecMat.difference(xx, x0),
											VecMat.difference(y1, xx));
							v.add(a);
							n+=a.size();
						}
					}
				}
			}
		
		coords=new double[n][3];
		idx=new int[v.size()][];
		for(int i=0, m=0; i<v.size(); i++) {
			Arc a=(Arc) v.get(i);
			idx[i]=new int[a.size()];
			for(int j=0; j<a.size(); j++) {
				coords[m]=a.get(j);
				if (m<n0)
					System.out.println(VecMat.norm(a.get(j)));
				idx[i][j]=m;
				m++;
			}
		}
			
		init();
	}

	public static void main(String[] args) throws IOException {
		Knot k=new Knot();
		k.read("/home/brinkman/illiMath04/pytantrix/knots/5-1.txt");
		Tertrix t=new Tertrix(k);
		SimpleScene.display(t, 4.);
	}
}

