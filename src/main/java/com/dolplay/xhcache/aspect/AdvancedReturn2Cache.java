package com.dolplay.xhcache.aspect;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.dolplay.xhcache.dao.AdvancedCacheDao;

public class AdvancedReturn2Cache extends Thread {
	private static Logger logger = LoggerFactory
			.getLogger(AdvancedReturn2Cache.class);

	private AdvancedCacheDao cacheDao;
	private String cacheKey;
	private int cacheTimeout;
	private List<?> cacheObj;
	private boolean reverse;
	private boolean isEternalCacheKeySetValid;
	private String zsetEternalCacheKeySetName;

	public AdvancedReturn2Cache(AdvancedCacheDao cacheDao, String cacheKey,
			int cacheTimeout, List<?> cacheObj, boolean reverse,
			boolean isEternalCacheKeySetValid, String zsetEternalCacheKeySetName) {
		super();
		this.cacheDao = cacheDao;
		this.cacheKey = cacheKey;
		this.cacheTimeout = cacheTimeout;
		this.cacheObj = cacheObj;
		this.reverse = reverse;
		this.isEternalCacheKeySetValid = isEternalCacheKeySetValid;
		this.zsetEternalCacheKeySetName = zsetEternalCacheKeySetName;
	}

	public void run() {
		if (cacheObj != null && cacheObj.size() > 0) {
			try {
				// 如果需要倒序存放入缓存中，则将顺序倒转
				if (reverse) {
					Collections.reverse(cacheObj);
				}
				// 按items的顺序依次插入相应的缓存中
				long now = System.currentTimeMillis();
				Map<Double, String> scoreItems = new HashMap<Double, String>();
				for (Object item : cacheObj) {
					String cacheValue = null;
					if (CharSequence.class.isAssignableFrom(item.getClass())) {
						cacheValue = item.toString();
					} else {
						cacheValue = JSON.toJSONString(item);
					}
					scoreItems.put((double) now++, cacheValue);
				}
				// 添加缓存，如果缓存超时时间设置的有效，则新增缓存时设置该超时时间，否则设置配置文件中所配置的超时时间
				cacheDao.zAdd(cacheKey, cacheTimeout, scoreItems);
				logger.debug("Set a new value for this cache -- " + cacheKey);
			} catch (Exception e) {
				logger.error("Error happen when set cache -- " + cacheKey, e);
			}
		} else {
			logger.warn("No value to set for this cache -- " + cacheKey);
		}
		// 往ZsetEternalCacheKeySet添加相应的Key
		if (isEternalCacheKeySetValid && cacheTimeout < 0) {
			try {
				cacheDao.sAdd(zsetEternalCacheKeySetName, cacheKey);
			} catch (Exception e) {
				logger.error("Error happen when set cache -- "
						+ zsetEternalCacheKeySetName, e);
			}
		}
	}
}
