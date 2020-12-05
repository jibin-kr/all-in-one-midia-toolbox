package com.glofora.toolbox.fragments.wa;

import android.os.Bundle;

import com.glofora.toolbox.fragments.SavedFilesFragment;
import com.google.android.material.tabs.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.glofora.toolbox.R;
import com.glofora.toolbox.adapter.ViewPagerWAAdapter;


public class WAFragment extends Fragment {

    ViewPager viewPager;
    TabLayout tabLayout;

    public WAFragment() {
        // Required empty public constructor
    }
    private int[] tabIcons = {
            R.drawable.ic_image_white_24dp,
            R.drawable.ic_play_video_24dp,
            R.drawable.ic_whatsapp_solid
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_wa, container, false);
        viewPager = v.findViewById(R.id.viewPager_wa);
        tabLayout = v.findViewById(R.id.tab_layout_wa);

        viewPager.setOffscreenPageLimit(3);
        ViewPagerWAAdapter adapter = new ViewPagerWAAdapter(getChildFragmentManager());
        adapter.addTabs("Status",new WAImageFragment());
        adapter.addTabs("Status",new WAVideoFragment());
        adapter.addTabs("Saved",new SavedFilesFragment());


        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(viewPager);
//        LinearLayout layout = tabLayout.getTabAt(0).view; // 0 => first tab
//        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)layout.getLayoutParams();
//        lp.weight = 0f;
//        lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
//        layout.setLayoutParams(lp);
        setupTabIcons();

        return v;
    }
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon( tabIcons[0]);
        tabLayout.getTabAt(1).setIcon( tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(  tabIcons[2]);
    }

}
