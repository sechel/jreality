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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;

/**
 *
 * This class broadcasts Serializable Parameters as UDP datagrams.
 * The Protocoll is:
 * 
 * 1. Integer containing number of parameters sent
 * 2.1. Integer byte length of current parameter
 * 2.2. byte array(s) of the serialized parameter
 *
 * @author weissman
 *
 */
public class ParameterBroadcaster {
	
	DatagramSocket socket;
	InetAddress group;
	private int DEST_PORT = 5555;
	private boolean debug = true;
	private int DATAGRAM_LENGTH = 8188;
	
	public ParameterBroadcaster() {
		try {
			socket = new DatagramSocket(); //5556);
			group = InetAddress.getByName("192.168.178.255");
			//socket.joinGroup(group);
//			socket.setTimeToLive(255);
		}
		catch (IOException ioe) { }
	}
	
	public void sendParameters(Serializable[] data) throws IOException {
		sendInt(data.length);
		for (int i = 0; i < data.length; i++) {
			send(data[i]);
		}
	}
	
	
	ByteArrayOutputStream b_out = new ByteArrayOutputStream();
	
	private void send(Serializable s) throws IOException {
		System.out.println("Sending "+(s == null ? "<null>" : (s.getClass().getName() + "  "/*+s*/)));
		ObjectOutputStream o_out = new ObjectOutputStream(b_out);
		o_out.writeObject(s);
		byte[] bytes = b_out.toByteArray();
		if (debug) System.out.print("\t Sending data size ["+bytes.length+"] ");
		sendInt(bytes.length);
		if (debug) System.out.print("\t Sending Data: ");
		sendSafe(bytes);
		b_out.reset();
	}

	ByteArrayOutputStream int_out = new ByteArrayOutputStream();

	private void sendInt(int i) throws IOException {
		DataOutputStream o_out = new DataOutputStream(int_out);
		o_out.writeInt(i);
		send(int_out.toByteArray());
		int_out.reset();
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private static byte[] int2byte(int value) {
		byte b[] = new byte[4];
		int i, shift; 
		for(i = 0, shift = 24; i < 4; i++, shift -= 8)
		b[i] = (byte)(0xFF & (value >> shift));
		return b;
	}
	
	private void send(byte[] bytes) throws IOException {
		int fullPieces = bytes.length/DATAGRAM_LENGTH;
	  	int lastPieceSize = bytes.length%DATAGRAM_LENGTH;
	  	if (debug) System.out.println("byte array ["+bytes.length+"] in "+(fullPieces+1)+" pieces. Last Piece size is "+lastPieceSize);
	  	for (int i = 0; i < fullPieces; i++) { // send full datagrams
		  	DatagramPacket dgram = new DatagramPacket(bytes, i*DATAGRAM_LENGTH, DATAGRAM_LENGTH, group, DEST_PORT); // multicast
		  	socket.send(dgram);
//		  	try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {}
//		  	System.out.println("\t\tsent "+i+". part");
	  	}
	  	// send last datagram
	  	DatagramPacket dgram = new DatagramPacket(bytes, fullPieces*DATAGRAM_LENGTH, lastPieceSize, group, DEST_PORT); // multicast
	  	socket.send(dgram);
	  	System.out.println("\t\tsent last part");
	}
	
	private void sendSafe(byte[] bytes) throws IOException {
		int fullPieces = bytes.length/DATAGRAM_LENGTH;
	  	int lastPieceSize = bytes.length%DATAGRAM_LENGTH;
	  	if (debug) System.out.println("byte array ["+bytes.length+"] in "+(fullPieces+1)+" pieces. Last Piece size is "+lastPieceSize);
  		byte[] indexedBytes = new byte[DATAGRAM_LENGTH+4];
	  	for (int i = 0; i < fullPieces; i++) { // send full datagrams
	  		System.arraycopy(int2byte(i), 0, indexedBytes, 0, 4);
	  		System.arraycopy(bytes, i*DATAGRAM_LENGTH, indexedBytes, 4, DATAGRAM_LENGTH);
	  		//dumpArray(indexedBytes);
	  		DatagramPacket dgram = new DatagramPacket(indexedBytes, (DATAGRAM_LENGTH+4), group, DEST_PORT); // multicast
		  	socket.send(dgram);
//		  	try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {}
//		  	System.out.println("\t\tsent "+i+". part");
	  	}
	  	// send last datagram
	  	System.arraycopy(int2byte(fullPieces), 0, indexedBytes, 0, 4);
  		System.arraycopy(bytes, fullPieces*DATAGRAM_LENGTH, indexedBytes, 4, lastPieceSize);
  		//dumpArray(indexedBytes, lastPieceSize+1);
  		DatagramPacket dgram = new DatagramPacket(indexedBytes, (lastPieceSize+4), group, DEST_PORT); // multicast
	  	socket.send(dgram);
	  	System.out.println("\t\tsent last ("+fullPieces+") part");
	}

	public static void main(String[] args) {
		ParameterBroadcaster pb = new ParameterBroadcaster();
		CatenoidHelicoid ch = new CatenoidHelicoid(419);
		Serializable[] s = {/*null, new Transformation(), new double[]{1,2,3,4,5,2,3,4,5,2,3,4,5}, new String("bla"), null, */
				ch.getVertexAttributes(/*Attribute.COORDINATES*/)};
		try {
			pb.sendParameters(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
