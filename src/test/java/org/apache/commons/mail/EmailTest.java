package org.apache.commons.mail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Properties;

import javax.mail.Session;

import static org.junit.Assert.*;

public class EmailTest {

    private static final String[] TEST_EMAILS = { "ab@bc.com", "a.b@c.org", "abcdefghijklmnopqrst@abcdefghijklmnopqrst.com.bd" };

    private EmailStub email;

    @Before
    public void setUp() {
        email = new EmailStub();
    }

    @After
    public void tearDown() {
        email = null;
    }

    // 1. addBcc(String... emails)
    @Test
    public void testAddBcc() throws Exception {
        email.addBcc(TEST_EMAILS);
        assertEquals(3, email.getBccAddresses().size());
    }

    @Test(expected = EmailException.class)
    public void testAddBccNullArray() throws Exception {
        email.addBcc((String[]) null);
    }

    @Test(expected = EmailException.class)
    public void testAddBccEmptyArray() throws Exception {
        email.addBcc(new String[0]);
    }

    // 2. addCc(String email)
    @Test
    public void testAddCc() throws Exception {
        email.addCc(TEST_EMAILS[0]);
        assertEquals(1, email.getCcAddresses().size());
        assertEquals(TEST_EMAILS[0], email.getCcAddresses().get(0).getAddress());
    }

    @Test
    public void testAddCcArray() throws Exception {
        email.addCc(TEST_EMAILS);
        assertEquals(3, email.getCcAddresses().size());
    }

    @Test(expected = EmailException.class)
    public void testAddCcNullArray() throws Exception {
        email.addCc((String[]) null);
    }

    @Test(expected = EmailException.class)
    public void testAddCcEmptyArray() throws Exception {
        email.addCc(new String[0]);
    }

    // 3. addHeader(String name, String value)
    @Test
    public void testAddHeader() {
        email.addHeader("X-Test-Header", "Value123");
        assertEquals(1, email.headers.size());
        assertEquals("Value123", email.headers.get("X-Test-Header"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderNullName() {
        email.addHeader(null, "Value123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderEmptyName() {
        email.addHeader("", "Value123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderNullValue() {
        email.addHeader("X-Test-Header", null);
    }

    // 4. addReplyTo(String email, String name)
    @Test
    public void testAddReplyTo() throws Exception {
        email.addReplyTo(TEST_EMAILS[0], "Test Name");
        assertEquals(1, email.getReplyToAddresses().size());
        assertEquals(TEST_EMAILS[0], email.getReplyToAddresses().get(0).getAddress());
        assertEquals("Test Name", email.getReplyToAddresses().get(0).getPersonal());
    }

    // 5. buildMimeMessage()
    @Test
    public void testBuildMimeMessage() throws Exception {
        email.setHostName("localhost");
        email.setFrom(TEST_EMAILS[0]);
        email.addTo(TEST_EMAILS[1]);
        email.setSubject("Test Subject");
        email.addHeader("X-Test-Header", "Value123");
        email.setCharset("UTF-8");
        email.setContent("Test Content", "text/plain");

        email.buildMimeMessage();

        assertNotNull(email.getMimeMessage());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildMimeMessageTwice() throws Exception {
        email.setHostName("localhost");
        email.setFrom(TEST_EMAILS[0]);
        email.addTo(TEST_EMAILS[1]);
        email.buildMimeMessage();
        
        email.buildMimeMessage();
    }

    @Test(expected = EmailException.class)
    public void testBuildMimeMessageNoFrom() throws Exception {
        email.setHostName("localhost");
        email.addTo(TEST_EMAILS[1]);
        email.buildMimeMessage();
    }

    @Test(expected = EmailException.class)
    public void testBuildMimeMessageNoReceivers() throws Exception {
        email.setHostName("localhost");
        email.setFrom(TEST_EMAILS[0]);
        email.buildMimeMessage();
    }

    @Test
    public void testBuildMimeMessageWithCcAndBccAndReplyTo() throws Exception {
        email.setHostName("localhost");
        email.setFrom(TEST_EMAILS[0]);
        email.addTo(TEST_EMAILS[1]);
        email.addCc(TEST_EMAILS[1]);
        email.addBcc(TEST_EMAILS[2]);
        email.addReplyTo(TEST_EMAILS[0]);
        
        email.buildMimeMessage();
        assertNotNull(email.getMimeMessage());
    }

    @Test
    public void testBuildMimeMessageWithEmailBody() throws Exception {
        email.setHostName("localhost");
        email.setFrom(TEST_EMAILS[0]);
        email.addTo(TEST_EMAILS[1]);
        
        javax.mail.internet.MimeMultipart multipart = new javax.mail.internet.MimeMultipart();
        email.setContent(multipart);
        
        email.buildMimeMessage();
        assertNotNull(email.getMimeMessage());
    }

    // 6. getHostName()
    @Test
    public void testGetHostNameWithoutSession() {
        email.setHostName("my.host.name");
        assertEquals("my.host.name", email.getHostName());
    }

    @Test
    public void testGetHostNameWithSession() {
        Properties properties = new Properties();
        properties.setProperty(EmailConstants.MAIL_HOST, "session.host.name");
        Session session = Session.getInstance(properties);
        email.setMailSession(session);
        
        assertEquals("session.host.name", email.getHostName());
    }

    @Test
    public void testGetHostNameNull() {
        assertNull(email.getHostName());
    }

    // 7. getMailSession()
    @Test
    public void testGetMailSession() throws Exception {
        email.setHostName("localhost");
        Session session = email.getMailSession();
        assertNotNull(session);
    }

    @Test(expected = EmailException.class)
    public void testGetMailSessionNoHostName() throws Exception {
        email.getMailSession();
    }

    @Test
    public void testGetMailSessionWithVariousProperties() throws Exception {
        email.setHostName("localhost");
        email.setAuthentication("user", "pwd");
        email.setBounceAddress("bounce@example.com");
        email.setSSLOnConnect(true);
        email.setSocketConnectionTimeout(1000);
        email.setSocketTimeout(2000);
        
        Session session = email.getMailSession();
        assertNotNull(session);
        assertEquals("localhost", session.getProperty(EmailConstants.MAIL_HOST));
    }

    // 8. getSentDate()
    @Test
    public void testGetSentDate() {
        Date d1 = new Date(123456789L);
        email.setSentDate(d1);
        Date d2 = email.getSentDate();
        
        assertEquals(d1.getTime(), d2.getTime());
        // Should return a copy
        assertNotSame(d1, d2);
    }

    @Test
    public void testGetSentDateNull() {
        // Should generate a new date if null
        assertNotNull(email.getSentDate());
    }

    // 9. getSocketConnectionTimeout()
    @Test
    public void testGetSocketConnectionTimeout() {
        assertEquals(EmailConstants.SOCKET_TIMEOUT_MS, email.getSocketConnectionTimeout());
        email.setSocketConnectionTimeout(5000);
        assertEquals(5000, email.getSocketConnectionTimeout());
    }

    // 10. setFrom(String email)
    @Test
    public void testSetFrom() throws Exception {
        email.setFrom(TEST_EMAILS[0]);
        assertEquals(TEST_EMAILS[0], email.getFromAddress().getAddress());
    }

    // Stub class to instantiate Email
    private static class EmailStub extends Email {
        @Override
        public Email setMsg(String msg) throws EmailException {
            return this;
        }
    }
}