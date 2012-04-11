package com.gentics.cr.monitoring;

import com.jamonapi.Monitor;

public class UseCase {

	private Monitor mon;
	private boolean enabled = false;

	protected UseCase(Monitor mon, boolean monitorenabled) {
		this.mon = mon;
		enabled = monitorenabled;
	}

	public void stop() {
		if (enabled) {
			this.mon.stop();
		}
	}
}
