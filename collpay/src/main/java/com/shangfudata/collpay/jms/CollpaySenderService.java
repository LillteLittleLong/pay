package com.shangfudata.collpay.jms;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class CollpaySenderService {

    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 将下游请求消息发送至队列中
     * @param destinationName 通道名
     * @param message 发送的请求消息
     */
    public void sendMessage(String destinationName , String message) {
        logger.info("发送队列的信息：：："+message);
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destinationName);
        try{
            jmsMessagingTemplate.convertAndSend(activeMQQueue , message);
        }catch (Exception e){
            logger.error("发送队列失败"+e);
        }

    }
}
