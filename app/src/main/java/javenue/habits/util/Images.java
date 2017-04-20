package javenue.habits.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

public class Images {
    public static Drawable scaleImage(Context context, Drawable image, float scaleFactor) {
        if ((image == null) || !(image instanceof BitmapDrawable)) {
            return image;
        }

        Bitmap b = ((BitmapDrawable) image).getBitmap();

        int sizeX = Math.round(image.getIntrinsicWidth() * scaleFactor);
        int sizeY = Math.round(image.getIntrinsicHeight() * scaleFactor);

        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false);

        image = new BitmapDrawable(context.getResources(), bitmapResized);

        return image;
    }

    public static void setTintColor(Context context, Drawable drawable, int colorId) {
        int color = ContextCompat.getColor(context, colorId);
        DrawableCompat.setTint(drawable, color);
    }
}
