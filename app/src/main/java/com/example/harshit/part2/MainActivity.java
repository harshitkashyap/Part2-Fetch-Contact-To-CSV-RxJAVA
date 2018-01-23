package com.example.harshit.part2;


import android.Manifest;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.FileWriter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;




public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;


    TextView textView;
    Button button;


    Contact c=new Contact();
    ArrayList<Contact> contactlist=new ArrayList<Contact>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView=(TextView)findViewById(R.id.text);
        button=(Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                startBackgroundTask(contactlist);


            }
        });


    }


    private void startBackgroundTask(ArrayList<Contact> clist) {

        Observable.just(clist)
                .map(this::doInBackground)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this::onPreExecute)
                .subscribe(this::onPostExecute);

    }

    private void onPreExecute(Disposable disposable) {

    }

    private String[] doInBackground(ArrayList<Contact> yoList) {

        yoList=getContacts();
        String path=writeCsv(yoList);
        String lis=getString(yoList);

        String[] st={path,lis};

        return st;
    }




    private void onPostExecute(String[] result) {


        textView.setText(result[1]);

        Snackbar snackbar = Snackbar
                .make((RelativeLayout)findViewById(R.id.cl), "CSV created at-"+result[0], Snackbar.LENGTH_LONG);

        snackbar.show();
    }



//----------------------------------------just because the code was looking messi-Mr Kashyap------------------------------------------------
// method to get Contact Object Array List

    public ArrayList<Contact> getContacts(){

        ArrayList<Contact> contactArrayList = new ArrayList<Contact>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);


        }
        else {




            Cursor cursor = null;
            ContentResolver contentResolver = getContentResolver();


            try {

                cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            } catch (Exception e) {

                Log.e("kya error hai", e.getMessage());
            }

            if (cursor.getCount() > 0) {

                while (cursor.moveToNext()) {

                    Contact contact = new Contact();

                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    contact.name = name;

                    int phoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                    if (phoneNumber > 0) {

                        Cursor phonecursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                , null
                                , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                                , new String[]{id}
                                , null);

                        while (phonecursor.moveToNext()) {

                            String pNumber = phonecursor.getString(phonecursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            contact.number = pNumber;
                        }
                        phonecursor.close();
                    }

                    contactArrayList.add(contact);

                }
            }
        }
        return contactArrayList;
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                contactlist=getContacts();
            } else {
                Toast.makeText(this, "HARSHIT KASHYAP wants you to Allow", Toast.LENGTH_SHORT).show();
            }
        }
    }
//---------------------------------------------------------------------------------------------------------------------------
//method to create a CSV file using opencsv external library

    public String writeCsv(ArrayList<Contact> contList)  {

        CSVWriter writer;
        String path= Environment.getExternalStorageDirectory().getPath()+"/HK_CSV["+ DateFormat.getDateTimeInstance().format(new Date())+"].csv";

        try {
            writer=new CSVWriter(new FileWriter(path), '\t');
            for(int j=0;j<contList.size();j++) {

                String[] entries = {contList.get(j).name,contList.get(j).number};
                writer.writeNext(entries);
                writer.close();
            }
        }
        catch (Exception e){

             Log.e("yeh csv ka error hai:-",e.getMessage());
        }

        return path;
    }

//-----------------------------------------------------------------------------------------------------------------------------
    public String getString(ArrayList<Contact> arr){

        String str="";

        for(int i=0;i<arr.size();i++){
            str=str+arr.get(i).name+"-"+arr.get(i).number+" \n";
        }
        return str;
    }


}

//------------------------------------------------------------------------------------------------------------------------------
    class Contact{

    public String name="";
    public String number="";
    public int id=0;
}



