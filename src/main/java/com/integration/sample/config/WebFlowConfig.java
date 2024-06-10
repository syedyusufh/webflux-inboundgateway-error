package com.integration.sample.config;

import static org.springframework.http.HttpMethod.POST;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;

import com.integration.sample.handler.AppValidator;
import com.integration.sample.model.dto.SomeClass;

@Configuration
public class WebFlowConfig {

	@Bean
	// @formatter:off
	public IntegrationFlow webFlow(MessageChannel appErrorChannel,
									AppValidator appValidator) {
		
		return IntegrationFlow.from(WebFlux.inboundGateway("/testuri")
											.requestMapping(reqSpec -> reqSpec.methods(POST))
											.requestPayloadType(SomeClass.class)
											.replyTimeout(1000)
											.replyChannel(webReplyChannel())
											.errorChannel(appErrorChannel))
				
							.enrichHeaders(hdrSpec -> hdrSpec.errorChannel(appErrorChannel, true))
							
							// validate payload
							.handle(appValidator)
							
							// final response
							.transform(p -> "All Good")
							
							.get();
		// @formatter:on
	}

	@Bean
	public IntegrationFlow webErrorFlow(MessageChannel appErrorChannel) {

		// @formatter:off
		return IntegrationFlow.from(appErrorChannel)
							.transform(Message.class, m -> MessageBuilder.withPayload("API Failed")
																		.copyHeaders(((MessagingException) m.getPayload()).getFailedMessage().getHeaders())
																		.build())
							.<MessagingException> log(LoggingHandler.Level.INFO, m -> "Error Response has been constructed here")
							.channel(webReplyChannel())
							.get();
		// @formatter:on
	}

	@Bean
	public MessageChannel webReplyChannel() {
		return new DirectChannel();
	}

}
