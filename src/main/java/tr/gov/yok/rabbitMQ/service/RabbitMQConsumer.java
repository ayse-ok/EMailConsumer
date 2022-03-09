package tr.gov.yok.rabbitMQ.service;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tr.gov.yok.rabbitMQ.exception.InvalidMessageException;
import tr.gov.yok.rabbitMQ.model.Mail;

@Component
public class RabbitMQConsumer {
	@Autowired
	MailService mailService;
	
	private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
	private CountDownLatch latch = new CountDownLatch(1);
	
	@RabbitListener(queues = "${tr.gov.yok.rabbitMQ.queue}")
	public void receivedMessage(Mail mail) throws Exception{				
		logger.info("Recieved Message From RabbitMQ: " + mail);
		System.out.println("Recieved Message From RabbitMQ: " + mail);
		
		if (mail.getId() == null) {
			throw new InvalidMessageException();			
		}else {			
			String mailIcerik = mailService.mailTemplateDuzenle(mail.getBody());
			mail.setBody(mailIcerik);
			
			mailService.sendMail(mail);			
			System.out.println("mail başarıyla gönderildi To: " + mail.getTo());
			latch.countDown();
		}
	}
	
	public CountDownLatch getLatch() {
		return latch;
	}
}
