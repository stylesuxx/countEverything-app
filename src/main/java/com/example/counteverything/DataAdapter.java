package com.example.counteverything;
/**
 * Created by stylesuxx on 10/14/13.
 */
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataAdapter {
    protected static final String TAG = "DataAdapter";
    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public DataAdapter(Context context) {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public DataAdapter createDatabase() throws SQLException {
        try{
            mDbHelper.createDataBase();
        } catch (IOException mIOException){
            Log.e(TAG, "createDatabase >>" + mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataAdapter open() throws SQLException {
        try{
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        } catch (SQLException mSQLException){
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    // Add a new beverage to the database
    public boolean addNewBeverage(String name, float amount) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("amount", amount);

        try{
            mDb.insert("beverages", "", values);
        } catch (Exception ex){
            return false;
        }

        return true;
    }

    // Return the name of a beverage by its id
    public String getItemName(int id) {
        try{
            String sql ="SELECT name FROM beverages WHERE _id=" + id;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null){
                mCur.moveToNext();
            }
            String name = "";
            if(mCur.moveToFirst()) {
                name = mCur.getString(mCur.getColumnIndex("name"));
                Log.v(TAG, name);
            }
            return name;
        } catch (SQLException mSQLException){
            Log.e(TAG, "getItemName >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    // Return the amount of a beverage by its id
    public float getItemAmount(int id) {
        try{
            String sql ="SELECT amount FROM beverages WHERE _id=" + id;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null){
                mCur.moveToNext();
            }
            float amount = 0;
            if(mCur.moveToFirst()) {
                amount = mCur.getFloat(mCur.getColumnIndex("amount"));
            }
            return amount;
        } catch (SQLException mSQLException){
            Log.e(TAG, "getItemAmount >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    // Get all available beverages
    public Cursor getItems() {
        try{
            String sql ="SELECT * FROM beverages";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null){
                mCur.moveToNext();
            }
            return mCur;
        } catch (SQLException mSQLException){
            Log.e(TAG, "getItems >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public boolean removeItem(int $id) {
        Log.v(TAG, "removeItem >> " + $id);
        try{
            mDb.execSQL("DELETE FROM beverages WHERE _id=" + $id);
            //String sql ="DELETE FROM beverages WHERE _id=" + $id;
            //Cursor mCur = mDb.rawQuery(sql, null);

            return true;
        } catch (SQLException mSQLException){
            Log.e(TAG, "removeItems >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }
}