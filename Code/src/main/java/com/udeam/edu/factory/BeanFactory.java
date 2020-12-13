package com.udeam.edu.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 底层BeanFactory工厂接口
 * @author Pilgrim
 */
public interface BeanFactory {

    /**
     * 存储bean单例
     */
    public final static Map<String, Object> IOC_MAP = new HashMap<>();


    /**
     * 对外提供获取bean接口
     *
     * @param id
     * @return bean对象
     */
    public Object getBean(String id);


    /**
     * 根据类型对外提供获取bean示例
     *
     * @param classz
     * @return bean
     */
    public Object getBean(Class<?> classz);

    /**
     * 获取容器中所有的bean
     *
     * @return
     */
    public Object getAllBean();
}
