package cn.zzu.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Invoker<T> {
    private String identifier;
    private Class<T> tcl;
    private T target;
    private List<Method> exportedMethods;

    public Invoker(final String identifier, final Class<T> tcl, final T target, String[] exports) {
        if (identifier == null || tcl == null || target == null) throw new NullPointerException();
        if (!tcl.isInterface()) throw RpcException.invalidServiceClass(tcl);

        this.identifier = identifier;
        this.tcl = tcl;
        this.target = target;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Class<T> getTcl() {
        return tcl;
    }

    public T getTarget() {
        return target;
    }

    public Object invoke(Invocation invocation) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        checkInterface(invocation);
        Method method = tcl.getDeclaredMethod(invocation.getMethod(), invocation.getParamTypes());
        method.setAccessible(true);
        return method.invoke(target, invocation.getArgs());
    }

    private void checkInterface(Invocation invocation) {
        if (!invocation.getInterface().equals(tcl.getCanonicalName()))
            throw new IllegalInvocationException("illegal interface");
    }
}
