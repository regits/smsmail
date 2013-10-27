package com.ov.smsmail;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import android.telephony.SmsMessage;

public class SendMail{
	private String from = ""; 
	private String password = "";
	private String to = "";

	void Set(String nf, String np, String nt)
	{
		from = nf;
		password = np;
		to = nt;
	}

	boolean send()
	{
		try {   
			GMailSender sender = new GMailSender(from, password);
			sender.sendMail("test",   "111",  from, to);
			Log.d("smsmail", "SendMail done");
		} catch (Exception e) {
			Log.e("smsmail", e.getMessage(), e);
			return false;
		}

		return true;
	}

	boolean sms_send_mail(SmsMessage message)
	{
        Log.d("smsmail", message.getOriginatingAddress() + " : " +
                  message.getDisplayOriginatingAddress() + " : " +
                  message.getDisplayMessageBody() + " : " +
                  message.getTimestampMillis());

		Log.d("smsmail", from + ":" + password + ":" + to);

		GMailSender sender = new GMailSender(from, password);

		Date date= new Date(message.getTimestampMillis());
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sendDate=simpleDateFormat.format(date);

		//boolean ok = true;
		boolean ok = sender.sendMail("SmsMail:" + message.getDisplayOriginatingAddress(),  message.getDisplayMessageBody() + "\n" + sendDate,  from, to);

		if(!ok){
			//TODO: retry
			Log.d("smsmail", "fail, TODO retry");
			return false;
		}

		Log.d("smsmail", "SendMail done");
		return true;
	}
}
