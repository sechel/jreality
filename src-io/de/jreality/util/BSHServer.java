/*
 * Created on Apr 12, 2005
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
package de.jreality.util;

import java.awt.Component;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

import javax.swing.JMenuBar;

import de.jreality.math.Pn;
import de.jreality.reader.ReaderBSH;
import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;


/**
 *
 * This class opens a ServerSocket and accepts connections.
 * The received input is passed to a ReaderBSH instance.
 * This class implements LoadableScene, so it can be plugged into a viewer.
 *
 * TODO: stop doesn't work yet.
 * 
 * @author weissman
 *
 */
public class BSHServer implements LoadableScene {
    
    private ReaderBSH readerBSH;
    private int port;
    private volatile boolean running;
    private final Object finishMutex = new Object();

    private Runnable server = new Runnable() {
        public void run() {
            ServerSocket s = null;
            try {
                s = new ServerSocket(port);
                s.setSoTimeout(20);
            } catch (IOException ioe) {
              LoggingSystem.getLogger(BSHServer.this).log(Level.SEVERE, "socket create error", ioe);
              return;
            }
            Socket sock=null;
            while (running) {
                try {
                    try {
                        sock = s.accept();
                    } catch (SocketTimeoutException st) {
                        continue;
                    }
                    InputStream is = sock.getInputStream();
                    PrintStream ps = new PrintStream(sock.getOutputStream());
                    synchronized (readerBSH) {
                        try {
                            readerBSH.appendInput(Input.getInput("socket input stream", is));
                        } catch(Exception e) {
                            e.printStackTrace(ps);
                        } finally
                        {
                          ps.close();
                          sock.close();
                        }
                    }
                } catch (IOException ioe) {
                  LoggingSystem.getLogger(BSHServer.this).log(Level.SEVERE, "socket IO error", ioe);
                }
            }
            try {
                if (sock != null) sock.close();
                if (s != null) s.close();
            } catch (IOException e) {
                LoggingSystem.getLogger(BSHServer.this).log(Level.INFO, "diconnect failed", e);
            }
            synchronized(finishMutex) {
                finishMutex.notifyAll();
            }
        }
    };
    
    /**
     * create a bsh server listening to the given port
     * @param port the port to listen
     * @throws Exception if IO failes or bsh failes
     */
    public BSHServer(int port) throws Exception {
        readerBSH = new ReaderBSH();
        this.port = port;
        start();
    }
    
    private void start() {
      if (running) throw new IllegalStateException("already started");
      running = true;
      new Thread(server).start();
    }
    
    public void finish() {
        synchronized (finishMutex) {
            running = false;
            try {
                finishMutex.wait();
            } catch (InterruptedException e) {
                throw new Error();
            }
        }
    }

    public void setConfiguration(ConfigurationAttributes config) {
    }

    public SceneGraphComponent makeWorld() {
        return readerBSH.getComponent();
    }

    public int getSignature() {
        return Pn.EUCLIDEAN;
    }

    public void customize(JMenuBar menuBar, Viewer viewer) {
    }

    
}
