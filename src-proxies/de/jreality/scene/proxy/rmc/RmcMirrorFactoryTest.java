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
package de.jreality.scene.proxy.rmc;

import java.util.Iterator;

import javax.print.attribute.standard.Finishings;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import junit.framework.TestCase;

/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class RmcMirrorFactoryTest extends TestCase {

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(RmcMirrorFactoryTest.class);
	}

	RmcMirrorFactory f;CatenoidHelicoid ch;
	private boolean finished;
	/*
	 * Class under test for void visit(de.jreality.scene.IndexedFaceSet)
	 */
	public void testVisitIndexedFaceSet() {
		f = new RmcMirrorFactory();
		RmcMirrorFactoryClient c1 = new RmcMirrorFactoryClient();
		ch = new CatenoidHelicoid(40);
		int id = System.identityHashCode(ch);
		for (int i = 0; i < 1; i++) { 
			new Thread(new Runnable() {
				public void run() {
					long l = System.currentTimeMillis();
					f.visit(ch);
					finished = true;
					System.out.println("visit: "+(System.currentTimeMillis()-l)+" ms");
				}
			}).start();
		}
		IndexedFaceSet copy = (IndexedFaceSet) RmcMirrorFactoryClient.getLocal(id);
		while (copy == null || !finished) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			copy = (IndexedFaceSet) RmcMirrorFactoryClient.getLocal(id);
		}
		assertEquals(ch.getVertexAttributes(), copy.getVertexAttributes());
		assertEquals(ch.getEdgeAttributes(), copy.getEdgeAttributes());
		assertEquals(ch.getFaceAttributes(), copy.getFaceAttributes());
	}

	public static void assertEquals(DataListSet arg0, DataListSet arg1) {
		assertEquals(arg0.storedAttributes(), arg1.storedAttributes());
		for (Iterator i = arg0.storedAttributes().iterator(); i.hasNext(); ) {
			Attribute a = (Attribute) i.next();
			assertEquals(arg0.getList(a), arg1.getList(a));
		}
			
	}

	public static void assertEquals(DataList arg0, DataList arg1) {
		assertEquals(arg0.getStorageModel(), arg1.getStorageModel());
		Object[] c0 = arg0.toArray();
		Object[] c1 = arg1.toArray();
		System.out.println("dl length = "+c0.length);
		for (int i = 0; i < c0.length; i++) assertEquals(c0[i], c1[i]);
	}
}
