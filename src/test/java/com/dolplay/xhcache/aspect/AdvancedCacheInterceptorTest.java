package com.dolplay.xhcache.aspect;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.dolplay.xhcache.assets.CacheKeyPrefix;
import com.dolplay.xhcache.assets.domain.User;
import com.dolplay.xhcache.assets.lang.Lang;
import com.dolplay.xhcache.assets.service.UserAdvancedService;

public class AdvancedCacheInterceptorTest {
	private static Logger logger = LoggerFactory.getLogger(CacheInterceptorTest.class);
	private static ShardedJedisPool pool;
	private static ShardedJedis jedis;
	private static UserAdvancedService userService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] { "xh-commons-cache.xml",
				"xh-commons-cache-test-assets.xml" });

		logger.info("初始化redis数据及连接...");
		pool = appContext.getBean("jedisPool", ShardedJedisPool.class);
		jedis = pool.getResource();
		Collection<Jedis> jedisColl = jedis.getAllShards();
		for (Jedis jedis : jedisColl) {
			jedis.flushAll();
		}

		// 初始化UserService
		userService = appContext.getBean(UserAdvancedService.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		pool.returnResource(jedis);
	}

	@Test
	public void testSimple() throws ParseException, InterruptedException {
		List<Integer> ids1 = userService.listIdByGender("male");
		logger.debug("第一次查询用户结果：" + JSON.toJSONString(ids1));
		Thread.sleep(500);
		List<Integer> ids2 = userService.listIdByGender("male");
		logger.debug("第二次查询用户结果：" + JSON.toJSONString(ids2));
		assertEquals(ids1, ids2);
		jedis.del(CacheKeyPrefix.TEST_CACHE_ALLUSERS_IDLIST + ":male");
		List<Integer> ids3 = userService.listIdByGender("male");
		logger.debug("第三次查询用户结果：" + JSON.toJSONString(ids3));
		Thread.sleep(500);
		List<Integer> ids4 = userService.listIdByGender("male");
		logger.debug("第四次查询用户结果：" + JSON.toJSONString(ids4));
		assertEquals(ids3, ids4);
	}

	@Test
	public void testObjList() throws InterruptedException {
		List<User> userList1 = userService.listByGender("male");
		logger.debug("第一次查询用户结果：" + JSON.toJSONString(userList1));
		Thread.sleep(500);
		List<User> userList2 = userService.listByGender("male");
		logger.debug("第二次查询用户结果：" + JSON.toJSONString(userList2));
		assertEquals(userList1, userList2);
	}

	@Test
	public void testReverse() throws Exception {
		List<String> names1 = userService.listNewUsers();
		logger.debug("第一次查询用户结果：" + JSON.toJSONString(names1));
		Thread.sleep(500);
		List<String> names2 = userService.listNewUsers();
		logger.debug("第二次查询用户结果：" + JSON.toJSONString(names2));
		assertEquals(names1, names2);
		jedis.zadd(CacheKeyPrefix.TEST_CACHE_NEWUSERS_NAMELIST,
				System.currentTimeMillis(), "peter");
		List<String> namesCache = new ArrayList(jedis.zrevrange(
				CacheKeyPrefix.TEST_CACHE_NEWUSERS_NAMELIST, 0, -1));
		logger.debug("从缓存中获取结果:" + JSON.toJSONString(namesCache));
		jedis.del(CacheKeyPrefix.TEST_CACHE_NEWUSERS_NAMELIST);
		jedis.del("nutz-cache:ZsetEternalCacheKeySet");
		List<String> names3 = Lang.list("peter", "kate", "jo", "tom", "john");
		logger.debug("第三次查询用户结果：" + JSON.toJSONString(names3));
		assertEquals(namesCache, names3);
	}
}
