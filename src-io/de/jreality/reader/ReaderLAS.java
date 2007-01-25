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

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;


/**
 * Simple reader for borehole measurement data files.
 * 
 * @author msommer
 */
public class ReaderLAS extends AbstractReader {

	public ReaderLAS() {
		root = new SceneGraphComponent("borehole");
		Appearance app = new Appearance();
//		app.setAttribute(CommonAttributes.SPHERES_DRAW, false);
//		app.setAttribute(CommonAttributes.PICKABLE, false);
//		app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE, false);
//		app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 30.);
//		app.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//		app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		root.setAppearance(app);
	}

	
	@Override
	public void setInput(Input input) throws IOException {
		super.setInput(input);
		read();
	}

	
	private void read() throws IOException {
		
		LineNumberReader r = new LineNumberReader(input.getReader());
		String line = r.readLine();  //header
		
		StringTokenizer st = new StringTokenizer(line);
		final int n = st.countTokens();  //number of columns
		
		for (int i = 0; i < 3; i++) {
//		while ( (line=r.readLine())!=null ) {
			line = r.readLine();
			if (line.equals("")) continue;  //skip empty lines
			
			st = new StringTokenizer(line);
		    
			while (st.hasMoreTokens())  
		    	System.out.println(st.nextToken());
		    System.out.println();
		    System.out.println();
		}
	}
	
}