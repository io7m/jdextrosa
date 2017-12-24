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

/**
 * DX7 Librarian (XML I/O SPI)
 */

module com.io7m.jdextrosa.io.xml.spi
{
  requires static org.immutables.value;
  requires static vavr.encodings;

  requires java.xml;

  requires com.io7m.jaffirm.core;
  requires com.io7m.jdextrosa.core;
  requires com.io7m.jlexing.core;
  requires com.io7m.jranges.core;
  requires com.io7m.junreachable.core;
  requires io.vavr;
  requires slf4j.api;

  exports com.io7m.jdextrosa.io.xml.spi;
}
