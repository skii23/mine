package com.fit2cloud.devops.service;

import com.fit2cloud.devops.config.RabbitConfig;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author caiwzh
 * @date 2022/11/24
 */
@Service
@Data
public class MessageProducer {

    public final RabbitTemplate rabbitTemplate;

    private final ApplicationContext applicationContext;

    @Autowired(required = false)
    private RabbitConfig rabbitConfig;

    public void sendMsg(String content) {
        if (rabbitConfig != null) {
            rabbitTemplate.convertAndSend(rabbitConfig.getExchange(), rabbitConfig.getRoutingKey(), content);
        }
    }
}
