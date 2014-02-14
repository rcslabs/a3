package com.rcslabs.rcl.test;


import com.rcslabs.rcl.JainSipGlobalParams;
import com.rcslabs.rcl.JainSipRclFactory;
import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IRclFactory;
import com.rcslabs.rcl.core.entity.ConnectionParams;
import com.rcslabs.rcl.core.entity.IConnectionParams;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.telephony.entity.CallParams;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.util.IpAddressUtils;
import gov.nist.core.CommonLogger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

public class TestBase {

	public enum TestUsers {
        ALICE("1002", "Alice", "1234", 50002, 50000),
		BOB("1003",   "Bob",   "1234", 50004, 50006);

		public IConnectionParams cnParams;
		public ICallParams callParams;
		private final int localVideoPort;
		private final int localAudioPort;
		
		private TestUsers(
				String phone, 
				String name, 
				String password, 
				int localVideoPort, 
				int localAudioPort) 
		{
			this.localVideoPort = localVideoPort;
			this.localAudioPort = localAudioPort;
			
			ConnectionParams paramsLocal = new ConnectionParams();
			paramsLocal.setPhoneNumber(phone);
			paramsLocal.setUserName(name);
			paramsLocal.setPassword(password);
			paramsLocal.setPresenceEnabled(false);
			this.cnParams = paramsLocal;
			
			CallParams callParamsLocal = new CallParams();
			callParamsLocal.setFrom(phone);
			this.callParams = callParamsLocal;
		}

		public int getLocalVideoPort() {
			return localVideoPort;
		}

		public int getLocalAudioPort() {
			return localAudioPort;
		}
	}

    protected JainSipGlobalParams jainSipGlobalParams;
	protected IRclFactory rclFactory;

    public TestBase() {
        jainSipGlobalParams = new JainSipGlobalParams();

        jainSipGlobalParams.setLocalIpAddress(
                null != System.getProperty("sip.local.address")
                ? System.getProperty("sip.local.address")
                : IpAddressUtils.getLocalIpAddress()
        );

        jainSipGlobalParams.setLocalPort(5062);

        jainSipGlobalParams.setSipServerAddress(
                null != System.getProperty("sip.server.address")
                ? System.getProperty("sip.server.address")
                : "freeswitch.local"
        );

        if (System.getProperty("sip.proxy.address") != null) {
            jainSipGlobalParams.setSipProxyAddress(System.getProperty("sip.proxy.address"));
        }

        if (System.getProperty("sip.user.agent") != null) {
            jainSipGlobalParams.setSipUserAgent(System.getProperty("sip.user.agent"));
        }

        jainSipGlobalParams.setAutomaticDispose(false);
	}

	@BeforeClass
	public static void beforeClass() {
		CommonLogger.useLegacyLogger = true;
		//CommonLogger.legacyLogger = new JainSipLogger();
	}
	
	@Before
	public void setUp() throws Exception {
		rclFactory = new JainSipRclFactory(jainSipGlobalParams);
	}

    @After
    public void tearDown() throws Exception {
        rclFactory.dispose();
    }

}
