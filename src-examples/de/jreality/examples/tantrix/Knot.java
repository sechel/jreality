/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples.tantrix;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Vector;

import de.jreality.examples.SimpleScene;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.MatrixBuilder;
import de.jreality.util.VecMat;

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
		double maxnorm=0.;
		for(int i=0; i<coords.length; i++) {
			double nrm=VecMat.norm(coords[i]);
			if (nrm>maxnorm)
				maxnorm=nrm;
		}
		for(int i=0; i<v.size(); i++)
			coords[i/3][i%3]/=maxnorm;
		
		idx=new int[1][coords.length+1];
		for(int i=0; i<idx[0].length; i++) idx[0][i]=i;
		idx[0][coords.length]=0;
		
		init();
	}

	public static void main(String[] args) throws IOException {
		Knot k=new Knot();
		k.read("/home/brinkman/illiMath04/pytantrix/knots/7-3smooth.txt");
		Tantrix r1=new Tantrix(k);
		Binix r2=new Binix(k);
		Tertrix r3=new Tertrix(k);
		
        SceneGraphComponent kbase=new SceneGraphComponent();
        SceneGraphComponent knode=new SceneGraphComponent();
        SceneGraphComponent rnode=new SceneGraphComponent();
        SceneGraphComponent r1node=new SceneGraphComponent();
        SceneGraphComponent r11node=new SceneGraphComponent();
        SceneGraphComponent r2node=new SceneGraphComponent();
        SceneGraphComponent r21node=new SceneGraphComponent();
        SceneGraphComponent r3node=new SceneGraphComponent();
        SceneGraphComponent r31node=new SceneGraphComponent();
        
        MatrixBuilder.euclidian().translate(1.5, 0, 0).assignTo(knode);
        MatrixBuilder.euclidian().translate(-1.5, 0, 0).assignTo(rnode);
        MatrixBuilder.euclidian().scale(-1).assignTo(r11node);
        MatrixBuilder.euclidian().scale(-1).assignTo(r21node);
        MatrixBuilder.euclidian().scale(-1).assignTo(r31node);
        
        kbase.addChild(knode);
        kbase.addChild(rnode);
        rnode.addChild(r1node);
        rnode.addChild(r2node);
        rnode.addChild(r3node);
        r1node.addChild(r11node);
        r2node.addChild(r21node);
        r3node.addChild(r31node);
        
        Appearance kapp=new Appearance();
        Appearance r1app=new Appearance();
        Appearance r2app=new Appearance();
        Appearance r3app=new Appearance();
        
        r1app.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1, 0, 0));
        r2app.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(0, 1, 0));
        r3app.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(0, 0, 1));
        
        knode.setAppearance(kapp);
        r1node.setAppearance(r1app);
        r2node.setAppearance(r2app);
        r3node.setAppearance(r3app);
        
        knode.setGeometry(k);
        r1node.setGeometry(r1);
        r11node.setGeometry(r1);
        r2node.setGeometry(r2);
        r21node.setGeometry(r2);
        r3node.setGeometry(r3);
        r31node.setGeometry(r3);
        
		SimpleScene.display(kbase, 4.);
	}
}
