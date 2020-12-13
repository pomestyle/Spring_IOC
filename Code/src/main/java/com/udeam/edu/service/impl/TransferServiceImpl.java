package com.udeam.edu.service.impl;

import com.udeam.edu.annotation.Autowired;
import com.udeam.edu.annotation.Service;
import com.udeam.edu.annotation.Transactional;
import com.udeam.edu.dao.AccountDao;
import com.udeam.edu.compoment.ConnetionManager;
import com.udeam.edu.compoment.TransferServiceManager;
import com.udeam.edu.pojo.Account;
import com.udeam.edu.service.TransferService;

/**
 * @author Pilgrim
 */
@Service(value = "transferServiceImpl")
@Transactional
public class TransferServiceImpl implements TransferService {

    @Autowired(name = "accountDao")
    private AccountDao accountDao;


    @Override

    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {

        try {

            //开启事务
            TransferServiceManager.get().start();


            Account from = accountDao.queryAccountByCardNo(fromCardNo);
            Account to = accountDao.queryAccountByCardNo(toCardNo);

            from.setMoney(from.getMoney()-money);
            to.setMoney(to.getMoney()+money);

            accountDao.updateAccountByCardNo(to);
            //模拟异常代码
            int c = 1/0;
            accountDao.updateAccountByCardNo(from);
            //提交事务
            TransferServiceManager.get().commit();
        }catch (Exception e){
            //回滚事务
            TransferServiceManager.get().rowback();
            System.out.println(e.getStackTrace());
            throw new RuntimeException("失败!");
        }finally {
            //关闭连接
            System.out.println(ConnetionManager.getconnetionUtil());
            ConnetionManager.getconnetionUtil().close();
        }

    }


}
