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

import net.sourceforge.jiu.gui.awt.ToolkitLoader;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.codecs.PNMCodec;
import net.sourceforge.jiu.codecs.CodecMode;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

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

    public static String getExtension(File file) {
        String ext = "";
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if(index != -1 && name.length() >= index) {
            ext = name.substring(index + 1);
        }
        return ext;
    }

    public static File convertToPnm(File file) throws Exception {
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
/*
    public static File convertToPnm(File file, String extension) {
        String conversionProgram = null;
        if(extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
            conversionProgram = "jpegtopnm";
        } else if(extension.equalsIgnoreCase("png")) {
            conversionProgram = "pngtopnm";
        } else if(extension.equalsIgnoreCase("gif")) {
            conversionProgram = "giftopnm";
        }

        if(conversionProgram != null) {
            try {
                String name = file.getName().split("[.]")[0] + ".pnm";
                File newFile = new File(file.getParent(), name);
//                String[] commandArray = {"cat", file.getPath(), "|", conversionProgram, ">", newFile.getPath()};
                String[] commandArray = {conversionProgram, file.getPath()};
//                String command = "cat \"" + file.getPath() + "\" | " + conversionProgram + " > \"" + newFile.getPath() + "\"";

                BufferedReader reader = RuntimeUtility.execute(commandArray);
                BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
                while(reader.ready()) {
                    writer.write(reader.readLine());
                    writer.newLine();
                }
                reader.close();
                writer.close();
                return newFile;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Cannot convert file with extension " + extension);
        }
    }
*/

}