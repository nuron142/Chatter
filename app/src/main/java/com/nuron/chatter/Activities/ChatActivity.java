package com.nuron.chatter.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.nuron.chatter.Adapters.ChatSingleAdapter;
import com.nuron.chatter.Model.ChatSingle;
import com.nuron.chatter.R;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 142;
    public static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 840;

    @Bind(R.id.chat_recycler_view)
    RecyclerView chatRecyclerView;

    @Bind(R.id.chat_editText)
    EditText chatEditText;

    @Bind(R.id.empty_items_layout)
    TextView emptyItemsLayout;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    Context context;
    CompositeSubscription allSubscriptions;
    Subscription imageUploadSub;
    String senderId, receiveId, receiverName;
    ChatSingleAdapter chatSingleAdapter;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        context = this;
        receiveId = getIntent().getExtras().getString(ChatSingle.RECEIVER_ID);
        receiverName = getIntent().getExtras().getString(LoginActivity.USER_ACCOUNT_NAME);
        senderId = ParseUser.getCurrentUser().getObjectId();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(receiverName);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatSingleAdapter = new ChatSingleAdapter(this, receiveId);
        chatRecyclerView.setAdapter(chatSingleAdapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        } else if (menuItem.getItemId() == R.id.chat_pick_image) {
            Log.d(TAG, "Pick image clicked");
            pickGalleryImage();

        } else if (menuItem.getItemId() == R.id.chat_camera_image) {
            Log.d(TAG, "Camera clicked");
            runCameraProgram();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.chat_attach_image, menu);
        return true;
    }


    @OnClick(R.id.send_message_button)
    public void sendMessage() {

        if (chatEditText.getText().length() < 1) {
            Toast.makeText(this, "Message can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatSingle chatSingle = new ChatSingle();
        chatSingle.setChatText(chatEditText.getText().toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        chatSingle.setSentDate(dateFormat.format(date));
        chatSingle.setSenderId(senderId);
        chatSingle.setReceiverId(receiveId);
        chatSingle.setReceiverName(receiverName);

        ParseACL acl = new ParseACL();
        acl.setReadAccess(ParseUser.getCurrentUser(), true);
        acl.setReadAccess(receiveId, true);
        acl.setWriteAccess(ParseUser.getCurrentUser(), true);

        chatSingle.setACL(acl);

        chatSingleAdapter.addData(chatSingle);
        chatSingleAdapter.notifyItemInserted(chatSingleAdapter.getItemCount());
        chatEditText.setText("");

        allSubscriptions.add(ParseObservable.save(chatSingle)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseObject>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(getApplicationContext(),
                                "Successfully saved", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context,
                                "Couldn't save. Please try again", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(ParseObject parseObject) {

                    }
                })
        );

    }

    @Override
    public void onStart() {
        super.onStart();
        if (allSubscriptions == null) {
            allSubscriptions = new CompositeSubscription();
        }
        loadChatMessages();

    }

    @Override
    public void onStop() {
        super.onStop();

        if (allSubscriptions != null && !allSubscriptions.isUnsubscribed()) {

            allSubscriptions.unsubscribe();
            allSubscriptions = null;
        }
    }

    private void pickGalleryImage() {

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
    }


    private void runCameraProgram() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = senderId + timeStamp + "_img";

        file = new File(getExternalFilesDir(null), imageFileName + ".jpg");

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

            if (file.exists()) {
                uploadImage();
            } else {
                Toast.makeText(this, "Couldn't save picture", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == PICK_IMAGE_ACTIVITY_REQUEST_CODE) {

            if (data != null) {
                final Uri imageUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(
                        imageUri, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();


                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    file = new File(filePath);

                    if (file.exists()) {
                        uploadImage();
                    } else {
                        Toast.makeText(this, "Couldn't save picture", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public void uploadImage() {

        Log.d(TAG, "Uploading image");

        if (allSubscriptions == null) {
            allSubscriptions = new CompositeSubscription();
        }

        final Cloudinary cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(this));

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Uploading picture");
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelImageUplaod();
            }
        });

        imageUploadSub = Observable.fromCallable(
                new Func0<Map>() {
                    @Override
                    public Map call() {
                        try {
                            Log.d(TAG, "Image uploading");
                            return cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                })
                .filter(new Func1<Map, Boolean>() {
                    @Override
                    public Boolean call(Map map) {
                        return map != null;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Image onCompleted");
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Image onError");
                    }

                    @Override
                    public void onNext(Map map) {
                        saveImageChat((String) map.get("public_id"));
                    }
                });
        allSubscriptions.add(imageUploadSub);
    }

    private void cancelImageUplaod() {

        if (imageUploadSub != null && !imageUploadSub.isUnsubscribed()) {
            imageUploadSub.unsubscribe();
            imageUploadSub = null;
        }
    }

    private void saveImageChat(String imageID) {

        Log.d(TAG, "Saving chatSingle image data");

        ChatSingle chatSingle = new ChatSingle();
        chatSingle.setChatText("");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        chatSingle.setSentDate(dateFormat.format(date));
        chatSingle.setSenderId(senderId);
        chatSingle.setReceiverId(receiveId);
        chatSingle.setReceiverName(receiverName);

        chatSingle.setImageId(imageID);

        ParseACL acl = new ParseACL();
        acl.setReadAccess(ParseUser.getCurrentUser(), true);
        acl.setReadAccess(receiveId, true);
        acl.setWriteAccess(ParseUser.getCurrentUser(), true);

        chatSingle.setACL(acl);

        allSubscriptions.add(ParseObservable.save(chatSingle)
                .subscribe(new Subscriber<ChatSingle>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ChatSingle chatSingle) {

                        Log.d(TAG, "Saving chatSingle onNext");
                        chatSingleAdapter.addData(chatSingle);
                        chatSingleAdapter.notifyItemInserted(chatSingleAdapter.getItemCount());
                        chatEditText.setText("");
                    }
                })
        );
    }

    public void loadChatMessages() {

        emptyItemsLayout.setVisibility(View.GONE);
        progressWheel.spin();
        chatSingleAdapter.clear();
        ParseQuery<ChatSingle> senderQuery = ParseQuery.getQuery(ChatSingle.class);
        senderQuery.whereEqualTo(ChatSingle.SENDER_ID,
                ParseUser.getCurrentUser().getObjectId());
        senderQuery.whereEqualTo(ChatSingle.RECEIVER_ID, receiveId);

        ParseQuery<ChatSingle> receiverQuery = ParseQuery.getQuery(ChatSingle.class);
        receiverQuery.whereEqualTo(ChatSingle.SENDER_ID, receiveId);
        receiverQuery.whereEqualTo(ChatSingle.RECEIVER_ID,
                ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ChatSingle>> queries = new ArrayList<>();
        queries.add(senderQuery);
        queries.add(receiverQuery);

        final ParseQuery<ChatSingle> messageQuery = ParseQuery.or(queries);
        messageQuery.setLimit(50);
        messageQuery.addAscendingOrder("createdAt");

        allSubscriptions.add(Observable.interval(0, 30, TimeUnit.SECONDS, Schedulers.newThread())
                .map(new Func1<Long, List<ChatSingle>>() {
                    @Override
                    public List<ChatSingle> call(Long aLong) {
                        Log.d(TAG, "Starting polling");
                        chatSingleAdapter.clear();
                        try {
                            return messageQuery.find();
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ChatSingle>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Exception during getting messages : " + e);
                        Toast.makeText(ChatActivity.this,
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<ChatSingle> chatSingleList) {
                        if (chatSingleList.size() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                            progressWheel.stopSpinning();
                        } else {

                            for (ChatSingle chatSingle : chatSingleList) {
                                chatSingleAdapter.addData(chatSingle);
                            }

                            progressWheel.stopSpinning();
                            chatSingleAdapter.notifyDataSetChanged();
                            chatRecyclerView.scrollToPosition(chatSingleList.size() - 1);
                        }
                    }

                })
        );

    }

}
