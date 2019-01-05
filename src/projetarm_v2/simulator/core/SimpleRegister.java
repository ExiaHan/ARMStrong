package projetarm_v2.simulator.core;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleRegister implements Register {
	private AtomicLong value = new AtomicLong(0);
	
	public SimpleRegister() {}

	public SimpleRegister(long value) {
		this.value.set(value);
	}
	
	public int getValue() {
		Long value = this.value.get();
		return value.intValue();
	}

	@Override
	public void setValue(long value) {
		this.value.set(value);
	}

}
