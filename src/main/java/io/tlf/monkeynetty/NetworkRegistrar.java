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

package io.tlf.monkeynetty;

import java.util.HashMap;

/**
 * NetworkRegistrar keeps a record of class names to UIDs.
 * This is used by monkey-netty for transporting objects by using UIDs for each object.
 */
public class NetworkRegistrar {

    private HashMap<String, Integer> classUID = new HashMap<>();
    private HashMap<Integer, String> uidClass = new HashMap<>();
    private volatile int uid = 0;

    /**
     * Register a class with the registrar.
     * If attempting to register the same class multiple times, the additional attempts
     * to register will be silently ignored.
     *
     * @param className The fully qualified class name to register
     */
    public void register(String className) {
        if (!classUID.containsKey(className)) {
            int newId = uid++;
            register(className, newId);
        }
    }

    /**
     * Internal Use Only
     * Force a classname and ID pair
     *
     * @param className The fully qualified class name to register
     * @param id        The UID of the class
     */
    public void register(String className, int id) {
        classUID.put(className, id);
        uidClass.put(id, className);
    }

    /**
     * @return The registry relating UID to class name
     */
    public HashMap<Integer, String> getUidRegistry() {
        return uidClass;
    }

    /**
     * @return The registry relating class name to UID
     */
    public HashMap<String, Integer> getClassRegistry() {
        return classUID;
    }
}
