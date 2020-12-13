package com.udeam.edu.compoment;

import com.udeam.edu.utils.DruidUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接工具类 全局唯一
 * @author Pilgrim
 */
public class ConnetionManager {

    //单例 保持唯一
    private static ConnetionManager connetionManager = new ConnetionManager();


    public static ConnetionManager getconnetionUtil(){
        return connetionManager;
    }



    private  ThreadLocal<Connection> threadLocal = new ThreadLocal<>();


    public  Connection getConnection() throws SQLException {
        Connection connection = threadLocal.get();
        if (threadLocal.get()==null){
             connection = DruidUtils.getInstance().getConnection();
            //绑定当前线程 数据库连接
            threadLocal.set(connection);
        }
        //存在直接返回这个连接
        return connection;

    }


    //开启事务
   public void start() throws SQLException {
        //不自动提交
       getConnection().setAutoCommit(false);
    }

    //提交事务
    public void commit() throws SQLException {
        getConnection().commit();
    }


    //关闭事务  回滚事务
    public  void rowback() throws SQLException {
        getConnection().rollback();
    }



    public  void close() throws SQLException {
        getConnection().close();
    }


}
