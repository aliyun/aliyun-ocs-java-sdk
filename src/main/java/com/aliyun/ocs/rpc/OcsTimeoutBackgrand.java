package com.aliyun.ocs.rpc;

import java.util.ArrayList;
import java.util.concurrent.DelayQueue;

public class OcsTimeoutBackgrand extends Thread {

	public OcsTimeoutBackgrand() {
		this.setDaemon(true);
		this.setName("OCS-Timeout-Channel-Checker");
	}

	protected DelayQueue<OcsRpcID> queue = new DelayQueue<OcsRpcID>();

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				ArrayList<OcsRpcID> idset = new ArrayList<OcsRpcID>();
				idset.add(queue.take());
				queue.drainTo(idset);
				for (OcsRpcID entry : idset) {
					entry.getSession().clearTimeoutCallTask(entry.getOpaque());
				}
				Thread.sleep(5);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public boolean registRpcID(OcsChannel session, Integer opaque, long timeout) {
		OcsRpcID rpcID = new OcsRpcID(session, opaque, timeout);
		session.setRpcID(rpcID);
		return queue.add(rpcID);
	}

	public void removeRpcID(OcsRpcID rpcID) {
		queue.remove(rpcID);
	}
}
