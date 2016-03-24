package org.logicalcliff.esppcalculator;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.logicalcliff.esppcalculator.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    float fmv;
    float sub_price;
    float purchase_price;
    float purchase_amt;
    float sale_amt;
    float discount;
    float sale_price;
    float ordinary_tax_rate, cap_tax_rate;
    int num_shares;
    Date grant_date, purchase_date, sale_date;
    float ord = 0, cap = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //(R.string.cap_rate_def)
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Called when the user clicks the Send button
     */
    public void onSubmit(View view) {
        // Do something in response to button
        EditText num;

        TextView result = (TextView) findViewById(R.id.disposition_type);
        if ((result == null)) throw new AssertionError();
        try {
            num = (EditText) findViewById(R.id.fmv);
            fmv = Float.parseFloat(num.getText().toString());
            System.out.print("FMV = " + fmv);
        } catch (NumberFormatException nfe) {
            result.setText("Error parsing data");
            System.out.print("FMV not found ");
            return;
        }

        try {
            num = (EditText) findViewById(R.id.subprice);
            sub_price = Float.parseFloat(num.getText().toString());
        } catch (NumberFormatException nfe) {
            result.setText("Error parsing data");
            return;
        }

        try {
            num = (EditText) findViewById(R.id.discount);
            discount = Float.parseFloat(num.getText().toString());
        } catch (NumberFormatException nfe) {
            result.setText("Error parsing data");
            return;
        }

        try {
            num = (EditText) findViewById(R.id.sale_price);
            sale_price = Float.parseFloat(num.getText().toString());
        } catch (NumberFormatException nfe) {
            result.setText("Error parsing data");
            return;
        }

        try {
            num = (EditText) findViewById(R.id.tax_rate);
            ordinary_tax_rate = Float.parseFloat(num.getText().toString());
        } catch (NumberFormatException nfe) {
            result.setText("Error parsing data");
            return;
        }

        try {
            num = (EditText) findViewById(R.id.cap_gain_rate);
            cap_tax_rate = Float.parseFloat(num.getText().toString());
        } catch (NumberFormatException nfe) {
            result.setText("Error");
        }

        try {
            num = (EditText) findViewById(R.id.num_shares);
            num_shares = Integer.parseInt(num.getText().toString());
        } catch (NumberFormatException nfe) {
            result.setText("Error parsing data");
            return;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM-DD-yy");

            num = (EditText) findViewById(R.id.grant_date);
            grant_date = formatter.parse(num.getText().toString());

            num = (EditText) findViewById(R.id.purchase_date);
            purchase_date = formatter.parse(num.getText().toString());

            num = (EditText) findViewById(R.id.sale_date);
            sale_date = formatter.parse(num.getText().toString());

        } catch (ParseException e) {
            System.out.println("Error parsing date");
            result.setText("Error parsing date");
            return;
        }
        //If we have read all data correctly, let's run the calculation
        String str = calculate();
        //and show the results to the user
        //result.setText(str);
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, str);
        startActivity(intent);


    }

    String calculate() {
        String str = "";
        purchase_amt = num_shares * sub_price * (1 - discount / 100);
        sale_amt = num_shares * sale_price;
        purchase_price = sub_price * (1 - discount / 100);


        Calendar cal = Calendar.getInstance();
        cal.setTime(grant_date);
        long grant = cal.getTimeInMillis();

        cal.setTime(purchase_date);
        long purchase = cal.getTimeInMillis();

        cal.setTime(sale_date);
        long sale = cal.getTimeInMillis();

        if ((sale - grant > 2 * 365) && (sale - purchase > 365)) {
            calcQualif();
            str += "Qualified Disposition\n";
        } else if (sale - grant < 2 * 365 && (sale - purchase > 365)) {
            calcDisqLong();
            str += "Disqualified Disposition\n";
        } else {
            calcDisqShort();
            str += "Disqualified Disposition\n";
        }

        str += "Purchase ($):           " + String.format("%.2f", purchase_amt) + "\n";
        str += "Sale ($):               " + String.format("%.2f", sale_amt) + "\n";
        if (sale_amt < purchase_amt) {
            str += "Gross Loss ($):         " + String.format("%.2f", (sale_amt - purchase_amt)) + "\n";
        } else {
            str += "Gross Profit ($):       " + String.format("%.2f", (sale_amt - purchase_amt)) + "\n";
        }
        str += "Ordinary income ($):    " + String.format("%.2f", ord) + "\n";
        str += "Short Term Tax ($):     " + String.format("%.2f", ord * ordinary_tax_rate / 100) + "\n";
        str += "Capital gain ($):       " + String.format("%.2f", cap) + "\n";
        str += "Long Term Tax ($):      " + String.format("%.2f", cap * cap_tax_rate / 100) + "\n";
        str += "Total tax ($):          " + String.format("%.2f", (cap * cap_tax_rate / 100 + ord * ordinary_tax_rate / 100)) + "\n";
        str += "----------------------------\n";
        str += "Profit after tax ($):   " + String.format("%.2f", ((ord + cap) - (cap * cap_tax_rate / 100 + ord * ordinary_tax_rate / 100))) + "\n";

        System.out.print(str);
        return str;
    }

    void calcDisqShort() {
        ord = sale_amt - purchase_amt;
    }

    void calcDisqLong() {
        if (sale_price > fmv) {
            ord = fmv * num_shares - purchase_price;
            cap = sale_price - fmv * num_shares;
        } else if (sale_price < fmv) {
            ord = sale_amt - purchase_amt;
        }
    }

    void calcQualif() {
        if (sale_price > sub_price) {
            ord = num_shares * (sub_price - purchase_price);
            cap = num_shares * (sale_price - sub_price);
        } else if (sale_price < sub_price && sale_price > purchase_price) {
            ord = num_shares * (sale_price - purchase_price);
            cap = 0;
        } else if (sale_price < purchase_price) {
            ord = 0;
            cap = num_shares * (sale_price - purchase_price); //will be negative
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.logicalcliff.esppcalculator/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.logicalcliff.esppcalculator/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
