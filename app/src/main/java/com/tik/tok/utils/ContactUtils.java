package com.tik.tok.utils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created by Quinin on 2019-11-19.
 **/
public class ContactUtils {

    private static final String TAG = "ContactUtils";

    public static void addContacts(Context context, String name, String phone) {
        try {
            //该用户是否存在，存在的话，就追加号码，不存在新增联系人
            boolean isExist = isThePhoneExist(context, phone);
            //long rawContactId = getContactsId(context, name);
            long rawContactId = isExist ? -1 : 0;
            ContentValues values = new ContentValues();

            if (rawContactId == 0) {
                //插入raw_contacts表，并获取_id属性
                Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
                ContentResolver resolver = context.getContentResolver();
                rawContactId = ContentUris.parseId(resolver.insert(uri, values));

                //插入data表
                uri = Uri.parse("content://com.android.contacts/data");

                //add Name
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/name");
                values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
                resolver.insert(uri, values);
                values.clear();

                //写入头像
               /* Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_poraital);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();*/
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                //values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, out.toByteArray());
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

                //写入手机号码
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                //插入data表
                Uri dataUri = Uri.parse("content://com.android.contacts/data");
                context.getContentResolver().insert(dataUri, values);
            }

        } catch (Exception e) {
            Log.i(TAG, "insertConstacts:  e = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteAllContacts(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            if (cursor == null)
                return;
            ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
            while (cursor.moveToNext()) {
                long cursorIndex = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID));
                operationList.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(
                        ContactsContract.RawContacts.CONTENT_URI, cursorIndex))
                        .withYieldAllowed(true)
                        .build());
            }

            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    /**
     * 判断某个手机号是否存在
     */
    public static boolean isThePhoneExist(Context context, String phoneNum) {
        //uri=  content://com.android.contacts/data/phones/filter/#
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + phoneNum);
            ContentResolver resolver = context.getContentResolver();
            cursor = resolver.query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME},
                    null, null, null); //从raw_contact表中返回display_name
            if (cursor.moveToFirst()) {
                //Log.i(TAG, "name=" + cursor.getString(0) + " , phoneNum = " + phoneNum);
                cursor.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }
}
