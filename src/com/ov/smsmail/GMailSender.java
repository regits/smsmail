package com.ov.smsmail;

import android.util.Log;
import javax.activation.DataHandler;   
import javax.activation.DataSource;   
import javax.mail.Message;   
import javax.mail.PasswordAuthentication;   
import javax.mail.Session;   
import javax.mail.Transport;   
import javax.mail.internet.InternetAddress;   
import javax.mail.internet.MimeMessage;   
import java.io.ByteArrayInputStream;   
import java.io.IOException;   
import java.io.InputStream;   
import java.io.OutputStream;   
import java.security.Security;   
import java.util.Properties;   

public class GMailSender extends javax.mail.Authenticator {   
	private String user;   
	private String password;   
	private Session session;   

	static {   
		Security.addProvider(new com.ov.smsmail.JSSEProvider());   
	}  

	public GMailSender(String user, String password) {   

		Log.d("smsmail", "GMailSender init");

		this.user = user;   
		this.password = password;   

		Properties props = new Properties();   
		props.setProperty("mail.transport.protocol", "smtp");   
		props.setProperty("mail.host", "smtp.gmail.com");   
		props.put("mail.smtp.auth", "true");   
		props.put("mail.smtp.port", "465");   
		//props.put("mail.smtp.timeout", "5000");   
		props.put("mail.smtp.socketFactory.port", "465");   
		props.put("mail.smtp.socketFactory.class",   
				"javax.net.ssl.SSLSocketFactory");   
		props.put("mail.smtp.socketFactory.fallback", "false");   
		props.setProperty("mail.smtp.quitwait", "false");   

		session = Session.getInstance(props, this);   
	}   

	protected PasswordAuthentication getPasswordAuthentication() {   
		return new PasswordAuthentication(user, password);   
	}   

	public synchronized boolean sendMail(String subject, String body, String sender, String recipients){   
	try{
		MimeMessage message = new MimeMessage(session);   
		DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));   
		message.setSender(new InternetAddress(sender));   
		message.setSubject(subject);   
		message.setDataHandler(handler);   
		if (recipients.indexOf(',') > 0)   
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));   
		else  
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));   
		Transport.send(message);

		return true;
	}catch(Exception e){
			Log.e("smsmail", e.getMessage(), e);

			return false;
	}
	}   

	public class ByteArrayDataSource implements DataSource {   
		private byte[] data;   
		private String type;   

		public ByteArrayDataSource(byte[] data, String type) {   
			super();   
			this.data = data;   
			this.type = type;   
		}   

		public ByteArrayDataSource(byte[] data) {   
			super();   
			this.data = data;   
		}   

		public void setType(String type) {   
			this.type = type;   
		}   

		public String getContentType() {   
			if (type == null)   
				return "application/octet-stream";   
			else  
				return type;   
		}   

		public InputStream getInputStream() throws IOException {   
			return new ByteArrayInputStream(data);   
		}   

		public String getName() {   
			return "ByteArrayDataSource";   
		}   

		public OutputStream getOutputStream() throws IOException {   
			throw new IOException("Not Supported");   
		}   
	}   
}  
