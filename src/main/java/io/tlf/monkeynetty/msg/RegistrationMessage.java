/*
MIT License

Copyright (c) 2020 Trevor Flynn

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package io.tlf.monkeynetty.msg;

import io.tlf.monkeynetty.NetworkProtocol;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 * <p>
 * RegistrationMessage is used to pass
 * a cross reference of class names to class unique identifiers to the client's
 * message encoder and decoder.
 */
public class RegistrationMessage implements NetworkMessage {
    private HashMap<Integer, String> classKeys;

    /**
     * @return The cross reference of class names to UIDs.
     */
    public HashMap<Integer, String> getClassKeys() {
        return classKeys;
    }

    /**
     * Set the xRef of class names to UIDs. Class names must be fully qualified classpath names.
     * i.e. java.lang.String
     * And UIDs must be completely unique.
     *
     * @param classKeys a cross reference of class names to UIDs
     */
    public void setClassKeys(HashMap<Integer, String> classKeys) {
        this.classKeys = classKeys;
    }

    @Override
    public String getName() {
        return "registration-message";
    }

    @Override
    public NetworkProtocol getProtocol() {
        return NetworkProtocol.TCP;
    }
}
