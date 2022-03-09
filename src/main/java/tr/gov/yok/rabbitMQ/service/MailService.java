package tr.gov.yok.rabbitMQ.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import tr.gov.yok.rabbitMQ.model.Attachment;
import tr.gov.yok.rabbitMQ.model.Mail;

@Service
public class MailService {
	private static final Logger logger = LogManager.getLogger();

	public String mailTemplateDuzenle(String parameterBody) {
		StringBuilder contentBuilder = new StringBuilder();
		InputStream stream = null;
		BufferedReader in = null;
		try {
			stream =  MailService.class.getClassLoader().getResourceAsStream("templates/MailTemplate.html");
			in = new BufferedReader(new InputStreamReader(stream));
			String str;

			while ((str = in.readLine()) != null) {						
				if (str.contains("myParameter")) {
					contentBuilder.append(parameterBody);
				} else {
					contentBuilder.append(str);
				}
			}
			
			String content = contentBuilder.toString();
			return content;	
		} catch (IOException e) {
			logger.error("YÖK RabbitMQ MailService mailTemplateDuzenle IOException : " + e);
			return null;
		} catch (Exception e) {
			logger.error("YÖK RabbitMQ MailService mailTemplateDuzenle Genel Exception : " + e);
			return null;
		}finally {
			try {
				in.close();
			} catch (IOException e) {
				logger.error("YÖK RabbitMQ MailService mailTemplateDuzenle finally in.close exception : " + e);
			}
			try {
				stream.close();
			} catch (IOException e) {
				logger.error("YÖK RabbitMQ MailService mailTemplateDuzenle finally stream.close exception : " + e);
			}
		}
	}
	
	

	public void sendMail(Mail mail) throws Exception {
		if (mail != null && mail.getBody() != null) {
			sender(mail.getSender(), mail.getSubject(), mail.getTo(), mail.getBody(),mail.getAttachments());
			System.out.println("Mail gönderildi : " + mail);
		} else {
			System.out.println("YÖK RabbitMQ MailService sendMail mail bilgisi bulunamadi");
		}
	}
	
	public boolean sender(String sender, String subject, String to, String body, List<Attachment> attachments) throws Exception {
		InputStream stream = null;				
		Properties props = new Properties();
		props.put("mail.smtp.host", "relay.yok.gov.tr");
		props.put("mail.smtp.port", "25");

		Session session = Session.getInstance(props, null);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(sender));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(body, "text/html; charset=utf-8");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// attachments...
			for(Attachment attch: attachments) {
				messageBodyPart = new MimeBodyPart();					
				stream =  MailService.class.getClassLoader().getResourceAsStream(attch.getFileBase64());
				 
				if(attch.getFileBase64() != null) {
					//  fileAttachment = yok logo
					if(attch.getFileName().equals("logo.png")) {						
						DataSource source = new ByteArrayDataSource(stream, "image/png");
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setFileName(attch.getFileName());
						multipart.addBodyPart(messageBodyPart);
					}else {		//  fileAttachment = image, pdf , text... base64
						String fileNameLW = attch.getFileName().toLowerCase();
						MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
						String contentType = mimetypesFileTypeMap.getContentType(fileNameLW);
						
						MimeBodyPart filePart = new PreencodedMimeBodyPart("base64");						
				        filePart.setContent(attch.getFileBase64(), contentType);
				        filePart.setFileName(attch.getFileName());
						multipart.addBodyPart(filePart);
					//	messageBodyPart.attachFile(file, "application/octet-stream", "base64");
					}
				}
			}

			message.setContent(multipart);
			Transport.send(message);
			return true;
		} catch (MessagingException mex) {
			logger.error("YÖK RabbitMQ MailService sender hata : " + mex);
			throw mex;
		} finally{
			stream.close();
		}
	}

}
