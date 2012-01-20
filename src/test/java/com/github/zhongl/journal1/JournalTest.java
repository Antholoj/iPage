/*
 * Copyright 2012 zhongl
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

package com.github.zhongl.journal1;

import com.github.zhongl.codec.*;
import com.github.zhongl.util.FileBase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class JournalTest extends FileBase {

    @Test
    public void usage() throws Exception {
        dir = testDir("usage");

        Applicable applicable = mock(Applicable.class);

        Codec codec = ComposedCodecBuilder.compose(new CompoundCodec(new StringCodec()))
                .with(ChecksumCodec.class)
                .with(LengthCodec.class)
                .build();

        Pages pages = mock(Pages.class);
        
        Journal journal = new Journal(pages);

        journal.append("1", false);
        journal.erase(journal.append("2", false));
        journal.append("3", true);

        journal.close(); // mock crash

        journal = new Journal(pages);

        journal.recover(applicable);

        ArgumentCaptor<Record> captor = ArgumentCaptor.forClass(Record.class);
        verify(applicable, times(1)).apply(captor.capture());
        Record record = captor.getValue();
        assertThat(record.offset(), is(0L));
        assertThat(record.<String>content(), is("3"));
        journal.close();
    }

}