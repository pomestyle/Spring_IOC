package com.udeam.edu;


import com.udeam.edu.annotation.Transactional;
import com.udeam.edu.factory.impl.AnnotationBeanFactory;
import com.udeam.edu.factory.BeanFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 启动类
 * 相当于SpringBoot雏形,如果内嵌tomcat容器的话,从此包下启动扫描当前包所在的包,然后可以进行ioc,装配bean等
 * @author Pilgrim
 */
public class StartApplication {

    //测试 IOC
    public static void main(String[] args) throws ClassNotFoundException {

        Class<?> aClass = Class.forName("com.udeam.edu.service.impl.TransferServiceImpl");
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            Transactional annotation = declaredMethod.getAnnotation(Transactional.class);
            if (Objects.nonNull(annotation)){
                String name = declaredMethod.getName();
                //生成代理对象
                break;
            }
        }
        Annotation declaredAnnotation1 = aClass.getDeclaredAnnotation(Transactional.class);


        BeanFactory beanFactory = new AnnotationBeanFactory();
        Object bean = beanFactory.getBean("transferServiceImpl");
        System.out.println(bean);
    }

}
