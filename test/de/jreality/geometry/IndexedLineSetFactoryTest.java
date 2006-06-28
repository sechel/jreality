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


package de.jreality.geometry;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.TestCase;
import de.jreality.ui.viewerapp.ViewerApp;

public class IndexedLineSetFactoryTest extends TestCase {

	IndexedLineSetFactory factory;
	
	static double [] vertices  = new double[] {

	 0,  0,  0,
	 1,  0,  0,
	 1,  1,  0,
	 0,  1,  0,

	 0,  0,  1,
	 1,  0,  1,
	 1,  1,  1,
	 0,  1,  1,

	};

	static int [][] indices = new int [][] {

	{ 0, 1, 2, 3 }, 
	{ 7, 6, 5, 4 }, 
	{ 0, 1, 5, 4 }, 
	{ 1, 2, 6, 5 }, 
	{ 2, 3, 7, 6 }, 
	{ 3, 0, 4, 7 }, 

	};

	ActionHandler actionHandler;
	Logger actionLogger = Logger.getLogger("de.jreality.geometry.action");
	
	public void setUp() {
		
		actionHandler = new ActionHandler();
		actionHandler.setLevel( Level.INFO );
		
		AbstractPointSetFactory.actionLogger = actionLogger;
		//actionLogger.setUseParentHandlers(false);
		
		actionLogger.addHandler( actionHandler );
		
		
		factory=new de.jreality.geometry.IndexedLineSetFactory();

	}
	
	public void tearDown() {
		
		
	}
	
	
	
	
	public void test() {

		factory.setVertexCount( 8 );
		factory.setVertexCoordinates( vertices );
		factory.setLineCount(6);
		factory.setEdgeIndices( indices );
		
		factory.update();
		
		ViewerApp.display( factory.getIndexedLineSet() );
		
		actionHandler.clear();
		
		factory.setEdgeIndices( indices );
		
		factory.update();
		
		actionHandler.clear();
		
		factory.setVertexCoordinates( vertices );
		
		factory.update();
		
	}
	
	static class ActionHandler extends Handler {

		HashSet actions = new HashSet();
		HashMap actionsParam = new HashMap();
		
		boolean isClosed = false;
		public void close() throws SecurityException {
			isClosed = true;
		}

		public void clear() {
			actions.clear();
			actionsParam.clear();
		}

		public void flush() {
		}

		public void publish(LogRecord record) {
			if( isClosed )
				throw new RuntimeException( "action handler is closed");
			
			String msg = record.getMessage();
			
			if(actions.contains(msg))
				TestCase.fail( "multiple action invocation" );
			else
				actions.add(msg);
			
			actionsParam.put( msg, record.getParameters() );
			
		}
		
	}
	
	public static void main( String [] arg ) {

		IndexedLineSetFactory factory = new IndexedLineSetFactory();


		factory.setVertexCount( 8 );
		factory.setLineCount( 6 );	
		factory.setVertexCoordinates( vertices );
		factory.setEdgeIndices( indices );
	    factory.setGenerateEdgeLabels(true);
	    factory.setGenerateVertexLabels(true);
		factory.setLineCount( 2 );
		factory.setEdgeIndices( new int[][] {{0,1},{2,3}} );
		factory.setEdgeColors( new Color[] {Color.RED, Color.YELLOW} );
		//factory.setEdgeLabels( new String[] {"A","B"} );
		factory.update();
		
		ViewerApp.display(factory.getIndexedLineSet());
	}
}