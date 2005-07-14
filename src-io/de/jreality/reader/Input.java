/*
 * Created on Mar 4, 2005
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

import java.io.*;
import java.net.*;

/**
 * Abstraction of an input source. Technically an instance covers an {@link java.io.InputStream}.
 * 
 * To create an Input instance, use the factory methods in {@link de.jreality.reader.Readers}.
 */
public final class Input
{
  private InputStream inputStream;
  private String description;
  private File sourceFile;
  private URL sourceURL;

  public Input(URL url) throws IOException
  {
    this(url.toString(), url.openStream());
    this.sourceURL=url;
  }
  public Input(File file) throws FileNotFoundException
  {
    this(file.toString(), new FileInputStream(file));
    this.sourceFile=file;
  }
  public Input(String description, InputStream is)
  {
    this.description=description;
    this.inputStream=new BufferedInputStream(is);
  }
  
  /**
   * @return the InputStream for this Input
   */
  public InputStream getInputStream()
  {
    return inputStream;
  }
  /**
   * Creates a Reader for this Input
   * 
   * @return a reader for the covered InputStream
   */
  public Reader getReader()
  {
    return new InputStreamReader(inputStream);
  }
  
  /**
   * Creates a Reader for this Input with given encoding.
   *  
   * @param encoding the encoding to use for the created Reader
   * @return a reader for the covered InputStream with the given encoding
   * @throws UnsupportedEncodingException
   */
  public Reader getReader(String encoding) throws UnsupportedEncodingException
  {
    return new InputStreamReader(inputStream, encoding);
  }
  
  /**
   * Tries to resolve a Resource relative to this Input. This works
   * only for Files and URLs.
   * 
   * @param name the relative name for the resource.
   * @return an Input for the relative resource.
   * @throws IOException if no such relative resource was found.
   */
  public Input getRelativeInput(String name) throws IOException
  {
    if(sourceFile!=null)
    {
      File p=sourceFile.getParentFile();
      return new Input((p==null)? new File(name): new File(p, name));
    }
    else if(sourceURL!=null)
    {
      return new Input(new URL(sourceURL, name));
    }
    else throw new IOException("cannot resolve \""
      +name+"\" relative to \""+description+'"');
  }
  
  /**
   * Tries to create an Input for the given name as follows:
   * <li> if there is a System resource with the given name, this is returned.
   * <li> if the given name is absolute, the corresponding Input is returned.
   * <li> if the input can be resolved relative to an underlying file or URL,
   * this Input is returned.
   * <li> Last try is to find the resource relative to the current directory.
   * 
   * @param name the name of the resource.
   * @return an Input for the given name.
   * @throws IOException if sth. goes wrong.
   */
  public Input resolveInput(String name) throws IOException {
    try {
      // TODO: is this the right way to find resources from classpath?
      URL test = ClassLoader.getSystemResource(name);
      if (test != null) {
        return new Input(test);
      }
      URI uri = new URI(name);
      if (uri.isAbsolute()) return new Input(uri.toURL());
      URI base;
      if (sourceFile != null)
        base = sourceFile.toURI();
      else if (sourceURL != null)
        base = new URI(sourceURL.toExternalForm());
      else
        //     throw new IOException("cannot resolve \""
        //        +name+"\" relative to \""+description+'"');
        base = new URI("file:"
            + System.getProperty("user.dir", "/").replace(File.separatorChar,
                '/')).normalize();
      URI resolved = base.resolve(uri);
      if (sourceFile != null && resolved.getScheme().equals("file"))
        return new Input(new File(resolved));
      else
        return new Input(resolved.toURL());
    } catch (URISyntaxException e) {
      throw new MalformedURLException(e.getMessage());
    } catch (IllegalArgumentException e2) {
      throw new MalformedURLException(e2.getMessage());
    }
  }
    
  /**
   * returns a {@link java.io.File} for this Input if possible.
   * @return the File of this Input.
   * 
   * @throws UnsupportedOperationException if this Input is not from a file.
   */
  public File toFile() throws UnsupportedOperationException {
    if (sourceFile == null) throw new UnsupportedOperationException("not a file");
    return sourceFile;
  }
  
  /**
   * returns a {@link java.net.URL} for this Input if possible.
   * @return the URL of this Input.
   * 
   * @throws MalformedURLException if this Input can't be converted to a URL.
   */
  public URL toURL() throws MalformedURLException {
    if (sourceURL != null) return sourceURL;
    if (sourceFile != null) return sourceFile.toURL();
    throw new MalformedURLException("cannot convert "+description+" to URL.");
  }
  public String getDescription() {
      return description;
  }
  
  public String toString() {
      return "Input: "+description;
  }

}
