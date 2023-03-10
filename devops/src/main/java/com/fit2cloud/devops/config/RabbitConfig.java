package com.fit2cloud.devops.config;

import com.fit2cloud.commons.utils.LogUtil;
import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Properties;


/**
 * @author caiwzh
 * @date 2022/11/24
 */
@Configuration
@Data
@Conditional(RabbitConfig.RabbitMQCondition.class)
@EnableRabbit
public class RabbitConfig {

    //@Value("${spring.rabbitmq.host}")
    //private String host;
    //
    //@Value("${spring.rabbitmq.port}")
    //private int port;
    //
    //@Value("${spring.rabbitmq.username}")
    //private String username;
    //
    //@Value("${spring.rabbitmq.password}")
    //private String password;
    //
    //@Value("${spring.rabbitmq.virtualHost}")
    //private String virtualHost;

    @Value("${spring.rabbitmq.exchange:EXCHANGE_A}")
    private String exchange;

    @Value("${spring.rabbitmq.routingKey:ROUTINGKEY_A}")
    private String routingKey;

    @Value("${spring.rabbitmq.queue:QUEUE_A}")
    private String queueName;
    //
    //@Bean
    //public ConnectionFactory connectionFactory() {
    //    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host,port);
    //    connectionFactory.setUsername(username);
    //    connectionFactory.setPassword(password);
    //    connectionFactory.setVirtualHost(virtualHost);
    //    connectionFactory.setPublisherConfirms(true);
    //    connectionFactory.createConnection().isOpen();
    //    return connectionFactory;
    //}
    //
    //@Bean
    //@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    ////?????????prototype??????
    //public RabbitTemplate rabbitTemplate() {
    //    RabbitTemplate template = new RabbitTemplate(connectionFactory());
    //    return template;
    //}

    @Bean
    public Queue rabbitmqDemoDirectQueue() {
        /**
         * 1???name:    ????????????
         * 2???durable: ???????????????
         * 3???exclusive: ??????????????????????????????????????????true??????????????????????????????????????????????????????????????????????????????private????????????
         * 4???autoDelete: ?????????????????????????????????????????????????????????????????????????????????????????????????????????
         * */
        return new Queue(queueName, true, false, false);
    }

    @Bean
    public DirectExchange rabbitmqDemoDirectExchange() {
        //Direct?????????
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Binding bindDirect() {
        //????????????????????????????????????????????????????????????
        return BindingBuilder
                //????????????
                .bind(rabbitmqDemoDirectQueue())
                //????????????
                .to(rabbitmqDemoDirectExchange())
                //??????????????????
                .with(routingKey);
    }

    public static class RabbitMQCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            try {
                Resource resource = context.getResourceLoader().getResource("file:fit2cloud.properties");
                Properties properties = new Properties();
                properties.load(resource.getInputStream());
                String host = properties.getProperty("spring.rabbitmq.host");
                String port = properties.getProperty("spring.rabbitmq.port");
                String username = properties.getProperty("spring.rabbitmq.username");
                String password = properties.getProperty("spring.rabbitmq.password");
                String virtualHost = properties.getProperty("spring.rabbitmq.virtualHost");
                StringBuffer info = new StringBuffer();
                info.append("Rabbitmq ???????????????host: ").append(host + ",").append("port: ").append(port + ",")
                        .append("username: ").append(username + ",").append("virtualHost: ").append(virtualHost).append("???");
                LogUtil.info(info);
                CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, Integer.valueOf(port));
                connectionFactory.setUsername(username);
                connectionFactory.setPassword(password);
                connectionFactory.setVirtualHost(virtualHost);
                connectionFactory.setPublisherConfirms(true);
                connectionFactory.createConnection().isOpen();
                LogUtil.info("Rabbitmq {},????????????", host);
                return true;
            } catch (Exception e) {
                LogUtil.warn("Rabbitmq ????????????", e);
            }
            return false;
        }
    }
}
