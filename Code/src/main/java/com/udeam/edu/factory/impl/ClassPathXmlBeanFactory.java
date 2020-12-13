package com.udeam.edu.factory.impl;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * Bean 工厂 xml实现方式
 *
 * @author Pilgrim
 */
public class ClassPathXmlBeanFactory extends AbstractBeanFactory {

    /**
     * xml方式IOC入口
     */
    public ClassPathXmlBeanFactory() {
        if (isTag) {
            return;
        }
        initClassPathXmlBeanFactory();
        isTag = true;
    }


    /**
     * 1 解析xml方式实现ioc容器
     */
    static void initClassPathXmlBeanFactory() {

        // 1 读取解析beans.xml  通过反射技术,生产bean对象,并将其存在map中
        InputStream resourceAsStream = ClassPathXmlBeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");


        //得到一个 文档对象
        try {
            Document read = new SAXReader().read(resourceAsStream);
            //获取跟对象
            Element rootElement = read.getRootElement();

            /**
             * xpath表达式 用法
             *   // 从匹配选择的当前节点选择文档中的节点,而不考虑他们的位置
             *   / 从根节点获取
             *  . 选取当前节点
             *  .. 选取当前节点的父节点
             *  @ 选取属性
             *
             */
            // //表示读取任意位置的bean标签
            List<Element> list = rootElement.selectNodes("//bean");

            if (Objects.isNull(list) || list.size() == 0) {
                throw new RuntimeException("无此bean标签");
            }

            list.forEach(x -> {
                //获取Id
                String id = x.attributeValue("id"); //accountDao
                //获取权限定命名
                String classz = x.attributeValue("class"); //com.udeam.edu.dao.impl.JdbcAccountDaoImpl

                //通过反射创建对象
                try {
                    Object o = Class.forName(classz).newInstance();
                    //存入ioc容器
                    IOC_MAP.put(id, o);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            });

            //获取所有properties 属性 并且set设置值
            List<Element> prList = rootElement.selectNodes("//property");

            prList.forEach(y -> {
                //获取 property 属性name值
                String name = y.attributeValue("name"); //   <property name="setAccountDao" ref = "accountDao"></property>
                String ref = y.attributeValue("ref");
                //获取父节点id
                Element parent = y.getParent();
                //获取父节点id
                String id = parent.attributeValue("id");
                //维护对象依赖关系
                Object o = IOC_MAP.get(id);
                //找到所有方法
                Method[] methods = o.getClass().getMethods();
                for (int i = 0; i < methods.length; i++) {
                    //方法就是set属性反方
                    if (methods[i].getName().equalsIgnoreCase("set" + name)) {
                        try {
                            //set设置对象
                            methods[i].invoke(o, IOC_MAP.get(ref));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        //set之后重新赋值
                        IOC_MAP.put(id, o);
                    }

                }


            });

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    /**
     * 对外提供获取bean接口
     * todo  // XML暂时没有提供根据 类型获取实现
     *
     * @param classz
     * @return bean对象
     */
    @Override
    public Object getBean(Class<?> classz) {
        return IOC_MAP.get(classz.getSimpleName());
    }

    /**
     * 对外提供获取bean接口
     *
     * @param id
     * @return bean对象
     */
    @Override
    public Object getBean(String id) {
        return IOC_MAP.get(id);
    }


    @Override
    public Object getAllBean() {
        return IOC_MAP;
    }

}
