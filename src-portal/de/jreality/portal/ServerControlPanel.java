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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import de.jreality.util.ConfigurationAttributes;

/**
 * @author weissman
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ServerControlPanel extends JFrame {

	JTextField tf;
	JButton button;

	RemoteServer server;

	public ServerControlPanel() {
		super();
		
		try {
			server = (RemoteServer) Naming.lookup(ConfigurationAttributes.getDefaultConfiguration().getProperty("server.uri"));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotBoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		tf = new JTextField();
		button = new JButton("load World...");

		getContentPane().add("North", tf);
		getContentPane().add("Center", button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadWorld();
			}
		});
		tf.setText("de.jreality.worlds.DebugLattice");
		pack();
		show();
		
	}

	private void loadWorld() {
		final String classname = tf.getText();
		try {
			server.loadWorld(classname);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws RemoteException {
		new ServerControlPanel();
	}

}