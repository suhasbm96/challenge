package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountUpdationException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.web.AccountsController;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	/**
	 * Updating the users account
	 */
	@Override
	public void updateAccount(Account account) throws AccountUpdationException {
		Account existingAccount = accounts.get(account.getAccountId());
		if (existingAccount != null) {
			accounts.put(account.getAccountId(), account);
		} else {
			log.error("Account updation failed {}", existingAccount);
			throw new AccountUpdationException("Updating Account Id" + account.getAccountId() + " does not exist");
		}

	}

}
