package de.jreality.geometry;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import de.jreality.ui.viewerapp.ViewerApp;

import junit.framework.TestCase;

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