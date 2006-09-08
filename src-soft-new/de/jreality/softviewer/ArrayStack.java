/*
 * Created on 06.09.2006
 *
 * This file is part of the de.jreality.soft package.
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
package de.jreality.softviewer;

import java.util.ArrayList;

public class ArrayStack<E> {
    private E[] data;
    int position = -1;
    private final int increment;
    public ArrayStack(int initialCapacity) {
        super();
        data = (E[])new Object[initialCapacity];
        increment = initialCapacity;
    }
    public boolean isEmpty() {
        return position == -1;
    }
    public int getSize() {
        return position+1;
    }
    public E pop() {
        if(position > -1) { 
            E result =  data[position];
        data[position--] = null;
        return result;
        } else 
            return null;
    }
    public E peek() {
        return data[position];
    }
    public void push(E element) {
        if(++position >=data.length) {
            E[] tmp = (E[])new Object[data.length + increment];
            System.arraycopy(data,0,tmp,0,data.length);
            data = tmp;
        }
        data[position] = element;
            
    }
    public E[] getArray() {
        return data;
    }
}
