/*
 * Created on 28-Feb-2005
 *
 * This file is part of the jReality package.
 *
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
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

/**
 *
 * reader for the Mathematica [SurfaceGraphics,...] file format. 
 * 
 * @author gonska
 *
 */
public class ReaderMATHEMATICA extends AbstractReader {

  public void setInput(Input input) throws IOException {
    try {
      Constructor lexC = Class.forName("de.jreality.reader.Mathematica.MathematicaLexer").getConstructor(new Class[]{InputStream.class});
      Object lexer = lexC.newInstance(new Object[]{input.getInputStream()});
      
      Constructor parseC = Class.forName("de.jreality.reader.Mathematica.MathematicaParser").getConstructor(new Class[]{Class.forName("antlr.TokenStream")});
      Object parser = parseC.newInstance(new Object[]{lexer});
      
      Expression parse = new Expression(parser, "start", null);
      root = (SceneGraphComponent) parse.getValue();
      
    } catch (ClassNotFoundException e) {
      LoggingSystem.getLogger(this).severe("Mathematica parsing failed, call ANTLR first!");
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      throw new Error();
//    } catch (IllegalArgumentException e) {
//      throw new Error();
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
