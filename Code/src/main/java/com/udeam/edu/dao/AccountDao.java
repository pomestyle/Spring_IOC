package com.udeam.edu.dao;

import com.udeam.edu.pojo.Account;

/**
 * @author Pilgrim
 */
public interface AccountDao {

    Account queryAccountByCardNo(String cardNo) throws Exception;

    int updateAccountByCardNo(Account account) throws Exception;
}
