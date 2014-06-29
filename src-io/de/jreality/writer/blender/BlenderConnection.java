package de.jreality.writer.blender;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

import de.jreality.io.JrScene;
import de.jreality.writer.WriterJRS;

/**
 * 
 * @author Stefan Sechelmann, Thilo Rörig
 *
 */
public class BlenderConnection {

	private Logger
		log = Logger.getLogger(BlenderConnection.class.getName());
	private File
		blenderApp = new File("/Applications/Blender/blender.app/Contents/MacOS/blender"),
		rendererScript = null;
	
	public BlenderConnection() {
	}
	
	protected void invokeBlender(File sceneFile, File outBlenderFile, File outImage) throws IOException {
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
//			outImage != null ? "--render=" + outImage.getAbsolutePath() : "",
			outBlenderFile != null ? "--save=" + outBlenderFile.getAbsolutePath() : "",
			"--file=" + sceneFile.getAbsolutePath()
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
			StringWriter sw = new StringWriter();
			while ((bIn = err.read()) != -1) {
				System.err.write(bIn);
				sw.write(bIn);
			}
			p.waitFor();
			String errString = sw.getBuffer().toString();
			if (!errString.isEmpty()) {
				throw new RuntimeException(errString);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected File writeScene(JrScene scene) throws IOException {
		File sceneFile = File.createTempFile("jrealityBlenderExportScene", ".jrs");
		sceneFile.deleteOnExit();
		OutputStream sceneOut = new FileOutputStream(sceneFile); 
		WriterJRS jrsWriter = new WriterJRS();
		jrsWriter.writeScene(scene, sceneOut);
		sceneOut.close();
		return sceneFile;
	}
	
	public void writeFile(JrScene scene, File f) throws IOException {
		File sceneFile = writeScene(scene);
		invokeBlender(sceneFile, f, null);
	}
	
	public void renderImage(JrScene scene, File f) throws IOException {
		File sceneFile = writeScene(scene);
		invokeBlender(sceneFile, null, f);
	}
	
	private File getRendererScript() throws IOException {
		if (rendererScript != null) {
			return rendererScript;
		}
		rendererScript = File.createTempFile("jrealityBlenderRenderer", "py");
		rendererScript.deleteOnExit();
		ReadableByteChannel rbc = Channels.newChannel(getClass().getResourceAsStream("renderer.py"));
		FileOutputStream fin = new FileOutputStream(rendererScript);
		FileChannel outChannel = fin.getChannel();
		outChannel.transferFrom(rbc, 0, Long.MAX_VALUE);
		outChannel.close();
		fin.close();
		return rendererScript;
	}
	
}
