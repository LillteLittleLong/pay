package com.shangfu.pay.reconciliation.reconciliation.service.impl;

import com.shangfu.pay.reconciliation.reconciliation.dao.ReconCheckInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.entity.ReconCheckInfo;
import com.shangfu.pay.reconciliation.reconciliation.service.ReconCheckService;
import com.shangfu.pay.reconciliation.reconciliation.service.ReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Created by tinlly to 2019/4/11
 * Package for com.shangfu.pay.reconciliation.reconciliation.service.impl
 */
@Service
public class ReconCheckServiceImpl implements ReconCheckService {

    @Autowired
    ReconciliationService reconciliationService;
    @Autowired
    ReconCheckInfoRepository reconCheckInfoRepository;

    @Scheduled(cron = "0 0/2 * * * ?")
    @Override
    public void checkReconCollPay() {
        String tradeType = "CP_PAY";
        // 获取所有的对账任务
        List<ReconCheckInfo> reconCheckInfos = reconCheckInfoRepository.findByTrade_type(tradeType);

        // 定时任务定时执行
        for (ReconCheckInfo reconCheckInfo : reconCheckInfos) {
            // 对账任务没有执行
            if (null == reconCheckInfo.getUp_check_status()) {
                System.out.println("当前执行 > " + reconCheckInfo);

                // 对比 5 次 , 每次间隔 10 秒钟
                int index = 5;
                while (index-- > 0) {
                    // 上游为主对账
                    if (checkUpRecon(tradeType)) {
                        reconCheckInfoRepository.changeUpCheckStatusByReconCheckId("ok", reconCheckInfo.getRecon_check_id());
                        // 系统为主对账 > 为对跳出循环 , 判断下一个通道
                        if (checkSysRecon(tradeType)) {
                            reconCheckInfoRepository.changeSysCheckStatusByReconCheckId("ok", reconCheckInfo.getRecon_check_id());
                            break;
                        }
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // TODO: 2019/4/11 异常
                        System.out.println("线程休眠错误");
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    @Override
    public void checkReconEasyPay() {
        String tradeType = "EPAY";
        // 获取所有的对账任务
        List<ReconCheckInfo> reconCheckInfos = reconCheckInfoRepository.findByTrade_type(tradeType);

        // 定时任务定时执行
        for (ReconCheckInfo reconCheckInfo : reconCheckInfos) {
            // 对账任务没有执行
            if (null == reconCheckInfo.getUp_check_status()) {
                System.out.println("当前执行 > " + reconCheckInfo);

                // 对比 5 次 , 每次间隔 10 秒钟
                int index = 5;
                while (index-- > 0) {
                    // 上游为主对账
                    if (checkUpRecon(tradeType)) {
                        reconCheckInfoRepository.changeUpCheckStatusByReconCheckId("ok", reconCheckInfo.getRecon_check_id());
                        // 系统为主对账 > 为对跳出循环 , 判断下一个通道
                        if (checkSysRecon(tradeType)) {
                            reconCheckInfoRepository.changeSysCheckStatusByReconCheckId("ok", reconCheckInfo.getRecon_check_id());
                            break;
                        }
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // TODO: 2019/4/11 异常
                        System.out.println("线程休眠错误");
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    @Override
    public void checkReconGateWay() {
        String tradeType = "GATEWAY_PAY";
        // 获取所有的对账任务
        List<ReconCheckInfo> reconCheckInfos = reconCheckInfoRepository.findByTrade_type(tradeType);

        // 定时任务定时执行
        for (ReconCheckInfo reconCheckInfo : reconCheckInfos) {
            // 对账任务没有执行
            if (null == reconCheckInfo.getUp_check_status()) {
                System.out.println("当前执行 > " + reconCheckInfo);

                // 对比 5 次 , 每次间隔 10 秒钟
                int index = 5;
                while (index-- > 0) {
                    // 上游为主对账
                    if (checkUpRecon(tradeType)) {
                        reconCheckInfoRepository.changeUpCheckStatusByReconCheckId("ok", reconCheckInfo.getRecon_check_id());
                        // 系统为主对账 > 为对跳出循环 , 判断下一个通道
                        if (checkSysRecon(tradeType)) {
                            reconCheckInfoRepository.changeSysCheckStatusByReconCheckId("ok", reconCheckInfo.getRecon_check_id());
                            break;
                        }
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // TODO: 2019/4/11 异常
                        System.out.println("线程休眠错误");
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    @Override
    public void checkReconDistillPay() {
        String tradeType = "DISTILL_PAY";
        // 获取所有的对账任务
        List<ReconCheckInfo> reconCheckInfos = reconCheckInfoRepository.findByTrade_type(tradeType);

        // 定时任务定时执行
        for (ReconCheckInfo reconCheckInfo : reconCheckInfos) {
            // 对账任务没有执行
            if (null == reconCheckInfo.getUp_check_status()) {
                System.out.println("当前执行 > " + reconCheckInfo);

                // 对比 5 次 , 每次间隔 10 秒钟
                int index = 5;
                while (index-- > 0) {
                    // 上游为主对账
                    if (checkUpRecon(tradeType)) {
                        reconCheckInfoRepository.changeUpCheckStatusByReconCheckId("ok", reconCheckInfo.getRecon_check_id());
                        // 系统为主对账 > 为对跳出循环 , 判断下一个通道
                        if (checkSysRecon(tradeType)) {
                            reconCheckInfoRepository.changeSysCheckStatusByReconCheckId("ok", reconCheckInfo.getRecon_check_id());
                            break;
                        }
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // TODO: 2019/4/11 异常
                        System.out.println("线程休眠错误");
                    }
                }
            }
        }
    }

    /**
     * 每天
     */
    @Override
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    //@Scheduled(cron = "0 0 0 1/1 * ?") // 每一天执行一次
    public void clearReconCheck(){
        reconCheckInfoRepository.updateReconCheck();
    }

    public boolean checkUpRecon(String tradeType) {
        return reconciliationService.upReconciliationSys(tradeType);
    }

    public boolean checkSysRecon(String tradeType) {
        return reconciliationService.sysReconciliationUp(tradeType);
    }

}