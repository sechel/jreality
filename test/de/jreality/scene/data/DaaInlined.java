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
public final class DaaInlined extends Daa{

    double[] data;
    int[] offsets;

    static ByteBuffer dataBuf_out;
    static ByteBuffer dataBuf_in;
    static DoubleBuffer dataSER;
    static ByteBuffer offsetsBuf_out;
    static ByteBuffer offsetsBuf_in;
    static IntBuffer offsetsSER;

    private static Object[] decompose(final double[][] initialData) {
        Object[] ret = new Object[2];
        final int length = initialData.length;
        final int[] offsets = new int[length+1]; 
        int dataLength=0;
        for (int i = 0; i < length-1; i++) {
            dataLength+=initialData[i].length;
            offsets[i+1]=dataLength;
        }
        dataLength+=initialData[length-1].length;
        offsets[length]=dataLength;
        final double[] dataVals=new double[dataLength];
        for (int i=0; i < length; i++)
            for (int j = 0; j < initialData[i].length; j++)
                dataVals[offsets[i]+j]=initialData[i][j];
        ret[1] = offsets; ret[0]=dataVals;
        return ret;
    }

    public DaaInlined(double[][] data) {
        this(decompose(data));
    }
    private DaaInlined(Object[] d) {
        this((double[])d[0], (int[])d[1]);
    }
    public DaaInlined(double[] data, int[] offsets) {
        super(offsets.length-1);
        this.data=data;
        this.offsets=offsets;
    }
    public int getLengthAt(int n)
    {
      return offsets[n+1]-offsets[n];
    }
    public double getValueAt(int n, int i)
    {
      return data[getIndex(n, i)];
    }

    private int getIndex(int n, int i) {
        return offsets[n]+i;
    }

    protected void setValueAt(int n, int j, double d) {
        data[getIndex(n, j)] = d;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        final int lengthOffset = offsets.length*4;
        if (offsetsBuf_out == null || offsetsBuf_out.capacity() < lengthOffset) {
            offsetsBuf_out = ByteBuffer.allocateDirect(lengthOffset).order(ByteOrder.nativeOrder());
        }
        offsetsBuf_out.asIntBuffer().put(offsets);
        out.writeInt(lengthOffset);
        Channels.newChannel(out).write(offsetsBuf_out);
        offsetsBuf_out.clear();
        
        final int lengthData = data.length*8;
        if (dataBuf_out == null || dataBuf_out.capacity() < lengthData) {
            dataBuf_out = ByteBuffer.allocateDirect(lengthData).order(ByteOrder.nativeOrder());
        }
        dataBuf_out.asDoubleBuffer().put(data);
        out.writeInt(lengthData);
        Channels.newChannel(out).write(dataBuf_out);
        dataBuf_out.clear();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int offsetsLength = in.readInt();
        if (offsetsBuf_in == null || offsetsBuf_in.capacity() < offsetsLength)
            offsetsBuf_in = ByteBuffer.allocateDirect(offsetsLength).order(ByteOrder.nativeOrder());
        Channels.newChannel(in).read(offsetsBuf_in);
        offsetsBuf_in.flip();
        offsetsBuf_in.asIntBuffer().get(offsets=new int[offsetsLength/4]);
        
        final int dataLength = in.readInt();
        if (dataBuf_in == null || dataBuf_in.capacity() < dataLength)
            dataBuf_in = ByteBuffer.allocateDirect(dataLength).order(ByteOrder.nativeOrder());
        Channels.newChannel(in).read(dataBuf_in);
        dataBuf_in.flip();
        dataBuf_in.asDoubleBuffer().get(data=new double[dataLength/8]);
    }
}
