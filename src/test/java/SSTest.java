import cn.zzu.rpc.Invocation;
import cn.zzu.rpc.netty.Packet;
import cn.zzu.ss.core.Deserializer;
import cn.zzu.ss.core.Serializer;
import test.*;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.*;

public class SSTest {

    private static void analyzeAndPrint(final Object o, int loop) {
        Runtime runtime;
        long totalMemory;
        long startTime;
        long endTime;
        long startMemory;
        long endMemory;
        byte[] data;

        runtime = Runtime.getRuntime();
        totalMemory = runtime.totalMemory();
        System.out.println("Total memory:\t" + totalMemory);

        Serializer serializer = new Serializer();
        System.out.println("begin serialize...");
        startMemory = runtime.freeMemory();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < loop; ++i) serializer.writeObject(o);
        endTime = System.currentTimeMillis();
        endMemory = runtime.freeMemory();

        data = serializer.getSerialData();

        System.out.println("Serialized!\nTime:\t" + (endTime - startTime) + ", Space:\t" + data.length + ", Memory:\t" + (startMemory - endMemory));

        Deserializer deserializer = new Deserializer(serializer.getSerialData());

        startMemory = runtime.freeMemory();
        startTime = System.currentTimeMillis();

        for (int i = 0; i < loop; i++) {
            deserializer.readObject();
        }

        endTime = System.currentTimeMillis();
        endMemory = runtime.freeMemory();

        System.out.println("Deserialize!\nTime:\t" + (endTime - startTime) + ", Memory:\t" + (startMemory - endMemory));


        try {
            FileOutputStream fos = new FileOutputStream("ana.obj");
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void primitive_test() {
        byte b = 1;
        boolean bool = false;
        short s = 2;
        char c = 'a';
        int i = 4;
        float f = 5;
        long l = 6;
        double d = 7;
        long start = System.currentTimeMillis();
        Serializer serializer = new Serializer();
        serializer.writeObject(b);
        serializer.writeObject(bool);
        serializer.writeObject(s);
        serializer.writeObject(c);
        serializer.writeObject(i);
        serializer.writeObject(f);
        serializer.writeObject(l);
        serializer.writeObject(d);
        long end = System.currentTimeMillis();

        byte[] data = serializer.getSerialData();
        System.out.println("time: " + (end - start) + ",    space: " + data.length);

        Deserializer deserializer = new Deserializer(data);

        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
    }

    @Test
    void ref_primitive() {
        Byte b = 1;
        Boolean bool = false;
        Short s = 2;
        Character c = 'a';
        Integer i = 4;
        Float f = 5f;
        Long l = 6L;
        Double d = null;
        long start = System.currentTimeMillis();
        Serializer serializer = new Serializer();
        serializer.writeObject(b);
        serializer.writeObject(bool);
        serializer.writeObject(s);
        serializer.writeObject(c);
        serializer.writeObject(i);
        serializer.writeObject(f);
        serializer.writeObject(l);
        serializer.writeObject(d);
        long end = System.currentTimeMillis();

        byte[] data = serializer.getSerialData();
        System.out.println("time: " + (end - start) + ",    space: " + data.length);

        Deserializer deserializer = new Deserializer(data);

        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
    }

    @Test
    void enum_test() {
        Enum man = Sex.MAN;
        Enum woman = Sex.WOMAN;

        Serializer serializer = new Serializer();
        serializer.writeObject(man);
        serializer.writeObject(woman);

        Deserializer deserializer = new Deserializer(serializer.getSerialData());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
    }

    @Test
    void man_woman_test() {
        Man man = new Man();
        man.setName("Tom");
        man.setAge(22);
        man.setStrength(80);

        Woman woman = new Woman();
        woman.setName("Jane");
        woman.setAge(20);
        woman.setSkill(66);

        Serializer serializer = new Serializer();
        serializer.writeObject(man);
        serializer.writeObject(woman);

        byte[] data = serializer.getSerialData();

        Deserializer deserializer = new Deserializer(data);

        Man dm = (Man) deserializer.readObject();
        Woman dwm = (Woman) deserializer.readObject();

        System.out.println(dm);
        System.out.println(dwm);
    }

    @Test
    void family_test() {
        Man man = new Man();
        man.setName("Tom");
        man.setAge(22);
        man.setStrength(80);

        Woman woman = new Woman();
        woman.setName("Jane");
        woman.setAge(20);
        woman.setSkill(66);

        Family family = new Family(man, woman);

        Serializer serializer = new Serializer();

        serializer.writeObject(family);

        byte[] data = serializer.getSerialData();

        //        System.out.println(data.length);

        Deserializer deserializer = new Deserializer(data);

        System.out.println(deserializer.readObject());
    }

    @Test
    @SuppressWarnings("unchecked")
    void collection_test() {
        List col = new LinkedList();
        col.add(new Man());
        col.add(new Woman());
        Serializer serializer = new Serializer();

        serializer.writeObject(col);

        Deserializer deserializer = new Deserializer(serializer.getSerialData());

        System.out.println(deserializer.readObject());

    }

    @Test
    @SuppressWarnings("unchecked")
    void map_test() {
        Map map = new HashMap();

        map.put("man", new Man());
        map.put("woman", new Woman());
        map.put("345", 666);
        map.put("666", "666");

        Serializer serializer = new Serializer();
        serializer.writeObject(map);
        Deserializer deserializer = new Deserializer(serializer.getSerialData());
        System.out.println(deserializer.readObject());
    }

    @Test
    void array_test() {
        int[] ints = {1, 2, 3, 4};
        int[][] iints = {{1, 2, 3, 4}, {1, 2, 3, 4}};

        Serializer serializer = new Serializer();
        serializer.writeObject(ints);
        serializer.writeObject(iints);

        Deserializer deserializer = new Deserializer(serializer.getSerialData());
        System.out.println(Arrays.toString((int[]) deserializer.readObject()));
        System.out.println(Arrays.toString((int[][]) deserializer.readObject()));
        System.out.println(deserializer.readObject());
    }

    @Test
    @SuppressWarnings("unchecked")
    void set_test() {
        Set set = new HashSet();
        set.add(new Man());
        set.add(new Woman());

        Serializer serializer = new Serializer();
        serializer.writeObject(set);

        Deserializer deserializer = new Deserializer(serializer.getSerialData());

        System.out.println(deserializer.readObject());
    }

    @Test
    void ana() {
        Man man = new Man();
        man.setName("Tom");
        man.setAge(22);
        man.setStrength(80);

        Woman woman = new Woman();
        woman.setName("Jane");
        woman.setAge(20);
        woman.setSkill(66);

        Family family = new Family(man, woman);

        analyzeAndPrint(family, 100);

        try {
            ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream("family.obj"));
            long start = System.currentTimeMillis();

            for (int i = 0; i < 100; i++) {
                o.writeObject(family);
            }

            long end = System.currentTimeMillis();

            System.out.println(end - start);
            o.flush();
            o.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void ref_array() {
        Person[] people = {new Man(), new Woman()};

        Serializer serializer = new Serializer();
        serializer.writeObject(people);
        Deserializer deserializer = new Deserializer(serializer.getSerialData());
        System.out.println(Arrays.toString((Person[]) deserializer.readObject()));
    }

    @Test
    void packet_ss() {
        Serializer serializer = new Serializer();
        serializer.writeObject(new Invocation());
        Deserializer deserializer = new Deserializer(serializer.getSerialData());
        System.out.println(deserializer.readObject());
    }

    @Test
    void class_serial() {
        Map map = new HashMap();
        map.put(AnimalAction.class, Person.class);

        List list = new ArrayList();
        list.add(AnimalAction.class);
        list.add("123");
        String[] strings = new String[]{"123", "456"};
        Class[] classes = new Class[]{AnimalAction.class, Man.class};
        Serializer serializer = new Serializer();
        serializer.writeObject(map);
        serializer.writeObject(list);
        serializer.writeObject(strings);
        serializer.writeObject(classes);
        // COLLECTION & MAP
        Deserializer deserializer = new Deserializer(serializer.getSerialData());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
        System.out.println(deserializer.readObject());
    }

    @Test
    void invocation_test() throws NoSuchMethodException {
        Method method = AnimalAction.class.getDeclaredMethod("learn", Object.class);
        Invocation invocation = new Invocation("action", AnimalAction.class, method, new Object[] {new Man()});
        System.out.println(invocation);
        System.out.println(invocation.hashCode());
//        Serializer serializer = new Serializer();
//        serializer.writeObject(invocation);
//        Deserializer deserializer = new Deserializer(serializer.getSerialData());
//        System.out.println(deserializer.readObject());
    }

    @Test
    void object_array() {
        Object[] array = new Object[]{new Object(), new Object()};
        Serializer serializer = new Serializer();
        serializer.writeObject(array);
        Deserializer deserializer = new Deserializer(serializer.getSerialData());
        System.out.println(deserializer.readObject());
    }

    @Test
    void packet() throws NoSuchMethodException {
        Method method = AnimalAction.class.getDeclaredMethod("learn", Object.class);
        Invocation invocation = new Invocation("action", AnimalAction.class, method, new Object[] {new Man()});
        Packet p = Packet.missService(invocation.getInvocationID(), invocation.getIdentifier());

        Serializer serializer = new Serializer();
        serializer.writeObject(p);
        Deserializer deserializer = new Deserializer(serializer.getSerialData());
        System.out.println(deserializer.readObject());
    }
}
