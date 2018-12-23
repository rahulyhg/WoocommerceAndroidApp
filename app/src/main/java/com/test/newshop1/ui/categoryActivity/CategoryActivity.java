package com.test.newshop1.ui.categoryActivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.google.android.material.tabs.TabLayout;
import com.test.newshop1.R;
import com.test.newshop1.data.database.category.Category;
import com.test.newshop1.ui.BaseActivity;
import com.test.newshop1.ui.ViewModelFactory;
import com.test.newshop1.ui.productListActivity.ProductListActivity;
import com.test.newshop1.utilities.InjectorUtil;

import java.util.List;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class CategoryActivity extends BaseActivity {

    private static final String TAG = "CategoryActivity";
    private static int LAST_PARENT_ID = -1;
    private static String LAST_PARENT_NAME;
    private CategoryViewModel mViewModel;
    private CategoryRecyclerViewAdapter categoryAdapter;
    private StaggeredGridLayoutManager layoutManager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        getSupportActionBar().setTitle("");

        tabLayout = findViewById(R.id.main_cat_tabs);
        RecyclerView recyclerView = findViewById(R.id.container_RV);

        ViewModelFactory factory = InjectorUtil.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(CategoryViewModel.class);
        mViewModel.loadCategories().observe(this, isLoaded -> {
            if (isLoaded != null && isLoaded){
                showSubCategories(0);
            }
        });

        categoryAdapter = new CategoryRecyclerViewAdapter();
        categoryAdapter.setOnItemClickListener(category -> {
            LAST_PARENT_ID = category.getParent();
            LAST_PARENT_NAME = category.getName();
            showSubCategories(category.getId());
        });
        layoutManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(categoryAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(
                searchManager != null ? searchManager.getSearchableInfo(getComponentName()) : null);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        return true;
    }

    @Override
    protected void onResume() {
        supportInvalidateOptionsMenu();
        super.onResume();
    }

    private void showSubCategories(int id) {

        List<Category> subCategories = mViewModel.getCategories(id);

        if (id == 0){
            for (Category category : subCategories) {

                tabLayout.addTab(tabLayout.newTab().setText(category.getName()));
                category.setSubCatTitles(mViewModel.getSubCatTitles(category.getId()));
            }
            layoutManager.setSpanCount(2);
        } else {
            layoutManager.setSpanCount(1);
        }

        if (subCategories.size() < 1) {
            updateLastParent();
            showProductList(id);
        } else {
            updateTitle();
            categoryAdapter.loadNewData(subCategories, true);
        }
    }

    private void updateTitle() {
        getSupportActionBar().setTitle(LAST_PARENT_NAME);
    }

    private void showProductList(int id) {

        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra(ProductListActivity.PARENT_ID, id);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        Log.d(TAG, "onBackPressed: " + LAST_PARENT_ID);
        if (LAST_PARENT_ID == -1 || drawer.isDrawerOpen(GravityCompat.START)) {
            finish();
            overridePendingTransition(0, R.anim.fade_out);
            //super.onBackPressed();

        } else {

            showSubCategories(LAST_PARENT_ID);
            updateLastParent();
        }
    }

    private void updateLastParent() {
        Category parentCategory = mViewModel.getParent(LAST_PARENT_ID);
        if (parentCategory != null) {
            LAST_PARENT_ID = parentCategory.getParent();
            LAST_PARENT_NAME = parentCategory.getName();
        } else {
            LAST_PARENT_ID = -1;
            LAST_PARENT_NAME = "";
        }
        updateTitle();
    }


}
