package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferDetail;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientAmountException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

	}

	@Test
	public void testTransferAmountFailure() throws Exception {
		TransferDetail transferDetail = new TransferDetail("123", "1234", new BigDecimal(123));
		try {
			this.accountsService.transferAmount(transferDetail);
			fail("Account is not present for the transaction");
		} catch (AccountNotFoundException e) {
			assertThat(e.getMessage()).isEqualTo("Account is not present for the transaction");
		}

	}
	
	@Test
	public void testTransferAmountSuccess() throws Exception {
		createMockAccounts();
		TransferDetail transferDetail = new TransferDetail("Id-2522", "Id-2321", new BigDecimal(100));
		
		this.accountsService.transferAmount(transferDetail);
		
		assertEquals(this.accountsService.getAccount("Id-2522").getBalance(), new BigDecimal(900));
		assertEquals(this.accountsService.getAccount("Id-2321").getBalance(), new BigDecimal(1100));

	}
	
	private void createMockAccounts() {
		Account account = new Account("Id-2522");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);
		Account account2 = new Account("Id-2321");
		account2.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account2);
	}
	
	@Test
	public void testTransferAmountNegativeBalanceFailure() throws Exception {
		createMockAccounts2();
		TransferDetail transferDetail = new TransferDetail("Id-700", "Id-800", new BigDecimal(-100));
		try {
			this.accountsService.transferAmount(transferDetail);
			fail("Transfer amount is not proper");
		} catch (InsufficientAmountException e) {
			assertThat(e.getMessage()).isEqualTo("Transfer amount is not proper");
		}
	}
	
	private void createMockAccounts2() {
		Account account = new Account("Id-700");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);
		Account account2 = new Account("Id-800");
		account2.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account2);
	}
	
	@Test
	public void testTransferInsufficientAmountFailure() throws Exception {
		createMockAccounts3();
		TransferDetail transferDetail = new TransferDetail("Id-888", "Id-999", new BigDecimal(5000));
		try {
			this.accountsService.transferAmount(transferDetail);
			fail("Sufficient Balance is not present for the transaction");
		} catch (InsufficientAmountException e) {
			assertThat(e.getMessage()).isEqualTo("Sufficient Balance is not present for the transaction");
		}
	}
	
	private void createMockAccounts3() {
		Account account = new Account("Id-888");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);
		Account account2 = new Account("Id-999");
		account2.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account2);
	}
	
	@Test
	public void testTransferAmountConcurrentSuccess() throws Exception {
		createMockAccounts4();
		TransferDetail transferDetail = new TransferDetail("Id-456", "Id-4567", new BigDecimal(100));
		TransferDetail transferDetail2 = new TransferDetail("Id-4567", "Id-456", new BigDecimal(100));
		
		this.accountsService.transferAmount(transferDetail);
		Thread.sleep(50);
		this.accountsService.transferAmount(transferDetail2);
		
		assertEquals(this.accountsService.getAccount("Id-456").getBalance(), new BigDecimal(1000));
		assertEquals(this.accountsService.getAccount("Id-4567").getBalance(), new BigDecimal(1000));

	}
	
	private void createMockAccounts4() {
		Account account = new Account("Id-456");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);
		Account account2 = new Account("Id-4567");
		account2.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account2);
	}
}
