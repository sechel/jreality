/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.swing;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.PaintEvent;
import java.awt.im.InputMethodHighlight;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.ContainerPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.WindowPeer;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

class FakeToolKit extends Toolkit {
	
    private static final boolean DUMP = false;

    private static  FakeToolKit ftk = new FakeToolKit();
    public static Toolkit getDefaultToolkit() {
        return ftk;
    }
    private Toolkit tk = Toolkit.getDefaultToolkit();
    public FakeToolKit() {
        super();
        // TODO Auto-generated constructor stub
    }

    public int getScreenResolution() throws HeadlessException {
        return tk.getScreenResolution();
    }

    public void beep() {
        tk.beep();

    }

    public void sync() {
        tk.sync();
    }

    public Dimension getScreenSize() throws HeadlessException {
        return tk.getScreenSize();
    }

    protected EventQueue getSystemEventQueueImpl() {
        return tk.getSystemEventQueue();
    }

    public Image createImage(byte[] imagedata, int imageoffset, int imagelength) {
        return tk.createImage(imagedata,imageoffset,imagelength);
    }

    public Clipboard getSystemClipboard() throws HeadlessException {
        return tk.getSystemClipboard();
    }

    public ColorModel getColorModel() throws HeadlessException {
        return tk.getColorModel();
    }

    public String[] getFontList() {
        return tk.getFontList();
    }

    public FontMetrics getFontMetrics(Font font) {
        return tk.getFontMetrics(font);
    }

    public Image createImage(ImageProducer producer) {
        return tk.createImage(producer);
    }

    public Image createImage(String filename) {
        return tk.createImage(filename);
    }

    public Image getImage(String filename) {
        return tk.getImage(filename);
    }

    public Image createImage(URL url) {
        return tk.createImage(url);
    }

    public Image getImage(URL url) {
        return tk.getImage(url);
    }

    public DragSourceContextPeer createDragSourceContextPeer(
            DragGestureEvent dge) throws InvalidDnDOperationException {
        return tk.createDragSourceContextPeer(dge);
    }

    public int checkImage(Image image, int width, int height,
            ImageObserver observer) {
        return tk.checkImage(image,width,height,observer);
    }

    public boolean prepareImage(Image image, int width, int height,
            ImageObserver observer) {
        return tk.prepareImage(image,width,height,observer);
    }

    protected ButtonPeer createButton(Button target) throws HeadlessException {
        return null;
    }

    protected CanvasPeer createCanvas(Canvas target) {
        return null;
    }

    protected CheckboxMenuItemPeer createCheckboxMenuItem(
            CheckboxMenuItem target) throws HeadlessException {
        return null;
    }

    protected CheckboxPeer createCheckbox(Checkbox target)
            throws HeadlessException {
        return null;
    }

    protected ChoicePeer createChoice(Choice target) throws HeadlessException {
        return null;
    }

    protected DialogPeer createDialog(Dialog target) throws HeadlessException {
        return null;
    }

    protected FileDialogPeer createFileDialog(FileDialog target)
            throws HeadlessException {
        return null;
    }

    protected FontPeer getFontPeer(String name, int style) {
        return null;
    }

    protected FramePeer createFrame(Frame target) throws HeadlessException {
        if (DUMP) System.out.println("new Frame peer");
        return new FakeFramePeer(target) ;
        }

    protected LabelPeer createLabel(Label target) throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected ListPeer createList(List target) throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected MenuBarPeer createMenuBar(MenuBar target)
            throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected MenuItemPeer createMenuItem(MenuItem target)
            throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected MenuPeer createMenu(Menu target) throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected PanelPeer createPanel(Panel target) {
        // TODO Auto-generated method stub
        return null;
    }

    protected PopupMenuPeer createPopupMenu(PopupMenu target)
            throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected ScrollPanePeer createScrollPane(ScrollPane target)
            throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected ScrollbarPeer createScrollbar(Scrollbar target)
            throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected TextAreaPeer createTextArea(TextArea target)
            throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected TextFieldPeer createTextField(TextField target)
            throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    protected WindowPeer createWindow(Window target) throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    public Map mapInputMethodHighlight(InputMethodHighlight highlight)
            throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    public PrintJob getPrintJob(Frame frame, String jobtitle, Properties props) {
        // TODO Auto-generated method stub
        return null;
    }

    public class FakeFramePeer implements FramePeer {
        private BufferedImage bi;
        private Frame frame;
        private Runnable repaintAction;
        Rectangle bounds;
        
        FakeFramePeer(Frame f) {
            frame = f;
            Dimension d = f.getSize();
            //TODO this is ugly probably it is better to not crate the image
            // and check on existence in the getGraphics etc.
            if(d.width==0) d.width=1;
            if(d.height==0) d.height=1;
            bounds = new Rectangle(d.width, d.height);
            bi= new BufferedImage(d.width,d.height,BufferedImage.TYPE_INT_ARGB);
        }
        public int getState() {
            return 0;
        }

        public void setState(int state) {
        }

        public void setResizable(boolean resizeable) {
        }

        public void setIconImage(Image im) {
        }

        public void setMenuBar(MenuBar mb) {
        }

        public void setMaximizedBounds(Rectangle bounds) {
        }

        public void setTitle(String title) {
        }

        public void toBack() {
        }

        public void toFront() {
        }

        public void beginLayout() {
        }

        public void beginValidate() {
        }

        public void endLayout() {
            if (DUMP) System.err.println("JFakeFramePeer end layout");
            frame.paint(bi.getGraphics());
            if(repaintAction != null)
                repaintAction.run();
        }

        public void endValidate() {
          if (DUMP) System.err.println("JFakeFramePeer end validate");
            frame.paint(bi.getGraphics());
            if(repaintAction != null)
                repaintAction.run();

        }

        public boolean isPaintPending() {
            return false;
        }

        public Insets getInsets() {
            return null;
        }

        public Insets insets() {
            return new Insets(0,0,0,0);
        }

        public void destroyBuffers() {
        }

        public void disable() {
        }

        public void dispose() {
        }

        public void enable() {
        }

        public void hide() {
        }

        public void show() {
        }

        public void updateCursorImmediately() {
        }

        public boolean canDetermineObscurity() {
            return false;
        }

        public boolean handlesWheelScrolling() {
            return true;
        }

        public boolean isFocusable() {
            return false;
        }

        public boolean isObscured() {
            return false;
        }

        public void reshape(int x, int y, int width, int height) {
          if (DUMP) System.err.println("JFakeFramePeer reshape");
        }

        public void setBounds(int x, int y, int width, int height) {
          if (DUMP) System.out.println("JFakeFrame set Bounds "+x+" "+y+" "+width+" "+height);
            bounds.setBounds(x, y, width, height);
            if(bi.getWidth()!=width || bi.getHeight()!= height) {
                bi =new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            }
        }

        public void repaint(long tm, int x, int y, int width, int height) {
          if (DUMP) System.err.println("JFakeFramePeer repaint");
        }

        public void setEnabled(boolean b) {
        }

        public void setVisible(boolean b) {
        }

        public void handleEvent(AWTEvent e) {
            //System.out.println("JFakeFramePeer handle event "+e);
        }

        public void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException {
        }

        public void flip(FlipContents flipAction) {
        }

        public void setBackground(Color c) {
        }

        public void setForeground(Color c) {
        }

        public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time) {
            return false;
        }

        public Dimension getMinimumSize() {
            return new Dimension(0,0);
        }

        public Dimension getPreferredSize() {
            return new Dimension(256,256);
        }

        public Dimension minimumSize() {
            return getMinimumSize();
        }

        public Dimension preferredSize() {
            return getPreferredSize();
        }

        public void setFont(Font f) {
        }

        public Graphics getGraphics() {
           // System.err.println("JFakeFramePeer getGraphics");
            //TODO why does this work???
            // repaint BEFORE graphics is handed away!
            // Turns out: Does not Work on 1.5: so we "invokeLater"...
            // gives a little performance penalty but what can we do?
            if(repaintAction != null)
                //repaintAction.run();
                EventQueue.invokeLater(repaintAction);
            return bi.createGraphics();
        }

        public void paint(Graphics g) {
            g.drawImage(bi,0,0,null);
        }

        public void print(Graphics g) {
            g.drawImage(bi,0,0,null);
        }

        public GraphicsConfiguration getGraphicsConfiguration() {
            return null;
        }

        public Image getBackBuffer() {
          if (DUMP) System.out.println("FakeFramePeer.getBackBuffer()");
          return null;
        }

        public Image createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        public Point getLocationOnScreen() {
            return new Point(0,0);
        }

        public Toolkit getToolkit() {
            return FakeToolKit.getDefaultToolkit();
        }

        public void coalescePaintEvent(PaintEvent e) {
          if (DUMP) System.err.println("JFakeFramePeer coalescePaintEvent");
            frame.paint(bi.getGraphics());
            if(repaintAction != null)
                repaintAction.run();
        }

        public ColorModel getColorModel() {
            return ColorModel.getRGBdefault();
        }

        public VolatileImage createVolatileImage(int width, int height) {
            return null;
        }

        public FontMetrics getFontMetrics(Font font) {
            return Toolkit.getDefaultToolkit().getFontMetrics(font);
        }

        public Image createImage(ImageProducer producer) {
            return Toolkit.getDefaultToolkit().createImage(producer);
        }

        public int checkImage(Image img, int w, int h, ImageObserver o) {
            return 0;
        }

        public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
            return false;
        }
        
        public BufferedImage getRootImage() {
            return bi;
        }
        public void setRepaintAction(Runnable r) {
            repaintAction = r;
        }

        public void setBoundsPrivate(int x, int y, int width, int height) {
          if (DUMP) System.out.println("FakeFramePeer.setBoundsPrivate()");
        }
        public void updateAlwaysOnTop() {
        }
        public boolean requestWindowFocus() {
          return true;
        }
        public void cancelPendingPaint(int x, int y, int w, int h) {
        }
        public void restack() {
        }
        public boolean isRestackSupported() {
          return false;
        }
        public void setBounds(int x, int y, int width, int height, int op) {
          if (DUMP) System.out.println("FakeFramePeer.setBounds() op="+op);
          setBounds(x, y, width, height);
        }
        public void reparent(ContainerPeer newContainer) {
        }
        public boolean isReparentSupported() {
          return false;
        }
        public void layout() {
        }
        public Rectangle getBounds() {
          return bounds;
        }
        public void updateFocusableWindowState() {
          // TODO Auto-generated method stub
          
        }

    };

    
}
