package com.fosun.financial.data.proxy.ipproxyclient.utils.redis;

import com.fosun.financial.data.proxy.ipproxyclient.utils.common.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis相关的处理方法
 *
 * @author mario1oreo
 * @date 2018-1-2 17:33:47
 */
public class RedisTools{
    protected static JedisPool pool = null;

    public RedisTools() {
        initPool();
    }

    public static Jedis initPool() {
        if (pool == null) {
            if (StringUtils.isEmpty(PropertiesUtil.getApplicationValue("ip.proxy.redis.password", ""))) {
                pool = new JedisPool(new JedisPoolConfig(),
                        PropertiesUtil.getApplicationValue("ip.proxy.redis.ip", "localhost"),
                        Integer.valueOf(PropertiesUtil.getApplicationValue("ip.proxy.redis.port", "6379")),
                        Integer.valueOf(PropertiesUtil.getApplicationValue("ip.proxy.redis.timeout", "3000")));
            } else {
                pool = new JedisPool(new JedisPoolConfig(),
                        PropertiesUtil.getApplicationValue("ip.proxy.redis.ip", "localhost"),
                        Integer.valueOf(PropertiesUtil.getApplicationValue("ip.proxy.redis.port", "6379")),
                        Integer.valueOf(PropertiesUtil.getApplicationValue("ip.proxy.redis.timeout", "3000")),
                        PropertiesUtil.getApplicationValue("ip.proxy.redis.password", "bigdata"));
            }
        }
        Jedis jedis = pool.getResource();
        jedis.select(Integer.valueOf(PropertiesUtil.getApplicationValue("ip.proxy.redis.lib", "2")));
        return jedis;
    }

    public void setStr(String key, int liveTimeSecond) {
        Jedis jedis = initPool();
        try {
            jedis.setex(key, liveTimeSecond, key);
        } finally {
            jedis.close();
        }
    }

    public String getStr(String key) {
        Jedis jedis = initPool();
        try {
            String value = jedis.get(key);
            if (StringUtils.isBlank(value)) {
                return StringUtils.EMPTY;
            }
            return value;
        } finally {
            jedis.close();
        }
    }

    public boolean existKey(String key) {
        Jedis jedis = initPool();
        try {
            return jedis.exists(key);
        } finally {
            jedis.close();
        }
    }

    public int expireKey(String key, int liveTimeSecond) {
        Jedis jedis = initPool();
        try {
            return Math.toIntExact(jedis.expire(key, liveTimeSecond));
        } finally {
            jedis.close();
        }
    }

    public int ttlKey(String key) {
        Jedis jedis = initPool();
        try {
            return Math.toIntExact(jedis.ttl(key));
        } finally {
            jedis.close();
        }
    }

    public void rpushStr(String key, String value) {
        Jedis jedis = initPool();
        try {
            jedis.rpush(key, value);
        } finally {
            jedis.close();
        }
    }

    public void lpushStr(String key, String value) {
        Jedis jedis = initPool();
        try {
            jedis.lpush(key, value);
        } finally {
            jedis.close();
        }
    }

    public String lpopStr(String key) {
        Jedis jedis = initPool();
        try {
            String value = jedis.lpop(key);
            if (StringUtils.isBlank(value)) {
                return StringUtils.EMPTY;
            }
            return value;
        } finally {
            jedis.close();
        }
    }

    public String rpopStr(String key) {
        Jedis jedis = initPool();
        try {
            String value = jedis.rpop(key);
            if (StringUtils.isBlank(value)) {
                return StringUtils.EMPTY;
            }
            return value;
        } finally {
            jedis.close();
        }
    }

    public int countStr(String key) {
        Jedis jedis = initPool();
        try {
            return Math.toIntExact(jedis.llen(key));
        } catch (ArithmeticException e) {
            return 0;
        } finally {
            jedis.close();
        }
    }

    public boolean sismemberSet(String key, String member) {
        Jedis jedis = initPool();
        try {
            return jedis.sismember(key, member);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
    }

    public void saddSet(String key, String member) {
        Jedis jedis = initPool();
        try {
            jedis.sadd(key, member);
        } finally {
            jedis.close();
        }
    }

    public void sremSet(String key, String member) {
        Jedis jedis = initPool();
        try {
            jedis.srem(key, member);
        } finally {
            jedis.close();
        }
    }

    public void zaddSortSet(String key, double score, String member) {
        Jedis jedis = initPool();
        try {
            jedis.zadd(key, score, member);
        } finally {
            jedis.close();
        }
    }

    public int zremSortSet(String key, String member) {
        Jedis jedis = initPool();
        try {
            return Math.toIntExact(jedis.zrem(key, member));
        } finally {
            jedis.close();
        }
    }

    public double zscoreSortSet(String key, String member) {
        Jedis jedis = initPool();
        try {
            return jedis.zscore(key, member);
        } finally {
            jedis.close();
        }
    }

    public boolean sismemberSortSet(String key, String member) {
        Jedis jedis = initPool();
        try {
            Long result = jedis.zrank(key, member);
            if (result == null) {
                return false;
            }
            return true;
        } finally {
            jedis.close();
        }
    }


    public static void main(String[] args) {

        double current = 0.5;
        for (int i = 0; i < 100; i++) {
            current = (current *14+0)/15;
            if (current < 0.3) {
                System.out.println("i = " + i);
                System.out.println("current = " + current);
                break;
            }
        }

    }
}
