package sandbox.sfwatergit.utils.emailer;

import com.google.common.collect.Maps;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;


/**
 * Created by sidneyfeygin on 12/5/15.
 */
public class EmailMessageBuilder {

    private ConcurrentMap<String, String> messageData;

    private EmailMessageBuilder() {
    }

    public static void write(

            final Consumer<EmailMessageBuilder> mailer) {
        final EmailMessageBuilder emailMessageBuilder = new EmailMessageBuilder();
        mailer.accept(emailMessageBuilder);
    }

    public static void main(String[] args) {

    }

    public EmailMessageBuilder from(String from) {
        messageData = Maps.newConcurrentMap();
        messageData.put("from", from);
        return this;
    }

    public EmailMessageBuilder to(String to) {
        messageData.put("to", to);
        return this;
    }

    public EmailMessageBuilder subject(String subject) {
        messageData.put("subject", subject);
        return this;
    }

    public EmailMessageBuilder body(String body) {
        messageData.put("body", body);
        return this;
    }

    public EmailMessageBuilder send(String username, String password) {

        Properties props = new Properties();
        props.setProperty("mail.user", username);
        props.setProperty("mail.password", password);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(messageData.get("from")));

            message.setRecipient(Message.RecipientType.TO, new InternetAddress(messageData.get("to")));
            message.setSubject(messageData.get("subject"));
            message.setText(messageData.get("body"));
            Transport.send(message);
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        }


        return this;

    }


}
