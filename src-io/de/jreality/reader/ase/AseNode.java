/*
 * Copyright (c) 2000,2001 David Yazel, Teseract Software, LLP
 * Copyright (c) 2003-2004, Xith3D Project Group
 * All rights reserved.
 *
 * Portions based on the Java3D interface, Copyright by Sun Microsystems.
 * Many thanks to the developers of Java3D and Sun Microsystems for their
 * innovation and design.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the 'Xith3D Project Group' nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 *
 */
package de.jreality.reader.ase;


import java.awt.Color;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.logging.Level;

import de.jreality.reader.vecmath.Vector3f;
import de.jreality.util.LoggingSystem;


/**
 * Base node for all ASE nodes.  Any node that inherits from this need only
 * define the method variables and assign them to the parse list and the node
 * will automatically parse itself.
 *
 * @author David Yazel
 *
 */
public class AseNode {
    
    static final boolean debug = true;
    
    static Level level = Level.FINER;
    
    HashMap properties;

    public AseNode() {
        properties = new HashMap();
    }

    /**
     * This is a generic parser method for any AseNodes.  Some might need to
     * override this in order to handle the parse properly (Like MESH_FACENORMAL).
     * Basically this reads a line and if it has a key it is looked up in the
     * properties hash.  If it finds a match it then checks the type of the
     * class property using reflection and then parses the input based on type.
     *
     * An example is Color3f.  If a property is of type Color3f then it will assume that
     * there will be 3 floats as parameters to this line, in which case it will parse it.
     */
    public boolean parseProperty(AseReader in) {
        String property = (String) properties.get(in.key);

        if (property != null) {
            //         if (debug) LoggingSystem.getLogger(this).log(level, "  matched "+in.key+", field "+property);
            try {
                Field f = this.getClass().getDeclaredField(property);
                String type = f.getType().getName();

                //            if (debug) LoggingSystem.getLogger(this).log(level, "  type is "+type);
                if (type.equals("float")) {
                    f.setFloat(this, Float.parseFloat(in.params[0]));

                    if (debug) {
                        LoggingSystem.getLogger(this).log(level, "  Setting " + in.key + "/" +
                            f.getName() + " to " + in.params[0]);
                    }
                } else if (type.equals("int")) {
                    f.setInt(this, Integer.parseInt(in.params[0]));

                    if (debug) {
                        LoggingSystem.getLogger(this).log(level, "  Setting " + in.key + "/" +
                            f.getName() + " to " + in.params[0]);
                    }
                } else if (type.equals("boolean")) {
                    f.setBoolean(this, Integer.parseInt(in.params[0]) == 1);

                    if (debug) {
                        LoggingSystem.getLogger(this).log(level, "  Setting " + in.key + "/" +
                            f.getName() + " to " + f.getBoolean(this));
                    }
                } else if (type.equals("java.awt.Color")) {
                    float x = Float.parseFloat(in.params[0]);
                    float y = Float.parseFloat(in.params[1]);
                    float z = Float.parseFloat(in.params[2]);
                    f.set(this, new Color(x, y, z));
                    
                    if (debug) {
                        LoggingSystem.getLogger(this).log(level, "  Setting " + in.key + "/" +
                            f.getName() + " to " + f.get(this));
                    }
                } else if (type.equals("javax.vecmath.Vector3f")) {
                    Vector3f v = (Vector3f) f.get(this);
                    v.x = Float.parseFloat(in.params[0]);
                    v.y = Float.parseFloat(in.params[1]);
                    v.z = Float.parseFloat(in.params[2]);

                    if (debug) {
                        LoggingSystem.getLogger(this).log(level, "  Setting " + in.key + "/" +
                            f.getName() + " to " + v);
                    }
                } else if (type.equals("java.lang.String")) {
                    f.set(this, in.params[0]);

                    if (debug) {
                        LoggingSystem.getLogger(this).log(level, "  Setting " + in.key + "/" +
                            f.getName() + " to " + in.params[0]);
                    }
                } else {
                    // if this is field is an AseNode then call the parser for it
                    try {
                        if (Class.forName("de.jreality.reader.ase.AseNode")
                                     .isAssignableFrom(f.getType())) {
                            if (debug) {
                                LoggingSystem.getLogger(this).log(level, "  AseNode detected");
                            }

                            AseNode node = (AseNode) f.get(this);
                            node.parse(in);
                        } else {
                            if (debug) {
                                LoggingSystem.getLogger(this).log(level, 
                                    "  Can't determine type of matched " +
                                    in.key + ", field " + property);
                            }

                            return false;
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException ee) {
                ee.printStackTrace();
            }
        } else {
            return false;
        }

        return true;
    }

    public void trashBlock(AseReader in) {
        // if this property is not registered in the list, and this
        // is a block, then trash the block since we don't handle it
        if (in.startBlock) {
            if (debug) {
                LoggingSystem.getLogger(this).log(level, "  Trashing block " + in.key);
            }

            int numOpen = 1;

            while (in.readAseLine()) {
                if (in.startBlock) {
                    numOpen++;
                } else if (in.endBlock) {
                    numOpen--;
                }

                if (numOpen <= 0) {
                    break;
                }
            }

            in.endBlock = false;
        }
    }

    public void parse(AseReader in) {
        // for this to work, blocks have to open on the same line as the
        // property definition.
        if (debug) {
            LoggingSystem.getLogger(this).log(level, "  parsing " + this.getClass().getName());
        }

        boolean inBlock = in.startBlock;

        while (in.readAseLine()) {
            if (!parseProperty(in)) {
                if (in.startBlock) {
                    trashBlock(in);
                }
            }

            if (inBlock && (in.endBlock)) {
                break;
            }
        }

        if (inBlock) {
            in.endBlock = false;
        }
    }
}
