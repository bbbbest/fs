package cn.zzu.rpc;

import cn.zzu.ss.core.SS;

import java.lang.reflect.Method;
import java.util.Arrays;

@SS
public class Invocation {
    private String identifier;
    private String _interface;
    private String method;
    private int paramCount;
    private Class[] paramTypes;
    private Object[] args;

    public Invocation(final String identifier, final Class<?> _interface, final Method method, Object[] args) {
        this.identifier = identifier;
        this._interface = _interface.getCanonicalName();
        this.method = method.getName();
        this.paramCount = method.getParameterCount();
        this.args = args == null ? new Object[0] : args;
        assert paramCount == this.args.length : "invoked method's arguments must equals to args' length";
        this.paramTypes = method.getParameterTypes();
    }

    public Invocation() {
    }

    public String getInterface() {
        return _interface;
    }

    public String getMethod() {
        return method;
    }

    public int getParamCount() {
        return paramCount;
    }

    public Class[] getParamTypes() {
        return paramTypes;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invocation that = (Invocation) o;
        return paramCount == that.paramCount && java.util.Objects
                .equals(identifier, that.identifier) && java.util.Objects
                .equals(_interface, that._interface) && java.util.Objects.equals(method, that.method) && Arrays
                .equals(paramTypes, that.paramTypes) && Arrays.equals(args, that.args);
    }

    @Override
    public String toString() {
        return "Invocation{" + "identifier='" + identifier + '\'' + ", _interface='" + _interface + '\'' + ", method='" + method + '\'' + ", paramCount=" + paramCount + ", _paramTypes=" + Arrays
                .toString(paramTypes) + ", args=" + Arrays.toString(args) + '}';
    }
}
