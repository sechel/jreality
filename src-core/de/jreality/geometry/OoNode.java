package de.jreality.geometry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
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
