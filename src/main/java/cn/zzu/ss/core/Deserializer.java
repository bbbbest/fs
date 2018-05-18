package cn.zzu.ss.core;

import cn.zzu.ss.util.MemberUtil;
import cn.zzu.ss.util.ReflectionException;
import cn.zzu.ss.util.TypeUtil;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.zzu.ss.core.Constants.*;

/**
 * @since 1.8
 */
public class Deserializer {

    private List<Object> handled;
    private int depth = 0;
    private Bytes bytes;

    public Deserializer(byte[] data) {
        bytes = Bytes.newBytes(data);
        handled = new ArrayList<>();
    }

    public Object readObject() {
        if (bytes.readable() <= 0) return null;
        Object res;
        byte f = bytes.peekByte();
        try {
            switch (f) {
                case Constants.FS_REF_PRIMITIVE:
                    res = read_ref_primitive(true, true);
                    break;
                case Constants.FS_STRING:
                    res = read_string();
                    break;
                case Constants.FS_COLLECTION:
                    res = read_collection();
                    break;
                case Constants.FS_MAP:
                    res = read_map();
                    break;
                case Constants.FS_ENUM:
                    res = read_enum(true, true);
                    break;
                case Constants.FS_ARRAY:
                    res = read_array(true);
                    break;
                case Constants.FC_NULL:
                    bytes.skipBytes(1);
                    res = null;
                    break;
                case Constants.FC_REFERENCE:
                    res = read_handled();
                    break;
                case Constants.FS_CLASS:
                    res = read_classname(true, true);
                    break;
                default:
                    res = read_object(true, true, true, null);
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new DeserializeException(e);
        }
        if (depth == 0) {
            check_reset();
        }
        return res;
    }


    /*------------------- private methods -----------------*/
    private String read_string() throws IOException {
        bytes.skipBytes(1);
        String str = bytes.readString();
        handled.add(str);
        return str;
    }

    private void check_reset() {
        if (bytes.peekByte() != Constants.FC_RESET) {
            throw new DeserializeException("invalid data");
        }
        bytes.skipBytes(1);
    }

    private Enum<?> read_enum(final boolean sign, final boolean clSign) throws IOException, ClassNotFoundException {
        Enum<?> res = null;
        check_flag(Constants.FS_ENUM, true);
        Class<?> enumType = read_classname(false, false);
        String name = bytes.readString();
        try {
            Method values = MemberUtil.getMethod("values", enumType, true);
            Enum<?>[] all = (Enum<?>[]) values.invoke(name);
            for (Enum one : all) {
                if (one.name().equals(name)) {
                    res = one;
                    break;
                }
            }
        } catch (ReflectionException | IllegalAccessException | InvocationTargetException ignored) {
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private Collection read_collection() throws IOException, ClassNotFoundException {
        check_flag(Constants.FS_COLLECTION, true);
        Class colType = read_classname(false, false);
        int len = bytes.readInt();
        Collection collection = null;
        try {
            collection = (Collection) colType.newInstance();
            handled.add(collection);
            Class<?> lastEleType = null;
            for (int i = 0; i < len; i++) {
                byte f = bytes.peekByte();
                Object o;
                switch (f) {
                    case Constants.FC_NULL:
                        o = null;
                        break;
                    case Constants.FC_REFERENCE:
                        o = read_handled();
                        break;
                    case Constants.FC_LAST_ELE_TYPE:
                        o = read_object(false, true, false, lastEleType);
                        break;
                    case Constants.FS_REF_PRIMITIVE:
                        o = read_ref_primitive(true, true);
                        break;
                    case Constants.FS_CLASS:
                        o = read_class(true, true);
                        break;
                    case Constants.FS_STRING:
                        o = read_string();
                        break;
                    default:
                        lastEleType = Class.forName(bytes.peekString().replace('/', '.'));
                        o = read_object(true, false, false, null);
                }

                collection.add(o);
            }
        } catch (InstantiationException | IllegalAccessException ignored) {
        }

        return collection;
    }

    private Object read_handled() {
        bytes.readByte();
        int idx = bytes.readInt();
        Object o;
        try {
            o = handled.get(idx);
        } catch (Exception e) {
            throw new DeserializeException("read handled error.");
        }
        return o;
    }

    @SuppressWarnings("unchecked")
    private Map read_map() throws IOException, ClassNotFoundException {
        check_flag(Constants.FS_MAP, true);
        Class<?> mapCl = read_classname(false, true);
        int size = bytes.readInt();
        Class lastKeyClass = null;
        Class lastValClass = null;
        Map res;
        try {
            res = (Map) mapCl.newInstance();
            handled.add(res);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DeserializeException(e);
        }
        Object key;
        Object val;
        byte tf;

        for (int i = 0; i < size; i++) {
            // read key
            tf = bytes.peekByte();
            switch (tf) {
                case FC_LAST_KEY_TYPE:
                    bytes.skipBytes(1);
                    key = read_object(false, true, false, lastKeyClass);
                    break;
                case FS_REF_PRIMITIVE:
                    key = read_ref_primitive(true, true);
                    break;
                case FS_STRING:
                    lastKeyClass = String.class;
                    key = read_string();
                    handled.add(key);
                    break;
                case FC_REFERENCE:
                    key = read_handled();
                    lastKeyClass = key.getClass();
                    break;
                case FS_CLASS:
                    key = read_class(true, true);
                    lastKeyClass = key.getClass();
                    break;
                default:
                    lastKeyClass = Class.forName(bytes.peekString().replace("/", "."));
                    key = read_object(true, false, false, null);
                    break;
            }

            // read val
            tf = bytes.peekByte();
            switch (tf) {
                case FC_LAST_VALUE_TYPE:
                    bytes.skipBytes(1);
                    val = read_object(false, true, false, lastValClass);
                    break;
                case FS_REF_PRIMITIVE:
                    val = read_ref_primitive(true, true);
                    break;
                case FS_STRING:
                    lastValClass = String.class;
                    val = read_string();
                    handled.add(val);
                    break;
                case FC_REFERENCE:
                    val = read_handled();
                    lastValClass = val.getClass();
                    break;
                case FS_CLASS:
                    val = read_class(true, true);
                    lastValClass = val.getClass();
                    break;
                default:
                    lastValClass = Class.forName(bytes.peekString().replace("/", "."));
                    val = read_object(true, false, false, null);
                    break;
            }

            res.put(key, val);
        }
        return res;
    }

    private Object read_array(final boolean sign) throws IOException, ClassNotFoundException {
        check_flag(Constants.FS_ARRAY, sign);
        String cl = bytes.readString();
        int len = bytes.readInt();
        Object arr;
        if (TypeUtil.isArraySign(cl)) {
            int w = 0;
            String tc;
            while (cl.charAt(w) == '[') w++;
            tc = cl.substring(w, cl.length());
            Class realCl;
            if ((realCl = TypeUtil.getSignClass(tc)) == null) {
                realCl = Class.forName(tc.substring(1, tc.length() - 1).replace('/', '.'));
            }
            for (int i = 0; i < w; i++) {
                arr = Array.newInstance(realCl, 0);
                realCl = arr.getClass();
            }
            arr = Array.newInstance(realCl, len);
            handled.add(arr);
            for (int i = 0; i < len; i++) {
                byte f = bytes.readByte();
                switch (f) {
                    case FC_NULL:
                        Array.set(arr, i, null);
                        break;
                    case Constants.FC_NON_NULL:
                        Array.set(arr, i, read_array(true));
                        break;
                    case Constants.FC_REFERENCE:
                        bytes.skipBytes(-1);
                        Array.set(arr, i, read_handled());
                        break;
                    default:
                        throw new InternalError();
                }
            }
        } else {
            Class<?> ct;
            if ((ct = TypeUtil.getSignClass(cl)) == null) {
                ct = Class.forName(cl.substring(1, cl.length() - 1).replace('/', '.'));
            }
            arr = Array.newInstance(ct, len);
            handled.add(arr);

            if (ct.isPrimitive()) {
                if (ct == Integer.TYPE) {
                    // int array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readInt());
                    }
                } else if (ct == Byte.TYPE) {
                    // byte array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte());
                    }
                } else if (ct == Long.TYPE) {
                    // long array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readLong());
                    }
                } else if (ct == Float.TYPE) {
                    // float array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readFloat());
                    }
                } else if (ct == Double.TYPE) {
                    // double array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readDouble());
                    }
                } else if (ct == Short.TYPE) {
                    // short array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readShort());
                    }
                } else if (ct == Character.TYPE) {
                    // char array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readChar());
                    }
                } else if (ct == Boolean.TYPE) {
                    // boolean array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readBoolean());
                    }
                } else {
                    throw new InternalError();
                }
            } else if (TypeUtil.isRefPrimitive(ct)) {
                if (ct == Integer.class) {
                    // Integer array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte() == FC_NULL ? null : bytes.readInt());
                    }
                } else if (ct == Byte.class) {
                    // Byte array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte() == FC_NULL ? null : bytes.readByte());
                    }
                } else if (ct == Long.class) {
                    // Long array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte() == FC_NULL ? null : bytes.readLong());
                    }
                } else if (ct == Float.class) {
                    // Float array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte() == FC_NULL ? null : bytes.readFloat());
                    }
                } else if (ct == Double.class) {
                    // Double array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte() == FC_NULL ? null : bytes.readDouble());
                    }
                } else if (ct == Short.class) {
                    // Short array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte() == FC_NULL ? null : bytes.readShort());
                    }
                } else if (ct == Character.class) {
                    // Character array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte() == FC_NULL ? null : bytes.readChar());
                    }
                } else if (ct == Boolean.class) {
                    // Boolean array
                    for (int i = 0; i < len; i++) {
                        Array.set(arr, i, bytes.readByte() == FC_NULL ? null : bytes.readBoolean());
                    }
                } else {
                    throw new InternalError();
                }
            } else if (String.class == ct) {
                // String array
                for (int i = 0; i < len; i++) {
                    byte f = bytes.readByte();
                    switch (f) {
                        case FC_NULL:
                            Array.set(arr, i, null);
                            break;
                        case Constants.FC_NON_NULL:
                            Array.set(arr, i, bytes.readString());
                            break;
                        case Constants.FC_REFERENCE:
                            bytes.skipBytes(-1);
                            Array.set(arr, i, read_handled());
                            break;
                        default:
                            throw new InternalError();
                    }
                }
            } else if (Collection.class.isAssignableFrom(ct)) {
                // Collection array
                for (int i = 0; i < len; i++) {
                    byte f = bytes.readByte();
                    switch (f) {
                        case FC_NULL:
                            Array.set(arr, i, null);
                            break;
                        case Constants.FC_NON_NULL:
                            Array.set(arr, i, read_collection());
                            break;
                        case Constants.FC_REFERENCE:
                            bytes.skipBytes(-1);
                            Array.set(arr, i, read_handled());
                            break;
                        default:
                            throw new InternalError();
                    }
                }
            } else if (Enum.class.isAssignableFrom(ct)) {
                // Enum array
                for (int i = 0; i < len; i++) {
                    byte f = bytes.readByte();
                    switch (f) {
                        case FC_NULL:
                            Array.set(arr, i, null);
                            break;
                        case Constants.FC_NON_NULL:
                            Array.set(arr, i, read_enum(true, true));
                            break;
                        case Constants.FC_REFERENCE:
                            bytes.skipBytes(-1);
                            Array.set(arr, i, read_handled());
                            break;
                        default:
                            throw new InternalError();
                    }
                }
            } else if (Map.class.isAssignableFrom(ct)) {
                // Map array
                for (int i = 0; i < len; i++) {
                    byte f = bytes.readByte();
                    switch (f) {
                        case FC_NULL:
                            Array.set(arr, i, null);
                            break;
                        case Constants.FC_NON_NULL:
                            Array.set(arr, i, read_map());
                            break;
                        case Constants.FC_REFERENCE:
                            bytes.skipBytes(-1);
                            Array.set(arr, i, read_handled());
                            break;
                        default:
                            throw new InternalError();
                    }
                }
            } else if (ct == Class.class) {
                // Class array
                for (int i = 0; i < len; i++) {
                    byte f = bytes.readByte();
                    switch (f) {
                        case FC_NULL:
                            Array.set(arr, i, null);
                            break;
                        case Constants.FC_NON_NULL:
                            Array.set(arr, i, read_class(true, true));
                            break;
                        case Constants.FC_REFERENCE:
                            bytes.skipBytes(-1);
                            Array.set(arr, i, read_handled());
                            break;
                        default:
                            throw new InternalError();
                    }
                }
            } else {
                // Object array
                for (int i = 0; i < len; i++) {
                    byte f = bytes.readByte();
                    switch (f) {
                        case FC_NULL:
                            Array.set(arr, i, null);
                            break;
                        case Constants.FC_NON_NULL:
                            Array.set(arr, i, read_object(true, true, true, null));
                            break;
                        case Constants.FC_REFERENCE:
                            bytes.skipBytes(-1);
                            Array.set(arr, i, read_handled());
                            break;
                        default:
                            throw new InternalError();
                    }
                }
            }
        }
        return arr;
    }

    private Object read_object(final boolean readClassName, final boolean checkClassFlag, final boolean readClassFlag, Class<?> cl) throws IOException, ClassNotFoundException {
        Object o;
        try {
            depth++;
            Class oCl;
            if (readClassName) {
                oCl = read_classname(checkClassFlag, readClassFlag);
            } else {
                if (cl == String.class || TypeUtil.isRefPrimitive(cl)) {
                    byte tb = bytes.peekByte();
                    switch (tb) {
                        case FS_REF_PRIMITIVE:
                            o = read_ref_primitive(true, true);
                            break;
                        case FS_STRING:
                            o = read_string();
                            break;
                        case FS_CLASS:
                            o = read_classname(true, true);
                            break;
                        default:
                            throw new InternalError();
                    }
                    handled.add(o);
                    return o;
                } else {
                    oCl = cl;
                }
            }
            ClassDescriptor classDescriptor = ClassDescriptor.resolve(oCl);

            o = classDescriptor.getConstructor().newInstance();
            handled.add(o);

            check_flag(FC_OBJECT, true);
            ClassDescriptor.FieldReflector fieldReflector = classDescriptor.getFieldReflector();

            // set primitive variables
            fieldReflector.setPrimValOfObj(o, bytes.readBytes());

            // set reference variables
            Integer[] refKeys = fieldReflector.getRefKeys();
            long[] offsets = fieldReflector.getOffsets();
            for (Integer refKey : refKeys) {
                byte ff = bytes.peekByte();
                switch (ff) {
                    case FC_NULL:
                        bytes.readByte();
                        fieldReflector.setValueTo(o, refKey, null);
                        break;
                    case Constants.FC_REFERENCE:
                        fieldReflector.setValueTo(o, refKey, read_handled());
                        break;
                    case Constants.FS_COLLECTION:
                        fieldReflector.setValueTo(o, refKey, read_collection());
                        break;
                    case Constants.FS_MAP:
                        fieldReflector.setValueTo(o, refKey, read_map());
                        break;
                    case Constants.FS_ARRAY:
                        fieldReflector.setValueTo(o, refKey, read_array(true));
                        break;
                    case Constants.FS_ENUM:
                        fieldReflector.setValueTo(o, refKey, read_enum(false, false));
                        break;
                    case Constants.FS_REF_PRIMITIVE:
                        fieldReflector.setValueTo(o, refKey, read_ref_primitive(true, true));
                        break;
                    case Constants.FS_STRING:
                        fieldReflector.setValueTo(o, refKey, read_string());
                        break;
                    default:
                        fieldReflector.setValueTo(o, refKey, read_object(true, true, true, null));
                }
            } // loop end

            read_super(o, classDescriptor.getFather());

            check_flag(Constants.FC_OBJECT_END, true);

        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new DeserializeException(e);
        } finally {
            depth--;
        }

        return o;
    }

    private Class read_classname(boolean checkFlag, boolean readFlag) throws IOException, ClassNotFoundException {
        if (checkFlag) {
            check_flag(Constants.FC_CLASS, readFlag);
        }
        String clStr = bytes.readString();
        return Class.forName(clStr.replace('/', '.'));
    }

    private Class read_class(boolean checkFlag, boolean readFlag) throws IOException, ClassNotFoundException {
        if (checkFlag) {
            check_flag(FS_CLASS, readFlag);
        }
        Class cl = read_classname(false, false);
        handled.add(cl);
        return cl;
    }

    private void check_flag(byte expected, boolean read) {
        byte f = bytes.peekByte();
        if (f != expected) {
            throw new DeserializeException("unexpected type flag");
        }

        if (read) {
            bytes.skipBytes(1);
        }
    }

    private void read_super(Object o, ClassDescriptor father) {
        check_flag(Constants.FC_SUPER, true);
        byte f = bytes.readByte();
        switch (f) {
            case FC_NULL:
                break;
            case Constants.FC_SUPER_INFO:
                ClassDescriptor.FieldReflector fieldReflector = father.getFieldReflector();
                fieldReflector.setPrimValOfObj(o, bytes.readBytes());
                Integer[] refKeys = fieldReflector.getRefKeys();
                long[] offsets = fieldReflector.getOffsets();

                for (Integer refKey : refKeys) {
                    FieldDescriptor fieldDescriptor = fieldReflector.getFieldDescriptor(refKey);
                    byte ff = bytes.peekByte();
                    switch (ff) {
                        case FC_NULL:
                            bytes.skipBytes(1);
                            fieldReflector.setValueTo(o, refKey, null);
                            break;
                        case FC_REFERENCE:
                            fieldReflector.setValueTo(o, refKey, read_handled());
                            break;
                        default:
                            fieldReflector.setValueTo(o, refKey, readObject());
                    }
                }
                read_super(o, father.getFather());
                break;
            default:
                throw new InternalError();
        }
    }

    private Object read_ref_primitive(final boolean flag, final boolean clSign) {
        if (flag) {
            bytes.readByte();
        }
        char tc;

        if (clSign) {
            tc = bytes.readChar();
        } else {
            tc = (char) bytes.peekShort();
        }
        Object res;
        switch (tc) {
            case 'Z':
                res = bytes.readBoolean();
                break;
            case 'B':
                res = bytes.readByte();
                break;
            case 'C':
                res = bytes.readChar();
                break;
            case 'S':
                res = bytes.readShort();
                break;
            case 'I':
                res = bytes.readInt();
                break;
            case 'F':
                res = bytes.readFloat();
                break;
            case 'J':
                res = bytes.readLong();
                break;
            case 'D':
                res = bytes.readDouble();
                break;
            default:
                throw new InternalError();
        }
        handled.add(res);
        return res;
    }
}
