package com.example.bikefalldetection;

import java.util.Stack;

public class FixedStack<T> extends Stack<T> {
    private final int size;

    public FixedStack(int stackSize){
        super();
        this.size = stackSize;
    }

    @Override
    public T push(T obj) {
        while(this.size() >= size) {
            this.remove(0);
        }
        return super.push(obj);
    }
}
