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

package com.io7m.jdextrosa.transforms;

import com.io7m.jdextrosa.core.DxImmutableStyleType;
import org.immutables.value.Value;

@DxImmutableStyleType
@Value.Immutable
public interface Dx7StaccatoParametersType
{
  @Value.Parameter
  AffectOperators affect();

  @Value.Parameter
  boolean modifyAttack();

  @Value.Parameter
  boolean modifyRelease();

  enum AffectOperators
  {
    AFFECT_CARRIERS,
    AFFECT_MODULATORS,
    AFFECT_ALL
  }
}
