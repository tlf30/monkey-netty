package io.tlf.monkeynetty;

import io.tlf.monkeynetty.msg.NetworkMessage;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NetworkRegistrar keeps a record of class names to UIDs.
 * This is used by monkey-netty for transporting objects by using UIDs for each object.
 */
public final class NetworkRegistrar {
    private NetworkRegistrar() {}

    private static HashMap<String, Integer> classUID = new HashMap<>();
    private static HashMap<Integer, String> uidClass = new HashMap<>();
    private volatile static int uid = 0;

    /**
     * Internal Use Only
     * Register internal messages
     */
    private static void registerInternal() {

    }

    /**
     * Clear the registry of class and UID relations.
     */
    public synchronized static void clear() {
        classUID.clear();
        uidClass.clear();
        uid = 0;
        registerInternal();
    }

    /**
     * Register a class with the registrar.
     * If attempting to register the same class multiple times, the additional attempts
     * to register will be silently ignored.
     * @param clazz The class to register
     */
    public synchronized static void register(Class<? extends NetworkMessage> clazz) {
        if (!classUID.containsKey(clazz.getName())) {
            int newId = uid++;
            classUID.put(clazz.getName(), newId);
            uidClass.put(newId, clazz.getName());
        }
    }

    /**
     * Unregister a class with the registrar.
     * Removing a class from the registrar will not free the class ID.
     * If attempting to unregistering a class multiple times, the additional attempts
     * will be silently ignored.
     * @param clazz The class to unreigster
     */
    public synchronized static void unregister(Class<? extends NetworkMessage> clazz) {
        if (classUID.containsKey(clazz.getName())) {
            int id = classUID.get(clazz.getName());
            classUID.remove(clazz.getName());
            uidClass.remove(id);
        }
    }

    /**
     * @return The registry relating UID to class name
     */
    protected synchronized static HashMap<Integer, String> getUidRegistry() {
        return uidClass;
    }

    /**
     * @return The registry relating class name to UID
     */
    protected static HashMap<String, Integer> getClassRegistry() {
        return classUID;
    }
}
