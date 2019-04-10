package com.shangfudata.gatewaypay.service;

import java.util.Map;


public interface GatewaypayService {

    String downGatewaypay(String gatewaypayInfoToJson) throws Exception;

    String gatewaypayToUp(String gatewaypayInfoToJson);

}
