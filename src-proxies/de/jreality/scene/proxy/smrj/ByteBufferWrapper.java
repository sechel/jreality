/*
 * Created on 18-Jan-2005
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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class ByteBufferWrapper implements Serializable {


	private static final Object outLock = new Object();
    private static ByteBuffer bufferOUT;
    
	private static final Object inLock = new Object();
    private static ByteBuffer bufferIN;
    private transient int length;
    
    private static ByteBufferWrapper instance = new ByteBufferWrapper();
    
    static ByteBufferWrapper getInstance() {
        return instance;
    }
    
    private ByteBufferWrapper() {}
    
    ByteBuffer createWriteBuffer(int length) {
        this.length = length;
        if (bufferOUT != null) bufferOUT.clear();
        if (bufferOUT == null || bufferOUT.capacity() < length) {
            bufferOUT = ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder());
        }
        bufferOUT.position(0).limit(length);
        return bufferOUT;
    }
    
    ByteBuffer getReadBuffer() {
        return bufferIN.duplicate().asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
    	synchronized(outLock) {
        out.writeInt(length);
        bufferOUT.position(0).limit(length);
        int wrote = 0;
        while ((wrote +=Channels.newChannel(out).write(bufferOUT)) < length) ;
        //if (wrote < length) throw new Error();
    	}
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	synchronized(inLock) {
        length = in.readInt();
        if (bufferIN == null || bufferIN.capacity() < length) {
            bufferIN = ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder());
        }
        bufferIN.position(0).limit(length);
        int read=0;
        while ((read +=Channels.newChannel(in).read(bufferIN))< length) ;
        //if (read < length) throw new Error();
        bufferIN.flip();
    	}
    }
    public int getLength() {
        return length;
    }
    public int getDoubleLength() {
        return length/8;
    }
    public int getIntLength() {
        return length/4;
    }
}
