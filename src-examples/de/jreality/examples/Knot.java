/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Vector;

public class Knot extends KnotBase {
	
	public void read(String filename) throws IOException {
		StreamTokenizer st=new StreamTokenizer(new BufferedReader(new FileReader(filename)));
		Vector v=new Vector();
		st.nextToken();
		while (st.ttype!=StreamTokenizer.TT_EOF) {
			v.add(new Double(st.nval));
			st.nextToken();
		}
		coords=new double[v.size()/3][3];
		for(int i=0; i<v.size(); i++)
			coords[i/3][i%3]=((Double) v.get(i)).doubleValue();
		
		idx=new int[1][coords.length+1];
		for(int i=0; i<idx[0].length; i++) idx[0][i]=i;
		idx[0][coords.length]=0;
		
		init();
	}

	public static void main(String[] args) throws IOException {
		Knot k=new Knot();
		k.read("/home/brinkman/illiMath04/pytantrix/knots/3-1.txt");
		SimpleScene.display(k, 4.);
	}
}
