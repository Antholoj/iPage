package com.github.zhongl.journal1;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface OverflowCallback<T> {
    void onOverflow(T rest, boolean force);
}
