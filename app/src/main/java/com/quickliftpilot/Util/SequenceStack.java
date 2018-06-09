package com.quickliftpilot.Util;

import android.util.Log;

import com.quickliftpilot.model.SequenceModel;

import java.util.Stack;

/**
 * Created by pandey on 1/4/18.
 */

public class SequenceStack{
    static Stack<SequenceModel> seqStack = null;

    public Stack<SequenceModel> getStack(){
        if (seqStack == null){
            seqStack = new Stack<>();
        }
        return seqStack;
    }

    private void pushData(SequenceModel model){
        if (seqStack.capacity() >= 6){
            Log.i("TAG","Stack full");
        }else {
            seqStack.push(model);
        }
    }

    private SequenceModel popData(){
        SequenceModel model = null;
        if (seqStack.isEmpty()){
            Log.i("TAG", "Stack is empty");
        }else {
            model = seqStack.pop();
        }
        return model;
    }
}
