
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class IndexedFaceSet extends de.jreality.scene.IndexedFaceSet implements RemoteIndexedFaceSet {
	
	public void setAndCheckFaceCountAndAttributes(Attribute attr, DataList dl) throws RemoteException {
		if (getNumFaces() == dl.size()) setFaceAttributes(attr, dl);
		else setFaceCountAndAttributes(attr, dl);
	}
	public void setAndCheckFaceCountAndAttributes(DataListSet dls) throws RemoteException {
		if (getNumFaces() == dls.getListLength()) setFaceAttributes(dls);
		else setFaceCountAndAttributes(dls);
	}
	public void setAndCheckEdgeCountAndAttributes(Attribute attr, DataList dl) throws RemoteException {
		if (getNumEdges() == dl.size()) setEdgeAttributes(attr, dl);
		else setEdgeCountAndAttributes(attr, dl);
	}
	public void setAndCheckEdgeCountAndAttributes(DataListSet dls) throws RemoteException {
		if (getNumEdges() == dls.getListLength()) setEdgeAttributes(dls);
		else setEdgeCountAndAttributes(dls);
	}
	public void setAndCheckVertexCountAndAttributes(Attribute attr, DataList dl) throws RemoteException {
		if (getNumPoints() == dl.size()) setVertexAttributes(attr, dl);
		else setVertexCountAndAttributes(attr, dl);
	}
	public void setAndCheckVertexCountAndAttributes(DataListSet dls) throws RemoteException {
		if (getNumPoints() == dls.getListLength()) setVertexAttributes(dls);
		else setVertexCountAndAttributes(dls);
	}

}
