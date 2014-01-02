package at.lnu.ass3.telephony;

import java.io.Serializable;

public class CallEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String senderPhoneNumber;

	public CallEntity(long id, String senderPhoneNumber) {
		super();
		this.id = id;
		this.senderPhoneNumber = senderPhoneNumber;
	}

	public long getId() {
		return id;
	}

	public String getSenderPhoneNumber() {
		return senderPhoneNumber;
	}

	public void setSenderPhoneNumber(String senderPhoneNumber) {
		this.senderPhoneNumber = senderPhoneNumber;
	}

	@Override
	public String toString() {
		return senderPhoneNumber;
	}
	
	
}
