package ru.careofhair.careofhare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Сергей on 10.09.2016.
 * Project name CareOfHare
 */

public class SaleActivity extends AppCompatActivity implements View.OnClickListener {

    Button button, btn0, btn10, btn15, btn20;
    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        button = (Button) findViewById(R.id.btn_ok);
        btn0 = (Button) findViewById(R.id.button2);
        btn10 = (Button) findViewById(R.id.button3);
        btn15 = (Button) findViewById(R.id.button4);
        btn20 = (Button) findViewById(R.id.button5);
        button.setOnClickListener(this);
        btn0.setOnClickListener(this);
        btn10.setOnClickListener(this);
        btn15.setOnClickListener(this);
        btn20.setOnClickListener(this);

        String sale = getIntent().getStringExtra("sale");
        result = sale;
        switch (sale) {
            case "0":
                btn0.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnpress));
                break;
            case "10":
                btn10.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnpress));
                break;
            case "15":
                btn15.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnpress));
                break;
            case "20":
                btn20.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnpress));
                break;
            default:
                break;

        }

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_ok:
                if (result != "") {
                    Intent intent = new Intent();
                    intent.putExtra("sale", result);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            case R.id.button2:
                result = btn0.getText().toString();
                btn0.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnpress));
                btn10.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn15.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn20.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                break;
            case R.id.button3:
                result = btn10.getText().toString();
                btn0.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn10.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnpress));
                btn15.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn20.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                break;
            case R.id.button4:
                result = btn15.getText().toString();
                btn0.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn10.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn15.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnpress));
                btn20.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                break;
            case R.id.button5:
                result = btn20.getText().toString();
                btn0.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn10.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn15.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnshape));
                btn20.setBackgroundDrawable(getResources().getDrawable(R.drawable.btnpress));
                break;
        }

    }
}
