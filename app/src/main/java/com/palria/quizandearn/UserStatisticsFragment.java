package com.palria.quizandearn;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.palria.quizandearn.models.StatisticsDataModel;
import com.palria.quizandearn.widgets.RatingBarWidget;

import java.util.ArrayList;

public class UserStatisticsFragment extends Fragment {
    AlertDialog alertDialog;
    // Initialize variables
    TabLayout tabLayout;
    ViewPager viewPager;

    FragmentManager fragmentManager;
    LinearLayout ratingContainer;
    public UserStatisticsFragment(){}
    public UserStatisticsFragment(FragmentManager fm) {
        // Required empty public constructor
        fragmentManager = fm;
    }

/*
 *This fragment will also contain child fragments
 * There should be three tabs which are: (Bookmarks tab, My reviews tab, and All reviews tab)
 *
 *  Bookmarks tab  :                  (This tab shows other libraries and tutorials bookmarked by this user  which will be contained in a fragment),
 * Total number of libraries :       (A view that reveals the number of libraries this author created),
 * is an author flag         :       (A view that reveals whether this user is an author or not),
 * Star rating chat layout   :       (This is a layout such like playstore review layout that shows the chat bars of star rating for this user.),
 * My reviews tab        :           (This tab shows a list of this user's reviews reviewed by other users which will be contained in a fragment),
 * All reviews  tab   :              (This tab shows other authors, libraries and tutorials this user reviewed which will be contained in a fragment),
 * Total number of profile visitors: (The number of times users visited this user's profile),
 * Total number of profile reach:    (The number of times  this user's profile reached users),
 * Last seen                     :   (The last day this user visited the Learn Era platform)
 *
 *
 *
 * //if there are more, then  include them//
 * */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_user_statistics, container, false);
        initUI(parentView);

        toggleProgress(true);
initStatistics(new InitStatsListener() {
    @Override
    public void onSuccess(StatisticsDataModel statisticsDataModel) {
        //access the public methods of StatisticsDataModel class
        toggleProgress(false);

    }

    @Override
    public void onFailed(String errorMessage) {
        //it failed to load the statistics
        toggleProgress(false);
        Toast.makeText(getContext(), "Failed to init stats", Toast.LENGTH_SHORT).show();

    }
});
        return parentView;
    }

    private void initUI(View parentView){
        //use the parentView to find the by Id as in : parentView.findViewById(...);

        // assign variable
        tabLayout=parentView.findViewById(R.id.tab_layout);
        viewPager=parentView.findViewById(R.id.layout_view_pager);

        ratingContainer = parentView.findViewById(R.id.ratingBarContainer);
        // Initialize array list
        ArrayList<String> arrayList=new ArrayList<>(0);

        // Add title in array list
        arrayList.add("Basic");
        arrayList.add("Advance");
        arrayList.add("Pro");

        // Setup tab layout
        tabLayout.setupWithViewPager(viewPager);

        // Prepare view pager
        prepareViewPagerItems(viewPager,arrayList);

        alertDialog = new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(getLayoutInflater().inflate(R.layout.default_loading_layout,null))
                .create();

        //load rating bar

    }


    @Deprecated
    private void prepareViewPager(ViewPager viewPager, ArrayList<String> arrayList) {
        // Initialize main adapter
        MainAdapter adapter=new MainAdapter(fragmentManager);

        // Initialize main fragment
        TestFragment mainFragment=new TestFragment();

        // Use for loop
        for(int i=0;i<arrayList.size();i++)
        {
            // Initialize bundle
            Bundle bundle=new Bundle();

            // Put title
            bundle.putString("title",arrayList.get(i));

            // set argument
            mainFragment.setArguments(bundle);

            // Add fragment
            adapter.addFragment(mainFragment,arrayList.get(i));
            mainFragment=new TestFragment();
        }
        // set adapter
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();
    }

    private void prepareViewPagerItems(ViewPager viewPager, ArrayList<String> arrayList) {
        // Initialize main adapter
        MainAdapter adapter=new MainAdapter(fragmentManager);

        // Initialize main fragment
        TestFragment mainFragment=new TestFragment();

        // Use for loop
        for(int i=0;i<arrayList.size();i++)
        {
            // Initialize bundle
            Bundle bundle=new Bundle();

            // Put title
            bundle.putString("title",arrayList.get(i));

            // set argument
            mainFragment.setArguments(bundle);

            // Add fragment
            adapter.addFragment(mainFragment,arrayList.get(i));
            mainFragment=new TestFragment();
        }
        // set adapter
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();
    }

    private class MainAdapter extends PagerAdapter {
        // Initialize arrayList
        ArrayList<Fragment> fragmentArrayList= new ArrayList<>();
        ArrayList<String> stringArrayList=new ArrayList<>();

        int[] imageList={R.drawable.ic_baseline_bookmarks_24,R.drawable.ic_baseline_reviews_24,R.drawable.ic_baseline_photo_camera_24};

        // Create constructor
        public void addFragment(Fragment fragment,String s)
        {
            // Add fragment
            fragmentArrayList.add(fragment);
            // Add title
            stringArrayList.add(s);
        }

        public MainAdapter(FragmentManager supportFragmentManager) {

        }



        @Override
        public int getCount() {
            // Return fragment array list size
            return 3;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View item_view = layoutInflater.inflate(R.layout.welcome_activity_vpadapter_layout_item,container,false);

            ImageView imageView = item_view.findViewById(R.id.image);
            TextView titleTextView = item_view.findViewById(R.id.title);
            TextView contentTextView = item_view.findViewById(R.id.body);


            titleTextView.setText("No Data Found");
            contentTextView.setText(
                    position==0?"Please add Bookmarks to see here." : (
                            position == 1 ? "Please Submit your reviews and visit again." :
                                    "We cannot find any relating reviews for you at this time."
                            )
            );
            imageView.setImageResource(R.drawable.undraw_no_data_re_kwbl);
            imageView.getLayoutParams().height = 150;
            imageView.requestLayout();
            container.addView(item_view);



            return item_view;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((LinearLayout)object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

            return (LinearLayout) object == view;
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {

            return "";

        }


    }


    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_bookmarks_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_reviews_24);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_baseline_stars_24);
    }

    private void toggleProgress(boolean show)
    {
        if(show){
            alertDialog.show();
        }else{
            alertDialog.cancel();
        }
    }



    private  void initStatistics(InitStatsListener initStatsListener){
        GlobalConfig.getFirebaseFirestoreInstance().collection(GlobalConfig.ALL_USERS_KEY).document(GlobalConfig.getCurrentUserId())
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        initStatsListener.onFailed(e.getMessage());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        long totalNumberOfProfileVisitor = (documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_USER_PROFILE_VISITORS_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_USER_PROFILE_VISITORS_KEY) : 0L;
                        long totalNumberOfProfileReach = (documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_USER_PROFILE_REACH_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_USER_PROFILE_REACH_KEY) : 0L;
                        long totalNumberOfOneStarRate = (documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_ONE_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_ONE_STAR_RATE_KEY) : 0L;
                        long totalNumberOfTwoStarRate = (documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_TWO_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_TWO_STAR_RATE_KEY) : 0L;
                        long totalNumberOfThreeStarRate = (documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_THREE_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_THREE_STAR_RATE_KEY) : 0L;
                        long totalNumberOfFourStarRate = (documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_FOUR_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_FOUR_STAR_RATE_KEY) : 0L;
                        long totalNumberOfFiveStarRate = (documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_FIVE_STAR_RATE_KEY) != null) ?  documentSnapshot.getLong(GlobalConfig.TOTAL_NUMBER_OF_FIVE_STAR_RATE_KEY) : 0L;

                        String lastSeen = documentSnapshot.getString(GlobalConfig.LAST_SEEN_KEY);
                        boolean isAnAuthor = (documentSnapshot.get(GlobalConfig.IS_USER_AUTHOR_KEY)!=null )? (documentSnapshot.getBoolean(GlobalConfig.IS_USER_AUTHOR_KEY)) : false;

                        initStatsListener.onSuccess(new StatisticsDataModel(
                                totalNumberOfProfileVisitor,
                                totalNumberOfProfileReach,
                                isAnAuthor,
                                lastSeen,
                                totalNumberOfOneStarRate,
                                totalNumberOfTwoStarRate,
                                totalNumberOfThreeStarRate,
                                totalNumberOfFourStarRate,
                                totalNumberOfFiveStarRate
                        ));
                        int[] ratings = {(int) totalNumberOfOneStarRate,(int)totalNumberOfTwoStarRate,(int)totalNumberOfThreeStarRate,(int)totalNumberOfFourStarRate,(int)totalNumberOfFiveStarRate};

                        RatingBarWidget ratingBarWidget = new RatingBarWidget();
                        if(getContext()!=null) {
                            ratingBarWidget.setContainer(ratingContainer)
                                    .setContext(getContext())
                                    .setMax_value(5)
                                    .setRatings(ratings)
                                    .setText_color(R.color.teal_700)
                                    .render();
                        }
                    }
                });
    }

    interface InitStatsListener{
        void onSuccess(StatisticsDataModel statisticsDataModel);
        void onFailed(String errorMessage);
    }

}