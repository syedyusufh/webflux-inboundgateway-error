package com.integration.sample.handler;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.integration.sample.model.dto.SomeObject;

@Component
public class AppValidator {

	@Autowired
	private Validator validator;

	public SomeObject validate(final SomeObject requestPayload) {

		var validations = validator.validate(requestPayload);
		if (!ObjectUtils.isEmpty(validations))
			throw new RuntimeException("Validation Failed");

		// return same payload to proceed if validation is passed
		return requestPayload;
	}

}
