package com.technototes.library.logging;

import com.technototes.library.logging.entry.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Supplier;


public class Logger {

    public ArrayList<Entry> entries;
    public Telemetry telemetry;
    public Object root;

    public Logger(Telemetry tel, Object r) {
        root = r;
        telemetry = tel;
        entries = new ArrayList<>();
        configure(r);
    }

    public void update() {
        if(entries == null){
            entries = new ArrayList<>();
        }else {
            entries.forEach((s) -> {
                //System.out.println("e");
                telemetry.addLine(s.toString());
            });
        }
    }

    public void configure(Object root) {
        for (Field field : root.getClass().getDeclaredFields()) {
            try {
                Object o = field.get(root);
                if (o instanceof Loggable) {
                    configure(o);
                } else if (field.isAnnotationPresent(Log.class) || field.isAnnotationPresent(Log.Number.class) ||
                        field.isAnnotationPresent(Log.NumberSlider.class) || field.isAnnotationPresent(Log.NumberBar.class)) {
                    if(field.getClass().isPrimitive()){
                        set(field.getDeclaredAnnotations(), field, root);
                    }else {
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
    public void set(Annotation[] a, Method m, Object root){
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
    public void set(Annotation[] a, Field m, Object root){
        set(a, () -> {
            try {
                return m.get(root);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
    public void set(Annotation[] a, Supplier<?> m) {
        Entry e = null;
        for(Annotation as : a){
            if(as instanceof Log.NumberSlider){
                e = new NumberSliderEntry(((Log.NumberSlider) as).name(), (Supplier<Number>)m,
                        ((Log.NumberSlider) as).x(), ((Log.NumberSlider) as).y(), ((Log.NumberSlider) as).min(),
                        ((Log.NumberSlider) as).max(), ((Log.NumberSlider) as).scale());
                entries.add(e);
                return;
            } else if(as instanceof Log.NumberBar){
                e = new NumberBarEntry(((Log.NumberBar) as).name(), (Supplier<Number>)m,
                        ((Log.NumberBar) as).x(), ((Log.NumberBar) as).y(), ((Log.NumberBar) as).min(),
                        ((Log.NumberBar) as).max(), ((Log.NumberBar) as).scale());
                entries.add(e);
                return;
            } else if (as instanceof Log.Number){
                e = new NumberEntry(((Log.Number) as).name(), (Supplier<Number>)m,
                        ((Log.Number) as).x(), ((Log.Number) as).y());
                entries.add(e);
                return;
            } else if (as instanceof Log){
                e = new StringEntry(((Log) as).name(), (Supplier<String>)m,
                        ((Log) as).x(), ((Log) as).y());
                entries.add(e);
                return;
            }
        }
    }
    private Object invoke(Method m, Object r){
        m.setAccessible(true);
        try {
            return m.invoke(r);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
