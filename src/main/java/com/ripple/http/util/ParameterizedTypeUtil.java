package com.ripple.http.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Author: fanyafeng
 * Data: 2020/7/22 09:44
 * Email: fanyafeng@live.cn
 * Description:
 */
public class ParameterizedTypeUtil {

    private ParameterizedTypeUtil() {

    }

    /**
     * 获取class上定义的泛型类型
     */
    public static Type getDirectParameterizedType(Class clazz) {
        /**
         * 迭代查找类上面的泛型
         */
        ParameterizedType p = findParameterizedType(clazz.getGenericSuperclass());
        return p.getActualTypeArguments()[0];
    }

    /**
     *
     */

    /**
     * 迭代向上找具体参数类型
     *
     * @param type
     * @return
     */
    public static ParameterizedType findParameterizedType(Type type) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
        }
        Type genericSuperclass = ((Class<?>) type).getGenericSuperclass();
        return findParameterizedType(genericSuperclass);
    }

    /**
     * 获取泛型参数
     *
     * @param ownerType     当前类型
     * @param declaredClass 最终的父类型
     * @param paramIndex    参数位于参数列表的第几个
     * @return 真实的参数类型
     */
    public static Type getParameterizedType(Type ownerType, Class<?> declaredClass, int paramIndex) {

        /**
         * 需要处理的class
         */
        Class<?> clazz;
        ParameterizedType pt;
        Type[] ats = null;
        TypeVariable<?>[] tps = null;
        if (ownerType instanceof ParameterizedType) {
            pt = (ParameterizedType) ownerType;
            clazz = (Class<?>) pt.getRawType();
            ats = pt.getActualTypeArguments();
            tps = clazz.getTypeParameters();
        } else {
            clazz = (Class<?>) ownerType;
        }
        if (declaredClass == clazz) {
            if (ats != null) {
                return ats[paramIndex];
            }
            return Object.class;
        }

        /**
         * 获取class的实现接口
         */
        Type[] types = clazz.getGenericInterfaces();
        if (types != null) {
            for (Type t : types) {
                if (t instanceof ParameterizedType) {
                    Class<?> cls = (Class<?>) ((ParameterizedType) t).getRawType();//获取真实类型
                    if (declaredClass.isAssignableFrom(cls)) {
                        try {
                            getTrueType(getParameterizedType(t, declaredClass, paramIndex), tps, ats);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            if (declaredClass.isAssignableFrom(superClass)) {
                return getTrueType(getParameterizedType(clazz.getGenericSuperclass(), declaredClass, paramIndex), tps, ats);
            }
        }

        throw new IllegalArgumentException("FindGenericType:" + ownerType +
                ", declaredClass: " + declaredClass + ", index: " + paramIndex);
    }

    private static Type getTrueType(Type type, TypeVariable<?>[] typeVariables, Type[] actualTypes) {
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            String name = tv.getName();
            if (actualTypes != null) {
                for (int i = 0; i < typeVariables.length; i++) {
                    if (name.equals(typeVariables[i].getName())) {
                        return actualTypes[i];
                    }
                }
            }
            return tv;
        } else if (type instanceof GenericArrayType) {
            Type ct = ((GenericArrayType) type).getGenericComponentType();
            if (ct instanceof Class<?>) {
                return Array.newInstance((Class<?>) ct, 0).getClass();
            }
        }
        return type;
    }

    public static <T> boolean hasInterfaceT(T t) {
        Type[] params = t.getClass().getGenericInterfaces();
        Type type = params[0];
        boolean finalNeedType;
        if (params.length > 1) {
            if (!(type instanceof ParameterizedType)) {
//                throw new IllegalStateException("没有填写泛型参数");
                finalNeedType = false;
            } else {
                finalNeedType = true;
            }
        } else {
            finalNeedType = true;
        }
        return finalNeedType;
    }

    public static <T> Class getTClass(T t) {
        Type[] params = t.getClass().getGenericInterfaces();
        Type type = params[0];
        Type finalNeedType;
        if (params.length > 1) {
            if (!(type instanceof ParameterizedType)) throw new IllegalStateException("没有填写泛型参数");
            finalNeedType = ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            finalNeedType = type;
        }
        final Class clazz = getClass(finalNeedType, 0);
        return clazz;
    }


    public static Class getGenericClass(ParameterizedType parameterizedType, int i) {
        Object genericClass = parameterizedType.getActualTypeArguments()[i];
        if (genericClass instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) genericClass).getRawType();
        } else if (genericClass instanceof GenericArrayType) {
            return (Class) ((GenericArrayType) genericClass).getGenericComponentType();
        } else if (genericClass instanceof TypeVariable) {
            return (Class) getClass(((TypeVariable) genericClass).getBounds()[0], 0);
        } else {
            return (Class) genericClass;
        }
    }


    public static Class getClass(Type type, int i) {
        if (type instanceof ParameterizedType) {
            return getGenericClass((ParameterizedType) type, i);
        } else if (type instanceof TypeVariable) {
            return (Class) getClass(((TypeVariable) type).getBounds()[0], 0);
        } else {
            return (Class) type;
        }
    }
}