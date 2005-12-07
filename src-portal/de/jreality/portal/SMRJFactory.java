package de.jreality.portal;

import java.io.IOException;

import de.smrj.Broadcaster;
import de.smrj.RemoteFactory;
import de.smrj.tcp.TCPBroadcasterNIO;

/**
 * TODO: maybe synch, configure via config file (at least the port)
 * 
 * @author gollwas
 */
class SMRJFactory {

  private static RemoteFactory factory;
  private static Broadcaster broadcaster;

  private SMRJFactory() {}
  
  static RemoteFactory getRemoteFactory() {
    if (factory == null) factory = getBroadcaster().getRemoteFactory();
    return factory;
  }
  
  static Broadcaster getBroadcaster() {
    if (broadcaster == null)
      try {
        broadcaster = new TCPBroadcasterNIO(8868);
      } catch (IOException e) {
        throw new Error();
      }
    return broadcaster;
  }
}
