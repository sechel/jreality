package de.jreality.geometry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.math.Pn;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.WritableDataList;

class AbstractPointSetFactory {
	
	final OoNode signature;
	
	DataListSet vertexDLS = new DataListSet(0);

	HashMap vertexAttributeNode = new HashMap();
	
	final PointSet ps;

	long timeStampOfLastRefactor;
	
	AbstractPointSetFactory( PointSet ps, int signature ) {
		
		this.signature = new OoNode( new Integer( signature ), "signature" );
		
		this.ps = ps;
		
		GeometryUtility.setSignature(ps,signature);
	}

	public AbstractPointSetFactory() {
		this( new PointSet(), Pn.EUCLIDEAN);
	}
	
	
	int nov(){
		return vertexDLS.getListLength();
	}
	
	public int getVertexCount() {
		return nov();
	}
	
	public void setVertexCount( int count ) {
		if( count == nov() )
			return;
		
		vertexDLS.reset(count);
	}
	
	static OoNode geometryAttributeNode( Map attributeNode, String name, Attribute attr ) {
		if( attributeNode.containsKey(attr))
			return (OoNode)attributeNode.get( attr );
		
		OoNode node = new OoNode( name+"."+attr );
		
		attributeNode.put( attr, node );
		
		return node;
	}
	
	OoNode vertexAttributeNode( Attribute attr ) {
		return this.geometryAttributeNode( vertexAttributeNode, "VERTEX", attr );
	}
	
	boolean nodeWasUpdated( OoNode node ) {
		return node.getTimeStampOfLastUpdate() > timeStampOfLastRefactor;
	}
	
	void updateVertexAttributes() {
		for( Iterator iter = vertexDLS.storedAttributes().iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			vertexAttributeNode( attr ).update();	
		}
	}
	
	protected void setVertexAttribute( Attribute attr, DataList data ) {
		setAttrImpl( vertexDLS, attr, data );
		vertexAttributeNode(attr).setObject( data );
	}
	
	protected void setVertexAttributes(DataListSet dls ) {
		vertexDLS = dls;	
		for( Iterator iter = dls.storedAttributes().iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			vertexAttributeNode(attr).setObject( dls.getList(attr));
		}
	}
	
	static final void setAttrImpl(DataListSet target, Attribute a, DataList d ) {
		
		if (d == null) {
			target.remove(a);
		} else {
			WritableDataList w;
			w=target.getWritableList(a);
			if(w==null) w=target.addWritable(a, d.getStorageModel());
			d.copyTo(w);
		}
	} 
	

	protected void setVertexCoordinates( DataList data ) {
		setVertexAttribute( Attribute.COORDINATES, data );
	}
	
	protected void setVertexCoordinates( double [] data ) {
		if( nov() == 0 && data.length != 0 || data.length % nov() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.COORDINATES, new DoubleArrayArray.Inlined( data, data.length / nov() ) );
	}
	
	protected void setVertexCoordinates( double [][] data ) {
		//setVertexAttribute( Attribute.COORDINATES, new DoubleArrayArray.Array( data, data[0].length ) );
		setVertexAttribute( Attribute.COORDINATES,
				StorageModel.DOUBLE_ARRAY.array(data[0].length).createReadOnly(data));
	}
	
	protected void setVertexNormals( DataList data ) {
		setVertexAttribute( Attribute.NORMALS, data );
	}
	
	protected void setVertexNormals( double [] data ) {
		if( data.length % nov() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.NORMALS, new DoubleArrayArray.Inlined( data,  data.length / nov() ) );
	}
	
	protected void setVertexNormals( double [][] data ) {
		setVertexAttribute( Attribute.NORMALS, new DoubleArrayArray.Array( data, data[0].length ) );
	}
	
	protected void setVertexColors( DataList data ) {
		setVertexAttribute( Attribute.COLORS, data );
	}
	
	protected void setVertexColors( double [] data ) {
		if( data.length % nov() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.COLORS, new DoubleArrayArray.Inlined( data, data.length / nov() )  );
	}
	
	protected void setVertexColors( double [][] data ) {
		setVertexAttribute( Attribute.COLORS, new DoubleArrayArray.Array( data, data[0].length ) );
//		setVertexAttribute( Attribute.COLORS,
//				StorageModel.DOUBLE_ARRAY.array(data[0].length).createReadOnly(data));}
	}
	
	protected void setVertexTextureCoordinates( DataList data ) {
		setVertexAttribute( Attribute.TEXTURE_COORDINATES, data );
	}
	
	protected void setVertexTextureCoordinates( double [] data ) {
		if( data.length % nov() != 0 )
			throw new IllegalArgumentException( "array has wrong length" );
		setVertexAttribute( Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Inlined( data, data.length / nov() ) );
	}
	
	protected void setVertexTextureCoordinates( double [][] data ) {
		setVertexAttribute( Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array( data, data[0].length ) );
	}

	void recompute() {
		
	}

	public void update() {
	
		recompute();
	
		Scene.executeWriter( ps, new Runnable() {
			
			public void run() {
				
				updateImpl();      
			}
		}
		);
		
		timeStampOfLastRefactor = System.currentTimeMillis();
	}

	void updateImpl() {
		
		GeometryUtility.setSignature(ps, getSignature());
		
		if( ps.getNumPoints() == nov() ) {
			
			for( Iterator iter = vertexDLS.storedAttributes().iterator(); iter.hasNext(); ) {
				Attribute attr = (Attribute)iter.next();
				
				vertexAttributeNode( attr ).update();
				
				if(  nodeWasUpdated(vertexAttributeNode( attr ))  ) {
					log( "set", attr, "vertex");
					ps.setVertexAttributes( attr, vertexDLS.getWritableList(attr));
				}
			}
		} else {
			updateVertexAttributes();
			ps.setVertexCountAndAttributes(vertexDLS);		
		}
	}
	
	public PointSet getPointSet() {
		return ps;
	}
	
	public int getSignature() {
		return ((Integer)signature.getObject()).intValue();
	}
	
	public void setSignature( int signature ) {
		this.signature.setObject( new Integer( signature) );
	}
	
	static Logger actionLogger = null;
	
	String logMessage( String action, Attribute attr, String cathegory ) {
		return action + " " + cathegory + " " + attr;
	}

	void log( String action, Attribute attr, String cathegory ) {
		if( actionLogger != null ) {
			actionLogger.log( Level.INFO, logMessage(action, attr, cathegory),
					new Object[] {action, attr, cathegory } );
		}
	}
}
