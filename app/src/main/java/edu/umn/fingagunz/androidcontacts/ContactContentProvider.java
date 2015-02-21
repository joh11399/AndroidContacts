package edu.umn.fingagunz.androidcontacts;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
public class ContactContentProvider extends ContentProvider
{
	private static final int CONTACTS = 0x0A;
	private static final int CONTACT = 0x0B;
	private static final String AUTHORITY = "edu.umn.fingagunz.androidcontacts";
	private static final String BASE_PATH = "contacts";

	public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/%s", AUTHORITY, BASE_PATH));
	public static final String CONTENT_ITEM_TYPE = String.format("%s/contact", ContentResolver.CURSOR_ITEM_BASE_TYPE);

	private SQLiteOpenHelper helper;
	private final UriMatcher uriMatcher;

	public static Uri getUriFromContactId(long contactId)
	{
		return Uri.parse(String.format("%s/%d", ContactContentProvider.CONTENT_URI, contactId));
	}

	public ContactContentProvider()
	{
		super();

		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, BASE_PATH, CONTACTS);
		uriMatcher.addURI(AUTHORITY, String.format("%s/#", BASE_PATH), CONTACT);
	}

	@Override
	public boolean onCreate()
	{
		helper = new ContactSQLiteOpenHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		checkColumns(projection);

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(ContactTable.TABLE_CONTACT);

		// Add WHERE clause if necessary
		switch(uriMatcher.match(uri))
		{
			case CONTACT:
				queryBuilder.appendWhere(String.format("%s = %s", ContactTable.COLUMN_ID, uri.getLastPathSegment()));
				break;

			case CONTACTS:
				break;

			default:
				throw new IllegalArgumentException(String.format("Unknown URI: %s", uri));
		}

		SQLiteDatabase database = helper.getWritableDatabase();
		Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		long id;

		switch(uriMatcher.match(uri))
		{
			case CONTACTS:
				id = helper.getWritableDatabase().insert(ContactTable.TABLE_CONTACT, null, values);
				break;

			default:
				throw new IllegalArgumentException(String.format("Unknown URI: %s", uri));
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(String.format("%s/%s", BASE_PATH, id));
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		int rowsUpdated;
		SQLiteDatabase database = helper.getWritableDatabase();

		switch(uriMatcher.match(uri))
		{
			case CONTACTS:
				rowsUpdated = database.update(ContactTable.TABLE_CONTACT, values, selection, selectionArgs);
				break;

			case CONTACT:
				long id = Long.parseLong(uri.getLastPathSegment());
				if(TextUtils.isEmpty(selection))
				{
					String whereClause = String.format("%s = %d", ContactTable.COLUMN_ID, id);
					rowsUpdated = database.update(ContactTable.TABLE_CONTACT, values, whereClause, null);
				}
				else
				{
					String whereClause = String.format("%s = %d AND %s", ContactTable.COLUMN_ID, id, selection);
					rowsUpdated = database.update(ContactTable.TABLE_CONTACT, values, whereClause, selectionArgs);
				}
				break;

			default:
				throw new IllegalArgumentException(String.format("Unknown URI: %s", uri));
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		SQLiteDatabase database = helper.getWritableDatabase();
		int rowsDeleted;

		switch(uriMatcher.match(uri))
		{
			case CONTACTS:
				rowsDeleted = database.delete(ContactTable.TABLE_CONTACT, selection, selectionArgs);
				break;

			case CONTACT:
				long id = Long.parseLong(uri.getLastPathSegment());
				if(TextUtils.isEmpty(selection))
				{
					String whereClause = String.format("%s = %d", ContactTable.COLUMN_ID, id);
					rowsDeleted = database.delete(ContactTable.TABLE_CONTACT, whereClause, null);
				}
				else
				{
					String whereClause = String.format("%s = %d AND %s", ContactTable.COLUMN_ID, id, selection);
					rowsDeleted = database.delete(ContactTable.TABLE_CONTACT, whereClause, selectionArgs);
				}
				break;

			default:
				throw new IllegalArgumentException(String.format("Unknown URI: %s", uri));
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) { return null; }

	private void checkColumns(String[] projection)
	{
		if(projection != null)
		{
			HashSet<String> availableColumns = new HashSet<>(Arrays.asList(ContactTable.ALL_COLUMNS));
			HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));

			if(!availableColumns.containsAll(requestedColumns))
			{
				throw new IllegalArgumentException("Unknown columns in projection.");
			}
		}
	}
}
