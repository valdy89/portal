package cz.mycom.veeam.portal.service;

public class SessionIdThreadLocal {
	static ThreadLocal<String> sessionId = new ThreadLocal<>();

	public static void setSessionId(String id) {
		sessionId.set(id);
	}

	public static String getSessionId() {
		return sessionId.get();
	}

	public static void clean() {
		sessionId.set(null);
	}

}
