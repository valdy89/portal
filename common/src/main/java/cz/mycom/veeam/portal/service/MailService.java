package cz.mycom.veeam.portal.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import cz.mycom.veeam.portal.repository.ConfigRepository;

/**
 * @author dursik
 */
@Slf4j
@Service
public class MailService {

	@Autowired
	private ConfigRepository configRepository;
	@Autowired
	private KeyStoreService keyStoreService;

	public void sendMail(String from, String to, String subject, String text) {
		sendMail(from, to, null, subject, text);
	}

	public void sendMail(String from, String to, String bcc, String subject, String text) {
		log.debug("Sending email - from: {}, to: {}, bcc: {}, subject: {}", from, to, bcc, subject);
		String host = configRepository.getOne("mail.smtp.host").getValue();
		String port = configRepository.getOne("mail.smtp.port").getValue();
		String username = configRepository.getOne("mail.smtp.user").getValue();
		String password = keyStoreService.readData("mail.smtp.password");

		Session session = getSession(host, port, username, password);

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			if (StringUtils.isNotBlank(bcc)) {
				message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
			}
			message.setSubject(subject);
			message.setText(text);

			sendMessage(host, port, username, password, session, message);
		} catch (MessagingException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private void sendMessage(String host, String port, String username, String password, Session session, Message message) throws MessagingException {
		Transport transport = session.getTransport("smtp");
		transport.connect(host, Integer.parseInt(port), username, password);
		transport.sendMessage(message, message.getAllRecipients());
	}

	public void sendMail(String from, String to, String subject, String text, String filename, ByteArrayOutputStream content) {
		log.debug("Sending email - from: {}, to: {}, subject: {}, filename: {}", from, to, subject, filename);
		String host = configRepository.getOne("mail.smtp.host").getValue();
		String port = configRepository.getOne("mail.smtp.port").getValue();
		String username = configRepository.getOne("mail.smtp.user").getValue();
		String password = keyStoreService.readData("mail.smtp.password");
		Session session = getSession(host, port, username, password);

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(text);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			DataSource source = new ByteArrayDataSource(new ByteArrayInputStream(content.toByteArray()), "application/pdf");
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(filename);
			multipart.addBodyPart(messageBodyPart);

			// Send the complete message parts
			message.setContent(multipart);

			sendMessage(host, port, username, password, session, message);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void sendError(String subject, Exception e) {
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		sendMail(configRepository.getOne("support.email").getValue(), configRepository.getOne("admin.email").getValue(), subject, writer.toString());
	}

	private Session getSession(String host, String port, String username, String password) {
		Properties props = new Properties();

		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", username);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.port", port);

		props.put("mail.smtp.starttls.enable", true); // added this line
		props.put("mail.smtp.auth", true);
		return Session.getInstance(props,
				new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
	}
}
