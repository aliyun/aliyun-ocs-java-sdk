package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsFuture;
import com.aliyun.ocs.OcsOptions;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.command.binary.Command;

public interface MemcachedSingleCommand extends Command {
	public OcsResult syncExecute(OcsOptions options);

	public OcsFuture<OcsResult> execute(OcsOptions options) throws OcsException;
}
