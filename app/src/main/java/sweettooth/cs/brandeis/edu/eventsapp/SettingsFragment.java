package sweettooth.cs.brandeis.edu.eventsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings Fragment
 */

public class SettingsFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener  {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference databaseRef = database.getReference();
    List<String> allcategories = new ArrayList<>();
    List<String> subbedcategories = new ArrayList<>();
    ArrayList<Integer> itemsSelected  = new ArrayList<>();;

    TextView user;
    TextView subList;
    ImageView i;
    Button subbutton;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(String text) {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View settingsFragmentView = inflater.inflate(R.layout.login_screen, container, false);


        //Subscribe to catagories code starts here
        subbutton = (Button) settingsFragmentView.findViewById(R.id.subscribe);
        subList = (TextView) settingsFragmentView.findViewById(R.id.subList);
        setSubButtonAction();
        //Subscribe to catagories code ends here


        //Google Authentication code starts here
        user = (TextView) settingsFragmentView.findViewById(R.id.currentUser);
        i = (ImageView) settingsFragmentView.findViewById(R.id.imageView);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            user.setText(mAuth.getCurrentUser().getDisplayName());
        }
        else{
            user.setText("Please sign in.");
        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d("GoogleActivity", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {

                    Log.d("GoogleActivity", "onAuthStateChanged:signed_out");
                }

            }
        };
        //Google Authentication code ends here

        return settingsFragmentView;
    }

    //Get all categories in DB
    protected void getAllCategories(){

        DatabaseReference catRef = databaseRef.child("CategoriesToEvents");

        allcategories = new ArrayList<>();
        ValueEventListener userEventsListener = new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Iterable<DataSnapshot> childSnapshots = dataSnapshot.getChildren();


                    for (DataSnapshot child : childSnapshots) {

                        allcategories.add(child.getKey() + "");


                    }

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                //error w/ onDataChange
                Log.d("DummyTag", "getUsersEventIDs:onCancelled", databaseError.toException());
            }
        };
        //attaches listener to reference
        catRef.addValueEventListener(userEventsListener);



    }

    //Get a list of categories that a user is subscribed to
    protected void getSubbedCategories(){

        DatabaseReference catRef = databaseRef.child("UserToCategories").child(getUserID());

        ValueEventListener userEventsListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Iterable<DataSnapshot> childSnapshots = dataSnapshot.getChildren();

                    subbedcategories = new ArrayList<>();
                    for (DataSnapshot child : childSnapshots) {

                        subbedcategories.add(child.getKey() + "");
                    }

                }

                if(subbedcategories.size() > 0){
                    subList.setText("Categories that you are currently subscribed to: " + subbedcategories);
                }
                else {
                    subList.setText("You are currently not subscribed to any categories. Add some above!");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //error w/ onDataChange
                Log.d("DummyTag", "getUsersEventIDs:onCancelled", databaseError.toException());
            }
        };
        //attaches listener to reference
        catRef.addValueEventListener(userEventsListener);

    }

    //Sets the categories of a user
    protected String setUserCategoryToDB(ArrayList<String> a) {
        //creates child of event node and gets reference
        DatabaseReference catRef = databaseRef.child("UserToCategories").child(getUserID());
        catRef.removeValue();
        System.out.println("DATABASE REF" + catRef);
        for (String s:a){
            catRef.child(s).setValue(true);
        }

        return null;
    }

    protected String getUserID(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            //return "WaUzsjdZwcN0og4vTu00JHPhWW32";
            return mAuth.getCurrentUser().getUid();

        }
        else return null;
    }

    protected void setSubButtonAction(){

        String uid = getUserID();


        if(uid != null) {
            System.out.println(uid);
            getAllCategories();
            getSubbedCategories();

            subbutton.setOnClickListener(new View.OnClickListener() {


                public void onClick(View view) {

                    String[] catArray = new String[allcategories.size()];
                    catArray = allcategories.toArray(catArray);

                    final boolean[] checkedValues = new boolean[allcategories.size()];
                    for(String s:subbedcategories){
                        int i = allcategories.indexOf(s);
                        if(i < 0){
                            System.out.print("Somehow you are subscribed to a category that does not exist in our server!");
                        }
                        else {
                            checkedValues[i] = true;


                        }

                    }

                    //For loops and compare subbedcategories to allcategories

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Select categories to sub to. To unsub, uncheck the category.");
                    builder.setMultiChoiceItems(catArray, checkedValues,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedItemId,
                                                    boolean isSelected) {
                                    if (isSelected) {
                                        checkedValues[selectedItemId] = true;

                                    } else if (checkedValues[selectedItemId]) {

                                        checkedValues[selectedItemId] = false;

                                    }
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {


                                    ArrayList<String> newsubcategories = new ArrayList<String>();
                                    for(int i = 0; i < checkedValues.length; i++){
                                        if(checkedValues[i]){
                                            newsubcategories.add(allcategories.get(i));
                                        }
                                    }

                                    setUserCategoryToDB(newsubcategories);

                                    subbedcategories = newsubcategories;

                                    if(newsubcategories.size() > 0){
                                        subList.setText("Categories that you are currently subscribed to: " + newsubcategories);
                                    }
                                    else {
                                        subList.setText("You are currently not subscribed to any categories. Add some above!");
                                    }
                                    System.out.println("NEWSUBBED"+ subbedcategories);

                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    final Dialog dialog = builder.create();


                    dialog.show();
                }

            });
        }
        else {
            subList.setText("Please sign in to subscribe to categories.");
            subbutton.setOnClickListener(new View.OnClickListener() {


                public void onClick(View view) {
                    android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(getActivity());
                    builder1.setMessage("Please sign in to subscribe to categories.");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });



                    android.support.v7.app.AlertDialog alert11 = builder1.create();
                    alert11.show();


                }

            });

        }
    }




    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        SignInButton inbutton = (SignInButton) v.findViewById(R.id.sign_in_button);
        Button outbutton = (Button) v.findViewById(R.id.sign_out_button);
        user = (TextView) v.findViewById(R.id.currentUser);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage((FragmentActivity)getActivity(), this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addApi(AppIndex.API).build();
        }

        inbutton.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {

                Log.d("BUTTONBUTTONBUTTON", "sign in button click");

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, 1);



            }

        });

        outbutton.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {
                signOut();
                setSubButtonAction();
                user.setText("Please sign in.");

            }

        });

        
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d("TESTSUCCESS", "" + result.getSignInAccount());
            Log.d("TESTGOOGLE", ""+ result);
            Log.d("TESTSTATUS", "" + result.getStatus());
            Log.d("TESTSUCCESS", "" + result.isSuccess());

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                // Google Sign In failed
            }

        }

    }

    void signOut() {
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("GoogleActivity", "firebaseAuthWithGoogle: " + acct.getDisplayName());
        Log.d("GoogleActivity", "IDTOKEN: " + acct.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        final Uri u = acct.getPhotoUrl();

        Log.d("GoogleActivity", "Cred: " + credential);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("GoogleActivity", "signInWithCredential:onComplete:" + task.isSuccessful());
                        if(mAuth.getCurrentUser() != null){

                            user.setText(mAuth.getCurrentUser().getDisplayName());
                            setSubButtonAction();
                            //Displays profile image in imageview, but broken
                            /*try {
                                InputStream stream = getActivity().getContentResolver().openInputStream(u);
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
                                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                                i.setImageBitmap(bmp);
                            }catch (IOException e){

                                Log.d("GoogleActivity", "Error setting profile image." + e);
                            }*/

                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("GoogleAuth", "signInWithCredential", task.getException());

                        }
                        // ...
                    }
                });


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
