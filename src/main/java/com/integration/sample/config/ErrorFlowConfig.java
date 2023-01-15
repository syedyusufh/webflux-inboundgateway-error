package com.integration.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;

@Configuration
public class ErrorFlowConfig {

	@Bean
	public MessageChannel appErrorChannel() {

		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow appErrorChannelFlow() {

		// @formatter:off
		return IntegrationFlows.from(appErrorChannel())
							.<MessagingException> log(LoggingHandler.Level.ERROR, m -> "Web Exception: " + m.getPayload().getMessage())
							.transform(Message.class,
							           errorMessage -> 
							                       MessageBuilder.withPayload("Error Response")
							                                .copyHeaders(((MessagingException) errorMessage.getPayload()).getFailedMessage().getHeaders())
							                                .build())
							.get();
		// @formatter:on

	}

}
