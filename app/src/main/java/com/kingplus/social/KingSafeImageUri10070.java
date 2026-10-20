package com.kingplus.social;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import java.io.InputStream;

/** Eagerly decodes local document URIs so revoked grants cannot crash ImageView during layout. */
final class KingSafeImageUri10070 {
    private static final int MAX_EDGE = 640;
    private KingSafeImageUri10070() {}

    static boolean show(ImageView target, Uri uri) {
        if (target == null || uri == null) return false;
        Bitmap bitmap = null;
        try {
            ContentResolver resolver = target.getContext().getContentResolver();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream input = resolver.openInputStream(uri)) {
                if (input == null) return false;
                BitmapFactory.decodeStream(input, null, bounds);
            }
            if (bounds.outWidth < 1 || bounds.outHeight < 1) return false;
            int sample = 1;
            while (bounds.outWidth / sample > MAX_EDGE || bounds.outHeight / sample > MAX_EDGE) sample *= 2;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try (InputStream input = resolver.openInputStream(uri)) {
                if (input == null) return false;
                bitmap = BitmapFactory.decodeStream(input, null, options);
            }
            if (bitmap == null) return false;
            target.setImageBitmap(bitmap);
            return true;
        } catch (Exception | OutOfMemoryError ignored) {
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            target.setImageDrawable(null);
            return false;
        }
    }
}
