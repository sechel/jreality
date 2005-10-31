package de.jreality.jogl.shader;

import java.beans.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.GlslSource;
import de.jreality.shader.GlslSource.UniformParameter;

public class GlslLoader {

  private static final WeakHashMap GL_TO_GLSL=new WeakHashMap();
  
  public static void render(GlslProgram prog, GLDrawable drawable) {
    GL gl = drawable.getGL();
    ProgramContext context = getContext(gl, prog);
    context.linkProgram(gl);
    context.activateProgram(gl);
    // now set all (changed) values
    for (Iterator it = prog.getSource().getUniformParameters().iterator(); it.hasNext(); ) {
      UniformParameter param = (UniformParameter) it.next();
      Object value = prog.getUniform(param.getName());
      Object oldValue = context.getUniform(param);
      //System.out.println("checking "+param);
      //System.out.println("\t old="+oldValue+ " new="+value);
      if (compare(oldValue, value, param.getPrimitiveType())) {
        context.writeValue(gl, param, value);
      }
    }
  }
  
  private static boolean compare(Object oldValue, Object value, Class primitive) {
    if (value == null) return false;
    if (oldValue == null) return true;
    if (primitive == float.class) {
      float[] old = (float[]) oldValue;
      float[] newF = (float[]) value;
      if (old.length != newF.length) return true;
      for (int i = 0; i < old.length; i++) if (old[i] != newF[i]) return true;
    } else {
      int[] old = (int[]) oldValue;
      int[] newF = (int[]) value;
      if (old.length != newF.length) return true;
      for (int i = 0; i < old.length; i++) if (old[i] != newF[i]) return true;
    }
    return false;
  }

  public static void postRender(GlslProgram prog, GLDrawable drawable) {
    GL gl = drawable.getGL();
    ProgramContext context = getContext(gl, prog);
    context.deavtivateProgram(gl);
  }
  
  private static ProgramContext getContext(GL gl, GlslProgram prog) {
    WeakHashMap glContexts = (WeakHashMap) GL_TO_GLSL.get(gl);
    if (glContexts == null) {
      glContexts=new WeakHashMap();
      GL_TO_GLSL.put(gl, glContexts);
    }
    ProgramContext context = (ProgramContext) glContexts.get(prog.getSource());
    if (context == null) {
      context = new ProgramContext(prog.getSource());
      glContexts.put(prog.getSource(), context);
    }
    return context;
  }

  private static class ProgramContext {
    final GlslSource source;
    final HashMap currentValues=new HashMap();
    boolean isLinked;
    Integer progID;
    
    ProgramContext(GlslSource source) {
      this.source=source;
    }
    
    public void activateProgram(GL gl) {
      gl.glUseProgramObjectARB(progID.intValue());
    }
    
    public void deavtivateProgram(GL gl) {
      gl.glUseProgramObjectARB(0);
    }

    Object getUniform(UniformParameter parameter) {
      return currentValues.get(parameter);
    }
    
    void writeValue(GL gl, UniformParameter param, Object value) {
      StringBuffer sb = new StringBuffer("glUniform");
      if (param.isMatrix()) sb.append("Matrix");
      sb.append(param.getPrimitiveSize());
      sb.append(param.getPrimitiveType() == float.class ? 'f' : 'i');
      sb.append('v');
      sb.append("ARB");

      Object[] params = new Object[3];
      params[0] = uniLocation(param.getName(), gl);
      params[1] = new Integer(param.isArray() ? param.getArrayLength() : 1);
      params[2] = value;
      Statement s = new Statement(gl, sb.toString(), params);
      //System.out.println("will call: "+s);
      try {
        s.execute();
        //printInfoLog(param.toString(), progID.intValue(), gl);
      } catch (Exception e) {
        printInfoLog(param.toString(), progID.intValue(), gl);
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      currentValues.put(param, value);
    }
    
    void linkProgram(GL gl) {
      if (isLinked) return;
      // check once again if no instance is loaded:
      int vertexProgID = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
      int fragmentProgID = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
      gl.glShaderSourceARB(vertexProgID, source.getVertexProgram().length, source.getVertexProgram(), (int[]) null);
      gl.glShaderSourceARB(fragmentProgID, source.getFragmentProgram().length, source.getFragmentProgram(), (int[]) null);
      gl.glCompileShaderARB(vertexProgID);
      gl.glCompileShaderARB(fragmentProgID);
      printInfoLog("vert compile", vertexProgID, gl);
      printInfoLog("frag compile", fragmentProgID, gl);
      progID = new Integer(gl.glCreateProgramObjectARB());
      gl.glAttachObjectARB(progID.intValue(), vertexProgID);
      gl.glAttachObjectARB(progID.intValue(), fragmentProgID);
      printInfoLog("vert attatch", vertexProgID, gl);
      printInfoLog("frag attatch", fragmentProgID, gl);
      printInfoLog("prog attatch", progID.intValue(), gl);
      gl.glLinkProgramARB(progID.intValue());
      printInfoLog("prog link", progID.intValue(), gl);
      System.out.println("loaded program ["+progID+"]");
      isLinked = true;
    }

    private Integer uniLocation(String name, GL gl) {
      int loc;
      loc = gl.glGetUniformLocationARB(progID.intValue(), name);
      if (loc == -1) {
        throw new IllegalStateException();
      }
      return new Integer(loc);
    }
  }
  
  private static void printInfoLog(String name, int objectHandle, GL gl) {
    int[] logLength = new int[1];
    int[] charsWritten = new int[1];
    byte[] infoLog;
    
    gl.glGetObjectParameterivARB(objectHandle, GL.GL_OBJECT_INFO_LOG_LENGTH_ARB, logLength);
    
    if (logLength[0] > 0) {
      infoLog = new byte[logLength[0]];
      gl.glGetInfoLogARB(objectHandle, logLength[0], charsWritten, infoLog );
      StringBuffer foo = new StringBuffer(charsWritten[0]);
      
      for (int i = 0; i< charsWritten[0]; ++i)  foo.append((char) infoLog[i]);
      if (foo.length() > 0)
        System.out.println("["+name+"] GLSL info log: "+foo.toString());
    }
  }

}
