package utn.dds.daos;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.InputStream;
import java.util.List;

public class Redis<T> implements IDAO<T> {
    private JedisPool jedisPool;
    public Redis(String url,String host, int port, String password) {
        initializePool(url,host,port,password);
    }
    
    private void initializePool(String url,String host, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        
        if (url != null && !url.trim().isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, url);
        } else if (password != null && !password.trim().isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        } else {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000);
        }
    }
    @Override
    public InputStream read() {
        throw new UnsupportedOperationException("No implementado");
    }

    @Override
    public List<T> find() {
        throw new UnsupportedOperationException("No implementado");
    }

    @Override
    public void save(T object) {
        throw new UnsupportedOperationException("No implementado");
    }
    
    public void save(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar en Redis", e);
        }
    }
    
    public String findById(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            if (value != null) {
               return value;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar el objeto por ID en Redis", e);
        }
    }
    
    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
} 