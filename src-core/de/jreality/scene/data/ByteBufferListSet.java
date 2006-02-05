/*
 * Created on 20-Feb-2005
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


/**
 *
 * DataListSet for converting an existing set into a set consisting of
 * ByteBufferList replacements if possible. As we cannot convert dynamic size
 * Arrays to ByteBufferLists, it should be possible, to simply add a readonly list.
 *
 * @author weissman
 *
 */
public class ByteBufferListSet extends DataListSet {

    public ByteBufferListSet(int numDataItems) {
        super(numDataItems);
    }

    public void addReadOnlyList(Attribute a, DataList list) {
        attr2attrDataList.put(a, list);
    }
}
