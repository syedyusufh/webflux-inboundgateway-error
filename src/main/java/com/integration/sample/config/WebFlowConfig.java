package com.integration.sample.config;

import static org.springframework.http.HttpMethod.POST;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.MessageChannel;

import com.integration.sample.handler.AppValidator;
import com.integration.sample.model.dto.SomeObject;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class WebFlowConfig {

	@Value("${integration.test.some-url}")
	private String someUrl;

	// @formatter:off
	@Bean
	public IntegrationFlow webFlow(MessageChannel appErrorChannel,
									AppValidator appValidator) {
		
		return IntegrationFlows.from(WebFlux.inboundGateway("/testuri")
											.requestMapping(reqSpec -> reqSpec.methods(POST))
											.requestPayloadType(SomeObject.class)
											.errorChannel(appErrorChannel))
							.enrichHeaders(hdrSpec -> hdrSpec.errorChannel(appErrorChannel, true))
							.handle(WebFlux.outboundGateway(someUrl)
											.httpMethod(HttpMethod.POST)
											.mappedRequestHeaders(HttpHeaders.CONTENT_TYPE)
											.bodyExtractor((clientHttpResponse, context) -> validateAPIResponse(clientHttpResponse)))
							// validate payload
							.log(LoggingHandler.Level.INFO, m -> "Expect this line to be printed, rather than going to appErrorChannel for: " + m.getPayload())
							.transform(p -> p)
							.get();
		// @formatter:on
	}

	private Mono<String> validateAPIResponse(ClientHttpResponse clientHttpResponse) {

		var httpStatusCode = clientHttpResponse.getStatusCode();

		if (httpStatusCode.is5xxServerError())
			return Mono.just("5xx Error");

		var someObjectMono = new Jackson2JsonDecoder().decodeToMono(clientHttpResponse.getBody(), ResolvableType.forClass(SomeObject.class), null,
				null);

		if (!httpStatusCode.is2xxSuccessful()) {
			return someObjectMono.map(someObject -> {
				return ((SomeObject) someObject).getFirstName() + " looks like a 4xx error";
			});
		}

		return someObjectMono.map(someObject -> {
			return ((SomeObject) someObject).getFirstName() + " All Good";
		});

	}

}
