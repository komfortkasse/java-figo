//
// Copyright (c) 2013 figo GmbH
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//

package me.figo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import me.figo.internal.TokenResponse;
import me.figo.models.Account;
import me.figo.models.Notification;
import me.figo.models.Payment;
import me.figo.models.PaymentProposal;
import me.figo.models.PaymentType;
import me.figo.models.Security;
import me.figo.models.StandingOrder;
import me.figo.models.TanScheme;
import me.figo.models.Transaction;
import me.figo.models.User;

import org.junit.Before;
import org.junit.Test;

public class SessionTest {

    FigoSession sut = null;

    @Before
    public void setUp() throws Exception {
        sut = new FigoSession("ASHWLIkouP2O6_bgA2wWReRhletgWKHYjLqDaqb0LFfamim9RjexTo22ujRIP_cjLiRiSyQXyt2kM1eXU2XLFZQ0Hro15HikJQT_eNeT_9XQ");
    }

    @Test
    public void testGetAccount() throws FigoException, IOException {
        Account a = sut.getAccount("A1.2");
        assertEquals(a.getAccountId(), "A1.2");
    }

    @Test
    public void testGetAccountBalance() throws FigoException, IOException	{
    	Account a = sut.getAccount("A1.2");
        assertNotNull(a.getBalance().getBalance());
        assertNotNull(a.getBalance().getBalanceDate());
    }

    @Test
    public void testGetAccountTransactions() throws FigoException, IOException	{
    	Account a = sut.getAccount("A1.2");
        List<Transaction> ts = sut.getTransactions(a);
        assertTrue(ts.size() > 0);
    }

    @Test
    public void testGetAccountPayments() throws FigoException, IOException	{
    	Account a = sut.getAccount("A1.2");
        List<Payment> ps = sut.getPayments(a);
        assertTrue(ps.size() >= 0);
    }
    
    @Test
    public void testGetSupportedTanSchemes() throws FigoException, IOException	{
    	Account a = sut.getAccount("A1.1");
    	List<TanScheme> schemes = a.getSupportedTanSchemes();
    	assertTrue(schemes.size() == 3);
    }

    @Test
    public void testGetTransactions() throws FigoException, IOException {
        List<Transaction> transactions = sut.getTransactions();
        assertTrue(transactions.size() > 0);
    }

    @Test
    public void testGetNotifications() throws FigoException, IOException {
        List<Notification> notifications = sut.getNotifications();
        assertTrue(notifications.size() > 0);
    }

    @Test
    public void testGetPayments() throws FigoException, IOException {
        List<Payment> payments = sut.getPayments();
        assertTrue(payments.size() >= 0);
    }

    @Test
    public void testMissingHandling() throws IOException, FigoException {
        assertNull(sut.getAccount("A1.5"));
    }

    @Test(expected=FigoException.class)
    public void testExceptionHandling() throws IOException, FigoException {
        sut.getSyncURL("", "http://localhost:3003/");
    }

    @Test
    public void testSyncUri() throws FigoException, IOException {
        assertNotNull(sut.getSyncURL("qwe", "http://figo.me/test"));
    }

    @Test
    public void testUser() throws FigoException, IOException {
        User user = sut.getUser();
        assertEquals("demo@figo.me", user.getEmail());
    }

    @Test
    public void testCreateUpdateDeleteNotification() throws FigoException, IOException {
        Notification addedNotificaton = sut.addNotification(new Notification("/rest/transactions", "http://figo.me/test", "qwe"));
        assertNotNull(addedNotificaton.getNotificationId());
        assertEquals(addedNotificaton.getObserveKey(), "/rest/transactions");
        assertEquals(addedNotificaton.getNotifyURI(), "http://figo.me/test");
        assertEquals(addedNotificaton.getState(), "qwe");

        addedNotificaton.setState("asd");
        Notification updatedNotification = sut.updateNotification(addedNotificaton);
        assertEquals(updatedNotification.getNotificationId(), addedNotificaton.getNotificationId());
        assertEquals(updatedNotification.getObserveKey(), "/rest/transactions");
        assertEquals(updatedNotification.getNotifyURI(), "http://figo.me/test");
        assertEquals(updatedNotification.getState(), "asd");

        sut.removeNotification(updatedNotification);

        Notification reretrievedNotification = sut.getNotification(addedNotificaton.getNotificationId());
        assertNull(reretrievedNotification);
    }

    @Test
    public void testCreateUpdateDeletePayment() throws FigoException, IOException {
        Payment addedPayment = sut.addPayment(new Payment("Transfer", "A1.1", "4711951501", "90090042", "figo", "Thanks for all the fish.", new BigDecimal(0.89)));
        assertNotNull(addedPayment.getPaymentId());
        assertEquals("A1.1", addedPayment.getAccountId());
        assertEquals("Demobank", addedPayment.getBankName());
        assertEquals(0.89f, addedPayment.getAmount().floatValue(), 0.0001);

        addedPayment.setAmount(new BigDecimal(2.39));
        Payment updatedPayment = sut.updatePayment(addedPayment);
        assertEquals(addedPayment.getPaymentId(), updatedPayment.getPaymentId());
        assertEquals("A1.1", updatedPayment.getAccountId());
        assertEquals("Demobank", updatedPayment.getBankName());
        assertEquals(2.39f, updatedPayment.getAmount().floatValue(), 0.0001);

        sut.removePayment(updatedPayment);

        Payment reretrievedPayment = sut.getPayment(addedPayment.getAccountId(), addedPayment.getPaymentId());
        assertNull(reretrievedPayment);
    }

    public void testGetPaymentProposals() throws FigoException, IOException	{
    	List<PaymentProposal> proposals = sut.getPaymentProposals();
    	assertEquals(2, proposals.size());
    }
    
    @Test
    public void testGetSupportedPaymentTypes() throws FigoException, IOException	{
    	HashMap<String, PaymentType> types = sut.getAccounts().get(0).getSupportedPaymentTypes();
    	assertEquals(2, types.size());
    }
    
    @Test
	public void testGetStandingOrders() throws IOException, FigoException {
        List<StandingOrder> so = sut.getStandingOrders();
        assertTrue(so.size() > 0);
	}

}
