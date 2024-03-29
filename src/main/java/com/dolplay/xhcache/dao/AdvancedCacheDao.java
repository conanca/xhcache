package com.dolplay.xhcache.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dolplay.xhcache.type.Order;

/**
 * 高级缓存DAO，可操作有序集缓存(注意：有序集缓存都是永久缓存)
 * @author conanca
 *
 */
public interface AdvancedCacheDao extends CacheDao {

	/**
	 * 为有序集缓存的值增添一个元素，需指定该元素的score。
	 * 如果缓存不存在则创建这个缓存，并指定缓存超时时间(秒)；如果缓存存在，则超时时间不会被更新
	 * 如果超时时间小于等于0，则为永久缓存
	 * @param cacheKey
	 * @param seconds
	 * @param score
	 * @param item
	 * @throws Exception
	 */
	public void zAdd(String cacheKey, int seconds, double score, Object item) throws Exception;

	/**
	 * 为有序集缓存的值增添一个元素，需指定该元素的score
	 * 如果缓存不存在则创建这个缓存。该缓存为永久缓存。
	 * @param cacheKey
	 * @param score
	 * @param item
	 * @throws Exception
	 */
	public void zAdd(String cacheKey, double score, Object item) throws Exception;

	/**
	 * 为有序集缓存的值增添多个元素其 score 值
	 * 注意：元素必须是String类型，否则需要先行用com.alibaba.fastjson.JSON.toJSONString进行转换
	 * 如果缓存不存在则创建这个缓存，并指定缓存超时时间(秒)；如果缓存存在，则超时时间不会被更新
	 * 如果超时时间小于等于0，则为永久缓存
	 * @param cacheKey
	 * @param seconds
	 * @param scoreItems
	 * @throws Exception
	 */
	public void zAdd(String cacheKey, int seconds, Map<Double, String> scoreItems) throws Exception;

	/**
	 * 为有序集缓存的值增添多个元素其 score 值
	 * 注意：元素必须是String类型，否则需要先行用com.alibaba.fastjson.JSON.toJSONString进行转换
	 * 如果缓存不存在则创建这个缓存。该缓存为永久缓存。
	 * @param cacheKey
	 * @param scoreItems
	 * @throws Exception
	 */
	public void zAdd(String cacheKey, Map<Double, String> scoreItems) throws Exception;

	/**
	 * 查询有序集缓存，按照区间及排序方式
	 * 如：startIndex=0 endIndex=9 order=Order.Desc，按第1条-第10条，然后按倒序返回一个list
	 *        （查全部的：startIndex=0 endIndex=-1）
	 * @param cacheKey
	 * @param startIndex
	 * @param endIndex
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public List<String> zQueryByRank(String cacheKey, long startIndex, long endIndex, Order order) throws Exception;

	/**
	 * 查询有序集缓存，按照区间
	 * 如：startIndex=0 endIndex=9，取第1条-第10条，返回一个list
	 *        （查全部的：startIndex=0 endIndex=-1）
	 * @param cacheKey
	 * @param startIndex
	 * @param endIndex
	 * @return
	 * @throws Exception
	 */
	public List<String> zQueryByRank(String cacheKey, long startIndex, long endIndex) throws Exception;

	/**
	 * 查询有序集缓存，按照score值范围及排序方式
	 * minScore=1997 maxScore=2013 order=Order.Desc，取score值在1997-2013的，然后按倒序返回一个list
	 * @param cacheKey
	 * @param minScore
	 * @param maxScore
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public List<String> zQueryByScore(String cacheKey, double minScore, double maxScore, Order order) throws Exception;

	/**
	 * 查询有序集缓存，按照score值范围
	 * minScore=1997 maxScore=2013，取score值在1997-2013的，返回一个list
	 * @param cacheKey
	 * @param minScore
	 * @param maxScore
	 * @return
	 * @throws Exception
	 */
	public List<String> zQueryByScore(String cacheKey, double minScore, double maxScore) throws Exception;

	/**
	 * 查询有序集缓存（全部item），按照排序方式
	 * @param cacheKey
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public List<String> zQueryAll(String cacheKey, Order order) throws Exception;

	/**
	 * 查询有序集缓存（全部item）
	 * @param cacheKey
	 * @return
	 * @throws Exception
	 */
	public List<String> zQueryAll(String cacheKey) throws Exception;

	/**
	 * 查询有序集缓存，按照区间及排序方式，指定了列表元素类型
	 * 如：startIndex=0 endIndex=9 order=Order.Desc，按第1条-第10条，然后按倒序返回一个list
	 *        （查全部的：startIndex=0 endIndex=-1）
	 * @param cacheKey
	 * @param startIndex
	 * @param endIndex
	 * @param order
	 * @param itemType
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> zQueryByRank(String cacheKey, long startIndex, long endIndex, Order order, Class<T> itemType)
			throws Exception;

	/**
	 * 查询有序集缓存，按照区间，指定了列表元素类型
	 * 如：startIndex=0 endIndex=9，取第1条-第10条，返回一个list
	 *        （查全部的：startIndex=0 endIndex=-1）
	 * @param cacheKey
	 * @param startIndex
	 * @param endIndex
	 * @param itemType
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> zQueryByRank(String cacheKey, long startIndex, long endIndex, Class<T> itemType)
			throws Exception;

	/**
	 * 查询有序集缓存，按照score值范围及排序方式，指定了列表元素类型
	 * minScore=1997 maxScore=2013 order=Order.Desc，取score值在1997-2013的，然后按倒序返回一个list
	 * @param cacheKey
	 * @param minScore
	 * @param maxScore
	 * @param order
	 * @param itemType
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> zQueryByScore(String cacheKey, double minScore, double maxScore, Order order, Class<T> itemType)
			throws Exception;

	/**
	 * 查询有序集缓存，按照score值范围，指定了列表元素类型
	 * minScore=1997 maxScore=2013，取score值在1997-2013的，返回一个list
	 * @param cacheKey
	 * @param minScore
	 * @param maxScore
	 * @param itemType
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> zQueryByScore(String cacheKey, double minScore, double maxScore, Class<T> itemType)
			throws Exception;

	/**
	 * 查询有序集缓存（全部item），按照排序方式，指定了列表元素类型
	 * @param cacheKey
	 * @param order
	 * @param itemType
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> zQueryAll(String cacheKey, Order order, Class<T> itemType) throws Exception;

	/**
	 * 查询有序集缓存（全部item），指定了列表元素类型
	 * @param cacheKey
	 * @param itemType
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> zQueryAll(String cacheKey, Class<T> itemType) throws Exception;

	/**
	 * 删除有序集缓存的一部分元素，按照元素的值
	 * @param cacheKey
	 * @param items
	 * @throws Exception
	 */
	public void zDel(String cacheKey, String... items) throws Exception;

	/**
	 * 删除有序集缓存的一部分元素，按照区间
	 * @param cacheKey
	 * @param startIndex
	 * @param endIndex
	 * @throws Exception
	 */
	public void zDelByRank(String cacheKey, long startIndex, long endIndex) throws Exception;

	/**
	 * 删除有序集缓存的一部分元素，按照socre值的范围
	 * @param cacheKey
	 * @param startIndex
	 * @param endIndex
	 * @throws Exception
	 */
	public void zDelByScore(String cacheKey, double minScore, double maxScore) throws Exception;

	public long sAdd(String key, String... members) throws Exception;

	public Set<String> sMember(String key) throws Exception;

	public long sRem(String key, String... members) throws Exception;

	public boolean sIsMember(String key, String member) throws Exception;
}
