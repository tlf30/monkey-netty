package io.tlf.monkeynetty;

import io.tlf.monkeynetty.msg.NetworkMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * NetworkRegistrar keeps a record of class names to UIDs.
 * This is used by monkey-netty for transporting objects by using UIDs for each object.
 */
public class NetworkRegistrar {
    public NetworkRegistrar() {
    }

    private final static Logger LOGGER = Logger.getLogger(NetworkRegistrar.class.getName());

    private HashMap<String, Integer> classUID = new HashMap<>();
    private HashMap<Integer, String> uidClass = new HashMap<>();
    private volatile int uid = 0;


    /**
     * Register a class with the registrar.
     * If attempting to register the same class multiple times, the additional attempts
     * to register will be silently ignored.
     *
     * @param clazz The class to register
     */
    public void register(Class<? extends Serializable> clazz) {
        register(clazz.getName());
    }

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
     * Unregister a class with the registrar.
     * Removing a class from the registrar will not free the class ID.
     * If attempting to unregistering a class multiple times, the additional attempts
     * will be silently ignored.
     *
     * @param clazz The class to unreigster
     */
    public void unregister(Class<? extends NetworkMessage> clazz) {
        if (classUID.containsKey(clazz.getName())) {
            int id = classUID.get(clazz.getName());
            classUID.remove(clazz.getName());
            uidClass.remove(id);
        }
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
