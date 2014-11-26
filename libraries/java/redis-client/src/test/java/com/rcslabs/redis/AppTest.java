package com.rcslabs.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.SafeEncoder;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@Ignore
public class AppTest
{
    private JedisPool pool;

    private RedisConnector connector;

    @Before
    public void setUp() throws Exception {
        URI uri = new URI("redis://192.168.1.200");
        // http://commons.apache.org/proper/commons-pool/api-1.6/index.html?org/apache/commons/pool/impl/GenericObjectPool.html
        GenericObjectPoolConfig c = new GenericObjectPoolConfig();
        c.setMinIdle(1);
        c.setMaxIdle(2);
        c.setMaxTotal(101);

        pool = new JedisPool(c, uri.getHost(), (-1 == uri.getPort() ? Protocol.DEFAULT_PORT : uri.getPort()));
        connector = new RedisConnector(pool);

    }

    @After
    public void tearDown() throws Exception {
        connector.dispose();
        connector = null;
        pool.destroy();
        pool = null;
    }

    @Test
    public void testConnect() throws Exception {
        connector.subscribe();
        Thread.sleep(1111);
    }

    @Test
    public void testGetAndReturnResource() throws Exception {
        Jedis j1 = connector.getResource();
        j1.set("test", "test");
        assertTrue(j1 instanceof Jedis);
        connector.returnResource(j1);

        BinaryJedis j2 = connector.getResource();
        j2.set(SafeEncoder.encode("test"), SafeEncoder.encode("test"));
        assertTrue(j2 instanceof BinaryJedis);
        connector.returnResource((Jedis)j2);
    }

    @Test
    public void testAddSerializer() throws Exception {
        IMessageSerializer ser1 = null;
        IMessageSerializer ser2 = new StringMessageSerializer();
        IMessageSerializer ser3 = new StringMessageSerializer();

        try {
            ser1 = connector.getSerializer();
            fail();
        }catch(MessagingException e){
            assertTrue(e.getErrorCode() == MessagingException.SERIALIZER_ABSENT);
        }

        assertNull(ser1);

        try {
            ser1 = connector.getSerializer("test");
            fail();
        }catch(MessagingException e){
            assertTrue(e.getErrorCode() == MessagingException.SERIALIZER_ABSENT);
        }

        assertNull(ser1);

        connector.addSerializer(ser2);
        assertSame(connector.getSerializer(), ser2);
        assertSame(connector.getSerializer("test"), ser2);

        connector.addSerializer(ser3, "test");
        assertSame(connector.getSerializer(), ser2);
        assertSame(connector.getSerializer("test"), ser3);
        assertNotSame(connector.getSerializer(), connector.getSerializer("test"));

        try {
            connector.addSerializer(new StringMessageSerializer());
            fail();
        }catch(MessagingException e){
            assertTrue(e.getErrorCode() == MessagingException.SERIALIZER_EXIST);
        }

        try {
            connector.addSerializer(new StringMessageSerializer(), "test");
            fail();
        }catch(MessagingException e){
            assertTrue(e.getErrorCode() == MessagingException.SERIALIZER_EXIST);
        }
    }

    @Test
    public void testAddListenerBeforeConnect() throws Exception
    {
        int i = 100;
        while(i-- > 0){
            connector.addMessageListener("test" + i, new CounterMessageListener());
            connector.addMessageListener("test" + i, new CounterMessageListener());
            connector.addMessageListener("test" + i, new CounterMessageListener());
        }

        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(2222);

        // expected 3 listeners on every of 100 channels
        Map<String, Channel> cc = getChannels();
        assertEquals(100, cc.size());
        for(Channel c : cc.values()){
            assertEquals(3, getListeners(c).size());
        }
    }

    @Test
    public void testAddListenerAfterConnect() throws Exception
    {
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(1111);
        int i = 100;
        while(i-- > 0){
            connector.addMessageListener("test" + i, new CounterMessageListener());
            connector.addMessageListener("test" + i, new CounterMessageListener());
            connector.addMessageListener("test" + i, new CounterMessageListener());
        }
        Thread.sleep(2222);

        // expected 3 listeners on every of 100 channels
        Map<String, Channel> cc = getChannels();
        assertEquals(100, cc.size());
        for(Channel c : cc.values()){
            assertEquals(3, getListeners(c).size());
        }
    }

    @Test
    public void testRemoveMessageListener1() throws Exception
    {
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(1111);
        IMessageListener sml = new CounterMessageListener();
        connector.addMessageListener("test", sml);
        connector.addMessageListener("test", new CounterMessageListener());
        connector.addMessageListener("test", new CounterMessageListener());
        Thread.sleep(1111);
        connector.removeMessageListener("test", sml);
        Thread.sleep(1111);
        // 2 listeners expected
        assertEquals(1, getChannels().size());
        assertEquals(2, getListeners( getChannels().get("test") ).size() );
    }

    @Test
    public void testRemoveMessageListener2() throws Exception
    {
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(1111);
        IMessageListener sml1 = new CounterMessageListener();
        IMessageListener sml2 = new CounterMessageListener();
        connector.addMessageListener("test", sml1);
        connector.addMessageListener("test", sml2);
        connector.addMessageListener("test", new CounterMessageListener());
        Thread.sleep(1111);
        connector.removeMessageListener("test", sml1);
        connector.removeMessageListener("test", sml2);
        Thread.sleep(1111);
        // 1 listener expected
        assertEquals(1, getChannels().size());
        assertEquals(1, getListeners( getChannels().get("test") ).size() );
    }

    @Test
    public void testRemoveMessageListener3() throws Exception
    {
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(1111);
        IMessageListener sml1 = new CounterMessageListener();
        IMessageListener sml2 = new CounterMessageListener();
        IMessageListener sml3 = new CounterMessageListener();
        connector.addMessageListener("test", sml1);
        connector.addMessageListener("test", sml2);
        connector.addMessageListener("test", sml3);
        Thread.sleep(1111);
        connector.removeMessageListener("test", sml1);
        connector.removeMessageListener("test", sml2);
        connector.removeMessageListener("test", sml3);
        Thread.sleep(1111);
        // Zero listeners expected on every channel
        // channels should unsubscribe
        assertEquals(0, getChannels().size());
    }

    @Test
    public void testRemoveAllListeners() throws Exception {
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(1111);
        connector.addMessageListener("test", new CounterMessageListener());
        connector.addMessageListener("test", new CounterMessageListener());
        connector.addMessageListener("test", new CounterMessageListener());
        Thread.sleep(1111);
        connector.removeAllListeners("test");
        Thread.sleep(1111);
        // Zero listeners expected on every channel
        // channels should unsubscribe
        assertEquals(0, getChannels().size());
    }

    @Test
    public void testPublish100MessagesFor100Channels() throws Exception {
        CounterMessageListener sml = new CounterMessageListener();
        int i = 100;
        while(i-- > 0){
            connector.addMessageListener("test" + i, sml);
        }
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(2222);

        i = 100;
        while(i-- > 0){
            runMessagePublisherThread("test" + i, 100);
        }
        Thread.sleep(3333);

        // count all received messages. Expected 10K
        assertEquals(10000, sml.getMessagesNum());
    }

    @Test
    public void testResubscribeOnConnectionFailed() throws Exception {
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(2222);
        CounterMessageListener sml = new CounterMessageListener();
        connector.addMessageListener("test", sml);

        int countdown = 10;
        System.out.println("You should manually RESTART the Redis server.");
        while(countdown > 0){
            System.out.println("Waiting " + countdown + " sec");
            Thread.sleep(1000);
            countdown--;
        }

        runMessagePublisherThread("test", 100);
        Thread.sleep(3333);

        // count all received messages. Expected 100
        assertEquals(100, sml.getMessagesNum() );
    }

    @Test
    public void testSubscribeOnDisconnectedRedis() throws Exception {
        int countdown = 10;
        System.out.println("You should manually STOP the Redis server.");
        while(countdown > 0){
            System.out.println("Waiting " + countdown + " sec");
            Thread.sleep(1000);
            countdown--;
        }

        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        CounterMessageListener sml = new CounterMessageListener();
        connector.addMessageListener("test", sml);

        countdown = 10;
        System.out.println("You should manually START the Redis server.");
        while(countdown > 0){
            System.out.println("Waiting " + countdown + " sec");
            Thread.sleep(1000);
            countdown--;
        }

        runMessagePublisherThread("test", 100);
        Thread.sleep(3333);

        // count all received messages. Expected 100
        assertEquals(100, sml.getMessagesNum() );
    }

    @Test
    public void testBlockingListeners() throws Exception {
        AtomicInteger cnt = new AtomicInteger(0);
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(2222);

        runBlockingListenerThread(7, cnt, "test0", false);
        runBlockingListenerThread(17, cnt, "test1", false);
        runBlockingListenerThread(37, cnt, "test2", false);
        runBlockingListenerThread(111, cnt, "test3", false);
        Thread.sleep(2222);

        int totalMessages = 512;

        runMessagePublisherThread("test0", totalMessages/4);
        runMessagePublisherThread("test1", totalMessages/4);
        runMessagePublisherThread("test2", totalMessages/4);
        runMessagePublisherThread("test3", totalMessages/4);
        Thread.sleep(2222);

        int t = 0;
        while(true){
            //System.out.println(cnt.get());
            Thread.sleep(100);
            t += 100;
            if(t > 120000){ break; } // 2 min - too long to wait ...
            if(cnt.get() == totalMessages){ break; } // all messages received
        }

        assertEquals(totalMessages, cnt.get());
    }

    @Test
    public void testBlockingSyncronizedListeners() throws Exception {
        AtomicInteger cnt = new AtomicInteger(0);
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(2222);

        runBlockingListenerThread(7, cnt, "test0", true);
        runBlockingListenerThread(17, cnt, "test1", true);
        runBlockingListenerThread(37, cnt, "test2", true);
        runBlockingListenerThread(111, cnt, "test3", true);
        Thread.sleep(2222);

        int totalMessages = 512;

        runMessagePublisherThread("test0", totalMessages / 4);
        runMessagePublisherThread("test1", totalMessages / 4);
        runMessagePublisherThread("test2", totalMessages / 4);
        runMessagePublisherThread("test3", totalMessages / 4);
        Thread.sleep(2222);

        int t = 0;
        while (true) {
            //System.out.println(cnt.get());
            Thread.sleep(100);
            t += 100;
            if (t > 120000) {
                break;
            } // 2 min - too long to wait ...
            if (cnt.get() == totalMessages) {
                break;
            } // all messages received
        }

        assertEquals(totalMessages, cnt.get());
    }

    @Test
    public void testRemoveSlowListenerWhenExecute() throws Exception {
        connector.addSerializer(new StringMessageSerializer());
        connector.subscribe();
        Thread.sleep(2222);
        AtomicInteger ai = new AtomicInteger(0);
        BlockingMessageListener ml = new BlockingMessageListener(2222, ai);
        connector.addMessageListener("test", ml);
        runMessagePublisherThread("test", 1);
        Thread.sleep(1111);
        connector.removeAllListeners("test");
        // channel listeners is empty, but single listener still executes
        // no way to unsubscribe and remove channel
        assertNotNull( getChannels().get("test") );
        assertEquals(0, getListeners( getChannels().get("test") ).size() );
        assertEquals(0, ai.get());
        Thread.sleep(2222);
        // work complete. listener removed.
        assertEquals(1, ai.get());
        // publish another message - expected no any work here
        runMessagePublisherThread("test", 1);
        Thread.sleep(3333);
        assertEquals(1, ai.get());
    }

    private void runBlockingListenerThread(final int delay, final AtomicInteger cnt, final String channel, final boolean sync)
            throws Exception {
        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                BlockingMessageListener ml = (sync ? new BlockingSyncronizedMessageListener(delay, cnt) : new BlockingMessageListener(delay, cnt));
                RedisConnector subscriber = new RedisConnector(pool);
                try {
                    subscriber.addSerializer(new StringMessageSerializer());
                    subscriber.addMessageListener(channel, ml);
                    subscriber.subscribe();
                } catch (MessagingException e) {}
            }
        });
    }

    private void runMessagePublisherThread(final String channel, final int messagesCnt) throws Exception
    {
        final AtomicInteger cnt = new AtomicInteger(messagesCnt);

        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                RedisConnector publisher = new RedisConnector(pool);
                try {
                    publisher.addSerializer(new StringMessageSerializer());
                } catch (MessagingException e) {}
                while (cnt.getAndDecrement() > 0){
                    publisher.publish(channel, new TestMessage());
                }
                publisher.dispose();
            }
        });
    }

    private <T> T getObjectField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name); //NoSuchFieldException
        f.setAccessible(true);
        return (T) f.get(obj); //IllegalAccessException
    }

    private Map<String, Channel> getChannels() throws Exception {
        RedisSubscriber subscriber = getObjectField(connector, "subscriber");
        return getObjectField(subscriber, "channels");
    }

    private List<IMessageListener> getListeners(Channel channel) throws Exception {
        return getObjectField(channel, "listeners");
    }
}
