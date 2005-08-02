/*
 * Created on 01.05.2004
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
package de.jreality.soft;

import java.io.*;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;

/**
 * This is an experimental PS viewer for jReality.
 * It is still verry rudimentary and rather a 
 * proof of concept thatn a full featured PS writer.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class PSViewer extends AbstractViewer implements Viewer {

    private String fileName;

    /**
     * 
     */
    public PSViewer(String file) {
        super();
        fileName =file;
    }


    public void render(int width, int height) {
        File f=new File(fileName);
        PrintWriter w;
        try {
            w = new PrintWriter(new FileWriter(f));
            rasterizer =new PSRasterizer(w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.render(width, height);
        w.close();
    }


    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#getSignature()
     */
    public int getSignature() {
        // TODO Auto-generated method stub
        return 0;
    }


    /* (non-Javadoc)
     * @see de.jreality.scene.Viewer#setSignature(int)
     */
    public void setSignature(int sig) {
        // TODO Auto-generated method stub
        
    }


	public SceneGraphComponent getAuxiliaryRoot() {
		// TODO Auto-generated method stub
		return null;
	}


	public void setAuxiliaryRoot(SceneGraphComponent ar) {
		// TODO Auto-generated method stub
		
	}    
    

}
