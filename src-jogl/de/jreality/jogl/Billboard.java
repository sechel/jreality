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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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

package de.jreality.jogl;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class Billboard extends IndexedFaceSet {

	static double[][] points = {{0,0,0},{1,0,0},{1,1,0},{0,1,0}};
    static int[][] indices = {{0,1,2,3}};
    static double[][] texs = {{0,1},{1,1},{1,0},{0,0}};
	public Billboard() {
		super();
		setNumPoints(4);
		vertexAttributes.addReadOnly(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3),points);
		setNumFaces(1);
		vertexAttributes.addReadOnly(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.array(2),texs);
		faceAttributes.addReadOnly(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY, indices);
		GeometryUtility.calculateAndSetNormals(this);
	}

	double xscale=1.0, yscale=1.0;
	double[] offset = {0,0,0};
	double[] position = {0,0,0};
	public double[] getPosition() {
		return position;
	}
	public void setPosition(double[] position) {
		this.position = position;
	}
	public double[] getOffset() {
		return offset;
	}
	public void setOffset(double[] offset) {
		this.offset = offset;
	}
	public double getXscale() {
		return xscale;
	}
	public void setXscale(double xscale) {
		this.xscale = xscale;
	}
	public double getYscale() {
		return yscale;
	}
	public void setYscale(double yscale) {
		this.yscale = yscale;
	}
	
	
}
