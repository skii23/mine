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
    ////必须是prototype类型
    //public RabbitTemplate rabbitTemplate() {
    //    RabbitTemplate template = new RabbitTemplate(connectionFactory());
    //    return template;
    //}

    @Bean
    public Queue rabbitmqDemoDirectQueue() {
        /**
         * 1、name:    队列名称
         * 2、durable: 是否持久化
         * 3、exclusive: 是否独享、排外的。如果设置为true，定义为排他队列。则只有创建者可以使用此队列。也就是private私有的。
         * 4、autoDelete: 是否自动删除。也就是临时队列。当最后一个消费者断开连接后，会自动删除。
         * */
        return new Queue(queueName, true, false, false);
    }

    @Bean
    public DirectExchange rabbitmqDemoDirectExchange() {
        //Direct交换机
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Binding bindDirect() {
        //链式写法，绑定交换机和队列，并设置匹配键
        return BindingBuilder
                //绑定队列
                .bind(rabbitmqDemoDirectQueue())
                //到交换机
                .to(rabbitmqDemoDirectExchange())
                //并设置匹配键
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
                info.append("Rabbitmq 连接信息【host: ").append(host + ",").append("port: ").append(port + ",")
                        .append("username: ").append(username + ",").append("virtualHost: ").append(virtualHost).append("】");
                LogUtil.info(info);
                CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, Integer.valueOf(port));
                connectionFactory.setUsername(username);
                connectionFactory.setPassword(password);
                connectionFactory.setVirtualHost(virtualHost);
                connectionFactory.setPublisherConfirms(true);
                connectionFactory.createConnection().isOpen();
                LogUtil.info("Rabbitmq {},连接成功", host);
                return true;
            } catch (Exception e) {
                LogUtil.warn("Rabbitmq 连接失败", e);
            }
            return false;
        }
    }
}
