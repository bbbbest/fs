package cn.zzu.ss.core;

import cn.zzu.ss.util.*;
import sun.misc.Unsafe;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;

import static cn.zzu.ss.util.MemberUtil.getSerializableConstructor;

/**
 * @since 1.8
 */
final class ClassDescriptor {
    private static final LightCache<Class<?>, ClassDescriptor> CACHES = new LightCache<>();
    private static final FieldDescriptor[] NO_FIELDS = new FieldDescriptor[0];
    private static final Long DEFAULT_UID = 1L;
    private static final Long INACCESSIBLE_UID = 0L;

    private final boolean serializable;
    private final boolean externalizable;

    private final Class<?> type;
    private final String name;
    private final ClassDescriptor father;
    private final Long serialVersionUID;
    private final FieldDescriptor[] fieldDescriptors;
    private final FieldReflector fieldReflector;
    private final Constructor<?> cons;
    private boolean refPrimitive;

    public ClassDescriptor(Class<?> type) {
        this.type = type;
        SS ss = type.getAnnotation(SS.class);

        serializable = Serializable.class.isAssignableFrom(type) || ss != null;

        externalizable = Externalizable.class.isAssignableFrom(type);

        name = type.getCanonicalName().replace(".", "/");

        Class<?> superClass = type.getSuperclass();

        father = (superClass == null || Object.class == superClass || TypeUtil
                .isRefPrimitive(type)) ? null : resolve(superClass);

        serialVersionUID = getSerialVersionUID(type);

        fieldDescriptors = getSerialFields(type);
        fieldReflector = new FieldReflector(fieldDescriptors);
        cons = getSerializableConstructor(type);
        if (cons == null) throw new NullPointerException(String
                .format("couldn't find default constructor for class %s.", type.getCanonicalName()));
        this.refPrimitive = false;
        CACHES.put(type, this);
    }

    private ClassDescriptor(final Class<?> type, boolean ref) {
        this.type = type;
        name = TypeUtil.getClassSignature(type);
        serializable = true;
        externalizable = false;
        father = null;
        this.serialVersionUID = MemberUtil.getSerialVersionID(type);
        fieldDescriptors = NO_FIELDS;
        fieldReflector = null;
        cons = null;
        this.refPrimitive = ref;
        CACHES.put(type, this);

    }

    private static FieldDescriptor[] getSerialFields(Class<?> cl) {
        Field[] fields = cl.getDeclaredFields();
        Set<Field> fieldSet = new HashSet<>(Arrays.asList(fields));
        fieldSet.removeIf(field->Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()));

        SS ss = cl.getAnnotation(SS.class);

        if (ss != null) {
            boolean pub = (ss.level() & SSLevel.PUBLIC) == SSLevel.PUBLIC;
            boolean pro = (ss.level() & SSLevel.PROTECTED) == SSLevel.PROTECTED;
            boolean pri = (ss.level() & SSLevel.PRIVATE) == SSLevel.PRIVATE;

            boolean all = pub && pro && pri;

            String[] includes = ss.includes();
            String[] excludes = ss.excludes();
            if (!all) {
                fieldSet.removeIf(field->!pub && Modifier.isPublic(field.getModifiers()) || !pri && Modifier
                        .isPrivate(field.getModifiers()) || !pro && Modifier.isProtected(field.getModifiers()));
            }
            ArrayList<Field> includeFields = new ArrayList<>();
            if (includes.length > 0 && !"".equals(includes[0])) {
                for (String in : includes) {
                    try {
                        includeFields.add(cl.getDeclaredField(in));
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            fieldSet.addAll(includeFields);
            if (excludes.length > 0 && !"".equals(excludes[0])) {
                for (String ex : excludes) {
                    fieldSet.removeIf(field->field.getName().equals(ex));
                }
            }
        }
        fields = fieldSet.toArray(new Field[0]);
        return fields.length == 0 ? NO_FIELDS : createFieldDesc(fields);
    }

    private static FieldDescriptor[] createFieldDesc(final Field[] fields) {
        Arrays.sort(fields, Comparator.comparing(Field::getName)); // sort fields as A-Z a-z
        int len = fields.length;
        FieldDescriptor[] descriptors = new FieldDescriptor[len];
        for (int i = 0; i < len; i++) {
            descriptors[i] = new FieldDescriptor(fields[i]);
        }
        return descriptors;
    }

    private static FieldDescriptor[] getDefaultSerialFields(Class<?> cl) {
        Field[] clFields = cl.getDeclaredFields();
        ArrayList<FieldDescriptor> list = new ArrayList<>();
        int mask = Modifier.STATIC | Modifier.TRANSIENT;

        for (Field clField : clFields) {
            if ((clField.getModifiers() & mask) == 0) {
                list.add(new FieldDescriptor(clField));
            }
        }
        FieldDescriptor[] res;
        if (list.size() == 0) {
            res = NO_FIELDS;
        } else {
            res = list.toArray(new FieldDescriptor[0]);
        }

        if (res.length > 0) {
            Arrays.sort(res, Comparator.comparing(FieldDescriptor::getName));
        }
        return res;
    }

    static ClassDescriptor resolve(String className) throws ClassNotFoundException {
        Class<?> cl = Class.forName(className);
        return resolve(cl);
    }

    static ClassDescriptor resolve(Class<?> cl) {


        ClassDescriptor res = CACHES.getIfPresent(cl);
        if (res == null) {
            if (TypeUtil.isRefPrimitive(cl)) res = new ClassDescriptor(cl, true);
            else res = new ClassDescriptor(cl);
        }
        return res;
    }

    private long getSerialVersionUID(Class<?> type) {
        if (!Serializable.class.isAssignableFrom(type) || Proxy.isProxyClass(type)) {
            return INACCESSIBLE_UID;
        }

        long uid;
        try {
            Field uidField = type.getDeclaredField("serialVersionUID");
            if (Modifier.isStatic(uidField.getModifiers())) {
                uid = (long) uidField.get(null);
            } else uid = DEFAULT_UID;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            uid = DEFAULT_UID;
        }
        return uid;
    }

    public boolean isSerializable() {
        return serializable;
    }

    public boolean isExternalizable() {
        return externalizable;
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ClassDescriptor getFather() {
        return father;
    }

    public long getSerialVersionUID() {
        return serialVersionUID;
    }

    public FieldDescriptor[] getFieldDescriptors() {
        return fieldDescriptors;
    }

    @Override
    public String toString() {
        return "ClassDescriptor{" + "type=" + type + ", name='" + name + '\'' + ", fieldDescriptors=" + Arrays
                .toString(fieldDescriptors) + ", father=" + father + '}';
    }

    public FieldReflector getFieldReflector() {
        return fieldReflector;
    }

    public Constructor<?> getConstructor() {
        return cons;
    }

    public boolean isRefPrimitive() {
        return refPrimitive;
    }

    static class FieldReflector {
        private static final Unsafe unsafe = UnsafeUtil.getUnsafe();
        private FieldDescriptor[] fieldDescriptors;
        private Integer[] priKeys;
        private Integer[] priSize;
        private Integer[] refKeys;
        private long[] offsets;
        private int priLen = 0;

        FieldReflector(FieldDescriptor[] fieldDescriptors) {
            this.fieldDescriptors = fieldDescriptors;
            int fieldNum = fieldDescriptors.length;
            this.offsets = new long[fieldNum];

            ArrayList<Integer> priFieldKey = new ArrayList<>();
            ArrayList<Integer> priFieldSize = new ArrayList<>();
            ArrayList<Integer> refFieldKey = new ArrayList<>();

            for (int i = 0; i < fieldNum; ++i) {
                FieldDescriptor descriptor = fieldDescriptors[i];
                Field field = descriptor.getField();
                if (descriptor.isPrimitive()) {
                    priFieldKey.add(i);
                    int l = primitiveFieldSize(descriptor);
                    priFieldSize.add(l);
                    priLen += l;
                } else {
                    refFieldKey.add(i);
                }

                offsets[i] = descriptor.getOffset();
            }

            priKeys = priFieldKey.toArray(new Integer[0]);
            priSize = priFieldSize.toArray(new Integer[0]);
            refKeys = refFieldKey.toArray(new Integer[0]);

            if (priKeys.length + refKeys.length != fieldDescriptors.length) {
                throw new InternalError();
            }
        }

        private static int primitiveFieldSize(FieldDescriptor descriptor) {
            int size;
            switch (descriptor.getTypeCode()) {
                case 'z':
                case 'b':
                    size = 1;
                    break;

                case 'c':
                case 's':
                    size = 2;
                    break;

                case 'i':
                case 'f':
                    size = 4;
                    break;

                case 'j':
                case 'd':
                    size = 8;
                    break;
                default:
                    throw new InternalError();
            }
            return size;
        }

        byte[] primValOfObj(Object o) {
            byte[] priVals = new byte[priLen];

            int pos = 0;

            for (Integer k : priKeys) {
                FieldDescriptor descriptor = fieldDescriptors[k];
                switch (descriptor.getTypeCode()) {
                    case 'z':
                        Bits.putBoolean(priVals, pos++, unsafe.getBoolean(o, offsets[k]));
                        break;
                    case 'b':
                        priVals[pos++] = unsafe.getByte(o, offsets[k]);
                        break;

                    case 'c':
                        Bits.putChar(priVals, pos, unsafe.getChar(o, offsets[k]));
                        pos += 2;
                        break;
                    case 's':
                        Bits.putShort(priVals, pos, unsafe.getShort(o, offsets[k]));
                        pos += 2;
                        break;
                    case 'i':
                        Bits.putInt(priVals, pos, unsafe.getInt(o, offsets[k]));
                        pos += 4;
                        break;
                    case 'f':
                        Bits.putFloat(priVals, pos, unsafe.getFloat(o, offsets[k]));
                        pos += 4;
                        break;
                    case 'j':
                        Bits.putLong(priVals, pos, unsafe.getLong(o, offsets[k]));
                        pos += 8;
                        break;
                    case 'd':
                        Bits.putDouble(priVals, pos, unsafe.getDouble(o, offsets[k]));
                        pos += 8;
                        break;
                    default:
                        throw new InternalError();
                }
            }
            return priVals;
        }

        void setPrimValOfObj(Object o, byte[] priVals) {
            if (priLen != priVals.length) throw new InvalidIndexException("invalid byte array range");
            int pos = 0;

            for (Integer k : priKeys) {
                FieldDescriptor descriptor = fieldDescriptors[k];
                switch (descriptor.getTypeCode()) {
                    case 'z':
                        unsafe.putBoolean(o, offsets[k], Bits.getBoolean(priVals, pos++));
                        break;
                    case 'b':
                        unsafe.putByte(o, offsets[k], priVals[pos++]);
                        break;
                    case 'c':
                        unsafe.putChar(o, offsets[k], Bits.getChar(priVals, pos));
                        pos += 2;
                        break;
                    case 's':
                        unsafe.putShort(o, offsets[k], Bits.getShort(priVals, pos));
                        pos += 2;
                        break;
                    case 'i':
                        unsafe.putInt(o, offsets[k], Bits.getInt(priVals, pos));
                        pos += 4;
                        break;
                    case 'f':
                        unsafe.putFloat(o, offsets[k], Bits.getFloat(priVals, pos));
                        pos += 4;
                        break;
                    case 'j':
                        unsafe.putLong(o, offsets[k], Bits.getLong(priVals, pos));
                        pos += 8;
                        break;
                    case 'd':
                        unsafe.putDouble(o, offsets[k], Bits.getDouble(priVals, pos));
                        pos += 8;
                        break;
                    default:
                        throw new InternalError();
                }
            }
        }

        int getPriLen() {
            return priLen;
        }

        Integer[] getRefKeys() {
            return refKeys;
        }

        FieldDescriptor[] getFieldDescriptors() {
            return fieldDescriptors;
        }

        Integer[] getPriKeys() {
            return priKeys;
        }

        Integer[] getPriSize() {
            return priSize;
        }

        long[] getOffsets() {
            return offsets;
        }

        FieldDescriptor getFieldDescriptor(int index) {
            return fieldDescriptors[index];
        }

        Object valueOf(Object o, int keyOfFieldDescs) {
            return unsafe.getObject(o, offsets[keyOfFieldDescs]);
        }

        void setValueTo(Object to, int keyOfFieldDescs, Object val) {
            unsafe.putObject(to, offsets[keyOfFieldDescs], val);
        }
    }
}
