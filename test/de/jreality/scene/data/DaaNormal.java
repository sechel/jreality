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

import java.io.*;
import java.nio.*;

public final class DaaNormal extends Daa {

    final double[][] data;
    
    public DaaNormal(double[][] data) {
        super(data.length);
        this.data=new double[getLength()][];
        for (int i = 0; i < getLength(); i++) {
            final double[] slot = this.data[i] = new double[data[i].length];
            for (int j=0; j<data[i].length; j++) slot[j] = data[i][j];
        }
    }
    public int getLengthAt(int n)
    {
      return data[n].length;
    }
    public double getValueAt(int n, int i)
    {
      return data[n][i];
    }
    protected void setValueAt(int n, int j, double d) {
        data[n][j]=d;
    }

//    static ByteBuffer outOffsetBuf;
//    static ByteBuffer outDataBuf;
//    static ByteBuffer inOffsetBuf;
//    static ByteBuffer inDataBuf;
    
//    private void writeObject(ObjectOutputStream out) throws IOException {
//        final int length = data.length;
//        if (outOffsetBuf ==null || outOffsetBuf.remaining() < ((length+1)*4)) outOffsetBuf = ByteBuffer.allocateDirect((length+1)*4).order(ByteOrder.nativeOrder());
//        final IntBuffer offsets = outOffsetBuf.asIntBuffer();
//        int dataLength=0;
//        for (int i = 0; i < length-1; i++) {
//            dataLength+=data[i].length;
//            offsets.put(i+1, dataLength);
//        }
//        dataLength+=data[length-1].length;
//        offsets.put(length, dataLength);
//        
//        out.writeInt(length);
//        Channels.newChannel(out).write(outOffsetBuf);
//        
//        if (outDataBuf ==null || outDataBuf.remaining() < dataLength*8) outDataBuf = ByteBuffer.allocateDirect(dataLength*8).order(ByteOrder.nativeOrder());
//        final DoubleBuffer dataVals=outDataBuf.asDoubleBuffer();
//        for (int i=0; i < length; i++)
//            for (int j = 0; j < data[i].length; j++)
//                dataVals.put(offsets.get(i)+j, data[i][j]);
//        
//        out.writeInt(dataLength*8);
//        Channels.newChannel(out).write(outDataBuf);
//    }
//    private void readObject(ObjectInputStream in) throws IOException, ClassCastException {
//        final int length = in.readInt();
//        if (inOffsetBuf ==null || inOffsetBuf.remaining() < ((length+1)*4)) inOffsetBuf = ByteBuffer.allocateDirect((length+1)*4).order(ByteOrder.nativeOrder());
//        inOffsetBuf.position(0).limit((length+1)*4);
//        Channels.newChannel(in).read(inOffsetBuf);
//        inOffsetBuf.flip();
//        final IntBuffer offsets = inOffsetBuf.asIntBuffer();
//        for (int i=0; i < length; i++) data[i]=new double[offsets.get(i+1)-offsets.get(i)];
//        final int dataLength = in.readInt();
//        if (inDataBuf ==null || inDataBuf.remaining() < dataLength*8) inDataBuf = ByteBuffer.allocateDirect(dataLength*8).order(ByteOrder.nativeOrder());
//        inDataBuf.position(0).limit(dataLength*8);
//        Channels.newChannel(in).read(inDataBuf);
//        inDataBuf.flip();
//        final DoubleBuffer dataVals = inDataBuf.asDoubleBuffer();
//        for (int i=0; i < data.length; i++)
//            for (int j=0; j<data[i].length; j++) data[i][j]=dataVals.get();
//    }
}
