package utn.dds.daos;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class Redis<T> implements IDAO<T> {
    private JedisPool jedisPool;
    private String key;
    private ObjectMapper objectMapper;
    private Class<T> clazz;
    
    public Redis(String url,String host, int port, String password, Class<T> clazz) {
        initializePool(url,host,port,password);
        initializeObjectMapper();
        this.clazz = clazz;
    }
    
    public Redis(String url,String host, int port, String password, String key, Class<T> clazz) {
        initializePool(url,host,port,password);
        initializeObjectMapper();
        this.key = key;
        this.clazz = clazz;
    }
    
    private void initializeObjectMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
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
        if (key == null) {
            throw new IllegalStateException("Key no configurada. Use el constructor con key o el método findById(key)");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            if (value != null) {
                // Manejar tipos simples como String de forma especial
                if (clazz == String.class) {
                    List<T> result = new ArrayList<>();
                    result.add((T) value);
                    return result;
                } else {
                    // Para objetos complejos, usar el ObjectMapper
                    return objectMapper.readValue(value, 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar objetos en Redis con key: " + key, e);
        }
    }

    @Override
    public void save(T object) {
        if (key == null) {
            throw new IllegalStateException("Key no configurada. Use el constructor con key o el método save(key, value)");
        }
        
        try {
            // Manejar tipos simples como String de forma especial
            if (clazz == String.class) {
                save(key, (String) object);
            } else {
                List<T> existingObjects = find();
                if (existingObjects == null) {
                    existingObjects = new ArrayList<>();
                }
                existingObjects.add(object);
                
                String value = objectMapper.writeValueAsString(existingObjects);
                save(key, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar objeto en Redis con key: " + key, e);
        }
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
    
    public void saveAll(List<T> objects) {
        if (key == null) {
            throw new IllegalStateException("Key no configurada. Use el constructor con key");
        }
        
        try {
            // Manejar tipos simples como String de forma especial
            if (clazz == String.class && !objects.isEmpty()) {
                save(key, (String) objects.get(0)); // Para String, solo guardamos el primer elemento
            } else {
                String value = objectMapper.writeValueAsString(objects);
                save(key, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar lista de objetos en Redis con key: " + key, e);
        }
    }
    
    public void addAll(List<T> objects) {
        if (key == null) {
            throw new IllegalStateException("Key no configurada. Use el constructor con key");
        }
        
        try {
            // Manejar tipos simples como String de forma especial
            if (clazz == String.class && !objects.isEmpty()) {
                save(key, (String) objects.get(0)); // Para String, solo guardamos el primer elemento
            } else {
                List<T> existingObjects = find();
                if (existingObjects == null) {
                    existingObjects = new ArrayList<>();
                }
                existingObjects.addAll(objects);
                
                String value = objectMapper.writeValueAsString(existingObjects);
                save(key, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar objetos en Redis con key: " + key, e);
        }
    }
    
    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
} 