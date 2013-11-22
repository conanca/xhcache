package com.dolplay.xhcache.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.alibaba.fastjson.JSON;

/**
 * Redis实现的缓存DAO
 * 
 * @author conanca
 * 
 */
public class RedisCacheDao implements CacheDao {
	private static Logger logger = LoggerFactory.getLogger(RedisCacheDao.class);

	protected ShardedJedisPool jedisPool;

	public RedisCacheDao(ShardedJedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public void set(String cacheKey, Object cacheValue) throws Exception {
		set(cacheKey, -1, cacheValue);
	}

	public void set(String cacheKey, int timeout, Object cacheValue)
			throws Exception {
		ShardedJedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			if (timeout <= 0) {
				jedis.set(cacheKey, JSON.toJSONString(cacheValue));
			} else {
				jedis.setex(cacheKey, timeout, JSON.toJSONString(cacheValue));
			}
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	public String get(String cacheKey) throws Exception {
		ShardedJedis jedis = null;
		boolean broken = false;
		String valueJson = null;
		try {
			jedis = jedisPool.getResource();
			valueJson = jedis.get(cacheKey);
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
		return valueJson;
	}

	public <T> T get(String cacheKey, Class<T> type) throws Exception {
		return JSON.parseObject(get(cacheKey), type);
	}

	public long remove(String... cacheKeys) throws Exception {
		ShardedJedis jedis = null;
		boolean broken = false;
		long count = 0;
		try {
			jedis = jedisPool.getResource();
			for (String cacheKey : cacheKeys) {
				count += jedis.del(cacheKey);
			}
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
		return count;
	}

	public boolean expire(String cacheKey, int seconds) throws Exception {
		ShardedJedis jedis = null;
		boolean broken = false;
		long success = 0;
		try {
			jedis = jedisPool.getResource();
			success = jedis.expire(cacheKey, seconds);
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
		return success == 1 ? true : false;
	}

	public boolean exists(String cacheKey) throws Exception {
		ShardedJedis jedis = null;
		boolean broken = false;
		boolean isExist = false;
		try {
			jedis = jedisPool.getResource();
			isExist = jedis.exists(cacheKey);
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
		return isExist;
	}

	public Set<String> keySet(String pattern) throws Exception {
		ShardedJedis jedis = null;
		boolean broken = false;
		Set<String> keySet = null;
		try {
			jedis = jedisPool.getResource();
			Collection<Jedis> jedisColl = jedis.getAllShards();
			keySet = new HashSet<String>();
			for (Jedis aJedis : jedisColl) {
				keySet.addAll(aJedis.keys(pattern));
			}
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
		return keySet;
	}

	public String keyType(String key) throws Exception {
		ShardedJedis jedis = null;
		boolean broken = false;
		String keyType = null;
		try {
			jedis = jedisPool.getResource();
			keyType = jedis.type(key);
		} catch (JedisConnectionException e) {
			logger.error("Redis connection lost.", e);
			broken = true;
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
		return keyType;
	}

	/**
	 * 根据连接是否已中断的标志，分别调用returnBrokenResource或returnResource。
	 */
	protected void closeResource(ShardedJedis jedis, boolean connectionBroken) {
		if (jedis != null) {
			try {
				if (connectionBroken) {
					jedisPool.returnBrokenResource(jedis);
				} else {
					jedisPool.returnResource(jedis);
				}
			} catch (Exception e) {
				logger.error(
						"Error happen when return jedis to pool, try to close it directly.",
						e);
				closeJedis(jedis);
			}
		}
	}

	/**
	 * 关闭Jedis
	 */
	protected void closeJedis(ShardedJedis jedis) {
		try {
			jedis.disconnect();
		} catch (Exception e) {
			logger.error("Error happen when close jedis.", e);
		}
	}

}
