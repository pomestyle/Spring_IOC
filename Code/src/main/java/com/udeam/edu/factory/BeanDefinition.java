package com.udeam.edu.factory;

/**
 * 定义bean示例对象
 * @author Pilgrim
 */
public class BeanDefinition {

    /**
     * bean对象
     */
    private Object bean;

    public BeanDefinition(Object bean) {
        this.bean = bean;
    }

    public Object getBean() {
        return bean;
    }

}
