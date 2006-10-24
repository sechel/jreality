package de.jreality.renderman;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

class RenderScript {
	
	final File dir;
	
	final String ribFileName, texCmd, shaderCmd, refMapCmd, renderer, texSuffix, refMapSuffix;
	
	HashSet<String> ribFiles=new HashSet<String>(),
					textures=new HashSet<String>(),
			  reflectionMaps=new HashSet<String>(),
			         shaders=new HashSet<String>();

	private boolean execute=false;
	
	protected RenderScript(File dir, String ribFileName, int type) {
		this.dir=dir;
		this.ribFileName=ribFileName;
		switch (type) {
		case RIBViewer.TYPE_AQSIS:
			texCmd="teqser -mode 'periodic' ";
			shaderCmd="aqsl ";
			refMapCmd="??? ";
			renderer="aqsis ";
			texSuffix=".tx";
			refMapSuffix=".???";
			break;
		case RIBViewer.TYPE_3DELIGHT:
			texCmd="tdlmake -mode 'periodic' ";
			shaderCmd="shaderdl ";
			refMapCmd="??? ";
			renderer="renderdl ";
			texSuffix=".tdl";
			refMapSuffix=".???";
			break;
		default:
			texCmd="txmake -mode 'periodic' -resize 'up-' ";
			shaderCmd="shader ";
			refMapCmd="??? ";
			renderer="prman ";
			texSuffix=".tex";
			refMapSuffix=".???";
		}
	}
	
	void addTexture(String tex) {
		textures.add(tex);
	}
	
	void addReflectionMap(String reflectionMap) {
		reflectionMaps.add(reflectionMap);
	}

	void addShader(String shader) {
		shaders.add(shader);
	}

	void addRibFile(String ribFile) {
		ribFiles.add(ribFile);
	}
	
	void dumpScript() {
		
		System.out.println("========= render script ==========\n\n");
		
        System.out.println("cd "+dir.getAbsolutePath());
        
        for (String texName : textures) {
        	String cmd = texCmd+ribFileName+texName+".tiff "+ribFileName+texName+texSuffix;
			System.out.println(cmd);
        	//if (execute)
			exec(cmd, true);
        }
        
        for (String shaderName : shaders) {
        	String cmd = shaderCmd+shaderName;
			System.out.println(cmd);
			exec(cmd, true);
        }

        for (String refMapName : reflectionMaps) {
        	System.out.println(refMapCmd+refMapName);
        }
        
        String renderCmd = renderer + ribFileName;
		System.out.println(renderCmd);
		exec(renderCmd, false);

		System.out.println("\n\n========= render script ==========\n\n");

	}

	private void exec(String cmd, boolean wait) {
		if (!execute) return;
		ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
		pb.directory(dir);
		pb.redirectErrorStream(true);
		try {
			final Process proc = pb.start();
			Thread t = new Thread(new Runnable() {
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = null;
					try {
						while ((line = br.readLine()) != null) System.out.println(line);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			t.start();
			if (wait) proc.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
