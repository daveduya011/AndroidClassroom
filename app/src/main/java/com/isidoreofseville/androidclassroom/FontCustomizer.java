package com.isidoreofseville.androidclassroom;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by Dave on 12/25/2017.
 */

public class FontCustomizer {

    private Typeface quicksandbold;
    private Typeface quicksand;

    public FontCustomizer(Context context) {
        quicksandbold = Typeface.createFromAsset(context.getAssets(), "quicksandbold.otf");
        quicksand = Typeface.createFromAsset(context.getAssets(), "quicksandregular.otf");
    }

    public void setToQuickSand(TextView text){
        text.setTypeface(quicksand);
    }
    public void setToQuickSand(TextView[] textCollection){
        for (TextView text : textCollection){
            text.setTypeface(quicksand);
        }
    }

    public void setToQuickSandBold(TextView text){
        text.setTypeface(quicksandbold);
    }

    public void setToQuickSandBold(TextView[] textCollection){
        for (TextView text : textCollection){
            text.setTypeface(quicksandbold);
        }
    }


}
