package com.github.zhongl.ex.cycle;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class BitmapTest {

    @Test
    public void usage() throws Exception {
        Bitmap bitmap = new Bitmap(16);

        bitmap.set(0, 3);

        assertThat(bitmap.nextClearBit(0), is(3));

        bitmap.reset();

        assertThat(bitmap.nextClearBit(0), is(0));
    }
}
