package com.integration.sample.model.dto;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomeObject {

	@NotBlank
	private String firstName;

	private String lastName;

}
