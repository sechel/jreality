/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.reader;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_EOL;
import static java.io.StreamTokenizer.TT_NUMBER;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.LinkedList;
import java.util.List;

/**
 * @author weissman
 *
 **/
public class ParserUtil {
	public final static double parseNumber(StreamTokenizer st)
		throws java.io.IOException {
		st.nextToken();
		
		double number = st.nval;

		st.nextToken();
    
	    if (st.ttype == StreamTokenizer.TT_NUMBER || 
    		st.ttype == StreamTokenizer.TT_EOL  || 
    		st.ttype == StreamTokenizer.TT_EOF ||
    		st.ttype == '\\' || 
    		st.sval.startsWith("+")) 
	    {
	      st.pushBack();
	      return number;
	    }
	
	    assert (st.sval.charAt(0) == 'e' || st.sval.charAt(0) == 'E');
	
	    double exp = Double.parseDouble( st.sval.substring(1) );
	    return number *  Math.pow(10, exp);
	  }

	public static double[] parseDoubleArray(StreamTokenizer st) throws IOException {
		List<Double> cList = new LinkedList<Double>();
		st.nextToken();
		while (st.ttype == TT_NUMBER || st.ttype == '\\' || st.sval.startsWith("+")) {
			if (st.ttype == '\\') {
				st.nextToken(); // the EOL
				st.nextToken(); // continue parsing in the next line
				continue;
			} else if(st.ttype == TT_NUMBER) {
				st.pushBack();
				cList.add(ParserUtil.parseNumber(st));
			} else if(st.sval.startsWith("+")) {
				cList.add(Double.parseDouble(st.sval.replace("+", "")));
			}
			st.nextToken();
			if(st.ttype == TT_EOF || st.ttype == TT_EOL) {
				break;
			}
		}
		st.pushBack();
		double[] coords = new double[cList.size()];
		for (int i = 0; i < coords.length; i++) {
			coords[i] = cList.get(i);
		}
		return coords;
	}

	public static List<String> parseStringArray(StreamTokenizer st) throws IOException {
		List<String> groupNames = new LinkedList<String>();
		
		st.nextToken();
		while(st.ttype != TT_EOL && st.ttype != TT_EOF) {
			if (st.ttype == '\\') {
				st.nextToken(); // the EOL
				st.nextToken(); // continue parsing in the next line
				continue;
			} 
			groupNames.add(st.sval);
			st.nextToken();
		}
		return groupNames;
	}

	public static List<Integer> parseIntArray(StreamTokenizer st) throws IOException {
		List<Integer> integers = new LinkedList<Integer>();
		
		st.nextToken();
		while(st.ttype != TT_EOL && st.ttype != TT_EOF) {
			if (st.ttype == '\\') {
				st.nextToken(); // the EOL
				st.nextToken(); // continue parsing in the next line
				continue;
			}
			if(st.ttype == StreamTokenizer.TT_NUMBER) {
				integers.add((int) st.nval);
			}
			st.nextToken();
		}
		return integers;
	}
}
