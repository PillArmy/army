/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.transaction;

import io.army.option.Option;
import io.army.session.RmSession;
import io.army.spec.OptionSpec;

import io.army.lang.Nullable;
import java.util.function.Function;

/// 
/// XID consist of following three :
/// 
/// - {@link #getGtrid()}
/// - {@link #getBqual()}
/// - {@link #getFormatId()}
/// 
/// 
/// 
/// To be safe,{@link RmSession} **possibly**  write gtrid and bqual as hex strings. steps :
/// 
/// - Get byte[] with {@link java.nio.charset.StandardCharsets#UTF_8}
/// - write gtrid or bqual as hex strings
/// 
/// the conversion process of {@code  RmSession#recover(int, java.util.function.Function)} is the reverse of above.
/// Application developer can get the instance of {@link Xid} by {@link #from(String, String, int)} method.
/// @see Option#XID
/// @see RmSession
public interface Xid extends OptionSpec {


    /// 
    /// The global transaction identifier string
    /// 
    /// 
    /// 
    /// - Global transaction identifier must have text
    /// 
    /// 
    /// @return a global transaction identifier.
    String getGtrid();

    /// Obtain the transaction branch identifier part of XID as an string.
    /// 
    /// 
    /// - If non-null,branch transaction identifier must have text
    /// 
    /// 
    /// @return a branch qualifier
    @Nullable
    String getBqual();

    /// Obtain the format identifier part of the XID.
    /// @return Format identifier. O means the OSI CCR format.
    int getFormatId();

    /// The implementation of {@link Xid} must correctly override this method with only following three :
    /// 
    /// - {@link #getGtrid()}
    /// - {@link #getBqual()}
    /// - {@link #getFormatId()}
    /// 
    /// Like following :
    /// <pre>
    /// <code>
    /// &#64;Override
    /// public int hashCode() {
    /// return Objects.hash(this.gtrid, this.bqual, this.formatId);
    /// }
    /// </code>
    /// </pre>
    /// 
    @Override
    int hashCode();

    /// The implementation of {@link Xid} must correctly override this method with only following three :
    /// 
    /// - {@link #getGtrid()}
    /// - {@link #getBqual()}
    /// - {@link #getFormatId()}
    /// 
    /// 
    /// Like following :
    /// <pre>
    /// <code>
    /// &#64;Override
    /// public boolean equals(final Object obj) {
    /// final boolean match;
    /// if (obj == this) {
    /// match = true;
    /// } else if (obj instanceof Xid) {
    /// final Xid o = (Xid) obj;
    /// match = this.gtrid.equals(o.getGtrid())
    /// && Objects.equals(o.getBqual(), this.bqual)
    /// && o.getFormatId() == this.formatId;
    /// } else {
    /// match = false;
    /// }
    /// return match;
    /// }
    /// </code>
    /// </pre>
    /// 
    @Override
    boolean equals(Object obj);

    /// override {@link Object#toString()}
    /// @return xid info, contain :
    /// - class name
    /// - {@link #getGtrid()}
    /// - {@link #getBqual()}
    /// - {@link #getFormatId()}
    /// - dialect option if exists
    /// - {@link System#identityHashCode(Object)}
    /// 
    @Override
    String toString();

    /// {@code  RmSession#recover(int, java.util.function.Function) } maybe add some dialect value.
    /// @return null or dialect option value.
    @Nullable
    @Override
    <T> T valueOf(Option<T> option);

    /// Create one {@link Xid} instance.
    /// @param gtrid must have text
    /// @param bqual null or must have text
    /// @throws IllegalArgumentException throw when gtrid or bqual error.
    static Xid from(String gtrid, @Nullable String bqual, int formatId) {
        return ArmyXid.from(gtrid, bqual, formatId, Option.EMPTY_FUNC);
    }

    /// Create one {@link Xid} instance.
    /// @param gtrid must have text
    /// @param bqual null or must have text
    /// @throws IllegalArgumentException throw when gtrid or bqual error.
    /// @throws NullPointerException     throw when optionFunc is null
    static Xid from(String gtrid, @Nullable String bqual, int formatId, Function<Option<?>, ?> optionFunc) {
        return ArmyXid.from(gtrid, bqual, formatId, optionFunc);
    }

}
