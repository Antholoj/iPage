/*
 * Copyright 2012 zhongl
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.zhongl.ipage;

import com.github.zhongl.util.DirectByteBufferCleaner;
import com.github.zhongl.util.Entry;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkState;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class LineEntryCodec<V> {
    public static final int KEY_AND_LEN = Key.BYTE_LENGTH + 4;
    private final Codec<V> vCodec;

    LineEntryCodec(Codec<V> vCodec) {
        this.vCodec = vCodec;
    }

    public ByteBuffer encode(Entry<Key, V> entry) {
        ByteBuffer buffer = vCodec.encode(entry.value());
        int length = buffer.remaining();
        ByteBuffer aggregated = ByteBuffer.allocate(KEY_AND_LEN + length);
        return (ByteBuffer) aggregated.put(entry.key().bytes())
                                      .putInt(length)
                                      .put(buffer)
                                      .flip();

    }

    public LazyDecoder<V> lazyDecoder(final ByteBuffer buffer) {
        checkState(DirectByteBufferCleaner.isNotCleaned(buffer));
        int from = buffer.position();
        int length = ((ByteBuffer) buffer.position(from + Key.BYTE_LENGTH)).getInt();
        int to = buffer.position() + length;

        buffer.position(to);

        final ByteBuffer origin = ((ByteBuffer) buffer.duplicate().limit(to).position(from)).slice();
        return new LazyDecoder() {

            @Override
            public Key key() {
                checkState(DirectByteBufferCleaner.isNotCleaned(buffer));
                byte[] bytes = new byte[Key.BYTE_LENGTH];
                origin.position(0);
                origin.get(bytes);
                return new Key(bytes);
            }

            public V value() {
                checkState(DirectByteBufferCleaner.isNotCleaned(buffer));
                origin.position(KEY_AND_LEN);
                return vCodec.decode(origin);
            }

            @Override
            public ByteBuffer origin() {
                checkState(DirectByteBufferCleaner.isNotCleaned(buffer));
                return (ByteBuffer) origin.position(0);
            }
        };
    }

    interface LazyDecoder<V> {
        Key key();

        V value();

        ByteBuffer origin();
    }
}
