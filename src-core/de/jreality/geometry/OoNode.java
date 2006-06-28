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

package de.jreality.geometry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

class OoNode {

	private Set ingr = new HashSet();
	private Set deps = new HashSet();
	
	private Object object;
	
	private String name;
	
	private long timeStampOfLastUpdate = System.currentTimeMillis();
	
	private boolean currentlyUpdating = false;
	
	private boolean outOfDate = true;
	
	UpdateMethod updateMethod = null;
	
	static Logger outdateLog = Logger.getAnonymousLogger();//.getLogger("de.jreality.geometry.factory.outdate");
	static Logger updateLog  = Logger.getAnonymousLogger();//.getLogger("de.jreality.geometry.factory.update");
	{
		updateLog.setUseParentHandlers(false);
		outdateLog.setUseParentHandlers(false);
		//outdateLog.setLevel(Level.OFF);
		//updateLog.setLevel(Level.OFF);
	}
	
	public OoNode( String name ) {
		setObject(object);
		setName( name );
	}

	public OoNode( Object object, String name ) {
		setObject(object);
		setName( name );
	}

	public Object getObject() {
		update();
		return object;
	}
	
	public void setObject( Object object ) {
		if( this.object == object ) //&& this.object.equals( object))  //overhead too big
			return;
		this.object=object;
		outdate();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName( String name ) {
		this.name = name;
	}
	
	public void addIngr( OoNode node ) {
		if( node == this )
			throw new IllegalArgumentException( "node must not equal this" );
		ingr.add(node);
		node.deps.add(this);
	}

	public void addDeps( OoNode node ) {
	if( node == this )
			throw new IllegalArgumentException( "node must not equal this" );
		deps.add(node);
		node.ingr.add(this);
	}
	
	void outdateDeps() {
		for( Iterator iter=deps.iterator(); iter.hasNext(); ) {
			((OoNode)iter.next()).outdate();
		}
	}
	
	public void outdate() {
		if( outOfDate )
			return;
		outdateLog.info(name);
		outOfDate=true;
		outdateDeps();
	}
	
	public void update() {
		if( !outOfDate )
			return;
		
		if( currentlyUpdating ) {
			throw new IllegalStateException("encounterd loop in update graph: " + name );
		}
		
		currentlyUpdating=true;

		try {
			
			for( Iterator iter=ingr.iterator(); iter.hasNext(); ) {
				((OoNode)iter.next()).update();
			}
		
			if( updateMethod != null ) {
				updateLog.info(name);
				Object newObject = updateMethod.update( object );
				/*
				if( newObject != null && !newObject.equals(object) || newObject == null && object != null )
					outdateDeps();
				*/
				object = newObject;
			}
			
			timeStampOfLastUpdate = System.currentTimeMillis();
			
		} finally {
			currentlyUpdating=false;
		}
		outOfDate=false;
	}

	public void fire() {
		outdate();
		update();
	}
	
	public interface UpdateMethod {
		public Object update( Object object );
	}

	public boolean isOutOfDate() {
		return this.outOfDate;
	}

	public void setUpdateMethod(UpdateMethod method) {
		if( method == updateMethod )
			return;
		updateMethod = method;
		outdate(); //TODO: is this o.k. ?
	}
	
	public long getTimeStampOfLastUpdate() {
		return timeStampOfLastUpdate;
	}
}
