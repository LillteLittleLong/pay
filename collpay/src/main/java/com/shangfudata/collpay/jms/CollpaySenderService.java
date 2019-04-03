package com.shangfudata.collpay.jms;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class CollpaySenderService {

    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    /**
     * 将下游请求消息发送至队列中
     * @param destinationName 通道名
     * @param message 发送的请求消息
     */
    public void sendMessage(String destinationName , String message) {
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(destinationName);
        jmsMessagingTemplate.convertAndSend(activeMQQueue , message);
    }
}
