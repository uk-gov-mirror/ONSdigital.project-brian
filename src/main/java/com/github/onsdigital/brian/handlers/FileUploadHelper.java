package com.github.onsdigital.brian.handlers;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.davidcarboni.encryptedfileupload.EncryptedFileItemFactory;
import com.github.onsdigital.brian.exception.BadRequestException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.github.onsdigital.brian.logging.LogEvent.logEvent;

/**
 * Provides functionality for handling multipart file uploads ensuring they will be written to disk encrypted.
 */
public class FileUploadHelper {

    static final String TEMP_DIR = "csdb";
    static final String TEMP_FILENAME = "csdb";
    static final String CSDB_EXT = ".csdb";

    /**
     * Extract the multipart upload file part from the request and write it to a temp file. Returns the path to the
     * temp file.
     */
    public Path getFileUploadPath(HttpServletRequest request, SecretKey key) throws IOException,
            FileUploadException, BadRequestException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new BadRequestException("expected multipart upload request but was not");
        }

        ServletFileUpload upload = getServletFileUpload();

        Optional<FileItem> fileItem = getFileItemFromRequest(upload.parseRequest(request));
        if (fileItem.isPresent()) {
            return writeToTempFile(fileItem.get(), key);
        }

        throw new BadRequestException("expected file item in request but none found");
    }

    private ServletFileUpload getServletFileUpload() throws IOException {
        EncryptedFileItemFactory factory = new EncryptedFileItemFactory();
        File repository = Files.createTempDirectory(TEMP_DIR).toFile();
        factory.setRepository(repository);
        return new ServletFileUpload(factory);
    }

    private Optional<FileItem> getFileItemFromRequest(List<FileItem> items) throws IOException {
        return items.stream().filter(item -> !item.isFormField()).findFirst();
    }

    private Path writeToTempFile(FileItem item, SecretKey key) throws IOException {
        Path tempFile = Files.createTempFile(TEMP_FILENAME, CSDB_EXT);
        try (
                InputStream inputStream = item.getInputStream();
                OutputStream stream = new Crypto().encrypt(Files.newOutputStream(tempFile), key)
        ) {
            IOUtils.copy(inputStream, stream);
            logEvent().path(tempFile).info("successfully wrote upload file to temp file");
            return tempFile;
        } catch (IOException io) {
            logEvent(io).path(tempFile).error("error while attempting to write stream to file");
            throw io;
        }
    }

}
