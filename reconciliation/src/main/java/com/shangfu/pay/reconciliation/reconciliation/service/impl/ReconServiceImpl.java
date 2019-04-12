package com.shangfu.pay.reconciliation.reconciliation.service.impl;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.shangfu.pay.reconciliation.reconciliation.dao.SysReconInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.dao.UpReconInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.entity.SysReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.entity.UpReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.service.ReconciliationService;
import com.shangfu.pay.reconciliation.reconciliation.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service.impl
 */
@Service
public class ReconServiceImpl implements ReconciliationService {

    String methodUrl = "http://localhost:8502/shangfu/pay/reconciliation/download";
    String signKey = "00000000000000000000000000000000";

    @Autowired
    UpReconInfoRepository upReconInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;

    //@Scheduled(cron = "0/60 * * * * *")
    //public void taskTest(){
    //    upReconciliationSys("CP_PAY");
    //    sysReconciliationUp("CP_PAY");
    //}

    /**
     * 1. 获取上游某业务对账信息
     * 2. 获取系统所有对账信息
     * 3. 以上游对账信息为主开始对账
     * 另一方有对账信息 , 开始对账
     * 另一方没有对账信息 , 对账失败
     * 4. 验证通道是否相同 , 验证商户号是否相同 , 验证机构号是否相同
     * 验证交易金额是否相同 , 验证手续费是否相同 , 验证交易状态是否相同
     */
    @Override
    public boolean upReconciliationSys(String tradeType) {
        System.out.println("开始以上游对账表为主比较系统订单表");
        // 1. 获取上游某通道的对账信息
        List<UpReconciliationInfo> all = upReconInfoRepository.queryUpReconciliationByTradeType(tradeType);

        if (all.size() == 0) {
            System.out.println("获取的内容为空 , 对账失败");
            return false;
        }

        for (UpReconciliationInfo upReconciliationInfo : all) {
            System.out.println("上有对账信息 > " + upReconciliationInfo.getTrade_no());
            // 获取另一方对账信息
            SysReconciliationInfo sysReconciliationInfo = sysReconInfoRepository.findByChTradeNo(upReconciliationInfo.getTrade_no());
            // 判断另一方有没有对账信息
            if (null == sysReconciliationInfo) { // 没有设置对账信息失败
                Console.error("另一放没有对账信息 , 对账失败");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                upReconInfoRepository.removeByTradeTime(upReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(upReconciliationInfo);
                return false;
            }

            // 另一方信息开始比较
            /**
             * 4. 验证通道是否相同 , 验证商户号是否相同 , 验证机构号是否相同
             *    验证交易金额是否相同 , 验证手续费是否相同 , 验证交易状态是否相同
             */

            if (!(sysReconciliationInfo.getSp_trade_no().equals(upReconciliationInfo.getSp_trade_no()))) {
                Console.error("对账错误 : " + upReconciliationInfo.getTrade_no() + " 通道不相同");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                 upReconInfoRepository.removeByTradeTime(upReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(upReconciliationInfo);
                return false;
            }

            // 商户通道比较
            if (!(sysReconciliationInfo.getSp_trade_no().equals(upReconciliationInfo.getSp_trade_no()))) {
                Console.error("对账错误 : " + upReconciliationInfo.getTrade_no() + " 商户不一样");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                 upReconInfoRepository.removeByTradeTime(upReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(upReconciliationInfo);
                return false;
            }
            // 机构通道比较
            if (!(sysReconciliationInfo.getTrade_no().equals(upReconciliationInfo.getTrade_no()))) {
                Console.error("对账错误 : " + upReconciliationInfo.getTrade_no() + " 机构不一样");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                 upReconInfoRepository.removeByTradeTime(upReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(upReconciliationInfo);
                return false;
            }
            // 交易状态比较
            if (!(sysReconciliationInfo.getTrade_state().equals(upReconciliationInfo.getTrade_state()))) {
                Console.error("对账错误 : " + upReconciliationInfo.getTrade_no() + " 交易状态不一样");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                 upReconInfoRepository.removeByTradeTime(upReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(upReconciliationInfo);
                return false;
            }
            // 总金额比较
            if (!(sysReconciliationInfo.getTotal_fee().equals(upReconciliationInfo.getTotal_fee()))) {
                Console.error("对账错误 : " + upReconciliationInfo.getTrade_no() + " 交易金额不一样");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                 upReconInfoRepository.removeByTradeTime(upReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(upReconciliationInfo);
                return false;
            }
            // TODO: 2019/4/9 利润比较
            // 手续费比较
            String sysRecon = sysReconciliationInfo.getHand_fee();
            String upRecon = upReconciliationInfo.getHand_fee();

            // TODO: 2019/4/11 系统手续费和上游手续费对账处理
            int sysReconInteger = Integer.parseInt(sysRecon);
            int upReconInteger = Integer.parseInt(upRecon);

            // 上游请求数据内容
            // 系统利润 - 上游利润
            // 大于 0 成功 , 小于 0 失败
            if ((sysReconInteger - upReconInteger) < 0) {
                Console.error("对账错误 : " + sysReconciliationInfo.getTrade_no() + " 手续费对账失败");
                sysReconInfoRepository.sysDateReconStateByTradeNo("false", sysReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                upReconInfoRepository.removeByTradeTime(sysReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(sysReconciliationInfo);
                return false;
            }
            // 当对账成功时改变状态
            upReconInfoRepository.updateReconStateByTradeNo("true", upReconciliationInfo.getTrade_no());
        }
        return true;
    }

    /**
     * 1. 获取所有系统订单信息和上游对账信息
     * 2. 以系统订单为主对比上游对账信息
     */
    @Override
    public boolean sysReconciliationUp(String tradeType) {
        System.out.println("开始以系统对账表为主比较上游订单表");
        // 获取系统某业务的对账信息
        List<SysReconciliationInfo> sysReconciliationInfos = sysReconInfoRepository.queryUpReconciliationByTradeType(tradeType);

        if (sysReconciliationInfos.size() == 0) {
            System.out.println("获取的内容为空 , 对账失败");
            return false;
        }

        for (SysReconciliationInfo sysReconciliationInfo : sysReconciliationInfos) {
            System.out.println("系统对账信息 > " + sysReconciliationInfo.getTrade_no());
            // 获取另一方对账信息
            UpReconciliationInfo upReconciliationInfo = upReconInfoRepository.findByChTradeNo(sysReconciliationInfo.getTrade_no());
            // 判断另一方有没有对账信息
            if (null == upReconciliationInfo) { // 没有设置对账信息失败
                Console.error("另一方没有对账内容 , 对账失败");
                sysReconInfoRepository.sysDateReconStateByTradeNo("false", sysReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                 upReconInfoRepository.removeByTradeTime(sysReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(sysReconciliationInfo);
                return false;
            }

            // 另一方游信息开始比较
            /**
             * 4. 验证通道是否相同 , 验证商户号是否相同 , 验证机构号是否相同
             *    验证交易金额是否相同 , 验证手续费是否相同 , 验证交易状态是否相同
             */

            if (!(sysReconciliationInfo.getSp_trade_no().equals(upReconciliationInfo.getSp_trade_no()))) {
                Console.error("对账错误 : " + sysReconciliationInfo.getTrade_no() + " 通道不相同");
                sysReconInfoRepository.sysDateReconStateByTradeNo("false", sysReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                upReconInfoRepository.removeByTradeTime(sysReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(sysReconciliationInfo);
                return false;
            }

            // 商户通道比较
            if (!(sysReconciliationInfo.getSp_trade_no().equals(upReconciliationInfo.getSp_trade_no()))) {
                Console.error("对账错误 : " + sysReconciliationInfo.getTrade_no() + " 商户不一样");
                sysReconInfoRepository.sysDateReconStateByTradeNo("false", sysReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                upReconInfoRepository.removeByTradeTime(sysReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(sysReconciliationInfo);
                return false;
            }
            // 机构通道比较
            if (!(sysReconciliationInfo.getTrade_no().equals(upReconciliationInfo.getTrade_no()))) {
                Console.error("对账错误 : " + sysReconciliationInfo.getTrade_no() + " 机构不一样");
                sysReconInfoRepository.sysDateReconStateByTradeNo("false", sysReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                upReconInfoRepository.removeByTradeTime(sysReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(sysReconciliationInfo);
                return false;
            }
            // 交易状态比较
            if (!(sysReconciliationInfo.getTrade_state().equals(upReconciliationInfo.getTrade_state()))) {
                Console.error("对账错误 : " + sysReconciliationInfo.getTrade_no() + " 交易状态不一样");
                sysReconInfoRepository.sysDateReconStateByTradeNo("false", sysReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                upReconInfoRepository.removeByTradeTime(sysReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(sysReconciliationInfo);
                return false;
            }
            // 总金额比较
            if (!(sysReconciliationInfo.getTotal_fee().equals(upReconciliationInfo.getTotal_fee()))) {
                Console.error("对账错误 : " + sysReconciliationInfo.getTrade_no() + " 交易金额不一样");
                sysReconInfoRepository.sysDateReconStateByTradeNo("false", sysReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                upReconInfoRepository.removeByTradeTime(sysReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(sysReconciliationInfo);
                return false;
            }
            // TODO: 2019/4/9 利润比较
            // 手续费比较
            String sysRecon = sysReconciliationInfo.getHand_fee();
            String upRecon = upReconciliationInfo.getHand_fee();

            // TODO: 2019/4/11 系统手续费和上游手续费对账处理
            int sysReconInteger = Integer.parseInt(sysRecon);
            int upReconInteger = Integer.parseInt(upRecon);

            // 上游请求数据内容
            // 系统利润 - 上游利润
            // 大于 0 成功 , 小于 0 失败
            if ((sysReconInteger - upReconInteger) < 0) {
                Console.error("对账错误 : " + sysReconciliationInfo.getTrade_no() + " 手续费对账失败");
                sysReconInfoRepository.sysDateReconStateByTradeNo("false", sysReconciliationInfo.getTrade_no());
                // 对账错误 , 删除数据表
                upReconInfoRepository.removeByTradeTime(sysReconciliationInfo.getTrade_time().substring(0,8));
                // 对账错误处理
                checkErrProcess(sysReconciliationInfo);
                return false;
            }
            // 当对账成功时改变状态
            sysReconInfoRepository.sysDateReconStateByTradeNo("true", sysReconciliationInfo.getTrade_no());
        }
        return true;
    }

    public void checkErrProcess(SysReconciliationInfo sysReconciliationInfo) {
        System.out.println("对账失败 , 重新从上游获取内容 , 保存至本地");
        Gson gson = new Gson();
        // 重新获取接口内容
        Map map = new HashMap();
        map.put("sp_id", sysReconciliationInfo.getSp_id());
        map.put("bill_date", sysReconciliationInfo.getTrade_time().substring(0, 8));
        map.put("nonce_str", "123456789");
        map.put("sign", SignUtils.sign(map, signKey));

        String requestMap = gson.toJson(map);
        HttpRequest.post(methodUrl).body(requestMap).execute();
    }

    public void checkErrProcess(UpReconciliationInfo upReconciliationInfo) {
        System.out.println("对账失败 , 重新从上游获取内容 , 保存至本地");
        Gson gson = new Gson();
        // 重新获取接口内容
        Map map = new HashMap();
        map.put("sp_id", upReconciliationInfo.getSp_id());
        map.put("bill_date", upReconciliationInfo.getTrade_time().substring(0, 8));
        map.put("nonce_str", "123456789");
        map.put("sign", SignUtils.sign(map, signKey));

        String requestMap = gson.toJson(map);
        HttpRequest.post(methodUrl).body(requestMap).execute();
    }

}