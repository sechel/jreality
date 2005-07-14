/*
 * Created on Apr 11, 2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.reader;

import java.beans.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Policy;
import java.security.SecureClassLoader;
import java.util.LinkedList;
import java.util.logging.Level;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SimpleURLPolicy;

/**
 *
 * Reader for bsh script files. It creates an bsh.Interpreter 
 * instance, see www.beanshell.org for details. In this instance
 * there is a SceneGraphComponent registered as "root", to which the script
 * should attatch the scene parts. Example script:
 * 
 * <pre>
 * a=new Appearance();
 * a.setAttribute("plogonShader.diffuseColor", new java.awt.Color(77,66,44,72));
 * root.setAppearance(a);
 * root.setGeometry(new CatenoidHelicoid(10));
 * </pre>
 *
 * <b>For security reasons, this reader sets up a SecurityManager
 * and a restricting Policy for the codebase of the bsh.jar URL.
 * For this to work the bsh jar must NOT be in the classpath. 
 * Pass the location of the bsh.jar file as System property, i.e.:
 * <pre>
 * -Djreality.bsh.jar=file:///opt/jars/bsh-1.3b2.jar
 * </pre>
 * </b>
 * 
 * @author weissman
 *
 */
public class ReaderBSH implements SceneReader {

  private static final SecureClassLoader bshLoader;
  private static final SimpleURLPolicy bshPolicy;
  
  static {
    try {
      URL bshURL=new URL(System.getProperty("jreality.bsh.jar"));
      System.out.println(bshURL);
      bshLoader = new URLClassLoader(new URL[]{bshURL}, Thread.currentThread().getContextClassLoader());
      LinkedList pc = new LinkedList();
//      pc.add(new java.lang.RuntimePermission("getClassLoader"));
//      pc.add(new java.lang.RuntimePermission("accessDeclaredMembers"));
//      pc.add(new java.util.PropertyPermission("*", "read"));
//      pc.add(new java.io.FilePermission("./-", "read"));
//      pc.add(new java.net.SocketPermission("*:1024-", "listen,accept"));
      pc.add(new AllPermission());
      bshPolicy = new SimpleURLPolicy(pc, bshURL);
      Policy.setPolicy(bshPolicy);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }
  
  private Object bsh;
  private SceneGraphComponent root;

  public ReaderBSH() throws Exception {
    System.setSecurityManager(new SecurityManager());
    root = new SceneGraphComponent();
    root.setName("BSHroot");
    bsh = bshLoader.loadClass("bsh.Interpreter").getConstructor(
        new Class[]{Reader.class, PrintStream.class, PrintStream.class, boolean.class}
        ).newInstance(new Object[]{null, System.out, System.err, Boolean.TRUE});
    exec(bsh, "eval", "import de.jreality.scene.*");
    exec(bsh, "eval", "import de.jreality.geometry.*");
    exec(bsh, "eval", "import de.jreality.scene.data.*");
    exec(bsh, "set", new Object[] { "root", root });
  }

  public void setInput(Input input) throws IOException {
    try {
      processReader(input.getReader(), null, null);
    } catch (Exception e) {
      LoggingSystem.getLogger(this).log(Level.SEVERE, "eval failed", e);
    }

  }

  /**
   * process the given Input in the current Instance.
   * @param input the Input to process
   * @throws IOException
   */
  public void appendInput(Input input) throws IOException {
    setInput(input);
  }

  public void processReader(Reader in, PrintStream out, PrintStream err) throws Exception {
    exec(bsh, "setOut", out);
    exec(bsh, "setErr", err);
    exec(bsh, "eval", in);
  }

  public SceneGraphComponent getComponent() {
    return root;
  }

  private static void exec(Object target, String methodName, Object arg) throws Exception {
    exec(target, methodName, new Object[]{arg});
  }

  private static void exec(Object target, String methodName, Object[] args) throws Exception {
    Statement s = new Statement(target, methodName, args);
    s.execute();
  }

}
