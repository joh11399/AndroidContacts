package edu.umn.fingagunz.androidcontacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactSQLiteOpenHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "Contacts.db";
	private static final int DATABASE_VERSION = 1;

	public ContactSQLiteOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		ContactTable.onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		ContactTable.onUpgrade(database, oldVersion, newVersion);
	}
}
