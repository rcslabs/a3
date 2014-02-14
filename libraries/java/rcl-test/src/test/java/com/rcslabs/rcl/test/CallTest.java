package com.rcslabs.rcl.test;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IConnectionListener;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;
import com.rcslabs.rcl.telephony.entity.*;
import com.rcslabs.rcl.telephony.event.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;

public class CallTest extends TestBase
        implements IConnectionListener, ICallListener, ITelephonyServiceListener, AsyncTest {

	protected IConnection connA;
	protected IConnection connB;
    protected ICall outgoingCall;

    enum State{
        INIT, CONNECTING, SEMICONNECTED, CONNECTED, CALLING, TALKING, GOING_TO_HANGUP, FINISHED, TEST_PASSED, TEST_FAILED
    };

    protected State state;

    protected final int LOOP_INTERVAL = 20;
    protected final int MAX_TIME_WITHOUT_STATE_CHANGED = 20000;
    protected final int TALKING_TIME = 3333;

    protected int countdown;

	@Override
	public void setUp() throws Exception {
		super.setUp();
        LogbackErrorAppender.setInstance(this);
        setState(State.INIT);
	}

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        LogbackErrorAppender.setInstance(null);
    }

    public void setAsFailed(){
        setState(State.TEST_FAILED);
    }

    protected void setState(State state){
        this.state = state;
        countdown = 0;
    }

	@Test
	public void test() throws Exception
    {
        while(!(state == State.TEST_FAILED || state == State.TEST_PASSED))
        {
            switch(state)
            {
                case INIT:
                    connA = rclFactory.newConnection();
                    connB = rclFactory.newConnection();
                    connA.addListener(this);
                    connB.addListener(this);
                    setState(State.CONNECTING);
                    connA.open(TestUsers.ALICE.cnParams);
                    connB.open(TestUsers.BOB.cnParams);
                    break;

                case CONNECTED:
                    connB.getService(ITelephonyService.class).addListener(this);
                    outgoingCall = rclFactory.findConnection(connA.getId()).getService(ITelephonyService.class).newCall();
                    ICallParams params = new CallParams();
                    params.setFrom("Alice", "sip:1002@192.168.1.200");
                    params.setTo("\"Bob\" <sip:1003@192.168.1.200>"); //TestUsers.BOB.cnParams.getPhoneNumber());
                    params.addParameter(new CallParameterSipTo("key1", "value1"));
                    params.addParameter(new CallParameterSipTo("key2", "value2"));
                    params.addParameter(new CallParameterSipHeader("X-Header1", "value3"));
                    params.addParameter(new CallParameterSipHeader("X-Header4", "value4"));

                    ISdpObject sdp = new SdpObject();
                    sdp.setOfferer(
                    "v=0\n" +
                    "o=- 0 0 IN IP4 192.168.1.40\n" +
                    "s=Session SIP/SDP\n" +
                    "c=IN IP4 192.168.1.40\n" +
                    "t=0 0\n" +
                    "m=audio 55555 RTP/AVP 8 101\n" +
                    "a=rtpmap:8 PCMA/8000/1\n" +
                    "a=rtpmap:101 telephone-event/8000/1\n" +
                    "a=fmtp:101 0-15\n" +
                    "a=ptime:20\n" +
                    "a=silenceSupp:off - - - -\n" );
                    params.setSdpObject(sdp);
                    outgoingCall.addListener(this);

                    setState(State.CALLING);
                    outgoingCall.start(params);
                    break;

                case GOING_TO_HANGUP:
                    outgoingCall.finish();
                    break;

                case FINISHED:
                    setState(State.TEST_PASSED);
                    break;
            }

            Thread.sleep(LOOP_INTERVAL);

            if((countdown+=LOOP_INTERVAL) > MAX_TIME_WITHOUT_STATE_CHANGED){
                setAsFailed(); // Too long staying in one state
            }
        }

        Assert.assertTrue(state == State.TEST_PASSED);
	}

    @Override
    public void onCallStarting(ICallStartingEvent event) {
        if(state != State.CALLING){
            setState(State.TEST_FAILED);
        }
    }

    @Override
    public void onCallStarted(ICallEvent event) {
        setState(State.TALKING);
        ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
        timeoutExecutor.schedule(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                setState(State.GOING_TO_HANGUP);
                return null;
            }
        }, TALKING_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onCallFinished(ICallEvent event) {
        setState(State.FINISHED);
    }

    @Override
    public void onCallFailed(ICallFailedEvent event) {
        setState(State.TEST_FAILED);
    }

    @Override
    public void onCallTransfered(ICallTransferEvent event) { }

    @Override
    public void onTransferFailed(ICallTransferEvent event) {}

    @Override
    public void onCallError(ICallEvent event) {
        setState(State.TEST_FAILED);
    }

    @Override
    public void onCallFinishNotification(ICallFinishNotificationEvent event) {}

    @Override
    public void onConnecting(IConnectionEvent event) { }

    @Override
    public void onConnected(IConnectionEvent event) {
        if(state == State.CONNECTING){
            setState(State.SEMICONNECTED);
        } else if (state == State.SEMICONNECTED) {
            setState(State.CONNECTED);
        } else {
            setState(State.TEST_FAILED);
        }
    }

    @Override
    public void onConnectionBroken(IConnectionEvent event) { setAsFailed(); }

    @Override
    public void onConnectionFailed(IConnectionEvent event) { setAsFailed(); }

    @Override
    public void onConnectionError(IConnectionEvent event) { setAsFailed(); }

    @Override
    public void onIncomingCall(ICall call, ITelephonyEvent event) {
        call.accept(CallType.AUDIO,
        "v=0\n" +
        "o=- 0 0 IN IP4 192.168.1.40\n" +
        "s=Session SIP/SDP\n" +
        "c=IN IP4 192.168.1.40\n" +
        "t=0 0\n" +
        "m=audio 55555 RTP/AVP 8 101\n" +
        "a=rtpmap:8 PCMA/8000/1\n" +
        "a=rtpmap:101 telephone-event/8000/1\n" +
        "a=fmtp:101 0-15\n" +
        "a=ptime:20\n" +
        "a=silenceSupp:off - - - -\n");
    }

    @Override
    public void onCancel(ICall call, ITelephonyEvent event) {}
}
