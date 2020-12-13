package com.udeam.edu.service;

/**
 * @author Pilgrim
 */
public interface TransferService {

    void transfer(String fromCardNo,String toCardNo,int money) throws Exception;
}
