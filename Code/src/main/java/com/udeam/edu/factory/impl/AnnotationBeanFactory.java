package com.udeam.edu.factory.impl;

import com.alibaba.druid.util.StringUtils;
import com.udeam.edu.StartApplication;
import com.udeam.edu.annotation.Autowired;
import com.udeam.edu.annotation.Repository;
import com.udeam.edu.annotation.Service;
import com.udeam.edu.annotation.Transactional;
import com.udeam.edu.factory.ProxyFactory;
import com.udeam.edu.service.TransferService;
import com.udeam.edu.service.impl.TransferServiceImpl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * 注解方式 实现 Bean工厂
 *
 * @author Pilgrim
 */
public class AnnotationBeanFactory extends AbstractBeanFactory {


    /**
     * 2  注解 + 扫描包 方式实现 ioc 容器
     * tomcat启动的时候去初始化容器
     */
    public AnnotationBeanFactory() {

        if (isTag) {
            return;
        }
        try {
            String packageName = StartApplication.class.getPackage().getName();
            //扫描启动类的包名
            System.out.println("------------------- [容器]正在初始化 ------------ ");
            System.out.println(String.format("------------------- 扫描当前包是%s  ------------ ", packageName));
            initBeanFactory(packageName);
            System.out.println("------------------- [容器]初始化完成 ------------ ");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        isTag = true;

    }

    /**
     * 初始化bean
     * 1 递归扫描包获取类权限定命名
     * 2 实例化bean
     * 3 装配
     * 4 扫描事务注解 生成代理对象
     *
     * @param packName
     * @throws UnsupportedEncodingException
     */
    public void initBeanFactory(String packName) throws UnsupportedEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException {

        if (Objects.isNull(packName) || packName.length() == 0) {
            throw new RuntimeException("无效的包路径");
        }
        packName = packName.replace(".", File.separator);
        URL resource = AnnotationBeanFactory.class.getClassLoader().getResource(packName);
        String path = resource.getPath();
        //解析中文
        String filePath = URLDecoder.decode(path, "UTF-8");
        //存放权限定名
        Set<String> stringSet = new HashSet<>();
        //解析包成java权限定命名com
        parseFilePackName(packName, stringSet, filePath);
        //实例化bean
        setBean(stringSet);

        //System.out.println(String.format("获取到的bean : %s ", IOC_MAP));

        //自动装配
        beanAutoWired();

        //扫描事务注解
        doScanTransactional();

    }


    /**
     * 递归处理路径下文件夹是否包含文件夹,如不包含则获取当前类的权限定命名存入set中
     *
     * @param packName
     * @param classNameSet
     * @param path
     */
    public static void parseFilePackName(String packName, Set<String> classNameSet, String path) {

        File packNamePath = new File(path);

        if (!packNamePath.isDirectory() || !packNamePath.exists()) {
            return;
        }
        //递归路径下所有文件和文件夹
        for (File file : packNamePath.listFiles()) {
            boolean directory = file.isDirectory();
            String classNamePath = packName + File.separator + file.getName().replace(File.separator, ".");
            if (directory) {
                parseFilePackName(classNamePath, classNameSet, file.getPath());
            } else if (file.isFile() && file.getName().endsWith(CLASS_STR)) {
                //存入set
                classNameSet.add(classNamePath.replace(File.separator, ".").replace(CLASS_STR, ""));
            }
        }

    }


    /**
     * bean实例化
     *
     * @param stringSet
     */
    private void setBean(Set<String> stringSet) {
        stringSet.forEach(x -> {
            try {
                //排除指定包
                if (!x.contains("servlet")) {
                    Class<?> aClassz = Class.forName(x);
                    serviceAnnotation(aClassz);
                    repositoryAnnotation(aClassz);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 自动装配bean方法
     */
    public static void beanAutoWired() throws ClassNotFoundException {
        //获取成员变量上有 Autowired 注解的字段,然后根据当前类类型去自动装配
        for (Map.Entry<String, Object> stringObjectEntry : IOC_MAP.entrySet()) {

            Object beanDefinition = stringObjectEntry.getValue();
            Class<?> aClass = beanDefinition.getClass();
            Field[] declaredFields = aClass.getDeclaredFields();

            if (Objects.isNull(declaredFields) && declaredFields.length == 0) {
                return;
            }

            for (Field field : declaredFields) {
                //field.get
                //字段含有 Autowired 注解的需要被自动装配对象
                Autowired autowired = field.getAnnotation(Autowired.class);

                if (Objects.nonNull(autowired)) {
                    //根据当前key获取需要注入示例对象
                    //先根据名字注入,如果名字获取不到,再根据类型去注入
                    String beanName = autowired.name();

                    if (StringUtils.isEmpty(beanName)) {
                        beanName = field.getType().getSimpleName();
                    }

                    //反射设置值
                    try {
                        //field = aClass.getDeclaredField(field.getName());
                        field.setAccessible(true);
                        //自动装配 线程不安全,Spring中默认单例
                        field.set(stringObjectEntry.getValue(), IOC_MAP.get(beanName));
                        //更新容器属性
                        //IOC_MAP.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    /**
     * 判断并设置bean存在ioc map中,默认单例如存在则抛出异常
     *
     * @param value     bean name
     * @param className 类名
     * @param clasz     字节码
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void setIocNameMap(String value, String className, Class clasz) throws IllegalAccessException, InstantiationException {
        String iocNameString = value;
        Object beanDefinition =  clasz.newInstance() ;
        if (value.length() > 0) {
            if (IOC_MAP.containsKey(value)) {
                throw new RuntimeException("the named" + className + ",  had one ... ");
            }
        } else {
            //默认设置bean首字母小写的
            iocNameString = getIocNameString(className);
            if (IOC_MAP.containsKey(iocNameString)) {
                throw new RuntimeException("the named  " + className + ",  had one ... ");
            }
        }

        // 根据父接口类型注入
        Class<?>[] interfaces = clasz.getInterfaces();
        if (interfaces != null) {
            for (Class<?> anInterface : interfaces) {
                IOC_MAP.put(anInterface.getSimpleName(), beanDefinition);
            }
        }
        IOC_MAP.put(iocNameString, beanDefinition);
    }

    /**
     * 扫描事务注解处理方法
     */
    public void doScanTransactional() throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        for (Map.Entry<String, Object> classBeanDefinitionEntry : IOC_MAP.entrySet()) {
            Object beanDefinition = classBeanDefinitionEntry.getValue();
            //判断生成代理对象
            Object proxy = getProxy(beanDefinition);
            if (proxy==null){
                proxy = beanDefinition;
            }
            //存入同一个对象
            IOC_MAP.put(classBeanDefinitionEntry.getKey(), proxy);
        }


    }

    /**
     * 判断选择使用哪个代理对象
     *
     * @param aClass
     * @return
     */
    public Object getProxy(Object aClass) {
        Object jdkProxy = null;

        Transactional annotation =  aClass.getClass().getDeclaredAnnotation(Transactional.class);
            if (Objects.nonNull(annotation)) {
                //有接口使用jdk动态代理
                 if (aClass.getClass().getInterfaces() == null || aClass.getClass().getInterfaces().length <= 0) {

                    //cglib动态代理
                    jdkProxy = ProxyFactory.getCglibProxy(aClass);
                } else {
                    for (Class anInterface : aClass.getClass().getInterfaces()) {
                        System.out.println(anInterface.getSimpleName());
                    }
                    jdkProxy = ProxyFactory.getJdkProxy(aClass);
                }
        }


        return jdkProxy;

    }

    public static void main(String[] args) {
        TransferServiceImpl t =  new TransferServiceImpl();
        Class<TransferServiceImpl> transferServiceClass = (Class<TransferServiceImpl>) t.getClass();

        Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), transferServiceClass.getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {

                Object invoke = method.invoke(t, objects);
                return invoke;
            }
        });

        System.out.println(o);

        TransferService jdkProxy2 = (TransferService) o;
        System.out.println(o);

        System.out.println(ProxyFactory.getJdkProxy(t));

    }

    /**
     * 设置字符串首字母小写
     *
     * @param className
     * @return bean实例id名
     */
    public static String getIocNameString(String className) {
        return (String.valueOf(className.toCharArray()[0])).toLowerCase() + className.substring(1, className.length());
    }


    /**
     * 根据全限定类名反射生成对象
     *
     * @param pageName 全限定类名
     * @return object
     */
    public static Object getNewInstants(String pageName) {

        try {
            return Class.forName(pageName).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 获取bean的名字 并且判断当前类是否有 Service 注解,如有则存入Ioc
     * 如包含属性value不为空,则设置value属性为bean的key
     *
     * @param aClass1
     * @throws ClassNotFoundException
     */
    public void serviceAnnotation(Class aClass1) throws InstantiationException, IllegalAccessException {
        Service annotation = (Service) aClass1.getAnnotation(Service.class);
        if (Objects.nonNull(annotation)) {
            setIocNameMap(annotation.value(), aClass1.getSimpleName(), aClass1);
        }
    }

    /**
     * 获取bean的名字 并且判断当前类是否有 Repository 注解,如有则存入Ioc
     * 如包含属性value不为空,则设置value属性为bean的key
     *
     * @param aClass
     * @throws ClassNotFoundException
     */
    public void repositoryAnnotation(Class aClass) throws InstantiationException, IllegalAccessException {
        Repository annotation = (Repository) aClass.getAnnotation(Repository.class);
        if (Objects.nonNull(annotation)) {
            setIocNameMap(annotation.value(), aClass.getSimpleName(), aClass);
        }
    }


    @Override
    public Object getBean(String id) {
        if (Objects.nonNull(id) && id.length() > 0) {
            Object beanDefinition = IOC_MAP.get(id);
            return beanDefinition;
        }
        return null;
    }


    @Override
    public Object getBean(Class<?> aClass) {
        if (Objects.isNull(aClass)) {
            return null;
        }
        System.out.println(aClass.getSimpleName());
        //todo 根据类型获取这儿
        Object beanDefinition = IOC_MAP.get(aClass.getSimpleName());
        System.out.println(beanDefinition.getClass().getTypeName());
        return beanDefinition;
    }

    @Override
    public Object getAllBean() {
        return IOC_MAP.keySet();
    }

}
