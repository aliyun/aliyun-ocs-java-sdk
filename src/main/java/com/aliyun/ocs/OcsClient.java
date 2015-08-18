package com.aliyun.ocs;

import java.util.Map;
import java.util.Set;

import com.aliyun.ocs.command.binary.multi.MemcachedBatchCombineCommand;
import com.aliyun.ocs.command.binary.multi.MemcachedBatchCommand;
import com.aliyun.ocs.command.binary.multi.MemcachedBatchCounterCommand;
import com.aliyun.ocs.command.binary.multi.MemcachedBatchDeleteCommand;
import com.aliyun.ocs.command.binary.multi.MemcachedBatchGATCommand;
import com.aliyun.ocs.command.binary.multi.MemcachedBatchGetCommand;
import com.aliyun.ocs.command.binary.multi.MemcachedBatchStoreCommand;
import com.aliyun.ocs.command.binary.single.MemcachedCombineCommand;
import com.aliyun.ocs.command.binary.single.MemcachedCounterCommand;
import com.aliyun.ocs.command.binary.single.MemcachedDeleteCommand;
import com.aliyun.ocs.command.binary.single.MemcachedFlushCommand;
import com.aliyun.ocs.command.binary.single.MemcachedGATCommand;
import com.aliyun.ocs.command.binary.single.MemcachedGetCommand;
import com.aliyun.ocs.command.binary.single.MemcachedNoopCommand;
import com.aliyun.ocs.command.binary.single.MemcachedQuitCommand;
import com.aliyun.ocs.command.binary.single.MemcachedSingleCommand;
import com.aliyun.ocs.command.binary.single.MemcachedStoreCommand;
import com.aliyun.ocs.command.binary.single.MemcachedTouchCommand;
import com.aliyun.ocs.protocol.memcached.binary.MemcachedOpCode;
import com.aliyun.ocs.rpc.OcsRpc;

public class OcsClient {
	OcsAccount ocsAccount = null;
	String domain = null;
	private OcsRpc rpc = null;

	/**
	 * Initialize Ocs client. If the parameters succeeds, the client is ready to
	 * process ocs requests. If the parameters fails, throws OcsException.
	 * 
	 * @param domain
	 *            domain name
	 * @param account
	 *            ocs account
	 * @throws OcsExecption
	 *             if demain and account were null
	 */
	public OcsClient(String domain, OcsAccount account) throws OcsException {
		if (domain == null || account == null) {
			throw new OcsException("null parametes");
		}
		if (account.getPassword() == null || account.getUsername() == null) {
			throw new OcsException("null username or password");
		}
		this.domain = domain;
		this.ocsAccount = account;
		rpc = new OcsRpc(domain, account);
	}

	/**
	 * Initialize Ocs client. If the parameters succeeds, the client is ready to
	 * process ocs requests. If the parameters fails, throws OcsException.
	 * 
	 * @param domain
	 *            domain name
	 * @param username
	 *            user name
	 * @param password
	 *            password
	 * @throws OcsExecption
	 *             if domain and account were null
	 */
	public OcsClient(String domain, String username, String password) {
		this.domain = domain;
		this.ocsAccount = new OcsAccount(username, password);
		rpc = new OcsRpc(domain, ocsAccount);
	}
	public OcsClient(String domain, String username) {
		this.domain = domain;
		this.ocsAccount = new OcsAccount(username);
		rpc = new OcsRpc(domain, ocsAccount);
	}

	/**
	 * gracefully disconnect the current connection to Ocs cluster, rebuild it
	 * again.
	 */
	public void reconnect() {
		if (rpc != null) {
			rpc.reconnect();
		}
	}

	private OcsFuture<OcsResult> store(byte opcode, String key, Object value, int exper, long cas, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedStoreCommand(rpc, opcode, key, value, 0, exper, cas);
		return command.execute(options);
	}

	/**
	 * Add the key value pairs to the Ocs cluster asynchronously. Add command
	 * MUST fail if the item already exist.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @param exper
	 *            experation time of the key value pairs. The actual value sent
	 *            may either be Unix time (number of seconds since January 1,
	 *            1970, as a 32-bit value), or a number of seconds starting from
	 *            current time. In the latter case, this number of seconds may
	 *            not exceed 60*60*24*30 (number of seconds in 30 days); if the
	 *            number sent by a client is larger than that, the server will
	 *            consider it to be real Unix time value rather than an offset
	 *            from current time.
	 * @param cas
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return OcsFuture<OcsResult> a future representing the processing of this
	 *         operation
	 * @throws OcsException
	 *             throw exception if ADD command fails.
	 * 
	 */
	public OcsFuture<OcsResult> add(String key, Object value, int exper, OcsOptions options) throws OcsException {
		return store(MemcachedOpCode.ADD, key, value, exper, 0, options);
	}
	public OcsFuture<OcsResult> add(String key, Object value, OcsOptions options) throws OcsException {
		return add(key, value, 0, options);
	}
	public OcsFuture<OcsResult> add(String key, Object value) throws OcsException {
		return add(key, value, 0, OcsOptions.defaultOptions());
	}

	/**
	 * Write the key value pair to the Ocs cluster asynchronously. Set should
	 * store the data unconditionally if the key exists or not.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @param exper
	 *            experation time of the key value pairs. The actual value sent
	 *            may either be Unix time (number of seconds since January 1,
	 *            1970, as a 32-bit value), or a number of seconds starting from
	 *            current time. In the latter case, this number of seconds may
	 *            not exceed 60*60*24*30 (number of seconds in 30 days); if the
	 *            number sent by a client is larger than that, the server will
	 *            consider it to be real Unix time value rather than an offset
	 *            from current time.
	 * @param cas
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return OcsFuture<OcsResult> a future representing the processing of this
	 *         operation
	 * @throws OcsException
	 *             throw exception if ADD command fails.
	 */
	public OcsFuture<OcsResult> set(String key, Object value, int exper, long cas, OcsOptions options) throws OcsException {
		return store(MemcachedOpCode.SET, key, value, exper, cas, options);
	}
	public OcsFuture<OcsResult> set(String key, Object value, int exper, OcsOptions options) throws OcsException {
		return store(MemcachedOpCode.SET, key, value, exper, 0, options);
	}
	public OcsFuture<OcsResult> set(String key, Object value, OcsOptions options) throws OcsException {
		return store(MemcachedOpCode.SET, key, value, 0, 0, options);
	}
	public OcsFuture<OcsResult> set(String key, Object value) throws OcsException {
		return store(MemcachedOpCode.SET, key, value, 0, 0, OcsOptions.defaultOptions());
	}

	/**
	 * Replace the exist key value pair in the Ocs cluster asynchronously.
	 * Replace MUST fail if the item doesn't exist.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @param exper
	 *            Expiration time of the key value pairs. The actual value sent
	 *            may either be Unix time (number of seconds since January 1,
	 *            1970, as a 32-bit value), or a number of seconds starting from
	 *            current time. In the latter case, this number of seconds may
	 *            not exceed 60*60*24*30 (number of seconds in 30 days); if the
	 *            number sent by a client is larger than that, the server will
	 *            consider it to be real Unix time value rather than an offset
	 *            from current time.
	 * @param cas
	 * @param options
	 *            Ocs options, such as decoder, rpc's timeout.
	 * @return OcsFuture<OcsResult> a future representing the processing of this
	 *         operation
	 * @throws OcsException
	 *             throw exception if ADD command fails.
	 */
	public OcsFuture<OcsResult> replace(String key, Object value, int exper, long cas, OcsOptions options) throws OcsException {
		return store(MemcachedOpCode.REPLACE, key, value, exper, cas, options);
	}
	public OcsFuture<OcsResult> replace(String key, Object value, int exper, OcsOptions options) throws OcsException {
		return store(MemcachedOpCode.REPLACE, key, value, exper, 0, options);
	}
	public OcsFuture<OcsResult> replace(String key, Object value, OcsOptions options) throws OcsException {
		return store(MemcachedOpCode.REPLACE, key, value, 0, 0, options);
	}
	public OcsFuture<OcsResult> replace(String key, Object value) throws OcsException {
		return store(MemcachedOpCode.REPLACE, key, value, 0, 0, OcsOptions.defaultOptions());
	}

	private OcsResult syncStore(byte opcode, String key, Object value, int exper, long cas, OcsOptions options) {
		MemcachedSingleCommand command = new MemcachedStoreCommand(rpc, opcode, key, value, 0, exper, cas);
		return command.syncExecute(options);
	}

	/**
	 * Write the key value pair to the Ocs cluster synchronously. Set should
	 * store the data unconditionally if the key exists or not.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @param exper
	 *            experation time of the key value pairs. The actual value sent
	 *            may either be Unix time (number of seconds since January 1,
	 *            1970, as a 32-bit value), or a number of seconds starting from
	 *            current time. In the latter case, this number of seconds may
	 *            not exceed 60*60*24*30 (number of seconds in 30 days); if the
	 *            number sent by a client is larger than that, the server will
	 *            consider it to be real Unix time value rather than an offset
	 *            from current time.
	 * @param cas
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return OcsResult operation result, include the status
	 * @throws OcsException
	 *             throw exception if SET command fails.
	 */
	public OcsResult syncSet(String key, Object value, int exper, long cas, OcsOptions options) {
		return syncStore(MemcachedOpCode.SET, key, value, exper, cas, options);
	}
	public OcsResult syncSet(String key, Object value, int exper, OcsOptions options) {
		return syncStore(MemcachedOpCode.SET, key, value, exper, 0, options);
	}
	public OcsResult syncSet(String key, Object value, OcsOptions options) {
		return syncStore(MemcachedOpCode.SET, key, value, 0, 0, options);
	}
	public OcsResult syncSet(String key, Object value) {
		return syncStore(MemcachedOpCode.SET, key, value, 0, 0, OcsOptions.defaultOptions());
	}

	/**
	 * Add the key value pairs to the Ocs cluster synchronously. Add command
	 * MUST fail if the item already exist.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @param exper
	 *            experation time of the key value pairs. The actual value sent
	 *            may either be Unix time (number of seconds since January 1,
	 *            1970, as a 32-bit value), or a number of seconds starting from
	 *            current time. In the latter case, this number of seconds may
	 *            not exceed 60*60*24*30 (number of seconds in 30 days); if the
	 *            number sent by a client is larger than that, the server will
	 *            consider it to be real Unix time value rather than an offset
	 *            from current time.
	 * @param cas
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return OcsResult operation result, include the status
	 * @throws OcsException
	 *             throw exception if ADD command fails.
	 * 
	 */
	public OcsResult syncAdd(String key, Object value, int exper, long cas, OcsOptions options) {
		return syncStore(MemcachedOpCode.ADD, key, value, exper, cas, options);
	}
	public OcsResult syncAdd(String key, Object value, int exper, OcsOptions options) {
		return syncStore(MemcachedOpCode.ADD, key, value, exper, 0, options);
	}
	public OcsResult syncAdd(String key, Object value, OcsOptions options) {
		return syncStore(MemcachedOpCode.ADD, key, value, 0, 0, options);
	}
	public OcsResult syncAdd(String key, Object value) {
		return syncStore(MemcachedOpCode.ADD, key, value, 0, 0, null);
	}

	/**
	 * Replace the exist key value pair in the Ocs cluster synchronously.
	 * Replace MUST fail if the item doesn't exist.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @param exper
	 *            Expiration time of the key value pairs. The actual value sent
	 *            may either be Unix time (number of seconds since January 1,
	 *            1970, as a 32-bit value), or a number of seconds starting from
	 *            current time. In the latter case, this number of seconds may
	 *            not exceed 60*60*24*30 (number of seconds in 30 days); if the
	 *            number sent by a client is larger than that, the server will
	 *            consider it to be real Unix time value rather than an offset
	 *            from current time.
	 * @param cas
	 * @param options
	 *            Ocs options, such as decoder, rpc's timeout.
	 * @return OcsResult operation result, include the status
	 * @throws OcsException
	 *             throw exception if ADD command fails.
	 */
	public OcsResult syncReplace(String key, Object value, int exper, long cas, OcsOptions options) {
		return syncStore(MemcachedOpCode.REPLACE, key, value, exper, cas, options);
	}
	public OcsResult syncReplace(String key, Object value, int exper, OcsOptions options) {
		return syncStore(MemcachedOpCode.REPLACE, key, value, exper, 0, options);
	}
	public OcsResult syncReplace(String key, Object value, OcsOptions options) {
		return syncStore(MemcachedOpCode.REPLACE, key, value, 0, 0, options);
	}
	public OcsResult syncReplace(String key, Object value) {
		return syncStore(MemcachedOpCode.REPLACE, key, value, 0, 0, null);
	}

	private OcsFuture<Map<String, OcsResult>> stores(byte opcode, OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchStoreCommand(rpc, opcode, c);
		return command.executeBatch(true, options);
	}

	/**
	 * Write multiple key value pairs in one batch call. NOTE: Set command
	 * should store the data unconditionally if the key exists or not.
	 * 
	 * @param c
	 *            the key value pair collection
	 * @param options
	 *            ocs options, such as rpc's timeout.
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             if sets fails
	 */
	public OcsFuture<Map<String, OcsResult>> sets(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return stores(MemcachedOpCode.SETQ, c, options);
	}

	/**
	 * Asynchronously write multiple key value pairs in one batch call. NOTE:
	 * Add command MUST fail if the item already exist.
	 * 
	 * @param c
	 *            the key value pair collection
	 * @param options
	 *            ocs options, such as rpc's timeout.
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             if sets fails
	 */
	public OcsFuture<Map<String, OcsResult>> adds(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return stores(MemcachedOpCode.ADDQ, c, options);
	}

	/**
	 * Asynchronously write multiple key value pairs in one batch call. NOTE:
	 * Replace MUST fail if the item doesn't exist.
	 * 
	 * @param c
	 *            the key value pair collection
	 * @param options
	 *            Ocs options, such as rpc's timeout.
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             if sets fails
	 */
	public OcsFuture<Map<String, OcsResult>> replaces(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return stores(MemcachedOpCode.REPLACEQ, c, options);
	}

	private Map<String, OcsResult> syncStores(byte opcode, OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchStoreCommand(rpc, opcode, c);
		return command.syncExecuteBatch(true, options);
	}

	/**
	 * Synchronously write multiple key value pairs in one batch call. NOTE: Set
	 * command should store the data unconditionally if the key exists or not.
	 * 
	 * @param c
	 *            the key value pair collection
	 * @param listener
	 *            user defined callback
	 * @param options
	 *            ocs options, such as rpc's timeout.
	 * @return the Ocs result map.
	 * @throws OcsException
	 *             if sets fails
	 */
	public Map<String, OcsResult> syncSets(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return syncStores(MemcachedOpCode.SETQ, c, options);
	}

	/**
	 * Synchronously write multiple key value pairs in one batch call. NOTE: Add
	 * command MUST fail if the item already exist. After RPC finished, the user
	 * defined listener will be invoked.
	 * 
	 * @param c
	 *            the key value pair collection
	 * @param options
	 *            ocs options, such as rpc's timeout.
	 * @return the map of OcsResult
	 * @throws OcsException
	 *             if sets fails
	 */
	public Map<String, OcsResult> syncAdds(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return syncStores(MemcachedOpCode.ADDQ, c, options);
	}

	/**
	 * Synchronously replace multiple key value pairs in one batch call. NOTE:
	 * REPLACE command MUST fail if the item was not already exist. After RPC
	 * finished, the user defined listener will be invoked.
	 * 
	 * @param c
	 *            the key value pair collection
	 * @param options
	 *            ocs options, such as rpc's timeout.
	 * @return the map of the OcsResult.
	 * @throws OcsException
	 *             if sysncReplaces fails
	 */
	public Map<String, OcsResult> syncReplaces(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return syncStores(MemcachedOpCode.REPLACEQ, c, options);
	}

	/**
	 * Get the given key asynchronously.
	 * 
	 * @param key
	 *            the key to fetch
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return a future that will hold the return value of the fetch
	 * @throws OcsException
	 *             this get operation occurs any exception.
	 */
	public OcsFuture<OcsResult> get(String key, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedGetCommand(rpc, MemcachedOpCode.GET, key);
		return command.execute(options);
	}

	/**
	 * Get the given key synchronously。
	 * 
	 * @param key
	 *            the key to fetch
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return a OcsResult that will hold the return value of the fetch
	 * @throws OcsException
	 *             this get operation occurs any exception.
	 */
	public OcsResult syncGet(String key, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedGetCommand(rpc, MemcachedOpCode.GET, key);
		return command.syncExecute(options);
	}

	/**
	 * Read multiple key value pairs for specified keys in one batch call
	 * asynchronously. The returned values are in positional order with the
	 * original key array order. If a key is not found, the positional value in
	 * the OcsResult will be null.
	 * 
	 * @param keys
	 *            array of keys
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return a future that will hold the return value of the fetch
	 * @throws OcsException
	 *             if gets fails
	 */
	public OcsFuture<Map<String, OcsResult>> gets(Set<String> keys, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchGetCommand(rpc, MemcachedOpCode.GETQ, keys);
		return command.executeBatch(false, options);
	}

	/**
	 * Read multiple key value pairs for specified keys in one batch call
	 * synchronously. The returned values are in positional order with the
	 * original key array order. If a key is not found, the positional value in
	 * the OcsResult will be null.
	 * 
	 * @param keys
	 *            array of keys
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return a map of the OcsResult that will hold the return value of the
	 *         fetch
	 * @throws OcsException
	 *             if syncGets fails
	 */
	public Map<String, OcsResult> syncGets(Set<String> keys, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchGetCommand(rpc, MemcachedOpCode.GET, keys);
		return command.syncExecuteBatch(false, options);
	}

	/**
	 * Get a single key and reset its expiration asynchronously.
	 * 
	 * @param key
	 *            the key to get
	 * @param exper
	 *            the new expiration for the key
	 * @return a future that will hold the return value of the fetch
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsFuture<OcsResult> gat(String key, int exper, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedGATCommand(rpc, MemcachedOpCode.GAT, key, exper);
		return command.execute(options);
	}
	public OcsFuture<OcsResult> gat(String key, int exper) throws OcsException {
		MemcachedSingleCommand command = new MemcachedGATCommand(rpc, MemcachedOpCode.GAT, key, exper);
		return command.execute(null);
	}


	/**
	 * Get a single key and reset its expiration synchronously.
	 * 
	 * @param key
	 *            the key to get
	 * @param exper
	 *            the new expiration for the key
	 * @return a OcsReslt that will hold the return value of the fetch
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsResult syncGat(String key, int exper, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedGATCommand(rpc, MemcachedOpCode.GAT, key, exper);
		return command.syncExecute(options);
	}

	/**
	 * Read multiple key value pairs and modify the expiration time for
	 * specified keys with the expiration time in one batch call asynchronously.
	 * The returned values are in positional order with the original key array
	 * order. If a key is not found, the positional value in the OcsResult will
	 * be null.
	 * 
	 * @param keys
	 *            array of keys
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return a future that will hold the return value of the fetch
	 * @throws OcsException
	 *             if gets fails
	 */
	public OcsFuture<Map<String, OcsResult>> gats(Map<String, Integer> keyExpers, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchGATCommand(rpc, MemcachedOpCode.GATQ, keyExpers);
		return command.executeBatch(false, options);
	}

	/**
	 * Read multiple key value pairs and modify the expiration time for
	 * specified keys with the expiration time in one batch call synchronously.
	 * The returned values are in positional order with the original key array
	 * order. If a key is not found, the positional value in the OcsResult will
	 * be null.
	 * 
	 * @param keys
	 *            array of keys
	 * @param options
	 *            ocs options, such as decoder, rpc's timeout.
	 * @return a map of OcsResult that will hold the return value of the fetch
	 * @throws OcsException
	 *             if syncGats fails
	 */
	public Map<String, OcsResult> syncGats(Map<String, Integer> keyExpers, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchGATCommand(rpc, MemcachedOpCode.GATQ, keyExpers);
		return command.syncExecuteBatch(false, options);
	}
	/**
	 * Touch the given key to reset its expiration time asynchronously.
	 * 
	 * @param key
	 *            the key to fetch
	 * @param exp
	 *            the new expiration to set for the given key
	 * @return a future that will hold the return value of whether or not the
	 *         fetch succeeded
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsFuture<OcsResult> touch(String key, int exper, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedTouchCommand(rpc, MemcachedOpCode.TOUCH, key, exper);
		return command.execute(options);
	}



	/**
	 * Touch the given key to reset its expiration time synchronously.
	 * 
	 * @param key
	 *            the key to fetch
	 * @param exp
	 *            the new expiration to set for the given key
	 * @return a OcsResult that will hold the return value of whether or not the
	 *         fetch succeeded
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsResult syncTouch(String key, int exper, OcsOptions options) {
		MemcachedSingleCommand command = new MemcachedTouchCommand(rpc, MemcachedOpCode.TOUCH, key, exper);
		return command.syncExecute(options);
	}
	public OcsResult syncTouch(String key, int exper) {
		MemcachedSingleCommand command = new MemcachedTouchCommand(rpc, MemcachedOpCode.TOUCH, key, exper);
		return command.syncExecute(OcsOptions.defaultOptions());
	}

	/**
	 * Asynchronously flush the items in the Ocs cluster now or some time in the
	 * future as specified by the expiration field. See the documentation of the
	 * textual protocol for the full description on how to specify the
	 * expiration time.
	 * 
	 * @param exper
	 *            the period of time to expire, in seconds
	 * @return a future which hold the value whether or not the operation was
	 *         accepted
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsFuture<OcsResult> flush(int exper, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedFlushCommand(rpc, MemcachedOpCode.FLUSH, exper);
		return command.execute(options);
	}

	/**
	 * Synchronously flush the items in the Ocs cluster now or some time in the
	 * future as specified by the expiration field. See the documentation of the
	 * textual protocol for the full description on how to specify the
	 * expiration time.
	 * 
	 * @param exper
	 *            the period of time to expire, in seconds
	 * @return a OcsResult which hold the value whether or not the operation was
	 *         accepted
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsResult syncflush(int exper, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedFlushCommand(rpc, MemcachedOpCode.FLUSH, exper);
		return command.syncExecute(options);
	}


	/**
	 * Used as a keep alive asynchronously.
	 * 
	 * @param options
	 *            rpc's timeout
	 * @return a future which hold the value whether or not the operation was
	 *         accepted
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsFuture<OcsResult> noop(OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedNoopCommand(rpc, MemcachedOpCode.NOOP);
		return command.execute(options);
	}
	public OcsFuture<OcsResult> noop() throws OcsException {
		MemcachedSingleCommand command = new MemcachedNoopCommand(rpc, MemcachedOpCode.NOOP);
		return command.execute(null);
	}

	/**
	 * Used as a keep alive synchronously.
	 * 
	 * @param options
	 *            rpc's timeout
	 * @return a OcsResult which hold the value whether or not the operation was
	 *         accepted
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsResult syncNoop(OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedNoopCommand(rpc, MemcachedOpCode.NOOP);
		return command.syncExecute(options);
	}
	public OcsResult syncNoop() throws OcsException {
		return syncNoop(null);
	}

	/**
	 * Close the connection to the OCS cluster asynchronously.
	 * 
	 * @param options
	 *            rpc's timeout
	 * @return a future which hold the value whether or not the operation was
	 *         accepted
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsFuture<OcsResult> quit(OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedQuitCommand(rpc, MemcachedOpCode.QUIT);
		return command.execute(options);
	}
	public OcsFuture<OcsResult> quit() throws OcsException {
		MemcachedSingleCommand command = new MemcachedQuitCommand(rpc, MemcachedOpCode.QUIT);
		return command.execute(null);
	}

	/**
	 * Close the connection to the OCS cluster synchronously.
	 * 
	 * @param options
	 *            rpc's timeout
	 * @return a OcsResult which hold the value whether or not the operation was
	 *         accepted
	 * @throws OcsException
	 *             this operation occurs any exceptions.
	 */
	public OcsResult syncQuit(OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedQuitCommand(rpc, MemcachedOpCode.QUIT);
		return command.syncExecute(options);
	}
	public OcsResult syncQuit() throws OcsException {
		return syncQuit(null);
	}

	/**
	 * Delete the given key from the cluster asynchronously.
	 * 
	 * @param key
	 *            the key to delete
	 * @param options
	 *            ocs options, rpc's timeout.
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             this delete operation occurs any exception.
	 */
	public OcsFuture<OcsResult> delete(String key, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedDeleteCommand(rpc, MemcachedOpCode.DELETE, key);
		return command.execute(options);
	}
	public OcsFuture<OcsResult> delete(String key) throws OcsException {
		MemcachedSingleCommand command = new MemcachedDeleteCommand(rpc, MemcachedOpCode.DELETE, key);
		return command.execute(null);
	}

	/**
	 * Delete the given key from the cluster synchronously.
	 * 
	 * @param key
	 *            the key to delete
	 * @param options
	 *            ocs options, rpc's timeout.
	 * @return a OcsResult which hold the value whether or not the operation was
	 *         accepted
	 * @throws OcsException
	 *             this delete operation occurs any exception.
	 */
	public OcsResult syncDelete(String key, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedDeleteCommand(rpc, MemcachedOpCode.DELETE, key);
		return command.syncExecute(options);
	}
	public OcsResult syncDelete(String key) throws OcsException {
		return syncDelete(key, null);
	}

	/**
	 * Delete multiple key value pairs for specified keys in one batch call
	 * asynchronously. The returned values are in positional order with the
	 * original key array order.
	 * 
	 * @param keys
	 *            array of keys
	 * @param options
	 *            ocs options, rpc's timeout.
	 * @return a future that will hold the return value of the fetch
	 * @throws OcsException
	 *             if deletes fails
	 */
	public OcsFuture<Map<String, OcsResult>> deletes(Set<String> keys, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchDeleteCommand(rpc, MemcachedOpCode.DELETEQ, keys);
		return command.executeBatch(false, options);
	}

	/**
	 * Delete multiple key value pairs for specified keys in one batch call
	 * synchronously. The returned values are in positional order with the
	 * original key array order.
	 * 
	 * @param keys
	 *            array of keys
	 * @param options
	 *            ocs options, rpc's timeout.
	 * @return a map of OcsResult.
	 * @throws OcsException
	 *             if syncDeletes fails
	 */
	public Map<String, OcsResult> syncDeletes(Set<String> keys, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchDeleteCommand(rpc, MemcachedOpCode.DELETEQ, keys);
		return command.syncExecuteBatch(false, options);
	}

	/**
	 * Delete multiple key value pairs for specified keys in one batch call
	 * asynchronously. The returned values are in positional order with the
	 * original key array order. After RPC finished, user defined listener will
	 * be invoked.
	 * 
	 * @param keys
	 *            array of keys
	 * @param options
	 *            ocs options, rpc's timeout.
	 * @param listener
	 *            the user defined callback
	 * @throws OcsException
	 *             if deletes fails
	 */
	private OcsFuture<OcsResult> counter(byte opcode, String key, long amount, long init, int exper, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedCounterCommand(rpc, opcode, key, amount, init, exper);
		return command.execute(options);
	}


	private OcsResult syncCounter(byte opcode, String key, long amount, long init, int exper, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedCounterCommand(rpc, opcode, key, amount, init, exper);
		return command.syncExecute(options);
	}

	/**
	 * Increment the given counter asynchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value.
	 * 
	 * @param key
	 *            the key
	 * @param amount
	 *            the amount to decrement
	 * @param init
	 *            the default value (if the counter does not exist)
	 * @param exper
	 *            the expiration of this object
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             this delete operation occurs any exception.
	 */
	public OcsFuture<OcsResult> incr(String key, long amount, long init, int exper, OcsOptions options) throws OcsException {
		return counter(MemcachedOpCode.INCREMENT, key, amount, init, exper, options);
	}

	/**
	 * Increment the given counter synchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value.
	 * 
	 * @param key
	 *            the key
	 * @param amount
	 *            the amount to decrement
	 * @param init
	 *            the default value (if the counter does not exist)
	 * @param exper
	 *            the expiration of this object
	 * @return a OcsResult representing the processing of this operation
	 * @throws OcsException
	 *             this delete operation occurs any exception.
	 */
	public OcsResult syncIncr(String key, long amount, long init, int exper, OcsOptions options) throws OcsException {
		return syncCounter(MemcachedOpCode.INCREMENT, key, amount, init, exper, options);
	}
	public OcsResult syncIncr(String key, long amount, long init, int exper) throws OcsException {
		return syncIncr(key, amount, init, exper, null);
	}


	/**
	 * Decrement the given counter asynchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value.
	 * 
	 * @param key
	 *            the key
	 * @param amount
	 *            the amount to decrement
	 * @param init
	 *            the default value (if the counter does not exist)
	 * @param exper
	 *            the expiration of this object
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             this delete operation occurs any exception.
	 */
	public OcsFuture<OcsResult> decr(String key, long amount, long init, int exper, OcsOptions options) throws OcsException {
		return counter(MemcachedOpCode.INCREMENT, key, amount, init, exper, options);
	}
	public OcsFuture<OcsResult> decr(String key, long amount, long init, int exper) throws OcsException {
		return counter(MemcachedOpCode.INCREMENT, key, amount, init, exper, null);
	}

	/**
	 * Decrement the given counter synchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value.
	 * 
	 * @param key
	 *            the key
	 * @param amount
	 *            the amount to decrement
	 * @param init
	 *            the default value (if the counter does not exist)
	 * @param exper
	 *            the expiration of this object
	 * @return a OcsResult representing the processing of this operation
	 * @throws OcsException
	 *             this delete operation occurs any exception.
	 */
	public OcsResult syncDecr(String key, long amount, long init, int exper, OcsOptions options) throws OcsException {
		return syncCounter(MemcachedOpCode.DECREMENT, key, amount, init, exper, options);
	}
	public OcsResult syncDecr(String key, long amount, long init, int exper) throws OcsException {
		return syncCounter(MemcachedOpCode.DECREMENT, key, amount, init, exper, null);
	}

	/**
	 * Decrement the given counter asynchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value. After RPC finished, the user defined listener will be
	 * invoked.
	 * 
	 * @param key
	 *            the key
	 * @param amount
	 *            the amount to decrement
	 * @param init
	 *            the default value (if the counter does not exist)
	 * @param exper
	 *            the expiration of this object
	 * @throws OcsException
	 *             this delete operation occurs any exception.
	 */
	
	private OcsFuture<Map<String, OcsResult>> counters(byte opcode, OcsKeyCounterCollection c, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchCounterCommand(rpc, opcode, c);
		return command.executeBatch(false, options);
	}

	private Map<String, OcsResult> syncCounters(byte opcode, OcsKeyCounterCollection c, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchCounterCommand(rpc, opcode, c);
		return command.syncExecuteBatch(false, options);
	}

	/**
	 * Increment multiple key counter pairs for specified keys in one batch call
	 * asynchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value.
	 * 
	 * @param OcsKeyCounterCollection
	 *            .key the key
	 * @param OcsKeyCounterCollection
	 *            .amount the amount to decrement
	 * @param OcsKeyCounterCollection
	 *            .init the default value (if the counter does not exist)
	 * @param OcsKeyCounterCollection
	 *            .exper the expiration of this object
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             this incrs operation occurs any exception.
	 */
	public OcsFuture<Map<String, OcsResult>> incrs(OcsKeyCounterCollection c, OcsOptions options) throws OcsException {
		return counters(MemcachedOpCode.INCREMENTQ, c, options);
	}

	/**
	 * Increment multiple key counter pairs for specified keys in one batch call
	 * synchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value.
	 * 
	 * @param c
	 *            the key counter pairs collection
	 * @return a map of the OcsResult representing the processing of this
	 *         operation
	 * @throws OcsException
	 *             this incrs operation occurs any exception.
	 */
	public Map<String, OcsResult> syncIncrs(OcsKeyCounterCollection c, OcsOptions options) throws OcsException {
		return syncCounters(MemcachedOpCode.INCREMENTQ, c, options);
	}

	/**
	 * Decrement multiple key counter pairs for specified keys in one batch call
	 * asynchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value.
	 * 
	 * @param c
	 *            key counters collection
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             this decrs operation occurs any exception.
	 */
	public OcsFuture<Map<String, OcsResult>> decrs(OcsKeyCounterCollection c, OcsOptions options) throws OcsException {
		return counters(MemcachedOpCode.DECREMENTQ, c, options);
	}

	/**
	 * Decrement multiple key counter pairs for specified keys in one batch call
	 * synchronously.
	 * 
	 * Due to the way the Ocs cluster operates on items, incremented and
	 * decremented items will be returned as Strings with any operations that
	 * return a value.
	 * 
	 * @param c
	 *            the key counter pairs collection
	 * @return a map of the OcsResult representing the processing of this
	 *         operation
	 * @throws OcsException
	 *             this syncDecrs operation occurs any exception.
	 */
	public Map<String, OcsResult> syncDecrs(OcsKeyCounterCollection c, OcsOptions options) throws OcsException {
		return syncCounters(MemcachedOpCode.DECREMENTQ, c, options);
	}

	private OcsFuture<OcsResult> combine(byte opcode, String key, Object value, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedCombineCommand(rpc, opcode, key, value);
		return command.execute(options);
	}

	private OcsResult syncCombine(byte opcode, String key, Object value, OcsOptions options) throws OcsException {
		MemcachedSingleCommand command = new MemcachedCombineCommand(rpc, opcode, key, value);
		return command.syncExecute(options);
	}


	/**
	 * Append value to existing key value pair. The options specifies the RPC
	 * timeout, asynchronously.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @return a future which hold the value whether or not the operation was
	 *         accepted
	 * 
	 * @throws OcsException
	 *             if append fails
	 */
	public OcsFuture<OcsResult> append(String key, Object value, OcsOptions options) throws OcsException {
		return combine(MemcachedOpCode.APPEND, key, value, options);
	}

	/**
	 * Append value to existing key value pair. The options specifies the RPC
	 * timeout, synchronously.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @return a OcsResult which hold the value whether or not the operation was
	 *         accepted
	 * 
	 * @throws OcsException
	 *             if append fails
	 */
	public OcsResult syncAppend(String key, Object value, OcsOptions options) throws OcsException {
		return syncCombine(MemcachedOpCode.APPEND, key, value, options);
	}

	/**
	 * Prepend value to existing key value pair. The options specifies the RPC
	 * timeout, asynchronously.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @return a future which hold the value whether or not the operation was
	 *         accepted
	 * 
	 * @throws OcsException
	 *             if append fails
	 */
	public OcsFuture<OcsResult> prepend(String key, Object value, OcsOptions options) throws OcsException {
		return combine(MemcachedOpCode.PREPEND, key, value, options);
	}

	/**
	 * Prepend value to existing key value pair. The options specifies the RPC
	 * timeout, synchronously.
	 * 
	 * @param key
	 *            Ocs key
	 * @param value
	 *            Ocs value
	 * @return a OcsResult which hold the value whether or not the operation was
	 *         accepted
	 * 
	 * @throws OcsException
	 *             if prepend fails
	 */
	public OcsResult syncPrepend(String key, Object value, OcsOptions options) throws OcsException {
		return syncCombine(MemcachedOpCode.PREPEND, key, value, options);
	}

	private OcsFuture<Map<String, OcsResult>> combines(byte opcode, OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchCombineCommand(rpc, opcode, c);
		return command.executeBatch(true, options);
	}

	private Map<String, OcsResult> syncCombines(byte opcode, OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		MemcachedBatchCommand command = new MemcachedBatchCombineCommand(rpc, opcode, c);
		return command.syncExecuteBatch(true, options);
	}

	/**
	 * Append multiple key value pairs in one batch call asynchronously.
	 * 
	 * 
	 * @param c
	 *            the key value collection.
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             this appends operation occurs any exception.
	 */
	public OcsFuture<Map<String, OcsResult>> appends(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return combines(MemcachedOpCode.APPENDQ, c, options);
	}

	/**
	 * Append multiple key value pairs in one batch call synchronously.
	 * 
	 * 
	 * @param c
	 *            the key value collection.
	 * @return a result map representing the processing of this operation
	 * @throws OcsException
	 *             this appends operation occurs any exception.
	 */
	public Map<String, OcsResult> syncAppends(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return syncCombines(MemcachedOpCode.APPENDQ, c, options);
	}

	/**
	 * Prepend multiple key value pairs in one batch call asynchronously.
	 * 
	 * @param c
	 *            the key value collection.
	 * @return a future representing the processing of this operation
	 * @throws OcsException
	 *             this appends operation occurs any exception.
	 */
	public OcsFuture<Map<String, OcsResult>> prepends(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return combines(MemcachedOpCode.PREPENDQ, c, options);
	}

	/**
	 * Prepend multiple key value pairs in one batch call synchronously.
	 * 
	 * 
	 * @param c
	 *            the key value collection.
	 * @return a map of OcsResult representing the processing of this operation
	 * @throws OcsException
	 *             this appends operation occurs any exception.
	 */
	public Map<String, OcsResult> syncPrepends(OcsKeyValueCollection c, OcsOptions options) throws OcsException {
		return syncCombines(MemcachedOpCode.PREPENDQ, c, options);
	}
}