package com.github.dreamhead.moco.mount;

import com.github.dreamhead.moco.HttpRequest;
import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.ResponseHandler;
import com.github.dreamhead.moco.handler.AbstractContentResponseHandler;
import com.github.dreamhead.moco.util.FileContentType;
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.toByteArray;

public class MountHandler extends AbstractContentResponseHandler {
    private final MountPathExtractor extractor;

    private final File dir;
    private final MountTo target;

    public MountHandler(File dir, MountTo target) {
        this.dir = dir;
        this.target = target;
        this.extractor = new MountPathExtractor(target);
    }

    @Override
    protected void writeContentResponse(HttpRequest request, ByteBuf buffer) {
        try {

            buffer.writeBytes(toByteArray(targetFile(request)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File targetFile(HttpRequest request) {
        Optional<String> relativePath = extractor.extract(request);
        return new File(dir, relativePath.get());
    }

    @Override
    protected String getContentType(HttpRequest request) {
        return new FileContentType(targetFile(request).getName()).getContentType();
    }

    @Override
    public ResponseHandler apply(final MocoConfig config) {
        if (config.isFor("uri")) {
            return new MountHandler(this.dir, this.target.apply(config));
        }

        if (config.isFor("file")) {
            return new MountHandler(new File(config.apply(this.dir.getName())), this.target);
        }

        return this;
    }
}
