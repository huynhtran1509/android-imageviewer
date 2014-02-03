/*
 * Copyright (C) 2014 Snowdream Mobile <yanghui1986527@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.snowdream.android.apps.imageviewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import com.github.snowdream.android.util.Log;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import uk.co.senab.photoview.PhotoViewAttacher;


public class MainActivity extends ActionBarActivity implements PhotoViewAttacher.OnViewTapListener {
    private ImageView imageView = null;
    private String imageUri = null;
    private String fileName = null;
    private ImageLoader imageLoader = null;
    private PhotoViewAttacher attacher = null;
    private ShareActionProvider shareActionProvider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initData();
        loadData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this); // Add this method.
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this); // Add this method.
    }

    public void initUI() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        imageView = (ImageView) findViewById(R.id.imageView);
        attacher = new PhotoViewAttacher(imageView);
       // attacher.setOnViewTapListener(this);
    }

    public void initData() {
        imageLoader = ImageLoader.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = (Uri) intent.getData();
            if (uri != null) {
                imageUri = "file://" + uri.getEncodedPath();
                fileName = uri.getLastPathSegment();
                getSupportActionBar().setSubtitle(fileName);
                Log.i("The path of the image is: " + imageUri);
            }
        } else {
            Log.w("The intent is null!");
        }
    }

    public void loadData() {
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.ic_stub) // resource or drawable
//                .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
//                .showImageOnFail(R.drawable.ic_error) // resource or drawable
//                .resetViewBeforeLoading(false)  // default
//                .delayBeforeLoading(1000)
//                .cacheInMemory(false) // default
//                .cacheOnDisc(false) // default
//                .preProcessor(...)
//        .postProcessor(...)
//        .extraForDownloader(...)
//        .considerExifParams(false) // default
//                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
//                .bitmapConfig(Bitmap.Config.ARGB_8888) // default
//                .decodingOptions(...)
//        .displayer(new SimpleBitmapDisplayer()) // default
//                .handler(new Handler()) // default
//                .build();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(false)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        imageLoader.displayImage(imageUri, imageView, options, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        Log.i("onLoadingStarted");
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Log.e("onLoadingFailed");
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        Log.i("onLoadingComplete");
                        attacher.update();
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Log.w("onLoadingCancelled");
                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                        Log.i("onProgressUpdate " + current + "/" + "total");
                    }
                }
        );
    }

    public void onViewTap(View view, float x, float y) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar.isShowing()) {
            actionBar.hide();
        } else {
            actionBar.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem itemShare = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(itemShare);
        // Set history different from the default before getting the action
        // view since a call to MenuItemCompat.getActionView() calls
        // onCreateActionView() which uses the backing file name. Omit this
        // line if using the default share history file is desired.
        shareActionProvider.setShareHistoryFileName("snowdream_android_imageviewer_share_history.xml");
        Intent shareIntent = createShareIntent();
        if (shareIntent != null) {
            doShare(shareIntent);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            doEdit();
        } else if (id == R.id.action_share) {
            Intent shareIntent = createShareIntent();
            if (shareIntent != null) {
                doShare(shareIntent);
            }
            return true;
        } else if (id == R.id.action_settings) {
            doSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent createShareIntent() {
        Intent shareIntent = null;

        if (!TextUtils.isEmpty(imageUri)) {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            Uri uri = Uri.parse(imageUri);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        return shareIntent;
    }

    public void doShare(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void doSettings() {
        if (!TextUtils.isEmpty(imageUri)) {
            Uri uri = Uri.parse(imageUri);
            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
            intent.setDataAndType(uri, "image/jpg");
            intent.putExtra("mimeType", "image/jpg");
            startActivityForResult(Intent.createChooser(intent, getText(R.string.action_settings)), 200);
        }
    }

    public void doEdit() {

    }


    }
