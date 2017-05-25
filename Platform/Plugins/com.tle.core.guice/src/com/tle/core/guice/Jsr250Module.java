package com.tle.core.guice;

import com.google.inject.AbstractModule;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.inject.matcher.Matchers.any;

public class Jsr250Module extends AbstractModule {

    public static final Method findMethodWithAnnotation(Class<?> type, Class<? extends Annotation> annotationType, boolean lookInSuperClass) {
        Class<?> currentClass = type;
        while (currentClass != Object.class && currentClass != null) {
            Method[] methods = currentClass.getDeclaredMethods();
            for (Method method : methods) {
                Annotation fromElement = method.getAnnotation(annotationType);
                if (fromElement != null) {
                    return method;
                }
            }
            if (lookInSuperClass) {
                currentClass = currentClass.getSuperclass();
            } else {
                break;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        bindListener(any(), new TypeListener() {
            public <I> void hear(TypeLiteral<I> injectableType, TypeEncounter<I> encounter) {
                Class<? super I> type = injectableType.getRawType();
                final Method method = findMethodWithAnnotation(type, PostConstruct.class, true);
                if (method != null) {
                    final PostConstruct annotation = method.getAnnotation(PostConstruct.class);
                    encounter.register(new InjectionListener<I>() {
                        public void afterInjection(I injectee) {

                            try {
                                method.setAccessible(true);
                                method.invoke(injectee);
                            } catch (InvocationTargetException ie) {
                                Throwable e = ie.getTargetException();
                                throw new ProvisionException(e.getMessage(), e);
                            } catch (IllegalAccessException e) {
                                throw new ProvisionException(e.getMessage(), e);
                            }
                        }
                    });
                }
            }
        });

    }

}