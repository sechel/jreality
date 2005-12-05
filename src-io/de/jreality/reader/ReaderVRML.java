/*
 * Author	gunn
 * Created on Nov 30, 2005
 *
 */
package de.jreality.reader;

import java.beans.Expression;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;

public class ReaderVRML extends AbstractReader {

	public ReaderVRML() {
		super();
	}

	public void setInput(Input input) throws IOException {
		try {
      Constructor lexC = Class.forName("de.jreality.reader.vrml.VRMLV1Lexer").getConstructor(new Class[]{InputStream.class});
 			Object lexer = lexC.newInstance(new Object[]{input.getInputStream()});
      
      Constructor parseC = Class.forName("de.jreality.reader.vrml.VRMLV1Parser").getConstructor(new Class[]{Class.forName("antlr.TokenStream")});
      Object parser = parseC.newInstance(new Object[]{lexer});
			
      Expression parse = new Expression(parser, "vrmlFile", null);
      root = (SceneGraphComponent) parse.getValue();
    } catch (ClassNotFoundException e) {
      LoggingSystem.getLogger(this).severe("VRML 1 parsing failed, call ANTLR first!");
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      throw new Error();
    } catch (IllegalArgumentException e) {
      throw new Error();
    } catch (InstantiationException e) {
      throw new Error();
    } catch (IllegalAccessException e) {
      throw new Error();
    } catch (InvocationTargetException e) {
      throw new Error();
    } catch (Exception e) {
      LoggingSystem.getLogger(this).severe("parsing "+input+" failed: "+e.getMessage());
    }
	}
}
