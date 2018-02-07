package com.techshroom.lettar.pipe.builtins.accept;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.techshroom.lettar.mime.MimeType;

@AutoValue
public abstract class ProducesData {

    public static ProducesData from(Produces annotation) {
        MimeType mimeType;
        if (Produces.UNSPECIFIED.equals(annotation.value())) {
            mimeType = null;
        } else {
            mimeType = MimeType.parse(annotation.value());
        }
        return of(mimeType, annotation.matchesAnything());
    }

    public static ProducesData of(@Nullable MimeType mimeType, boolean matchAnything) {
        return new AutoValue_ProducesData(Optional.ofNullable(mimeType), matchAnything);
    }

    ProducesData() {
    }

    public abstract Optional<MimeType> getMimeType();

    public abstract boolean isMatchAnything();

}
