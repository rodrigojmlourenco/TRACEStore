package org.trace.store.services.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserRegistryRequest {

	private String 	name,
					username,
					email,
					password,
					confirm,
					phone,
					address;

	public UserRegistryRequest(){}
	
	public UserRegistryRequest(
			String name,
			String username, String email,
			String password, String confirm,
			String phone, String address){
		
		this.name = name;
		this.username = username;
		this.email = email;
		this.password = password;
		this.confirm = confirm;
		this.phone = phone;
		this.address = address;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirm() {
		return confirm;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
