package com.di7ak.nn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.di7ak.util.Array;

public class Layer {
    private List<Neurone> neurons;
    private int inputs;
    public int[] links;

    public Layer(int inputs) {
        this.inputs = inputs;
        neurons = new ArrayList<Neurone>();
    }

    public int post(float[] input, boolean upd) {
        if(input.length != inputs) throw new IllegalArgumentException();
        float[] result = new float[0];
        float[] outs = new float[neurons.size()];
        for(int i = 0; i < neurons.size(); i ++) {
            neurons.get(i).setInput(input);
            outs[i] = neurons.get(i).post();
            //System.out.println(i + " " + (int)(outs[i] * 100) + "%");
        }
        int maxIndex = Array.maxIndex(outs);
        if(outs.length == 0 || outs[maxIndex] < 0.95 && upd) {
            maxIndex = neurons.size();
            Neurone n = new Neurone(input.length);
            n.setWeights(input);
            neurons.add(n);
        }// else neurons.get(maxIndex).updateWeights();
        return maxIndex;
    }

    public List<Neurone> getNeurons() {
        return neurons;
    }

    public void save(OutputStream os) {
        String s = neurons.size() + ":" + neurons.get(0).getWeights().length + "\n";
        try {
            os.write(s.getBytes());
            s = "";
            for(int i = 0; i < links.length; i ++) {
                s += links[i] + ":";
            }
            s += "\n";
            os.write(s.getBytes());
            for(int i = 0; i < neurons.size(); i ++) {
                for(int j = 0; j < neurons.get(i).getWeights().length; j ++) {
                    os.write((Float.toString(neurons.get(i).getWeights()[j]) + "\n").getBytes());
                }
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void load(InputStream is) {
        int c = 0;
        String buffer = "";
        boolean h = true, l = true;;
        neurons = new ArrayList<Neurone>();
        Neurone n;
        int nid = 0;
        int ncid = 0;
        float[][] weights = null;
        try {
            while((c = is.read()) != -1) {
                if(c == 10) {
                    if(h) {
                        h = false;
                        weights = new float[(int)Integer.valueOf(buffer.split(":")[0])][(int)Integer.valueOf(buffer.split(":")[1])];
                    } else if(l) {
                        l = false;
                        String[] lnk = buffer.split(":");
                        links = new int[lnk.length];
                        for(int i = 0; i < lnk.length; i ++)
                            links[i] = Integer.valueOf(lnk[i]);
                    } else {
                        weights[nid][ncid] = Float.valueOf(buffer);
                        ncid ++;
                        if(ncid >= weights[nid].length) {
                            ncid = 0;
                            nid ++;
                        }
                    }
                    buffer = "";
                } else buffer += (char)c;
            }
            for(int i = 0; i < weights.length; i ++) {
                n = new Neurone(weights[i].length);
                n.setWeights(weights[i]);
                neurons.add(n);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int getInputs() {
        return inputs;
    }

    public void setNeurons(List<Neurone> neurons) {
        this.neurons = neurons;
    }

}
