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
package de.jreality.scene.data;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class uses a pool of ByteBuffer when being de-serialized. Clients should
 * copy data and release the buffer afterwards.
 * 
 * Be careful: this class is intended to be used as a traffic object for transferring data
 * <br> <b>Dont use for usual data storage! Only 1- and 2-dimensional int and double arrays are supported!</b>
 */
public class ByteBufferList extends DataList implements Serializable
{
  public static final class BufferPool {
    final static LinkedList free = new java.util.LinkedList();
    final static LinkedList used = new java.util.LinkedList();
    
    final static boolean debug = false;
    
    private static void dumpUsage(String str) {
	    if (!debug) return;
        int freeBytes=0;
        for (Iterator i = free.iterator(); i.hasNext(); ) freeBytes+=((ByteBuffer)i.next()).capacity();
        int usedBytes=0;
        for (Iterator i = used.iterator(); i.hasNext(); ) usedBytes+=((ByteBuffer)i.next()).capacity();
        System.out.println("\n"+str);
        System.out.println("/tfree Lists: "+free.size()+" bytes: "+freeBytes);
        System.out.println("/tused Lists: "+used.size()+" bytes: "+usedBytes);
    }
    
    public static ByteBuffer getBuffer(int length) {
        for (Iterator i = free.iterator(); i.hasNext(); ) {
        final ByteBuffer bb = (ByteBuffer) i.next();
        if (bb.capacity() >= length) {
          i.remove();
          used.add(bb);
          return (ByteBuffer)bb.position(0).limit(length);
        }
      }
       ByteBuffer bb = ByteBuffer.allocateDirect(length);
       used.add(bb);
       dumpUsage("getBuffer");
       return bb;
    }
    
    public static void release(ByteBuffer bb) {
      bb.clear();
      used.remove(bb);
      free.add(bb);
      dumpUsage("release");
    }
    
    public static void releaseAll() {
      for (Iterator i = used.iterator(); i.hasNext(); i.remove()) {
          ByteBuffer bb = (ByteBuffer) i.next();
          bb.clear();
          free.add(bb);
      }
      dumpUsage("releaseAll");
    }
  }
  
  ByteBufferList(ByteBuffer bb, int offset, int length)
  {
    super(ByteBufferStorage.MODEL, bb, offset, length);
  }
  
  StorageModel coveredModel;
  int coveredLength;
  int entryLength = -1;
  int int_not_double = -1; 

  public int getCoveredLength() {
      return coveredLength;
  }
  public void setCoveredLength(int coveredLength) {
      this.coveredLength = coveredLength;
  }
  public StorageModel getCoveredModel() {
      return coveredModel;
  }
  public void setCoveredModel(StorageModel coveredModel) {
      this.coveredModel = coveredModel;
  }

  public static LinkedList usedDataLists = new LinkedList();
  
  public static void freeDataList(ByteBufferList wdl) {
      BufferPool.release((ByteBuffer) wdl.data);
  }
  
  public static ByteBufferList createByteBufferCopy(DataList dl) {
      int len = ByteBufferStorage.numBytes(dl.getStorageModel(), dl.data, dl.length);
      ByteBuffer bb = BufferPool.getBuffer(len);
      final ByteBufferList wdl = new ByteBufferList(bb, 0, len);
      ByteBufferStorage.MODEL.copy(dl.format, dl.data, dl.offset, bb, 0, dl.length);
      wdl.setCoveredLength(dl.length);
      wdl.setCoveredModel(dl.getStorageModel());
      if (dl instanceof IntArrayArray) {
          wdl.setEntryLength(((IntArrayArray)dl).getLengthAt(0));          
      }
      if (dl instanceof DoubleArrayArray) {
          wdl.setEntryLength(((DoubleArrayArray)dl).getLengthAt(0));          
      }
      usedDataLists.add(wdl);
      return wdl;
  }
  
  public String toString() {
      return "ByteBufferList: "+data+ " covered:" +getCoveredModel()+ " length="+getCoveredLength()+ " entrySize="+getEntryLength();
  }
  
    public int getEntryLength() {
        return entryLength;
    }
    public void setEntryLength(int entryLength) {
        this.entryLength = entryLength;
    }
    
    public Object createFittingDataObject() {
        if (getCoveredModel() == StorageModel.DOUBLE_ARRAY) return new double[getCoveredLength()];
        if (getCoveredModel() == StorageModel.INT_ARRAY) return new int[getCoveredLength()];
        if (getCoveredModel() instanceof StorageModel.DAA) return new double[getCoveredLength()][getEntryLength()];
        if (getCoveredModel() instanceof StorageModel.DAI) return new double[getCoveredLength()*getEntryLength()];
        if (getCoveredModel() instanceof StorageModel.IAA) return new int[getCoveredLength()][getEntryLength()];
        if (getCoveredModel() instanceof StorageModel.IAI) return new int[getCoveredLength()*getEntryLength()];
        throw new IllegalStateException("unsupported storage model: "+getCoveredModel());
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        //System.out.println("WRITING BYTEBUFFERLIST!");
        out.writeObject(getCoveredModel());
        out.writeInt(getCoveredLength());
        out.writeInt(getEntryLength());
    }
    private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
        //System.out.println("READING BYTEBUFFERLIST!");
        Object obj=((IdentityHashMap)pendingIO.get()).get(this);
        ByteBufferList dl=(ByteBufferList)obj;
        dl.setCoveredModel((StorageModel) in.readObject());
        dl.setCoveredLength(in.readInt());
        dl.setEntryLength(in.readInt());
    }

    public static DataListSet prepareDataListSet(DataListSet attributes) {
        ByteBufferListSet ret = new ByteBufferListSet(attributes.getListLength());
        for (Iterator i = attributes.storedAttributes().iterator(); i.hasNext(); ) {
            Attribute a = (Attribute) i.next();
            DataList dl = attributes.getList(a);
            if (dl.getStorageModel() != ByteBufferStorage.MODEL && canCopy(dl))
            	dl=ByteBufferList.createByteBufferCopy(dl);
          	ret.addReadOnlyList(a, dl);
        }
        return ret;
    }

    public static void releaseDataListSet(DataListSet attributes) {
        for (Iterator i = attributes.storedAttributes().iterator(); i.hasNext(); ) {
            DataList dl = attributes.getList((Attribute) i.next());
            if (dl instanceof ByteBufferList) ByteBufferList.freeDataList((ByteBufferList) dl);
        }
    }
    
	//TODO: make this better!!
	public static boolean canCopy(DataList dl) {
		try {
			ByteBufferStorage.numBytes(dl.getStorageModel(), dl.data, dl.length);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * @param copy
	 */
	public static void releaseList(ByteBufferList copy) {
		BufferPool.release((ByteBuffer) copy.data);
	}

}
