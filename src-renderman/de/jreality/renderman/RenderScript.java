package de.jreality.renderman;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import de.jreality.shader.Texture2D;

class RenderScript {
	
  private boolean display=true;
  
	final File dir;
	
	final String ribFileName, texCmd, shaderCmd, refMapCmd, renderer, texSuffix, refMapSuffix;
	
  final int type;
  
	HashSet<String> ribFiles=new HashSet<String>(),
			         shaders=new HashSet<String>();
  HashSet<String[]> textures=new HashSet<String[]>(),
  					reflectionMaps=new HashSet<String[]>();


	private boolean execute=false;
	
	protected RenderScript(File dir, String ribFileName, int type) {
		this.dir=dir;
		this.ribFileName=ribFileName;
    this.type=type;
		switch (type) {
		case RIBViewer.TYPE_AQSIS:
			texCmd="teqser ";
			shaderCmd="aqsl ";
			refMapCmd="teqser -envcube ";
			renderer="aqsis ";
			texSuffix=".tex";
			refMapSuffix=".env";
			break;
		case RIBViewer.TYPE_3DELIGHT:
			texCmd="tdlmake ";
			shaderCmd="shaderdl ";
			refMapCmd="tdlmake -envcube ";
			renderer="renderdl ";
			texSuffix=".tex";
			refMapSuffix=".env";
			break;
    case RIBViewer.TYPE_PIXIE:
      texCmd="texmake ";
      shaderCmd="sdrc ";
      refMapCmd="texmake -envcube ";
      renderer="rndr ";
      texSuffix=".tex";
      refMapSuffix=".env";
      break;
		default:
			texCmd="txmake -resize 'up-' ";
			shaderCmd="shader ";
			refMapCmd="txmake -envcube ";
			renderer="prman ";
			texSuffix=".tex";
			refMapSuffix=".env";
		}
	}
	
	void addTexture(String tex, int smode, int tmode) {   
	    String repeatS=type == RIBViewer.TYPE_AQSIS ? "-swrap " : "-smode ";
	    switch(smode){
	    //case Texture2D.GL_MIRRORED_REPEAT: repeatS="";  // <- no support in renderman
	    case(Texture2D.GL_REPEAT): repeatS+="'periodic' "; break;           
	    case(Texture2D.GL_CLAMP):                  
	    case(Texture2D.GL_CLAMP_TO_EDGE): repeatS+="'clamp' "; break;
	    default: repeatS+="'periodic' ";
	    }    
	    String repeatT=type == RIBViewer.TYPE_AQSIS ? "-twrap " : "-tmode ";
	    switch(smode){
	    //case Texture2D.GL_MIRRORED_REPEAT: repeatS="";  // <- no support in renderman
	    case(Texture2D.GL_REPEAT): repeatT+="'periodic' "; break;           
	    case(Texture2D.GL_CLAMP):                  
	    case(Texture2D.GL_CLAMP_TO_EDGE): repeatT+="'clamp' "; break;
	    default: repeatT+="'periodic' ";
	    }    
	   	textures.add(new String[]{tex, repeatS, repeatT});
	}
	
	void addReflectionMap(String... reflectionMap) {
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
        
        for (String[] texName : textures) {
        	String cmd = texCmd+texName[1]+texName[2]+ribFileName+texName[0]+".tiff "+ribFileName+texName[0]+texSuffix;
			System.out.println(cmd);
			exec(cmd, true);
        }
        
        for (String shaderName : shaders) {
        	String cmd = shaderCmd+shaderName;
			System.out.println(cmd);
			exec(cmd, true);
        }

        for (String[] refMap : reflectionMaps) {
        	System.out.println(refMapCmd+refMap[1]+" "+refMap[2]+" "+refMap[3]+" "+refMap[4]+" "+refMap[5]+" "+refMap[6]+" "+ribFileName+refMap[0]+refMapSuffix);
        }
        String renderCmd = renderer + ribFileName;
        if(display&&(type!=RIBViewer.TYPE_PIXAR)&&(type!=RIBViewer.TYPE_PIXIE)) renderCmd = renderer +"-d "+ ribFileName +" &";
        
		System.out.println(renderCmd);
		exec(renderCmd, false);
    
//    if(display&&((type==RIBViewer.TYPE_PIXAR)||(type!=RIBViewer.TYPE_PIXIE))){
//      String fileName=ribFileName.substring(0,ribFileName.length()-4); 
//      System.out.println("display "+ fileName+".tif &");
//      
//    }
      

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
