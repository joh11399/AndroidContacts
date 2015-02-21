package edu.umn.fingagunz.androidcontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;

public class ContactsActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
	private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

		getListView().setOnItemLongClickListener(new ListViewItemLongClickListener());
		loadAdapter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_contacts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_add_contact:
				startActivity(new Intent(this, ContactEditActivity.class));
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		String[] projection = new String[] { ContactTable.COLUMN_ID, ContactTable.COLUMN_NAME, ContactTable.COLUMN_TITLE, ContactTable.COLUMN_PHONE };
		return new CursorLoader(this, ContactContentProvider.CONTENT_URI, projection, null, null, String.format("%s COLLATE NOCASE", ContactTable.COLUMN_NAME));
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		viewContact(id);
	}

	private void loadAdapter()
	{
		getLoaderManager().initLoader(0, null, this);

		String[] from = new String[] { ContactTable.COLUMN_NAME, ContactTable.COLUMN_TITLE, ContactTable.COLUMN_PHONE };
		int[] to = new int[] { R.id.name, R.id.title, R.id.phone };
		adapter = new SimpleCursorAdapter(this, R.layout.contact_row, null, from, to, 0);

		setListAdapter(adapter);
	}

	private void viewContact(long id)
	{
		Intent intent = new Intent(this, ContactViewActivity.class);
		Uri contactUri = ContactContentProvider.getUriFromContactId(id);
		intent.putExtra(ContactContentProvider.CONTENT_ITEM_TYPE, contactUri);

		startActivity(intent);
	}

	// This seems like it should already be a built in method, but it is mostly for the nested
	// classes that need access to the parent activity.  This will provide them that access.
	private Activity getActivity()
	{
		return this;
	}

	// This class handles when a list item has a long click event
	private class ListViewItemLongClickListener implements AdapterView.OnItemLongClickListener
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
		{
			String contactName = ((TextView)view.findViewById(R.id.name)).getText().toString();
			new BottomSheet.Builder(getActivity())
				.sheet(R.menu.bottom_menu_contacts)
				.title(contactName)
				.listener(new BottomSheetClickListener(view, id))
				.show();

			return true;
		}
	}

	// This class handles when an item in the bottom sheet is clicked
	private class BottomSheetClickListener implements DialogInterface.OnClickListener
	{
		private final View contactView;
		private final long contactId;

		public BottomSheetClickListener(View contactView, long contactId)
		{
			this.contactView = contactView;
			this.contactId = contactId;
		}

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			switch(which)
			{
				case R.id.action_delete:
					final String contactName = ((TextView)contactView.findViewById(R.id.name)).getText().toString();
					new AlertDialog.Builder(getActivity())
						.setTitle(String.format(getResources().getString(R.string.delete_contact), contactName))
						.setPositiveButton(R.string.button_ok, new DialogPositiveClickListener(contactId))
						// If the user cancels, we want to do nothing, so we set a null listener on the negative button
						.setNegativeButton(R.string.button_cancel, null)
						.show();
					break;

				case R.id.action_view:
					viewContact(contactId);
					break;
			}
		}
	}

	// This class handles when the positive button in the delete dialog is clicked
	private class DialogPositiveClickListener implements DialogInterface.OnClickListener
	{
		private final long contactId;

		public DialogPositiveClickListener(long contactId)
		{
			this.contactId = contactId;
		}

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			getContentResolver().delete(ContactContentProvider.getUriFromContactId(contactId), null, null);
		}
	}
}
