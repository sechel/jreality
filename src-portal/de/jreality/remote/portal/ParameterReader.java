/*
 * Created on 18-Dec-2004
 *
 * This file is part of the jReality_new package.
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
package de.jreality.remote.portal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class ParameterReader implements Runnable {

	DatagramSocket socket;
	InetAddress group;
	private int DEST_PORT = 5555;
	private boolean debug = true;
	private int DATAGRAM_LENGTH = 65506;
	private boolean running = true;
	
	private List values = new LinkedList();
	
	public ParameterReader() {
		try {
			socket = new DatagramSocket(DEST_PORT);
			//group = InetAddress.getByName("192.168.178.255");
			//socket.joinGroup(group);
			//socket.setTimeToLive(255);
		}
		catch (IOException ioe) { }
	}
	
	public void run() {
		while (running = true) {
			try {
				// read number of parameters (Integer)
				int paramCount = readLong().intValue();
				System.out.println("Reading "+paramCount+" parameters:");
				for (int i = 0; i < paramCount; i++) {
					Object o = readParameter();
					System.out.println("Read "+(o == null ? "<null>" : (o.getClass().getName() + "  "/*+o*/)));
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			System.out.println("Reading done!!\n\n");
		}
	}

	DatagramPacket dgram = new DatagramPacket(new byte[1], 0);
	private Object readParameter() throws IOException {
		byte[] bytes = new byte[readLong().intValue()];
		int fullPieces = bytes.length/DATAGRAM_LENGTH;
	  	int lastPieceSize = bytes.length%DATAGRAM_LENGTH;
	  	if (debug) System.out.println("Reading byte array ["+bytes.length+"] in "+(fullPieces+1)+" pieces. Last Piece size is "+lastPieceSize);
	  	for (int i = 0; i < fullPieces; i++) { // read full datagrams
		  	dgram.setData(bytes, i*DATAGRAM_LENGTH, DATAGRAM_LENGTH); // multicast
		  	socket.receive(dgram);
//		  	System.out.println("\t\treceived "+i+". part");
	  	}
	  	// read last datagram
	  	dgram.setData(bytes, fullPieces*DATAGRAM_LENGTH, lastPieceSize); // multicast
	  	socket.receive(dgram);
	  	System.out.println("\treceived last part");
		ByteArrayInputStream obj_in = new ByteArrayInputStream(bytes);
		ObjectInputStream o_in = new ObjectInputStream(obj_in);
		try {
			return o_in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	boolean suddenEnd;
	private Object readParameterSafe() throws IOException {
		suddenEnd = false;
		byte[] bytes = new byte[readLong().intValue()];
		int fullPieces = bytes.length/DATAGRAM_LENGTH;
	  	int lastPieceSize = bytes.length%DATAGRAM_LENGTH;
	  	if (debug) System.out.println("Reading byte array ["+bytes.length+"] in "+(fullPieces+1)+" pieces. Last Piece size is "+lastPieceSize);
  		byte[] indexedBytes = new byte[DATAGRAM_LENGTH+1];
	  	for (int i = 0; i < fullPieces && !suddenEnd; i++) { // read full datagrams
		  	dgram.setData(indexedBytes); // multicast
		  	socket.receive(dgram);
		  	//dumpArray(indexedBytes);
		  	if (indexedBytes[0] != (i % 10)) {
		  		System.out.println("Missed datagram "+i+" got: "+indexedBytes[0]);
		  		i++;
		  	}
		  	if (indexedBytes[0] == 99) {
		  		System.out.println("Unexpected end of data!");
		  		suddenEnd = true;
		  		continue;
		  	}
		  	System.arraycopy(indexedBytes, 1, bytes, i*DATAGRAM_LENGTH, DATAGRAM_LENGTH);
//		  	System.out.println("\t\treceived "+i+". part");
	  	}
	  	// read last datagram
	  	if (!suddenEnd) {
	  		dgram.setData(indexedBytes, 0, lastPieceSize+1); // multicast
	  		socket.receive(dgram);
		  	//dumpArray(indexedBytes, lastPieceSize+1);
	  		if (indexedBytes[0] != 99) System.out.println("missed last package!");
		  	System.out.println("\treceived last part");
	  	}
	  	System.arraycopy(indexedBytes, 1, bytes, fullPieces*DATAGRAM_LENGTH, lastPieceSize);
		ByteArrayInputStream obj_in = new ByteArrayInputStream(bytes);
		ObjectInputStream o_in = new ObjectInputStream(obj_in);
		try {
			return o_in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	byte[] longBytes = new byte[82]; 
	ByteArrayInputStream long_in = new ByteArrayInputStream(longBytes);
	DatagramPacket longDgram = new DatagramPacket(longBytes, 82);
	private Long readLong() throws IOException {
		socket.receive(longDgram); // blocks
		ObjectInputStream o_in = new ObjectInputStream(long_in);
		Long l = null;
		try {
			l = (Long) o_in.readObject();
		} catch (ClassNotFoundException e) {}
		long_in.reset(); // reset so next read is from start of byte[] again
		System.out.println("Read long "+l);
		return l;
	}
	
	public static void main(String[] args) {
		new Thread(new ParameterReader()).start();
	}

	private void dumpArray(byte[] b) {
		dumpArray(b, b.length);
	}
	private void dumpArray(byte[] b, int max) {
		System.out.print("{ ");
		for (int i = 0; i < max; i++) {
			System.out.print(b[i]+", ");
		}
		System.out.println("}");
	}

}
