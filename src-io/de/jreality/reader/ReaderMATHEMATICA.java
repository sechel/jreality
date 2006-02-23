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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StreamTokenizer;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;

/**
 *
 * simple reader for the Mathematica [SurfaceGraphics,...] file format. 
 * 
 * @author gonska
 *
 */
public class ReaderMATHEMATICA extends AbstractReader {

  public void setInput(Input input) throws IOException {
    super.setInput(input);
    load();
  }

  private StreamTokenizer globalSyntax(StreamTokenizer st) {
    st.resetSyntax();
    st.eolIsSignificant(true);
    st.wordChars('0', '9');
    st.wordChars('A', 'Z');
    st.wordChars('a', 'z');
    st.wordChars('_', '_');
    st.wordChars('.', '.');
    st.wordChars('-', '-');
    st.wordChars('+', '+');
    st.wordChars('*', '*');
    st.wordChars('^', '^');
    st.wordChars('\u00A0', '\u00FF');
    st.whitespaceChars('\u0000', '\u0020');
    st.commentChar('#');
    st.ordinaryChar('/');
    //st.parseNumbers();
    return st;
  }

  private void load() throws IOException {
    StreamTokenizer st = new StreamTokenizer(input.getReader());
    globalSyntax(st);
    System.out.println("Start reading...");
    
    root = new SceneGraphComponent();
    
    // add content to root!
    while (st.nextToken() != StreamTokenizer.TT_EOF) {
      if (st.ttype == StreamTokenizer.TT_WORD) {
        System.out.println("word="+st.sval);
      }
//      else if (st.ttype == StreamTokenizer.TT_NUMBER) {
//        System.out.println("number="+st.nval);
//      }
      else if (st.ttype == StreamTokenizer.TT_EOL) {
        continue;
      } else {
        System.out.println("unknown="+st.ttype);
      }
    } 
    System.out.println("...finish reading!");
  }

}
