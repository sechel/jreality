/*
 * Created on 14-Jan-2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.scene.proxy.smrj;

import java.rmi.RemoteException;

import de.jreality.scene.data.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.WritableDataList;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class PointSet extends de.jreality.scene.proxy.rmi.PointSet implements
        RemotePointSet {

    private double[] vertices;
    private double[] vertexNormals;

        public void setVertices(ByteBufferWrapper data, int vertexSize) {
            System.out.println("PointSet.setVertices()");
            if (vertices == null || data.getDoubleLength() != vertices.length) {
                WritableDataList dl = new WritableDataList(StorageModel.DOUBLE_ARRAY.inlined(vertexSize), data);
                vertices = (double[]) dl.getData();
                setVertexCountAndAttributes(Attribute.COORDINATES, dl);
            } else {
                nodeLock.writeLock();
                data.getReadBuffer().asDoubleBuffer().get(vertices);
                nodeLock.writeUnlock();
            }
        }
        
        public void setVertexNormals(ByteBufferWrapper data, int normalSize) {
            System.out.println("PointSet.setVertexNormals()");
            if (vertexNormals == null || data.getDoubleLength() != vertexNormals.length) {
                WritableDataList dl = new WritableDataList(StorageModel.DOUBLE_ARRAY.inlined(normalSize), data);
                vertexNormals = (double[]) dl.getData();
                setVertexCountAndAttributes(Attribute.NORMALS, dl);
            } else {
                nodeLock.writeLock();
                data.getReadBuffer().asDoubleBuffer().get(vertexNormals);
                nodeLock.writeUnlock();
            }
        }
}
