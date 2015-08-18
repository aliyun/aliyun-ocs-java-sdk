package com.aliyun.ocs;

import java.util.HashMap;
import java.util.Map;

public class OcsKeyCounterCollection {
	public static class OcsCounter {
		private long value;
		private int exper;
		private long init;

		public OcsCounter(long value, long init, int exper) {
			this.value = value;
			this.setExper(exper);
			this.setInit(init);
		}

		public long getValue() {
			return value;
		}

		public void setValue(long value) {
			this.value = value;
		}

		public int getExper() {
			return exper;
		}

		public void setExper(int exper) {
			this.exper = exper;
		}

		public long getInit() {
			return init;
		}

		public void setInit(long init) {
			this.init = init;
		}
	}

	private Map<String, OcsCounter> collection = new HashMap<String, OcsCounter>();

	public OcsKeyCounterCollection() {

	}

	public OcsKeyCounterCollection addKeyCounter(String key, long value, long init, int exper) {
		collection.put(key, new OcsCounter(value, init, exper));
		return this;
	}

	public OcsKeyCounterCollection addKeyCounter(String key, long value, long init) {
		collection.put(key, new OcsCounter(value, init, 0));
		return this;
	}

	public Map<String, OcsCounter> build() {
		return collection;
	}
}
