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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
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

	MulticastSocket socket;
	InetAddress group;
	private int DEST_PORT = 5555;
	private boolean debug = true;
	private int DATAGRAM_LENGTH = 8188;
	private boolean running = true;
	
	private List values = new LinkedList();
	
	public ParameterReader() {
		try {
			socket = new MulticastSocket(DEST_PORT);
			group = InetAddress.getByName("229.1.1.1");
			socket.joinGroup(group);
		}
		catch (IOException ioe) { }
	}
	
	public void run() {
		while (running = true) {
			try {
				// read number of parameters (Integer)
				int paramCount = readInt();
				System.out.println("Reading "+paramCount+" parameters:");
				for (int i = 0; i < paramCount; i++) {
					Object o = readParameterSafe();
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
		byte[] bytes = new byte[readInt()];
		int fullPieces = bytes.length/DATAGRAM_LENGTH;
	  	int lastPieceSize = bytes.length%DATAGRAM_LENGTH;
	  	if (debug) System.out.println("Reading byte array ["+bytes.length+"] in "+(fullPieces+1)+" pieces. Last Piece size is "+lastPieceSize);
	  	for (int i = 0; i < fullPieces; i++) { // read full datagrams
		  	dgram.setData(bytes, i*DATAGRAM_LENGTH, DATAGRAM_LENGTH); // multicast
		  	socket.receive(dgram);
		  	System.out.println("\t\treceived "+i+". part");
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
		byte[] bytes = new byte[readInt()];
		int fullPieces = bytes.length/DATAGRAM_LENGTH;
	  	int lastPieceSize = bytes.length%DATAGRAM_LENGTH;
	  	if (debug) System.out.println("Reading byte array ["+bytes.length+"] in "+(fullPieces+1)+" pieces. Last Piece size is "+lastPieceSize);
  		byte[] indexedBytes = new byte[DATAGRAM_LENGTH+4];
	  	for (int i = 0; i < fullPieces && !suddenEnd; i++) { // read full datagrams
	  		dgram.setData(indexedBytes); // multicast
		  	socket.receive(dgram);
		  	int id = getIntFromByte(indexedBytes);
		  	if (id != i) {
		  		System.out.println("Missed datagram "+i+" got: "+indexedBytes[0]);
		  		i++;
		  	}
		  	if (id == fullPieces) {
		  		System.out.println("Unexpected end of data!");
		  		suddenEnd = true;
		  		continue;
		  	}
		  	System.arraycopy(indexedBytes, 4, bytes, i*DATAGRAM_LENGTH, DATAGRAM_LENGTH);
		  	System.out.println("\t\treceived "+i+". part");
	  	}
	  	// read last datagram
	  	if (!suddenEnd) {
	  		dgram.setData(indexedBytes, 0, lastPieceSize+4); // multicast
	  		socket.receive(dgram);
		  	//dumpArray(indexedBytes, lastPieceSize+1);
	  		if (getIntFromByte(indexedBytes) != fullPieces) System.out.println("missed last package!");
		  	else System.out.println("\treceived last ("+fullPieces+") part");
	  	}
	  	System.arraycopy(indexedBytes, 4, bytes, fullPieces*DATAGRAM_LENGTH, lastPieceSize);
		ByteArrayInputStream obj_in = new ByteArrayInputStream(bytes);
		ObjectInputStream o_in = new ObjectInputStream(obj_in);
		try {
			return o_in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	byte[] intBytes = new byte[4]; 
	ByteArrayInputStream int_in = new ByteArrayInputStream(intBytes);
	DatagramPacket intDgram = new DatagramPacket(intBytes, 4);
	
	private int readInt() throws IOException {
		int_in.reset();
		socket.receive(intDgram); // blocks
		return getIntFromByte(intBytes);
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
	
	private static int getIntFromByte(byte[] bytes) {
		return getIntFromByte(bytes, 0);
	}
	private static int getIntFromByte(byte[] bytes, int pos) {
		int returnNumber = 0;
	    returnNumber += byteToInt(bytes[pos++]) << 24;
	    returnNumber += byteToInt(bytes[pos++]) << 16;
	    returnNumber += byteToInt(bytes[pos++]) << 8;
	    returnNumber += byteToInt(bytes[pos++]) << 0;
	    return returnNumber;
	}
  
	private static int byteToInt(byte b) {
		return (int) b & 0xFF;
	}


}
