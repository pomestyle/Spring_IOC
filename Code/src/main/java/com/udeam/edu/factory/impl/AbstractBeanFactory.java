package com.udeam.edu.factory.impl;

import com.udeam.edu.factory.BeanFactory;

public abstract class AbstractBeanFactory implements BeanFactory {
    /**
     * 容器执行一次 标识
     */
    public static boolean isTag = false;

    public static final String CLASS_STR = ".class";

}
