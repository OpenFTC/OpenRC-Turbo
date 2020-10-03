package com.technototes.library.logging;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Repeatable(Log.Logs.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(value={FIELD, LOCAL_VARIABLE, METHOD})
public @interface Log {

    int index() default -1;

    String name() default "Log";

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Logs {
        Log[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={FIELD, LOCAL_VARIABLE, METHOD})
    @interface Number{
        int index() default -1;

        String name() default "Number";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={FIELD, LOCAL_VARIABLE, METHOD})
    @interface NumberBar{
        int index() default -1;

        double min() default -1;
        double max() default 1;
        double scale() default 0.1;

        String name() default "NumberBar";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={FIELD, LOCAL_VARIABLE, METHOD})
    @interface NumberSlider{
        int index() default -1;

        double min() default -1;
        double max() default 1;
        double scale() default 0.1;

        String name() default "NumberSlider";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={FIELD, LOCAL_VARIABLE, METHOD})
    @interface Boolean{
        int index() default -1;

        String valueWhenTrue() default "true";
        String valueWhenFalse() default "false";

        String name() default "Boolean";
    }
}
