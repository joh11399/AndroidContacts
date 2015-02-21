package edu.umn.fingagunz.androidcontacts;

import android.database.sqlite.SQLiteDatabase;

public class ContactTable
{
	public static final String TABLE_CONTACT = "Contact";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "Name";
	public static final String COLUMN_TITLE = "Title";
	public static final String COLUMN_PHONE = "Phone";
	public static final String COLUMN_EMAIL = "Email";
	public static final String COLUMN_TWITTER = "Twitter";
	public static final String[] ALL_COLUMNS =
		new String[]
		{
			COLUMN_ID,
			COLUMN_NAME,
			COLUMN_TITLE,
			COLUMN_PHONE,
			COLUMN_EMAIL,
			COLUMN_TWITTER
		};
	public static final String[] ALL_COLUMNS_WITHOUT_ID =
		new String[]
			{
				COLUMN_NAME,
				COLUMN_TITLE,
				COLUMN_PHONE,
				COLUMN_EMAIL,
				COLUMN_TWITTER
			};

	private static final String DATABASE_CREATE =
		String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s TEXT, %s TEXT)",
			TABLE_CONTACT, COLUMN_ID, COLUMN_NAME, COLUMN_TITLE, COLUMN_PHONE, COLUMN_EMAIL, COLUMN_TWITTER);

	private static final String DATABASE_DROP = String.format("DROP TABLE IF EXISTS %s", TABLE_CONTACT);

	public static void onCreate(SQLiteDatabase database) { database.execSQL(DATABASE_CREATE); }

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		database.execSQL(DATABASE_DROP);
		onCreate(database);
	}
}
