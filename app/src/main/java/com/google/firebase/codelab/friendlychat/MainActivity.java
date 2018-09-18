/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.PersonBuilder;
import android.os.AsyncTask;
import android.graphics.Bitmap;
import java.io.InputStream;
import android.graphics.BitmapFactory;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.checked;
import static com.google.firebase.codelab.friendlychat.R.layout.perfil;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }

//    public static class PseudonimoViewHolder extends RecyclerView.ViewHolder {
//        public TextView pseudonimoTextView;
//
//        public MessageViewHolder(View v) {
//            super(v);
//            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
//            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
//            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
//        }
//    }

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 100 ;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    // Firebase instance variables
    private  FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
//    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;

    private FirebaseRecyclerAdapter mFirebaseAdapter;

    private int radioPerfilOpcao;
    private String userID;

    private String pseudonimo;

    Button btnGoActivity_main, btnGoActivity_main2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }

            //LoadInserePseudonimo(mFirebaseUser);
        }
    }

    public void LoadPerfil(){
        setContentView(perfil);
        ImageView ivPhoto = (ImageView) findViewById(R.id.perfilfoto);
        TextView tvName = (TextView) findViewById(R.id.perfilnome);
        TextView tvEmail = (TextView) findViewById(R.id.perfilemail);
        final TextView tvPseudonimo = (TextView) findViewById(R.id.perfilpseudonimo);
        final RadioGroup perfilradiotipo = (RadioGroup) findViewById(R.id.perfilradiotipo);
        final RadioButton radioperfilpessoal = (RadioButton) findViewById(R.id.radioperfilpessoal);
        final RadioButton radioperfilpseudonimo = (RadioButton) findViewById(R.id.radioperfilpseudonimo);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url

            tvName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());



            // COMECANDO A MEXER AQUI

            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Pseudonimos").child(userID.toString());




           // mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages").child();
            //DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Pseudonimos").child(userID.toString());


            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        for (DataSnapshot pseudonimoSnapshot : dataSnapshot.getChildren()) {
                            //The key of the question
                            //String pseudonimoKey = pseudonimoSnapshot.getKey();
                            //And if you want to access the rest:
                            String testeNomePseudonimo = pseudonimoSnapshot.child("nomePseudonimo").getValue(String.class);
                            String testeRadioOpcao = pseudonimoSnapshot.child("radioOpcao").getValue(String.class);
                            tvPseudonimo.setText(testeNomePseudonimo);

                            // PROXIMO PASSO: RECUPERAR O DADO DO RADIO OPCAO DO FIREBASE

                            //String oi = testeRadioOpcao;
                           // perfilradiotipo.check(Integer.parseInt(testeRadioOpcao));
                        }
                    }
                    else{
                        tvPseudonimo.setText("Pseudonimo pendente");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });

            perfilradiotipo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup perfilradiotipo, int checkedId) {
                    Log.d(TAG, "changed");
                    radioPerfilOpcao = perfilradiotipo.getCheckedRadioButtonId();

                    Map<String, Object> optionUpdates = new HashMap<String, Object>();
                    optionUpdates.put("radioOpcao", radioPerfilOpcao);

                    ref.updateChildren(optionUpdates);


                }
            });

            Glide.with(MainActivity.this)
                    .load(mPhotoUrl)
                    .into(ivPhoto);

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
        }

    }

    public void LoadInserePseudonimo(final FirebaseUser mFirebaseUser){

        setContentView(R.layout.pseudonimo);
        Button buttonP = (Button) findViewById(R.id.buttonP);
        final EditText editTextP = (EditText) findViewById(R.id.editTextP);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("Pseudonimos").child(userID);


        buttonP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String frase = editTextP.getText().toString();
                if(frase != null && !frase.isEmpty()){
                    Pseudonimo pseudo = new Pseudonimo(userID, frase);
                    //mFirebaseDatabaseReference.push().setValue(pseudo);

                    Map<String, Object> pseudoUpdates = new HashMap<String, Object>();
                    pseudoUpdates.put("nomePseudonimo", frase);

                    mFirebaseDatabaseReference.updateChildren(pseudoUpdates);
                }
                LoadRooms();
            }
        });

    }

    public void LoadRooms() {
        setContentView(R.layout.room);
        Button btnGoActivity_main1 = (Button) findViewById(R.id.btnRoom1);
        btnGoActivity_main1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setContentView(R.layout.activity_main);
                String reference = "Beleza";
                StartRoom(reference);

            }
        });

       Button btnGoActivity_main2 = (Button) findViewById(R.id.btnRoom2);
        btnGoActivity_main2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reference = "Trabalho";
                StartRoom(reference);

            }
        });

        Button btnGoActivity_main3 = (Button) findViewById(R.id.btnRoom3);
        btnGoActivity_main3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reference = "Violencia";
                StartRoom(reference);

            }
        });

        Button btnGoActivity_main4 = (Button) findViewById(R.id.btnRoom4);
        btnGoActivity_main4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reference = "Assedio";
                StartRoom(reference);

            }
        });

        Button btnGoActivity_main5 = (Button) findViewById(R.id.btnRoom5);
        btnGoActivity_main5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reference = "Saude";
                StartRoom(reference);

            }
        });

        Button btnGoActivity_main6 = (Button) findViewById(R.id.btnRoom6);
        btnGoActivity_main6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reference = "Maternidade";
                StartRoom(reference);
            }
        });

    }

    //Inicializar sala 1 (activity_main com item_message.xml)
    public void StartRoom(String reference){
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        // New child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages").child(reference);

        Query query = FirebaseDatabase.getInstance().getReference("Messages").child(reference);


        FirebaseRecyclerOptions<FriendlyMessage> options =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(query, FriendlyMessage.class)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {

            protected void populateViewHolder(MessageViewHolder viewHolder,
                                              FriendlyMessage friendlyMessage, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.messageTextView.setText(friendlyMessage.getText());
                viewHolder.messengerTextView.setText(friendlyMessage.getName());
                if (friendlyMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView
                            .setImageDrawable(ContextCompat
                                    .getDrawable(MainActivity.this,
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(MainActivity.this)
                            .load(friendlyMessage.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }
            }

            @Override
            protected void onBindViewHolder(MessageViewHolder holder, int position, FriendlyMessage model) {
                populateViewHolder(holder,model,position);
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);
                return new MessageViewHolder(view);
            }

        };

        mFirebaseAdapter.startListening();

        /*
        FirebaseListOptions<FriendlyMessage> options = new FirebaseListOptions.Builder<FriendlyMessage>()
                .setQuery(query, FriendlyMessage.class)
                .build();

        mFirebaseAdapter = new FirebaseListAdapter<FriendlyMessage>(options) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder,
                                              FriendlyMessage friendlyMessage, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.messageTextView.setText(friendlyMessage.getText());
                viewHolder.messengerTextView.setText(friendlyMessage.getName());
                if (friendlyMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView
                            .setImageDrawable(ContextCompat
                                    .getDrawable(MainActivity.this,
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(MainActivity.this)
                            .load(friendlyMessage.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }
            }
        };

*/

        /*
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage,
                MessageViewHolder>(
                FriendlyMessage.class,
                R.layout.item_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference){ //MESSAGES_CHILD

            protected void populateViewHolder(MessageViewHolder viewHolder,
                                              FriendlyMessage friendlyMessage, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.messageTextView.setText(friendlyMessage.getText());
                viewHolder.messengerTextView.setText(friendlyMessage.getName());
                if (friendlyMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView
                            .setImageDrawable(ContextCompat
                                    .getDrawable(MainActivity.this,
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(MainActivity.this)
                            .load(friendlyMessage.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }
            }

            @Override
            protected void onBindViewHolder(MessageViewHolder holder, int position, FriendlyMessage model) {
                populateViewHolder(holder,model,position);
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);
                return new MessageViewHolder(view);
            }
        };

*/

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, mPhotoUrl);
                mFirebaseDatabaseReference.push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

    }

    public void onBackPressed(){ //Botão BACK padrão do android
        startActivity(new Intent(this, MainActivity.class)); //O efeito ao ser pressionado do botão (no caso abre a activity)
        return;
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mFirebaseAdapter != null)
            mFirebaseAdapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.


        if (mFirebaseAdapter != null)
            mFirebaseAdapter.startListening();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null){
            //LoadRooms();
            userID = mFirebaseUser.getUid();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Pseudonimos").child(userID.toString());


            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()) {
                        LoadInserePseudonimo(mFirebaseUser);
                    }
                    else {
                        LoadRooms();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                mFirebaseUser = null;
                LoginManager.getInstance().logOut();
                //Auth.GoogleSignInApi.signOut(mGoogleApiClient); // foi transferido para o signInActivity.java
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.menuVoltar:
                desconectar();
                setContentView(R.layout.room);
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.btnPerfil:
               LoadPerfil();
                return true;
            case R.id.btnSalas:
                desconectar();
                LoadRooms();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public void desconectar(){
        if(mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

}
