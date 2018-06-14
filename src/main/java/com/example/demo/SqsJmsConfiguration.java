package com.example.demo;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.util.Optional;

import javax.jms.ConnectionFactory;

/**
 * Configures JMS {@link ConnectionFactory} for Amazon SQS.
 * Automatically deserialises JSON messages into objects using Jackson 2.x {@link ObjectMapper}.
 */
@Configuration
@EnableConfigurationProperties(SqsProperties.class)
class SqsJmsConfiguration implements JmsListenerConfigurer {

    private final Optional<ObjectMapper> objectMapper;

    SqsJmsConfiguration(Optional<ObjectMapper> objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    ProviderConfiguration providerConfiguration(SqsProperties sqsProperties) {
        return new ProviderConfiguration().withNumberOfMessagesToPrefetch(sqsProperties.getPrefetch());
    }

    @Bean
    SQSConnectionFactory sqsConnectionFactory(RegionProvider regionProvider,
                                              AWSCredentialsProvider awsCredentialsProvider,
                                              ProviderConfiguration providerConfiguration) {
        return new SQSConnectionFactory(providerConfiguration, AmazonSQSClientBuilder.standard()
                                                                                     .withRegion(Regions.fromName(regionProvider.getRegion().getName()))
                                                                                     .withCredentials(awsCredentialsProvider)
                                                                                     .build());
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            DefaultJmsListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        // SQS does not support transactions
        factory.setSessionTransacted(false);
        return factory;
    }

    // beans defined below enables automatic message deserialization in @JmsListeners using MessageConverter
    @Bean
    public DefaultMessageHandlerMethodFactory handlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        objectMapper.ifPresent(converter::setObjectMapper);
        return converter;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(handlerMethodFactory());
    }
}
