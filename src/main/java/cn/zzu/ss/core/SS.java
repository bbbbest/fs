package cn.zzu.ss.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SS {
    int level() default SSLevel.PUBLIC | SSLevel.PROTECTED | SSLevel.PRIVATE;

    String[] includes() default "";

    String[] excludes() default "";
}
