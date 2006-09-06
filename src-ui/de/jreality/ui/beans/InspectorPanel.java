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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;

import de.jreality.util.LoggingSystem;

/**
 * TODO: comment InspectorPanel
 */
public class InspectorPanel extends JPanel
{
	static {
	    PropertyEditorManager.registerEditor(Color.class, ColorEditor.class);
	    PropertyEditorManager.registerEditor(Paint.class, ColorEditor.class);
	    PropertyEditorManager.registerEditor(Boolean.class, BooleanEditor.class);
	    PropertyEditorManager.registerEditor(Double.class, DoubleEditor.class);
	    PropertyEditorManager.registerEditor(Integer.class, IntegerEditor.class);
	    PropertyEditorManager.registerEditor(Font.class, FontEditor.class);
//	    PropertyEditorManager.registerEditor(Transformation.class, TransformationEditor.class);
	  }

  private boolean reading;
  private Object currObject;
  static final HashMap <Class, InspectorPanel> CLASS_TO_PANEL = new HashMap<Class, InspectorPanel>();
  Class type;
  List <PropertyDescriptor> properties;
  List <PropertyEditor> editors;
  ChangeListener changeListener;//XXX hack

  HashSet currentProperties=new HashSet();
  
  public InspectorPanel()
  {
    super(new BorderLayout());
  }
  InspectorPanel(Class cl) throws IntrospectionException
  {
    if(cl==null) throw new NullPointerException("class==null");
    type=cl;
    editors=new ArrayList<PropertyEditor>();
    properties=new ArrayList<PropertyDescriptor>();
    setup();
  }
  private void setup() throws IntrospectionException
  {
      currentProperties.clear();
    BeanInfo bi=Introspector.getBeanInfo(type);
    PropertyDescriptor[] pd=bi.getPropertyDescriptors();
    setLayout(new GridBagLayout());
    GridBagConstraints label=new GridBagConstraints();
    label.anchor=GridBagConstraints.EAST;
    label.ipadx=3;
    GridBagConstraints editor=new GridBagConstraints();
    editor.fill=GridBagConstraints.BOTH;
    editor.anchor=GridBagConstraints.CENTER;
    editor.weightx=1;
    editor.gridwidth=GridBagConstraints.REMAINDER;
    for(int ix=0, num=pd.length; ix<num; ix++) try
    {
      PropertyDescriptor descriptor=pd[ix];
      final PropertyEditor pe=editor(descriptor);
      Component c=editorComponent(pe, descriptor);
      if(c==null) continue;
      JLabel l=new JLabel(descriptor.getDisplayName());
      l.setLabelFor(c);
      String sd = descriptor.getShortDescription();
      if(sd!=null&&!sd.equals(descriptor.getDisplayName()))
        l.setToolTipText(sd);
      properties.add(descriptor);
      editors.add(pe);
      this.add(l, label);
      this.add(c, editor);
      final Method m=descriptor.getWriteMethod();
      if(m!=null)
      {
        pe.addPropertyChangeListener(new PropertyChangeListener()
        {
          public void propertyChange(PropertyChangeEvent evt)
          {
            if(!reading) try
            {
              m.invoke(currObject, new Object[]{ pe.getValue() });
              if(changeListener!=null)
                changeListener.stateChanged(new ChangeEvent(currObject));
            } catch (IllegalArgumentException e)
            {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (IllegalAccessException e)
            {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (InvocationTargetException e)
            {
              // TODO Auto-generated catch block
              e.getTargetException().printStackTrace();
            }
          }
        });
      }
    } catch(Exception ex){
      ex.printStackTrace();
    }
    editor.weighty=1;
    this.add(new JLabel(), editor);
  }
  public void setObject(Object o)
  {
    if(o==currObject) return;
    currObject=o;
    if(type==null)
    {
      switchTo(o);
      return;
    }
    reading=true;
    try
    {
      if(!type.isInstance(o)) throw new IllegalArgumentException();
      for(int ix=0, n=properties.size(); ix<n; ix++)
      try
      {
        PropertyDescriptor pd=(PropertyDescriptor)properties.get(ix);
        Method m=pd.getReadMethod();
        if(m==null) continue;
        PropertyEditor pe=(PropertyEditor)editors.get(ix);
        pe.setValue(m.invoke(o));
      } catch(Exception ex){}//TODO
    } finally
    {
      reading=false;
    }
  }
  private void switchTo(Object o)
  {
    removeAll();
    if(o!=null)
    {
      Class clazz=o.getClass();
      Object p=CLASS_TO_PANEL.get(clazz);
      InspectorPanel ip;
      if(p!=null) ip=(InspectorPanel)p;
      else try
      {
        CLASS_TO_PANEL.put(clazz, ip=new InspectorPanel(clazz));
        ip.changeListener=changeListener;
      } catch (IntrospectionException e)
      {
        e.printStackTrace();
        return;
      }
      try
      {
        ip.setObject(o);
        this.add(ip, BorderLayout.CENTER);
      } catch(Exception ex)
      {
        ex.printStackTrace();
      }
    }
    revalidate();
    repaint();
  }
  private PropertyEditor editor(PropertyDescriptor descriptor)
  {
    Class edcl=descriptor.getPropertyEditorClass();
    PropertyEditor pe=null;
    if(edcl!=null) try
    {
      pe=(PropertyEditor)edcl.newInstance();
    } catch(Exception ex){
      ex.printStackTrace();
    }
    if(pe==null) { 
      try {
        pe=PropertyEditorManager.findEditor(descriptor.getPropertyType());
      } catch (Exception e) {
        LoggingSystem.getLogger(this).info("Exception in findEditor for property="+descriptor.getName()+" type="+descriptor.getPropertyType());
      }
    }
    return pe;
  }
  
  private Component editorComponent(PropertyEditor pe, PropertyDescriptor pd)
  {
    if(pe==null) return null;
    final boolean editable=pd.getWriteMethod()!=null;
    Component c;
    if(pe.supportsCustomEditor())
    {
      c=pe.getCustomEditor();
      if(c instanceof JTextComponent)
        ((JTextComponent)c).setEditable(editable);
      else c.setEnabled(editable);
    }
    else
    {
      String[] tags=pe.getTags();
      if(tags!=null) c=choices(pe, tags, editable);
      else c=textual(pe, editable);
    }
    String sd = pd.getShortDescription();
    if(sd!=null&&!sd.equals(pd.getDisplayName())&&
      (c instanceof JComponent))
      ((JComponent)c).setToolTipText(sd);
    return c;
  }
  private Component textual(final PropertyEditor pe, boolean editable)
  {
    final JTextField tf=new JTextField();
    pe.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        Object o=evt.getNewValue();
        if(o==null) o=pe.getValue();
        tf.setText(o==null? "": o.toString());
      }
    });
    tf.setEditable(editable);
    if(editable) tf.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        pe.setAsText(tf.getText());
      }
    });
    return tf;
  }
  private Component choices(final PropertyEditor pe, String[] tags, boolean editable)
  {
    final JComboBox cb=new JComboBox(tags);
    pe.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        String o=pe.getAsText();
        cb.setSelectedItem(o==null? "": o);
      }
    });
    cb.setEditable(false);
    cb.setEnabled(editable);
    if(editable) cb.getModel().addListDataListener(new ListDataListener()
    {
      public void contentsChanged(ListDataEvent e)
      {
        if(Math.min(e.getIndex0(), e.getIndex1())<0)
          pe.setAsText((String)cb.getSelectedItem());
      }
      public void intervalAdded(ListDataEvent e) {}      
      public void intervalRemoved(ListDataEvent e) {}
    });
    return cb;
  }

  public void addChangeListener(ChangeListener listener) {
    changeListener=ChangeEventMulticaster.add(changeListener, listener);
  }
  public void removeChangeListener(ChangeListener listener) {
    changeListener=ChangeEventMulticaster.remove(changeListener, listener);
  }

}
