/*
 * FileUtilities.java
 *
 * Copyright (C) 2003 Robert McKinnon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.delineate.utility;

import net.sourceforge.jiu.codecs.CodecMode;
import net.sourceforge.jiu.codecs.PNMCodec;
import net.sourceforge.jiu.color.promotion.PromotionRGB24;
import net.sourceforge.jiu.color.reduction.RGBToGrayConversion;
import net.sourceforge.jiu.color.reduction.ReduceToBilevelThreshold;
import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.gui.awt.ToolkitLoader;
import net.sourceforge.jiu.ops.OperationFailedException;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * File helper methods.
 * @author robmckinnon@users.sourceforge.net
 */
public class FileUtilities {

//    static {
//        Properties properties = System.getProperties();
//        Enumeration enumeration = properties.propertyNames();
//        while(enumeration.hasMoreElements()) {
//            String name = (String)enumeration.nextElement();
//            System.out.println(name + " " + properties.get(name));
//        }
//    }

    public static String getUri(String filePath) {
        if(filePath.startsWith(".")) {
            String directory = System.getProperty("user.dir");
            filePath = directory + filePath.substring(1);
            System.out.println("fille " + filePath);
        }
        return "file:" + filePath;
    }

    public static File getFile(String uri) {
        String pathname = uri.substring(uri.indexOf(':')  + 1);
        File file = new File(pathname);
        return file;
    }

    public static String getFileSize(File file) {
        String size;

        long bytes = file.length();

        if(bytes == 0) {
           size = "";
        } else if(bytes < 1024) {
            size = bytes + "b";
        } else {
            float kb = bytes / 1024F;

            if(kb < 1024) {
                kb = Math.round(kb * 10) / 10F;
                size = kb + "kb";
            } else {
                float mb = kb / 1024;
                mb = Math.round(mb * 10) / 10F;

                size = mb + "mb";
            }
        }
        return size;
    }

    public static String normalizeFileName(String value) {
        if(value.indexOf(' ') != -1) {
            value = '"' + value + '"';
        }

        return value;
    }

    public static void copy(File inputFile, File outputFile) throws IOException {
        FileReader in = new FileReader(inputFile);
        FileWriter out = new FileWriter(outputFile);
        int c;

        while((c = in.read()) != -1) {
            out.write(c);
        }

        in.close();
        out.close();
    }

    public static boolean inBmpFormat(String ext) {
        return ext.equalsIgnoreCase("bmp");
    }

    public static boolean inPnmFormat(String ext) {
        return ext.equalsIgnoreCase("pnm") // any file
                || ext.equalsIgnoreCase("pbm") // black and white
                || ext.equalsIgnoreCase("pgm") // grey scale
                || ext.equalsIgnoreCase("ppm"); // other palette
    }

    public static boolean inPbmFormat(String ext) {
        return ext.equalsIgnoreCase("pbm");
    }

    public static String getExtension(File file) {
        String ext = "";
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if(index != -1 && name.length() >= index) {
            ext = name.substring(index + 1);
        }
        return ext;
    }

    public static File convertToPnm(File file) throws IOException, OperationFailedException {
        PixelImage pixelImage = ToolkitLoader.loadViaToolkitOrCodecs(file.getPath(), true, null);
        PNMCodec codec = new PNMCodec();
        String extension = codec.suggestFileExtension(pixelImage);
        File outputFile = new File(file.getParent(), file.getName() + extension);
        codec.setImage(pixelImage);
        codec.setFile(outputFile, CodecMode.SAVE);
        codec.process();
        codec.close();

        return outputFile;
    }

    public static Dimension getDimension(File file) throws IOException, OperationFailedException {
        PNMCodec codec = new PNMCodec();
        codec.setFile(file, CodecMode.LOAD);
        codec.process();
        PixelImage image = codec.getImage();
        return new Dimension(image.getWidth(), image.getHeight());
    }

    public static File convertToPbm(File inputFile, int thresholdPercent) throws IOException, OperationFailedException  {
        String extension = getExtension(inputFile);
        if(inPbmFormat(extension)) {
            return inputFile;
        }

        File file = inPnmFormat(extension) ? inputFile : convertToPnm(inputFile);

        PNMCodec codec = new PNMCodec();
        codec.setFile(file, CodecMode.LOAD);
        codec.process();
        PixelImage image = codec.getImage();

        if(!(image instanceof RGB24Image)) {
            PromotionRGB24 promoter = new PromotionRGB24();
            promoter.setInputImage(image);
            promoter.process();
        }

        oldCode();
        image = convertGreyToBilevel(convertRgbToGrey((RGB24Image)image), thresholdPercent);

        codec = new PNMCodec();
        codec.setImage(image);
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.')) + ".pbm";
        File outputFile = new File(file.getParent(), name);
        codec.setFile(outputFile, CodecMode.SAVE);
        codec.process();
        codec.close();

        return outputFile;
    }

    private static void oldCode() {
//        MedianCutQuantizer quantizer = new MedianCutQuantizer();
//        quantizer.setInputImage(image);
//        quantizer.setMethodToDetermineRepresentativeColors(MedianCutQuantizer.METHOD_REPR_COLOR_WEIGHTED_AVERAGE);
//        quantizer.setPaletteSize(2);
//        quantizer.process();
//        image = quantizer.getOutputImage();
//
//        if(image instanceof RGB24Image || image instanceof Paletted8Image) {
//            image = convertRgbToGrey(image);
//        }
    }

    private static PixelImage convertGreyToBilevel(GrayIntegerImage image, int thresholdPercent) throws OperationFailedException  {
        ReduceToBilevelThreshold reducer = new ReduceToBilevelThreshold();
        reducer.setInputImage(image);
        reducer.setThreshold(image.getMaxSample(0) * thresholdPercent / 100);
        reducer.process();

        return reducer.getOutputImage();
    }

    private static GrayIntegerImage convertRgbToGrey(RGB24Image image) throws OperationFailedException  {
        RGBToGrayConversion rgbtogray = new RGBToGrayConversion();
        rgbtogray.setInputImage(image);
//        rgbtogray.setColorWeights(0.33f, 0.33f, 0.33f);
        rgbtogray.process();
        return (GrayIntegerImage)rgbtogray.getOutputImage();
    }

}