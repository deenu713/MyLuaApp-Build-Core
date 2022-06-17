/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dingyi.terminal.virtualprocess;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Simple alternative to JDK {@link PipedOutputStream};
 * @see ByteQueueInputStream
 * @since 2.9.0
 */
public class ByteQueueOutputStream extends OutputStream {

     final ByteQueue blockingQueue;


    /**
     * Constructs a new instance with no limit to internal buffer size.
     */
    public ByteQueueOutputStream() {
        this(new ByteQueue(1024));
    }

    /**
     * Constructs a new instance with given buffer.
     *
     * @param blockingQueue backing queue for the stream
     */
    public ByteQueueOutputStream(final ByteQueue blockingQueue) {
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
    }

    /**
     * Creates a new QueueInputStream instance connected to this. Writes to this output stream will be visible to the
     * input stream.
     *
     * @return QueueInputStream connected to this stream
     */
    public ByteQueueInputStream newQueueInputStream() {
        return new ByteQueueInputStream(blockingQueue);
    }

    private boolean isClose = false;


    private final byte[] writeBuffer = new byte[1];

    /**
     * Writes a single byte.
     *
     * @throws IOException if the thread is interrupted while writing to the queue.
     */
    @Override
    public void write(final int b) throws IOException {
        writeBuffer[0] = (byte) (b);
        write(writeBuffer);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (isClose) {
            throw new IOException("Stream is closed");
        }

        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException("off: " + off + ", len: " + len + ", b.length: " + b.length);
        }

        blockingQueue.write(b, off, len);

    }

    @Override
    public void close() throws IOException {
        isClose = true;
    }
}
