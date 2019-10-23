package com.example.myapplication;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by weizheng.huang on 2019-10-09.
 */
public class MuxerMedia {
    private static MediaExtractor mediaExtractor;
    private static MediaMuxer mediaMuxer;

    public static  void divideMedia (@Nullable String srcPath,@Nullable String srcPath2, @Nullable String dstPath) throws IOException {
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
        ArrayList<String> videoPathList = new ArrayList<>();
        videoPathList.add(srcPath);
        videoPathList.add(srcPath2);
        Iterator iterator = videoPathList.iterator();
        long timeV = 0;
        long timeA = 0;
        Log.d("tag",videoPathList.size()+"");

        while(iterator.hasNext()) {
            String videoPath =(String) iterator.next();
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(videoPath);
            mediaExtractor.selectTrack(videoIndex);
            timeV = writeIntoBuf(inputSize, mediaExtractor, mediaMuxer, videoTrack, videoIndex, timeV);
            mediaExtractor.selectTrack(audioIndex);
            timeA = writeIntoBuf(inputSize, mediaExtractor, mediaMuxer, audioTrack, audioIndex, timeA);
        }

        mediaExtractor.release();
        mediaMuxer.stop();
        mediaMuxer.release();
    }

    private static long writeIntoBuf(int INPUTSIZE, @Nullable MediaExtractor extractor, @Nullable MediaMuxer mediaMuxer, int  track, int index, long runtime){

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
           // MediaCodec.BUFFER_FLAG_PARTIAL_FRAME;
            info.flags = extractor.getSampleFlags();
            info.offset = 0;
            info.presentationTimeUs =extractor.getSampleTime()+runtime;
            info.size = size;
            mediaMuxer.writeSampleData(track, byteBuffer, info);

            extractor.advance();
        }

        return info.presentationTimeUs;

    }

}
