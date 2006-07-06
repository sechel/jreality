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


package de.jreality.toolsystem.config;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Attr;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import de.jreality.util.Input;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class ToolSystemConfiguration {
  
  public static ToolSystemConfiguration loadDefaultConfiguration() {
    try {
      return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig.xml")));
    } catch (IOException e) {
      throw new Error();
    }
  }
  
  public static ToolSystemConfiguration loadDefaultDesktopAndPortalConfiguration() throws IOException {
    List all = new LinkedList();
    all.add(loadDefaultPortalConfiguration());
    all.add(loadDefaultDesktopConfiguration());
    return merge(all);
  }

  public static ToolSystemConfiguration loadDefaultDesktopConfiguration() throws IOException {
    return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig.xml")));
  }
  
  public static ToolSystemConfiguration loadDefaultDesktopConfiguration(List additionalInputs) throws IOException {
    if (additionalInputs.isEmpty()) return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig.xml")));
    List all = new LinkedList();
    all.add(loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig.xml"))));
    all.add(loadConfiguration(additionalInputs));
    return merge(all);
  }

  public static ToolSystemConfiguration loadDefaultPortalConfiguration() throws IOException {
    return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig-portal.xml")));
  }
  
  public static ToolSystemConfiguration loadDefaultPortalConfiguration(List additionalInputs) throws IOException {
    if (additionalInputs.isEmpty()) return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig-portal.xml")));
    List all = new LinkedList();
    all.add(loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig.xml"))));
    all.add(loadConfiguration(additionalInputs));
    return merge(all);
  }

  public static ToolSystemConfiguration loadConfiguration(Input xmlFile) throws IOException {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Input xslt = Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig.xsl"));
    DOMResult outResult = new DOMResult();    
    try {
      Transformer transformer = tfactory.newTransformer(new StreamSource(xslt.getInputStream()));
      transformer.transform(new StreamSource(xmlFile.getInputStream()), outResult);
    } catch (TransformerException e) {
      e.printStackTrace();
    }
    XMLDecoder dec = new XMLDecoder(domToInputStream(outResult.getNode()), null,
        new ExceptionListener() {
        public void exceptionThrown(Exception e) {
          e.printStackTrace();
        }
      });
    ToolSystemConfiguration tsc = (ToolSystemConfiguration) dec.readObject();
    return tsc;
  }
  
  public static ToolSystemConfiguration loadConfiguration(List inputs) throws IOException {
    List confs = new LinkedList();
    for (Iterator i = inputs.iterator(); i.hasNext(); ) {
      Input in = (Input) i.next();
      confs.add(loadConfiguration(in));
    }
    return merge(confs);
  }
  
  private static ToolSystemConfiguration merge(List list) {
    ToolSystemConfiguration result = new ToolSystemConfiguration();
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      ToolSystemConfiguration conf = (ToolSystemConfiguration) i.next();
      result.rawConfigs.addAll(conf.rawConfigs);
      result.rawMappings.addAll(conf.rawMappings);
      result.virtualConfigs.addAll(conf.virtualConfigs);
      result.virtualConstants.addAll(conf.virtualConstants);
      result.virtualMappings.addAll(conf.virtualMappings);
    }
    return result;
  }

  private List rawConfigs = new LinkedList();
  private List rawMappings = new LinkedList();
  private List virtualConfigs = new LinkedList();
  private List virtualMappings = new LinkedList();
  private List virtualConstants = new LinkedList();
  
  public List getRawConfigs() {
    return rawConfigs;
  }
  public List getRawMappings() {
    return rawMappings;
  }
  public List getVirtualConfigs() {
    return virtualConfigs;
  }
  public List getVirtualMappings() {
    return virtualMappings;
  }
  public List getVirtualConstants() {
    return virtualConstants;
  }
  public void setRawConfigs(List rawConfigs) {
    this.rawConfigs = rawConfigs;
  }
  public void setRawMappings(List rawMappings) {
    this.rawMappings = rawMappings;
  }
  public void setVirtualConfigs(List virtualConfigs) {
    this.virtualConfigs = virtualConfigs;
  }
  public void setVirtualMappings(List virtualMappings) {
    this.virtualMappings = virtualMappings;
  }
  public void setVirtualConstants(List virtualConstants) {
    this.virtualConstants = virtualConstants;
  }
  public void addRawDeviceConfig(RawDeviceConfig config) {
    rawConfigs.add(config);
  }
  public void addRawMapping(RawMapping mapping) {
    rawMappings.add(mapping);
  }
  public void addVirtualDeviceConfig(VirtualDeviceConfig config) {
    virtualConfigs.add(config);
  }
  public void addVirtualMapping(VirtualMapping mapping) {
    virtualMappings.add(mapping);
  }
  public void addVirtualConstant(VirtualConstant constant) {
    virtualConstants.add(constant);
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("RawDevices:\n");
    for (Iterator i = getRawConfigs().iterator(); i.hasNext(); )
      sb.append("\t"+i.next().toString()).append('\n');
    sb.append("\nRawMappings:\n");
    for (Iterator i = getRawMappings().iterator(); i.hasNext(); )
      sb.append("\t"+i.next().toString()).append('\n');
    sb.append("\nVirtualDevices:\n");
    for (Iterator i = getVirtualConfigs().iterator(); i.hasNext(); )
      sb.append("\t"+i.next().toString()).append('\n');
    sb.append("\nVirtualMappings:\n");
    for (Iterator i = getVirtualMappings().iterator(); i.hasNext(); )
      sb.append("\t"+i.next().toString()).append('\n');
    sb.append("\nVirtualConstants:\n");
    for (Iterator i = getVirtualConstants().iterator(); i.hasNext(); )
      sb.append("\t"+i.next().toString()).append('\n');
    sb.append('\n');
    return sb.toString();
  }
  
  public static InputStream domToInputStream(Node root) throws UnsupportedEncodingException {
    StringBuffer sb = new StringBuffer(1024);
    domToString(root, sb, 0);
    byte[] bytes = sb.toString().getBytes("UTF-8");
    return new ByteArrayInputStream(bytes);
  }
  
  public static void domToString(Node node, StringBuffer sb, int ind)
  {
    switch(node.getNodeType())
    {
      case Node.ELEMENT_NODE:
        String name=node.getNodeName();
        switch(sb.length()>0? sb.charAt(sb.length()-1): '\n')
        {
          case '>': sb.append('\n'); //missing break is intentional
          case '\n':
            for(int ix=0; ix<ind; ix++) sb.append(' ');
        }
        sb.append('<').append(name);
        NamedNodeMap attr=node.getAttributes();
        if(attr!=null)
        {
          for(int ix=0, n=attr.getLength(); ix<n; ix++)
          {
            final Node a=attr.item(ix);
            if(((Attr)a).getSpecified())
            {
              sb.append(' ').append(a.getNodeName()).append("=\"");
              quote(a.getNodeValue(), sb);
              sb.append('"');
            }
          }
        }
        if(!node.hasChildNodes())
        {
          sb.append("/>");
          return;
        }
        sb.append(">");
        int lastPos=sb.length();
        ind+=2;
        for(Node n=node.getFirstChild(); n!=null; n=n.getNextSibling())
          domToString(n, sb, ind);
        ind-=2;
        if(lastPos<sb.length())
        {
          switch(sb.charAt(sb.length()-1))
          {
            case '>': if(sb.charAt(sb.length()-2)==']') break;
              sb.append('\n'); //missing break is intentional
            case '\n':
              for(int ix=0; ix<ind; ix++) sb.append(' ');
          }
        }
        sb.append("</").append(name).append('>');
        break;
      case Node.TEXT_NODE:
        String text=node.getNodeValue();
        quote(text, sb);
        break;
      case Node.CDATA_SECTION_NODE:
        sb.append("<![CDATA[").append(node.getNodeValue()).append("]]>");
        break;
      case Node.COMMENT_NODE:
        sb.append("<!--").append(node.getNodeValue()).append("-->");
        break;
      case Node.DOCUMENT_NODE:
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        for(Node n=node.getFirstChild(); n!=null; n=n.getNextSibling())
          domToString(n, sb, ind);
        break;
      case Node.DOCUMENT_TYPE_NODE:
        DocumentType type=(DocumentType)node;
        sb.append("<!DOCTYPE ").append(type.getName()).append(' ');
        String spec=type.getPublicId();
        if(spec!=null) sb.append("PUBLIC \"").append(spec).append('"');
        spec=type.getSystemId();
        if(spec!=null) sb.append("SYSTEM \"").append(spec).append('"');
        sb.append(">\n");
        break;
    }
  }
  
  private static void quote(String text, StringBuffer sb) {
    sb.ensureCapacity(sb.length()+text.length());
    for(int ix=0, num=text.length(); ix<num; ix++)
      switch(text.charAt(ix))
      {
        default: sb.append(text.charAt(ix)); break;
        case '&': sb.append("&amp;"); break;
        case '<': sb.append("&lt;"); break;
        case '>': sb.append("&gt;"); break;
        case '"': sb.append("&quot;"); break;
      }
  }

  public static void main(String[] args) throws IOException {
    ToolSystemConfiguration ts = ToolSystemConfiguration.loadDefaultConfiguration();
    System.out.println(ts);
  }
}
