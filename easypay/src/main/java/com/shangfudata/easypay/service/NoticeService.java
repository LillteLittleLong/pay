package com.shangfudata.easypay.service;

import com.shangfudata.easypay.entity.EasypayInfo;

import java.util.Map;


public interface NoticeService {

    void Upnotice(Map map);

    void ToDown(EasypayInfo easypayInfo);
}
