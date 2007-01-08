/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.ui.viewerapp.actions.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;

import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;


/**
 * Sets the viewer aspect ratio to 4:3.<br>
 * There is only one instance of this action.
 * 
 * @author pinkall
 */
@SuppressWarnings("serial")
public class ViewerAspect4To3 extends AbstractJrAction {
	
	private ViewerApp viewerApp;

	private static HashMap <ViewerApp, ViewerAspect4To3> sharedInstances = new HashMap <ViewerApp, ViewerAspect4To3>();


	private ViewerAspect4To3(String name, ViewerApp viewerApp) {
		super(name);
		this.viewerApp = viewerApp;
		this.parentComp = viewerApp.getFrame();

		setShortDescription("Set viewer aspect ratio to 4:3");
	}


	/**
	 * Returns a shared instance of this action depending on the specified viewerApp
	 * (i.e. there is a shared instance for each viewerApp). 
	 * The action's name is overwritten by the specified name.
	 * @param name name of the action
	 * @param viewerApp the viewerApp displaying the viewer
	 * @throws UnsupportedOperationException if viewerApp equals null
	 * @return shared instance of ViewerAspect with specified name
	 */
	public static ViewerAspect4To3 sharedInstance(String name, ViewerApp viewerApp) {
		if (viewerApp == null) 
			throw new UnsupportedOperationException("ViewerApp not allowed to be null!");

		ViewerAspect4To3 sharedInstance = sharedInstances.get(viewerApp);
		if (sharedInstance == null) {
			sharedInstance = new ViewerAspect4To3(name, viewerApp);
			sharedInstances.put(viewerApp, sharedInstance);
		}

		sharedInstance.setName(name);
		return sharedInstance;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		JFrame frame = viewerApp.getFrame();
		JComponent v = (JComponent) viewerApp.getViewer().getViewingComponent();
		int height = v.getHeight();
		v.setPreferredSize(new Dimension((int)(height/3.*4.),height));
		frame.pack();
		v.requestFocusInWindow();
	}
}