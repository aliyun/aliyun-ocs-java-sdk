package com.aliyun.ocs.rpc;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import com.aliyun.ocs.OcsAccount;
import com.aliyun.ocs.support.logging.Log;
import com.aliyun.ocs.support.logging.LogFactory;
import com.aliyun.ocs.util.OcsPlainHandler;
import com.aliyun.ocs.util.Util;

public class OcsConnection {
	private Log logger = LogFactory.getLog(OcsConnection.class);
	private final SocketAddress addr;
	private final OcsAccount account;
	private final String domain;
	/*
	 * SASL
	 */
	private OcsPlainHandler plainHandler = null;
	protected String[] mech;
	protected byte[] challenge;
	private String mechanism;
	private byte[] evaluate;
	private AtomicInteger done = new AtomicInteger(1);
	private AtomicBoolean sparked = new AtomicBoolean(false);
	private final AtomicInteger SEQ_NUMBER = new AtomicInteger(0);
	private OcsChannel channel = null;
	private int key = 0;
	public int generateOpaque() {
		int rv = SEQ_NUMBER.incrementAndGet();
		while (rv < 0) {
			SEQ_NUMBER.compareAndSet(rv, 0);
			rv = SEQ_NUMBER.incrementAndGet();
		}
		return rv;
	}

	public OcsConnection(final String domain, final OcsAccount account, int key) {
		this.domain = domain;
		this.addr = Util.cast2SocketAddress(domain);
		this.account = account;
		this.setKey(key);
		if (account.isExemptPassword() == false) {
			this.plainHandler = new OcsPlainHandler(account.getUsername(), account.getPassword());
			mech = new String[] { "PLAIN" };
			try {
				SaslClient sc = Sasl.createSaslClient(mech, null, "memcached", addr.toString(), null, plainHandler);
				evaluate = sc.evaluateChallenge(/* empty bytes */new byte[0]);
				mechanism = sc.getMechanismName();
			} catch (SaslException e) {
				logger.error("SaslClient exception", e);
				throw new RuntimeException("SaslClient exception", e);
			}
		}
		channel = null;
	}

	public byte[] getAuthEveluate() {
		return this.evaluate;
	}

	public byte[] getAuthMechanism() {
		return this.mechanism.getBytes();
	}

	public boolean isAuthenticated() {
		return sparked.get();
	}

	public SocketAddress getAddress() {
		return addr;
	}

	public void authCompelete(boolean done) {
		if (done == false) {
			this.done.set(1);
			sparked.set(false);
		} else {
			this.done.set(-1);
			sparked.set(true);
		}
	}

	public boolean needAuthStartNow() {
	    return done.decrementAndGet() == 0;

	}

	public OcsChannel getChannel() {
		return channel;
	}

	public void setChannel(OcsChannel channel) {
		this.channel = channel;
	}

	public OcsAccount getAccount() {
		return account;
	}

	public void removeChannel() {
		this.setChannel(null);
		this.authCompelete(false);
	}

	public String getDomain() {
		return domain;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}
}
