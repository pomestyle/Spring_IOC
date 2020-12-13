package com.udeam.edu.compoment;

import java.sql.SQLException;

/**
 * 事务管理器 全局唯一
 * @author Pilgrim
 */
public class TransferServiceManager {

    //单例
    private static TransferServiceManager t =  new TransferServiceManager();

    //对外提供对象接口
    public static TransferServiceManager get(){
        return t;
    }


    //开启事务
    public  void start() throws SQLException {
        //不自动提交
        ConnetionManager.getconnetionUtil().start();
    }

    //提交事务
    public void commit() throws SQLException {
        ConnetionManager.getconnetionUtil().commit();
    }


    //关闭事务  回滚事务
    public  void rowback() throws SQLException {
        ConnetionManager.getconnetionUtil().rowback();
    }


}
