/*
 * Created on 03.01.2005
 *
 * This file is part of the de.jreality.renderman package.
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
package de.jreality.renderman;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class SLShader {
final String name;
HashMap parameters = new HashMap();
public SLShader(String name) {
    this.name  = name;
}
/**
 * @return Returns the name.
 */
public String getName() {
    return name;
}
/**
 * @return Returns the parameters.
 */
public Map getParameters() {
    return parameters;
}
public void addParameter(String name, Object value) {
    parameters.put(name, value);
}
public String toString() {
    return name;
}
}
