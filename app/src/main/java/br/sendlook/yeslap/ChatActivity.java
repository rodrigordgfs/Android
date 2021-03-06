package br.sendlook.yeslap;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.thunder413.datetimeutils.DateTimeUnits;
import com.github.thunder413.datetimeutils.DateTimeUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import br.sendlook.yeslap.model.ChatMessage;
import br.sendlook.yeslap.model.MesageAdapter;
import br.sendlook.yeslap.model.Message;
import br.sendlook.yeslap.model.Utils;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference datadase;
    private EditText etChat;
    private ImageView btnSendMessage, btnGoToProfile, btnGoToSettings;
    private ListView lvChat;
    private TextView tvUsername, tvStatus, tvNoMessages;
    private String uidAddressee;
    private String uidSender;
    private String usernameSender;
    private ArrayList<Message> messages;
    private ArrayAdapter<Message> adapter;
    private ValueEventListener valueEventListenerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();

        etChat = (EditText) findViewById(R.id.etChat);
        btnSendMessage = (ImageView) findViewById(R.id.btnSendMessage);
        btnGoToProfile = (ImageView) findViewById(R.id.btnGoToProfile);
        btnGoToSettings = (ImageView) findViewById(R.id.btnGoToSettings);
        lvChat = (ListView) findViewById(R.id.lvChat);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvNoMessages = (TextView) findViewById(R.id.tvNoMessages);

        getBundleIntent();
        getUserData();
        setStatus();
        checkIfHaveMessages();

        btnGoToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnGoToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundSentMessage();
                String message = etChat.getText().toString().trim();

                if (message.isEmpty()) {
                    Utils.toastyInfo(getApplicationContext(), getString(R.string.enter_a_messege_to_send));
                } else {

                    //salva para o remetente
                    mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES).child(uidSender).child(uidAddressee).push();
                    String push = mDatabase.getKey();
                    Boolean returnSender = saveMessage(uidSender, uidAddressee, message, getDateTimeNow(), mDatabase);
                    if (!returnSender) {
                        Utils.toastyError(getApplicationContext(), getString(R.string.error_send_message));
                    } else {
                        //salva para o destinatario
                        mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES).child(uidAddressee).child(uidSender).child(push);
                        Boolean returnAddressee = saveMessage(uidSender, uidAddressee, message, getDateTimeNow(), mDatabase);
                        if (!returnAddressee) {
                            Utils.toastyError(getApplicationContext(), getString(R.string.error_send_message));
                        } else {
                            etChat.setText("");
                        }
                    }

                    //Salva a Conversa para o Remetente
                    ChatMessage chatSender = new ChatMessage();
                    chatSender.setUid(uidAddressee);
                    chatSender.setName(tvUsername.getText().toString());
                    chatSender.setMessage(message);
                    setStatus();
                    Boolean returnSaveChatSender = saveChat(uidSender, uidAddressee, chatSender);
                    if (!returnSaveChatSender) {
                        Utils.toastyError(getApplicationContext(), getString(R.string.error_saving_conversation));
                    } else {
                        //Salva a Conversa para o Remetente
                        ChatMessage chatAddressee = new ChatMessage();
                        chatAddressee.setUid(uidSender);
                        chatAddressee.setName(usernameSender);
                        chatAddressee.setMessage(message);
                        setStatus();
                        Boolean returnSaveChatAdressee = saveChat(uidAddressee, uidSender, chatAddressee);
                        if (!returnSaveChatAdressee) {
                            Utils.toastyError(getApplicationContext(), getString(R.string.error_saving_conversation));
                        }
                    }


                }
                lvChat.smoothScrollToPosition(messages.size());
            }
        });

        messages = new ArrayList<>();
        adapter = new MesageAdapter(ChatActivity.this, messages);
        lvChat.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES).child(uidSender).child(uidAddressee);

        valueEventListenerMessages = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                checkIfHaveMessages();
                messages.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    //Utils.toastyInfo(getApplicationContext(), dataSnapshot.getChildren().toString());
                    Message message = data.getValue(Message.class);
                    messages.add(message);
                    lvChat.smoothScrollToPosition(messages.size());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.addValueEventListener(valueEventListenerMessages);

        getStatus();

        //String dateDB = "2018-1-5 00:00:01";
        //int diff = DateTimeUtils.getDateDiff(getDateTimeNow(), dateDB, DateTimeUnits.MINUTES);
        //Utils.toastyInfo(getApplicationContext(), String.valueOf(diff));

    }

    private void playSoundSentMessage() {
        MediaPlayer whooap = MediaPlayer.create(this, R.raw.whooap);
        whooap.start();
    }

    private String getDateTimeNow() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int yyyy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        int hh = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        int seg  =calendar.get(Calendar.SECOND);
        return yyyy + "-" + mm + "-" + dd + " " + hh + ":" + min + ":" + seg ;
    }

    private void getStatus() {
        datadase = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES_STATUS).child(mAuth.getCurrentUser().getUid()).child(uidAddressee);
        datadase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvStatus.setText(dataSnapshot.child(Utils.STATUS).getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkIfHaveMessages() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES).child(uidSender).child(uidAddressee);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    DatabaseReference database1 = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES).child(uidAddressee).child(uidSender);
                    database1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            if (dataSnapshot1.getChildrenCount() == 0) {
                                tvNoMessages.setVisibility(View.VISIBLE);
                            } else {
                                tvNoMessages.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                } else {
                    tvNoMessages.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setStatus() {
        KeyboardVisibilityEvent.setEventListener(ChatActivity.this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        DatabaseReference database;
                        if (isOpen) {
                            database = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES_STATUS).child(uidAddressee).child(uidSender);
                            Map<String, Object> status = new HashMap<>();
                            status.put(Utils.STATUS, "Typing ...");
                            database.updateChildren(status);
                        } else {
                            database = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES_STATUS).child(uidAddressee).child(uidSender);
                            database.removeValue();
                        }
                    }
                });
    }

    private boolean saveMessage(String uidSender, String uidAddressee, String message, String date, DatabaseReference datadase) {
        try {

            HashMap<String, String> msg = new HashMap<>();
            msg.put(Utils.UID_SENDER, uidSender);
            msg.put(Utils.UID_ADDRESSEE, uidAddressee);
            msg.put(Utils.MESSAGE, message);
            msg.put(Utils.DATE, date);
            msg.put(Utils.KEY, mDatabase.getKey());
            datadase.setValue(msg);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveChat(String uidSender, String uidAddressee, ChatMessage chatMessage) {
        try {

            mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.CHAT_MESSAGES);
            mDatabase.child(uidSender).child(uidAddressee).setValue(chatMessage);

            return true;
        } catch (Exception e) {
            Utils.toastyError(getApplicationContext(), e.getMessage());
            return false;
        }
    }

    private void getUserData() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(uidAddressee);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child(Utils.USERNAME).getValue(String.class);

                tvUsername.setText(username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getBundleIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            uidAddressee = bundle.getString(Utils.UID);
            uidSender = mAuth.getCurrentUser().getUid();

            mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(uidSender);
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    usernameSender = dataSnapshot.child(Utils.USERNAME).getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabase.removeEventListener(valueEventListenerMessages);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth != null) {
            setStatusOnline();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth != null) {
            setStatusOffline();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Utils.MESSAGES_STATUS).child(uidAddressee).child(uidSender);
            database.removeValue();
        }
    }

    private void setStatusOnline() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> status = new HashMap<>();
        status.put(Utils.STATUS, "online");
        mDatabase.updateChildren(status);
    }

    private void setStatusOffline() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> status = new HashMap<>();
        status.put(Utils.STATUS, "offline");
        mDatabase.updateChildren(status);
    }
}
