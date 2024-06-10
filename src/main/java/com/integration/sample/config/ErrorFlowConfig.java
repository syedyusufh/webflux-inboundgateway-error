package com.integration.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;

@Configuration
public class ErrorFlowConfig {

	@Bean
	public MessageChannel appErrorChannel() {
		return new PublishSubscribeChannel();
	}

	@Bean
	public IntegrationFlow appErrorChannelFlow() {

		// @formatter:off
		return IntegrationFlow.from(appErrorChannel())
							.<MessagingException> log(LoggingHandler.Level.ERROR, m -> "Web Exception Cause: " + m.getPayload().getCause().getMessage())
							.log(LoggingHandler.Level.INFO, m -> "This IntegrationFlow's job is ONLY to log the exceptions, which is done above")
							.get();
		// @formatter:on
	}

}
