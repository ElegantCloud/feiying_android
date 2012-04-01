package com.ivyinfo.feiying.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivyinfo.contact.beans.Contact;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.ContactConstants;

public class ContactMultiSelectListAdapter extends BaseAdapter {
	LayoutInflater mInflater;

	private List<Contact> contacts;

	private List<JSONObject> selectedContacts;

	public ContactMultiSelectListAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
		contacts = new ArrayList<Contact>();
		resetSelectedContacts();
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
		notifyDataSetChanged();
	}

	/**
	 * reset selected contact list to be empty
	 */
	public void resetSelectedContacts() {
		selectedContacts = new ArrayList<JSONObject>();
	}

	public ArrayList<String> getSelectedContacts() {
		ArrayList<String> selectedContactList = new ArrayList<String>();
		for (JSONObject obj : selectedContacts) {
			selectedContactList.add(obj.toString());
		}
		return selectedContactList;
	}

	private void add2SelectedContacts(String name, String phone)
			throws JSONException {
		Log.d("feiying", "add2SelectedContacts: " + phone);
		JSONObject obj = new JSONObject();
		obj.put(ContactConstants.name.name(), name);
		obj.put(ContactConstants.phone_number.name(), phone);
		selectedContacts.add(obj);
	}

	private void removeFromSelectedContacts(String phone) throws JSONException {
		Log.d("feiying", "removeFromSelectedContacts: " + phone);
		for (JSONObject obj : selectedContacts) {
			String number = obj.getString(ContactConstants.phone_number.name());
			if (number.equals(phone)) {
				selectedContacts.remove(obj);
				break;
			}
		}
	}
	
	private boolean isPhoneNumberSelected(String phoneNumber) {
		boolean selected = false;
		for (JSONObject obj : selectedContacts) {
			try {
				String number = obj.getString(ContactConstants.phone_number.name());
				if (number.equals(phoneNumber)) {
					selected = true;
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		return selected;
	}

	@Override
	public int getCount() {
		return contacts.size();
	}

	@Override
	public Object getItem(int position) {
		return contacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		Contact c = contacts.get(position);
		if (c == null) {
			return -1;
		}
		return c.getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final Contact contact = contacts.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.mul_select_contact_listitem_view, null);
			viewHolder.nameTV = (TextView) convertView
					.findViewById(R.id.mul_select_contact_name);
			viewHolder.phonesContainer = (LinearLayout) convertView
					.findViewById(R.id.mul_select_phonenum_container);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.nameTV.setText(contact.getDisplayName());
		// clear phone number container
		viewHolder.phonesContainer.removeAllViews();

		// add phone number
		List<String> phones = contact.getPhones();
		if (phones != null) {
			for (final String phone : phones) {
				View numberCBView = mInflater.inflate(
						R.layout.template_phone_num_checkbox, null);
				viewHolder.phonesContainer.addView(numberCBView);
				TextView numberTV = (TextView) numberCBView
						.findViewById(R.id.t_c_phone_number);
				CheckBox selectCB = (CheckBox) numberCBView
						.findViewById(R.id.t_c_select);
				numberTV.setText(phone);
				selectCB.setChecked(isPhoneNumberSelected(phone));
				
				selectCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							try {
								add2SelectedContacts(contact.getDisplayName(), phone);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						} else {
							try {
								removeFromSelectedContacts(phone);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		}

		return convertView;
	}

	final class ViewHolder {
		public TextView nameTV;
		public LinearLayout phonesContainer;
	}
}
