/*
 * Created on 12.02.2006
 *
 * This file is part of the de.jreality.swing package.
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
package de.jreality.swing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.RepaintManager;

import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;

public class JFakeFrame extends JFrame {
    private static final long serialVersionUID = 3258688793266958393L;
    MouseEventTool tool;
    BufferedImage bufferedImage;
    RepaintManager oldRM;
    HashSet comps = new HashSet();
    private Graphics2D graphics;

    Component current = null;
    Appearance appearance;
    private Texture2D tex;
    private String praefix = "polygonShader";

    public JFakeFrame() throws HeadlessException {
        super();
        init();
    }
    
    private void init() {
        

        tool = new MouseEventTool(this);
              
              appearance = new Appearance();
              appearance.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
              appearance.setAttribute(CommonAttributes.EDGE_DRAW, false);
              appearance.setAttribute(CommonAttributes.TUBES_DRAW, false);
              System.out.println(RepaintManager.currentManager(this));
              
    }
    public void addNotify() {
        super.addNotify();
        ((FakeToolKit.FakeFramePeer)getPeer()).setRepaintAction(new Runnable() {

            public void run() {
                fire();
            }
            
        });

        //fire();
    }
    public void setVisible(boolean b) {
        super.setVisible(b);
        fire();
    }
    private void fire() {
 //           System.out.println("fire");
        FakeToolKit.FakeFramePeer peer = (FakeToolKit.FakeFramePeer)getPeer();
        if(peer != null) {
            bufferedImage = peer.getRootImage();
            graphics = bufferedImage.createGraphics();
            tool.setSize(getWidth(),getHeight());
            if(graphics != null) {
                graphics.setColor(getBackground());
                graphics.fillRect(0, 0, getWidth(),getHeight());
                paint(graphics);
//                printAll(graphics);
                ImageData img = new de.jreality.shader.ImageData(bufferedImage);
                if(appearance != null) {
//                    System.err.print("set...");
                    if(tex == null)
                        tex = TextureUtility.createTexture(appearance, praefix ,img);
                    else tex.setImage(img);
//                    System.err.println(". texture "+bufferedImage.getWidth());
                }
            }
        }
    }
    
    public MouseEventTool getTool() {
        return tool;
    }

    public Appearance getAppearance() {
        return appearance;
    }
    
    public JFakeFrame(GraphicsConfiguration gc) {
        super(gc);
        init();
    }

    public JFakeFrame(String title) throws HeadlessException {
        super(title);
        init();
    }

    public JFakeFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        init();
    }

    public Toolkit getToolkit() {
        return FakeToolKit.getDefaultToolkit();
    }

}
