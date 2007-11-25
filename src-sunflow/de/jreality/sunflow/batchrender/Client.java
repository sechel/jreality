package de.jreality.sunflow.batchrender;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import de.jreality.io.JrScene;
import de.jreality.reader.ReaderJRS;
import de.jreality.sunflow.RenderOptions;
import de.jreality.sunflow.SunflowRenderer;
import de.jreality.util.Input;

public class Client {
	
	private static String sceneFile;
	private static JrScene scene;
	
	private static RenderOptions opts;
	private static int width, height;
	private static int tilesX, tilesY;
	private static String extension;
	
	public Client() {}
	
	public static void setJrsFile(String fileName) {
		if (sceneFile == fileName) return;
		sceneFile = fileName;
		scene = null;
	}

	private static void load() throws IOException {
		if (scene!=null) return;
		System.out.println("loading scene "+sceneFile);
		ReaderJRS r = new ReaderJRS();
		r.setInput(Input.getInput(sceneFile));
		scene = r.getScene();
		System.out.println("loaded scene "+sceneFile);
	}
	
	public static void setRenderOptions(RenderOptions opts) {
		Client.opts = opts;
	}
	
	public static void setExtension(String ext) {
		extension = ext;
		System.out.println("ext="+ext);
	}
	
	public static void setImageSize(Integer w, Integer h) {
		width=w;
		height=h;
		System.out.println("w="+w+"h="+h);
	}
	
	public static void setTiling(Integer tX, Integer tY) {
		tilesX=tX;
		tilesY=tY;
		System.out.println("tX="+tX+"tY="+tY);
	}
	
	public static String renderTile(int tileX, int tileY) {
		try {
			load();
			String name = sceneFile.replaceAll("/", "_")+"_tile_"+tileX+"x"+tileY+"."+extension;
			FileDisplay fd = new FileDisplay(name);
			SunflowRenderer renderer = new SunflowRenderer();
			renderer.setOptions(opts);
			renderer.render(
					scene.getSceneRoot(),
					scene.getPath("cameraPath"),
					fd,
					width, height, new int[]{tilesX, tilesY, tileX, tileY});
			return new File(name).getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return null;
	}

}
