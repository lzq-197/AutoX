package org.autojs.autojs.ui.compose.widget;

import android.content.Context;
import android.view.View;

public class ClickableIconView extends androidx.appcompat.widget.AppCompatImageView implements View.OnClickListener {

    public ClickableIconView(Context context) {
        super(context);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
