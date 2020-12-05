package com.glofora.toolbox.recycler;


import android.content.Context;
import android.os.Build;

import androidx.core.view.MenuItemCompat;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.glofora.toolbox.GenericAdapter;
import com.glofora.toolbox.HelperMethods;
import com.glofora.toolbox.InstanceHandler;
import com.glofora.toolbox.R;
import com.glofora.toolbox.adapter.WAImageAdapter;
import com.glofora.toolbox.adapter.WAVideoAdapter;
import com.glofora.toolbox.fragments.bwa.BWAImageFragment;
import com.glofora.toolbox.fragments.bwa.BWAVideoFragment;
import com.glofora.toolbox.fragments.wa.WAImageFragment;
import com.glofora.toolbox.fragments.wa.WAVideoFragment;
import com.glofora.toolbox.model.WAImageModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SONU on 22/03/16.
 */
public class ToolbarActionModeCallback implements ActionMode.Callback {

    private Context context;
    private WAVideoAdapter waVideoAdapter;
    private WAImageAdapter waImageAdapter;
    private ArrayList<WAImageModel> message_models;
    WAImageFragment waImageFragment;
    WAVideoFragment waVideoFragment;
    BWAImageFragment bwaImageFragment;
    BWAVideoFragment bwaVideoFragment;
    String s = "";
    List<File> mSelected;


    public ToolbarActionModeCallback(Context context, GenericAdapter<?> adapter, ArrayList<WAImageModel> message_models, InstanceHandler<?> instance) {
        this.context = context;
        this.waVideoAdapter = waVideoAdapter;
        this.message_models = message_models;
        s = instance.getValue().getClass().getSimpleName();
        switch (s) {
            case "WAVideoFragment":
                waVideoFragment = (WAVideoFragment) instance.getValue();
                waVideoAdapter = (WAVideoAdapter) adapter.getValue();

                break;
            case "BWAImageFragment":
                bwaImageFragment = (BWAImageFragment) instance.getValue();
                waImageAdapter = (WAImageAdapter) adapter.getValue();
                break;
            case "WAImageFragment":
                waImageFragment = (WAImageFragment) instance.getValue();
                waImageAdapter = (WAImageAdapter) adapter.getValue();
                break;
            case "BWAVideoFragment":
                bwaVideoFragment = (BWAVideoFragment) instance.getValue();
                waVideoAdapter = (WAVideoAdapter) adapter.getValue();
                break;
        }

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.selection_menu, menu);//Inflate the menu_main over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
        //So here show action menu_main according to SDK Levels
        if (Build.VERSION.SDK_INT < 11) {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_save), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {

            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        SparseBooleanArray selectedIds;
        int size;
        switch (item.getItemId()) {
            case R.id.action_delete:
                switch (s) {
                    case "WAVideoFragment":
                        selectedIds = waVideoAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                String str = (String) waVideoAdapter.getItem(selectedIds.keyAt(size)).getPath();
                                File file = new File(str);
                                try {
                                    if (file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        waVideoFragment.deleteRows();
                        waVideoFragment.refresh();
                        mode.finish();
                        return true;
                    case "BWAVideoFragment":
                        selectedIds = waVideoAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                String str = (String) waVideoAdapter.getItem(selectedIds.keyAt(size)).getPath();
                                File file = new File(str);
                                try {
                                    if (file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        bwaVideoFragment.deleteRows();
                        bwaVideoFragment.refresh();
                        mode.finish();
                        return true;
                    case "WAImageFragment":
                        selectedIds = waImageAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                String str = (String) waImageAdapter.getItem(selectedIds.keyAt(size)).getPath();
                                File file = new File(str);
                                try {
                                    if (file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        waImageFragment.deleteRows();
                        waImageFragment.refresh();
                        mode.finish();
                        return true;
                    case "BWAImageFragment":
                        selectedIds = waImageAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                String str = (String) waImageAdapter.getItem(selectedIds.keyAt(size)).getPath();
                                File file = new File(str);
                                try {
                                    if (file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        bwaImageFragment.deleteRows();
                        bwaImageFragment.refresh();
                        mode.finish();
                        return true;
                }
            case R.id.action_save:
                switch (s) {
                    case "WAVideoFragment":
                        selectedIds = waVideoAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                HelperMethods.transfer(new File((String) waVideoAdapter.getItem(selectedIds.keyAt(size)).getPath()));
                            }
                        }
                        Toast.makeText(context, "Done! :)", Toast.LENGTH_SHORT).show();
                        mode.finish();//Finish action mode

                        return true;
                    case "BWAVideoFragment":
                        selectedIds = waVideoAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                HelperMethods.transfer(new File((String) waVideoAdapter.getItem(selectedIds.keyAt(size)).getPath()));
                            }
                        }
                        Toast.makeText(context, "Done! :)", Toast.LENGTH_SHORT).show();
                        mode.finish();//Finish action mode

                        return true;
                    case "WAImageFragment":
                        selectedIds = waImageAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                HelperMethods.transfer(new File((String) waImageAdapter.getItem(selectedIds.keyAt(size)).getPath()));
                            }
                        }
                        Toast.makeText(context, "Done! :)", Toast.LENGTH_SHORT).show();
                        mode.finish();//Finish action mode

                        return true;
                    case "BWAImageFragment":
                        selectedIds = waImageAdapter.getSelectedIds();
                        for (size = selectedIds.size() - 1; size >= 0; size--) {
                            if (selectedIds.valueAt(size)) {
                                HelperMethods.transfer(new File((String) waImageAdapter.getItem(selectedIds.keyAt(size)).getPath()));
                            }
                        }
                        Toast.makeText(context, "Done! :)", Toast.LENGTH_SHORT).show();
                        mode.finish();//Finish action mode

                        return true;

                }
                return false;
        }
        return false;
    }


        @Override
        public void onDestroyActionMode (ActionMode mode){

            //When action mode destroyed remove selected selections and set action mode to null
            //First check current fragment action mode
            Fragment recyclerFragment;
            switch (s) {
                case "WAVideoFragment":
                    waVideoAdapter.removeSelection();  // remove selection
                    recyclerFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_wa_video);//Get recycler fragment
                    if (recyclerFragment != null)
                        ((WAVideoFragment) recyclerFragment).setNullToActionMode();//Set action mode null
                    break;
                case "BWAVideoFragment":
                    waVideoAdapter.removeSelection();  // remove selection
                    recyclerFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_wa_video);//Get recycler fragment
                    if (recyclerFragment != null)
                        ((BWAVideoFragment) recyclerFragment).setNullToActionMode();//Set action mode null
                    break;
                case "WAImageFragment":
                    waImageAdapter.removeSelection();  // remove selection
                    recyclerFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_wa_image);//Get recycler fragment
                    if (recyclerFragment != null)
                        ((WAImageFragment) recyclerFragment).setNullToActionMode();//Set action mode null
                    break;
                case "BWAImageFragment":
                    waImageAdapter.removeSelection();  // remove selection
                    recyclerFragment = ((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_wa_image);//Get recycler fragment
                    if (recyclerFragment != null)
                        ((BWAImageFragment) recyclerFragment).setNullToActionMode();//Set action mode null
                    break;

            }

        }
    }
