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


package de.jreality.ui.viewerapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;


public class Selection {

	protected SceneGraphPath sgPath;
	protected LinkedList<Object> tail;

	public Selection() {
		sgPath = new SceneGraphPath();
		tail = new LinkedList<Object>();
	}

	public Selection(Selection s) {
		this(s.sgPath);
		this.tail.addAll(s.tail);
	}
	
	public Selection(SceneGraphPath path) {
		this();
		sgPath = new SceneGraphPath(path);
	}

	public void clear() {
		sgPath.clear();
		tail.clear();
	}

	/**
	 * Selections always start with a SceneGraphPath.
	 * @return first SceneGraphNode of the contained path.
	 */
	public SceneGraphNode getFirstElement() {
		return sgPath.getFirstElement();
	}


	public double[] getInverseMatrix(double[] aMatrix, int begin, int end) {
		return sgPath.getInverseMatrix(aMatrix, begin, end);
	}
	public double[] getInverseMatrix(double[] invMatrix, int begin) {
		return sgPath.getInverseMatrix(invMatrix, begin);
	}
	public double[] getInverseMatrix(double[] invMatrix) {
		return sgPath.getInverseMatrix(invMatrix);
	}

	
	/**
	 * Truncates the selection to SceneGraphNodes.
	 * @return the contained scene graph path
	 */
	public SceneGraphPath getSGPath() {
		return sgPath;
	}


	public SceneGraphComponent getLastComponent() {
		return sgPath.getLastComponent();
	}


	public SceneGraphNode getLastNode() {
		return sgPath.getLastElement();
	}


	public Object getLastElement() {
		if (tail.isEmpty()) return getLastNode();
		return tail.getLast();
	}


	public boolean isSGPath() {
		return tail.isEmpty();
	}
	
	
	public int getLength() {
		return (sgPath.getLength() + tail.size());
	}


	public double[] getMatrix(double[] aMatrix, int begin, int end) {
		return sgPath.getMatrix(aMatrix, begin, end);
	}
	public double[] getMatrix(double[] aMatrix, int begin) {
		return sgPath.getMatrix(aMatrix, begin);
	}
	public double[] getMatrix(double[] aMatrix) {
		return sgPath.getMatrix(aMatrix);
	}


	@Override
	public int hashCode() {
		int result = 1;
		for (Object element : toList())
			result = 31 * result + element.hashCode();
		return result;
	}


	@Override
	public boolean equals(Object s) {
		if (s instanceof Selection)
			return isEqual((Selection) s);
		return false;
	}


	public boolean isEqual(Selection anotherSelection) {

		if (anotherSelection == null || getLength() != anotherSelection.getLength())	return false;

		List l1 = toList();
		List l2 = anotherSelection.toList();
		for (int i=0; i<getLength(); ++i)
			if (!l1.get(i).equals(l2.get(i))) return false;

		return true;
	}


//	public boolean isValid() {
//		return sgPath.isValid();
//	}


	public ListIterator<Object> iterator() {
		return Collections.unmodifiableList(toList()).listIterator();
	}


	public ListIterator<Object> iterator(int start) {
		return Collections.unmodifiableList(toList()).listIterator(start);
	}


	public Iterator reverseIterator() {
		return reverseIterator(getLength());
	}


	public Iterator reverseIterator(int start) {
		final ListIterator iter = iterator(start);
		return new Iterator() {

			public void remove() {
				iter.remove();
			}

			public boolean hasNext() {
				return iter.hasPrevious();
			}

			public Object next() {
				return iter.previous();
			}
		};
	}


	public boolean startsWith(Selection potentialPrefix) {
		if (getLength() < potentialPrefix.getLength()) return false;
		Iterator i1 = iterator();
		Iterator i2 = potentialPrefix.iterator();
		while(i2.hasNext())
			if (i1.next() != i2.next()) return false;
		return true;
	}


	public List<Object> toList() {
		List<Object> list = new ArrayList<Object>(getLength());
		list.addAll(sgPath.toList());
		list.addAll(tail);
		return list;
	}


	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(sgPath.toString());
		for (Object t : tail)	str.append(" : ").append(t.toString());
		return str.toString();
	}

}