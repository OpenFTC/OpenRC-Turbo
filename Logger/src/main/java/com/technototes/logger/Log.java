package com.technototes.logger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;

/** The root annotation for annotation logging, also doubles as a basic string log
 * @author Alex Stedman
 */
@Repeatable(Log.Logs.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(value={FIELD, LOCAL_VARIABLE, METHOD})
public @interface Log {
    /** Store index for this annotation (position in telemetry)
     *
     * @return The index
     */
    int index() default -1;

    /** Store the name for this annotation to be be beside
     *
     * @return The name as a string
     */
    String name() default "Log";

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Logs {
        Log[] value();
    }

    /** Log a number
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={FIELD, LOCAL_VARIABLE, METHOD})
    @interface Number{
        /** Store index for this annotation (position in telemetry)
         *
         * @return The index
         */
        int index() default -1;

        /** Store the name for this annotation to be be beside
         *
         * @return The name as a string
         */
        String name() default "Number";
    }

    /** Log a number, but store it as a number bar
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={FIELD, LOCAL_VARIABLE, METHOD})
    @interface NumberBar{
        /** Store index for this annotation (position in telemetry)
         *
         * @return The index
         */
        int index() default -1;

        /** Store the min for the number bar to scale to
         *
         * @return The min
         */
        double min() default -1;
        /** Store the max for the number bar to scale to
         *
         * @return The max
         */
        double max() default 1;
        /** Store the scale for the number bar to scale to
         *
         * @return The scale
         */
        double scale() default 0.1;

        /** Store the name for this annotation to be be beside
         *
         * @return The name as a string
         */
        String name() default "NumberBar";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={FIELD, LOCAL_VARIABLE, METHOD})
    @interface NumberSlider{
        /** Store index for this annotation (position in telemetry)
         *
         * @return The index
         */
        int index() default -1;

        /** Store the min for the number bar to scale to
         *
         * @return The min
         */
        double min() default -1;
        /** Store the max for the number bar to scale to
         *
         * @return The max
         */
        double max() default 1;
        /** Store the scale for the number bar to scale to
         *
         * @return The scale
         */
        double scale() default 0.1;

        /** Store the name for this annotation to be be beside
         *
         * @return The name as a string
         */
        String name() default "NumberSlider";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value={FIELD, LOCAL_VARIABLE, METHOD})
    @interface Boolean{
        /** Store index for this annotation (position in telemetry)
         *
         * @return The index
         */
        int index() default -1;
        /** Store the string when the annotated method returns true
         *
         * @return The string
         */
        String valueWhenTrue() default "true";
        /** Store the string when the annotated method returns false
         *
         * @return The string
         */
        String valueWhenFalse() default "false";
        /** Store the name for this annotation to be be beside
         *
         * @return The name as a string
         */
        String name() default "Boolean";
    }
}
