package com.youngseum.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        MovieDetail detail = intent.getParcelableExtra(Intent.EXTRA_INTENT);

        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(detail.title);

        TextView tv_overview = (TextView) findViewById(R.id.tv_overview);
        tv_overview.setText(detail.overview);

        TextView tv_releaseDate = (TextView) findViewById(R.id.tv_releaseDate);
        tv_releaseDate.setText( formatDate(detail.release) );

        TextView tv_rating = (TextView) findViewById(R.id.tv_rating);
        tv_rating.setText(detail.vote + " / 10.0");

        ImageView iv_poster = (ImageView) findViewById(R.id.iv_poster);
        String imgURL = "http://image.tmdb.org/t/p/w185" + detail.poster;
        Picasso.with(this).load(imgURL).into(iv_poster);


    }

    private String formatDate(String time) {
        String year = time.substring(0, 4);
        String month = time.substring(5, 7);
        String date = time.substring(8, 10);

        switch(month) {
            case "01": month = "January"; break;
            case "02": month = "February"; break;
            case "03": month = "March"; break;
            case "04": month = "April"; break;
            case "05": month = "May"; break;
            case "06": month = "June"; break;
            case "07": month = "July"; break;
            case "08": month = "August"; break;
            case "09": month = "September"; break;
            case "10": month = "October"; break;
            case "11": month = "November"; break;
            case "12": month = "December"; break;
            default: month = "Error"; break;
        }

        if (date.substring(0,1).equals("0"))
            date = date.substring(1,2);

        return month + " " + date + ", " + year;
    }
}
