package com.tank.util;

import lombok.NonNull;
import lombok.val;
import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.ACL;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author fuchun
 * @date 2018-12-18
 */
public class ZooKeeperWatcher implements Watcher {


  public ZooKeeperWatcher(@NonNull final String connUrls, @NonNull final String serverName) {
    super();
    this.connUrls = connUrls;
    this.serverName = serverName;
    this.conn();
  }

  public void conn() {
    try {
      this.zooKeeper = new ZooKeeper(this.connUrls, 5000, this);
      connLatch.await();
      logger.info("connect zookeeper success");
    } catch (Exception e) {
      do {
        conn();
        reConnectTimes.incrementAndGet();
        logger.warning("reconnect zookeeper server:" + reConnectTimes.get());
      } while (reConnectTimes.get() == 5);
      e.printStackTrace();
    }

  }

  public boolean createFixedNode(final String path, final String value) {
    try {
      this.zooKeeper.create(path, value.getBytes(), aclList, CreateMode.PERSISTENT);
      this.zooKeeper.exists(path, true);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean createTemporaryNode(final String path) {
    try {
      this.zooKeeper.create(path, this.serverName.getBytes(), aclList, CreateMode.EPHEMERAL);
      this.zooKeeper.exists(path, true);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public void close() {
    if (this.zooKeeper != null) {
      try {
        this.zooKeeper.close();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public Optional<List<String>> simpleChild(final String path) {
    try {
      val list = this.zooKeeper.getChildren(path, true);
      val servers = new LinkedList<String>();
      for (String relativePath : list) {
        val absolutePath = path + File.separator + relativePath;
        System.out.println(absolutePath);
        val stat = this.zooKeeper.exists(absolutePath, true);
        if (!Objects.isNull(stat)) {
          byte[] data = this.zooKeeper.getData(absolutePath, this, null);
          val name = new String(data, "UTF-8");
          servers.add(name);
        }
      }

      return Optional.ofNullable(servers);
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }


  public boolean isExists(@NonNull final String path) throws KeeperException, InterruptedException {
    val result = this.zooKeeper.exists(path, true);
    return !Objects.isNull(result);
  }

  public boolean isRunningStatus() {

    throw new UnsupportedOperationException();
  }

  @Override
  public void process(WatchedEvent event) {
    final EventType eventType = event.getType();
    final KeeperState keeperState = event.getState();
    if (keeperState == KeeperState.SyncConnected) {
      if (eventType == EventType.None) {
        connLatch.countDown();
      } else if (eventType == EventType.NodeCreated) {
        logger.info("create node success, path is:" + event.getPath());
      } else if (eventType == EventType.NodeDeleted) {
        this.simpleChild("/");
        logger.info("delete node success, path is:" + event.getPath());
      }
    }

  }

  private ZooKeeperWatcher() {

  }


  private String connUrls;

  private CountDownLatch connLatch = new CountDownLatch(1);

  private AtomicInteger reConnectTimes = new AtomicInteger(0);

  private Logger logger = Logger.getLogger(ZooKeeperWatcher.class.getSimpleName());

  private List<ACL> aclList = ZooDefs.Ids.OPEN_ACL_UNSAFE;

  private ZooKeeper zooKeeper = null;

  private String serverName = null;
}
