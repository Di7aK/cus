package com.di7ak.nn;

public class Neurone {
    private float[] weights,
            input;

    public Neurone(int inputs) {
        weights = new float[inputs];
        input = new float[inputs];
    }

    public void setInput(float[] input) {
        if(weights.length != input.length) throw new IllegalArgumentException();
        this.input = input;
    }

    public float post() {
        float out = 0;
        float t = 0;
        for(int i = 0; i < input.length; i ++) {
            out += weights[i] * input[i];
            t += weights[i] * weights[i];
        }
        float res = out > t ? t / out : out / t;
        return res;
    }

    public void updateWeights() {
        for(int i = 0; i < input.length; i ++)
            //if(weights[i] == 0 && input[i] == 0) weights[i] = 0;
            weights[i] += 0.5f * (input[i] - weights[i]);
    }

    public void setWeights(float[] weights) {
        if(this.weights.length != weights.length) throw new IllegalArgumentException();
        this.weights = weights;
    }

    public float[] getWeights() {
        return weights;
    }

}
