package com.aliyun.ocs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OcsKeyValueCollection {
	public static class OcsValue {
		private Object value;
		private int exper;
		private long cas;

		public OcsValue(Object value, int exper, long cas) {
			this.value = value;
			this.setExper(exper);
			this.setCas(cas);
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public int getExper() {
			return exper;
		}

		public void setExper(int exper) {
			this.exper = exper;
		}

		public long getCas() {
			return cas;
		}

		public void setCas(long cas) {
			this.cas = cas;
		}
	}

	private Map<String, OcsValue> collection = new HashMap<String, OcsValue>();

	public OcsKeyValueCollection() {

	}

	public OcsKeyValueCollection addKeyValue(String key, Object value, int exper, long cas) {
		collection.put(key, new OcsValue(value, exper, cas));
		return this;
	}

	public OcsKeyValueCollection addKeyValue(String key, Object value) {
		collection.put(key, new OcsValue(value, 0, 0l));
		return this;
	}

	public Map<String, OcsValue> build() {
		return collection;
	}
	
	public OcsValue getValue(String key) {
		return collection.get(key);
	}
	
	public Set<String> keySet() {
		return collection.keySet();
	}
}
