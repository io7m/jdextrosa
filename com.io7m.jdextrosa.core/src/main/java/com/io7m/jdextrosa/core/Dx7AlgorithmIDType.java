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

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.RangeInclusiveI;
import org.immutables.value.Value;

/**
 * The numeric identifier of the algorithm used for a voice. The valid range
 * of algorithms on a DX7 is {@code [1, 32]}.
 */

@ImmutablesStyleType
@Value.Immutable
public interface Dx7AlgorithmIDType extends Comparable<Dx7AlgorithmIDType>
{
  /**
   * @return The raw integer ID
   */

  @Value.Parameter
  int id();

  @Override
  default int compareTo(
    final Dx7AlgorithmIDType o)
  {
    return Integer.compare(this.id(), o.id());
  }

  /**
   * @return The algorithm index as it would appear in a packed SysEx structure
   */

  default int external()
  {
    return this.id() - 1;
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.id(),
      "Algorithm",
      RangeInclusiveI.of(1, 32),
      "Valid algorithms");
  }
}
