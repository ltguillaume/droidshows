package nl.asymmetrics.droidshows.utils;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Android specific file utilities */
public class AndroidFileUtils {
    public static @NonNull String getDatabaseMimeType() {
        String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension("db");

        // https://stackoverflow.com/questions/31301552/android-what-is-the-mime-type-to-use-if-i-want-to-see-pick-a-sqlite-database-fr
        // Since Since Feb. 2018 application/vnd.sqlite3.
        // The usage of application/x-sqlite3 is deprecated
        if (TextUtils.isEmpty(mimetype)) mimetype = "application/vnd.sqlite3";
        return mimetype;
    }

    /**
     * deletes fileNames[fileNames.length - 1] and renames fileNames[i] to fileNames[i+1].
     * As with all DocumentFile: Instead of IOExceptions the caller must check the boolean result of the function.
     *
     * @param destDir where all the files are processed
     * @param fileNames 2 or more file names.
     * @return false if something went wrong
     */
    public static boolean renameFiles(@NonNull DocumentFile destDir, String... fileNames) {
        boolean success = true;
        if (destDir == null || fileNames == null || fileNames.length < 2) {
            throw new IllegalArgumentException("renameFiles needs an existing destDir and 2 or more file names");
        }
        int last = fileNames.length - 1;
        DocumentFile file = destDir.findFile(fileNames[last]);
        if (file != null && file.exists()) success = file.delete();

        for (int i = last - 1; success && i >= 0; i --) {
            file = destDir.findFile(fileNames[i]);
            if (file != null && file.exists()) success = file.renameTo(fileNames[i + 1]);
        }
        return success;
    }

    public static void copyDocumentFile(@NonNull Context context,
                                        @NonNull DocumentFile source,
                                        @NonNull DocumentFile destination) throws IOException {
        if (context == null || source == null || source == null) {
            throw new IllegalArgumentException("copy: null arguments not allowed ");
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getContentResolver().openInputStream(source.getUri());
            out = context.getContentResolver().openOutputStream(destination.getUri());
            IOUtils.copy(in, out);
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
