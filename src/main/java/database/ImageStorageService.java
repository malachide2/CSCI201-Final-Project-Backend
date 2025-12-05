package database;

import java.io.InputStream;

/**
 * Abstraction for image storage. Implementations should return a publicly
 * accessible URL (or server-relative path) for a stored image.
 */
public interface ImageStorageService {

    /**
     * Store original image bytes and return a URL.
     * @param in InputStream of image. Implementation must NOT close the stream.
     * @param filename desired filename (may be adjusted by implementation)
     * @return publicly accessible URL or path
     */
    String storeImage(InputStream in, String filename) throws Exception;

    /**
     * Store thumbnail bytes and return a URL.
     */
    String storeThumbnail(InputStream in, String filename) throws Exception;

}
