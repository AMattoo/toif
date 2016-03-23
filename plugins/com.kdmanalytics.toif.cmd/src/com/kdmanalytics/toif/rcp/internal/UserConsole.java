
package com.kdmanalytics.toif.rcp.internal;

/*******************************************************************************
 * Copyright (c) 2016 KDM Analytics, Inc. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Open
 * Source Initiative OSI - Open Software License v3.0 which accompanies this
 * distribution, and is available at
 * http://www.opensource.org/licenses/osl-3.0.php/
 ******************************************************************************/
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.kdmanalytics.toif.rcp.internal.cmd.AdaptorCmd;
import com.kdmanalytics.toif.rcp.internal.cmd.MergeCmd;
import com.kdmanalytics.toif.rcp.internal.cmd.VersionCmd;
import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.HelpRequestedException;

public class UserConsole {
  
  private static final Logger LOG = LoggerFactory.getLogger(UserConsole.class);

  
  private String toifArgs[] = null;
  
  private String AdaptorArgs[] = null;
  
  public UserConsole() {
  }
  
  public UserConsole(String toifArgs[], String adaptorArgs[]) {
    this.toifArgs = toifArgs;
    this.AdaptorArgs = adaptorArgs;
  }
  
  public void execute() {
    
    Cli<ToifCli> cli = CliFactory.createCli(ToifCli.class);
    // Check out CLI options
    ToifCli toifCli = null;
    try {
      if (toifArgs.length == 0) {
        System.out.println(cli.getHelpMessage());
        return;
      }
      toifCli = CliFactory.parseArguments(ToifCli.class, toifArgs);
    } catch (HelpRequestedException ex) {
      System.out.println(cli.getHelpMessage());
      return;
    } catch (Exception ex) {
      LOG.error("Invalid Arguments: " + ex.getMessage());
      return;
    }
    
    // Check if supplied options are valid
    if (argsValid(toifCli) == false) {
      return;
    }
    
    // Are we doing Version?
    if (toifCli.isVersion()) doVersion(toifCli);
    if (toifCli.isAdaptor()) doAdaptor(toifCli);
    if (toifCli.isMerge()) doMerge(toifCli);
    
  }
  
  private void doVersion(ToifCli toifCli) {
    VersionCmd cmd = new VersionCmd();
    cmd.execute(toifCli, this.AdaptorArgs);
  }
  
  private void doAdaptor(ToifCli toifCli) {
    // Ensure that output directory is specifed
    if (!toifCli.isOutputdirectory()) {
      LOG.error("Output directory needs to be specified");
      return;
    }
    
    // Ensure that specified directory exists
    if (!toifCli.getOutputdirectory().exists()) toifCli.getOutputdirectory().mkdirs();
    
    AdaptorCmd cmd = new AdaptorCmd();
    cmd.execute(toifCli, this.AdaptorArgs);
    
  }
  
  private void doMerge(ToifCli toifCli) {
    MergeCmd cmd = new MergeCmd();
    cmd.execute(toifCli, this.AdaptorArgs);
  }
  
  /***************************************************
   * Just perform some sanity checking on the arguments
   * 
   **************************************************/
  private boolean argsValid(ToifCli cli) {
    
    // Check that we are only doing a single command
    if (cli.isMerge() && cli.isAdaptor()) {
      LOG.error("Can only do adaptor or merge");
      return false;
    }
    
    // Check inputfile
    if (cli.isInputfile()) {
      for (File file : cli.getInputfile()) {
        if (!file.exists()) {
          LOG.error("Specified inputfile does not exist: " + file.getAbsolutePath());
          return false;
        }
        
        if (!file.isFile() && !file.isDirectory()) {
          LOG.error("Specified inputfile not valid: " + file.getAbsolutePath());
          return false;
        }
        
      }
      
    }
    
    // Check house keeping
    if (cli.isHousekeeping()) {
      if (!cli.getHousekeeping().isFile()) {
        LOG.error("Specified housekeeping file not valid: " + cli.getHousekeeping());
        return false;
      }
    }
    
    
    //Check KDM file output file
    if (cli.isKdmfile()) {
      File kFile = cli.getKdmfile();
      try {
        Files.createParentDirs(kFile);
      } catch (IOException ex) {
        LOG.error("Unable to create parent directory of specified parent file: " + cli.getKdmfile());
        return false;
      }
    }
    
    return true;
  }
}
