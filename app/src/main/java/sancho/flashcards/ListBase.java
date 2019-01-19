package sancho.flashcards;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListBase extends MainActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        db = dbHelper.getWritableDatabase();
        c = db.query("mytable", null, null, null, null, null, null);
        Log.d(LOG_TAG, "- - - list - - -");

        int[] colors = new int[2];
        colors[0] = Color.parseColor("#ffffff");
        colors[1] = Color.parseColor("#f5f5f5");

        LinearLayout linLayout = (LinearLayout) findViewById(R.id.linLayout);

        LayoutInflater ltInflater = getLayoutInflater();

        c.moveToFirst();
//                pos = c.getPosition();
        int rowCount = c.getCount();
        Log.d(LOG_TAG, "всего строк: " + rowCount);

        for (int i = 0; i < rowCount; i++) {
//                    Log.d("myLogs", "i = " + i);
            View item = ltInflater.inflate(R.layout.item, linLayout, false);
            c.moveToPosition(i);

            TextView tvFront = (TextView) item.findViewById(R.id.tvFront);
            TextView tvBack = (TextView) item.findViewById(R.id.tvBack);
            TextView tvLearned = (TextView) item.findViewById(R.id.tvLearned);


            tvFront.setText(c.getString(c.getColumnIndex("front")));
            tvBack.setText(c.getString(c.getColumnIndex("back")));
            tvLearned.setText(c.getString(c.getColumnIndex("learned")));

            item.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            item.setBackgroundColor(colors[i % 2]);
            linLayout.addView(item);
        }
    }

}
