package de.jreality.shader;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jreality.util.Input;

/**
 * program parsing is very poor!!
 * 
 * @author gollwas
 *
 */
public class GlslSource implements Serializable {

  private String[] vertexProgs;
  private String[] fragmentProgs;
  
  private final HashMap uniforms = new HashMap();
  private final Collection UNIFORMS = Collections.unmodifiableCollection(uniforms.values());
  
  private static final Pattern pattern = Pattern.compile(
    "^[\\w]*uniform[\\s]+([\\w]+)[\\s]+([\\w,\t ]+)[\\s]*\\[?[\\s]*([0-9]*)[\\s]*\\]?[\\s;]+", Pattern.MULTILINE
  );
  
  public GlslSource(Input vertexProgram, Input fragmentProgram) throws IOException {
      vertexProgs = vertexProgram == null ? null : new String[]{readString(vertexProgram)};
      fragmentProgs = fragmentProgram == null ? null : new String[]{readString(fragmentProgram)};
      extractUniforms();
  }
  
  public GlslSource(String vertexProgram, String fragmentProgram) {
    this.vertexProgs = vertexProgram == null ? null : new String[]{new String(vertexProgram)};
    this.fragmentProgs = fragmentProgram == null ? null : new String[]{new String(fragmentProgram)};
    extractUniforms();
  }

  public GlslSource(String[] vertexProgram, String[] fragmentProgram) {
    this.vertexProgs = vertexProgram;
    this.fragmentProgs = fragmentProgram;
    extractUniforms();
  }
  
  private void extractUniforms() {
    if (vertexProgs != null) for (int i = 0; i < vertexProgs.length; i++)
      extractUniforms(vertexProgs[i]);
    if (fragmentProgs != null) for (int i = 0; i < fragmentProgs.length; i++)
      extractUniforms(fragmentProgs[i]);
  }
  
  private void extractUniforms(String prog) {
    Matcher m = pattern.matcher(prog);
    while (m.find()) {
      String type = m.group(1);
      int arrayLen = -1;
      try {
        arrayLen = Integer.parseInt(m.group(3));
      } catch (Exception e) {}
      String nameStr = m.group(2);
      String[] names = nameStr.split(", ");
      for (int i = 0; i < names.length; i++) {
        UniformParameter param = new UniformParameter(names[i], type, arrayLen);
        uniforms.put(names[i], param);
        //System.out.println("found "+param);
      }
    }
  }

  public Collection getUniformParameters() {
    return UNIFORMS;
  }
  
  public UniformParameter getUniformParameter(String name) {
    return (UniformParameter) uniforms.get(name);
  }
  
  public String[] getVertexProgram() {
    return vertexProgs;
  }
  
  public String[] getFragmentProgram() {
    return fragmentProgs;
  }
 
  private static String readString(Input input) throws IOException {
    StringBuffer sb = new StringBuffer();
    char[] buf = new char[255];
    Reader r = input.getReader();
    int read = -1;
    while ((read = r.read(buf)) != -1)
      sb.append(buf, 0, read);
    return sb.toString();
  }
  
  public class UniformParameter {
    private final String name, type;
    private final boolean isArray;
    private final boolean isMatrix;
    private final Class primitiveType;
    private final int primitiveSize;
    private final int dataSize;
    private final int arrayLength;
    private final String stringRep;
    
    private UniformParameter(String name, String type, int arrayLen) {
      this.name=name;
      this.type=type;
      this.isArray=arrayLen > 0;
      this.isMatrix=type.startsWith("mat");
      this.arrayLength = arrayLen;
      int s=1;
      try {
        s = Integer.parseInt(type.substring(type.length()-1));
      } catch (Exception e) {}
      primitiveSize = s;
      primitiveType = (type.startsWith("i") || type.startsWith("sampler")) ? int.class : float.class;
      int prDataSize = isMatrix ? primitiveSize*primitiveSize : primitiveSize;
      dataSize = isArray ? arrayLen * prDataSize : prDataSize;
      StringBuffer sb = new StringBuffer("glUniform");
      if (isMatrix()) sb.append("Matrix");
      sb.append(getPrimitiveSize());
      sb.append(getPrimitiveType() == float.class ? 'f' : 'i');
      sb.append('v');
      sb.append("ARB");
      stringRep = sb.toString();
   }
    
    public boolean isArray() {
      return isArray;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public boolean isMatrix() {
      return isMatrix;
    }
    
    public int getPrimitiveSize() {
      return primitiveSize;
    }
    
    public int getDataSize() {
      return dataSize;
    }
    
    public Class getPrimitiveType() {
      return primitiveType;
    }
    
    public int getArrayLength() {
      if (isArray) return arrayLength;
      throw new IllegalStateException("no array");
    }
    
    public String toString() {
      return "uniform: "+type+" "+name+" [array="+isArray+" matrix="+isMatrix+" primitive="+primitiveType+" size="+primitiveSize+"]";
    }

	public String getStringRep() {
		// TODO Auto-generated method stub
		return stringRep;
	}
  }
  
  public static void main(String[] args) throws Exception {
    GlslSource source = new GlslSource(
        Input.getInput("de/jreality/jogl/shader/resources/brick.vert"),
        Input.getInput("de/jreality/jogl/shader/resources/brick.frag")
    );
  }
}
