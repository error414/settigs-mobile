package com.lib.pngj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ar.com.hjg.pngj.BufferedStreamFeeder;
import ar.com.hjg.pngj.ChunkReader;
import ar.com.hjg.pngj.PngHelperInternal;
import ar.com.hjg.pngj.chunks.PngChunkTEXT;

public class InsertChunk {
    private BufferedStreamFeeder streamFeeder;
    private OutputStream os;
    private ChunkSeqBasic cs;


    private HashMap<String, String> chunks = new HashMap<String, String>();

    /**
     *
     * @param inputStream
     * @param osx
     */
    public InsertChunk(InputStream inputStream, OutputStream osx) {
        streamFeeder    = new BufferedStreamFeeder(inputStream);
        os              = osx;

        cs = new ChunkSeqBasic(false) {
            @Override
            protected void postProcessChunk(ChunkReader chunkR) {
                super.postProcessChunk(chunkR);
                chunkR.getChunkRaw().writeChunk(os); // send the chunk straight to the os
            }

            @Override
            protected void startNewChunk(int len, String nextChukn, long offset) {
                super.startNewChunk(len, nextChukn, offset);
                if (nextChukn.equals("IEND")) { // insert
                    PngChunkTEXT t = new PngChunkTEXT(null);

                    Iterator it = chunks.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        t.setKeyVal((String)pair.getKey(), (String)pair.getValue());
                        t.createRawChunk().writeChunk(os);
                    }
                }
            }
        };
    }

    /**
     *
     * @param key
     * @param value
     */
    public void addChunk(String key, String value){
        chunks.put(key, value);
    }

    /**
     *
     * @return
     */
    public boolean save(){
        PngHelperInternal.writeBytes(os, PngHelperInternal.getPngIdSignature());
        while (streamFeeder.hasMoreToFeed()) {
            // async feeding
            streamFeeder.feed(cs);
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}