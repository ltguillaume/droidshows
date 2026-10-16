package nl.asymmetrics.droidshows.thetvdb;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;

/**
 * Android spezific code TheTVDB.
 */
public class TheTVDBAndroid {

    public static void deleteThumbs(Context ctx) {
        DocumentFile thumbsDir = DocumentFile.fromFile(new File(ctx.getFilesDir().getAbsolutePath() + "/thumbs/banners/posters"));
        DocumentFile[] thumbs = (thumbsDir != null && thumbsDir.exists() && thumbsDir.isDirectory() ) ? thumbsDir.listFiles() : null;
        if (thumbs != null)
            for (DocumentFile thumb : thumbs)
                thumb.delete();
    }

}
