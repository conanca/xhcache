package com.dolplay.xhcache.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dolplay.xhcache.CacheConfig;
import com.dolplay.xhcache.annotation.Cache;
import com.dolplay.xhcache.annotation.CacheKeySuffix;
import com.dolplay.xhcache.dao.AdvancedCacheDao;
import com.dolplay.xhcache.lang.CStrings;
import com.dolplay.xhcache.type.CacheType;
import com.dolplay.xhcache.type.Order;

@Aspect
public class CacheAspect {
	private static Logger logger = LoggerFactory.getLogger(CacheAspect.class);

	private AdvancedCacheDao cacheDao;

	private int defaultStringCacheTimeout;
	private int defaultZsetCacheTimeout;
	private boolean stringEternalCacheKeySetIsValid;
	private boolean zsetEternalCacheKeySetIsValid;
	private String stringEternalCacheKeySetName;
	private String zsetEternalCacheKeySetName;
	private boolean stringSetUseNewThread;
	private boolean zsetSetUseNewThread;

	@Around("@annotation(com.dolplay.xhcache.annotation.Cache )")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		// 取得被拦截的方法及其注解
		MethodSignature joinPointObject = (MethodSignature) joinPoint
				.getSignature();
		Method method = joinPointObject.getMethod();
		Cache cacheAn = method.getAnnotation(Cache.class);
		Object[] args = joinPoint.getArgs();
		// 获取cacheKey
		String cacheKey = createCacheKey(args, method, cacheAn);
		// 若cacheKey不为空，将对该缓存及该方法返回值做相应操作否则直接执行方法
		if (cacheKey != null) {
			return cacheReturn(cacheKey, joinPoint, method, cacheAn);
		} else {
			// 执行方法
			return joinPoint.proceed();
		}
	}

	protected Object cacheReturn(String cacheKey,
			ProceedingJoinPoint joinPoint, Method method, Cache cacheAn)
			throws Throwable {
		boolean isEternalCacheKeySetValid = isEternalCacheKeySetValid(CacheType.zset);
		int cacheTimeout = createCacheTimeout(cacheAn, CacheType.zset);
		// 获取缓存类型，根据缓存类型不同分别对缓存有不同的操作方式
		CacheType cacheType = cacheAn.cacheType();
		if (cacheType.equals(CacheType.string)) {
			// 获取该方法欲读取的缓存的 VALUE
			String cacheValue = null;
			try {
				cacheValue = cacheDao().get(cacheKey);
			} catch (Exception e) {
				logger.error("Error happen when read cache -- " + cacheKey, e);
			}
			// 若缓存值不为空，则该方法直接返回缓存里相应的值
			if (cacheValue != null) {
				Class<?> returnType = method.getReturnType();
				Object returnValue = null;
				if (returnType.isAssignableFrom(List.class)) {
					returnValue = JSON.parseArray(cacheValue,
							(Class<?>) ((ParameterizedType) method
									.getGenericReturnType())
									.getActualTypeArguments()[0]);
				} else {
					returnValue = JSON.parseObject(cacheValue, returnType);
				}
				logger.debug("Get a value from this cache -- " + cacheKey);
				return returnValue;
			} else {
				logger.debug("Can't get any value from this cache -- "
						+ cacheKey);
				if (isEternalCacheKeySetValid && cacheTimeout < 0) {
					try {
						if (cacheDao().sIsMember(stringEternalCacheKeySetName,
								cacheKey)) {
							logger.debug(cacheKey + " is in "
									+ stringEternalCacheKeySetName
									+ ",will return null right now");
							return null;
						}
					} catch (Exception e) {
						logger.error("Error happen when read cache -- "
								+ stringEternalCacheKeySetName, e);
					}
				}
			}
			// 执行方法并获取方法返回值
			Object returnObj = joinPoint.proceed();
			// 插入相应缓存
			Return2Cache r2c = new Return2Cache(cacheDao(), cacheKey,
					cacheTimeout, returnObj, isEternalCacheKeySetValid,
					stringEternalCacheKeySetName);
			if (stringSetUseNewThread) {
				r2c.start();
			} else {
				r2c.run();
			}
			return returnObj;
		} else if (cacheType.equals(CacheType.zset)) {
			// 获取该方法欲读取的缓存的 VALUE
			List<?> cacheValue = null;
			Class<?> returnListItemType = (Class<?>) ((ParameterizedType) method
					.getGenericReturnType()).getActualTypeArguments()[0];
			try {
				if (cacheAn.reverse()) {
					cacheValue = cacheDao().zQueryAll(cacheKey, Order.Desc,
							returnListItemType);
				} else {
					cacheValue = cacheDao().zQueryAll(cacheKey, Order.Asc,
							returnListItemType);
				}
			} catch (Exception e) {
				logger.error("Error happen when read cache -- " + cacheKey, e);
			}
			// 若缓存值不为空，则该方法直接返回缓存里相应的值STRING_ETERNAL_CACHE_KEY_SET_IS_VALID
			if (cacheValue != null && cacheValue.size() > 0) {
				logger.debug("Get a value from this cache:" + cacheKey);
				return cacheValue;
			} else {
				logger.debug("Can't get any value from this cache -- "
						+ cacheKey);
				if (isEternalCacheKeySetValid && cacheTimeout < 0) {
					try {
						if (cacheDao().sIsMember(zsetEternalCacheKeySetName,
								cacheKey)) {
							logger.debug(cacheKey + " is in "
									+ zsetEternalCacheKeySetName
									+ ",will return empty list right now");
							return new ArrayList();
						}
					} catch (Exception e) {
						logger.error("Error happen when read cache -- "
								+ zsetEternalCacheKeySetName, e);
					}
				}
			}

			// 执行方法,获取方法返回值并增加相应缓存
			Object cacheObj = joinPoint.proceed();
			List<?> cacheObjList = null;
			if (cacheObj != null) {
				cacheObjList = new ArrayList((List<?>) cacheObj);
			}
			AdvancedReturn2Cache ar2c = new AdvancedReturn2Cache(cacheDao(),
					cacheKey, cacheTimeout, cacheObjList, cacheAn.reverse(),
					isEternalCacheKeySetValid, zsetEternalCacheKeySetName);
			// 是否在新线程中插入缓存
			if (zsetSetUseNewThread) {
				ar2c.start();
			} else {
				ar2c.run();
			}

			return cacheObj;
		} else {
			logger.error(
					"The method annotation of CacheType Error! will not use cache for this method",
					new RuntimeException("CacheType Error"));
			return joinPoint.proceed();
		}
	}

	/**
	 * 创建缓存名
	 * 
	 * @param chain
	 * @return
	 */
	protected String createCacheKey(Object args[], Method method, Cache cacheAn) {
		String cacheKey = null;

		// 获取缓存KEY的前缀：cacheKeyPrefix
		String cacheKeyPrefix = cacheAn.cacheKeyPrefix();
		// 若cacheKeyPrefix不为空，将对该缓存做相应操作否则直接执行方法
		if (!StringUtils.isEmpty(cacheKeyPrefix)) {
			// 获取该方法欲读取的缓存的 KEY——将拼接方法注解中的cacheKeyPrefix及标有“CacheKeySuffix”注解的参数
			String[] cacheParaArr = new String[args.length];
			Annotation[][] ans = method.getParameterAnnotations();
			int k = 0;
			if (ans.length > 0) {
				for (int i = 0; i < ans.length; i++) {
					for (int j = 0; j < ans[i].length; j++) {
						if (ans[i][j].annotationType() == CacheKeySuffix.class) {
							if (args[i] == null) {
								cacheParaArr[k] = "";
							} else if (CharSequence.class
									.isAssignableFrom(args[i].getClass())) {
								cacheParaArr[k] = args[i].toString();
							} else {
								cacheParaArr[k] = JSON.toJSONString(args[i],
										SerializerFeature.UseSingleQuotes);
							}
							k++;
						}
					}
				}
			}
			cacheKey = CStrings.cacheKey(cacheKeyPrefix, cacheParaArr);
			logger.debug("Cache key : " + cacheKey);
		} else {
			logger.warn("cacheKeyPrefix is empty!");
		}

		return cacheKey;
	}

	protected int createCacheTimeout(Cache cacheAn, CacheType type) {
		int cacheTimeout = cacheAn.cacheTimeout();
		if (cacheTimeout == CacheConfig.INVALID_TIMEOUT) {
			if (type.equals(CacheType.string)) {
				cacheTimeout = defaultStringCacheTimeout;
			} else if (type.equals(CacheType.zset)) {
				cacheTimeout = defaultZsetCacheTimeout;
			} else {
				logger.warn("Unknown cache type" + type);
				cacheTimeout = -1;
			}
		}
		return cacheTimeout;
	}

	protected boolean isEternalCacheKeySetValid(CacheType type) {
		boolean isValid = false;
		if (type.equals(CacheType.string)) {
			isValid = stringEternalCacheKeySetIsValid;
		} else if (type.equals(CacheType.zset)) {
			isValid = zsetEternalCacheKeySetIsValid;
		} else {
			logger.warn("Unknown cache type" + type);
		}
		return isValid;
	}

	public AdvancedCacheDao cacheDao() {
		return cacheDao;
	}

	public AdvancedCacheDao getCacheDao() {
		return cacheDao;
	}

	public void setCacheDao(AdvancedCacheDao cacheDao) {
		this.cacheDao = cacheDao;
	}

	public int getDefaultStringCacheTimeout() {
		return defaultStringCacheTimeout;
	}

	public void setDefaultStringCacheTimeout(int defaultStringCacheTimeout) {
		this.defaultStringCacheTimeout = defaultStringCacheTimeout;
	}

	public int getDefaultZsetCacheTimeout() {
		return defaultZsetCacheTimeout;
	}

	public void setDefaultZsetCacheTimeout(int defaultZsetCacheTimeout) {
		this.defaultZsetCacheTimeout = defaultZsetCacheTimeout;
	}

	public boolean isStringEternalCacheKeySetIsValid() {
		return stringEternalCacheKeySetIsValid;
	}

	public void setStringEternalCacheKeySetIsValid(
			boolean stringEternalCacheKeySetIsValid) {
		this.stringEternalCacheKeySetIsValid = stringEternalCacheKeySetIsValid;
	}

	public boolean isZsetEternalCacheKeySetIsValid() {
		return zsetEternalCacheKeySetIsValid;
	}

	public void setZsetEternalCacheKeySetIsValid(
			boolean zsetEternalCacheKeySetIsValid) {
		this.zsetEternalCacheKeySetIsValid = zsetEternalCacheKeySetIsValid;
	}

	public String getStringEternalCacheKeySetName() {
		return stringEternalCacheKeySetName;
	}

	public void setStringEternalCacheKeySetName(
			String stringEternalCacheKeySetName) {
		this.stringEternalCacheKeySetName = stringEternalCacheKeySetName;
	}

	public String getZsetEternalCacheKeySetName() {
		return zsetEternalCacheKeySetName;
	}

	public void setZsetEternalCacheKeySetName(String zsetEternalCacheKeySetName) {
		this.zsetEternalCacheKeySetName = zsetEternalCacheKeySetName;
	}

	public boolean isStringSetUseNewThread() {
		return stringSetUseNewThread;
	}

	public void setStringSetUseNewThread(boolean stringSetUseNewThread) {
		this.stringSetUseNewThread = stringSetUseNewThread;
	}

	public boolean isZsetSetUseNewThread() {
		return zsetSetUseNewThread;
	}

	public void setZsetSetUseNewThread(boolean zsetSetUseNewThread) {
		this.zsetSetUseNewThread = zsetSetUseNewThread;
	}
}
