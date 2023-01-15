package com.integration.sample.config;

import static org.springframework.http.HttpMethod.POST;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.MessageChannel;

import com.integration.sample.handler.AppValidator;
import com.integration.sample.model.dto.SomeObject;

@Configuration
public class WebFlowConfig {

	// @formatter:off
	@Bean
	public IntegrationFlow webFlow(MessageChannel appErrorChannel,
									AppValidator appValidator) {
		
		return IntegrationFlows.from(WebFlux.inboundGateway("/testuri")
											.requestMapping(reqSpec -> reqSpec.methods(POST))
											.requestPayloadType(SomeObject.class)
											//.replyChannel(webReplyChannel())
											.errorChannel(appErrorChannel))
							.enrichHeaders(hdrSpec -> hdrSpec.errorChannel(appErrorChannel, true))
							// validate payload
							.handle(appValidator, "validate")
							.get();
		// @formatter:on
	}

	@Bean
	public MessageChannel webReplyChannel() {

		return new DirectChannel();
	}

}
