package com.palria.quizandearn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;
import com.palria.quizandearn.bootservice.BootReceiver;
import com.palria.quizandearn.bootservice.BootService;
import com.palria.quizandearn.models.CurrentUserProfileDataModel;
import com.palria.quizandearn.widgets.LEBottomSheetDialog;

import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, Serializable {
    /*
    TODO enable google ads in production release but disable if in debug mode (just remove the if statement in the ads methods)
    TODO disable activity log feature for now. maybe later we enable it.
    TODO apply from: 'https://raw.githubusercontent.com/duanhong169/bintray-gradle/master/bintray.gradle'
     */
    BottomNavigationView bottomNavigationView;
    BottomAppBar bottomAppBar;
    boolean isHomeFragmentOpen = false;
    boolean isUserProfileFragmentOpen = false;
    boolean isExploreFragmentOpen = false;
    FrameLayout homeFrameLayout;
    FrameLayout exploreFrameLayout;
    FrameLayout userProfileFrameLayout;
    OnConfigurationLoadCallback onConfigurationLoadCallback;
    FloatingActionButton fab;
    Button notificationMenuButton;
    Button menu_search_button;
    View loadingIndicator;
    //learn era bottom sheet dialog
    LEBottomSheetDialog leBottomSheetDialog;
    RoundedImageView currentUserProfile;

    /**
     * loading alert dialog
     *
     */
    AlertDialog alertDialog;
    GlobalConfig.OnCurrentUserProfileFetchListener onCurrentUserProfileFetchListener;

//
//    final String MAIN_ACTIVITY_BANNER_AD_UNIT_ID = "/9395051129" ;
//    final String MAIN_ACTIVITY_INTERSTITIAL_AD_UNIT_ID = "/9817152131" ;
//    final String MAIN_ACTIVITY_REWARDED_VIDEO_AD_UNIT_ID = "/5095308492" ;
//    final String MAIN_ACTIVITY_REWARDED_INTERSTITIAL_AD_UNIT_ID = "/8094960632" ;
//    final String MAIN_ACTIVITY_APP_OPEN_AD_UNIT_ID = "/9106705696" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
            scheduleJob();
      if( !GlobalConfig.isUserLoggedIn()) {
//          if (GlobalConfig.isFirstOpen(MainActivity.this)) {
              GlobalConfig.setIsFirstOpen(MainActivity.this, false);
              Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
              startActivity(intent);
              MainActivity.this.finish();
              return;
//          }
      }
        if(GlobalConfig.isNightMode(MainActivity.this)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_main);
//        startActivity(new Intent(MainActivity.this, UploadPageActivity.class));


//        Intent intent1 = new Intent(this, BootService.class);
//        startForegroundService(intent1);

//registerReceiver(new BootReceiver(){
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        super.onReceive(context, intent);
//    }
//},new IntentFilter(Intent.ACTION_SCREEN_ON));

        Toolbar tp = findViewById(R.id.topBar);
        setSupportActionBar(tp);
//        Log.d("LEARN_ERA_USER_ID",GlobalConfig.getCurrentUserId());
        onConfigurationLoadCallback = new OnConfigurationLoadCallback() {
            @Override
            public void onLoad(DocumentSnapshot documentSnapshot) {

                ArrayList<String> allCategoryList = documentSnapshot.get(GlobalConfig.CATEGORY_LIST_KEY)!=null? (ArrayList<String>) documentSnapshot.get(GlobalConfig.CATEGORY_LIST_KEY):new ArrayList();

                GlobalConfig.setCategoryList(allCategoryList,MainActivity.this);
                bottomAppBar.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                bottomNavigationView.setOnNavigationItemSelectedListener(MainActivity.this);
                bottomNavigationView.setSelectedItemId(R.id.home_item);
            }

            @Override
            public void onFailed(String errorMessage) {

                if(GlobalConfig.isConnectedOnline(MainActivity.this)) {
                    loadConfiguration();
                }else{
                    onConfigurationLoadCallback.onSuccess();
                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onSuccess() {

                    bottomAppBar.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                    bottomNavigationView.setOnNavigationItemSelectedListener(MainActivity.this);
                    bottomNavigationView.setSelectedItemId(R.id.home_item);
                    loadingIndicator.setVisibility(View.GONE);

                    //                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();

            }
        };

            initUI();
            if(GlobalConfig.isCategorySaved(this)){
                onConfigurationLoadCallback.onSuccess();
            }

        loadConfiguration();


        initializeApp();


            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leBottomSheetDialog.show();

                }
            });
        notificationMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =new Intent(MainActivity.this, NotificationActivity.class);
                    startActivity(intent);
                }
            });
        menu_search_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                    startActivity(intent);

                }
            });

    }

//

    private void toggleProgress(boolean show) {
        if(show){
            alertDialog.show();
        }else{
            alertDialog.cancel();
        }
    }

    /**<p>initializes this activity's views</p>
     * This method must be invoked first after the inVocation of {@link AppCompatActivity#setContentView(View)} and  before any initializations
     * to avoid null pointer exception.
     * */
    private void initUI() {
        loadingIndicator = findViewById(R.id.loadingIndicatorId);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        homeFrameLayout = findViewById(R.id.homeFragment);
        exploreFrameLayout = findViewById(R.id.exploreFrameLayoutId);
        userProfileFrameLayout = findViewById(R.id.userProfileFragment);
        currentUserProfile = findViewById(R.id.currentUserProfile);


        fab = findViewById(R.id.fab);
        notificationMenuButton = findViewById(R.id.notificationMenuButtonId);
        menu_search_button = findViewById(R.id.menu_search_button);

        bottomNavigationView.setBackground(null);
//        bottomNavigationView.getIt
        alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setView(getLayoutInflater().inflate(R.layout.default_loading_layout, null))
                .create();

        leBottomSheetDialog = new LEBottomSheetDialog(this);

        leBottomSheetDialog.addOptionItem("Create Quiz", R.drawable.baseline_quiz_24, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (GlobalConfig.isUserLoggedIn()) {
                            leBottomSheetDialog.hide();
//                            Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
//                            startActivity(intent);
                        }
                    }
                }, 0);
        if(GlobalConfig.isLearnEraAccount()){
        leBottomSheetDialog.addOptionItem("Notify", R.drawable.baseline_notifications_24, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalConfig.isUserLoggedIn()) {
                    leBottomSheetDialog.hide();
                    Intent intent = new Intent(MainActivity.this, CreateNewNotificationActivity.class);
                    startActivity(intent);
                }
            }
        }, 0)
                .addOptionItem("Add Category", R.drawable.ic_baseline_post_add_24, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (GlobalConfig.isUserLoggedIn()) {
                            leBottomSheetDialog.hide();
                            Intent intent = GlobalConfig.getHostActivityIntent(MainActivity.this, null, GlobalConfig.CATEGORY_FRAGMENT_TYPE_KEY, "");
                            startActivity(intent);
                        }
                    }
                }, 0)
                .addOptionItem("Verify Accounts", R.drawable.ic_baseline_stars_24, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (GlobalConfig.isUserLoggedIn()) {
                            Intent i = new Intent(MainActivity.this, HostActivity.class);
                            i.putExtra(GlobalConfig.IS_ACCOUNT_VERIFICATION_KEY, true);
                            startActivity(GlobalConfig.getHostActivityIntent(MainActivity.this, i, GlobalConfig.AUTHORS_FRAGMENT_TYPE_KEY, null));

                            leBottomSheetDialog.hide();
                        }
                    }
                }, 0);
    }
//                .addOptionItem("New Post", R.drawable.ic_baseline_add_circle_24, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        Intent intent = new Intent(getApplicationContext() , UploadPageActivity.class);
//                        startActivity(intent);
//                        leBottomSheetDialog.hide();
//
//                    }
//                }, 0)

        leBottomSheetDialog.render();


        currentUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalConfig.createPopUpMenu(MainActivity.this,R.menu.home_overflow_menu , currentUserProfile, new GlobalConfig.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClicked(MenuItem item) {
                        /**
                         *                         if(item.getItemId()== R.id.login_item){
                         * //
                         *                             MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
                         *                             builder.setTitle("Log out?")
                         *                                     .setMessage("Are you sure you want to log out now?")
                         *                                     .setCancelable(false)
                         *                                     .setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                         *                                         @Override
                         *                                         public void onClick(DialogInterface dialogInterface, int i) {
                         *                                             FirebaseAuth.getInstance().signOut();
                         *                                             GlobalConfig.setCurrentUserId(null);
                         *                                             SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
                         *                                             SharedPreferences.Editor editor = sharedPreferences.edit();
                         *                                             editor.clear().apply();
                         *                                             Intent intent =new Intent(MainActivity.this, SignInActivity.class);
                         *                                             startActivity(intent);
                         *                                             MainActivity.super.onBackPressed();
                         *                                         }
                         *                                     })
                         *                                     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         *                                         @Override
                         *                                         public void onClick(DialogInterface dialogInterface, int i) {
                         *                                             Toast.makeText(MainActivity.this, "cancel log out.", Toast.LENGTH_SHORT).show();
                         *                                         }
                         *                                     })
                         *                                     .show();
                         *                         }
                         *                         else
                         */

//                    if(item.getItemId() == R.id.notification_item){
////                            Toast.makeText(MainActivity.this, "notification clicked.", Toast.LENGTH_SHORT).show();
//                            Intent intent =new Intent(MainActivity.this, NotificationActivity.class);
//                            startActivity(intent);
//                        }
                         if(item.getItemId() == R.id.settings_item){
                         //settings activity starts here 
//                            Toast.makeText(MainActivity.this, "Setting clicked", Toast.LENGTH_SHORT).show();
                            Intent intent =new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }else if(item.getItemId() == R.id.log_out_item){
                        //log out user code here 
                           MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
                           builder.setTitle("Log out?")
                                   .setMessage("Are you sure you want to log out now?")
                                   .setCancelable(false)
                                   .setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialogInterface, int i) {
                                           signOut();

//                                             Toast.makeText(MainActivity.this, "logged out", Toast.LENGTH_SHORT).show();
                                            //log out code goes here ]
                                       }
                                   })
                                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialogInterface, int i) {
                                           Toast.makeText(MainActivity.this, "cancel log out.", Toast.LENGTH_SHORT).show();
                                       }
                                   })
                                   .show();
                        }
                        return true;
                    }
                });
            }
        });
        


    }

    /**<p>Initializes global variables which will be shared across every activities</p>
     * This method has to be called immediately after the invocation of {@link MainActivity#initUI()}
     * */
    private void initializeApp(){
GlobalConfig.setFirebaseFirestoreInstance();
GlobalConfig.setFirebaseStorageInstance();
if(GlobalConfig.isUserLoggedIn()) {
    GlobalConfig.setCurrentUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
    fetchToken();
    initUserProfileData();
    GlobalConfig.setBlockedItemsList();
    GlobalConfig.setReportedItemsList();

}

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.exploreItemId:

                if (isExploreFragmentOpen) {
                    //Just set the frame layout visibility
                    setFrameLayoutVisibility(exploreFrameLayout);

                } else {
                    isExploreFragmentOpen = true;
                    setFrameLayoutVisibility(exploreFrameLayout);
                    ExploreFragment exploreFragment = new ExploreFragment(bottomAppBar);
                    Bundle bundle = new Bundle();
                    exploreFragment.setArguments(bundle);
                    initFragment(exploreFragment, exploreFrameLayout);
                }
                return true;


//                return true;
            case R.id.home_item:
                if (isHomeFragmentOpen) {
                    //Just set the frame layout visibility
                    setFrameLayoutVisibility(homeFrameLayout);

                } else {
                    isHomeFragmentOpen = true;

                    setFrameLayoutVisibility(homeFrameLayout);
//                    initFragment(new HomeFragment(bottomAppBar), homeFrameLayout);
                }
                return true;
            case R.id.profile_item:
                if(GlobalConfig.isUserLoggedIn()){
                if (isUserProfileFragmentOpen) {
                    //Just set the frame layout visibility
                    setFrameLayoutVisibility(userProfileFrameLayout);
                } else {
                    isUserProfileFragmentOpen = true;

                    setFrameLayoutVisibility(userProfileFrameLayout);
                    UserProfileFragment userProfileFragment = new UserProfileFragment(bottomAppBar);
                    Bundle bundle = new Bundle();
                    bundle.putString(GlobalConfig.USER_ID_KEY, GlobalConfig.getCurrentUserId());
                    userProfileFragment.setArguments(bundle);
                    initFragment(userProfileFragment, userProfileFrameLayout);
                }
                    return true;

                }else{

                    Intent intent = new Intent(MainActivity.this,SignInActivity.class);
                    startActivity(intent);
                    super.onBackPressed();

                    return false;
                }
        }
        return false;
    }

    private void initFragment(Fragment fragment,FrameLayout frameLayout){
        FragmentManager fragmentManager = getSupportFragmentManager();
       try {
           fragmentManager.beginTransaction()
                   .replace(frameLayout.getId(), fragment)
                   .commit();
       }catch(Exception e){}

    }

    private void setFrameLayoutVisibility(FrameLayout frameLayoutToSetVisible){
        homeFrameLayout.setVisibility(View.GONE);
        exploreFrameLayout.setVisibility(View.GONE);
        userProfileFrameLayout.setVisibility(View.GONE);
        frameLayoutToSetVisible.setVisibility(View.VISIBLE);
    }

    void fetchToken() {
        if( FirebaseAuth.getInstance().getCurrentUser()!=null){
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {
                GlobalConfig.setCurrentUserTokenId(getTokenResult.getToken());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //retries recursively until it succeeds
                fetchToken();
            }
        });
    }
}

    private void initUserProfileData(){
    GlobalConfig.getFirebaseFirestoreInstance().collection(GlobalConfig.ALL_USERS_KEY).document(GlobalConfig.getCurrentUserId())
            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if(value!=null){
                        DocumentSnapshot documentSnapshot = value;
                            long totalNumberOfProfileVisitor = (documentSnapshot.get(GlobalConfig.TOTAL_NUMBER_OF_USER_PROFILE_VISITORS_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_USER_PROFILE_VISITORS_KEY) : 0L;
                            long totalNumberOfProfileReach = (documentSnapshot.get(GlobalConfig.TOTAL_NUMBER_OF_USER_PROFILE_REACH_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_USER_PROFILE_REACH_KEY) : 0L;
                            long totalNumberOfOneStarRate = (documentSnapshot.get(GlobalConfig.TOTAL_NUMBER_OF_ONE_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_ONE_STAR_RATE_KEY) : 0L;
                            long totalNumberOfTwoStarRate = (documentSnapshot.get(GlobalConfig.TOTAL_NUMBER_OF_TWO_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_TWO_STAR_RATE_KEY) : 0L;
                            long totalNumberOfThreeStarRate = (documentSnapshot.get(GlobalConfig.TOTAL_NUMBER_OF_THREE_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_THREE_STAR_RATE_KEY) : 0L;
                            long totalNumberOfFourStarRate = (documentSnapshot.get(GlobalConfig.TOTAL_NUMBER_OF_FOUR_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_FOUR_STAR_RATE_KEY) : 0L;
                            long totalNumberOfFiveStarRate = (documentSnapshot.get(GlobalConfig.TOTAL_NUMBER_OF_FIVE_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_FIVE_STAR_RATE_KEY) : 0L;

                            ArrayList<String> lastViewedAuthorsId = (documentSnapshot.get(GlobalConfig.LAST_VIEWED_AUTHORS_ARRAY_KEY) != null) ? (ArrayList<String>) documentSnapshot.get(GlobalConfig.LAST_VIEWED_AUTHORS_ARRAY_KEY) : new ArrayList<String>();
                            GlobalConfig.setLastViewedAuthorsArray(lastViewedAuthorsId);


                            String lastSeen = documentSnapshot.getString(GlobalConfig.LAST_SEEN_KEY);
                            String userName = documentSnapshot.getString(GlobalConfig.USER_DISPLAY_NAME_KEY);
                            String userId = documentSnapshot.getString(GlobalConfig.USER_ID_KEY);
                            String gender = documentSnapshot.getString(GlobalConfig.USER_GENDER_TYPE_KEY);
                            String dateOfBirth = documentSnapshot.getString(GlobalConfig.USER_BIRTH_DATE_KEY);
                            String dateRegistered = documentSnapshot.getString(GlobalConfig.USER_PROFILE_DATE_CREATED_KEY);
                            String userPhoneNumber = documentSnapshot.getString(GlobalConfig.USER_CONTACT_PHONE_NUMBER_KEY);
                            String userEmail = documentSnapshot.getString(GlobalConfig.USER_CONTACT_EMAIL_ADDRESS_KEY);
                            String userResidentialAddress = documentSnapshot.getString(GlobalConfig.USER_RESIDENTIAL_ADDRESS_KEY);
                            String userCountryOfResidence = documentSnapshot.getString(GlobalConfig.USER_COUNTRY_OF_RESIDENCE_KEY);
                            long age = documentSnapshot.get(GlobalConfig.USER_AGE_KEY)!=null ? documentSnapshot.getLong(GlobalConfig.USER_AGE_KEY):0L;
                            boolean isUserProfileCompleted = documentSnapshot.get(GlobalConfig.IS_USER_PROFILE_COMPLETED_KEY)!=null ? documentSnapshot.getBoolean(GlobalConfig.IS_USER_PROFILE_COMPLETED_KEY):false;
                            String userProfileImageDownloadUrl = documentSnapshot.getString(GlobalConfig.USER_PROFILE_PHOTO_DOWNLOAD_URL_KEY);
                            boolean isAnAuthor = (documentSnapshot.get(GlobalConfig.IS_USER_AUTHOR_KEY)!=null )? (documentSnapshot.getBoolean(GlobalConfig.IS_USER_AUTHOR_KEY)) : false;
                            boolean isAccountVerified = documentSnapshot.get(GlobalConfig.IS_ACCOUNT_VERIFIED_KEY)!=null ? documentSnapshot.getBoolean(GlobalConfig.IS_ACCOUNT_VERIFIED_KEY):false;
                            boolean isAccountSubmittedForVerification = documentSnapshot.get(GlobalConfig.IS_SUBMITTED_FOR_VERIFICATION_KEY)!=null ? documentSnapshot.getBoolean(GlobalConfig.IS_SUBMITTED_FOR_VERIFICATION_KEY):false;

                            ArrayList<String> usersFollowingList = documentSnapshot.get(GlobalConfig.USERS_FOLLOWING_LIST_KEY)!=null ? (ArrayList<String>) documentSnapshot.get(GlobalConfig.USERS_FOLLOWING_LIST_KEY):new ArrayList<>();
                            for(String userFollowingId : usersFollowingList){
                                GlobalConfig.addToUsersFollowingList(MainActivity.this,userFollowingId);
                            }

                            GlobalConfig.setIsCurrentUserAccountVerified(isAccountVerified);
                            GlobalConfig.setIsAccountSubmittedForVerification(isAccountSubmittedForVerification);
                            //show current user profile there .
                        try {
                            Glide.with(MainActivity.this)
                                    .load(userProfileImageDownloadUrl)
                                    .centerCrop()
                                    .placeholder(R.drawable.default_profile)
                                    .into(currentUserProfile);
                        }catch(Exception e){};

                            new CurrentUserProfileDataModel(
                                    userName,
                                    userId,
                                    userProfileImageDownloadUrl,
                                    isAnAuthor,
                                    gender,
                                    age,
                                    dateOfBirth,
                                    dateRegistered,
                                    lastSeen,
                                    isUserProfileCompleted,
                                    userPhoneNumber,
                                    userEmail,
                                    userResidentialAddress,
                                    userCountryOfResidence,
                                    totalNumberOfProfileVisitor,
                                    totalNumberOfProfileReach,
                                    totalNumberOfOneStarRate,
                                    totalNumberOfTwoStarRate,
                                    totalNumberOfThreeStarRate,
                                    totalNumberOfFourStarRate,
                                    totalNumberOfFiveStarRate
                            );

//                   if(CurrentUserProfileDataModel.isAnAuthor()){
//                    fab.setVisibility(View.VISIBLE);
//                   }else{
//                       fab.setVisibility(View.GONE);
//                   }

                    }
                }
            });

}

    private void signOut(){
    FirebaseAuth.getInstance().signOut();
    finish();
    GlobalConfig.setCurrentUserId(null);
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
        try {
            GlobalConfig.class.newInstance();
        }catch(Exception e){};
        Intent intent = new Intent(MainActivity.this,WelcomeActivity.class);
        startActivity(intent);
        super.onBackPressed();
//         MainActivity.this.finish();
        GlobalConfig.newlyJoinedQuizList.clear();

}

private void loadConfiguration(){
    GlobalConfig.getFirebaseFirestoreInstance().collection(GlobalConfig.PLATFORM_CONFIGURATION_FILE_KEY).document(GlobalConfig.PLATFORM_CONFIGURATION_FILE_KEY).get().addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {

        }
    }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
            onConfigurationLoadCallback.onLoad(documentSnapshot);
        }
    });
}
private void scheduleJob(){

        JobScheduler jb = (JobScheduler)(getSystemService(JOB_SCHEDULER_SERVICE));
        ComponentName cN = new ComponentName(this,BootService.class);
        JobInfo jobInfo = new JobInfo.Builder(1,cN).setRequiredNetworkType(JobInfo.NETWORK_TYPE_CELLULAR).build();
        jb.schedule(jobInfo);
}

//private void validateApp(){
//    FirebaseApp.initializeApp(this);
//    FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
//
//}
//TODO: ALL_NOTIFICATIONS, PLATFORM_CONFIGURATION_FILE rules addition
private void manageNotificationAlarmBroadCast(){
    Intent intent = new Intent(this,BootReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),2002,intent,0);
    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),60*60*1000,pendingIntent);

}

private interface OnConfigurationLoadCallback{
        void onLoad(DocumentSnapshot documentSnapshot);
        void onSuccess();
        void onFailed(String errorMessage);
}

void pastes(){
    //
//            profileImageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    WriteBatch writeBatch = GlobalConfig.getFirebaseFirestoreInstance().batch();
////                    FirebaseAuth.getInstance().getCurrentUser().delete();
////        DocumentReference userProfileDocumentReference = GlobalConfig.getFirebaseFirestoreInstance().collection(GlobalConfig.ALL_USERS_KEY).document(GlobalConfig.getCurrentUserId()).collection(GlobalConfig.USER_PROFILE_KEY).document(GlobalConfig.getCurrentUserId());
//                    DocumentReference userProfileDocumentReference = GlobalConfig.getFirebaseFirestoreInstance().collection(GlobalConfig.ALL_USERS_KEY).document(authorId);
//
//                    HashMap<String,Object>userProfileDetails = new HashMap<>();
//
////                    userProfileDetails.put(GlobalConfig.TOTAL_NUMBER_OF_LIBRARY_CREATED_KEY,2L);
////                    userProfileDetails.put(GlobalConfig.TOTAL_NUMBER_OF_TUTORIAL_CREATED_KEY,0L);
////                    userProfileDetails.put(GlobalConfig.TOTAL_NUMBER_OF_AUTHOR_REVIEWS_KEY,1L);
////                    userProfileDetails.put(GlobalConfig.USER_PROFILE_DATE_CREATED_TIME_STAMP_KEY, FieldValue.serverTimestamp());
//
//                    writeBatch.delete(userProfileDocumentReference);
//
//                    writeBatch.commit();
//                }
//            });
//            numOfLibraryTextView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    WriteBatch writeBatch = GlobalConfig.getFirebaseFirestoreInstance().batch();
//
////        DocumentReference userProfileDocumentReference = GlobalConfig.getFirebaseFirestoreInstance().collection(GlobalConfig.ALL_USERS_KEY).document(GlobalConfig.getCurrentUserId()).collection(GlobalConfig.USER_PROFILE_KEY).document(GlobalConfig.getCurrentUserId());
//                    DocumentReference userProfileDocumentReference = GlobalConfig.getFirebaseFirestoreInstance().collection(GlobalConfig.ALL_USERS_KEY).document(authorId);
//
//                    HashMap<String,Object>userProfileDetails = new HashMap<>();
//
//                    userProfileDetails.put(GlobalConfig.TOTAL_NUMBER_OF_LIBRARY_CREATED_KEY,1L);
////                    userProfileDetails.put(GlobalConfig.TOTAL_NUMBER_OF_TUTORIAL_CREATED_KEY,0L);
////                    userProfileDetails.put(GlobalConfig.TOTAL_NUMBER_OF_AUTHOR_REVIEWS_KEY,1L);
////                    userProfileDetails.put(GlobalConfig.USER_PROFILE_DATE_CREATED_TIME_STAMP_KEY, FieldValue.serverTimestamp());
//
//                    writeBatch.set(userProfileDocumentReference,userProfileDetails, SetOptions.merge());
//
//                    writeBatch.commit();
//
//                }
//            });
//<?xml version="1.0" encoding="utf-8"?>
//<androidx.constraintlayout.widget.ConstraintLayout
//    xmlns:android="http://schemas.android.com/apk/res/android"
//    xmlns:app="http://schemas.android.com/apk/res-auto"
//    xmlns:tools="http://schemas.android.com/tools"
//    android:layout_width="match_parent"
//    android:background="@color/teal_700"
//    android:layout_height="match_parent"
//    tools:context=".UploadPageActivity">
//
//
//    <LinearLayout
//        android:id="@+id/topLinearLayoutId"
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        android:paddingVertical="7pt"
//        android:paddingHorizontal="5pt"
//        android:orientation="horizontal"
//        android:background="@color/teal_700"
//        app:layout_constraintStart_toStartOf="parent"
//        app:layout_constraintTop_toTopOf="parent"
//        >
//
//
// <EditText
//        android:id="@+id/pageTitleEditTextId"
//        android:layout_width="0dp"
//        android:layout_height="wrap_content"
//        android:layout_weight="1"
//        android:layout_margin="2dp"
//        android:hint="Title"
//     android:paddingHorizontal="3pt"
//     android:paddingVertical="5pt"
//        android:textColor="@color/black"
//        android:background="@drawable/default_input_bg"
//        />
//
//        <ImageView
//            android:layout_marginLeft="4pt"
//            android:layout_gravity="center"
//            app:tint="@color/white"
//            android:id="@+id/coverImagePicker"
//            android:src="@drawable/ic_baseline_add_photo_alternate_24"
//            android:layout_width="48dp"
//            android:layout_height="48dp"/>
//
//    </LinearLayout>
//    <com.makeramen.roundedimageview.RoundedImageView
//        android:id="@+id/coverImage"
//        app:riv_corner_radius="4pt"
//        app:riv_border_width="1pt"
//        app:riv_border_color="@color/white"
//        android:src="@drawable/placeholder"
//        android:layout_marginHorizontal="5pt"
//        android:scaleType="centerCrop"
//        app:layout_constraintTop_toBottomOf="@+id/topLinearLayoutId"
//        android:layout_marginVertical="2pt"
//        android:layout_width="match_parent"
//        android:layout_height="45pt"
//        />
//    <include
//        android:layout_marginTop="2pt"
//        app:layout_constraintTop_toBottomOf="@id/coverImage"
//        android:layout_height="wrap_content"
//        layout="@layout/rich_text_editor_layout"
//        android:layout_width="match_parent" />
//    <LinearLayout
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        android:orientation="horizontal"
//        app:layout_constraintEnd_toEndOf="parent"
//        app:layout_constraintBottom_toBottomOf="parent"
//        >
//
//        <ImageButton
//            android:visibility="gone"
//            android:id="@+id/addImageActionButtonId"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:backgroundTint="@android:color/transparent"
//            android:src="@drawable/ic_baseline_add_photo_alternate_24"
//            />
//
//
//        <ImageButton
//            android:visibility="gone"
//            android:id="@+id/addTodoListActionButtonId"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:backgroundTint="@android:color/transparent"
//            android:src="@drawable/ic_baseline_format_list_bulleted_24"
//            />
//
//
//        <ImageButton
//            android:visibility="gone"
//            android:id="@+id/addTableActionButtonId"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:backgroundTint="@android:color/transparent"
//            android:src="@drawable/ic_baseline_table_chart_24"
//            />
//
//
//
//    </LinearLayout>
//
//    <com.google.android.material.floatingactionbutton.FloatingActionButton
//        android:layout_width="wrap_content"
//        android:id="@+id/btn"
//        android:layout_margin="10pt"
//        android:tint="@color/teal_700"
//        android:backgroundTint="@color/teal_700"
//        android:src="@drawable/baseline_check_circle_outline_24"
//        app:layout_constraintEnd_toEndOf="parent"
//        app:layout_constraintBottom_toBottomOf="parent"
//        android:layout_height="wrap_content"/>
//
//
//    <Switch
//        android:id="@+id/visibilitySwitchId"
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        android:text="Make public"
//        android:textStyle="bold"
//        android:textColor="@color/black"
//        android:fontFamily="sans-serif-condensed-medium"
//        app:layout_constraintStart_toStartOf="parent"
//        app:layout_constraintBottom_toBottomOf="parent"
//        tools:ignore="UseSwitchCompatOrMaterialXml"
//        android:checked="true"
//        android:paddingEnd="200dp"
//        android:background="@color/white"
//        />
//</androidx.constraintlayout.widget.ConstraintLayout>
}
}
