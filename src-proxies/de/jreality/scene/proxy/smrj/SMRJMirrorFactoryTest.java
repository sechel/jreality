/*
 * Created on 20-Dec-2004
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
package de.jreality.scene.proxy.smrj;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.smrj.Receiver;
import de.smrj.RemoteFactory;
import de.smrj.RemoteKey;
import de.smrj.tcp.TCPBroadcasterIO;
import de.smrj.tcp.TCPReceiverIO;
import de.smrj.tcp.TCPReceiverNIO;

/**
 * 
 * TODO: comment this
 * 
 * @author weissman
 *  
 */
public class SMRJMirrorFactoryTest extends TestCase {

    private final static String HOST;
    private final static RemoteFactory rf;
    private final static int localClients = 1;
    private final static Receiver[] rec = new Receiver[localClients*2]; 

    static {
        Logger.getLogger("SMRJ").setLevel(Level.ALL);
        try {
            HOST = InetAddress.getLocalHost().getHostName();
            rf = new TCPBroadcasterIO(8868).getRemoteFactory();
            for (int i = 0; i < localClients; i++) {
                rec[2*i] = new TCPReceiverIO(HOST, 8868);
                rec[2*i].start();
                rec[2*i+1] = new TCPReceiverNIO(HOST, 8868);
                rec[2*i+1].start();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } catch (UnknownHostException e) {
            throw new ExceptionInInitializerError(e);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.run(SMRJMirrorFactoryTest.class);
    }

    SMRJMirrorScene proxyScene;
    
    CatenoidHelicoid ch;
    private boolean finished;

    /*
     * Class under test for void visit(de.jreality.scene.IndexedFaceSet)
     */
    public void testVisitIndexedFaceSet() throws IOException {
        proxyScene = new SMRJMirrorScene(rf);
        ch = new CatenoidHelicoid(2);
        ch.buildEdgesFromFaces();
        long l = System.currentTimeMillis();
        Object proxy = proxyScene.createProxyScene(ch);
        System.out.println(proxy);
        RemoteKey key = rf.getProxyKey(proxy);
        System.out
                .println("visit: " + (System.currentTimeMillis() - l) + " ms");
        IndexedFaceSet copy = (IndexedFaceSet) rec[0].getClientFactory().getLocal(key);
        System.out.println("copy: "+copy);
        assertEquals(ch.getVertexAttributes(), copy.getVertexAttributes());
        assertEquals(ch.getEdgeAttributes(), copy.getEdgeAttributes());
        assertEquals(ch.getFaceAttributes(), copy.getFaceAttributes());
    }

    public static void assertEquals(DataListSet arg0, DataListSet arg1) {
        assertEquals(arg0.storedAttributes(), arg1.storedAttributes());
        for (Iterator i = arg0.storedAttributes().iterator(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            assertEquals(arg0.getList(a), arg1.getList(a));
        }

    }

    public static void assertEquals(DataList arg0, DataList arg1) {
        assertEquals(arg0.getStorageModel(), arg1.getStorageModel());
        Object[] c0 = arg0.toArray();
        Object[] c1 = arg1.toArray();
        System.out.println("dl length = " + c0.length);
        for (int i = 0; i < c0.length; i++)
            assertEquals(c0[i], c1[i]);
    }
}
