package com.ivyinfo.feiying.activity.other;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;
import com.ivyinfo.contact.beans.Contact;
import com.ivyinfo.feiying.adapter.ContactMultiSelectListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.ActivityRequests;

/**
 * multi-selection contact list
 * 
 * @author sk
 * 
 */
public class ContactMultiSelectListAcitivity extends Activity {
	private EditText searchET;

	private ContactManager cm;

	private List<Contact> contacts;

	private ContactMultiSelectListAdapter listAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mul_select_contact_list_view);
		cm = ContactManagerFactory.getContactManager();
		if (cm == null) {
			ContactManagerFactory.initContactManager(this);
			cm = ContactManagerFactory.getContactManager();
		}

		searchET = (EditText) findViewById(R.id.mul_select_search_et);

		contacts = cm.getAllContactsByNameSort();

		ListView contactListView = (ListView) findViewById(R.id.mul_select_contact_list);
		listAdapter = new ContactMultiSelectListAdapter(this);
		contactListView.setAdapter(listAdapter);

		listAdapter.setContacts(contacts);

		initSearchField();
	}

	private void initSearchField() {
		searchET.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				searchContacts(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void searchContacts(String searchText) {
		if (contacts == null) {
			return;
		}
		List<Contact> result = cm.search(searchText, contacts);
		listAdapter.setContacts(result);
	}

	/**
	 * call back method for CLEAR button
	 * @param v
	 */
	public void onClear(View v) {
		searchET.setText("");
	}

	/**
	 * call back method for BACK button
	 * @param view
	 */
	public void onBack(View view) {
		finish();
	}

	/**
	 * call back method for DONE button
	 * @param v
	 */
	public void onDone(View v) {
		ArrayList<String> selectedContacts = listAdapter.getSelectedContacts();

		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("selected_contacts", selectedContacts);
		intent.putExtras(bundle);
		setResult(ActivityRequests.RESULT_OK, intent);
		finish();
	}
}
