/*
 * Created on Jul 14, 2004
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

package de.jreality.portal;

import java.rmi.*;

import de.jreality.scene.Transformation;

/**
 * @author weissman
 *
 * Interface for the PORTAL viewers - and other remote viewers
 * 
 * TODO: build in scene graph distribution
 */
public interface JoglRender extends Remote {

	public void init() throws RemoteException;
	public void quit() throws RemoteException;
	public void setHeadPosition(Transformation t) throws RemoteException;
	public void setNavigationTransformation(Transformation t) throws RemoteException;
	public Transformation getWorldTransformation() throws RemoteException;
	public void swapBuffers() throws RemoteException;
	public void render() throws RemoteException;
	public void loadWorld(String classname) throws RemoteException;
	
	public void setFar(double dist)  throws RemoteException;
	public void setNear(double dist)  throws RemoteException;
	public void showFrameRate() throws RemoteException;

}
