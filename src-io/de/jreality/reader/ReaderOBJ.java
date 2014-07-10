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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import de.jreality.reader.OBJModel.Vertex;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;

/**
 * 
 * simple reader for the OBJ file format.
 * 
 * smoothening group support is poor (what is a smoothining group?)
 * 
 * @author weissman
 * 
 */
public class ReaderOBJ extends AbstractReader {

	private static Logger logger = LoggingSystem.getLogger(ReaderOBJ.class.getSimpleName());
	
	/**
	 * If true the edges of the indexed face set are generated automatically. 
	 */
	private boolean generateEdgesFromFaces = true;
	
	/**
	 * In an obj-file multiple texture and normal coordinates may be specified.
	 * These are ignored depending on the value of this parameter. 
	 */
	private boolean useMultipleTexAndNormalCoords = false;
	

	public boolean isUseMultipleTexAndNomalCoords() {
		return useMultipleTexAndNormalCoords;
	}

	public void setUseMultipleTexAndNormalCoords(boolean useMultipleTexAndNormalCoords) {
		this.useMultipleTexAndNormalCoords = useMultipleTexAndNormalCoords;
	}

	public boolean isGenerateEdgesFromFaces() {
		return generateEdgesFromFaces;
	}

	public void setGenerateEdgesFromFaces(boolean generateEdgesFromFaces) {
		this.generateEdgesFromFaces = generateEdgesFromFaces;
	}

	public ReaderOBJ() {
		root = new SceneGraphComponent();
		root.setAppearance(ParserMTL.createDefault());
	}

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
		st.wordChars('\u00A0', '\u00FF');
		st.whitespaceChars('\u0000', '\u0020');
		st.commentChar('#');
		st.ordinaryChar('/');
		st.parseNumbers();
		return st;
	}

	private StreamTokenizer filenameSyntax(StreamTokenizer st) {
		st.resetSyntax();
		st.eolIsSignificant(true);
		st.wordChars('0', '9');
		st.wordChars('A', 'Z');
		st.wordChars('a', 'z');
		st.wordChars('_', '_');
		st.wordChars('.', '.');
		st.wordChars('-', '-');
		st.wordChars('+', '+');
		st.wordChars('\u00A0', '\u00FF');
		st.whitespaceChars('\u0000', '\u0020');
		st.commentChar('#');
		st.ordinaryChar('/');
		st.parseNumbers();
		return st;
	}

	private void load() throws IOException {
		StreamTokenizer st = new StreamTokenizer(input.getReader());
		globalSyntax(st);
		
		OBJModel model = new OBJModel();
		
		while (st.nextToken() != StreamTokenizer.TT_EOF) {
			if (st.ttype == StreamTokenizer.TT_WORD) {
				String word = st.sval;
				if (word.equalsIgnoreCase("v")) { // vertex
					double[] coords = ParserUtil.parseDoubleArray(st);
					if (coords.length != 3 && coords.length != 4 ) {
						System.err.println("vertex coordinates must have dimension 3 or 4");
					} else {
						model.addVertexCoords(coords);
					}
					continue;
				}
				if (word.equalsIgnoreCase("vp")) { // vertex parameter data
					ignoreTag(st);
					continue;
				}
				if (word.equalsIgnoreCase("vn")) { // vertex normal
					double[] n = ParserUtil.parseDoubleArray(st);
					if (n.length > 3) {
						System.err.println("vertex normal must have dimension 3");
					} else {
						model.addNormalCoords(n);
					}
					continue;
				}
				if (word.equalsIgnoreCase("vt")) { // vertex texture coordinate
					double[] tex = ParserUtil.parseDoubleArray(st);
					if (tex.length > 4) {
						System.err.println("texture coordinates must have dimension <= 4");
					} else {
						model.addTextureCoords(tex);
					}
					continue;
				}
				if (word.equalsIgnoreCase("g")) { // grouping
					List<String> groupNames = ParserUtil.parseStringArray(st);
					model.setActiveGroups(groupNames);
					continue;
				}
				if (word.equalsIgnoreCase("s")) { // smoothening group
					ignoreTag(st);
					continue;
				}
				if (word.equalsIgnoreCase("p")) { // points v1 v2 v3 ...
					List<Vertex> points = parseVertexList(st);
					model.addPoints(points);
					continue;
				}
				if (word.equalsIgnoreCase("l")) { // lines v1/vt1 v2/vt2 ...
					List<Vertex> l = parseVertexList(st);
					model.addLine(l);
					continue;
				}
				if (word.equalsIgnoreCase("f")) { // facet v1/vt1/vn1 v2/vt2/vn2
					List<Vertex> face = parseVertexList(st);
					model.addFace(face);
					continue;
				}
				if (word.equalsIgnoreCase("mtllib")) { //mtllib filename1 filename2
					filenameSyntax(st);
					List<String> mtlfiles = ParserUtil.parseStringArray(st);
					for(String fileName : mtlfiles) {
						try {
							List<Appearance> app = ParserMTL.readAppearences(input.resolveInput(fileName));
							for (Iterator<Appearance> i = app.iterator(); i.hasNext();) {
								Appearance a = (Appearance) i.next();
								model.addMaterial(a);
							}
						} catch (FileNotFoundException fnfe) {
							logger.info("Couldn't find material file: " + fileName);
						}
					}
					globalSyntax(st);
					continue;
				}
				if (word.equalsIgnoreCase("usemtl")) { // facet v1/vt1/vn1
					List<String> mtlList = ParserUtil.parseStringArray(st);
					String mtlName = mtlList.get(0);
					model.useMaterial(mtlName);
					continue;
				}
				logger.fine("Unhandled tag: " + word);
				int token = st.nextToken();
				while (token != TT_EOL && token != TT_EOF) {
					if (st.ttype == StreamTokenizer.TT_NUMBER)
						logger.fine("" + st.nval);
					else
						logger.fine(st.sval);
					token = st.nextToken();
				}
				logger.fine("Unhandled tag: " + word + " end");
			}
		}
		for(SceneGraphComponent sgc : model.getComponents(useMultipleTexAndNormalCoords,generateEdgesFromFaces)) {
			root.addChild(sgc);
		}
	}

	private void ignoreTag(StreamTokenizer st) throws IOException {
		while (st.nextToken() != StreamTokenizer.TT_EOL)
			;
	}

	static Vertex parseVertex(StreamTokenizer st) throws IOException {
		Vertex v = new OBJModel.Vertex();
		st.nextToken();
		v.setVertexIndex((int) st.nval);
		st.nextToken();
		if (st.ttype == '/') {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_NUMBER) {
				v.setTextureIndex((int) st.nval);
				st.nextToken();
			} 
			if(st.ttype == '/') {
				st.nextToken();
				if (st.ttype == StreamTokenizer.TT_NUMBER) {
					v.setNormalIndex((int) st.nval);
				}
			} else {
				st.pushBack();
			}
		} else {
			st.pushBack();
		}
		return v;
	}
	
	static List<Vertex> parseVertexList(StreamTokenizer st) throws IOException {
		ArrayList<Vertex> v = new ArrayList<Vertex>(3);
		st.nextToken();
		while (st.ttype != TT_EOL && st.ttype != TT_EOF) {
			if (st.ttype == '\\') {
				st.nextToken(); // the EOL
				st.nextToken(); // continue parsing in the next line
				continue;
			} 
			else {
				st.pushBack();
				v.add(parseVertex(st));
			}
			st.nextToken();
		}
		return v;
	}
}
