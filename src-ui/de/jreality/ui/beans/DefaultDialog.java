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


package de.jreality.ui.beans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class DefaultDialog extends JDialog 
{

    public static int BUTTON_NONE = 0;
    public static int BUTTON_OKAY = 1;
    public static int BUTTON_APPLY = 2;
    public static int BUTTON_CANCEL = 4;
    public static int BUTTON_CLOSE = 8;

    //--- Data field(s) ---

    //protected static Logger logger = Logger.getLogger(DefaultDialog.class);

    protected ApplyAction acApply = new ApplyAction();
    protected CancelAction acCancel = new CancelAction();
    protected CloseAction acClose = new CloseAction();
    protected OkayAction acOkay = new OkayAction();
    protected boolean isOkay = false;
    protected JPanel jpButtons;
    protected JPanel jpMain;

    //--- Constructor(s) ---

    public DefaultDialog(int buttons, JComponent component, 
       boolean exitOnEnter)
    {
  jpMain = new JPanel();

  if (component != null) {
      setMainComponent(component);
  }

  jpButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

  if ((buttons & BUTTON_OKAY) > 0) {
      JButton jbOkay = new JButton(acOkay);
      jpButtons.add(jbOkay);
  }
  if ((buttons & BUTTON_CLOSE) > 0) {
      jpButtons.add(new JButton(acClose));
  }
  if ((buttons & BUTTON_APPLY) > 0) {
      jpButtons.add(new JButton(acApply));
  }
  if ((buttons & BUTTON_CANCEL) > 0) {
      JButton jbCancel = new JButton(acCancel);
      jpButtons.add(jbCancel);
  }

  JPanel jpFrame = new JPanel(new BorderLayout());
  jpFrame.setBorder(new EmptyBorder(5, 5, 5, 5));
  jpFrame.add(jpMain, BorderLayout.CENTER);
  jpFrame.add(jpButtons, BorderLayout.SOUTH);

  getContentPane().setLayout(new BorderLayout());
  getContentPane().add(jpFrame, BorderLayout.CENTER);

  setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new CloseListener());

  if (component != null) {
      pack();
  }
    }

    public DefaultDialog(int buttons, JComponent component)
    {
  this(buttons, component, true);
    }

    public DefaultDialog(int buttons)
    {
  this(buttons, null);
    }
    
    public DefaultDialog(int buttons, boolean exitOnEnter)
    {
  this(buttons, null, exitOnEnter);
    }

    public DefaultDialog()
    {
  this(BUTTON_OKAY | BUTTON_CANCEL);
    }

    //--- Method(s) ---

    /**
     * Dialog is closed, if true is returned.
     */
    public void apply()
    {
    }

    public void close()
    {
  dispose();
    }

    public JPanel getButtonPanel()
    {
  return jpButtons;
    }

    public void setMainComponent(Component component)
    {
  getMainPanel().setLayout(new BorderLayout());
  getMainPanel().add(component, BorderLayout.CENTER);
    }
    
    public JPanel getMainPanel()
    {
  return jpMain;
    }

    public boolean isOkay()
    {
  return isOkay;
    }

    public void show(Component c)
    {
  if (c != null) {
      setLocationRelativeTo(c);
  }
  show();
    }

    /**
     * Handles the apply button.
     */
    private class ApplyAction extends AbstractAction {

        public ApplyAction() 
  {
      putValue(Action.NAME, XNap.tr("Apply"));
      putValue(Action.SHORT_DESCRIPTION, XNap.tr("Applies changes."));
  }
  
        public void actionPerformed(ActionEvent event) 
  {
      try {
    apply();
      }
      catch (IllegalArgumentException e) {
    JOptionPane.showMessageDialog
        (DefaultDialog.this, e.getMessage(), 
         XNap.tr("Illegal Value"), JOptionPane.ERROR_MESSAGE);
      }
        }
  
    }

    /**
     * Handles the Cancel button.
     */
    private class CancelAction extends AbstractAction {

        public CancelAction()
  {
            putValue(Action.NAME, XNap.tr("Cancel"));
            putValue(Action.SHORT_DESCRIPTION, XNap.tr("Closes the dialog without saving changes."));
  }

        public void actionPerformed(ActionEvent event) 
  {
      close();
  }
  
    }

    /**
     * Handle the Close button.
     */
    private class CloseAction extends AbstractAction {

        public CloseAction() 
  {
      putValue(Action.NAME, XNap.tr("Close"));
      putValue(Action.SHORT_DESCRIPTION, XNap.tr("Closes the dialog."));
  }
  
        public void actionPerformed(ActionEvent event) 
  {
      close();
        }
  
    }

    /**
     * Handle the Okay button.
     */
    private class OkayAction extends AbstractAction {

        public OkayAction() 
  {
      putValue(Action.NAME, XNap.tr("OK"));
      putValue(Action.SHORT_DESCRIPTION, XNap.tr("Closes the dialog saving changes."));
      putValue(Action.MNEMONIC_KEY, new Integer('O'));
  }
  
        public void actionPerformed(ActionEvent event) 
  {
      try {
    apply();
    isOkay = true;
    close();
      }
      catch (IllegalArgumentException e) {
    JOptionPane.showMessageDialog
        (DefaultDialog.this, e.getMessage(), 
         XNap.tr("Illegal Value"), JOptionPane.ERROR_MESSAGE);
      }
        }
  
    }

    private class CloseListener extends WindowAdapter
    {
  public void windowClosing (java.awt.event.WindowEvent evt) 
  {
      close();
  }
    }

}