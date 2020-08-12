package cml.lib.registry.apache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class WinRegistry {

    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;
    public static final int REG_SUCCESS = 0;
    public static final int REG_NOTFOUND = 2;
    public static final int REG_ACCESSDENIED = 5;

    private static final int KEY_ALL_ACCESS = 0xf003f;
    private static final int KEY_READ = 0x20019;
    private static final Preferences USER_ROOT = Preferences.userRoot();
    private static final Preferences SYSTEM_ROOT = Preferences.systemRoot();
    private static final Class<? extends Preferences> USER_CLASS = USER_ROOT.getClass();
    private static final Method REG_OPEN_KEY;
    private static final Method REG_CLOSE_KEY;
    private static final Method REG_QUERY_VALUE_BOX;
    private static final Method REG_ENUM_VALUE;
    private static final Method REG_QUERY_INFO_KEY;
    private static final Method REG_ENUM_KEY_EX;
    private static final Method REG_CREATE_KEY_EX;
    private static final Method REG_SET_VALUE_EX;
    private static final Method REG_DELETE_KEY;
    private static final Method REG_DELETE_VALUE;

    static {
        try {
            REG_OPEN_KEY = USER_CLASS.getDeclaredMethod("WindowsRegOpenKey",
                    new Class[]{int.class, byte[].class, int.class});
            REG_OPEN_KEY.setAccessible(true);
            REG_CLOSE_KEY = USER_CLASS.getDeclaredMethod("WindowsRegCloseKey",
                    new Class[]{int.class});
            REG_CLOSE_KEY.setAccessible(true);
            REG_QUERY_VALUE_BOX = USER_CLASS.getDeclaredMethod("WindowsRegQueryValueEx",
                    new Class[]{int.class, byte[].class});
            REG_QUERY_VALUE_BOX.setAccessible(true);
            REG_ENUM_VALUE = USER_CLASS.getDeclaredMethod("WindowsRegEnumValue",
                    new Class[]{int.class, int.class, int.class});
            REG_ENUM_VALUE.setAccessible(true);
            REG_QUERY_INFO_KEY = USER_CLASS.getDeclaredMethod("WindowsRegQueryInfoKey1",
                    new Class[]{int.class});
            REG_QUERY_INFO_KEY.setAccessible(true);
            REG_ENUM_KEY_EX = USER_CLASS.getDeclaredMethod(
                    "WindowsRegEnumKeyEx", new Class[]{int.class, int.class,
                        int.class});
            REG_ENUM_KEY_EX.setAccessible(true);
            REG_CREATE_KEY_EX = USER_CLASS.getDeclaredMethod(
                    "WindowsRegCreateKeyEx", new Class[]{int.class,
                        byte[].class});
            REG_CREATE_KEY_EX.setAccessible(true);
            REG_SET_VALUE_EX = USER_CLASS.getDeclaredMethod(
                    "WindowsRegSetValueEx", new Class[]{int.class,
                        byte[].class, byte[].class});
            REG_SET_VALUE_EX.setAccessible(true);
            REG_DELETE_VALUE = USER_CLASS.getDeclaredMethod(
                    "WindowsRegDeleteValue", new Class[]{int.class,
                        byte[].class});
            REG_DELETE_VALUE.setAccessible(true);
            REG_DELETE_KEY = USER_CLASS.getDeclaredMethod(
                    "WindowsRegDeleteKey", new Class[]{int.class,
                        byte[].class});
            REG_DELETE_KEY.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private WinRegistry() {
    }

    /**
     * Read a value from key and value name
     *
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key key
     * @param valueName valueName
     * @return the value
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static String readString(int hkey, String key, String valueName)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        switch (hkey) {
            case HKEY_LOCAL_MACHINE:
                return readString(SYSTEM_ROOT, hkey, key, valueName);
            case HKEY_CURRENT_USER:
                return readString(USER_ROOT, hkey, key, valueName);
            default:
                throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Read value(s) and value name(s) form given key
     *
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key key
     * @return the value name(s) plus the value(s)
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static Map<String, String> readStringValues(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        switch (hkey) {
            case HKEY_LOCAL_MACHINE:
                return readStringValues(SYSTEM_ROOT, hkey, key);
            case HKEY_CURRENT_USER:
                return readStringValues(USER_ROOT, hkey, key);
            default:
                throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Read the value name(s) from a given key
     *
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key key
     * @return the value name(s)
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static List<String> readStringSubKeys(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        switch (hkey) {
            case HKEY_LOCAL_MACHINE:
                return readStringSubKeys(SYSTEM_ROOT, hkey, key);
            case HKEY_CURRENT_USER:
                return readStringSubKeys(USER_ROOT, hkey, key);
            default:
                throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Create a key
     *
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key key
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static void createKey(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        int[] ret;
        switch (hkey) {
            case HKEY_LOCAL_MACHINE:
                ret = createKey(SYSTEM_ROOT, hkey, key);
                REG_CLOSE_KEY.invoke(SYSTEM_ROOT, new Object[]{ret[0]});
                break;
            case HKEY_CURRENT_USER:
                ret = createKey(USER_ROOT, hkey, key);
                REG_CLOSE_KEY.invoke(USER_ROOT, new Object[]{ret[0]});
                break;
            default:
                throw new IllegalArgumentException("hkey=" + hkey);
        }
        if (ret[1] != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
        }
    }

    /**
     * Write a value in a given key/value name
     *
     * @param hkey hkey
     * @param key key
     * @param valueName valueName
     * @param value value
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static void writeStringValue(int hkey, String key, String valueName, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        switch (hkey) {
            case HKEY_LOCAL_MACHINE:
                writeStringValue(SYSTEM_ROOT, hkey, key, valueName, value);
                break;
            case HKEY_CURRENT_USER:
                writeStringValue(USER_ROOT, hkey, key, valueName, value);
                break;
            default:
                throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Delete a given key
     *
     * @param hkey hkey
     * @param key key
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static void deleteKey(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        int rc = -1;
        if (hkey == HKEY_LOCAL_MACHINE) {
            rc = deleteKey(SYSTEM_ROOT, hkey, key);
        } else if (hkey == HKEY_CURRENT_USER) {
            rc = deleteKey(USER_ROOT, hkey, key);
        }
        if (rc != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
        }
    }

    /**
     * delete a value from a given key/value name
     *
     * @param hkey hkey
     * @param key key
     * @param value value
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    public static void deleteValue(int hkey, String key, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        int rc = -1;
        if (hkey == HKEY_LOCAL_MACHINE) {
            rc = deleteValue(SYSTEM_ROOT, hkey, key, value);
        } else if (hkey == HKEY_CURRENT_USER) {
            rc = deleteValue(USER_ROOT, hkey, key, value);
        }
        if (rc != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
        }
    }

    // =====================
    private static int deleteValue(Preferences root, int hkey, String key, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        int[] handles = (int[]) REG_OPEN_KEY.invoke(root, new Object[]{
            hkey, toCstr(key), KEY_ALL_ACCESS});
        if (handles[1] != REG_SUCCESS) {
            return handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
        }
        int rc = ((Integer) REG_DELETE_VALUE.invoke(root,
                new Object[]{
                    handles[0], toCstr(value)
                }));
        REG_CLOSE_KEY.invoke(root, new Object[]{handles[0]});
        return rc;
    }

    private static int deleteKey(Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        int rc = ((Integer) REG_DELETE_KEY.invoke(root,
                new Object[]{hkey, toCstr(key)}));
        return rc;  // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
    }

    private static String readString(Preferences root, int hkey, String key, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        int[] handles = (int[]) REG_OPEN_KEY.invoke(root, new Object[]{
            hkey, toCstr(key), KEY_READ});
        if (handles[1] != REG_SUCCESS) {
            return null;
        }
        byte[] valb = (byte[]) REG_QUERY_VALUE_BOX.invoke(root, new Object[]{
            handles[0], toCstr(value)});
        REG_CLOSE_KEY.invoke(root, new Object[]{handles[0]});
        return (valb != null ? new String(valb).trim() : null);
    }

    private static Map<String, String> readStringValues(Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        HashMap<String, String> results = new HashMap<>();
        int[] handles = (int[]) REG_OPEN_KEY.invoke(root, new Object[]{
            hkey, toCstr(key), KEY_READ});
        if (handles[1] != REG_SUCCESS) {
            return null;
        }
        int[] info = (int[]) REG_QUERY_INFO_KEY.invoke(root,
                new Object[]{handles[0]});

        int count = info[0]; // count  
        int maxlen = info[3]; // value length max
        for (int index = 0; index < count; index++) {
            byte[] name = (byte[]) REG_ENUM_VALUE.invoke(root, new Object[]{
                handles[0], index, maxlen + 1});
            String value = readString(hkey, key, new String(name));
            results.put(new String(name).trim(), value);
        }
        REG_CLOSE_KEY.invoke(root, new Object[]{handles[0]});
        return results;
    }

    private static List<String> readStringSubKeys(Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        List<String> results = new ArrayList<>();
        int[] handles = (int[]) REG_OPEN_KEY.invoke(root, new Object[]{
            hkey, toCstr(key), KEY_READ});
        if (handles[1] != REG_SUCCESS) {
            return null;
        }
        int[] info = (int[]) REG_QUERY_INFO_KEY.invoke(root,
                new Object[]{handles[0]});

        int count = info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj, confirmed by Petrucio
        int maxlen = info[3]; // value length max
        for (int index = 0; index < count; index++) {
            byte[] name = (byte[]) REG_ENUM_KEY_EX.invoke(root, new Object[]{
                handles[0], index, maxlen + 1});
            results.add(new String(name).trim());
        }
        REG_CLOSE_KEY.invoke(root, new Object[]{handles[0]});
        return results;
    }

    private static int[] createKey(Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        return (int[]) REG_CREATE_KEY_EX.invoke(root,
                new Object[]{hkey, toCstr(key)});
    }

    private static void writeStringValue(Preferences root, int hkey, String key, String valueName, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        int[] handles = (int[]) REG_OPEN_KEY.invoke(root, new Object[]{
            hkey, toCstr(key), KEY_ALL_ACCESS});

        REG_SET_VALUE_EX.invoke(root,
                new Object[]{
                    handles[0], toCstr(valueName), toCstr(value)
                });
        REG_CLOSE_KEY.invoke(root, new Object[]{handles[0]});
    }

    // utility
    private static byte[] toCstr(String str) {
        byte[] result = new byte[str.length() + 1];

        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }
}
