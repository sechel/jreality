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

import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Util;

/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class TimedMarshaller implements RpcDispatcher.Marshaller{

	public byte[] objectToByteBuffer(Object arg0) throws Exception {
		long l = System.currentTimeMillis();
		byte[] r = Util.objectToByteBuffer(arg0);
		System.out.println("serialization ["+r.length/1024+" kb] "+(System.currentTimeMillis()-l)+" ms ");
		return r;
	}

	public Object objectFromByteBuffer(byte[] arg0) throws Exception {
		long l = System.currentTimeMillis();
		Object o = Util.objectFromByteBuffer(arg0);
		System.out.println("deserialization took "+(System.currentTimeMillis()-l)+" ms");
		return o;
	}

}
