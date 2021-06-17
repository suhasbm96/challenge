package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TransferDetail {

	@NotNull
	@NotEmpty
	private final String accountFromId;

	@NotNull
	@NotEmpty
	private final String accountToId;

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private final BigDecimal transferAmount;

	@JsonCreator
	public TransferDetail(@JsonProperty("accountFromId") String accountFromId,
			@JsonProperty("accountToId") String accountToId,
			@JsonProperty("transferAmount") BigDecimal transferAmount) {
		super();
		this.accountFromId = accountFromId;
		this.accountToId = accountToId;
		this.transferAmount = transferAmount;
	}

}
