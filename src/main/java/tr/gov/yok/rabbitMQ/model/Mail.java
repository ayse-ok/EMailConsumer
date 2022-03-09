package tr.gov.yok.rabbitMQ.model;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mail implements Serializable{
	private static final long serialVersionUID = 6279595443086992379L;
	
	private String id;
	
	private String to;
	private String sender;
	private String sendTime;
	private String subject;
	private String body;
	
	@SerializedName("attachments")
	private List<Attachment> attachments;	
}
