package de.jreality.plugin.blender;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jreality.writer.WriterJRS;

public class BlenderRenderer {

	private Logger
		log = Logger.getLogger(BlenderRenderer.class.getName());
	private JrScene
		scene = null;
	private File
		blenderApp = new File("/Applications/Blender/blender.app/Contents/MacOS/blender"),
		rendererScript = null;
	
	public BlenderRenderer(JrScene scene) {
		this.scene = scene;
	}
	
	public BufferedImage render() throws IOException {
		// write scene
		File sceneFile = new File("test.jrs");//File.createTempFile("jrealityBlenderScene", ".jrs");
//		sceneFile.deleteOnExit();
		OutputStream sceneOut = new FileOutputStream(sceneFile); 
		WriterJRS jrsWriter = new WriterJRS();
		jrsWriter.writeScene(scene, sceneOut);
		sceneOut.close();
		
		// create result file
		File result = File.createTempFile("jrealityBlenderResult", ".png");
		result.deleteOnExit();
		
		// run renderer
		File script = null;
		try {
			script = getRendererScript();
		} catch (IOException e1) {
			log.warning("could not write blender renderer script: " + e1.getMessage());
			throw e1;
		}
		String[] args = {
			"./blender",
			"--background",
			"--factory-startup",
			"--render-format", "PNG",
			"--python",
			script.toString(),
			"--",
			"--render=" + result.getAbsolutePath(),
			"--file=" + sceneFile.getAbsolutePath(),
			"--save=/Users/sechel/workspace/jreality/test.blend"
		};
		try {
			File dir = blenderApp.getParentFile();
			Process p = Runtime.getRuntime().exec(args, new String[]{}, dir);
			InputStream in = new BufferedInputStream(p.getInputStream());
			InputStream err = new BufferedInputStream(p.getErrorStream());
			int bIn = 0;
			while ((bIn = in.read()) != -1) {
				System.out.write(bIn);
			}
			while ((bIn = err.read()) != -1) {
				System.err.write(bIn);
			}
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ImageIO.read(result);
	}
	
	private File getRendererScript() throws IOException {
		return new File("/Users/sechel/workspace/jreality/src-plugin/de/jreality/plugin/blender/renderer.py");
//		if (rendererScript != null) {
//			return rendererScript;
//		}
//		rendererScript = File.createTempFile("jrealityBlenderRenderer", "py");
//		rendererScript.deleteOnExit();
//		ReadableByteChannel rbc = Channels.newChannel(getClass().getResourceAsStream("renderer.py"));
//		FileOutputStream fin = new FileOutputStream(rendererScript);
//		FileChannel outChannel = fin.getChannel();
//		outChannel.transferFrom(rbc, 0, Long.MAX_VALUE);
//		outChannel.close();
//		fin.close();
//		return rendererScript;
	}
	
	public static void main(String[] args) throws Exception {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("Scene Root");
		SceneGraphComponent icosahedron = new SceneGraphComponent();
		icosahedron.setName("Icosahedron Root");
		icosahedron.setGeometry(Primitives.icosahedron());
		root.addChild(icosahedron);
		SceneGraphComponent cameraRoot = new SceneGraphComponent();
		cameraRoot.setName("Camera Root");
		Camera cam = new Camera("My Camera");
		cameraRoot.setCamera(cam);
		root.add(cameraRoot);
		JrScene scene = new JrScene(root);
		scene.addPath("cameraPath", new SceneGraphPath(root, cameraRoot, cam));
		BlenderRenderer r = new BlenderRenderer(scene);
		BufferedImage image = r.render();
		if (image != null) {
			JFrame f = new JFrame();
			f.add(new JLabel(new ImageIcon(image)));
			f.pack();
			f.setVisible(true);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}
	
}
