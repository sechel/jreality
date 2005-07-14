/*
 * Created on May 18, 2005
 *
 * This file is part of the de.jreality.reader package.
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

import java.io.StreamTokenizer;

/**
 * @author weissman
 *
 **/
class ParserUtil {
  final static double parseNumber(StreamTokenizer st)
      throws java.io.IOException {

    st.nextToken();
    
    double number = st.nval;

    st.nextToken();
    
    if (st.ttype == StreamTokenizer.TT_NUMBER || st.ttype == StreamTokenizer.TT_EOL  || st.ttype == StreamTokenizer.TT_EOF ) {
      st.pushBack();
      return number;
    }

    assert (st.sval.charAt(0) == 'e' || st.sval.charAt(0) == 'E');

    double exp = Double.parseDouble( st.sval.substring(1) );
    return number *  Math.pow(10, exp);
  }
}
