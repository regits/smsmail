package com.ov.smsmail;

import android.telephony.SmsManager;
import android.util.Log;
import javax.activation.DataHandler;   
import javax.activation.DataSource;   
import javax.mail.Flags;
import javax.mail.search.FlagTerm;
import javax.mail.Folder;
import javax.mail.Store;
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
import java.util.List;

public class GMailReader extends javax.mail.Authenticator {   
	private String user;   
	private String password;   
	private Session session;   

	static {   
		Security.addProvider(new com.ov.smsmail.JSSEProvider());   
	}  

	public GMailReader(String user, String password) {   

		Log.d("smsmail", "GMailReader init");

		this.user = user;   
		this.password = password;   

		Properties props = new Properties();   
		props.setProperty("mail.imap.host", "imap.gmail.com");   
		props.put("mail.store.protocol", "imap");
		props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");  
		props.setProperty("mail.imap.socketFactory.fallback", "false");  
		//props.put("mail.imap.connectiontimeout", ConfigKeys.IMAP_CONNECTIONTIMEOUT);  
		props.setProperty("mail.imap.port", "993");  
		props.setProperty("mail.imap.socketFactory.port", "993");

		session = Session.getInstance(props, this);   
		//session.setDebug(true);
	}   

	protected PasswordAuthentication getPasswordAuthentication() {   
		return new PasswordAuthentication(user, password);   
	}   

	public synchronized int check(){
	try{
		Store store;
		int count;
		store = session.getStore();
		store.connect();

		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);

		int mailCount = folder.getMessageCount();

		Log.d("smsmail", String.format("Mail count: %d", mailCount) );

		Message messages[] = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
		Log.d("smsmail", String.format("SEEN count: %d", messages.length) );

		int sms_sent = 0;

		for(int i = 0; i < messages.length; i++){
			ResolveMail mm = new ResolveMail((MimeMessage) messages[i]);
			String subject = mm.getSubject();
			Log.d("smsmail", subject );

			//check subject, if it's "SmsMail:", get content
			int smsto_index = subject.indexOf("SmsMail:");
			if (smsto_index == -1) {
				continue;
			}
			String smsto = subject.substring(smsto_index + 8);
			String content = mm.getMailContent();

			//remove '@gmail.com'
			int fti = content.indexOf("@gmail.com");
			if (fti != -1){
				content = content.substring(0, fti);
				fti = content.lastIndexOf("\n");
				if(fti != -1){
					content = content.substring(0, fti);
				}
			}
			//remove empty line
			fti = content.indexOf("\n\n");
			if (fti != -1){
				content = content.substring(0, fti);
			}

			Log.d("smsmail", "SMS to " + smsto + ":" + content );

			SmsManager sms_man = SmsManager.getDefault();
			if(content.length() <= 70){
				sms_man.sendTextMessage(smsto,  null, content, null, null);
				sms_sent ++;
			}else{
				List<String> smsDivs = sms_man.divideMessage(content);
				for(String sms : smsDivs) {
					sms_sent ++;
					sms_man.sendTextMessage(smsto, null, sms, null, null);
				}
			}
		}

		folder.close(true);
		store.close();

		return sms_sent;
	}catch(Exception e){
			Log.e("smsmail", e.getMessage(), e);

			return -1;
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
