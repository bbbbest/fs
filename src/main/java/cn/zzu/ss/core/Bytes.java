package cn.zzu.ss.core;

import cn.zzu.ss.util.Bits;
import cn.zzu.ss.util.InvalidIndexException;
import cn.zzu.ss.util.InvalidMarkException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

/**
 * 字节缓冲区，拥有自增以及滑动特性。 |-1 |_0_|_1_|_2_|_3_|_4_|_5_|_6_|_7_|_8_|_9_| 11 |mark |   |pos --> | | cap | |limit -->
 * <p>
 * | 已读区域 | 可读区域| 可写区域|
 *
 * @author fzh
 * @see #position pos
 * @see #limit
 * @see #capacity cap
 * @see #mark
 * @since 1.8
 */
public final class Bytes {
    private static final int DEFAULT_CAPACITY = 128;

    /*data 为缓冲区的实际容器*/
    private byte[] data;
    /*
     * position 指向已读区域的最后一个位置
     * position 开始于 0,
     * 0 <= position < limit,
     */
    private int position;
    /*
     * limit 指向可读区域的最后一个位置
     * limit 开始于 0,
     * position < limit <= capacity,
     */
    private int limit;

    /*
     * mark 为一个标记，值为调用mark() 时的position值
     */
    private int mark;

    /*
     * capacity 为当前缓冲区的容量
     */
    private int capacity;

    private Bytes(byte[] bytes, int position, int mark, int limit) {
        this.data = bytes;
        this.position = position;
        this.mark = mark;
        this.limit = limit;
        this.capacity = bytes.length;
    }

    private Bytes() {
        this(new byte[DEFAULT_CAPACITY], 0, -1, 0);
    }

    public static Bytes newBytes() {
        return new Bytes();
    }

    public static Bytes newBytes(byte[] bytes) {
        return new Bytes(bytes, 0, -1, bytes.length);
    }

    /**
     * 返回缓冲区可读的字节数
     */
    public int readable() {
        return limit - position;
    }

    /**
     * 返回缓冲区已读的字节数
     */
    public int read() {
        return position;
    }

    /**
     * 返回缓冲区可写的字节数
     */
    public int writable() {
        return capacity - limit;
    }

    public void mark() {
        mark = position;
    }

    public Bytes reset() {
        if (mark < 0) throw new InvalidMarkException(String.format("Invalid mark position %d", mark));
        position = mark;
        return this;
    }

    /**
     * 重构缓冲区，只保留未读内容
     *
     * @return 重构后的缓冲区
     */
    public Bytes align() {
        byte[] newData;
        if ((capacity >>> 2) > limit) {
            capacity = capacity >>> 1;
        }
        newData = new byte[capacity];
        System.arraycopy(data, position, newData, 0, limit - position);
        data = newData;

        limit -= position;
        position = 0;

        return this;
    }

    private void grow(int newCapacity) {
        int adviceCap;
        if (newCapacity > data.length) {
            adviceCap = newCapacity;
        } else {
            adviceCap = data.length;
        }

        adviceCap = adviceCap + (adviceCap >> 1);

        byte[] newData = new byte[adviceCap];
        System.arraycopy(data, 0, newData, 0, data.length);
        capacity = adviceCap;
        data = newData;
    }

    /**
     * 预读一个字节
     */
    public byte peekByte() {
        checkBound(position + 1, position, limit);
        return data[position];
    }

    /**
     * 预读一个short型数据
     */
    public short peekShort() {
        checkBound(position + 2, position, limit);
        return Bits.getShort(data, position);
    }

    /**
     * 预读一个int型数据
     */
    public int peekInt() {
        checkBound(position + 4, position, limit);
        return Bits.getInt(data, position);
    }

    /**
     * 预读一个long型数据
     */
    public long peekLong() {
        checkBound(position + 8, position, limit);
        return Bits.getLong(data, position);
    }

    /**
     * 预读一个long型数据
     */
    public String peekString() throws IOException {
        int strLen = getLen(false);
        checkBound(position + strLen, position, limit);
        int m = position;
        reset();
        return new String(data, m, strLen, "UTF-8");
    }

    /**
     * 跳过 n 个字节
     *
     * @param n bytes
     *
     * @return 跳过之后的position
     */
    public int skipBytes(int n) {
        checkBound(position + n, position, limit);
        return position += n;
    }

    /**
     * 读一个boolean 型数据
     */
    public boolean readBoolean() {
        checkBound(position + 1, position, limit);
        return Bits.getBoolean(data, position++);
    }

    /**
     * 读一个 byte 型数据
     */
    public byte readByte() {
        checkBound(position + 1, position, limit);
        return data[position++];
    }

    /**
     * 读一个 无符号 byte 型数据
     */
    public int readUnsignedByte() {
        checkBound(position + 1, position, limit);
        return (int) data[position++];
    }

    /**
     * 读一个 short 型数据
     */
    public short readShort() {
        checkBound(position + 2, position, limit);
        position += 2;
        return Bits.getShort(data, position - 2);
    }

    /**
     * 读一个 无符号 short 型数据
     */
    public int readUnsignedShort() {
        checkBound(position + 2, position, limit);
        position += 2;
        return (int) Bits.getShort(data, position - 2);
    }

    /**
     * 读一个 char 型数据
     */
    public char readChar() {
        checkBound(position + 2, position, limit);
        position += 2;
        return Bits.getChar(data, position - 2);
    }

    /**
     * 读一个 int 型数据
     */
    public int readInt() {
        checkBound(position + 4, position, limit);
        position += 4;
        return Bits.getInt(data, position - 4);
    }

    /**
     * 读一个 long 型数据
     */
    public long readLong() {
        checkBound(position + 8, position, limit);
        position += 8;
        return Bits.getLong(data, position - 8);
    }


    /**
     * 读一个 float 型数据
     */
    public float readFloat() {
        checkBound(position + 4, position, limit);
        position += 4;
        return Bits.getFloat(data, position - 4);
    }

    /**
     * 读一个 double 型数据
     */
    public double readDouble() {
        checkBound(position + 8, position, limit);
        position += 8;
        return Bits.getDouble(data, position - 8);
    }

    /**
     * 读一个字符串
     */
    public String readString() throws IOException {
        int strLen = getLen(true);
        checkBound(position + strLen, position, limit);
        position += strLen;
        return new String(data, position - strLen, strLen, "UTF-8");
    }

    /**
     * 写入一个 boolean 型数据
     */
    public void writeBoolean(boolean v) {
        limit(limit + 1);
        Bits.putBoolean(data, limit - 1, v);
    }

    /**
     * 写入一个 byte 型数据
     */
    public void writeByte(byte v) {
        limit(limit + 1);
        data[limit - 1] = v;
    }

    /**
     * 写入一个 short 型数据
     */
    public void writeShort(int v) {
        limit(limit + 2);
        Bits.putShort(data, limit - 2, (short) v);
    }

    /**
     * 写入一个 char 型数据
     */
    public void writeChar(int v) {
        limit(limit + 2);
        Bits.putChar(data, limit - 2, (char) v);
    }

    /**
     * 写入一个 int 型数据
     */
    public void writeInt(int v) {
        limit(limit + 4);
        Bits.putInt(data, limit - 4, v);
    }

    /**
     * 写入一个 long 型数据
     */
    public void writeLong(long v) {
        limit(limit + 8);
        Bits.putLong(data, limit - 8, v);
    }

    /**
     * 写入一个 float 型数据
     */
    public void writeFloat(float v) {
        limit(limit + 4);
        Bits.putFloat(data, limit - 4, v);
    }

    /**
     * 写入一个 double 型数据
     */
    public void writeDouble(double v) {
        limit(limit + 8);
        Bits.putDouble(data, limit - 8, v);
    }

    /**
     * 写入一个字符串
     */
    public void writeString(String s) throws IOException {
        byte[] str = s.getBytes("UTF-8");
        int strLen = str.length;
        writeLen(strLen);
        limit(limit + strLen);
        System.arraycopy(str, 0, data, limit - strLen, strLen);
    }

    public void writeBytes(byte[] bytes) {
        Objects.requireNonNull(bytes);
        int arrSize = bytes.length;
        writeLen(arrSize);
        limit(limit + arrSize);
        System.arraycopy(bytes, 0, data, limit - arrSize, arrSize);
    }

    public void writeBooleans(boolean[] booleans) {
        Objects.requireNonNull(booleans);
        int arrSize = booleans.length;
        writeLen(arrSize);
        limit(limit + arrSize);
        for (int i = 0; i < arrSize; i++) {
            Bits.putBoolean(data, limit - arrSize + i, booleans[i]);
        }
    }

    public void writeShorts(short[] shorts) {
        Objects.requireNonNull(shorts);
        int arrSize = shorts.length;
        writeLen(arrSize);
        limit(limit + arrSize * 2);
        for (int i = 0; i < arrSize; i++) {
            Bits.putShort(data, limit - (arrSize - i) * 2, shorts[i]);
        }
    }

    public void writeChars(char[] chars) {
        Objects.requireNonNull(chars);
        int arrSize = chars.length;
        writeLen(arrSize);
        limit(limit + arrSize * 2);
        for (int i = 0; i < arrSize; i++) {
            Bits.putChar(data, limit - (arrSize - i) * 2, chars[i]);
        }
    }

    public void writeInts(int[] integers) {
        Objects.requireNonNull(integers);
        int arrSize = integers.length;
        writeLen(arrSize);
        limit(limit + arrSize * 4);
        for (int i = 0; i < arrSize; i++) {
            Bits.putInt(data, limit - (arrSize - i) * 4, integers[i]);
        }
    }

    public void writeFloats(float[] floats) {
        Objects.requireNonNull(floats);
        int arrSize = floats.length;
        writeLen(arrSize);
        limit(limit + arrSize * 4);
        for (int i = 0; i < arrSize; i++) {
            Bits.putFloat(data, limit - (arrSize - i) * 4, floats[i]);
        }
    }

    public void writeLongs(long[] longs) {
        Objects.requireNonNull(longs);
        int arrSize = longs.length;
        writeLen(arrSize);
        limit(limit + arrSize * 8);
        for (int i = 0; i < arrSize; i++) {
            Bits.putLong(data, limit - (arrSize - i) * 8, longs[i]);
        }
    }

    public void writeDoubles(double[] doubles) {
        Objects.requireNonNull(doubles);
        int arrSize = doubles.length;
        writeLen(arrSize);
        limit(limit + arrSize * 8);
        for (int i = 0; i < arrSize; i++) {
            Bits.putDouble(data, limit - (arrSize - i) * 8, doubles[i]);
        }
    }

    public byte[] readBytes() {
        int arrSize = getLen(true);
        checkBound(position + arrSize, position, limit);
        byte[] res = (byte[]) Array.newInstance(byte.class, arrSize);

        for (int i = 0; i < arrSize; i++) {
            res[i] = data[position++];
        }
        return res;
    }

    public boolean[] readBooleans() {
        int arrSize = getLen(true);
        checkBound(position + arrSize, position, limit);
        boolean[] res = (boolean[]) Array.newInstance(boolean.class, arrSize);

        for (int i = 0; i < arrSize; ++i) {
            res[i] = Bits.getBoolean(data, position++);
        }
        return res;
    }

    public short[] readShorts() {
        int arrSize = getLen(true);
        checkBound(position + arrSize * 2, position, limit);
        short[] res = (short[]) Array.newInstance(short.class, arrSize);
        for (int i = 0; i < arrSize; ++i) {
            res[i] = Bits.getShort(data, position);
            position += 2;
        }
        return res;
    }

    public char[] readChars() {
        int arrSize = getLen(true);
        checkBound(position + arrSize * 2, position, limit);
        char[] res = (char[]) Array.newInstance(char.class, arrSize);
        for (int i = 0; i < arrSize; ++i) {
            res[i] = Bits.getChar(data, position);
            position += 2;
        }
        return res;
    }

    public int[] readInts() {
        int arrSize = getLen(true);
        checkBound(position + arrSize * 4, position, limit);
        int[] res = (int[]) Array.newInstance(int.class, arrSize);
        for (int i = 0; i < arrSize; ++i) {
            res[i] = Bits.getInt(data, position);
            position += 4;
        }
        return res;
    }

    public float[] readFloats() {
        int arrSize = getLen(true);
        checkBound(position + arrSize * 4, position, limit);
        float[] res = (float[]) Array.newInstance(float.class, arrSize);
        for (int i = 0; i < arrSize; ++i) {
            res[i] = Bits.getFloat(data, position);
            position += 4;
        }
        return res;
    }

    public long[] readLongs() {
        int arrSize = getLen(true);
        checkBound(position + arrSize * 8, position, limit);
        long[] res = (long[]) Array.newInstance(long.class, arrSize);
        for (int i = 0; i < arrSize; ++i) {
            res[i] = Bits.getLong(data, position);
            position += 8;
        }
        return res;
    }

    public double[] readDoubles() {
        int arrSize = getLen(true);
        checkBound(position + arrSize * 8, position, limit);
        double[] res = (double[]) Array.newInstance(double.class, arrSize);
        for (int i = 0; i < arrSize; ++i) {
            res[i] = Bits.getDouble(data, position);
            position += 8;
        }
        return res;
    }

    private void checkBound(int index, int minIndex, int maxIndex) {
        if (index < minIndex && index >= maxIndex)
            throw new InvalidIndexException(String.format("%d out of bound %d - %d", index, minIndex, maxIndex));
    }

    /**
     * 返回缓冲区的大小
     */
    public int capacity() {
        return capacity;
    }

    /**
     * 返回缓冲区当前的指向位置
     */
    public int position() {
        return position;
    }

    /**
     * 返回缓冲区的数据大小
     */
    public int limit() {
        return limit;
    }

    private int limit(int limit) {
        if (limit < 0) {
            throw new InvalidIndexException(String.format("Invalid limit %d", limit));
        }
        this.limit = limit;

        if (limit >= capacity) {
            grow(limit);
        }
        return limit;
    }

    /**
     * 压缩缓冲区
     */
    public Bytes compact() {
        byte[] bytes = new byte[capacity - limit];
        System.arraycopy(data, 0, bytes, 0, capacity - limit);
        return new Bytes(bytes, position, mark, limit);
    }

    /**
     * 压缩缓冲区
     */
    public byte[] compactData() {
        byte[] bytes = new byte[limit];
        System.arraycopy(data, 0, bytes, 0, limit);
        return bytes;
    }

    private void writeLen(int len) {
        if (len < 0) throw new NegativeArraySizeException();
        byte[] bytes;
        if (len <= 0x7F) {
            bytes = new byte[1];
            bytes[0] = (byte) (len & 0x7F);
        } else if (len <= 0x3FFF) {
            bytes = new byte[2];
            bytes[0] = (byte) ((len >>> 7) | 0x80);
            bytes[1] = (byte) (len & 0x7F);
        } else if (len <= 0x1FFFFF) {
            bytes = new byte[3];
            bytes[0] = (byte) ((len >>> 14) | 0x80);
            bytes[1] = (byte) ((len >>> 7) | 0x80);
            bytes[2] = (byte) (len & 0x7F);
        } else {
            bytes = new byte[4];
            bytes[0] = (byte) ((len >>> 21) | 0x80);
            bytes[1] = (byte) ((len >>> 14) | 0x80);
            bytes[2] = (byte) ((len >>> 7) | 0x80);
            bytes[3] = (byte) (len & 0x7F);
        }
        for (byte b : bytes) {
            writeByte(b);
        }
    }

    private int getLen(final boolean read) {
        int p = 0;
        int n = 0;
        int cur;
        do {
            cur = position + p;
            checkBound(cur, position, limit);
            if ((data[cur] & 0x80) == 0x80) {
                // 1xxx xxxx
                n = (n << 7) | (data[cur] & 0x7F);
                ++p;
            } else {
                // 0xxx xxxx
                n = (n << 7) | (data[cur] & 0x7F);
                break;
            }
        } while (p < 4);

        if (n < 0) throw new NegativeArraySizeException();

        if (!read) {
            mark();
        }
        position = cur + 1;
        return n;
    }

    public byte[] rangeOf(int off, int len) {
        if (off < position || off + len > limit) {
            throw new InvalidIndexException("Invalid off or len");
        }

        return Arrays.copyOfRange(data, off, off + len);
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }

    public void clear() {
        capacity = DEFAULT_CAPACITY;
        position = 0;
        limit = position;
        mark = -1;
        data = new byte[capacity];
    }
}
