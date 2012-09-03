package de.jreality.tutorial.viewer;

import java.io.File;
import java.io.IOException;

import de.jreality.plugin.JRViewer;
import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;

public class SimpleJRViewer {
	public static void main(String[] args) throws IOException {
		SceneGraphComponent read = Readers.read(new File("/homes/geometer/gunn/Downloads/face.stl"));
		JRViewer.display(read);
	}

}
