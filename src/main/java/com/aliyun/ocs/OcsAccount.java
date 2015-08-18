package com.aliyun.ocs;

public class OcsAccount {
	private String username;
	private String password;
	private boolean exemptPassword = false;

	public OcsAccount(String username, String password) {
		this.setUsername(username);
		this.setPassword(password);
	}
	public OcsAccount(String username) {
		this.setUsername(username);
		this.exemptPassword = true;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isExemptPassword() {
		return exemptPassword;
	}

	public void setExemptPassword(boolean exemptPassword) {
		this.exemptPassword = exemptPassword;
	}
}
