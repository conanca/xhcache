package com.dolplay.xhcache.dao;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;

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

import com.dolplay.xhcache.assets.domain.User;
import com.dolplay.xhcache.dao.CacheDao;

public class CacheDaoTest {
	private static Logger logger = LoggerFactory.getLogger(CacheDaoTest.class);
	private static CacheDao cacheDao;
	private static ShardedJedisPool pool;
	private static ShardedJedis jedis;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("xh-commons-cache.xml");

		// 初始化redis数据及连接
		logger.info("初始化redis数据及连接...");
		pool = appContext.getBean("jedisPool", ShardedJedisPool.class);
		jedis = pool.getResource();
		Collection<Jedis> jedisColl = jedis.getAllShards();
		for (Jedis jedis : jedisColl) {
			jedis.flushAll();
		}

		// 初始化cacheDao
		cacheDao = appContext.getBean("cacheDao", CacheDao.class);
	}

	@AfterClass
	public static void setUpAfterClass() throws Exception {
		pool.returnResource(jedis);
	}

	@Test
	public void testExists() throws Exception {
		jedis.set("oooo", "OK!");
		assertTrue(cacheDao.exists("oooo"));
		assertFalse(cacheDao.exists("xxxx"));
	}

	@Test
	public void testSet() throws Exception {
		cacheDao.set("test:testSet:name", "testSet");
		assertTrue(jedis.exists("test:testSet:name"));
	}

	@Test
	public void testSetTimeout() throws Exception {
		cacheDao.set("test:testSetTimeout:name", 300, "testSetTimeout");
		assertTrue(jedis.exists("test:testSet:name"));
		assertTrue(jedis.ttl("test:testSetTimeout:name") <= 300
				&& jedis.ttl("test:testSetTimeout:name") > 290);
	}

	@Test
	public void testGet() throws Exception {
		jedis.set("test:testGet:name", "testGet");
		assertEquals(jedis.get("test:testGet:name"),
				cacheDao.get("test:testGet:name"));
	}

	@Test
	public void testSetGetWithType() throws Exception {
		User user = new User();
		user.setId(12L);
		user.setName("jack");
		user.setGender("male");
		user.setDescription("for test");
		user.setBirthday(new Date());
		cacheDao.set("test:user:12", user);
		assertTrue(jedis.exists("test:user:12"));
		User user2 = cacheDao.get("test:user:12", User.class);
		assertTrue(user2.equals(user));
	}

	@Test
	public void testRemove() throws Exception {
		jedis.set("test:testRemove:name", "testRemove");
		cacheDao.remove("test:testRemove:name");
		assertFalse(jedis.exists("test:testRemove:name"));
	}

}
