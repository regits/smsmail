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

public class CheckMail{
	private String from = ""; 
	private String password = "";

	void Set(String nf, String np)
	{
		from = nf;
		password = np;
	}

	boolean check()
	{
        Log.d("smsmail", "start check mail");

		GMailReader reader = new GMailReader(from, password);

		int sms_sent = reader.check();

		if(sms_sent < 0){
			//TODO: retry
			Log.d("smsmail", "check mail fail");
			return false;
		}

		Log.d("smsmail", String.format("CheckMail done, total send SMS:%d", sms_sent) );
		return true;
	}
}
