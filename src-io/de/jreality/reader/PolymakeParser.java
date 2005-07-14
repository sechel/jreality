/*
 * Created on 24.09.2004
 *
 * This file is part of the de.jreality.reader package.
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
package de.jreality.reader;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;

/**
 * Simple parser for polymake files
 * 
 * TODO: make this implement SceneReader + rename to ReaderPOLY

 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 * @deprecated use ReaderPOLY instead.
 */
public class PolymakeParser {

/**
 * @param string
 * @return
 */
public static SceneGraphComponent readFromFile(String string) {
    SceneGraphComponent result = null;
    try {
        FileInputStream is = new FileInputStream( string );
        result = ReaderPOLY.parse(is);
         
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    return result;
}

public static SceneGraphComponent readFromFile(File file)	{
    SceneGraphComponent result = null;
    try {
        FileInputStream is = new FileInputStream( file );
        result = ReaderPOLY.parse(is);
         
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    return result;
	
}
}
