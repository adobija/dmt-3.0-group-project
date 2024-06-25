package com.dmt.bankingapp.springTests.controllerTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import com.dmt.bankingapp.controller.TransactionController;
import com.dmt.bankingapp.entity.Account;
import com.dmt.bankingapp.entity.Client;
import com.dmt.bankingapp.entity.Transaction;
import com.dmt.bankingapp.repository.AccountRepository;
import com.dmt.bankingapp.repository.ClientRepository;
import com.dmt.bankingapp.repository.TransactionRepository;
import com.dmt.bankingapp.service.interfaceClass.DetailsOfLoggedClient;

public class TransactionControllerTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DetailsOfLoggedClient detailsOfLoggedClient;

    @InjectMocks
    private TransactionController transactionController;

    private MockHttpServletRequest request;
    private Client adminClient;
    private Client nonAdminClient;
    private Account checkingAccount;
    private Account receivingAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        request = new MockHttpServletRequest();
        adminClient = new Client("Admin", true, "password");
        adminClient.setClientID(1);
        nonAdminClient = new Client("User", false, "password");
        nonAdminClient.setClientID(2);
        checkingAccount = new Account("checking1", Account.AccountType.CHECKING, nonAdminClient);
        checkingAccount.setAccountID(1);  // Set the account ID
        checkingAccount.setAccountBalance(500.0, false);  // Set sufficient balance
        receivingAccount = new Account("receiving1", Account.AccountType.CHECKING, adminClient);
        receivingAccount.setAccountID(2);  // Set the account ID

        nonAdminClient.setCheckingAccount(checkingAccount);
        adminClient.setCheckingAccount(receivingAccount);
    }

    @Test
    void testAddNewTransactionSuccess() {
        // Arrange
        when(detailsOfLoggedClient.getNameFromClient(request)).thenReturn(nonAdminClient.getClientName());
        when(clientRepository.findByClientName(nonAdminClient.getClientName())).thenReturn(nonAdminClient);
        when(accountRepository.findById(checkingAccount.getAccountID())).thenReturn(Optional.of(checkingAccount));
        when(accountRepository.findById(receivingAccount.getAccountID())).thenReturn(Optional.of(receivingAccount));

        // Act
        String response = transactionController.addNewTransaction(checkingAccount.getAccountID(), receivingAccount.getAccountID(), 100.0, request);

        // Assert
        assertEquals("Transaction created successfully! Amount transfered: 100.0", response);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testAddNewTransactionGiverNotFound() {
        // Arrange
        when(detailsOfLoggedClient.getNameFromClient(request)).thenReturn(nonAdminClient.getClientName());
        when(clientRepository.findByClientName(nonAdminClient.getClientName())).thenReturn(nonAdminClient);
        when(accountRepository.findById(checkingAccount.getAccountID())).thenReturn(Optional.empty());

        // Act and Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            transactionController.addNewTransaction(checkingAccount.getAccountID(), receivingAccount.getAccountID(), 100.0, request);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Sender's account has not been found", exception.getReason());
    }

    @Test
    void testAddNewTransactionReceiverNotFound() {
        // Arrange
        when(detailsOfLoggedClient.getNameFromClient(request)).thenReturn(nonAdminClient.getClientName());
        when(clientRepository.findByClientName(nonAdminClient.getClientName())).thenReturn(nonAdminClient);
        when(accountRepository.findById(checkingAccount.getAccountID())).thenReturn(Optional.of(checkingAccount));
        when(accountRepository.findById(receivingAccount.getAccountID())).thenReturn(Optional.empty());

        // Act and Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            transactionController.addNewTransaction(checkingAccount.getAccountID(), receivingAccount.getAccountID(), 100.0, request);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Receiver's account has not been found", exception.getReason());
    }

    @Test
    void testGetByAccountIdNotAdmin() {
        // Arrange
        when(detailsOfLoggedClient.getNameFromClient(request)).thenReturn(nonAdminClient.getClientName());
        when(clientRepository.findByClientName(nonAdminClient.getClientName())).thenReturn(nonAdminClient);

        // Act and Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            transactionController.getByAccountId("checking1", request, mock(Model.class));
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You don't have permission!", exception.getReason());
    }

    @Test
    void testGetByAccountIdAccountNotFound() {
        // Arrange
        when(detailsOfLoggedClient.getNameFromClient(request)).thenReturn(adminClient.getClientName());
        when(clientRepository.findByClientName(adminClient.getClientName())).thenReturn(adminClient);
        when(accountRepository.findByAccountNumber("checking1")).thenReturn(null);

        // Act and Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            transactionController.getByAccountId("checking1", request, mock(Model.class));
        });

        assertEquals("Account not found with account number: checking1", exception.getMessage());
    }

    @Test
    void testGetClientOutgoingTransactions() {
        // Arrange
        when(detailsOfLoggedClient.getNameFromClient(request)).thenReturn(nonAdminClient.getClientName());
        when(clientRepository.findByClientName(nonAdminClient.getClientName())).thenReturn(nonAdminClient);
        when(transactionRepository.findByGiver(checkingAccount)).thenReturn(createSampleTransactions(checkingAccount, receivingAccount));

        Model model = mock(Model.class);

        // Act
        String viewName = transactionController.getClientOutgoingTransactions(request, model);

        // Assert
        assertEquals("transactionTemplates/outgoing", viewName);
        verify(model, times(1)).addAttribute(eq("outgoing"), anyString());
    }

    @Test
    void testGetClientIncomingTransactions() {
        // Arrange
        when(detailsOfLoggedClient.getNameFromClient(request)).thenReturn(nonAdminClient.getClientName());
        when(clientRepository.findByClientName(nonAdminClient.getClientName())).thenReturn(nonAdminClient);
        Model model = mock(Model.class);

        // Act
        String viewName = transactionController.getClientIncomingTransactions(request, model);

        // Assert
        assertEquals("transactionTemplates/incoming", viewName);
        verify(model, times(1)).addAttribute(eq("incoming"), anyString());
    }

    private List<Transaction> createSampleTransactions(Account giver, Account receiver) {
        Transaction transaction1 = new Transaction(giver, receiver, 100.0);
        transaction1.setTimestamp(LocalDateTime.now());
        transaction1.setTransactionID(1);

        Transaction transaction2 = new Transaction(giver, receiver, 200.0);
        transaction2.setTimestamp(LocalDateTime.now().minusDays(1));
        transaction2.setTransactionID(2);

        return Arrays.asList(transaction1, transaction2);
    }
}
