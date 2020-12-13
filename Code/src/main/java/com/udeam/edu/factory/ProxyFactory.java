package com.udeam.edu.factory;

import com.udeam.edu.compoment.ConnetionManager;
import com.udeam.edu.compoment.TransferServiceManager;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;


/**
 * 代理类工厂
 *
 * @author Pilgrim
 */
public class ProxyFactory {

   final TransferServiceManager t = TransferServiceManager.get();

    /**
     * Jdk动态代理
     *
     * @param obj 被代理的对象
     * @return 返回代理对象
     */
    public static Object getJdkProxy(Object obj) {

        Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), obj.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {

                Object invoke = null;
                try {
                    // 开启事务(关闭事务的自动提交)
                    TransferServiceManager.get().start();
                    invoke = method.invoke(obj, objects);
                    // 提交事务
                    TransferServiceManager.get().commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    // 回滚事务
                    TransferServiceManager.get().rowback();
                    throw e;
                }

                return invoke;
            }
        });

        return o;

    }


    /**
     * cglib动态代理
     *
     * @param object 被代理的对象
     * @return 返回代理对象
     */
    public static Object getCglibProxy(Object object) {

        //生成代理对象
        return Enhancer.create(object.getClass(), new MethodInterceptor() {

            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                Object result = null;
                try {

                    //开启事务
                    TransferServiceManager.get().start();

                    result = method.invoke(object, objects);

                    //提交事务
                    TransferServiceManager.get().commit();
                } catch (Exception e) {
                    //回滚事务
                    TransferServiceManager.get().rowback();
                    throw e;
                }
                return result;

            }
        });

    }


    /**
     * 封装需要执行的事务方法
     *
     * @param object
     * @param method
     * @param objects
     * @return
     * @throws SQLException
     */
    private static Object doTransactionVoid(Object object, Method method, Object[] objects) throws SQLException {


        Object result = null;

        try {

            //开启事务
            TransferServiceManager.get().start();

            result = method.invoke(object, objects);

            //提交事务
            TransferServiceManager.get().commit();
        } catch (Exception e) {
            //回滚事务
            TransferServiceManager.get().rowback();
        }
        return result;

    }


}

