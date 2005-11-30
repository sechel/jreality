/*
 * Author	gunn
 * Created on Nov 30, 2005
 *
 */
package de.jreality.reader;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import de.jreality.reader.vrml.VRMLV1Lexer;
import de.jreality.reader.vrml.VRMLV1Parser;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;

public class ReaderVRML extends AbstractReader {

	public ReaderVRML() {
		super();
	}

	public void setInput(Input input) throws IOException {
		try {
 			VRMLV1Lexer lexer = new VRMLV1Lexer(new DataInputStream(input.getInputStream()));
			//lexer.setFilename(input.get);
			VRMLV1Parser parser = new VRMLV1Parser(lexer);
			//parser.setFilename(name);
            root = parser.vrmlFile();
            //VRMLHelper.viewSceneGraph(r);
        } 
        catch (RecognitionException e)  {
            e.printStackTrace();
        }
        catch (TokenStreamException e)  {
            e.printStackTrace();
        }
		
	}
}
