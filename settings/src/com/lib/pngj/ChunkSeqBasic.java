package com.lib.pngj;

import ar.com.hjg.pngj.ChunkReader;
import ar.com.hjg.pngj.ChunkSeqReader;

public class ChunkSeqBasic extends ChunkSeqReader {
    public boolean checkCrc = true;

    public ChunkSeqBasic(boolean checkCrc) {
        this.checkCrc = checkCrc;
    }

    @Override
    protected void postProcessChunk(ChunkReader chunkR) {
        super.postProcessChunk(chunkR);
    }

    @Override
    protected boolean isIdatKind(String id) {
        return false;
    }

    @Override
    protected boolean shouldCheckCrc(int len, String id) {
        return checkCrc;
    }

}