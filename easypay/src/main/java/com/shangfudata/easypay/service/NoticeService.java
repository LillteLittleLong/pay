package com.shangfudata.easypay.service;

import com.shangfudata.easypay.entity.EasypayInfo;

import java.util.Map;

public interface NoticeService {

    String Upnotice(Map<String,String> map);

    void ToDown(EasypayInfo easypayInfo);
}
