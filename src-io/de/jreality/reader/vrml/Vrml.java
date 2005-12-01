package de.jreality.reader.vrml;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;

class Vrml {
	
	public static void main(String[] args) {
		JFileChooser fc = new JFileChooser("/homes/geometer/gunn/Documents/Models/VRML/");
		//JOGLConfiguration.theLog.log(Level.INFO,"FCI resource dir is: "+resourceDir);
		int result = fc.showOpenDialog(new JFrame());
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			String name = file.getAbsolutePath();
			try {
	        	//InputStream is = Vrml.class.getResourceAsStream(name);
				InputStream is = null;
				try {
					is = new FileInputStream(name);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				VRMLV1Lexer lexer = new VRMLV1Lexer(new DataInputStream(is));
				lexer.setFilename(name);
				VRMLV1Parser parser = new VRMLV1Parser(lexer);
				parser.setFilename(name);
	            SceneGraphComponent r = parser.vrmlFile();
	            //VRMLHelper.viewSceneGraph(r);
	        } 
	        catch (RecognitionException e)  {
	            e.printStackTrace();
	        }
	        catch (TokenStreamException e)  {
	            e.printStackTrace();
	        }
	        System.out.println("Got through it");
		}
	}
}
