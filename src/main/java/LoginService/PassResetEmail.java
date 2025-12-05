package LoginService;

import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class PassResetEmail {

    private static final String SENDER_EMAIL = "rateyourhike212@gmail.com";
    private static final String SENDER_PASSWORD = "IloveCSCI201!";
    private static final String SMTP_HOST = "smtp.gmail.com"; 
    private static final String SMTP_PORT = "587";

    public static void sendSecurityCode(String recipientEmail, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Your Security Verification Code");
        
        String htmlContent = "<h3>Security Check</h3>"
                + "<p>Your verification code is: <b>" + code + "</b></p>"
                + "<p>This code expires in 15 minutes.</p>";
        
        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
    }
}