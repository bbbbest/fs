import cn.zzu.ss.core.Bytes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BytesTest {

    @Test
    void grow() {
        Bytes bytes = Bytes.newBytes();
        System.out.println("0-" + bytes.read());
        System.out.println("0-" + bytes.readable());
        System.out.println("0-" + bytes.writable());

        bytes.writeByte((byte) 18);
        bytes.writeShort(520);
        System.out.println("1-" + bytes.read());
        System.out.println("1-" + bytes.readable());
        System.out.println("1-" + bytes.writable());
        System.out.println("1-" + bytes.limit());

        System.out.println("2-" + bytes.peekByte());
        System.out.println("2-" + bytes.readByte());
        System.out.println("2-" + bytes.read());
        System.out.println("2-" + bytes.readable());
        System.out.println("2-" + bytes.writable());
        System.out.println("2-" + bytes.limit());

        bytes.align();

        System.out.println("3-" + bytes.peekShort());
        System.out.println("3-" + bytes.readShort());
        System.out.println("3-" + bytes.read());
        System.out.println("3-" + bytes.readable());
        System.out.println("3-" + bytes.writable());
        System.out.println("3-" + bytes.limit());
        System.out.println("3-" + bytes.capacity());
    }

    @Test
    void byebuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);

        byteBuffer.put((byte) 0x12);
        byteBuffer.putShort((short) 520);
        byteBuffer.flip();
        System.out.println(byteBuffer.get());
        System.out.println(byteBuffer.mark());
        System.out.println(byteBuffer.reset());
        System.out.println(byteBuffer.getShort());
    }

    @Test
    void utf_8() throws IOException {
        Bytes bytes = Bytes.newBytes();
        bytes.writeInt(100);
        bytes.writeString("你无法从UNICODE字符数判断出UTF-8文本的字节数，因为UTF-8是一种变长编码它需要用2个字节编码那些用扩展ASCII字符集只需1个字节的字符 ISO Latin-1 是UNICODE的子集，但不是UTF-8的子集 8位字符的UTF-8编码会被email网关过滤，因为internet信息最初设计为7位ASCII码。因此产生了UTF-7编码。 UTF-8 在它的表示中使用值100xxxxx的几率超过50%， 而现存的实现如ISO 2022， 4873， 6429， 和8859系统，会把它错认为是C1 控制码。因此产生了UTF-7.5编码。你无法从UNICODE字符数判断出UTF-8文本的字节数，因为UTF-8是一种变长编码它需要用2个字节编码那些用扩展ASCII字符集只需1个字节的字符 ISO Latin-1 是UNICODE的子集，但不是UTF-8的子集 8位字符的UTF-8编码会被email网关过滤，因为internet信息最初设计为7位ASCII码。因此产生了UTF-7编码。 UTF-8 在它的表示中使用值100xxxxx的几率超过50%， 而现存的实现如ISO 2022， 4873， 6429， 和8859系统，会把它错认为是C1 控制码。因此产生了UTF-7.5编码。");
        System.out.println("int-" + bytes.readInt());
        System.out.println(bytes.read());
        System.out.println(bytes.readable());
        System.out.println(bytes.writable());
        System.out.println(bytes.limit());
    }

    @Test
    void class_equals() {
        Bytes bytes1 = Bytes.newBytes();
        Bytes bytes2 = Bytes.newBytes();
        System.out.println(bytes1.getClass() == bytes2.getClass());
    }
}
