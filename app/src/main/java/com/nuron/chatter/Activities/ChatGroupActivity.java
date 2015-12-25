package com.nuron.chatter.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.nuron.chatter.Adapters.ChatSingleMessageAdapter;
import com.nuron.chatter.Model.ChatSingleMessage;
import com.nuron.chatter.R;
import com.parse.ParseACL;
import com.parse.ParseException;
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

/**
 * Created by nuron on 26/12/15.
 */
public class ChatGroupActivity extends AppCompatActivity {

    private static final String TAG = ChatSingleActivity.class.getSimpleName();
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
    ChatSingleMessageAdapter chatSingleMessageAdapter;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        context = this;
        receiveId = getIntent().getExtras().getString(ChatSingleMessage.RECEIVER_ID);
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

        chatSingleMessageAdapter = new ChatSingleMessageAdapter(this, receiveId);
        chatRecyclerView.setAdapter(chatSingleMessageAdapter);

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

        saveChatMessage(chatEditText.getText().toString(), null);
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

        file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                imageFileName + ".jpg");

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

            if (file.exists()) {

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

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
                        saveChatMessage(null, (String) map.get("public_id"));
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

    private void saveChatMessage(String chatText1, String imageID1) {

        String chatText;
        String imageID;

        if (imageID1 == null || imageID1.isEmpty()) {
            imageID = "";
        } else {
            imageID = imageID1;
        }

        if (chatText1 == null || chatText1.isEmpty()) {
            chatText = "";
        } else {
            chatText = chatText1;
        }

        Log.d(TAG, "Saving chat Message");

        ChatSingleMessage chatSingleMessage = new ChatSingleMessage();
        chatSingleMessage.setChatText(chatText);
        chatSingleMessage.setSenderId(senderId);
        chatSingleMessage.setReceiverId(receiveId);
        chatSingleMessage.setSenderName(
                ParseUser.getCurrentUser().getString(LoginActivity.USER_ACCOUNT_NAME));
        chatSingleMessage.setImageId(imageID);

        ParseACL acl = new ParseACL();
        acl.setReadAccess(ParseUser.getCurrentUser(), true);
        acl.setReadAccess(receiveId, true);
        acl.setWriteAccess(ParseUser.getCurrentUser(), true);

        chatSingleMessage.setACL(acl);

        chatSingleMessageAdapter.addData(chatSingleMessage);
        chatSingleMessageAdapter.notifyItemInserted(chatSingleMessageAdapter.getItemCount());
        chatRecyclerView.scrollToPosition(chatSingleMessageAdapter.getItemCount() - 1);
        chatEditText.setText("");

        allSubscriptions.add(ParseObservable.save(chatSingleMessage)
                .subscribe(new Subscriber<ChatSingleMessage>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Chat Message saved");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(ChatSingleMessage chatSingleMessage) {
                    }
                })
        );
    }

    public void loadChatMessages() {

        emptyItemsLayout.setVisibility(View.GONE);
        progressWheel.spin();
        chatSingleMessageAdapter.clear();
        ParseQuery<ChatSingleMessage> senderQuery = ParseQuery.getQuery(ChatSingleMessage.class);
        senderQuery.whereEqualTo(ChatSingleMessage.SENDER_ID,
                ParseUser.getCurrentUser().getObjectId());
        senderQuery.whereEqualTo(ChatSingleMessage.RECEIVER_ID, receiveId);

        ParseQuery<ChatSingleMessage> receiverQuery = ParseQuery.getQuery(ChatSingleMessage.class);
        receiverQuery.whereEqualTo(ChatSingleMessage.SENDER_ID, receiveId);
        receiverQuery.whereEqualTo(ChatSingleMessage.RECEIVER_ID,
                ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ChatSingleMessage>> queries = new ArrayList<>();
        queries.add(senderQuery);
        queries.add(receiverQuery);

        final ParseQuery<ChatSingleMessage> messageQuery = ParseQuery.or(queries);
        messageQuery.setLimit(50);
        messageQuery.addAscendingOrder("createdAt");

        allSubscriptions.add(Observable.interval(0, 30, TimeUnit.SECONDS, Schedulers.newThread())
                .map(new Func1<Long, List<ChatSingleMessage>>() {
                    @Override
                    public List<ChatSingleMessage> call(Long aLong) {
                        Log.d(TAG, "Starting polling");
                        chatSingleMessageAdapter.clear();
                        try {
                            return messageQuery.find();
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ChatSingleMessage>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Exception during getting messages : " + e);
                        Toast.makeText(ChatGroupActivity.this,
                                "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<ChatSingleMessage> chatSingleMessageList) {
                        if (chatSingleMessageList.size() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                            progressWheel.stopSpinning();
                        } else {

                            for (ChatSingleMessage chatSingleMessage : chatSingleMessageList) {
                                chatSingleMessageAdapter.addData(chatSingleMessage);
                            }

                            progressWheel.stopSpinning();
                            chatSingleMessageAdapter.notifyDataSetChanged();
                            chatRecyclerView.scrollToPosition(chatSingleMessageList.size() - 1);
                        }
                    }

                })
        );

    }

}
