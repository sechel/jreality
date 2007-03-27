package de.jreality.geometry;

import java.util.Iterator;
import java.util.Map;

import de.jreality.geometry.OoNode.IsUpdateCounter;
import de.jreality.scene.Geometry;
import de.jreality.scene.Scene;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;


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
		if( !gals.hasEntries())
			return;
		
		Geometry geometry = gals.factory.geometry;
		String category = gals.category;
		
		if( geometry.getNumEntries( category ) == gals.noa() ) {
			
			for( Iterator iter = gals.DLS.storedAttributes().iterator(); iter.hasNext(); ) {
				Attribute attr = (Attribute)iter.next();
				gals.attributeNode( attr ).update();
				if(  nodeWasUpdated(gals.attributeNode( attr ))  ) {
					log( "set", attr, category);
					geometry.setAttributes( category, attr, gals.DLS.getWritableList(attr));
				}
			}
		} else {
			gals.updateAttributes();
			geometry.setCountAndAttributes( category, gals.DLS);		
		}
	}
	
	void updateNode( GeometryAttributeListSet gals, Attribute attr, boolean generate, OoNode node ) {
		Geometry geometry = gals.factory.geometry;
		String category = gals.category;
			
		if (generate) {
			if (nodeWasUpdated(node)) {
				log("set", attr, category);
				DataList dl = node.createDataList();
				geometry.setAttributes(category, attr, dl );
			}
		} else if (!gals.DLS.containsAttribute(attr)
				&& geometry.getAttributes(gals.category, attr) != null) {
			log("cancle", attr, category);
			geometry.setAttributes(category, attr, null);
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
