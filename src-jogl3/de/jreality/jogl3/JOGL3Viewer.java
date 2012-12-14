package de.jreality.jogl3;

import static de.jreality.shader.CommonAttributes.SKY_BOX;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;

import de.jreality.jogl3.light.JOGLDirectionalLightEntity;
import de.jreality.jogl3.light.JOGLDirectionalLightInstance;
import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.jogl3.light.JOGLPointLightEntity;
import de.jreality.jogl3.light.JOGLPointLightInstance;
import de.jreality.jogl3.light.JOGLSpotLightEntity;
import de.jreality.jogl3.light.JOGLSpotLightInstance;
import de.jreality.jogl3.shader.PointShader;
import de.jreality.jogl3.shader.Texture2DLoader;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.StereoViewer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CubeMap;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;


public class JOGL3Viewer implements de.jreality.scene.Viewer, StereoViewer, GLEventListener {

	SceneGraphComponent auxiliaryRoot;
	protected JPanel component;
	protected GLCanvas canvas;
	
	public JOGL3Viewer() throws Exception{
		System.out.println("JOGL3Viewer constuctor called");
		this.auxiliaryRoot = SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot");
		
		GLProfile glp = null;
		
		try{
			glp = GLProfile.get("GL3");
			String s = glp.glAvailabilityToString();
			System.out.println(s);
			if(!s.contains("3.3") && !s.contains("GL4 true")){
				System.err.println("opengl 3.3 not available, thus no jogl3-backend");
				throw new Exception("opengl 3.3 not available, thus no jogl3-backend");
			}
		}catch(GLException e){
			System.out.println(e.getMessage());
		}
		if(glp == null){
			try{
				glp = GLProfile.get("GL3bc");
			}catch(GLException e){
				System.out.println(e.getMessage());
			}
		}
		if(glp == null){
			try{
				glp = GLProfile.get("GL4");
			}catch(GLException e){
				System.out.println(e.getMessage());
			}
		}
		if(glp == null){
			try{
				glp = GLProfile.get("GL4bc");
			}catch(GLException e){
				System.out.println(e.getMessage());
				throw new Exception("no openGL profile available to support JOGL3");
			}
		}
		
		GLCapabilities caps = new GLCapabilities(glp);
		//TODO: check caps.setAccumAlphaBits(8);
		caps.setAlphaBits(8);
		caps.setStereo(false);
		caps.setDoubleBuffered(true);
		caps.setNumSamples(16);
		caps.setSampleBuffers(true);
		
		canvas = new GLCanvas(caps);
		canvas.addGLEventListener(this);
		
	}
	
	public boolean canRenderAsync() {
		System.out.println("can render async called");
		// TODO Auto-generated method stub
		return false;
	}

	public SceneGraphComponent getAuxiliaryRoot() {
		System.out.println("getAuxiliaryRoot");
		// TODO Auto-generated method stub
		return auxiliaryRoot;
	}

	public SceneGraphPath getCameraPath() {
		//System.out.println("getCameraPath");
		return camPath;
	}

	public SceneGraphComponent getSceneRoot() {
		System.out.println("getSceneRoot");
		// TODO Auto-generated method stub
		return sceneRoot;
	}

	KeyListener keyListener = new KeyListener() {
        public void keyPressed(KeyEvent e) {
          component.dispatchEvent(e);
        }
        public void keyReleased(KeyEvent e) {
          component.dispatchEvent(e);
        }
        public void keyTyped(KeyEvent e) {
          component.dispatchEvent(e);
        }
      };
      MouseListener mouseListener = new MouseListener() {
        public void mouseClicked(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mouseEntered(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mouseExited(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mousePressed(MouseEvent e) {
          component.dispatchEvent(e);
          canvas.requestFocus();
        }
        public void mouseReleased(MouseEvent e) {
          component.dispatchEvent(e);
        }
      };
    MouseWheelListener mouseWheelListener = new MouseWheelListener() {
        public void mouseWheelMoved(MouseWheelEvent e) {
          component.dispatchEvent(e);
        }
      };

      MouseMotionListener mouseMotionListener = new MouseMotionListener() {
        public void mouseDragged(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mouseMoved(MouseEvent e) {
          component.dispatchEvent(e);
        }
      };
	
	public Object getViewingComponent() {
		//System.out.println("getViewingComponent");
		// TODO Auto-generated method stub
		if (component == null) {
		      component=new javax.swing.JPanel();
		      component.setLayout(new java.awt.BorderLayout());
		      component.setMaximumSize(new java.awt.Dimension(32768,32768));
		      component.setMinimumSize(new java.awt.Dimension(10,10));
		      if (canvas == null) return component;
		      component.add("Center", canvas);
			canvas.addKeyListener(keyListener);
			canvas.addMouseListener(mouseListener);
			canvas.addMouseMotionListener(mouseMotionListener);
			canvas.addMouseWheelListener(mouseWheelListener);
		}
		return component;
	}

	public Dimension getViewingComponentSize() {
		return ((Component) getViewingComponent()).getSize();
	}

	public boolean hasViewingComponent() {
		return true;
	}

	public void render() {
		//this method is called by the jReality event mechanism,
		//it calls the GLCanvas.display() method. GLCanvas decides
		//whether the GL has been set up properly, tries to call init(),
		//and if all goes well it calls the GLEventListener.display(GLAutoDrawable arg0) method
		//implemented here.
		//System.out.println("called render");
		if(sceneRoot == null)
			System.out.println("scene root is null");
		else{
			//print(sceneRoot);
		}
		
		canvas.display();
	}
	
	public void renderAsync() {
		System.out.println("renderAsync");
		//if (disposed) return;
	    
	}

	public void setAuxiliaryRoot(SceneGraphComponent auxRoot) {
		System.out.println("set Aux Root");
		this.auxiliaryRoot = auxRoot;
	}
	SceneGraphPath camPath = null;
	public void setCameraPath(SceneGraphPath cameraPath) {
		//System.out.println("setCameraPath");
		camPath = cameraPath;
		
	}
	
	SceneGraphComponent sceneRoot = null;
	
	JOGLSceneGraph proxyScene = null;
	
	public void setSceneRoot(SceneGraphComponent root) {
		if (proxyScene != null) proxyScene.dispose();
		System.out.println("setSceneRoot");
		sceneRoot = root;
		proxyScene = new JOGLSceneGraph(root);
		proxyScene.createProxyTree();
	}
	public int getStereoType() {
		System.out.println("getStereoType");
		// TODO Auto-generated method stub
		return stereoType;
	}
	int stereoType = 0;
	public void setStereoType(int type) {
		System.out.println("setStereoType");
		stereoType = type;
	}
	
	public void display(GLAutoDrawable arg0){
		display(arg0, component.getWidth(), component.getHeight());
	}
	
	CubeMap skyboxCubemap;
	
	public void display(GLAutoDrawable arg0, int width, int height) {
		
		// TODO Auto-generated method stub
		//System.out.println("display called---------------------------------");
		if(arg0.getGL() != null && arg0.getGL().getGL3() != null){
			
//			double[] aMatrix = new double[16];
//			this.camPath.getMatrix(aMatrix);
			double[] mat = new double[16];
	        Camera cam = (Camera)(this.camPath.getLastElement());
	        double ar = ((double) arg0.getWidth())/arg0.getHeight();
	        //ar = width/height;
	        mat = CameraUtility.getCameraToNDC(cam, ar);
//	        P3.makePerspectiveProjectionMatrix(mat, CameraUtility.getViewport(cam, 1), (float)cam.getNear(), (float)cam.getFar());
	        double[] dmat = new double[16];
	        this.camPath.getInverseMatrix(dmat);
	        //Rn.times(dmat, mat, dmat);
	        
			
			GL3 gl = arg0.getGL().getGL3();
			
			gl.glClearColor(0f, 0f, 0f, 1f);
			gl.glClear(gl.GL_COLOR_BUFFER_BIT);
			gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
			
	    	gl.glViewport(0, 0, width, height);
	        
	        
	        
			//rootState = new JOGLRenderState(rootState, dmat);
			
			JOGLSceneGraphComponentInstance rootInstance = (JOGLSceneGraphComponentInstance) proxyScene.getTreeRoot();
			
			//update sky box
			JOGLAppearanceInstance rootApInst = (JOGLAppearanceInstance)rootInstance.getAppearanceTreeNode();
			if(!((JOGLAppearanceEntity)rootApInst.getEntity()).dataUpToDate){
				System.out.println("cube map not upToDate");
				Appearance rootAp = (Appearance) rootApInst.getEntity().getNode();
				if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class,SKY_BOX, rootAp)) {
					skyboxCubemap = (CubeMap) AttributeEntityUtility.createAttributeEntity(CubeMap.class, SKY_BOX, rootAp, true);
				}else{
					skyboxCubemap = null;
				}
				Texture2DLoader.deleteTextures(gl);
			}
			
			//render skybox
			JOGLSkybox.render(gl, dmat, mat, skyboxCubemap, cam);

			//enable depth test
			gl.glEnable(gl.GL_DEPTH_TEST);
			//collect lights from scene graph
			JOGLLightCollection lightCollection = new JOGLLightCollection(dmat);
			rootInstance.collectGlobalLights(dmat, lightCollection);
			//can load global lights texture here.
			int lightTex = loadGlobalLightTexture(lightCollection, gl);
			
			//calculate window dimensions and such needed for sprite size calculation
			Rectangle2D r = CameraUtility.getViewport(cam, ar);
			float x = (float)(r.getMaxX()-r.getMinX());
			float y = (float)(r.getMaxY()-r.getMinY());
			//render scene graph
			JOGLRenderState rootState = new JOGLRenderState(gl, dmat, mat, lightTex, lightCollection.directionalLights.size(), lightCollection.pointLights.size(), lightCollection.spotLights.size(), Math.min(component.getWidth(), component.getHeight()), Math.min(x, y));
			rootInstance.render(rootState);
			rootInstance.setAppearanceEntitiesUpToDate();
			
		}
	}

	private int loadGlobalLightTexture(JOGLLightCollection lc, GL3 gl) {
		//texture id
		int[] textures = new int[1];
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE0);
		gl.glGenTextures(1, textures, 0);
		
		gl.glBindTexture(gl.GL_TEXTURE_2D, textures[0]);
		
		int width = lc.directionalLights.size()*2+lc.pointLights.size()*3+lc.spotLights.size()*5;
//		System.out.println("dir lights" + lc.directionalLights.size());
//		System.out.println("point lights" + lc.pointLights.size());
//		System.out.println("spot lights" + lc.spotLights.size());
//		System.out.println("width = " + width);
		float[] data = new float[width*4];
		int i = 0;
		for(JOGLDirectionalLightInstance d : lc.directionalLights){
			JOGLDirectionalLightEntity dl = (JOGLDirectionalLightEntity)d.getEntity();
			
			data[i+0] = dl.getColor()[0]*(float)dl.getIntensity();
			data[i+1] = dl.getColor()[1]*(float)dl.getIntensity();
			data[i+2] = dl.getColor()[2]*(float)dl.getIntensity();
			data[i+3] = dl.getColor()[3]*(float)dl.getIntensity();
			
			//direction of the light
			data[i+4] = (float)d.trafo[2];
			data[i+5] = (float)d.trafo[6];
			data[i+6] = (float)d.trafo[10];
			data[i+7] = (float)d.trafo[14];//?needed?
			
			i+=8;
		}
		for(JOGLPointLightInstance d : lc.pointLights){
			JOGLPointLightEntity dl = (JOGLPointLightEntity)d.getEntity();
			
			data[i+0] = dl.getColor()[0]*(float)dl.getIntensity();
			data[i+1] = dl.getColor()[1]*(float)dl.getIntensity();
			data[i+2] = dl.getColor()[2]*(float)dl.getIntensity();
			data[i+3] = dl.getColor()[3]*(float)dl.getIntensity();
			
			//position of the light
			data[i+4] = (float)d.trafo[3];
			data[i+5] = (float)d.trafo[7];
			data[i+6] = (float)d.trafo[11];
			data[i+7] = (float)d.trafo[15];
			
			//attenuation
			data[i+8] = (float)dl.A0;
			data[i+9] = (float)dl.A1;
			data[i+10] = (float)dl.A2;
			
			i+=12;
		}
		for(JOGLSpotLightInstance d : lc.spotLights){
			JOGLSpotLightEntity dl = (JOGLSpotLightEntity)d.getEntity();
			
			data[i+0] = dl.getColor()[0]*(float)dl.getIntensity();
			data[i+1] = dl.getColor()[1]*(float)dl.getIntensity();
			data[i+2] = dl.getColor()[2]*(float)dl.getIntensity();
			data[i+3] = dl.getColor()[3]*(float)dl.getIntensity();
			
			//direction
			data[i+4] = (float)d.trafo[2];
			data[i+5] = (float)d.trafo[6];
			data[i+6] = (float)d.trafo[10];
			data[i+7] = (float)d.trafo[14];
			
			//position
			data[i+8] = (float)d.trafo[3];
			data[i+9] = (float)d.trafo[7];
			data[i+10] = (float)d.trafo[11];
			data[i+11] = (float)d.trafo[15];
			
			data[i+12] = (float)dl.coneAngle;
			data[i+13] = (float)dl.coneAngleDelta;
			data[i+14] = (float)dl.distribution;
			
			//attenuation
			data[i+16] = (float)dl.A0;
			data[i+17] = (float)dl.A1;
			data[i+18] = (float)dl.A2;
			
			i+=20;
		}
		
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST); 
	    gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
	    
	    gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGBA32F, 
    	width, 1, 0, gl.GL_RGBA,
	    gl.GL_FLOAT, FloatBuffer.wrap(data));
		
		return textures[0];
	}

	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		System.out.println("calling JOGL3Viewer.dispose");
	}
	public void init(GLAutoDrawable arg0) {
		System.out.println("init!!!!!!!!!!!!");
		
		GL3 gl = arg0.getGL().getGL3();
//		int[] arr = new int[1];
//		gl.glGetIntegerv(gl.GL_SAMPLE_BUFFERS, arr, 0);
//		System.out.println(arr[0]);
//		gl.glEnable(gl.GL_MULTISAMPLE);
//		gl.glGetIntegerv(gl.GL_SAMPLE_BUFFERS, arr, 0);
//		System.out.println(arr[0]);
		
		
		int buf[] = new int[1];
	    int sbuf[] = new int[1];

	    //gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	    gl.glEnable(gl.GL_BLEND);
		//gl.glBlendEquation(gl.GL_FUNC_ADD);
		//gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
		//initialize shaders once
		GLShader.initDefaultShaders(gl);
		PointShader.init(gl);
		//skybox = new JOGLSkybox();
		JOGLSkybox.init(gl);
		//initialize vbo once
	}
	
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4) {
		// TODO Auto-generated method stub
		
	}
	
}
