/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended for
 * use in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

/*
 * @(#)SampleTree.java	1.22 03/01/23
 */

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import com.jcraft.weirdx.WeirdX;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.swing.JFakeFrame;
import de.jreality.ui.viewerapp.ViewerApp;


public class WeirdXJR
{
    //
    protected JFakeFrame frame;
    //protected JFrame frame;

    protected WeirdX weirdx;
    
    public WeirdXJR() {
	
    weirdx = new WeirdX();
    
    frame = new JFakeFrame("Weird");
//    frame = new JFrame("Weird");
    frame.setSize(600,800);
	frame.getContentPane().add("Center", weirdx);
    wxInit(weirdx);

    System.out.println(weirdx);
//	frame.setJMenuBar(menuBar);
	frame.setBackground(Color.lightGray);



	frame.addWindowListener( new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {System.exit(0);}});

	//frame.pack();
    frame.validate();
	frame.setVisible(true);
    }

 

    static public void main(String args[]) {
	WeirdXJR wjr = new WeirdXJR();
    JFakeFrame frame = wjr.frame;
    CatenoidHelicoid catenoid = new CatenoidHelicoid(50);
    //catenoid.setAlpha(Math.PI/2.-0.3);
    
    SceneGraphComponent catComp= new SceneGraphComponent();
    Transformation gt= new Transformation();

    catComp.setTransformation(gt);
    catComp.setGeometry(catenoid);
    
    
    catComp.addTool(frame.getTool());
    
 System.out.print("setting appearance ");
    catComp.setAppearance(frame.getAppearance());
    System.out.println("done");
    ViewerApp.display(catComp);
    frame.setVisible(true);
    wjr.weirdx.start();
    }

    
    private void wxInit(Applet wx) {
        String s;
        WeirdX weirdx=new WeirdX();

        Properties props=new Properties();
        try{
          InputStream rs = null;
          /* accept a command line argument of a URL from which to get
             properties. This is required because of a bug in netscape, where
             it refuses to properly load resources in certain cases */
         
           rs = weirdx.getClass().getResourceAsStream("/props");
          
          if(rs!=null)
            props.load(rs);
        }
        catch(Exception e){ 
          //System.err.println(e);
        }

        try{
          String root=props.getProperty("user.dir", null);
          File guess=new File(new File(root, "config"), "props");
          props.load(new FileInputStream(guess));
        }
        catch(Exception e){ 
          //System.err.println(e);
        }

        Properties sprops=null;
        try{
          sprops=System.getProperties();
        } 
        catch (Exception e) {
          System.err.println("Unable to read system properties: "+e);
          sprops=new Properties();
        }
        for(Enumeration e=props.keys() ; e.hasMoreElements() ;) {
          String key=(String)(e.nextElement());
          //if(key.startsWith("weirdx.") && sprops.get(key)==null){
          //  System.setProperty(key, (String)(props.get(key)));
          //}
          if(key.startsWith("weirdx.") && sprops.get(key)==null){
            sprops.put(key, (String)(props.get(key)));
          }
        }

        try{
          System.setProperties(sprops);
          props=System.getProperties();
        }
        catch (Exception e) {
          System.err.println("Error updating system properties: "+e);
        }

        final Properties fprops = props;
        final AppletContext ac = new AppletContext() {

            public AudioClip getAudioClip(URL url) {
                return null;
            }

            public Image getImage(URL url) {
                return null;
            }

            public Applet getApplet(String name) {
                return null;
            }

            public Enumeration getApplets() {
                return null;
            }

            public void showDocument(URL url) {
                
            }

            public void showDocument(URL url, String target) {
                
            }

            public void showStatus(String status) {
                
            }

            public void setStream(String key, InputStream stream) throws IOException {
                
            }

            public InputStream getStream(String key) {
                return null;
            }

            public Iterator getStreamKeys() {
                return null;
            }
            
        };
        final AppletStub as = new AppletStub() {

            public boolean isActive() {
                return true;
            }

            public URL getDocumentBase() {
                return null;
            }

            public URL getCodeBase() {
                return null;
            }

            public String getParameter(String name) {
              System.out.println("Param="+name+" value="+fprops.getProperty(name));
                return fprops.getProperty(name);
            }

            public AppletContext getAppletContext() {
                return ac;
            }

            public void appletResize(int width, int height) {
            }
            
        };
       wx.setStub(as);
       wx.init();
    }
}
