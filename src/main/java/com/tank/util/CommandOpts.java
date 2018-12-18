package com.tank.util;

import org.apache.commons.cli.*;

import java.util.logging.Logger;

/**
 * @author fuchun
 */
public class CommandOpts {

  public CommandOpts initParameters() {
    final Option connUrlOpt = new Option("connUrls", true, "this is zookeeper conn urls");
    final Option serverNameOpt = new Option("server", true, "this is name of server");
    options.addOption(connUrlOpt);
    options.addOption(serverNameOpt);
    return this;
  }

  public CommandLine parseCommand(final String[] args) {
    final DefaultParser parser = new DefaultParser();
    try {
      return parser.parse(this.options, args);
    } catch (ParseException e) {
      e.printStackTrace();
      log.warning("parse input parameter errors");
      return null;
    }
  }

  private final Options options = new Options();

  private final Logger log = Logger.getLogger(CommandOpts.class.getSimpleName());
}
