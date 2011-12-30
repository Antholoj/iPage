/*
 * Copyright 2011 zhongl
 *
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

package com.github.zhongl.index;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.github.zhongl.page.Accessors.LONG;


/**
 * {@link Slot} = {@link State} {@link Md5Key} {@link Long}
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
public class Slot {

    public static final int LENGTH = 1 + Md5Key.BYTE_LENGTH + 8;

    private final int beginPosition;
    private final FileChannel channel;

    public Slot(int beginPosition, FileChannel channel) {
        this.beginPosition = beginPosition;
        this.channel = channel;
    }

    public State state() throws IOException {
        return State.readFrom(channel, beginPosition);
    }

    public Md5Key key() throws IOException {
        channel.position(beginPositionOfKey());
        return Md5Key.ACCESSOR.reader().readFrom(channel);
    }

    public Long offset() throws IOException {
        channel.position(beginPositionOfOffset());
        return LONG.reader().readFrom(channel);
    }

    Long add(Md5Key key, Long offset) throws IOException {
        State.OCCUPIED.writeTo(channel, beginPosition);
        Md5Key.ACCESSOR.writer(key).writeTo(channel);
        LONG.writer(offset).writeTo(channel);
        return null;
    }

    private int beginPositionOfOffset() {return beginPosition + 1 + Md5Key.BYTE_LENGTH;}

    private int beginPositionOfKey() {return beginPosition + 1;}

    Long replace(Md5Key key, Long offset) throws IOException {
        Long previous = offset();
        add(key, offset);
        return previous;
    }

    Long release() throws IOException {
        State.RELEASED.writeTo(channel, beginPosition);
        return offset();
    }

    enum State {
        EMPTY, OCCUPIED, RELEASED;

        public void writeTo(FileChannel channel, int offset) throws IOException {
            channel.position(offset);
            channel.write(ByteBuffer.wrap(new byte[] {(byte) ordinal()}));
        }

        public static State readFrom(FileChannel channel, int offset) throws IOException {
            channel.position(offset);
            byte[] bytes = new byte[1];
            channel.read(ByteBuffer.wrap(bytes));
            if (EMPTY.ordinal() == bytes[0]) return EMPTY;
            if (OCCUPIED.ordinal() == bytes[0]) return OCCUPIED;
            if (RELEASED.ordinal() == bytes[0]) return RELEASED;
            throw new IllegalStateException("Unknown slot state: " + bytes[0]);
        }
    }

}
