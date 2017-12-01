package com.techshroom.lettar.mime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Simplified representation of a MIME type. Supports <code>*&#47;*</code>, for
 * the {@code Accept} header.
 */
@AutoValue
public abstract class MimeType {

    // cache of instance -> instance, uses key to set value to indicate cached
    // otherwise the key gets thrown away, so it takes no space :)
    private static final LoadingCache<MimeType, MimeType> MIME_CACHE = CacheBuilder.newBuilder()
            .maximumSize(50)
            .build(CacheLoader.from(k -> k));

    private static final Splitter SLASH = Splitter.on('/').limit(2);

    public static MimeType parse(String mimeType) {
        checkArgument(!mimeType.isEmpty(), "MIME type cannot be empty");
        // very loose mime definition
        // we don't really need strictness
        Iterator<String> parts = SLASH.split(mimeType).iterator();
        checkState(parts.hasNext(), "not even one part? broken Splitter?");
        String primary = parts.next();
        String secondary = parts.hasNext() ? parts.next() : "*";
        if (primary.equals("*")) {
            checkArgument(secondary.equals("*"), "only */* is allowed, not */%s", secondary);
        }
        return of(primary, secondary);
    }

    public static MimeType of(String primary, String secondary) {
        return MIME_CACHE.getUnchecked(new AutoValue_MimeType(primary, secondary));
    }

    MimeType() {
    }

    public abstract String getPrimaryType();

    public abstract String getSecondaryType();

    /**
     * Checks if this MIME type is a subtype of the provided MIME type.
     * 
     * @param mimeType
     *            - the MIME type to check against
     * @return {@code true} if this MIME type is a subtype of the provided MIME
     *         type
     */
    public final boolean matches(MimeType mimeType) {
        if (mimeType.getPrimaryType().equals("*")) {
            // matches all
            return true;
        }
        if (!mimeType.getPrimaryType().equals(getPrimaryType())) {
            return false;
        }
        String mtSec = mimeType.getSecondaryType();
        return mtSec.equals("*") || mtSec.equals(getSecondaryType());
    }

}
