package com.di7ak.kolyaloh.anticaptcha;

import android.graphics.Bitmap;

import com.di7ak.nn.Layer;
import com.di7ak.util.Array;

public class Anticaptcha {

    public static String handle(Bitmap source, Layer layer) throws AnticaptchaException {
        if (source == null) throw new AnticaptchaException("source image cannot be null");
        if(source.getWidth() < 20 || source.getHeight() < 20) throw new AnticaptchaException("min image size 20x20");
        source = Bitmap.createBitmap(source, 2, 2, source.getWidth() - 4, source.getHeight() - 4);

        Bitmap[] images = split(source);

        if(images.length != 4) throw new AnticaptchaException();

        String result = "";

        for (int i = 0; i < images.length; i++) {
            float[] input = new float[12 * 18];
            for(int x = 0; x < images[i].getWidth(); x ++)
                for(int y = 0; y < images[i].getHeight(); y ++)
                    input[x * 12 + y] = images[i].getPixel(x, y) > 0xff777777 ? 1 : 0;

            if(layer.links == null) {
                layer.links = new int[1];
            }
            int r = layer.post(input, false);
            if(r >= layer.links.length) {
                int[] buff = new int[r + 1];
                System.arraycopy(layer.links, 0, buff, 0, layer.links.length);
                layer.links = buff;
                layer.links[r] = 0;
            }
            result += layer.links[r];
        }

        return result;
    }

    public static Bitmap[] split(Bitmap source) {
        Bitmap[] result = new Bitmap[0];
        int[] colors = new int[256];
        for (int x = 0; x < source.getWidth(); x++)
            for (int y = 0; y < source.getHeight(); y++)
                colors[(((source.getPixel(x, y) >> 16) & 0xff) + ((source.getPixel(x, y) >> 8) & 0xff) + (source.getPixel(x, y) & 0xff)) / 3]++;

        int start = -1;
        for (int x = 0; x < source.getWidth(); x++) {
            boolean sc = false;
            for (int y = 0; y < source.getHeight(); y++)
                if (colors[(((source.getPixel(x, y) >> 16) & 0xff) + ((source.getPixel(x, y) >> 8) & 0xff) + (source.getPixel(x, y) & 0xff)) / 3] < 100) {
                    sc = true;
                    if (start == -1) start = x;
                }
            if (!sc && start != -1 && x - start > 8) {
                Bitmap[] buff = new Bitmap[result.length + 1];
                System.arraycopy(result, 0, buff, 0, result.length);
                result = buff;
                result[result.length - 1] = Bitmap.createScaledBitmap(toMono(trim(Bitmap.createBitmap(source, start, 0, x - start, source.getHeight()), colors), colors), 12, 18, false);
                start = -1;
            }
        }
        return result;
    }

    public static Bitmap trim(Bitmap source, int[] colors) {
        Bitmap result = source;

        int start = -1;
        int offset = 0;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++)
                if (colors[(((source.getPixel(x, y) >> 16) & 0xff) + ((source.getPixel(x, y) >> 8) & 0xff) + (source.getPixel(x, y) & 0xff)) / 3] < 100) {
                    if (start == -1) start = y;
                    else offset = y;
                }
        }
        return Bitmap.createBitmap(source, 0, start, source.getWidth(), offset - start);
    }

    private static Bitmap toMono(Bitmap source, int[] colors) {
        for (int x = 0; x < source.getWidth(); x++)
            for (int y = 0; y < source.getHeight(); y++) {
                if ((((source.getPixel(x, y) >> 16) & 0xff) + ((source.getPixel(x, y) >> 8) & 0xff) + (source.getPixel(x, y) & 0xff)) / 3 != Array.maxIndex(colors))
                    source.setPixel(x, y, 0xffffffff);
                else source.setPixel(x, y, 0xff000000);
            }
        return source;
    }

}