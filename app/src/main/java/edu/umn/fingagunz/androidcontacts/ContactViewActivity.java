package edu.umn.fingagunz.androidcontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

public class ContactViewActivity extends Activity
{
	private static final int EDIT_CONTACT_REQUEST = 0x01;
	private Uri contactUri;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_view);

		findViewById(R.id.phone_layout).setOnClickListener(new PhoneClickListener());
		findViewById(R.id.email_layout).setOnClickListener(new EmailClickListener());
		findViewById(R.id.twitter_layout).setOnClickListener(new TwitterClickListener());

		Intent intent = getIntent();
		contactUri = intent.getParcelableExtra(ContactContentProvider.CONTENT_ITEM_TYPE);
		populateUI(contactUri);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_contact_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_edit_contact:
				Intent intent = new Intent(this, ContactEditActivity.class);
				intent.putExtra(ContactContentProvider.CONTENT_ITEM_TYPE, contactUri);
				startActivityForResult(intent, EDIT_CONTACT_REQUEST);
				break;

			case R.id.action_delete_contact:
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
				String contactName = ((TextView)findViewById(R.id.name)).getText().toString();
				dialogBuilder.setTitle(String.format(getResources().getString(R.string.delete_contact), contactName));
				dialogBuilder.setPositiveButton
					(
						R.string.button_ok,
						new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								getContentResolver().delete(contactUri, null, null);
								setResult(RESULT_OK);
								finish();
							}
						}
					);
				dialogBuilder.setNegativeButton(R.string.button_cancel, null);
				dialogBuilder.show();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == EDIT_CONTACT_REQUEST)
		{
			if(resultCode == RESULT_OK)
			{
				populateUI(contactUri);
			}
		}
	}

	private void populateUI(Uri contactUri)
	{
		String[] projection = new String[] { ContactTable.COLUMN_NAME, ContactTable.COLUMN_TITLE, ContactTable.COLUMN_PHONE, ContactTable.COLUMN_EMAIL, ContactTable.COLUMN_TWITTER };
		Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);

		if(cursor != null)
		{
			cursor.moveToFirst();

			String name = cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME));
			((TextView)findViewById(R.id.name)).setText(name);

			String title = cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_TITLE));
			setView(title, R.id.title, R.id.label_title, 0, null);

			String phone = cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_PHONE));
			setView(phone, R.id.phone, R.id.label_phone, R.id.separator_title_phone, title);

			String email = cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_EMAIL)).trim();
			setView(email, R.id.email, R.id.label_email, R.id.separator_phone_email, phone);

			String twitter = cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_TWITTER));
			setView(twitter, R.id.twitter, R.id.label_twitter, R.id.separator_email_twitter, email);

			cursor.close();
		}
	}

	private void setView(String text, int textViewId, int labelViewId, int separatorId, String previousText)
	{
		TextView textView = (TextView)findViewById(textViewId);
		textView.setText(text);

		int visibility = TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE;
		textView.setVisibility(visibility);
		findViewById(labelViewId).setVisibility(visibility);

		if(separatorId > 0)
		{
			int separatorVisibility = ((text != null) && (!text.isEmpty()) && (previousText != null) && (!previousText.isEmpty())) ? View.VISIBLE : View.GONE;
			findViewById(separatorId).setVisibility(separatorVisibility);
		}
	}

	private class PhoneClickListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			String phoneNumber = ((TextView)v).getText().toString().replaceAll("[^0-9]", "");
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse(String.format("tel:%s", phoneNumber)));
			startActivity(intent);
		}
	}

	private class EmailClickListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			String emailAddress = ((TextView)v).getText().toString();
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null));
			startActivity(Intent.createChooser(intent, "Send email..."));
		}
	}

	private class TwitterClickListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			String twitterHandle = ((TextView)v).getText().toString();
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://twitter.com/%s", twitterHandle)));
			startActivity(intent);
		}
	}
}
