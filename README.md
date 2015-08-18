#### OCS  Java SDK Package

OCS Java SDK is an open source compatible MEMCACHED protocol that used to  access ALIYUN's OCS and other compatible Memached service. 
This pacakge contains a single-threadded,  and thread-safe ALIYUN's OCS client, which supports both asynchronous and synchronous style API,
and contains full source code.

Please use the latest version 0.0.1.
#### Prerequisites:

Java 1.6 or greater.
Maven 2.0 or greater.
Netty  3.6.3.Final.
The source code can be imported into any Java IDE. 
Please use Maven to build this package.

#### Build instructions:
mvn assembly:assembly


#### Examples


```
import com.aliyun.ocs.OcsClient;
import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsOptions;
import com.aliyun.ocs.utils.*;


void testOcs() {
	String domain = " aliyunocs.com";
	String username = "your ocs instance id";
	String password = "password";

	OcsClient ocs = new OcsClient(domain, username, password);
	OcsOptions options = OcsOptions.defaultOptions();

	OcsFuture<OcsResult> setFuture = ocs.set("key", "value", 0, 0, options);
	OcsFuture<OcsResult> getFuture = ocs.get("key", options);

	System.out.println(setFuture.get());
	System.out.println(getFuture.get())
}
```