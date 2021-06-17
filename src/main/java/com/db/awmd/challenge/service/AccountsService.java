package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferDetail;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientAmountException;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * This method is used Transfer the amount in a synchronized manner
	 * 
	 * @param transferDetail
	 * @throws InsufficientAmountException
	 */
	public synchronized void transferAmount(TransferDetail transferDetail) throws InsufficientAmountException {
		Account transferFromAccount = this.accountsRepository.getAccount(transferDetail.getAccountFromId());
		Account transferToAccount = this.accountsRepository.getAccount(transferDetail.getAccountToId());

		// checking for the accounts
		if (transferFromAccount == null || transferToAccount == null) {
			log.error("Account is not available {} - {}", transferFromAccount, transferToAccount);
			throw new AccountNotFoundException("Account is not present for the transaction");
		}
		
		// checking for the valid positive number
		if (transferDetail.getTransferAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new InsufficientAmountException("Transfer amount is not proper");
		}
		
		// starting the transaction
		if (transferFromAccount.getBalance().compareTo(transferDetail.getTransferAmount()) >= 0) {
			log.debug("Transaction started to transfer amount {}", transferDetail);
			
			transferToAccount.setBalance(transferToAccount.getBalance().add(transferDetail.getTransferAmount()));
			transferFromAccount
					.setBalance(transferFromAccount.getBalance().subtract(transferDetail.getTransferAmount()));
			this.accountsRepository.updateAccount(transferToAccount);
			this.accountsRepository.updateAccount(transferFromAccount);
			
			//call the notify method
			sendNotification(transferFromAccount, transferToAccount, transferDetail.getTransferAmount());
		} else {
			log.error("Insufficient Amount {}", transferFromAccount);
			throw new InsufficientAmountException("Sufficient Balance is not present for the transaction");
		}
	}

	/**
	 * private method to notify the owners for the transaction
	 * 
	 * @param transferFromAccount
	 * @param transferToAccount
	 * @param transferAmount
	 */
	private void sendNotification(Account transferFromAccount, Account transferToAccount, BigDecimal transferAmount) {
		NotificationService notificationService = new EmailNotificationService();
		notificationService.notifyAboutTransfer(transferFromAccount,
				"Amount of " + transferAmount + " is been deducted and sent to " + transferToAccount.getAccountId());
		notificationService.notifyAboutTransfer(transferToAccount,
				"Amount of " + transferAmount + " is been credited from " + transferFromAccount.getAccountId());
	}
}
