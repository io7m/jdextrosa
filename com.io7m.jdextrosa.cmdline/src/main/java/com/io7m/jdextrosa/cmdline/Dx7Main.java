/*
 * Copyright © 2017 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jdextrosa.cmdline;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main command line entry point.
 */

public final class Dx7Main implements Runnable
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Dx7Main.class);
  }

  private final Map<String, Dx7CommandType> commands;
  private final JCommander commander;
  private final String[] args;
  private int exit_code;

  private Dx7Main(
    final String[] in_args)
  {
    this.args =
      Objects.requireNonNull(in_args, "Command line arguments");

    final Dx7CommandRoot r =
      new Dx7CommandRoot();
    final Dx7CommandConvertBatch cmd_convert_batch =
      new Dx7CommandConvertBatch();
    final Dx7CommandConvert cmd_convert =
      new Dx7CommandConvert();

    this.commands = new HashMap<>(8);
    this.commands.put("convert-batch", cmd_convert_batch);
    this.commands.put("convert", cmd_convert);

    this.commander = new JCommander(r);
    this.commander.setProgramName("jdextrosa");
    this.commander.addCommand("convert-batch", cmd_convert_batch);
    this.commander.addCommand("convert", cmd_convert);
  }

  /**
   * The main entry point.
   *
   * @param args Command line arguments
   */

  public static void main(final String[] args)
  {
    final Dx7Main cm = new Dx7Main(args);
    cm.run();
    System.exit(cm.exitCode());
  }

  /**
   * @return The program exit code
   */

  public int exitCode()
  {
    return this.exit_code;
  }

  @Override
  public void run()
  {
    try {
      this.commander.parse(this.args);

      final String cmd = this.commander.getParsedCommand();
      if (cmd == null) {
        final StringBuilder sb = new StringBuilder(128);
        this.commander.usage(sb);
        LOG.info("Arguments required.\n{}", sb.toString());
        this.exit_code = 1;
        return;
      }

      final Dx7CommandType command = this.commands.get(cmd);
      final Dx7CommandType.Status status = command.execute();
      this.exit_code = status.exitCode();
    } catch (final ParameterException e) {
      final StringBuilder sb = new StringBuilder(128);
      this.commander.usage(sb);
      LOG.error("{}\n{}", e.getMessage(), sb.toString());
      this.exit_code = 1;
    } catch (final Exception e) {
      LOG.error("{}", e.getMessage(), e);
      this.exit_code = 1;
    }
  }
}
