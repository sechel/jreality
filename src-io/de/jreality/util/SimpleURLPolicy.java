/*
 * Created on Apr 22, 2005
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

import java.net.URL;
import java.security.*;
import java.util.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;

/**
 * A policy which restricts access from a configured set of
 * code source {@link URL}s and grants access to all other.
 * The policy can be activated by calling <code>
 * {@link java.security.Policy#setPolicy(java.security.Policy)}
 * </code>.
 */
public class SimpleURLPolicy extends Policy
{
  private final static AllPermission ALL_PERM = new AllPermission(); 
  private final HashSet restricted = new HashSet();
  private final LinkedList restrictedPermissions=new LinkedList();
  
  public SimpleURLPolicy(Collection permissions, URL url) {
    this(permissions, Collections.singleton(url));
  }
  public SimpleURLPolicy(Collection permissions, Collection urls) {
    restrictedPermissions.addAll(permissions);
    restricted.addAll(urls);
    restricted.add(null);
  }
  public PermissionCollection getPermissions(CodeSource codesource)
  {
    Permissions pc=new Permissions();
    if(restricted.contains(codesource.getLocation()))
    {
      for (Iterator iter = restrictedPermissions.iterator(); iter.hasNext();) {
        Permission element = (Permission) iter.next();
        pc.add(element);
      }
    }
    else {
      pc.add(ALL_PERM);//unrestricted 
    }
    return pc;
  }

  public void refresh() {
    LoggingSystem.getLogger(this).log(Level.FINER, "refresh called");
  }
}
