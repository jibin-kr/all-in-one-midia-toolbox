package com.glofora.whatstustoolbox.whatsappstickers.activities;

import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.glofora.whatstustoolbox.R;
import com.glofora.whatstustoolbox.whatsappstickers.identities.StickerPacksContainer;
import com.glofora.whatstustoolbox.whatsappstickers.utils.StickerPacksManager;

public class AddToStickerPackActivity extends AppCompatActivity {

    Uri stickerUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_sticker_pack);
        this.stickerUri = this.getIntent().getData();
        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toast.makeText(this, stickerUri.getPath(), Toast.LENGTH_LONG).show();
    }
}