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

package com.io7m.jdextrosa.tests;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;

public final class TestMemoryFilesystemExtension
  implements AfterEachCallback, ParameterResolver
{
  private static final String KEY = "memoryFilesystem";

  public TestMemoryFilesystemExtension()
  {

  }

  @Override
  public void afterEach(
    final ExtensionContext context)
    throws Exception
  {
    final FileSystem filesystem =
      this.getStore(context).get(KEY, FileSystem.class);

    if (filesystem != null) {
      try {
        filesystem.close();
      } catch (final Exception e) {
        // silent failures
      }
    }
  }

  @Override
  public boolean supportsParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    final Class<?> type = parameterContext.getParameter().getType();
    return type.isAssignableFrom(FileSystem.class);
  }

  @Override
  public Object resolveParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext)
    throws ParameterResolutionException
  {
    return this.getStore(extensionContext).getOrComputeIfAbsent(KEY, key -> {
      try {
        return MemoryFileSystemBuilder.newLinux().build("test");
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private ExtensionContext.Store getStore(
    final ExtensionContext ctx)
  {
    return ctx.getStore(this.namespace(ctx));
  }

  private ExtensionContext.Namespace namespace(
    final ExtensionContext ctx)
  {
    return ExtensionContext.Namespace.create(this.getClass(), ctx);
  }
}
