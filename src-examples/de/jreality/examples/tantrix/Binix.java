/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples.tantrix;

import java.io.IOException;

import de.jreality.examples.SimpleScene;
import de.jreality.util.VecMat;
import java.util.Vector;

public class Binix extends KnotBase {
	
	public Binix(Knot k) {
		super();
		Vector v=new Vector();
		int n=0;
		for(int i=0; i<k.size(); i++)
			for(int j=i+2; j<i+k.size()-2; j++) {
				double temp[]=new double[3];
				VecMat.cross(VecMat.difference(k.get(j), k.get(i-1)), 
							 VecMat.difference(k.get(j-1), k.get(i-1)), temp);
				double test=-VecMat.dot(temp,
								VecMat.difference(k.get(i-1), k.get(i-2)))*
							VecMat.dot(temp,
							 	VecMat.difference(k.get(i), k.get(i-1)));
				if (test>0) {
					Arc a=new GreatArc(
						VecMat.difference(k.get(j-1), k.get(i-1)),
						VecMat.difference(k.get(j), k.get(i-1))
					);
					v.add(a);
					n+=a.size();
				}
			}

		coords=new double[n][3];
		idx=new int[v.size()][];
		for(int i=0, m=0; i<v.size(); i++) {
			Arc a=(Arc) v.get(i);
			idx[i]=new int[a.size()];
			for(int j=0; j<a.size(); j++) {
				coords[m]=a.get(j);
				idx[i][j]=m;
				m++;
			}
		}
			
		init();
	}

	public static void main(String[] args) throws IOException {
		Knot k=new Knot();
		k.read("/home/brinkman/illiMath04/pytantrix/knots/3-1.txt");
		Binix b=new Binix(k);
		SimpleScene.display(b, 4.);
	}
}
