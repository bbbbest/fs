import test.AnimalAction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectTest {
    @Test
    void reflect_test() {
        System.out.println(Arrays.toString(allPublicMethodName(AnimalAction.class)));
    }

    private static String[] allPublicMethodName(Class<?> cl) {
        Method[] methods = cl.getMethods();
        String[] mNames = new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            mNames[i] = methods[i].getName();
        }
        return mNames;
    }
}
