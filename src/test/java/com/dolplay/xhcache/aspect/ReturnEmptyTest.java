package com.dolplay.xhcache.aspect;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
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
import com.dolplay.xhcache.assets.domain.User;
import com.dolplay.xhcache.assets.service.ReturnEmptyService;

public class ReturnEmptyTest {
	private static Logger logger = LoggerFactory.getLogger(ReturnEmptyTest.class);
	private static ShardedJedisPool pool;
	private static ShardedJedis jedis;
	private static ReturnEmptyService testService;

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
		testService = appContext.getBean(ReturnEmptyService.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		pool.returnResource(jedis);
	}

	@Test
	public void testReturnNullString() {
		User u1 = testService.viewXobj();
		logger.debug("第一次查询结果：" + JSON.toJSONString(u1));
		assertNull(u1);
		User u2 = testService.viewXobj();
		logger.debug("第二次查询结果：" + JSON.toJSONString(u2));
		assertNull(u2);
		User u3 = testService.viewXobj();
		logger.debug("第三次查询结果：" + JSON.toJSONString(u3));
		assertNull(u3);
		Assert.assertTrue(jedis.exists("cache:StringEternalCacheKeySet"));
	}

	@Test
	public void testReturnNullInteger() {
		Integer a1 = testService.viewXinteger();
		logger.debug("第一次查询结果：" + JSON.toJSONString(a1));
		assertNull(a1);
		Integer a2 = testService.viewXinteger();
		logger.debug("第二次查询结果：" + JSON.toJSONString(a2));
		assertNull(a2);
		Integer a3 = testService.viewXinteger();
		logger.debug("第三次查询结果：" + JSON.toJSONString(a3));
		assertNull(a3);
		Assert.assertTrue(jedis.exists("cache:StringEternalCacheKeySet"));
	}

	@Test
	public void testReturnEmptyList() {
		List l1 = testService.xlist();
		logger.debug("第一次查询结果：" + JSON.toJSONString(l1));
		assertEquals(0, l1.size());
		List l2 = testService.xlist();
		logger.debug("第二次查询结果：" + JSON.toJSONString(l2));
		assertEquals(0, l2.size());
		List l3 = testService.xlist();
		logger.debug("第三次查询结果：" + JSON.toJSONString(l3));
		assertEquals(0, l3.size());
		Assert.assertTrue(jedis.exists("cache:ZsetEternalCacheKeySet"));
	}

	@Test
	public void testReturnNullList() {
		List l1 = testService.xlist2();
		logger.debug("第一次查询结果：" + JSON.toJSONString(l1));
		assertNull(l1);
		List l2 = testService.xlist2();
		logger.debug("第二次查询结果：" + JSON.toJSONString(l2));
		// TODO
		// assertNull(l2);
		List l3 = testService.xlist2();
		logger.debug("第三次查询结果：" + JSON.toJSONString(l3));
		// assertNull(l3);
		Assert.assertTrue(jedis.exists("cache:ZsetEternalCacheKeySet"));
	}
}
