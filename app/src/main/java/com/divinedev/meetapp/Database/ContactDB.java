package com.divinedev.meetapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.divinedev.meetapp.Helper.Contacts;
import com.divinedev.meetapp.Helper.intTobool;

import java.util.ArrayList;
import java.util.List;

public class ContactDB extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME = "Contacts";

    // Table columns
    public static final String _ID = "ID";
    public static final String SUBJECT = "subject";
    public static final String DESC = "description";
    public static final String NAME = "NAME";
    public static final String PHONE = "PHONE";
    public static final String ACTIVE = "ACTIVE";


    // Database Information
    static final String DB_NAME = "MeetApp.DB";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "" +
            "( ID INTEGER , " + NAME + " VARCHAR NOT NULL, " + PHONE + " VARCHAR PRIMARY KEY,"+ACTIVE+" INTEGER );";

    public ContactDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public void insertContact(Contacts contact){
        intTobool ib=new intTobool();
        String Name,phone;
        int active;
        Name=contact.getName();
        phone=contact.getPhone();
        active=ib.boolOf(contact.isActive());
        SQLiteDatabase db = this.getWritableDatabase();
        /*String insert="insert into "+TABLE_NAME+
                "(ID,NAME,PHONE,ACTIVE) "+"VALUES ((SELECT MAX(ID) + 1 FROM "+TABLE_NAME+"),"+Name+","+phone+","+active+");";

        db.execSQL(insert);*/
        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(NAME, Name);
        values.put(PHONE, phone);
        values.put(ACTIVE, active);



        // insert row
        try {
            db.insertOrThrow(TABLE_NAME, null, values);
        }
        catch (SQLiteConstraintException e){

        }




        // insert row


        // close db connection
        db.close();


    }
    public void update(String phone,int active){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ACTIVE, active);

        // updating row
        db.update(TABLE_NAME, values, PHONE + " = ?",
                new String[]{phone});
        db.close();
    }
    public Contacts getPhone(String phone){
        // get readable database as we are not inserting anything
        intTobool ib=new intTobool();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{_ID, NAME,PHONE,ACTIVE},
                PHONE + "=?",
                new String[]{phone}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        Contacts cont = new Contacts(
                cursor.getString(cursor.getColumnIndex(NAME)),
                cursor.getString(cursor.getColumnIndex(PHONE)),
                ib.intOf(cursor.getInt(cursor.getColumnIndex(ACTIVE))),null);

        // close the db connection
        cursor.close();

        return cont;
    }
    public List<Contacts> getAll(){
        List<Contacts> notes = new ArrayList<>();
        intTobool ib=new intTobool();

        // Select All Query
        String selectQuery = "SELECT  * FROM " +TABLE_NAME ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contacts cont = new Contacts(
                        cursor.getString(cursor.getColumnIndex(NAME)),
                        cursor.getString(cursor.getColumnIndex(PHONE)),
                        ib.intOf(cursor.getInt(cursor.getColumnIndex(ACTIVE))),null);

                notes.add(cont);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }
    public int getContactCount() {
        String countQuery = "SELECT  * FROM " +TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }
    public List<Contacts> getActive(){
        List<Contacts> notes = new ArrayList<>();
        intTobool ib=new intTobool();

        // Select All Query
        String selectQuery = "SELECT  * FROM " +TABLE_NAME+" WHERE "+ACTIVE+"=1" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contacts cont = new Contacts(
                        cursor.getString(cursor.getColumnIndex(NAME)),
                        cursor.getString(cursor.getColumnIndex(PHONE)),
                        ib.intOf(cursor.getInt(cursor.getColumnIndex(ACTIVE))),null);

                notes.add(cont);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }
    public void truncateDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
        db.close();
    }
}
