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


package de.jreality.sunflow;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import de.jreality.scene.Viewer;


@SuppressWarnings("serial")
public class Sunflow {

	private static String[] filters = {"png", "tga", "hdr"};

	private Sunflow() {}

	public static void renderAndSave(final Viewer v, RenderOptions options, final Dimension dim, File file) {
		final RenderDisplay renderDisplay = new RenderDisplay(file.getAbsolutePath());

		new Thread(new Runnable() {
			public void run() {
				SunflowRenderer renderer = new SunflowRenderer();
				renderer.render(
						v.getSceneRoot(),
						v.getCameraPath(),
						renderDisplay,
						dim.width,
						dim.height
				);
			}
		}).start();
	}

	public static void render(Viewer v, Dimension dim, RenderOptions options) {
		final SunflowViewer viewer = new SunflowViewer();
		viewer.setWidth(dim.width);
		viewer.setHeight(dim.height);
		viewer.setSceneRoot(v.getSceneRoot());
		viewer.setCameraPath(v.getCameraPath());
		final JFrame frame = new JFrame("Sunflow");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				viewer.cancel();
			}
		});
		frame.setLayout(new BorderLayout());
		JMenuBar bar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				save(viewer, frame);
			}
		});
		bar.add(fileMenu);
		frame.setJMenuBar(bar);
		viewer.setOptions(options);
		frame.setContentPane((Container) viewer.getViewingComponent());
		frame.pack();
		frame.setVisible(true);

		new Thread(new Runnable() {
			public void run() {
				viewer.render();
			}
		}).start();
	}


	private static void save(SunflowViewer v, JFrame frame) {
		FileSystemView view = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(view.getHomeDirectory(), view);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);

		//create file filters
		for (int i = 0; i < filters.length; i++) {
			final int ind = i;
			chooser.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return (f.isDirectory() || 
							f.getName().toLowerCase().endsWith("."+filters[ind]));
				}
				@Override
				public String getDescription() {
					return filters[ind].toUpperCase()+" Image (*."+filters[ind]+")";
				}
			});
		}
		chooser.setFileFilter(chooser.getChoosableFileFilters()[0]);

		//get target file and let user confirm an overwriting
		File file = null;
		while (true) {
			int choice = chooser.showSaveDialog(frame);
			if (choice == JFileChooser.APPROVE_OPTION) {
				File chosen = chooser.getSelectedFile();
				if (!chosen.exists()) {
					file = chosen; break;
				} 
				else {  //file exists
					int confirm = JOptionPane.showConfirmDialog(frame, "Overwrite file "+chosen.getName()+"?");
					if (confirm == JOptionPane.OK_OPTION) {
						file = chosen; break;
					} 
					else if (confirm == JOptionPane.NO_OPTION) continue;
					break;  //confirm == CANCEL_OPTION
				}
			} else break;  //choice == CANCEL_OPTION
		}

		if (file != null) v.getViewingComponent().save(file.getAbsolutePath());
	}

}