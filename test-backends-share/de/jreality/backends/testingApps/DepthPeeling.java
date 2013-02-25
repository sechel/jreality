package de.jreality.backends.testingApps;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.shader.GLVBOFloat;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.*;

/**
 * A minimal program that draws with JOGL in an AWT Frame.
 *
 * @author Wade Walker
 */
// TODO convert this to openGL 3.3
// TODO replace triangle by cube with different colors
// TODO implement depth peeling
public class DepthPeeling implements GLEventListener{
	
	static float quadCoords[] = {
		-1, 1, 0, 1,
		.5f, .5f, 0, 1,
		1, -1, 0, 1,
		1, -1, 0, 1,
		-.5f, -.5f, 0, 1,
		-1, 1, 0, 1
	   };
	static float texCoords[] = {
		0,0,
		0,1,
		1,1,
		1,1,
		1,0,
		0,0
	};
	
	public static GLShader depth = new GLShader("/homes/extern/mvws9-02/depth.v", "/homes/extern/mvws9-02/depth.f");
	public static GLShader transp = new GLShader("/homes/extern/mvws9-02/transparent.v", "/homes/extern/mvws9-02/transparent.f");
	GLVBOFloat quadVerts, quadTex;
	@Override
    public void reshape(GLAutoDrawable dr, int x, int y, int width, int height){
    	System.out.println("Reshape");
    }
    
    @Override
    public void init( GLAutoDrawable dr ) {
    	System.out.println("Init");
    	GL3 gl = dr.getGL().getGL3();
    	depth.init(gl);
    	transp.init(gl);
    	
    	quadVerts = new GLVBOFloat(gl, quadCoords, "vertex_coordinates");
//    	quadTex = new GLVBOFloat(gl, texCoords, "texCoords");
    	
    	
    	
    	
//    	System.out.println("Init");
//    	GL3 gl = dr.getGL().getGL3();
//    	
//    	System.out.println("max vert attribs" + gl.GL_MAX_VERTEX_ATTRIBS);
//    	
//    	gl.glGenBuffers(1, buffers, 0);
//    	for(int i = 0; i < buffers.length; i++){
//    		System.out.println(i + ", " + buffers[i]);
//    	}
//    	gl.glBindBuffer(gl.GL_ARRAY_BUFFER, buffers[0]);
//    	Buffer b = FloatBuffer.wrap(triangleCoords);
//    	gl.glBufferData(gl.GL_ARRAY_BUFFER, 4*triangleCoords.length, b, gl.GL_STATIC_DRAW);
//    	
//    	gl.glVertexAttribPointer(0, 3, gl.GL_FLOAT, false, 0, 0);
//    	
//    	//setup the Framebuffer Object
//    	gl.glGenTextures(2, texs, 0);
//    	
//    	gl.glBindTexture(gl.GL_TEXTURE_2D, texs[0]);
//    	gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
//    	gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
//    	
//    	gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
//    	gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
//    	
//    	Buffer bu = ByteBuffer.allocate(512*512*3);
//    	
//    	gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGB, 512, 512, 0, gl.GL_RGB, gl.GL_BYTE, bu);
//    	
//    	
//    	
//    	gl.glGenFramebuffers(1, fbos, 0);
//    	gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
//    	
//    	gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D, texs[0], 0);
//    	
//    	
//    	
//    	//init vbos for quad
////    	gl.glGenBuffers(2, quad, 0);
////    	gl.glBindBuffer(gl.GL_ARRAY_BUFFER, quad[0]);
////    	Buffer b1 = FloatBuffer.wrap(quadCoords);
////    	gl.glBufferData(gl.GL_ARRAY_BUFFER, 4*quadCoords.length, b1, gl.GL_STATIC_DRAW);
////    	
////    	gl.glVertexAttribPointer(0, 3, gl.GL_FLOAT, false, 0, 0);
//    	
    }
    
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    	System.out.println("Dispose");
    }
    
    @Override
    public void display(GLAutoDrawable dr){
    	System.out.println("Display");
    	GL3 gl = dr.getGL().getGL3();
    	
    	transp.useShader(gl);
    	
    	gl.glBindBuffer(gl.GL_ARRAY_BUFFER, quadVerts.getID());
    	gl.glVertexAttribPointer(gl.glGetAttribLocation(transp.shaderprogram, quadVerts.getName()), quadVerts.getElementSize(), quadVerts.getType(), false, 0, 0);
    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(transp.shaderprogram, quadVerts.getName()));
	
    	gl.glDrawArrays(gl.GL_TRIANGLES, 0, quadVerts.getLength()/4);
    	
    	gl.glDisableVertexAttribArray(gl.glGetAttribLocation(transp.shaderprogram, quadVerts.getName()));
		
    	transp.dontUseShader(gl);
    	
//    	//gl2.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
//    	gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
//    	gl.glClear(gl.GL_COLOR_BUFFER_BIT);
//    	gl.glViewport(0, 0, 512, 512);
//    	
//    	gl.glEnableVertexAttribArray(0);
//    	gl.glDrawArrays(gl.GL_TRIANGLES, 3, 6);
//    	gl.glDisable(gl.GL_TEXTURE_2D);
//    	gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
//    	
//    	gl.glClear(gl.GL_COLOR_BUFFER_BIT);
//    	
//    	gl.glBindTexture(gl.GL_TEXTURE_2D, texs[0]);
//    	gl.glEnable(gl.GL_TEXTURE_2D);
//    	
//    	gl.glBegin(gl.GL_QUADS);
//    	
//    	gl.glTexCoord2f(0, 0);
//    	gl.glVertex2d(-1, 1);
//    	
//    	gl.glTexCoord2f(0, 1);
//    	gl.glVertex2d(.5, .5);
//    	
//    	gl.glTexCoord2f(1, 1);
//    	gl.glVertex2d(1, -1);
//    	
//    	gl.glTexCoord2f(1, 0);
//    	gl.glVertex2d(-.5, -.5);
//    	
//    	gl.glEnd();
    }
    
    
//	static int[] buffers = new int[1];
////	static int[] quad = new int[2];
//	static int[] fbos = new int[1];
//	static int[] texs = new int[2];
//	
//	static float triangleCoords[] = {
//			 // X, Y, Z
//             .5f, .5f, 0,
//             0.5f, 0f, 0,
//             0.0f,  0, 0,
//             // X, Y, Z
//             -.5f, -.5f, 0,
//             -0.5f, 0f, 0,
//             0.0f,  0, 0,
//             };
	
    public static void main( String [] args ) {
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        final GLCanvas glcanvas = new GLCanvas( glcapabilities );
        DepthPeeling depthPeeling = new DepthPeeling();
        glcanvas.addGLEventListener(depthPeeling);

        final Frame frame = new Frame( "One Triangle AWT" );
        frame.add( glcanvas );
        frame.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) {
                frame.remove( glcanvas );
                frame.dispose();
                System.exit( 0 );
            }
        });

        frame.setSize( 640, 480 );
        frame.setVisible( true );
    }

}