package com.example.myapplication;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by weizheng.huang on 2019-09-27.
 */
//已完成：完成视频文件的解封装和封装拷贝
//已完成：完成视频文件的拼接
    //难点：
    //无法解决两份文件的读写
    //尝试：使用setdatasources进行两次解封装和合并，失败
    //尝试：直接进行两份数据的读写，耗时过长
    //尝试：使用线程完成两份数据的读写，耗时过长，主线程卡住。
    //尝试：读取最后一次的info可展示时间作为下一次读写数据的基点，成功
public class ExtractorMuxer  {
    private static MediaExtractor mediaExtractor;
    private static MediaMuxer mediaMuxer;
    public static  void divideMedia (@Nullable String srcPath, @Nullable String dstPath) throws IOException {
        mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(srcPath);
        mediaMuxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        int audioIndex = -1;
        int videoIndex = -1;

        for(int i =0;i<mediaExtractor.getTrackCount();i++){
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            if(format.getString(MediaFormat.KEY_MIME).startsWith("video")){
                videoIndex = i;
                continue;
            }
            if(format.getString(MediaFormat.KEY_MIME).startsWith("audio")){
                audioIndex = i;
            }
        }
        MediaFormat formatA = mediaExtractor.getTrackFormat(audioIndex);
        int audioTrack = mediaMuxer.addTrack(formatA);
        MediaFormat formatV = mediaExtractor.getTrackFormat(videoIndex);
        int videoTrack = mediaMuxer.addTrack(formatV);
        int width = formatV.getInteger(MediaFormat.KEY_WIDTH);
        int height = formatV.getInteger(MediaFormat.KEY_HEIGHT);
        width = width>0?width:1280;
        height = height>0?height:720;
        int inputSize = width*height*3;
        mediaMuxer.start();

        mediaExtractor.selectTrack(videoIndex);
        long timeV = writeIntoBuf(inputSize,mediaExtractor,mediaMuxer,videoTrack,videoIndex,0);
        mediaExtractor.selectTrack(audioIndex);
        long timeA = writeIntoBuf(inputSize,mediaExtractor,mediaMuxer,audioTrack,audioIndex,0);
/*
        mediaExtractor.selectTrack(videoIndex);
        writeIntoBuf(inputSize, mediaExtractor,mediaMuxer,videoTrack,videoIndex,timeV);
        mediaExtractor.selectTrack(audioIndex);
        writeIntoBuf(inputSize, mediaExtractor,mediaMuxer,audioTrack,audioIndex,timeA);
*/
        mediaExtractor.release();
        mediaMuxer.stop();
        mediaMuxer.release();
    }

    private static long writeIntoBuf(int INPUTSIZE, @Nullable MediaExtractor extractor,@Nullable MediaMuxer mediaMuxer,int  track,int index,long runtime){

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs =0;
            ByteBuffer byteBuffer = ByteBuffer.allocate(INPUTSIZE);
            while (true) {
                int size = extractor.readSampleData(byteBuffer, 0);
                if (size < 0) {
                    extractor.unselectTrack(index);
                    break;
                }

                if (index != extractor.getSampleTrackIndex())
                    continue;

                info.flags =MediaCodec.BUFFER_FLAG_KEY_FRAME;
                info.offset = 0;
                info.presentationTimeUs =extractor.getSampleTime()+runtime;
                info.size = size;
                mediaMuxer.writeSampleData(track, byteBuffer, info);

                extractor.advance();
            }

        return info.presentationTimeUs;

    }


}
