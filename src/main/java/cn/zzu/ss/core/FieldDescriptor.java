package cn.zzu.ss.core;

import cn.zzu.ss.util.UnsafeUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import static cn.zzu.ss.util.TypeUtil.getClassSignature;

/**
 * @since 1.8
 */
final class FieldDescriptor {

    static final long INVALID_FIELD_OFFSET = Unsafe.INVALID_FIELD_OFFSET;
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();
    private static final char[] REF_PRIM = {'Z', 'B', 'C', 'S', 'I', 'F', 'J', 'D', 'V'};

    private boolean isEnum;
    private boolean isMap;
    private boolean isCollection;
    private boolean isArray;

    private Class<?> type;
    /*real offset in class*/
    private long offset;
    private String name;
    private Field field;
    private String signature;
    private boolean primitive;
    private boolean refPrimitive;

    public FieldDescriptor(Field field) {
        this.field = field;
        this.name = field.getName();

        this.type = field.getType();

        this.signature = getClassSignature(type).intern();
        offset = UNSAFE.objectFieldOffset(field);
        whichType(type);
    }

    private void whichType(Class<?> type) {
        primitive = type.isPrimitive();
        char tc = getTypeCode();

        for (Character c : REF_PRIM) {
            if (tc == c) {
                refPrimitive = true;
                break;
            }
        }

        isEnum = Enum.class.isAssignableFrom(type);
        isMap = Map.class.isAssignableFrom(type);
        isCollection = Collection.class.isAssignableFrom(type);
        isArray = type.isArray();
    }

    public boolean isEnum() {
        return isEnum;
    }

    public boolean isMap() {
        return isMap;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public boolean isArray() {
        return isArray;
    }

    public Class<?> getType() {
        return type;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(final int i) {
        this.offset = i;
    }

    public String getName() {
        return name;
    }

    public Field getField() {
        return field;
    }

    public String getSignature() {
        return signature;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public boolean isRefPrimitive() {
        return refPrimitive;
    }

    @Override
    public String toString() {
        return "FieldDescriptor{" + "type=" + type + ", name='" + name + '\'' + ", signature='" + signature + '\'' + '}';
    }

    public char getTypeCode() {
        return signature.charAt(0);
    }
}
