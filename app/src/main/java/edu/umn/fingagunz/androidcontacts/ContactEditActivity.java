package edu.umn.fingagunz.androidcontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class ContactEditActivity extends Activity
{
	private Uri contactUri;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_edit);

		((EditText)findViewById(R.id.phone)).addTextChangedListener(new PhoneNumberFormattingTextWatcher());

		Intent intent = getIntent();
		if(intent.hasExtra(ContactContentProvider.CONTENT_ITEM_TYPE))
		{
			setTitle(R.string.title_activity_contact_edit);
			contactUri = intent.getParcelableExtra(ContactContentProvider.CONTENT_ITEM_TYPE);
			populateUI(contactUri);
		}
		else
		{
			setTitle(R.string.title_activity_contact_new);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_contact_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_save_contact:
				String name = ((EditText)findViewById(R.id.name)).getText().toString();
				if(name.isEmpty())
				{
					new AlertDialog.Builder(this)
						.setTitle(R.string.no_name_error)
						.setPositiveButton(R.string.button_ok, null)
						.show();
				}
				else
				{
					saveContact();
					setResult(RESULT_OK);
					finish();
				}
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void populateUI(Uri contactUri)
	{
		Cursor cursor = getContentResolver().query(contactUri, ContactTable.ALL_COLUMNS_WITHOUT_ID, null, null, null);

		if(cursor != null)
		{
			cursor.moveToFirst();
			((EditText)findViewById(R.id.name)).setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME)));
			((EditText)findViewById(R.id.title)).setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_TITLE)));
			((EditText)findViewById(R.id.phone)).setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_PHONE)));
			((EditText)findViewById(R.id.email)).setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_EMAIL)));
			((EditText)findViewById(R.id.twitter)).setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_TWITTER)));

			cursor.close();
		}
	}

	private void saveContact()
	{
		ContentValues values = new ContentValues();
		values.put(ContactTable.COLUMN_NAME, ((EditText)findViewById(R.id.name)).getText().toString().trim());
		values.put(ContactTable.COLUMN_TITLE, ((EditText)findViewById(R.id.title)).getText().toString().trim());
		values.put(ContactTable.COLUMN_PHONE, ((EditText)findViewById(R.id.phone)).getText().toString().trim());
		values.put(ContactTable.COLUMN_EMAIL, ((EditText)findViewById(R.id.email)).getText().toString().trim());
		values.put(ContactTable.COLUMN_TWITTER, ((EditText)findViewById(R.id.twitter)).getText().toString().trim());

		if(contactUri == null)
		{
			contactUri = getContentResolver().insert(ContactContentProvider.CONTENT_URI, values);
		}
		else
		{
			getContentResolver().update(contactUri, values, null, null);
		}
	}
}
