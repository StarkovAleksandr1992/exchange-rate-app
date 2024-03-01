
package ru.starkov.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The ConnectionManager class provides methods to manage a connection pool for JDBC connections. It
 * uses a blocking queue to manage connections and provides a proxy mechanism for safely returning
 * connections to the pool.
 */
public final class ConnectionManager {

  private static final String URL = "db.url";
  private static final String USER = "db.username";
  private static final String PASSWORD = "db.password";
  private static final String POOL_SIZE = "db.pool.size";
  private static final int DEFAULT_POOL_SIZE = 5;
  private static BlockingQueue<Connection> pool;
  private static List<Connection> connections;


  private ConnectionManager() {
  }

  /**
   * Initializes the connection pool based on the properties loaded from the properties file. Uses
   * reflection to create proxy connections for safely returning connections to the pool.
   */
  public static void initConnectionPool() {
    var poolSize = PropertiesLoader.get(POOL_SIZE);
    var size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
    pool = new ArrayBlockingQueue<>(size);
    connections = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Connection connection = open();
      Connection proxyConnection = (Connection) Proxy.newProxyInstance(
          ConnectionManager.class.getClassLoader(),
          new Class[]{Connection.class},
          (proxy, method, args) -> {
            if (method.getName().equals("close")) {
              Connection returnedProxyConnection = (Connection) proxy;
              // Workaround to ensure auto-commit is enabled in service method
              returnedProxyConnection.setAutoCommit(true);
              return pool.add(returnedProxyConnection);
            } else {
              return method.invoke(connection, args);
            }
          });
      pool.add(proxyConnection);
      connections.add(connection);
    }
  }

  /**
   * Retrieves a connection from the connection pool. Blocks until a connection is available.
   *
   * @return a Connection object from the pool
   */
  public static Connection getConnection() {
    try {
      return pool.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Closes all connections in the connection pool.
   */
  public static void closeConnections() {
    for (Connection connection : connections) {
      try {
        connection.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Opens a new database connection based on the properties loaded from the properties file.
   *
   * @return a new Connection object
   */
  private static Connection open() {
    var url = PropertiesLoader.get(URL);
    var user = PropertiesLoader.get(USER);
    var password = PropertiesLoader.get(PASSWORD);
    try {
      return DriverManager.getConnection(url, user, password);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
