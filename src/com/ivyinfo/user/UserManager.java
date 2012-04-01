package com.ivyinfo.user;

import com.ivyinfo.feiying.utity.CommonUtil;

/**
 * UserManager - manage the current logined user
 * @author sk
 *
 */
public class UserManager {
	private UserBean user;

	private static UserManager instance;

	private UserManager() {
		user = new UserBean();
	}

	public synchronized static UserManager getInstance() {
		if (instance == null) {
			instance = new UserManager();
		}
		return instance;
	}
	
	/**
	 * set user info
	 * @param name
	 * @param password
	 */
	public UserBean setUser(String name, String password) {
		if (user == null) {
			user = new UserBean();
		}
		String digest = CommonUtil.md5(name + password);
		user.setName(name);
		user.setPassword(password);
		user.setUserkey(digest);
		return user;
	}
	
	public UserBean setUserInfo(String name, String userkey) {
		if (user == null) {
			user = new UserBean();
		}
		user.setName(name);
		user.setUserkey(userkey);
		return user;
	}
	
	/**
	 * set user
	 * @param user
	 */
	public void setUser(UserBean user) {
		this.user = user;
	}
	
	/**
	 * get user
	 * @return user bean or null
	 */
	public UserBean getUser() {
		return user;
	}
	
	public void removeUser() {
		user = null;
	}
}
