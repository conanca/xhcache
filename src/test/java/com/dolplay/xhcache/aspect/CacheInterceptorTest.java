package com.dolplay.xhcache.aspect;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dolplay.xhcache.CacheConfig;
import com.dolplay.xhcache.assets.CacheKeyPrefix;
import com.dolplay.xhcache.assets.domain.User;
import com.dolplay.xhcache.assets.lang.Pager;
import com.dolplay.xhcache.assets.service.UserService;

public class CacheInterceptorTest {
	private static Logger logger = LoggerFactory.getLogger(CacheInterceptorTest.class);
	private static ShardedJedisPool pool;
	private static ShardedJedis jedis;
	private static UserService userService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] { "xh-commons-cache.xml",
				"xh-commons-cache-test-assets.xml" });

		// 初始化redis数据及连接
		logger.info("初始化redis数据及连接...");
		pool = appContext.getBean("jedisPool", ShardedJedisPool.class);
		jedis = pool.getResource();
		Collection<Jedis> jedisColl = jedis.getAllShards();
		for (Jedis jedis : jedisColl) {
			jedis.flushAll();
		}

		// 初始化UserService
		userService = appContext.getBean(UserService.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		pool.returnResource(jedis);
	}

	@Test
	public void testReturnObj() throws InterruptedException {
		User user1 = userService.viewReferenceUser();
		logger.debug("第一次查询结果：" + JSON.toJSONString(user1));
		Thread.sleep(500);
		assertTrue(jedis.exists(CacheKeyPrefix.TEST_CACHE_REFERENCEUSER));
		User user2 = userService.viewReferenceUser();
		logger.debug("第二次查询结果：" + JSON.toJSONString(user2));
		assertEquals(user1, user2);
	}

	@Test
	public void testReturnString() throws InterruptedException {
		String user1 = userService.viewReferenceUserName();
		logger.debug("第一次查询结果：" + user1);
		Thread.sleep(500);
		assertTrue(jedis.exists(CacheKeyPrefix.TEST_CACHE_REFERENCEUSERNAME));
		String user2 = userService.viewReferenceUserName();
		logger.debug("第二次查询结果：" + user2);
		assertEquals(user1, user2);
	}

	@Test
	public void testReturnLong() throws InterruptedException {
		long user1 = userService.viewReferenceUserId();
		logger.debug("第一次查询结果：" + user1);
		Thread.sleep(500);
		assertTrue(jedis.exists(CacheKeyPrefix.TEST_CACHE_REFERENCEUSERID));
		long user2 = userService.viewReferenceUserId();
		logger.debug("第二次查询结果：" + user2);
		assertEquals(user1, user2);
	}

	@Test
	public void testCacheKeySuffix() throws InterruptedException {
		User user1 = userService.view(3);
		logger.debug("第一次查询结果：" + JSON.toJSONString(user1));
		Thread.sleep(500);
		assertTrue(jedis.exists(CacheKeyPrefix.TEST_CACHE_USER
				+ CacheConfig.CACHEKEY_DELIMITER + 3));
		User user2 = userService.view(3);
		logger.debug("第二次查询结果：" + JSON.toJSONString(user2));
		assertEquals(user1, user2);
	}

	@Test
	public void testCacheKeySuffix2() throws InterruptedException {
		Pager pager = new Pager();
		pager.setPageNumber(1);
		pager.setPageSize(5);
		pager.setRecordCount(0);
		List<User> userList1 = userService.listInPage(pager);
		Thread.sleep(500);
		logger.debug("第一次查询结果：" + JSON.toJSONString(userList1));
		String key = CacheKeyPrefix.TEST_CACHE_ALLUSERS_INPAGE + ":"
				+ JSON.toJSONString(pager, SerializerFeature.UseSingleQuotes);
		logger.debug(key);
		assertTrue(jedis.exists(key));
		List<User> userList2 = userService.listInPage(pager);
		logger.debug("第二次查询结果：" + JSON.toJSONString(userList2));
		assertEquals(userList1, userList2);
	}

	@Test
	public void testTimeOut() throws InterruptedException {
		int count1 = userService.countUser();
		assertTrue(jedis.exists(CacheKeyPrefix.TEST_CACHE_COUNTUSER));
		Thread.sleep(1000);
		long ttl = jedis.ttl(CacheKeyPrefix.TEST_CACHE_COUNTUSER);
		logger.debug("ttl:" + ttl);
		assertTrue(ttl <= 10 && ttl > 7);
		int count2 = userService.countUser();
		assertEquals(count1, count2);
		Thread.sleep(10000);
		assertFalse(jedis.exists(CacheKeyPrefix.TEST_CACHE_COUNTUSER));
	}

	@Test
	public void testReturnSet() throws InterruptedException {
		Set<Integer> userIds1 = userService.userIds();
		logger.debug("第一次查询结果：" + JSON.toJSONString(userIds1));
		Thread.sleep(500);
		assertTrue(jedis.exists(CacheKeyPrefix.TEST_CACHE_USERIDS));
		Set<Integer> userIds2 = userService.userIds();
		logger.debug("第二次查询结果：" + JSON.toJSONString(userIds2));
		assertEquals(userIds1, userIds2);
	}

	@Test
	public void testReturnList() throws InterruptedException {
		List<Integer> userIds1 = userService.userTop5();
		logger.debug("第一次查询结果：" + JSON.toJSONString(userIds1));
		Thread.sleep(500);
		assertTrue(jedis.exists(CacheKeyPrefix.TEST_CACHE_TOP5));
		List<Integer> userIds2 = userService.userTop5();
		logger.debug("第二次查询结果：" + JSON.toJSONString(userIds2));
		assertEquals(userIds1, userIds2);
	}
}
