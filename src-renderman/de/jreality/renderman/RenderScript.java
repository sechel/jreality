package de.jreality.renderman;

import java.io.File;
import java.util.HashSet;

class RenderScript {
	
	final File dir;
	
	final String ribFileName, texCmd, shaderCmd, refMapCmd, renderer, texSuffix, refMapSuffix;
	
	HashSet<String> ribFiles=new HashSet<String>(),
					textures=new HashSet<String>(),
			  reflectionMaps=new HashSet<String>(),
			         shaders=new HashSet<String>();
	
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
        	System.out.println(texCmd+ribFileName+texName+".tiff "+ribFileName+texName+texSuffix);
        }
        
        for (String shaderName : shaders) {
        	System.out.println(shaderCmd+shaderName);
        }

        for (String refMapName : reflectionMaps) {
        	System.out.println(refMapCmd+refMapName);
        }
        
        System.out.println(renderer + ribFileName);

		System.out.println("\n\n========= render script ==========\n\n");

	}
	
	
	
}
