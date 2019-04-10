package com.shangfudata.easypay.service;

public interface EasypayService {

    String downEasypay(String easypayInfoToJson) throws Exception;

    String easypayToUp(String easypayInfoToJson);
}
