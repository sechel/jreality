
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class PointSet extends de.jreality.scene.PointSet implements RemotePointSet {

	public void setAndCheckVertexCountAndAttributes(Attribute attr, DataList dl) throws RemoteException {
		if (getNumPoints() == dl.size()) setVertexAttributes(attr, dl);
		else setVertexCountAndAttributes(attr, dl);
	}

	public void setAndCheckVertexCountAndAttributes(DataListSet dls) throws RemoteException {
		if (getNumPoints() == dls.getListLength()) setVertexAttributes(dls);
		else setVertexCountAndAttributes(dls);
	}
	
}
