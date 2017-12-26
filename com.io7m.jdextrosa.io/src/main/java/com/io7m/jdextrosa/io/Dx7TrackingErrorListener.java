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

package com.io7m.jdextrosa.io;

import java.util.Objects;

final class Dx7TrackingErrorListener implements Dx7ParseErrorListenerType
{
  private final Dx7ParseErrorListenerType delegate;
  private boolean warning;
  private boolean error;

  Dx7TrackingErrorListener(
    final Dx7ParseErrorListenerType in_delegate)
  {
    this.delegate = Objects.requireNonNull(in_delegate, "Delegate");
  }

  void reset()
  {
    this.warning = false;
    this.error = false;
  }

  @Override
  public void receiveError(
    final Dx7ParseError in_error)
  {
    switch (in_error.severity()) {
      case WARNING: {
        this.warning = true;
        break;
      }
      case ERROR: {
        this.error = true;
        break;
      }
    }

    this.delegate.receiveError(in_error);
  }

  public boolean errorsEncountered()
  {
    return this.error;
  }
}
