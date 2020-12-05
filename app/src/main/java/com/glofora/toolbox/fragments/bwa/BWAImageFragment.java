package com.glofora.toolbox.fragments.bwa;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.glofora.toolbox.GenericAdapter;
import com.glofora.toolbox.HelperMethods;
import com.glofora.toolbox.InstanceHandler;
import com.glofora.toolbox.R;
import com.glofora.toolbox.adapter.WAImageAdapter;
import com.glofora.toolbox.model.WAImageModel;
import com.glofora.toolbox.recycler.RecyclerClick_Listener;
import com.glofora.toolbox.recycler.RecyclerTouchListener;
import com.glofora.toolbox.recycler.ToolbarActionModeCallback;
import com.glofora.toolbox.viewer.ImageViewer;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by umer on 03-May-18.
 */

public class BWAImageFragment extends Fragment {
    private static BWAImageFragment mInstance;
    RecyclerView recyclerView;
    FragmentActivity activity;
    ProgressBar progressBar;
    FloatingActionButton fab;
    WAImageAdapter waImageAdapter;
    ArrayList<WAImageModel> arrayList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    private static View v;
    private ActionMode mActionMode;
    Fragment frg;
    FragmentTransaction ft=null;

    public BWAImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_wa_image, container, false);
        activity = getActivity();
        mInstance = this;
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.ref);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_wa_image);
        progressBar = (ProgressBar) v.findViewById(R.id.progressbar_wa);

        populateRecyclerView();
        implementRecyclerViewClickListeners();




        swipeRefreshLayout.setColorSchemeResources(new int[]{R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimaryDark});
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        fab = v.findViewById(R.id.wa_image_fab_save_all);
        fab.setOnClickListener(v -> {
            saveAll();

        });
        return v;
    }

    private void populateRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));
        getStatus();
        waImageAdapter = new WAImageAdapter(activity, arrayList);
        recyclerView.setAdapter(waImageAdapter);
        waImageAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    //Implement item click and long click over recycler view
    private void implementRecyclerViewClickListeners() {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerClick_Listener() {
            @Override
            public void onClick(View view, int position) {
                //If ActionMode not null select item
                if (mActionMode != null)
                    onListItemSelect(position);
                else {
                    String str = waImageAdapter.getItem(position).getPath();
                    try {
                        Intent intent = new Intent(getActivity(), ImageViewer.class);
                        intent.putExtra("pos", str);
                        intent.putExtra("position", position);
                        intent.putExtra("is_saved", false);
                        startActivityForResult(intent, 1);
                    } catch (Throwable e) {
                        throw new NoClassDefFoundError(e.getMessage());
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //Select item on long click
                mActionMode = null;
                onListItemSelect(position);
            }
        }));
    }


    //List item select method
    private void onListItemSelect(int position) {
        waImageAdapter.toggleSelection(position);//Toggle the selection
        List<Fragment> fragments;

        boolean hasCheckedItems = waImageAdapter.getSelectedCount() > 0;//Check if any items are already selected or not


        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ToolbarActionModeCallback(getActivity(), new GenericAdapter<WAImageAdapter>(waImageAdapter), arrayList, new InstanceHandler<BWAImageFragment>(mInstance)));
        }
        else if (!hasCheckedItems && mActionMode != null)
        // there no selected items, finish the actionMode
        {
            mActionMode.finish();
            mActionMode=null;
        }

        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(waImageAdapter
                    .getSelectedCount()) + " selected");


    }
    //Set action mode null after use
    public void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }

    private void saveAll() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);

        // set title
        alertDialogBuilder.setTitle("Save All Status");

        // set dialog message
        alertDialogBuilder
                .setMessage("This Action will Save all the available Image Statuses... \nDo you want to Continue?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    // if this button is clicked, close
                    // current activity
                    File[] listFiles = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp Business/Media/.Statuses/").toString()).listFiles();
                    if (waImageAdapter.getItemCount() == 0) {
                        Toast.makeText(activity, "No Status available to Save...", Toast.LENGTH_SHORT).show();
                    } else {
                        int i = 0;
                        while (i < listFiles.length) {
                            try {
                                File file = listFiles[i];
                                String str = file.getName().toString();
                                if (str.endsWith(".jpg") || str.endsWith(".jpeg") || str.endsWith(".png")) {
                                    HelperMethods helperMethods = new HelperMethods(activity.getApplicationContext());
                                    HelperMethods.transfer(file);
                                }
                                i++;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                        Toast.makeText(activity, "Done :)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel();
                });


        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black_overlay));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black_overlay));
            }
        });

        // show it
        alertDialog.show();
    }

    public void getStatus(){
        File[] listFiles = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp Business/Media/.Statuses/").toString()).listFiles();
        if (listFiles != null && listFiles.length >= 1) {
            Arrays.sort(listFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        }
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png")) {
                    WAImageModel model=new WAImageModel(file.getAbsolutePath());
                    arrayList.add(model);
                }
            }
        }
    }
    public void deleteRows() {
        SparseBooleanArray selected = waImageAdapter
                .getSelectedIds();//Get selected ids

        //Loop all selected ids
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)) {
                //If current id is selected remove the item via key
                arrayList.remove(selected.keyAt(i));
                waImageAdapter.notifyDataSetChanged();//notify adapter

            }
        }
        Toast.makeText(getActivity(), selected.size() + " item deleted.", Toast.LENGTH_SHORT).show();//Show Toast
        mActionMode.finish();//Finish action mode after use

    }
    public void refresh() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
//        waImageAdapter.notifyDataSetChanged();
        waImageAdapter.updateData(new ArrayList<WAImageModel>());
        populateRecyclerView();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null)  {
            if(resultCode == -1)
            {
                refresh();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                refresh();
            }
        }
    }

}