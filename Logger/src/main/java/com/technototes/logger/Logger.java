package com.technototes.logger;

import com.technototes.logger.entry.BooleanEntry;
import com.technototes.logger.entry.Entry;
import com.technototes.logger.entry.NumberBarEntry;
import com.technototes.logger.entry.NumberEntry;
import com.technototes.logger.entry.NumberSliderEntry;
import com.technototes.logger.entry.StringEntry;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** The class to manage logging
 * @author Alex Stedman
 */
public class Logger {

    private Entry[] entries;
    private ArrayList<Entry> unindexedEntries;
    private Telemetry telemetry;
    private Object root;
    private int total = 0, max = -1;
    /** The divider between the tag and the entry for telemetry (default ':')
     *
     */
    public char captionDivider = ':';

    /** Instantiate the logger
     *
     * @param tel The Telemetry object the robot uses
     * @param r The Object of the OpMode (pass "this" as this parameter)
     */
    public Logger(Telemetry tel, Object r) {
        root = r;
        telemetry = tel;
        tel.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        entries = new Entry[30];
        unindexedEntries = new ArrayList<>();
        configure(r);

        mergeEntries();

    }

    private void mergeEntries() {
        for(int i = 0; unindexedEntries.size() > 0; i++){
            if(entries[i] == null) {
                entries[i] = unindexedEntries.remove(0).setIndex(i);
            }
        }
        entries = Arrays.copyOfRange(entries, 0, Math.max(total, max+1));

    }

    /** Update the logged items in temeletry
     *
     */
    public void update() {
        for(int i = 0; i < entries.length; i++){
            telemetry.addLine((i > 9 ? i+"| " : i+" | ") + (entries[i] == null ? "" :
                    entries[i].getTag().replace('`', captionDivider)+entries[i].toString()));
        }
    }

    private void configure(Object root) {
        for (Field field : root.getClass().getDeclaredFields()) {
            try {
                Object o = field.get(root);
                if (o instanceof Loggable && field.isAnnotationPresent(Log.class)) {
                    configure(o);
                } else if (field.isAnnotationPresent(Log.class) || field.isAnnotationPresent(Log.Number.class) ||
                        field.isAnnotationPresent(Log.NumberSlider.class) || field.isAnnotationPresent(Log.NumberBar.class)
                || field.isAnnotationPresent(Log.Boolean.class)) {
                    if(field.getType().isPrimitive() || o instanceof String) {
                        set(field.getDeclaredAnnotations(), field, root);
                    }else if(getCustom(o) != null) {
                        set(field.getDeclaredAnnotations(), getCustom(o));
                    }else{
                        for (Method m : o.getClass().getDeclaredMethods()) {
                            if (m.isAnnotationPresent(Log.class)) {
                                set(field.getDeclaredAnnotations(), m, o);
                            }

                        }
                    }
                }
            } catch (IllegalAccessException e) {
                continue;
            }
        }
        for (Method m : root.getClass().getDeclaredMethods()) {

            set(m.getDeclaredAnnotations(), m, root);
        }

    }
    private void set(Annotation[] a, Method m, Object root){
        set(a, () -> {
            try {
                return m.invoke(root);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
    private void set(Annotation[] a, Field m, Object root){
        set(a, () -> {
            try {
                return m.get(root);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
    private void set(Annotation[] a, Supplier<?> m) {
        Entry e = null;
        for (Annotation as : a) {
            if (as instanceof Log.NumberSlider) {
                e = new NumberSliderEntry(((Log.NumberSlider) as).name(), (Supplier<Number>) m,
                        ((Log.NumberSlider) as).index(), ((Log.NumberSlider) as).min(),
                        ((Log.NumberSlider) as).max(), ((Log.NumberSlider) as).scale(),
                        ((Log.NumberSlider) as).color(), ((Log.NumberSlider) as).sliderBackground(),
                        ((Log.NumberSlider) as).outline(), ((Log.NumberSlider) as).slider());
                processEntry(e);
                break;
            } else if (as instanceof Log.NumberBar) {
                e = new NumberBarEntry(((Log.NumberBar) as).name(), (Supplier<Number>) m,
                        ((Log.NumberBar) as).index(), ((Log.NumberBar) as).min(),
                        ((Log.NumberBar) as).max(), ((Log.NumberBar) as).scale(),
                        ((Log.NumberBar) as).color(), ((Log.NumberBar) as).completeBarColor(),
                        ((Log.NumberBar) as).outline(), ((Log.NumberBar) as).incompleteBarColor());
                processEntry(e);
                break;
            } else if (as instanceof Log.Number) {
                e = new NumberEntry(((Log.Number) as).name(), (Supplier<Number>) m,
                        ((Log.Number) as).index(), ((Log.Number) as).color(),
                        ((Log.Number) as).numberColor());
                processEntry(e);
                break;
            } else if (as instanceof Log) {
                e = new StringEntry(((Log) as).name(), (Supplier<String>) m,
                        ((Log) as).index(), ((Log) as).color(), ((Log) as).format(), ((Log) as).entryColor());
                processEntry(e);
                break;
            } else if (as instanceof Log.Boolean) {
                e = new BooleanEntry(((Log.Boolean) as).name(), (Supplier<Boolean>) m, ((Log.Boolean) as).index(),
                        ((Log.Boolean) as).trueValue(), ((Log.Boolean) as).falseValue(),
                        ((Log.Boolean) as).color(), ((Log.Boolean) as).trueFormat(),
                        ((Log.Boolean) as).falseFormat(), ((Log.Boolean) as).trueColor(),
                        ((Log.Boolean) as).falseColor());
                processEntry(e);
                break;
            }
        }
    }

    private void processEntry(Entry e){
        if(e.getIndex() != -1) {
            if(entries[e.getIndex()] != null){
                unindexedEntries.add(e);
            }   else{
                entries[e.getIndex()] = e;
            }
        }else{
            unindexedEntries.add(e);
        }
        total++;
        max = Math.max(max, e.getIndex());
    }

    /** Get an array of all logger entries
     *
     * @return The array
     */
    public Entry[] getEntries() {
        return entries;
    }

    /** Repeat a String
     *
     * @param s The String to repeat
     * @param num The amount of times to repeat the String
     * @return The String s repeated num times
     */
    public static String repeat(String s, int num){
        return num > 0 ? repeat(s, num-1)+s : "";
    }

    public static Supplier getCustom(Object o){
        if(o instanceof Supplier){
            return (Supplier) o;
        } else if(o instanceof BooleanSupplier){
            return ()->((BooleanSupplier) o).getAsBoolean();
        }else if(o instanceof IntSupplier){
            return ()->((IntSupplier) o).getAsInt();
        }else if(o instanceof DoubleSupplier){
            return ()->((DoubleSupplier) o).getAsDouble();
        }else if(o instanceof Func){
            return ()->((Func) o).value();
        }else {
            return null;
        }
    }
}
