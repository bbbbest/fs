package cn.zzu.ss.core;

import cn.zzu.ss.util.TypeUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

import static cn.zzu.ss.core.Constants.*;

/**
 * @since 1.8
 */
public class Serializer {

    private List<Object> handled;
    private int depth = 0;
    private Bytes bytes;

    public Serializer() {
        handled = new ArrayList<>();
        bytes = Bytes.newBytes();
    }

    /**
     * class name | primitive variables | reference objects | -> super class name | primitive variables | reference
     * objects | -> super class name | primitive variables | reference objects | -> ... obj-end-flag
     */
    public void writeObject(Object obj) {
        int idx;
        if (obj == null) {
            bytes.writeByte(FC_NULL);
        } else if ((idx = handled.indexOf(obj)) != -1) {
            refer_to(idx);
        } else {
            Class<?> objCls = obj.getClass();

            try {
                if (TypeUtil.isRefPrimitive(objCls)) {
                    write_ref_primitive(objCls, obj);      // reference primitive
                } else if (obj instanceof Class) {
                    write_class((Class<?>) obj, true);
                } else if (obj instanceof Collection) {
                    write_collection((Collection) obj); // collection
                } else if (obj instanceof Map) {
                    write_map((Map) obj); // map
                } else if (obj instanceof Enum) {
                    write_enum((Enum) obj); // enum
                } else if (objCls.isArray()) {
                    write_array(obj); // array
                } else if (String.class == objCls) {
                    write_string((String) obj); // string
                } else {
                    ClassDescriptor descriptor = ClassDescriptor.resolve(objCls);
                    write_object(obj, descriptor, true, true);
                }
            } catch (IOException e) {
                throw new SerializeException(e);
            }
        }
        if (depth == 0) {
            reset();
        }
    }

    public byte[] getSerialData() {
        return bytes.compactData();
    }

    /**
     * This method will flush all wrote data to the OutputStream - os.
     *
     * @param os the OutputStream
     *
     * @throws IOException the flush exception
     */
    public void flushTo(OutputStream os) throws IOException {
        handled.clear();
        byte[] data = bytes.compactData();
        os.write(data);
        os.flush();
        bytes.clear();
    }

    /*-----------------------private methods-------------------------------*/
    private void reset() {
        bytes.writeByte(FC_RESET);
    }

    private void refer_to(int idx) {
        bytes.writeByte(FC_REFERENCE);
        bytes.writeInt(idx);
    }

    private void write_classname(Class<?> cl, boolean clFlag) throws IOException {
        if (clFlag) {
            bytes.writeByte(FC_CLASS);
        }
        bytes.writeString(cl.getCanonicalName().replace('.', '/'));
    }

    /*
     * fs_enum + class_name + enum_string
     */
    private void write_enum(final Enum o) throws IOException {
        bytes.writeByte(FS_ENUM);
        write_classname(((Enum) o).getClass(), false);
        bytes.writeString((((Enum) o)).name());
    }

    /*
     * array format:
     * fs_array + signature + array_size + [1,2,3]
     * 1. if component type is primitive:  ele + ele + ... + ele
     * 2. if component type is reference primitive:
     *   a. null
     *   b. non_null + ele
     * 3. if others:
     *   a. null
     *   b. reference + idx
     *   c. non_null + ele[string, collection, map, enum, array, object]
     *
     * @see #write_string(String)
     * @see #write_collection(Collection)
     * @see #write_map(Map)
     * @see #write_enum(Enum)
     * @see #write_array(Object)
     * @see #write_object(Object, ClassDescriptor, boolean, boolean)
     */
    private void write_array(final Object o) throws IOException {
        handled.add(o);
        bytes.writeByte(FS_ARRAY);
        Class<?> eleType = o.getClass().getComponentType();
        int len = Array.getLength(o);
        bytes.writeString(TypeUtil.getClassSignature(eleType));
        bytes.writeInt(len);

        if (eleType.isPrimitive()) {
            if (eleType == Integer.TYPE) {
                // int array
                for (int i = 0; i < len; i++) {
                    int x = (int) Array.get(o, i);
                    bytes.writeInt(x);
                }
            } else if (eleType == Byte.TYPE) {
                // byte array
                for (int i = 0; i < len; i++) {
                    byte x = (byte) Array.get(o, i);
                    bytes.writeByte(x);
                }
            } else if (eleType == Long.TYPE) {
                // long array
                for (int i = 0; i < len; i++) {
                    long x = (long) Array.get(o, i);
                    bytes.writeLong(x);
                }
            } else if (eleType == Float.TYPE) {
                // float array
                for (int i = 0; i < len; i++) {
                    float x = (float) Array.get(o, i);
                    bytes.writeFloat(x);
                }
            } else if (eleType == Double.TYPE) {
                // double array
                for (int i = 0; i < len; i++) {
                    double x = (double) Array.get(o, i);
                    bytes.writeDouble(x);
                }
            } else if (eleType == Short.TYPE) {
                // short array
                for (int i = 0; i < len; i++) {
                    short x = (short) Array.get(o, i);
                    bytes.writeShort(x);
                }
            } else if (eleType == Character.TYPE) {
                // char array
                for (int i = 0; i < len; i++) {
                    char x = (char) Array.get(o, i);
                    bytes.writeChar(x);
                }
            } else if (eleType == Boolean.TYPE) {
                // boolean array
                for (int i = 0; i < len; i++) {
                    boolean x = (boolean) Array.get(o, i);
                    bytes.writeBoolean(x);
                }
            } else {
                throw new InternalError();
            }
        } else if (TypeUtil.isRefPrimitive(eleType)) {
            if (eleType == Integer.class) {
                // Integer array
                for (int i = 0; i < len; i++) {
                    Integer x = (Integer) Array.get(o, i);
                    if (x == null) bytes.writeByte(FC_NULL);
                    else {
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeInt(x);
                    }
                }
            } else if (eleType == Byte.class) {
                // Byte array
                for (int i = 0; i < len; i++) {
                    Byte x = (Byte) Array.get(o, i);
                    if (x == null) bytes.writeByte(FC_NULL);
                    else {
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeByte(x);
                    }
                }
            } else if (eleType == Long.class) {
                // Long array
                for (int i = 0; i < len; i++) {
                    Long x = (Long) Array.get(o, i);
                    if (x == null) bytes.writeByte(FC_NULL);
                    else {
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeLong(x);
                    }
                }
            } else if (eleType == Float.class) {
                // Float array
                for (int i = 0; i < len; i++) {
                    Float x = (Float) Array.get(o, i);
                    if (x == null) bytes.writeByte(FC_NULL);
                    else {
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeFloat(x);
                    }
                }
            } else if (eleType == Double.class) {
                // Double array
                for (int i = 0; i < len; i++) {
                    Double x = (Double) Array.get(o, i);
                    if (x == null) bytes.writeByte(FC_NULL);
                    else {
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeDouble(x);
                    }
                }
            } else if (eleType == Short.class) {
                // Short array
                for (int i = 0; i < len; i++) {
                    Short x = (Short) Array.get(o, i);
                    if (x == null) bytes.writeByte(FC_NULL);
                    else {
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeShort(x);
                    }
                }
            } else if (eleType == Character.class) {
                // Character array
                for (int i = 0; i < len; i++) {
                    Character x = (Character) Array.get(o, i);
                    if (x == null) bytes.writeByte(FC_NULL);
                    else {
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeChar(x);
                    }

                }
            } else if (eleType == Boolean.class) {
                // Boolean array
                for (int i = 0; i < len; i++) {
                    Boolean x = (Boolean) Array.get(o, i);
                    if (x == null) bytes.writeByte(FC_NULL);
                    else {
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeBoolean(x);
                    }
                }
            } else {
                throw new InternalError();
            }
        } else if (String.class == eleType) {
            // String array
            for (int i = 0; i < len; i++) {
                String x = ((String) Array.get(o, i));
                if (x == null) bytes.writeByte(FC_NULL);
                else {
                    int idx = handled.indexOf(x);
                    if (idx > 0) {
                        refer_to(idx);
                    } else {
                        handled.add(x);
                        bytes.writeByte(FC_NON_NULL);
                        bytes.writeString(x);
                    }
                }
            }
        } else if (eleType.isArray()) {
            // Array array
            for (int i = 0; i < len; i++) {
                Object x = Array.get(o, i);
                if (x == null) bytes.writeByte(FC_NULL);
                else {
                    int idx = handled.indexOf(x);
                    if (idx > 0) {
                        refer_to(idx);
                    } else {
                        bytes.writeByte(FC_NON_NULL);
                        write_array(x);
                    }
                }
            }
        } else if (Collection.class.isAssignableFrom(eleType)) {
            // Collection array
            for (int i = 0; i < len; i++) {
                Collection x = (Collection) Array.get(o, i);
                if (x == null) bytes.writeByte(FC_NULL);
                else {
                    int idx = handled.indexOf(x);
                    if (idx > 0) {
                        refer_to(idx);
                    } else {
                        bytes.writeByte(FC_NON_NULL);
                        write_collection(x);
                    }
                }
            }
        } else if (Enum.class.isAssignableFrom(eleType)) {
            // Enum array
            for (int i = 0; i < len; i++) {
                Enum x = (Enum) Array.get(o, i);
                if (x == null) bytes.writeByte(FC_NULL);
                else {
                    int idx = handled.indexOf(x);
                    if (idx > 0) {
                        refer_to(idx);
                    } else {
                        bytes.writeByte(FC_NON_NULL);
                        write_enum(x);
                    }
                }
            }
        } else if (Map.class.isAssignableFrom(eleType)) {
            // Map array
            for (int i = 0; i < len; i++) {
                Map x = (Map) Array.get(o, i);
                if (x == null) bytes.writeByte(FC_NULL);
                else {
                    int idx = handled.indexOf(x);
                    if (idx > 0) {
                        refer_to(idx);
                    } else {
                        bytes.writeByte(FC_NON_NULL);
                        write_map(x);
                    }
                }
            }
        } else if (eleType == Class.class) {
            // Class array
            for (int i = 0; i < len; i++) {
                Class x = (Class) Array.get(o, i);
                if (x == null) bytes.writeByte(FC_NULL);
                else {
                    int idx = handled.indexOf(x);
                    if (idx > 0) {
                        refer_to(idx);
                    } else {
                        bytes.writeByte(FC_NON_NULL);
                        write_class(x, true);
                    }
                }
            }
        } else {
            // Object array
            for (int i = 0; i < len; i++) {
                Object x = Array.get(o, i);
                if (x == null) bytes.writeByte(FC_NULL);
                else {
                    int idx = handled.indexOf(x);
                    if (idx >= 0) {
                        refer_to(idx);
                    } else {
                        handled.add(x);
                        bytes.writeByte(FC_NON_NULL);
                        Class xCl = x.getClass();
                        write_object(x, ClassDescriptor.resolve(xCl), true, true);
                    }
                }
            }
        }

    }

    /*
     * map format:
     * fs_map + map_class + map_size + loop[1,2,3]
     * key:
     * 1. null
     * 2. reference + idx
     * 3. fc_last_key_type
     * 4. fc_class + class_name
     * value:
     * 1. null
     * 2. reference + idx
     * 3. fc_last_val_type
     * 4. fc_class + class_name
     *
     **/
    private void write_map(final Map obj) throws IOException {
        handled.add(obj);
        bytes.writeByte(FS_MAP);

        write_classname(obj.getClass(), false);
        bytes.writeInt(obj.size());
        Class<?> lastKeyClass = null;
        Class<?> lastValClass = null;
        Set entries = obj.entrySet();
        for (Object o : entries) {
            Map.Entry entry = (Map.Entry) o;
            Object key = entry.getKey();
            Object val = entry.getValue();
            int idx;

            if (key == null) {
                bytes.writeByte(FC_NULL);
            } else {

                Class kCl = key.getClass();

                idx = handled.indexOf(key);

                if (idx >= 0) {
                    refer_to(idx);
                } else {
                    boolean eql = lastKeyClass == kCl;
                    if (eql) {
                        bytes.writeByte(FC_LAST_KEY_TYPE);
                    } else {
                        lastKeyClass = kCl;
                    }
                    write_object(key, ClassDescriptor.resolve(lastKeyClass), !eql, false);
                }
            }

            if (val == null) {
                bytes.writeByte(FC_NULL);
            } else {
                Class vCl = val.getClass();
                idx = handled.indexOf(val);
                if (idx >= 0) {
                    refer_to(idx);
                } else {
                    boolean eql = lastValClass == vCl;
                    if (eql) {
                        bytes.writeByte(FC_LAST_VALUE_TYPE);
                    } else {
                        lastValClass = vCl;
                    }
                    write_object(val, ClassDescriptor.resolve(lastValClass), !eql, false);
                }
            }
        }
    }

    /*
     * fs_collection + collection_class + collection_size + loop[1,2,3]
     * 1. null
     * 2. reference + idx
     * 3. fc_last_ele_type
     * 4. fc_class + class_name
     * @see #write_object(Object, ClassDescriptor, boolean, boolean)
     **/
    private void write_collection(final Collection obj) throws IOException {
        handled.add(obj);
        bytes.writeByte(FS_COLLECTION);
        write_classname(obj.getClass(), false);
        bytes.writeInt(obj.size());
        Class<?> lastEleType = null;
        for (Object o : obj) {
            if (o == null) {
                bytes.writeByte(FC_NULL);
                continue;
            }

            Class<?> ocl = o.getClass();
            int idx = handled.indexOf(o);
            if (idx >= 0) {
                refer_to(idx);
            } else {
                boolean eql = lastEleType == ocl;
                if (eql) {
                    bytes.writeByte(FC_LAST_ELE_TYPE);
                } else {
                    lastEleType = ocl;
                }
                write_object(o, ClassDescriptor.resolve(o.getClass()), !eql, false);
            }
        }
    }

    /*
     * fs_super + [1,2]
     * 1. null
     * 2. fc_super_info + primitive_variables + reference_variables[a,b]
     *   a. null
     *   b. reference + idx
     */
    private void write_super(Object obj, ClassDescriptor father) {
        bytes.writeByte(FC_SUPER);
        if (father == null || !father.isSerializable()) {
            bytes.writeByte(FC_NULL);
            return;
        }
        bytes.writeByte(FC_SUPER_INFO);
        // write primitive variables
        ClassDescriptor.FieldReflector fieldReflector = father.getFieldReflector();
        bytes.writeBytes(fieldReflector.primValOfObj(obj));

        // write reference variables
        Integer[] refKeys = fieldReflector.getRefKeys();
        long[] offsets = fieldReflector.getOffsets();
        for (Integer refKey : refKeys) {
            Object o = fieldReflector.valueOf(obj, refKey);
            if (o == null) {
                bytes.writeByte(FC_NULL);
            } else {
                int idx = handled.indexOf(o);
                if (idx >= 0) {
                    refer_to(idx);
                } else {
                    writeObject(o);
                    //                    write_object(o, ClassDescriptor.resolve(o.getClass()), true, true);
                }
            }
        } // loop end
        write_super(obj, father.getFather()); // write super recursively
    }

    /*
     * fs_ref_primitive + value
     **/
    private void write_ref_primitive(Class<?> cl, Object o) {

        bytes.writeByte(FS_REF_PRIMITIVE);

        if (cl == Integer.class) {
            bytes.writeChar('I');
            bytes.writeInt((Integer) o);    // int
        } else if (cl == Byte.class) {
            bytes.writeChar('B');
            bytes.writeByte((Byte) o);      // byte
        } else if (cl == Long.class) {
            bytes.writeChar('J');
            bytes.writeLong((Long) o);      // long
        } else if (cl == Float.class) {
            bytes.writeChar('F');
            bytes.writeFloat((Float) o);    // float
        } else if (cl == Double.class) {
            bytes.writeChar('D');
            bytes.writeDouble((Double) o);  // double
        } else if (cl == Short.class) {
            bytes.writeChar('S');
            bytes.writeShort((Short) o);    // short
        } else if (cl == Character.class) {
            bytes.writeChar('C');
            bytes.writeChar((Character) o); // char
        } else if (cl == Boolean.class) {
            bytes.writeChar('Z');
            bytes.writeBoolean((Boolean) o); // boolean
        } else {
            throw new InternalError();
        }
    }

    /*
     * fs_ref_primitive + value
     **/
    private void write_ref_primitive(char tc, Object o) {
        bytes.writeByte(FS_REF_PRIMITIVE);
        bytes.writeChar(tc);
        switch (tc) {
            case 'Z':
                bytes.writeBoolean((Boolean) o);
                break;
            case 'B':
                bytes.writeByte((Byte) o);
                break;
            case 'C':
                bytes.writeChar((Character) o);
                break;
            case 'S':
                bytes.writeShort((Short) o);
                break;
            case 'I':
                bytes.writeInt((Integer) o);
                break;
            case 'F':
                bytes.writeFloat((Float) o);
                break;
            case 'J':
                bytes.writeLong((Long) o);
                break;
            case 'D':
                bytes.writeDouble((Double) o);
                break;
        }
    }


    /*
     * fc_object + class_name* + primitive_variables + loop[1,2,3]
     * 1. fc_null
     * 2. fc_reference + idx
     *
     * @see #write_array(Object)
     * @see #write_map(Map)
     * @see #write_collection(Collection)
     * @see #write_enum(Enum)
     * @see #write_ref_primitive(char, Object)
     * @see #write_object(Object, ClassDescriptor, boolean, boolean)
     **/
    private void write_object(final Object obj, final ClassDescriptor descriptor, boolean writeClassName, boolean writeClassFlag) throws IOException {
        if (!descriptor.isSerializable()) {
            throw new SerializeException(String
                    .format("The object of %s could not be serialized.", descriptor.getType()));
        }

        Class<?> objType = descriptor.getType();

        try {
            depth++;
            handled.add(obj);

            if (obj instanceof Class) {
                write_class((Class<?>) obj, true);
                return;
            }

            if (descriptor.isRefPrimitive()) {
                write_ref_primitive(descriptor.getName().charAt(0), obj);
                return;
            }

            if (objType == String.class) {
                write_string((String) obj);
                return;
            }

            if (writeClassName) {
                write_classname(objType, writeClassFlag);
            }

            bytes.writeByte(FC_OBJECT);

            // write primitive variables
            ClassDescriptor.FieldReflector fieldReflector = descriptor.getFieldReflector();
            bytes.writeBytes(fieldReflector.primValOfObj(obj));

            // write reference variables
            Integer[] refKeys = fieldReflector.getRefKeys();
            long[] offsets = fieldReflector.getOffsets();

            for (Integer refKey : refKeys) {
                FieldDescriptor fieldDescriptor = fieldReflector.getFieldDescriptor(refKey);
                Object o = fieldReflector.valueOf(obj, refKey);
                if (o == null) {
                    bytes.writeByte(FC_NULL);
                } else {
                    int idx = handled.indexOf(o);
                    if (idx >= 0) {
                        refer_to(idx);
                    } else {
                        Class oCl = o.getClass();
                        if (fieldDescriptor.isArray()) {
                            // array
                            write_array(o);
                        } else if (fieldDescriptor.isMap()) {
                            // map
                            write_map((Map) o);
                        } else if (fieldDescriptor.isCollection()) {
                            // collection
                            write_collection((Collection) o);
                        } else if (fieldDescriptor.isEnum()) {
                            // enum
                            write_enum((Enum) o);
                        } else if (fieldDescriptor.isRefPrimitive()) {
                            // reference primitive variable
                            write_ref_primitive(fieldDescriptor.getTypeCode(), o);
                        } else if (String.class == o.getClass()) {
                            write_string((String) o);
                        } else {
                            // other object
                            write_object(o, ClassDescriptor.resolve(o.getClass()), true, true);
                        }
                    }
                }
            } // loop end

            write_super(obj, descriptor.getFather());
            bytes.writeByte(FC_OBJECT_END);

        } finally {
            depth--;
        }
    }

    private void write_string(final String obj) throws IOException {
        bytes.writeByte(FS_STRING);
        bytes.writeString(obj);
    }

    private void write_class(final Class<?> cl, boolean writeFlag) throws IOException {
        if (writeFlag) {
            bytes.writeByte(FS_CLASS);
        }
        bytes.writeString(cl.getCanonicalName().replace('.', '/'));
    }

    private void checkSerializable(Class<?> cl) {
        if (Serializable.class.isAssignableFrom(cl) && cl.getAnnotation(SS.class) == null)
            throw new SerializeException(String.format("Class [%s] could not be serialized.", cl.getCanonicalName()));
    }
}
