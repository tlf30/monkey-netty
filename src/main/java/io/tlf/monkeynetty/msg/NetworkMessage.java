
package io.tlf.monkeynetty.msg;

import io.tlf.monkeynetty.NetworkProtocol;

import java.io.Serializable;

/**
 *
 * @author Trevor
 */
public interface NetworkMessage extends Serializable {
    String getName();
    NetworkProtocol getProtocol();
}
