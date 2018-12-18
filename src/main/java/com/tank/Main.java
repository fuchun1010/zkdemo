package com.tank;

import com.tank.util.CommandOpts;
import com.tank.util.ZooKeeperWatcher;
import lombok.val;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author fuchun
 * Zookeeper HA demo
 */
public class Main {
  public static void main(final String[] args) throws Exception {
    val parameters = new CommandOpts();
    CountDownLatch status = new CountDownLatch(1);
    val commands = parameters.initParameters().parseCommand(args);
    if (Objects.isNull(commands)) {
      log.warning("parse command error");
      return;
    }

    val connUrls = commands.getOptionValue("connUrls");
    val serverName = commands.getOptionValue("server");

    val zkWatcher = new ZooKeeperWatcher(connUrls, serverName);
    val rootPath = "/realTime";
    val tempPath = rootPath + File.separator + "server";
    boolean isOk = true;
    if (!zkWatcher.isExists(rootPath)) {
      isOk = zkWatcher.createFixedNode(rootPath, "ok".toUpperCase());
    }

    Executors.newFixedThreadPool(1).execute(() -> {
      while (true) {

        boolean isCreated = zkWatcher.createTemporaryNode(rootPath + File.separator + "server");
        if (isCreated) {
          status.countDown();
          break;
        } else {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          System.out.println("server:" + commands.getOptionValue("server") + " is standby");
        }
      }
    });


    Thread.sleep(1000 * 60);
    status.await();

    log.info("server:" + serverName + " is running now");

  }

  private static Logger log = Logger.getLogger(Main.class.getName());

}
