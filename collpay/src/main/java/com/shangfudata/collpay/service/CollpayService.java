package com.shangfudata.collpay.service;

public interface CollpayService {

    String downCollpay(String CollpayInfoToJson)  throws  Exception;

    void collpayToUp(String collpayInfoToJson)throws Exception;
}
