package com.ov.smsmail;

import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import android.os.Bundle;

/**
 * 
 **/

public class ResolveMail{

	private MimeMessage mineMsg = null;
	private StringBuffer mailContent = new StringBuffer();
	private String dataFormat = "yy-MM-dd HH:mm";

	/**
	 * 
	 * @param mimeMessage
	 */

	public ResolveMail(MimeMessage mimeMessage) {
		this.mineMsg = mimeMessage;
	}

	public void setMimeMessage(MimeMessage mimeMessage) {
		this.mineMsg = mimeMessage;
	}

	/**
	 * 
	 * @throws MessagingException
	 */

	public String getFrom() throws MessagingException {

		InternetAddress address[] = (InternetAddress[]) mineMsg.getFrom();
		String addr = address[0].getAddress();
		String name = address[0].getPersonal();

		if (addr == null) {

			addr = "";
		}
		if (name == null) {

			name = "";
		}

		String nameAddr = name + "<" + addr + ">";
		return nameAddr;

	}

	/**
	 * 
	 * @return
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getMailAddress(String Type) throws Exception {
		String mailAddr = "";
		String addType = Type.toUpperCase();
		InternetAddress[] address = null;
		if (addType.equals("TO")) {

			address = (InternetAddress[]) mineMsg
					.getRecipients(Message.RecipientType.TO);
		} else if (addType.equals("CC")) {
			address = (InternetAddress[]) mineMsg
					.getRecipients(Message.RecipientType.CC);
		} else if (addType.equals("BBC")) {
			address = (InternetAddress[]) mineMsg
					.getRecipients(Message.RecipientType.BCC);

		} else {
			System.out.println("error type!");
			throw new Exception("Error emailaddr type!");
		}

		if (address != null) {
			for (int i = 0; i < address.length; i++) {

				String mailaddress = address[i].getAddress();
																
																
				if (mailaddress != null) {
					mailaddress = MimeUtility.decodeText(mailaddress);
				} else {
					mailaddress = "";
				}

				String name = address[i].getPersonal();
				if (name != null) {
					name = MimeUtility.decodeText(name);
				} else {
					name = " ";
				}
				mailAddr = name + "<" + mailaddress + ">";
			}

		}
		return mailAddr;

	}

	/**
	 * 
	 * @return String
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */

	public String getSubject() throws UnsupportedEncodingException,
			MessagingException {
		String subject = "";
		subject = MimeUtility.decodeText(mineMsg.getSubject());
		if (subject == null) {
			subject = "";

		}
		return subject;

	}

	/**
	 * 
	 * @throws MessagingException
	 */
	public String getSentDate() throws MessagingException {
		Date sentdata = mineMsg.getSentDate();
		if (sentdata != null) {
			SimpleDateFormat format = new SimpleDateFormat(dataFormat);
			return format.format(sentdata);
		} else {

			return "Unknown";
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public String getMailContent() throws Exception {
		compileMailContent((Part) mineMsg);
		return mailContent.toString();
	}

	public void setMailContent(StringBuffer mailContent) {
		this.mailContent = mailContent;
	}

	/**
	 * 
	 * @param part
	 * @throws MessagingException
	 * @throws IOException
	 * @throws Exception
	 */
	public void compileMailContent(Part part) throws MessagingException,
			IOException {
		String contentType = part.getContentType();
			if (part.isMimeType("text/plain")) {
				mailContent.append((String) part.getContent());
			} else if (part.isMimeType("text/html")) {
				mailContent.append((String) part.getContent());
			} else if (part.isMimeType("multipart/*")) {
				Multipart multipart = (Multipart) part.getContent();
				int counts = multipart.getCount();
				for (int i = 0; i < counts; i++) {
					compileMailContent(multipart.getBodyPart(i));
				}
			} else if (part.isMimeType("message/rfc822")) {
				compileMailContent((Part) part.getContent());
			} else {
				Log.d("smsmail", "contentType:" + contentType);
			}
	}

	/**
	 */
	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

}
