package com.palria.quizandearn;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

public class HostActivity extends AppCompatActivity {
    Intent intent;
    String FRAGMENT_TYPE = "";
    FrameLayout hostFrameLayout;
    String userId = "";
    String libraryOpenType = "";
    String libraryCategory = "";
    String authorId = "";

    MaterialToolbar materialToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        initUI();
        fetchIntentData();
        openIncomingFragment();
    }

    private void initUI(){
        materialToolbar=findViewById(R.id.topBar);
        hostFrameLayout = findViewById(R.id.hostFrameLayoutId);
        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }



    void fetchIntentData(){
         intent = getIntent();
        FRAGMENT_TYPE = intent.getStringExtra(GlobalConfig.FRAGMENT_TYPE_KEY);
        userId = intent.getStringExtra(GlobalConfig.USER_ID_KEY);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openIncomingFragment(){
        Bundle bundle = new Bundle();

        switch(FRAGMENT_TYPE) {
            case GlobalConfig.USER_PROFILE_FRAGMENT_TYPE_KEY:
                bundle = new Bundle();
                bundle.putString(GlobalConfig.USER_ID_KEY, userId);
                materialToolbar.setTitle("Profile");
                initFragment(bundle, new UserProfileFragment(null));
                break;
            case GlobalConfig.AUTHORS_FRAGMENT_TYPE_KEY:
                bundle = new Bundle();
                bundle.putBoolean(GlobalConfig.IS_AUTHOR_OPEN_TYPE_KEY, true);
                if(getIntent().getBooleanExtra(GlobalConfig.IS_ACCOUNT_VERIFICATION_KEY,false)) {
                    materialToolbar.setTitle("Verify accounts");

                    bundle.putBoolean(GlobalConfig.IS_AUTHOR_OPEN_TYPE_KEY, false);
                    bundle.putBoolean(GlobalConfig.IS_ACCOUNT_VERIFICATION_KEY, getIntent().getBooleanExtra(GlobalConfig.IS_ACCOUNT_VERIFICATION_KEY,false));

                }else{
                    materialToolbar.setTitle("Authors");
                }
                initFragment(bundle, new AllUsersFragment());
                break;
            case GlobalConfig.CATEGORY_FRAGMENT_TYPE_KEY:
                bundle = new Bundle();
                materialToolbar.setTitle("Category");
                initFragment(bundle, new AddCategoryFragment());
                break;
            case GlobalConfig.QUIZ_FRAGMENT_TYPE_KEY:
                bundle = new Bundle();
                materialToolbar.setTitle("Join Ongoing Quiz");
//                initFragment(bundle, new AllQuizFragment());
                break;
        }
    }

    void initFragment(Bundle bundle,Fragment fragment){
        fragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(hostFrameLayout.getId(),fragment)
                .commit();
    }
}