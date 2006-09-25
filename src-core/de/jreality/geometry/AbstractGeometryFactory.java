package de.jreality.geometry;

import java.util.Iterator;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.ISUB;

import de.jreality.geometry.OoNode.IsUpdateCounter;
import de.jreality.scene.Geometry;
import de.jreality.scene.Scene;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class AbstractGeometryFactory {

	final OoNode signature;
	
	UpdateCounter update = new UpdateCounter();
	
	static class UpdateCounter implements IsUpdateCounter {
		long counter = 0;
		public long getUpdateCount() {
			return counter;
		}
	};
	
	final Geometry geometry;
	
	AbstractGeometryFactory( Geometry geometry, int signature ) {
		
		this.signature = node( new Integer( signature ), "signature" );
		this.geometry = geometry;
		GeometryUtility.setSignature( geometry,signature);
	}
	
	public Geometry getGeometry() {
		return geometry;
	}
	
	OoNode node(String name) {
		return new OoNode( name, update );
	}

	OoNode node(Object object, String name) {
		return new OoNode( object, name, update );
	}

	OoNode geometryAttributeNode(Map attributeNode, String name, Attribute attr) {
		if( attributeNode.containsKey(attr))
			return (OoNode)attributeNode.get( attr );
		
		OoNode node = node( name+"."+attr );
		
		attributeNode.put( attr, node );
		
		return node;
	}

	protected boolean nodeWasUpdated(OoNode node) {
		return node.getCounterOfLastUpdate() == update.getUpdateCount();
	}

	public int getSignature() {
		return ((Integer)signature.getObject()).intValue();
	}

	public void setSignature(int signature) {
		this.signature.setObject( new Integer( signature) );
	}

	
	void recompute() {
	}

	public void update() {
		update.counter++;
		recompute();
		
		Scene.executeWriter( geometry, new Runnable() {
			
			public void run() {
				
				updateImpl();
			}
		}
		);
		
	}

	void updateImpl() {
		GeometryUtility.setSignature( geometry, getSignature());	
	}

	void updateGeometryAttributeCathegory( GeometryAttributeListSet gals ) {
		Geometry geometry = gals.factory.geometry;
		String cathegory = gals.cathegory;
		
		if( geometry.getNumEntries( cathegory ) == gals.noa() ) {
			
			for( Iterator iter = gals.DLS.storedAttributes().iterator(); iter.hasNext(); ) {
				Attribute attr = (Attribute)iter.next();
				
				gals.attributeNode( attr ).update();
				if(  nodeWasUpdated(gals.attributeNode( attr ))  ) {
					log( "set", attr, cathegory);
					geometry.setAttributes( cathegory, attr, gals.DLS.getWritableList(attr));
				}
			}
		} else {
			gals.updateAttributes();
			geometry.setCountAndAttributes( cathegory, gals.DLS);		
		}
	}
	
	void updateStringArray( GeometryAttributeListSet gals, Attribute attr, boolean generate, OoNode node ) {
		Geometry geometry = gals.factory.geometry;
		String cathegory = gals.cathegory;
			
		if( generate ) { 
			if( nodeWasUpdated( node ) ) { 
				log( "set", attr,  attr.toString());
				geometry.setAttributes( cathegory, attr, StorageModel.STRING_ARRAY.createReadOnly( (String[])(node.getObject())));
			} 
		} else if( !gals.DLS.containsAttribute( attr ) ) {
			log( "cancle", attr, attr.toString());
			geometry.setAttributes( cathegory, attr, null );
		}
	
	}
	
	String logMessage(String action, String attr, String cathegory) {
		return action + " " + cathegory + " " + attr;
	}

	void log(String action, Attribute attr, String cathegory) {
		log(action, attr.getName(), cathegory);
	}

	boolean debug = false;

	void log(String action, String attr, String cathegory) {
	//		if( actionLogger != null ) {
	//			actionLogger.log( Level.INFO, logMessage(action, attr, cathegory),
	//					new Object[] {action, attr, cathegory } );
	//		}
			if( debug )
				System.out.println( logMessage( action, attr, cathegory ) );
		}

}
