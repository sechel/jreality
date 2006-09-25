package de.jreality.geometry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.WritableDataList;

public class GeometryAttributeListSet {

	final AbstractGeometryFactory factory;

	final DataListSet DLS = new DataListSet(0);

	final HashMap attributeNode = new HashMap();
	
	final HashSet blockAttribute = new HashSet();
	
	final String cathegory;

	GeometryAttributeListSet( AbstractGeometryFactory factory, String cathegory) {
		this.factory = factory;
		this.cathegory = cathegory;
	}

	int noa() {
		return DLS.getListLength();
	}

	int getCount() {
		return noa();
	}

	void setCount(int count) {
		if (count == noa())
			return;

		DLS.reset(count);
	}

	OoNode geometryAttributeNode(Map attributeNode, Attribute attr) {
		if (attributeNode.containsKey(attr))
			return (OoNode) attributeNode.get(attr);

		OoNode node = factory.node(cathegory + "." + attr);

		attributeNode.put(attr, node);

		return node;
	}

	OoNode attributeNode(Attribute attr) {
		return this.geometryAttributeNode(attributeNode, attr);
	}

	void updateAttributes() {
		for (Iterator iter = DLS.storedAttributes().iterator(); iter.hasNext();) {
			Attribute attr = (Attribute) iter.next();
			attributeNode(attr).update();
		}
	}

	void setAttribute(Attribute attr, DataList data) {
		if( blockAttribute.contains(attr))
			throw new UnsupportedOperationException( "cannot set attribute " + attr );
		setAttrImpl(DLS, attr, data);
		attributeNode(attr).setObject(data);
	}

	void setAttributes(DataListSet dls) {
		for( Iterator iter = dls.storedAttributes().iterator(); iter.hasNext(); ) {
			Attribute attr = (Attribute)iter.next();
			if( blockAttribute.contains(attr))
				throw new UnsupportedOperationException( "cannot set attribute " + attr );
		}
		setAttrImpl(DLS, dls);
		for (Iterator iter = dls.storedAttributes().iterator(); iter.hasNext();) {
			Attribute attr = (Attribute) iter.next();
			attributeNode(attr).setObject(DLS.getList(attr));
		}
	}

	final void setAttrImpl(DataListSet target, DataListSet data) {
		for (Iterator i = data.storedAttributes().iterator(); i.hasNext();) {
			Attribute a = (Attribute) i.next();
			setAttrImpl(target, a, data.getList(a));
		}
	}

	static final void setAttrImpl(DataListSet target, Attribute a, DataList d) {

		if (d == null) {
			target.remove(a);
		} else {
			WritableDataList w;
			w = target.getWritableList(a);
			if (w == null)
				w = target.addWritable(a, d.getStorageModel());
			d.copyTo(w);
		}
	}

}

