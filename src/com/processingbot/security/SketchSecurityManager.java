package com.processingbot.security;

import java.security.Permission;

public class SketchSecurityManager extends SecurityManager {

	private final SecurityManager previous;
	
	public SketchSecurityManager() {
		this.previous = System.getSecurityManager();
	}
	
//	@Override
//	public void checkExit(int status) {
//        throwExitException();
//    }
	
	@Override
	public void checkPermission(Permission perm) {
		System.out.println(perm.getName() + " " + perm.getActions());
		switch(perm.getActions()) {
		case "read":
		case "accessDeclaredMembers":
			return;
		case "exitVM":
			throwExitException();
		default:
			if(this.previous != null) this.previous.checkPermission(perm);
			else super.checkPermission(perm);
		}
	}
	
	private void throwExitException() {
		throw new SecurityException("Cannot use System.exit() in sketches. To kill your sketch use exit() instead.");
	}
	
	public void enable() {
		System.setSecurityManager(this);
	}
	
	public void disable() {
		System.setSecurityManager(this.previous);
	}
	
}