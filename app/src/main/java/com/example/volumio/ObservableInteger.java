package com.example.volumio;

/**
 * Created by Max on 18-05-2018.
 */

public class ObservableInteger {
    private OnIntegerChangeListener listener;

    private double value;

    public void SetOnIntegerChangeListener(OnIntegerChangeListener listener){
        this.listener = listener;
    }

    public double get(){
        return value;
    }

    public void set(double value){
        this.value = value;

        if (listener != null){
            listener.onIntegerChanged(value);
        }
    }
}
