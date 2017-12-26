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

package com.io7m.jdextrosa.core;

import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.RangeInclusiveI;
import org.immutables.value.Value;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * A named voice.
 */

@DxImmutableStyleType
@Value.Immutable
public interface Dx7VoiceNamedType
{
  /**
   * @return The name of the voice
   */

  @Value.Parameter
  String name();

  /**
   * @return The voice parameters
   */

  @Value.Parameter
  Dx7Voice voice();

  /**
   * @return Optional metadata for the voice
   */

  @Value.Parameter
  Optional<Dx7VoiceMetadata> metadata();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.name().getBytes(StandardCharsets.US_ASCII).length,
      "Name length",
      RangeInclusiveI.of(0, 10),
      "Valid name lengths");
  }
}
