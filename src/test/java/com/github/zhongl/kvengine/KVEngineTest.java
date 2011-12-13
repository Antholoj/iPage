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

package com.github.zhongl.kvengine;

import com.github.zhongl.buffer.CommonAccessors;
import com.github.zhongl.index.Md5Key;
import com.github.zhongl.util.FileBase;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class KVEngineTest extends FileBase {

    protected BlockingKVEngine<String> engine;

    @Test
    public void putAndGetAndRemove() throws Exception {
        dir = testDir("putAndGetAndRemove");
        newEngineAndStartup();

        String value = "value";
        Md5Key key = Md5Key.generate(value.getBytes());

        assertThat(engine.put(key, value), is(nullValue()));
        assertThat(engine.get(key), is(value));
        assertThat(engine.remove(key), is(value));
    }

    private void newEngineAndStartup() throws IOException {
        engine = new BlockingKVEngine<String>(KVEngine.<String>baseOn(dir).valueAccessor(CommonAccessors.STRING).build());
        engine.startup();
    }

    @Test
    public void getAndRemoveNonExistKey() throws Exception {
        dir = testDir("getAndRemoveNonExistKey");
        newEngineAndStartup();
        Md5Key key = Md5Key.generate("non-exist".getBytes());
        assertThat(engine.get(key), is(nullValue()));
        assertThat(engine.remove(key), is(nullValue()));
    }

    @Test
    public void iterator() throws Exception {
        dir = testDir("valueIterator");

        newEngineAndStartup();

        String value0 = "value0";
        engine.put(Md5Key.generate(value0.getBytes()), value0);
        String value1 = "value1";
        engine.put(Md5Key.generate(value1.getBytes()), value1);
        String value2 = "value2";
        engine.put(Md5Key.generate(value2.getBytes()), value2);


        String value3 = "value3";
        engine.put(Md5Key.generate(value0.getBytes()), value3);
        engine.remove(Md5Key.generate(value1.getBytes()));

        Iterator<String> iterator = engine.valueIterator();

        assertThat(iterator.next(), is(value2));
        assertThat(iterator.next(), is(value3));
        assertThat(iterator.hasNext(), is(false));
    }

    @After
    public void tearDown() throws Exception {
        if (engine != null) engine.shutdown();
    }
}
