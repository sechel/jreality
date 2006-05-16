package de.jreality.reader.mathematica;
import java.io.File;
import java.io.FileReader;


import de.jreality.scene.SceneGraphComponent;
import de.jreality.reader.mathematica.MathematicaLexer;
import de.jreality.reader.mathematica.MathematicaParser;


public class TestParser {
	public static void main(String[] args) throws Exception {
		
//		File f= new File("testAll.m");
//		FileReader r= new FileReader(f);
//		MathematicaLexer l= new MathematicaLexer(r);
//		MathematicaParser p=new MathematicaParser(l);
		
		MathematicaParser p=new MathematicaParser
			(new MathematicaLexer(new FileReader(new File("testAll.m"))));
		SceneGraphComponent cmp =p.start();
		
		//SceneGraphComponent cmp =de.jreality.reader.Readers.read(Input.getInput("testAll.m"));
		if (cmp==null) System.out.println("kein Graph !!!!!!!!!!!!!!!!");
		else de.jreality.ui.viewerapp.ViewerApp.display(cmp);	
		
	}
}