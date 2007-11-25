package de.jreality.sunflow.batchrender;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import de.jreality.sunflow.RenderOptions;
import de.jreality.util.ImageUtility;
import de.smrj.Broadcaster;
import de.smrj.executor.DistributedExecutorService;
import de.smrj.executor.RemoteCallable;

public class Master {
	
	int tilesX, tilesY;
	BufferedImage img;
	File outFile;
	
	boolean[][] merged;
	
	public Master(String pngFile, int w, int h, int tX, int tY) throws IOException {
		outFile=new File(pngFile);
		outFile.createNewFile();
		if (outFile.isDirectory() || !outFile.canWrite()) throw new IOException("illegal file: "+pngFile);
		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		tilesX=tX;
		tilesY=tY;
		merged = new boolean[tX][tY];
	}
	
	synchronized void tileFinished(int tx, int ty, String filename) throws IOException {
		System.out.println("writing tile: "+tx+"x"+ty);
		merged[tx][ty]=true;
		if (filename != null) {
			BufferedImage bi = ImageIO.read(new File(filename));
			img.getGraphics().drawImage(bi, tx*img.getWidth()/tilesX, img.getHeight()-(ty+1)*img.getHeight()/tilesY, null);
		}
		checkFinished();
	}

	private void checkFinished() {
		for (int i=0; i<tilesX; i++) {
			for (int j=0; j<tilesY; j++) {
				if (!merged[i][j]) return;
			}
		}
		try {
			ImageIO.write(img, "png", outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) throws IOException {
		int width = 8000;
		int height = 8000;
		int tilesX = 40;
		int tilesY = 40;
		String base = args[0]; // "/net/MathVis/Projects/Exhibition/tetranoidAusstellung";
		
		RenderOptions renderOptions = new RenderOptions();
		renderOptions.setProgressiveRender(false);
		renderOptions.setAaMin(0);
		renderOptions.setAaMax(4);
		renderOptions.setDepthsRefraction(8);
		renderOptions.setDepthsReflection(4);
		
		renderOptions.setFilter("gaussian");
		
		String[] hosts = new String[100]; //parseHosts("/net/MathVis/Projects/Exhibition/good_guys");
		//System.out.println(System.getProperty("java.class.path"));
		//System.out.println(Arrays.toString(hosts));
		//if (true) return;
		final DistributedExecutorService des = new DistributedExecutorService(hosts);
		broadcastStaticMethodCall(des, "setJrsFile", new Class<?>[]{String.class}, new Object[]{base+".jrs"});
		broadcastStaticMethodCall(des, "setRenderOptions", new Class<?>[]{RenderOptions.class}, new Object[]{renderOptions});
		broadcastStaticMethodCall(des, "setExtension", new Class<?>[]{String.class}, new Object[]{"png"});
		broadcastStaticMethodCall(des, "setImageSize", new Class<?>[]{Integer.class, Integer.class}, new Object[]{width, height});
		broadcastStaticMethodCall(des, "setTiling", new Class<?>[]{Integer.class, Integer.class}, new Object[]{tilesX, tilesY});
		
		final Master master = new Master(base+".png", width, height, tilesX, tilesY);
		
		for (int i=0; i<tilesX; i++) {
			for (int j=0; j<tilesX; j++) {
				final int tX=i;
				final int tY=j;
				
				final Future<String> f = des.submit(new RemoteCallable<String>() {
						private static final long serialVersionUID = 3713825184723907804L;
						public String call() throws Exception {
							return Client.renderTile(tX, tY);
						}						
					});
				new Thread() {
					@Override
					public void run() {
						try {
							master.tileFinished(tX, tY, f.get());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
			}
		}
		
		
	}

	private static String[] parseHosts(String string) {
		LinkedList<String> hosts = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(string));
			String line=null;
			while ((line=br.readLine())!=null) hosts.add(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hosts.toArray(new String[0]);
	}

	private static void broadcastStaticMethodCall(DistributedExecutorService des, 
			String methodName, Class<?>[] sig, Object[] params) {
		System.out.println("broadcast call: "+methodName);
		for (Broadcaster bc : des.broadcasters()) {
			System.out.print('.');
			try {
				bc.getRemoteFactory().createRemoteViaStaticMethod(Object.class, Client.class, methodName, sig, params);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("\ndone.");
	}
}
