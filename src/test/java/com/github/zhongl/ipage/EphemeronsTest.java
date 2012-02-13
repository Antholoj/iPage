package com.github.zhongl.ipage;

import com.github.zhongl.util.*;
import com.google.common.util.concurrent.FutureCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class EphemeronsTest {
    SecondLevelStore secondLevelStore;
    Ephemerons4T ephemerons4T;

    @Before
    public void setUp() throws Exception {
        secondLevelStore = spy(new SecondLevelStore());
        ephemerons4T = new Ephemerons4T();
    }

    @Test
    public void phantom() throws Exception {
        Key key = key(1);
        ephemerons4T.add(key, 1, FutureCallbacks.<Void>ignore());
        ephemerons4T.remove(key);
        ephemerons4T.flush();
        verify(secondLevelStore, never()).merge(any(Collection.class), any(Collection.class));
    }

    @Test
    public void flowControlAndOrdering() throws Exception {

        CallbackFuture<Void> future = null;
        for (int i = 0; i < 8; i++) {
            try {
                future = new CallbackFuture<Void>();
                ephemerons4T.add(key(i), i, future);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ephemerons4T.flush();
        future.get();

        ArgumentCaptor<Collection> appendingsCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection> removingsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(secondLevelStore, times(2)).merge(appendingsCaptor.capture(), removingsCaptor.capture());

        // verify the ordering
        int i = 0;

        for (Collection allValue : appendingsCaptor.getAllValues()) {
            for (Object o : allValue) {
                Entry<Key, Integer> entry = (Entry<Key, Integer>) o;
                assertThat(entry.value(), is(i++));
            }
        }

    }

    private Key key(int i) {return new Key(Md5.md5((i + "").getBytes()));}

    class SecondLevelStore {
        final Map<Key, Integer> secondLevel = new ConcurrentSkipListMap<Key, Integer>();

        public Integer get(Key key) {
            return secondLevel.get(key);
        }

        public void merge(Collection<Entry<Key, Integer>> appendings, Collection<Key> removings) {
            waitFor(10L); // mock long time flushing
            for (Entry<Key, Integer> entry : appendings) {
                if (entry.value() != Nils.OBJECT)
                    secondLevel.put(entry.key(), entry.value());
            }
            for (Key key : removings) {
                secondLevel.remove(key);
            }
        }
    }

    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class Ephemerons4T extends Ephemerons<Integer> {

        protected Ephemerons4T() {
            super(new ConcurrentHashMap<Key, Record>());
            throughout(4);
        }

        @Override
        protected void requestFlush(Collection<Entry<Key, Integer>> appendings, Collection<Key> removings, FutureCallback<Void> flushedCallback) {
            secondLevelStore.merge(appendings, removings);
            flushedCallback.onSuccess(Nils.VOID);
        }

        @Override
        protected Integer getMiss(Key key) {
            return secondLevelStore.get(key);
        }

    }
}
