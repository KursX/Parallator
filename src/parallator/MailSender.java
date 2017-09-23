package parallator;


import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Date;
import java.util.Properties;

public class MailSender extends Authenticator {

    private String subject;
    private String body = "";

    private Multipart multipart = new MimeMultipart();

    public MailSender(String subject, String body) {
        this.subject = subject;
        this.body = body;

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public boolean send() throws Exception {
        Properties props = _setProperties();
        Session session = Session.getInstance(props, this);
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject(subject);
        msg.setFrom(new InternetAddress("kursx.noreply@gmail.com"));
        msg.setRecipients(MimeMessage.RecipientType.TO, new InternetAddress[] {
                new InternetAddress("kursxinc@gmail.com")
        });
        msg.setSentDate(new Date());
        msg.setContent(multipart);
        Transport.send(msg);
        return true;
    }

    public MailSender addFile(File file) throws Exception {
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setContent(body, "text/html");
        multipart.addBodyPart(mbp1);
        MimeBodyPart mbp2 = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(file);
        mbp2.setDataHandler(new DataHandler(fds));
        mbp2.setFileName(fds.getName());
        multipart.addBodyPart(mbp2);
        return this;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("kursx.noreply@gmail.com", "kursx_noreply");
    }

    private Properties _setProperties() {
        Properties props = new Properties();
        props.put("mail.debug", "false");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        return props;
    }
}