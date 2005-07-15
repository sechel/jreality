/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples.tantrix;

import java.io.IOException;

import de.jreality.examples.SimpleScene;
import de.jreality.util.VecMat;

public class Tantrix extends KnotBase {
	
	public Tantrix(Knot k) {
		super();
		GreatArc ga[]=new GreatArc[k.size()];
		int n=0;
		for(int i=0; i<k.size(); i++) {
			ga[i]=new GreatArc(
				VecMat.difference(k.get(i), k.get(i-1)),
				VecMat.difference(k.get(i+1), k.get(i))
			);
			n+=ga[i].size()-1;
		}
		coords=new double[n][3];
		for(int i=0, m=0; i<ga.length; i++)
			for(int j=0; j<ga[i].size()-1; j++, m++)
				coords[m]=ga[i].get(j);
		
		idx=new int[1][coords.length];
		for(int i=0; i<idx[0].length; i++) idx[0][i]=i;
		
		init();
	}

	public static void main(String[] args) throws IOException {
		Knot k=new Knot();
		k.read("/home/brinkman/illiMath04/pytantrix/knots/3-1.txt");
		Tantrix t=new Tantrix(k);
		SimpleScene.display(t, 4.);
	}
}
