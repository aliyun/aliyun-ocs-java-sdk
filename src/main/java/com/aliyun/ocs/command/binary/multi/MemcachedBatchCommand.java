package com.aliyun.ocs.command.binary.multi;

import java.util.Map;
import java.util.Set;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsFuture;
import com.aliyun.ocs.OcsOptions;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.command.binary.Command;

public interface MemcachedBatchCommand extends Command {
	public OcsFuture<Map<String, OcsResult>> executeBatch(boolean write, OcsOptions options) throws OcsException;

	public Map<String, OcsResult> syncExecuteBatch(boolean write, OcsOptions options) throws OcsException;

	public Set<String> getKeys();

	public int dummyStatus();
}
