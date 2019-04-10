package com.shangfudata.gatewaypay.service;

import com.shangfudata.gatewaypay.entity.GatewaypayInfo;

import java.util.Map;

public interface NoticeService {

    void Upnotice(Map<String,String> map);

    void ToDown(GatewaypayInfo gatewaypayInfo);
}
