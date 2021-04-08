package com.qb.zip.controller;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zoulinjun
 * @title: ZipUtil
 * @projectName zip
 * @description: TODO
 * @date 2021/2/2 13:37
 */
@Slf4j
public class ZipUtil {
    public static void compress(File file) {
        if (file == null) {
            log.info("the file to be compressed should not be null;");
            return;
        }
        if (!file.exists()) {
            log.info("the file to be compressed named:" + file + " does not exist.");
            return;
        }
        try {
            if (file.length() >= (1024 * 1024)) {//超过1M
                if (file.getName().toLowerCase().endsWith(".png")) {
                    Thumbnails.of(file)
                            .scale(0.5f) .outputQuality(1f).toFile(file);
                } else {
                    //检查是否有GPS信息
                    TiffOutputSet outPutSet = getGpsOutPutSet(file);
                    if (outPutSet != null) {
                        Thumbnails.of(file).scale(0.25f) .outputQuality(1f).toFile(file);
                        byte[] fileBytes = toByteArray(file);
                        FileOutputStream os = new FileOutputStream(file);
                        new ExifRewriter().updateExifMetadataLossless(fileBytes, os,
                                outPutSet);//将GPS信息写回压缩后的图片中
                    } else {
                        Thumbnails.of(file).scale(0.25f) .outputQuality(1f).toFile(file);
                    }
                }
            }
        } catch (IOException | ImageReadException | ImageWriteException e) {
            e.printStackTrace();
        }
    }


    private static byte[] toByteArray(File file) throws IOException {
        FileInputStream fs = null;
        FileChannel channel = null;
        try {
            fs = new FileInputStream(file);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while ((channel.read(byteBuffer)) > 0) {
            }

            return byteBuffer.array();
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void compressAndCopyFile(File srcfile, File destfile) {
        if (srcfile == null) {
            log.info("the file to be compressed should not be null;");
            return;
        }
        if (!srcfile.exists()) {
            log.info("the file to be compressed named:" + srcfile + " does not exist.");
            return;
        }
        try {
            if (srcfile.length() >= (1024 * 1024)) {//超过1M
                if (srcfile.getName().toLowerCase().endsWith(".png")) {
                    Thumbnails.of(srcfile)
                            .scale(0.5f) .outputQuality(1f).toFile(destfile);
                } else {
                    //检查是否有GPS信息
                    TiffOutputSet outPutSet = getGpsOutPutSet(srcfile);
                    if (outPutSet != null) {
                        Thumbnails.of(srcfile).scale(0.25f).outputQuality(1f).toFile(destfile);
                        byte[] fileBytes = toByteArray(destfile);
                        FileOutputStream os = new FileOutputStream(destfile);
                        new ExifRewriter().updateExifMetadataLossless(fileBytes, os,
                                outPutSet);//将GPS信息写回压缩后的图片中
                    } else {
                        Thumbnails.of(srcfile).scale(0.25f) .outputQuality(1f).toFile(destfile);
                    }
                }
            } else {
                fileCopyWithFileChannel(srcfile, destfile);
            }
        } catch (IOException | ImageReadException | ImageWriteException e) {
            e.printStackTrace();
        }
    }

    public static void fileCopyWithFileChannel(File fromFile, File toFile) {
        try (// 得到fileInputStream的文件通道
             FileChannel fileChannelInput = new FileInputStream(fromFile).getChannel();
             // 得到fileOutputStream的文件通道
             FileChannel fileChannelOutput = new FileOutputStream(toFile).getChannel()) {

            //将fileChannelInput通道的数据，写入到fileChannelOutput通道
            fileChannelInput.transferTo(0, fileChannelInput.size(), fileChannelOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static TiffOutputSet getGpsOutPutSet(File file) {
        TiffOutputSet outputSet = null;
        try {
            IImageMetadata metadata = Sanselan.getMetadata(file);
            JpegImageMetadata jpegMetadata = null;
            if (metadata != null) {
                jpegMetadata = (JpegImageMetadata) metadata;
                TiffImageMetadata exif = jpegMetadata.getExif();
                if (null != exif) {
                    outputSet = exif.getOutputSet();
                    if (outputSet != null) {
                        TiffImageMetadata.GPSInfo gpsInfo = exif.getGPS();
                        if (null != gpsInfo) {
                            double longitude = gpsInfo
                                    .getLongitudeAsDegreesEast();
                            double latitude = gpsInfo
                                    .getLatitudeAsDegreesNorth();
                            outputSet.setGPSInDegrees(longitude, latitude);
                        }
                    }
                }
            }
        } catch (IOException ie) {
        } catch (ImageReadException re) {
        } catch (ImageWriteException we) {

        }
        return outputSet;
    }
}
