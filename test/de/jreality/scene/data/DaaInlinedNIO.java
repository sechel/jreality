/*
 * Created on 16-Jan-2005
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
package de.jreality.scene.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public final class DaaInlinedNIO extends Daa {

    transient ByteBuffer dataBuf;
    transient DoubleBuffer data;
    transient ByteBuffer offsetsBuf;
    transient IntBuffer offsets;

    private static Object[] decompose(final double[][] initialData) {
        Object[] ret = new Object[2];
        final int length = initialData.length;
        final ByteBuffer offsetsBuf = ByteBuffer.allocateDirect((length+1)*4).order(ByteOrder.nativeOrder());
        final IntBuffer offsets = offsetsBuf.asIntBuffer();
        int dataLength=0;
        for (int i = 0; i < length-1; i++) {
            dataLength+=initialData[i].length;
            offsets.put(i+1, dataLength);
        }
        dataLength+=initialData[length-1].length;
        offsets.put(length, dataLength);
        final ByteBuffer dataValsBuf=ByteBuffer.allocateDirect(dataLength*8).order(ByteOrder.nativeOrder());
        final DoubleBuffer dataVals=dataValsBuf.asDoubleBuffer();
        for (int i=0; i < length; i++)
            for (int j = 0; j < initialData[i].length; j++)
                dataVals.put(offsets.get(i)+j, initialData[i][j]);
        ret[1] = offsetsBuf; ret[0]=dataValsBuf;
        return ret;
    }

    public DaaInlinedNIO(double[][] data) {
        this(decompose(data));
    }
    private DaaInlinedNIO(Object[] d) {
        this((ByteBuffer)d[0], (ByteBuffer)d[1]);
    }
    public DaaInlinedNIO(ByteBuffer data, ByteBuffer offsets) {
        super((offsets.limit()/4)-1);
        dataBuf=data;
        this.data = dataBuf.asDoubleBuffer();
        this.offsetsBuf=offsets;
        this.offsets  = offsetsBuf.asIntBuffer();
    }
    public int getLengthAt(int n)
    {
      return offsets.get(n+1)-offsets.get(n);
    }
    public double getValueAt(int n, int i)
    {
      return data.get(getIndex(n, i));
    }
    protected void setValueAt(int n, int j, double d) {
        data.put(getIndex(n, j), d);
    }
    private int getIndex(int n, int i) {
        return offsets.get(n)+i;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(offsetsBuf.limit());
        Channels.newChannel(out).write(offsetsBuf);
        offsetsBuf.clear();
        out.writeInt(dataBuf.limit());
        Channels.newChannel(out).write(dataBuf);
        dataBuf.clear();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int offsetsLength = in.readInt();
        offsetsBuf = ByteBuffer.allocateDirect(offsetsLength).order(ByteOrder.nativeOrder());
        Channels.newChannel(in).read(offsetsBuf);
        offsetsBuf.flip();
        offsets = offsetsBuf.asIntBuffer();
        final int dataLength = in.readInt();
        dataBuf = ByteBuffer.allocateDirect(dataLength).order(ByteOrder.nativeOrder());
        Channels.newChannel(in).read(dataBuf);
        dataBuf.flip();
        data = dataBuf.asDoubleBuffer();
    }

    /* (non-Javadoc)
     * @see de.jreality.scene.data.Daa#toByteBuffer(java.nio.ByteBuffer)
     */
    public void toByteBuffer(ByteBuffer bb) {
        bb.put(dataBuf);
    }
    
}
